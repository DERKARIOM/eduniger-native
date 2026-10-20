package com.naniger.elim.model.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scénarios de la mission « cache intelligent et mode hors ligne » (tests JVM, sans Android).
 * Les numéros renvoient à la liste des tests demandés.
 */
public class CacheTest {

    private static final String KEY = "https://api.example/api/recommendations";
    private static final long TTL = 5 * 60_000L;
    private static final String DATA_V1 = "[{\"idBook\":\"1\"}]";
    private static final String DATA_V2 = "[{\"idBook\":\"1\"},{\"idBook\":\"2\"}]";

    private File mTmp;
    private File mCacheRoot;
    private long mNow = 1_000_000L;
    private final CacheStore.Clock mClock = () -> mNow;

    /** Réseau simulé, programmable. */
    private final class FakeNet implements CachedLoader.Fetcher {
        volatile CachedLoader.Response next = new CachedLoader.Response(200, DATA_V1);
        volatile boolean offline = false;
        volatile CountDownLatch gate = null;
        final AtomicInteger calls = new AtomicInteger();

        @Override
        public CachedLoader.Response fetch(String url) throws IOException {
            calls.incrementAndGet();
            if (gate != null) {
                try { gate.await(5, TimeUnit.SECONDS); } catch (InterruptedException ignored) { }
            }
            if (offline) throw new IOException("mode avion");
            return next;
        }
    }

    /** Enregistre ce que reçoit l'écran. */
    private static class Screen implements CachedLoader.Callback {
        final List<String> events = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch done = new CountDownLatch(1);
        volatile String shown;

        @Override public void onCached(String body, long ageMs) { events.add("cache"); shown = body; }
        @Override public void onFresh(String body) { events.add("fresh"); shown = body; done.countDown(); }
        @Override public void onFailure(boolean hadCache) { events.add(hadCache ? "fail-cache" : "fail-empty"); done.countDown(); }
    }

    private final CachedLoader.Validator jsonArray = b -> b.trim().startsWith("[") && b.trim().endsWith("]");

    @Before
    public void setUp() throws IOException {
        mTmp = Files.createTempDirectory("elim-cache-test").toFile();
        mCacheRoot = new File(mTmp, "cache/elim_api_cache");
    }

    @After
    public void tearDown() {
        delete(mTmp);
    }

    private CacheStore newStore(String user) {
        CacheStore s = new CacheStore(mCacheRoot, 5 * 1024 * 1024, 200_000, mClock);
        s.setScope(user);
        return s;
    }

    /** Exécuteur synchrone : tout se déroule dans l'appel load(). */
    private CachedLoader syncLoader(CacheStore store, FakeNet net) {
        return new CachedLoader(store, net, Runnable::run, Runnable::run);
    }

    // 1. Première ouverture avec Internet : récupération puis mise en cache.
    @Test
    public void firstOpen_fetchesAndCaches() {
        FakeNet net = new FakeNet();
        CachedLoader loader = syncLoader(newStore("u1"), net);
        Screen s = new Screen();
        loader.load(KEY, KEY, TTL, false, jsonArray, s);
        assertEquals(Collections.singletonList("fresh"), s.events);
        assertEquals(1, net.calls.get());
        assertNotNull(newStore("u1").get(KEY));
    }

    // 2. Deuxième ouverture : affichage immédiat depuis le cache, pas d'appel si frais.
    @Test
    public void secondOpen_servedFromCacheWithoutNetwork() {
        FakeNet net = new FakeNet();
        CacheStore store = newStore("u1");
        syncLoader(store, net).load(KEY, KEY, TTL, false, jsonArray, new Screen());
        mNow += 60_000;                     // 1 min plus tard : encore frais
        Screen s = new Screen();
        syncLoader(store, net).load(KEY, KEY, TTL, false, jsonArray, s);
        assertEquals(Collections.singletonList("cache"), s.events);
        assertEquals(1, net.calls.get());
    }

    // 3. Fermeture forcée puis redémarrage : nouvelle instance (mémoire perdue), disque conservé.
    @Test
    public void restart_dataStillOnDisk() {
        syncLoader(newStore("u1"), new FakeNet()).load(KEY, KEY, TTL, false, jsonArray, new Screen());
        CacheStore afterRestart = newStore("u1");
        CacheStore.Entry e = afterRestart.get(KEY);
        assertNotNull(e);
        assertEquals(DATA_V1, e.body);
    }

