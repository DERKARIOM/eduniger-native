package com.naniger.elim.model.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cache des réponses de l'API (JSON) : mémoire (LRU) + disque, cloisonné par compte.
 *
 * <p>Code Java pur (aucune dépendance Android) : testable en test unitaire JVM.
 *
 * <ul>
 *   <li><b>Mémoire</b> : LRU limité en nombre de caractères (évite de garder des listes
 *       énormes) ; vidé quand le compte change.</li>
 *   <li><b>Disque</b> : un fichier par clé, dans {@code racine/<sha256(compte)>/}. Écriture
 *       atomique (fichier temporaire puis renommage) : une coupure en cours d'écriture ne
 *       laisse jamais un fichier corrompu. Taille totale plafonnée ; les entrées les plus
 *       anciennes sont supprimées en premier.</li>
 *   <li><b>Cloisonnement</b> : les données d'un compte ne sont jamais lues par un autre
 *       (dossier différent + mémoire vidée au changement de compte).</li>
 *   <li><b>Pas d'écrasement par une réponse plus ancienne</b> : chaque entrée garde l'heure
 *       à laquelle sa requête a été LANCÉE ; une réponse lancée avant l'entrée existante
 *       est ignorée.</li>
 * </ul>
 *
 * <p>La racine doit être un dossier réservé au cache (ex. {@code getCacheDir()/elim_api_cache}) :
 * les livres téléchargés par l'utilisateur sont ailleurs et ne sont jamais touchés.
 */
public class CacheStore {

    /** Horloge injectable (tests). */
    public interface Clock {
        long now();
    }

    /** Entrée lue dans le cache. */
    public static final class Entry {
        public final String body;
        /** Heure de lancement de la requête qui a produit ce contenu (ms). */
        public final long fetchedAt;

        Entry(String body, long fetchedAt) {
            this.body = body;
            this.fetchedAt = fetchedAt;
        }

        public long ageMs(long now) {
            return Math.max(0, now - fetchedAt);
        }

        public boolean isFresh(long ttlMs, long now) {
            return ageMs(now) < ttlMs;
        }
    }

    private static final String SUFFIX = ".json";

    private final File mRoot;
    private final long mMaxDiskBytes;
    private final int mMaxMemoryChars;
    private final Clock mClock;

    private String mScope = "anonymous";
    private int mMemoryChars = 0;
    private final LinkedHashMap<String, Entry> mMemory = new LinkedHashMap<>(16, 0.75f, true);

    public CacheStore(File root, long maxDiskBytes, int maxMemoryChars, Clock clock) {
        mRoot = root;
        mMaxDiskBytes = maxDiskBytes;
        mMaxMemoryChars = maxMemoryChars;
        mClock = clock;
    }

    public Clock clock() {
        return mClock;
    }

    /**
     * Compte courant. Changer de compte vide le cache mémoire : les données du compte
     * précédent ne peuvent plus être servies.
     */
    public synchronized void setScope(String userId) {
        String scope = (userId == null || userId.trim().isEmpty()) ? "anonymous" : userId.trim();
        if (!scope.equals(mScope)) {
            mScope = scope;
            mMemory.clear();
            mMemoryChars = 0;
        }
    }

    public synchronized Entry get(String key) {
        Entry e = mMemory.get(key);
        if (e != null) return e;
        File f = fileFor(key);
        if (!f.isFile()) return null;
        try {
            String raw = readFile(f);
            int nl = raw.indexOf('\n');
            if (nl <= 0) return null;
            long fetchedAt = Long.parseLong(raw.substring(0, nl).trim());
            e = new Entry(raw.substring(nl + 1), fetchedAt);
            remember(key, e);
            return e;
        } catch (IOException | NumberFormatException ex) {
            // Fichier illisible : on l'écarte plutôt que de servir un contenu douteux.
            //noinspection ResultOfMethodCallIgnored
            f.delete();
            return null;
        }
    }

