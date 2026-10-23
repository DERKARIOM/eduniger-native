package com.naniger.elim.model.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Chargement « cache d'abord, actualisation ensuite » (stale-while-revalidate).
 *
 * <ol>
 *   <li>Si une donnée est en cache, elle est livrée IMMÉDIATEMENT ({@link Callback#onCached}),
 *       sans attendre le réseau.</li>
 *   <li>Si elle est encore fraîche (âge &lt; durée de validité) et que l'actualisation n'est pas
 *       forcée, aucun appel réseau n'est fait.</li>
 *   <li>Sinon la requête part en arrière-plan. Une réponse valide (HTTP 2xx + validateur) est
 *       enregistrée puis livrée ({@link Callback#onFresh}). Une erreur réseau, un code HTTP
 *       d'erreur ou un contenu invalide/vide ne remplacent JAMAIS la donnée en cache
 *       ({@link Callback#onFailure}).</li>
 *   <li>Deux écrans qui demandent la même donnée en même temps partagent une seule requête.</li>
 * </ol>
 *
 * <p>Code Java pur : le réseau ({@link Fetcher}), le thread principal ({@link Poster}) et
 * l'exécuteur sont injectés, ce qui permet de tester tous les scénarios sur la JVM.
 */
public class CachedLoader {

    /** Réponse HTTP brute. */
    public static final class Response {
        public final int code;
        public final String body;

        public Response(int code, String body) {
            this.code = code;
            this.body = body;
        }

        boolean isSuccessful() {
            return code >= 200 && code < 300;
        }
    }

    public interface Fetcher {
        Response fetch(String url) throws IOException;
    }

    public interface Validator {
        boolean isValid(String body);
    }

    /** Exécute sur le thread principal (Handler Android ; immédiat en test). */
    public interface Poster {
        void post(Runnable r);
    }

    public interface Callback {
        /** Donnée du cache, livrée tout de suite (éventuellement périmée). */
        void onCached(String body, long ageMs);

        /** Donnée fraîche, reçue du serveur et enregistrée. */
        void onFresh(String body);

        /**
         * Échec réseau ou réponse invalide.
         *
         * @param hadCache true si une donnée en cache a déjà été livrée : l'écran garde
         *                 alors son contenu (affichage hors ligne), sinon il affiche l'état
         *                 « pas de connexion » avec « Réessayer ».
         */
        void onFailure(boolean hadCache);

        /**
         * La ressource n'existe pas (HTTP 404) : identifiant invalide ou contenu supprimé.
         * L'entrée de cache éventuelle est retirée. Par défaut : traité comme un échec.
         */
        default void onNotFound() {
            onFailure(false);
        }
    }

    /** Callback vide pour un préchargement sans affichage. */
    public static class SimpleCallback implements Callback {
        @Override public void onCached(String body, long ageMs) { }
        @Override public void onFresh(String body) { }
        @Override public void onFailure(boolean hadCache) { }
    }

    private final CacheStore mStore;
    private final Fetcher mFetcher;
    private final Executor mExecutor;
    private final Poster mPoster;

    /** Requêtes en cours, par clé : les demandes identiques s'y rattachent. */
    private final Map<String, List<Callback>> mInFlight = new HashMap<>();
    private volatile int mNetworkCalls = 0;

    public CachedLoader(CacheStore store, Fetcher fetcher, Executor executor, Poster poster) {
        mStore = store;
        mFetcher = fetcher;
        mExecutor = executor;
        mPoster = poster;
    }

    public CacheStore store() {
        return mStore;
    }

    /** Nombre d'appels réseau réellement effectués (mesure / tests). */
    public int networkCalls() {
        return mNetworkCalls;
    }

    /**
     * @param key          clé de cache (en pratique l'URL, paramètres compris)
     * @param url          URL appelée
     * @param ttlMs        durée pendant laquelle la donnée est considérée fraîche
     * @param forceRefresh true pour le « tirer pour actualiser » : appelle le réseau même
     *                     si le cache est frais (le cache reste affiché en attendant)
     */
    public void load(String key, String url, long ttlMs, boolean forceRefresh,
                     Validator validator, Callback callback) {
        mExecutor.execute(() -> {
            CacheStore.Entry cached = mStore.get(key);
            long now = mStore.clock().now();
            if (cached != null) {
                final String body = cached.body;
                final long age = cached.ageMs(now);
                mPoster.post(() -> callback.onCached(body, age));
                if (!forceRefresh && cached.isFresh(ttlMs, now)) return;
            }
            final boolean hadCache = cached != null;

            synchronized (mInFlight) {
                List<Callback> waiting = mInFlight.get(key);
                if (waiting != null) {           // même requête déjà en cours
                    waiting.add(wrap(callback, hadCache));
                    return;
                }
                List<Callback> list = new ArrayList<>();
                list.add(wrap(callback, hadCache));
                mInFlight.put(key, list);
            }

            long startedAt = mStore.clock().now();
            String fresh = null;
            boolean notFound = false;
            try {
                mNetworkCalls++;
                Response r = mFetcher.fetch(url);
                if (r != null && r.code == 404) {
                    notFound = true;
                    mStore.remove(key);   // ne plus servir une ressource supprimée
                } else if (r != null && r.isSuccessful() && r.body != null
                        && (validator == null || validator.isValid(r.body))) {
                    fresh = r.body;
                    // false = une réponse plus récente a déjà été enregistrée : on garde la plus récente.
                    if (!mStore.put(key, fresh, startedAt)) {
                        CacheStore.Entry newer = mStore.get(key);
                        if (newer != null) fresh = newer.body;
                    }
                }
            } catch (IOException | RuntimeException ignored) {
                // réseau indisponible, délai dépassé, réponse illisible... : on garde le cache
            }

            List<Callback> toNotify;
            synchronized (mInFlight) {
                toNotify = mInFlight.remove(key);
            }
            if (toNotify == null) return;
            for (Callback cb : toNotify) {
                if (notFound) {
                    mPoster.post(cb::onNotFound);
                } else if (fresh != null) {
                    final String body = fresh;
                    mPoster.post(() -> cb.onFresh(body));
                } else {
                    mPoster.post(() -> cb.onFailure(false)); // hadCache est porté par wrap()
                }
            }
        });
    }

    /** Lecture du cache seul (sans réseau), ex. pour un premier affichage synchrone. */
    public CacheStore.Entry peek(String key) {
        return mStore.get(key);
    }

    /** Associe à chaque abonné sa propre information « avait du cache ». */
    private static Callback wrap(Callback cb, boolean hadCache) {
        return new Callback() {
            @Override public void onCached(String body, long ageMs) { cb.onCached(body, ageMs); }
            @Override public void onFresh(String body) { cb.onFresh(body); }
            @Override public void onFailure(boolean ignored) { cb.onFailure(hadCache); }
            @Override public void onNotFound() { cb.onNotFound(); }
        };
    }
}
