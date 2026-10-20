package com.naniger.elim;

import android.app.Application;
import android.util.Log;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Initialisation globale. Configure ici le cache d'IMAGES de Picasso (couvertures, logos,
 * photos d'auteurs, bannières) pour l'affichage rapide et hors ligne.
 *
 * <p>Avant : Picasso suivait les en-têtes HTTP du serveur (au mieux 1 h pour les couvertures,
 * rien pour les autres) ; hors ligne, une image expirée ne s'affichait plus.
 * Désormais :
 * <ul>
 *   <li>cache disque dédié de 50 Mo ({@code getCacheDir()/elim-images}), nettoyé
 *       automatiquement (LRU d'OkHttp) — séparé des livres téléchargés ;</li>
 *   <li>une image téléchargée est réutilisée 7 jours sans nouvel appel réseau ;</li>
 *   <li>sans réseau, l'image déjà vue est servie depuis le disque même au-delà de 7 jours ;</li>
 *   <li>seules les images effectivement affichées sont mises en cache (aucun préchargement
 *       massif de la bibliothèque).</li>
 * </ul>
 * Glide (utilisé par quelques écrans) a déjà son propre cache mémoire + disque (250 Mo).
 */
public class ElimApplication extends Application {

    private static final long IMAGE_CACHE_BYTES = 50L * 1024 * 1024;
    private static final int IMAGE_MAX_AGE_SECONDS = 7 * 24 * 3600;

    @Override
    public void onCreate() {
        super.onCreate();
        setupImageCache();
    }

    private void setupImageCache() {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .cache(new Cache(new File(getCacheDir(), "elim-images"), IMAGE_CACHE_BYTES))
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    // Hors ligne : on retente depuis le cache disque, même périmé.
                    .addInterceptor(chain -> {
                        Request request = chain.request();
                        try {
                            return chain.proceed(request);
                        } catch (IOException networkError) {
                            Response cached = chain.proceed(request.newBuilder()
                                    .cacheControl(CacheControl.FORCE_CACHE).build());
                            if (cached.code() == 504) {   // absent du cache
                                cached.close();
                                throw networkError;
                            }
                            return cached;
                        }
                    })
                    // Images reçues : conservées 7 jours (le serveur envoie no-cache ou 1 h).
                    .addNetworkInterceptor(chain -> {
                        Response response = chain.proceed(chain.request());
                        String type = response.header("Content-Type", "");
                        if (response.isSuccessful() && type != null && type.startsWith("image/")) {
                            return response.newBuilder()
                                    .removeHeader("Pragma")
                                    .header("Cache-Control", "public, max-age=" + IMAGE_MAX_AGE_SECONDS)
                                    .build();
                        }
                        return response;
                    })
                    .build();

            Picasso picasso = new Picasso.Builder(this)
                    .downloader(new OkHttp3Downloader(client))
                    .build();
            Picasso.setSingletonInstance(picasso);
        } catch (IllegalStateException alreadySet) {
            Log.w("ElimApplication", "Picasso déjà initialisé : cache d'images par défaut conservé.");
        }
    }
}