    /**
     * Enregistre une réponse VALIDE (la validation est faite par l'appelant).
     *
     * @param requestStartedAt heure de lancement de la requête.
     * @return false si une entrée plus récente existe déjà (réponse obsolète ignorée).
     */
    public synchronized boolean put(String key, String body, long requestStartedAt) {
        if (body == null) return false;
        Entry current = get(key);
        if (current != null && current.fetchedAt > requestStartedAt) return false;

        Entry e = new Entry(body, requestStartedAt);
        remember(key, e);
        File f = fileFor(key);
        File dir = f.getParentFile();
        if (dir != null && !dir.isDirectory() && !dir.mkdirs()) return true; // mémoire seule
        File tmp = new File(dir, f.getName() + ".tmp");
        try (OutputStream out = new FileOutputStream(tmp)) {
            out.write((requestStartedAt + "\n" + body).getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            //noinspection ResultOfMethodCallIgnored
            tmp.delete();
            return true;
        }
        if (!tmp.renameTo(f)) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
            //noinspection ResultOfMethodCallIgnored
            tmp.renameTo(f);
        }
        trimDisk();
        return true;
    }

    /** Vide le cache du compte courant (mémoire + disque). */
    public synchronized void clearScope() {
        mMemory.clear();
        mMemoryChars = 0;
        deleteRecursively(scopeDir());
    }

    /** Vide tout le cache API (tous comptes) — à la déconnexion. Ne touche qu'à la racine du cache. */
    public synchronized void clearAll() {
        mMemory.clear();
        mMemoryChars = 0;
        deleteRecursively(mRoot);
    }

    /** Taille actuelle du cache disque (tous comptes), en octets. */
    public synchronized long diskSize() {
        long total = 0;
        for (File f : allFiles()) total += f.length();
        return total;
    }

    // ------------------------------------------------------------------ interne

    private void remember(String key, Entry e) {
        Entry old = mMemory.put(key, e);
        if (old != null) mMemoryChars -= old.body.length();
        mMemoryChars += e.body.length();
        Iterator<Map.Entry<String, Entry>> it = mMemory.entrySet().iterator();
        while (mMemoryChars > mMaxMemoryChars && it.hasNext()) {
            Map.Entry<String, Entry> eldest = it.next();
            if (eldest.getKey().equals(key) && mMemory.size() == 1) break;
            mMemoryChars -= eldest.getValue().body.length();
            it.remove();
        }
    }

    private void trimDisk() {
        File[] files = allFiles();
        long total = 0;
        for (File f : files) total += f.length();
        if (total <= mMaxDiskBytes) return;
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        for (File f : files) {
            if (total <= mMaxDiskBytes) break;
            total -= f.length();
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }

    private File[] allFiles() {
        java.util.List<File> out = new java.util.ArrayList<>();
        File[] scopes = mRoot.listFiles();
        if (scopes != null) {
            for (File s : scopes) {
                File[] fs = s.listFiles((d, n) -> n.endsWith(SUFFIX));
                if (fs != null) out.addAll(Arrays.asList(fs));
            }
        }
        return out.toArray(new File[0]);
    }

    private File scopeDir() {
        return new File(mRoot, sha256(mScope));
    }

    private File fileFor(String key) {
        return new File(scopeDir(), sha256(key) + SUFFIX);
    }

    static String sha256(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i++) sb.append(String.format("%02x", d[i]));
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(s.hashCode());
        }
    }

    private static String readFile(File f) throws IOException {
        try (InputStream in = new FileInputStream(f)) {
            byte[] buf = new byte[(int) f.length()];
            int off = 0, n;
            while (off < buf.length && (n = in.read(buf, off, buf.length - off)) > 0) off += n;
            return new String(buf, 0, off, StandardCharsets.UTF_8);
        }
    }

    private static void deleteRecursively(File f) {
        if (f == null || !f.exists()) return;
        File[] children = f.listFiles();
        if (children != null) for (File c : children) deleteRecursively(c);
        //noinspection ResultOfMethodCallIgnored
        f.delete();
    }
}
