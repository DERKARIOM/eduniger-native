package com.naniger.elim.model.cache;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.naniger.elim.model.net.ApiClient;
import com.naniger.elim.model.table.Session;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Point d'entrée Android du cache de l'API (singleton partagé par tous les écrans).
 *
 * <ul>
 *   <li>Stockage : {@code getCacheDir()/elim_api_cache/} — dossier de CACHE, distinct des
 *       livres téléchargés ({@code getExternalFilesDir()}) qui ne sont jamais touchés.
 *       Android peut le vider en cas de manque d'espace : ce n'est qu'un cache d'affichage.</li>
 *   <li>Limites : 5 Mo sur disque, ~1 M de caractères en mémoire.</li>
 *   <li>Cloisonnement : portée = identifiant du compte connecté ; vidé à la déconnexion
 *       ({@link #clearAll(Context)}). Seules les réponses JSON sont stockées, jamais le jeton
 *       (porté par l'en-tête Authorization, ajouté par ApiClient).</li>
 *   <li>Réseau : client OkHttp partagé (jeton, renouvellement sur 401, délais 15 s / 20 s).
 *       Aucun test de connectivité préalable : un réseau « connecté » sans Internet se
 *       traduit simplement par un échec, et le cache reste affiché.</li>
 * </ul>
 */
public final class ApiCache {

    private static final String TAG = "ApiCache";

    /** Durées de validité (au-delà : affichage du cache + actualisation en arrière-plan). */
    public static final long TTL_RECOMMENDATIONS = 5 * 60_000L;
    public static final long TTL_STRUCTURES = 10 * 60_000L;
    public static final long TTL_AUTHORS = 30 * 60_000L;
    public static final long TTL_CATEGORIES = 30 * 60_000L;

    private static final long MAX_DISK_BYTES = 5L * 1024 * 1024;
    private static final int MAX_MEMORY_CHARS = 1_000_000;

    private static volatile CachedLoader sLoader;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    /** Réponse attendue : tableau JSON (listes de livres, structures, auteurs, catégories). */
    public static final CachedLoader.Validator JSON_ARRAY = body -> {
        try {
            new JSONArray(body);
            return true;
        } catch (Exception e) {
            return false;
        }
    };

    /** Réponse attendue : objet JSON sans champ d'erreur Laravel. */
    public static final CachedLoader.Validator JSON_OBJECT = body -> {
        try {
            JSONObject o = new JSONObject(body);
            return !o.has("exception") && !(o.has("message") && o.length() == 1);
        } catch (Exception e) {
            return false;
        }
    };

    private ApiCache() {
    }

    public static CachedLoader get(Context context) {
        if (sLoader == null) {
            synchronized (ApiCache.class) {
                if (sLoader == null) {
                    Context app = context.getApplicationContext();
                    CacheStore store = new CacheStore(new File(app.getCacheDir(), "elim_api_cache"),
                            MAX_DISK_BYTES, MAX_MEMORY_CHARS, System::currentTimeMillis);
                    CachedLoader.Fetcher fetcher = url -> {
                        Request request = new Request.Builder().url(url).get()
                                .header("Accept", "application/json").build();
                        try (Response r = ApiClient.getInstance(app).newCall(request).execute()) {
                            String body = r.body() != null ? r.body().string() : null;
                            return new CachedLoader.Response(r.code(), body);
                        }
                    };
                    sLoader = new CachedLoader(store, fetcher, EXECUTOR, MAIN::post);
                }
            }
        }
        sLoader.store().setScope(currentUser(context));
        return sLoader;
    }

    /** Chargement « cache d'abord » d'une liste JSON (GET). */
    public static void loadList(Context context, String url, long ttlMs, boolean forceRefresh,
                                CachedLoader.Callback callback) {
        get(context).load(url, url, ttlMs, forceRefresh, JSON_ARRAY, callback);
    }

    /** À appeler à la déconnexion : aucune donnée du compte ne doit rester pour le suivant. */
    public static void clearAll(Context context) {
        try {
            // Synchrone (quelques fichiers, ≤ 5 Mo) : rien ne doit subsister quand le
            // compte suivant se connecte.
            get(context).store().clearAll();
        } catch (Exception e) {
            Log.w(TAG, "Vidage du cache impossible : " + e.getMessage());
        }
    }

    private static String currentUser(Context context) {
        try {
            return new Session(context.getApplicationContext()).getIdNumber();
        } catch (Exception e) {
            return null; // pas de session : portée « anonyme »
        }
    }
}
