package com.ninotech.eduniger.model.net;

import android.content.Context;

import com.ninotech.eduniger.model.data.TokenStore;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Fournit un OkHttpClient PARTAGE et pre-configure avec :
 *  - l'ajout automatique du header "Authorization: Bearer <accessToken>" (AuthInterceptor) ;
 *  - le renouvellement automatique du token sur reponse 401 (AuthAuthenticator) ;
 *  - des timeouts explicites (connexion/lecture/ecriture), absents des multiples
 *   "new OkHttpClient()" disperses dans le projet, qui heritaient des timeouts par defaut
 *    d'OkHttp (10s) sans garantie explicite - ici fixes et documentes.
 *
 * IMPORTANT (etat actuel, voir rapport final) : ce client n'est pour l'instant branche que
 * sur les nouveaux flux d'authentification (login/refresh/logout). Les ~34 instanciations
 * directes de "new OkHttpClient()" deja presentes ailleurs dans le projet (une par ecran,
 * cf. audit precedent) n'ont PAS ete migrees vers ce client partage dans cette intervention :
 * elles continueront donc de fonctionner pour les endpoints publics, mais n'enverront PAS
 * automatiquement le token sur les endpoints nouvellement proteges par AuthMiddleware. La
 * migration de chaque ecran vers ApiClient.getInstance(context) est le travail de suite
 * necessaire pour que l'authentification par token fonctionne de bout en bout dans toute
 * l'app (voir section "Travail restant" du rapport).
 */
public class ApiClient {
    private static volatile OkHttpClient sInstance;

    private ApiClient() {
    }

    public static OkHttpClient getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ApiClient.class) {
                if (sInstance == null) {
                    TokenStore tokenStore = new TokenStore(context.getApplicationContext());
                    sInstance = new OkHttpClient.Builder()
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .addInterceptor(new AuthInterceptor(tokenStore))
                            .authenticator(new AuthAuthenticator(context, tokenStore))
                            .build();
                }
            }
        }
        return sInstance;
    }
}