    // 4 et 6. Mode avion / API indisponible : le cache reste affiché.
    @Test
    public void offline_showsCachedData() {
        FakeNet net = new FakeNet();
        CacheStore store = newStore("u1");
        syncLoader(store, net).load(KEY, KEY, TTL, false, jsonArray, new Screen());
        mNow += TTL + 1;                    // périmé : une actualisation sera tentée
        net.offline = true;
        Screen s = new Screen();
        syncLoader(store, net).load(KEY, KEY, TTL, false, jsonArray, s);
        assertEquals(java.util.Arrays.asList("cache", "fail-cache"), s.events);
        assertEquals(DATA_V1, s.shown);
    }

    @Test
    public void serverError_keepsCachedData() {
        FakeNet net = new FakeNet();
        CacheStore store = newStore("u1");
        syncLoader(store, net).load(KEY, KEY, TTL, false, jsonArray, new Screen());
        mNow += TTL + 1;
        net.next = new CachedLoader.Response(500, "<html>Erreur serveur</html>");
        Screen s = new Screen();
        syncLoader(store, net).load(KEY, KEY, TTL, false, jsonArray, s);
        assertEquals(java.util.Arrays.asList("cache", "fail-cache"), s.events);
        assertEquals(DATA_V1, store.get(KEY).body);
    }

    // 5. Connexion lente : le cache est livré AVANT la fin de la requête réseau.
    @Test
    public void slowNetwork_cacheShownBeforeNetworkAnswers() throws Exception {
        FakeNet net = new FakeNet();
        CacheStore store = newStore("u1");
        syncLoader(store, net).load(KEY, KEY, TTL, false, jsonArray, new Screen());
        mNow += TTL + 1;
        net.gate = new CountDownLatch(1);   // le serveur « traîne »
        net.next = new CachedLoader.Response(200, DATA_V2);
        ExecutorService bg = Executors.newSingleThreadExecutor();
        CountDownLatch cachedShown = new CountDownLatch(1);
        Screen s = new Screen() {
            @Override public void onCached(String body, long ageMs) { super.onCached(body, ageMs); cachedShown.countDown(); }
        };
        new CachedLoader(store, net, bg, Runnable::run).load(KEY, KEY, TTL, false, jsonArray, s);
        assertTrue("le cache doit s'afficher sans attendre le serveur", cachedShown.await(2, TimeUnit.SECONDS));
        assertEquals(DATA_V1, s.shown);
        net.gate.countDown();               // le serveur répond enfin
        assertTrue(s.done.await(2, TimeUnit.SECONDS));
        assertEquals(DATA_V2, s.shown);
        bg.shutdownNow();
    }

    // 7. Cache vide et pas de réseau : état « hors ligne » (pas de donnée).
    @Test
    public void emptyCacheOffline_reportsNoData() {
        FakeNet net = new FakeNet();
        net.offline = true;
        Screen s = new Screen();
        syncLoader(newStore("u1"), net).load(KEY, KEY, TTL, false, jsonArray, s);
        assertEquals(Collections.singletonList("fail-empty"), s.events);
        assertNull(s.shown);
    }

    // 8. Retour d'Internet : actualisation correcte.
    @Test
    public void networkBack_refreshes() {
        FakeNet net = new FakeNet();
        CacheStore store = newStore("u1");
        syncLoader(store, net).load(KEY, KEY, TTL, false, jsonArray, new Screen());
        net.offline = true;
        syncLoader(store, net).load(KEY, KEY, TTL, true, jsonArray, new Screen());
        net.offline = false;
        net.next = new CachedLoader.Response(200, DATA_V2);
        Screen s = new Screen();
        syncLoader(store, net).load(KEY, KEY, TTL, true, jsonArray, s);
        assertEquals(java.util.Arrays.asList("cache", "fresh"), s.events);
        assertEquals(DATA_V2, store.get(KEY).body);
    }

    // 9. Réponses vides ou erronées : les bonnes données ne sont pas perdues.
    @Test
    public void invalidResponses_doNotOverwriteCache() {
        FakeNet net = new FakeNet();
        CacheStore store = newStore("u1");
        syncLoader(store, net).load(KEY, KEY, TTL, false, jsonArray, new Screen());
        String[] bad = {"", "<html>502 Bad Gateway</html>", "{\"message\":\"Unauthenticated.\"}"};
        for (String b : bad) {
            net.next = new CachedLoader.Response(200, b);
            syncLoader(store, net).load(KEY, KEY, TTL, true, jsonArray, new Screen());
        }
        net.next = new CachedLoader.Response(401, "{\"message\":\"Unauthenticated.\"}");
        syncLoader(store, net).load(KEY, KEY, TTL, true, jsonArray, new Screen());
        assertEquals(DATA_V1, store.get(KEY).body);
        assertEquals(DATA_V1, newStore("u1").get(KEY).body);
    }

    // Réponse ancienne qui arrive après une plus récente : ignorée.
    @Test
    public void olderResponse_doesNotOverwriteNewer() {
        CacheStore store = newStore("u1");
        assertTrue(store.put(KEY, DATA_V2, 2_000L));
        assertFalse(store.put(KEY, DATA_V1, 1_000L));
        assertEquals(DATA_V2, store.get(KEY).body);
    }

    // 10. Plusieurs écrans en même temps : une seule requête.
    @Test
    public void concurrentScreens_shareOneRequest() throws Exception {
        FakeNet net = new FakeNet();
        net.gate = new CountDownLatch(1);
        ExecutorService bg = Executors.newFixedThreadPool(3);
        CachedLoader loader = new CachedLoader(newStore("u1"), net, bg, Runnable::run);
        Screen a = new Screen(), b = new Screen(), c = new Screen();
        loader.load(KEY, KEY, TTL, false, jsonArray, a);
        Thread.sleep(150);                  // la 1re requête est en vol
        loader.load(KEY, KEY, TTL, false, jsonArray, b);
        loader.load(KEY, KEY, TTL, false, jsonArray, c);
        Thread.sleep(150);
        net.gate.countDown();
        assertTrue(a.done.await(2, TimeUnit.SECONDS));
        assertTrue(b.done.await(2, TimeUnit.SECONDS));
        assertTrue(c.done.await(2, TimeUnit.SECONDS));
        assertEquals(1, net.calls.get());
        assertEquals(DATA_V1, b.shown);
        bg.shutdownNow();
    }

    // 11. Nettoyage du cache : les téléchargements personnels restent intacts.
    @Test
    public void clearCache_keepsUserDownloads() throws IOException {
        File downloads = new File(mTmp, "files");   // équivalent de getExternalFilesDir(null)
        assertTrue(downloads.mkdirs());
        File book = new File(downloads, "ELIMA0001_ch1.mp3");
        try (FileOutputStream o = new FileOutputStream(book)) { o.write(new byte[1024]); }
        CacheStore store = newStore("u1");
        store.put(KEY, DATA_V1, 1L);
        store.clearAll();
        assertNull(newStore("u1").get(KEY));
        assertTrue(book.isFile());
        assertEquals(1024, book.length());
    }

    // Limite de taille disque : les plus anciennes entrées sont supprimées.
    @Test
    public void diskLimit_isEnforced() {
        CacheStore store = new CacheStore(mCacheRoot, 2_000, 200_000, mClock);
        store.setScope("u1");
        StringBuilder big = new StringBuilder("[");
        for (int i = 0; i < 300; i++) big.append('1');
        big.append(']');
        for (int i = 0; i < 20; i++) store.put("k" + i, big.toString(), i);
        assertTrue(store.diskSize() <= 2_000);
    }

    // 12. Changement de compte : aucune donnée du compte précédent.
    @Test
    public void accountSwitch_isolatesData() {
        CacheStore store = newStore("compte-A");
        store.put(KEY, DATA_V1, 1L);
        store.setScope("compte-B");
        assertNull(store.get(KEY));
        store.put(KEY, DATA_V2, 2L);
        store.setScope("compte-A");
        assertEquals(DATA_V1, store.get(KEY).body);
    }

    private static void delete(File f) {
        File[] c = f.listFiles();
        if (c != null) for (File x : c) delete(x);
        //noinspection ResultOfMethodCallIgnored
        f.delete();
    }
}
