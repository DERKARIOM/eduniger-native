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
 * Tous les points d'appel du projet qui instanciaient auparavant leur propre
 * "new OkHttpClient()" ont ete migres vers ApiClient.getInstance(context) (ou
 * ApiClient.newBuilder(context) quand un ecran a besoin de timeouts specifiques,
 * par ex. les gros envois de fichiers) afin que le token soit systematiquement
 * attache et renouvele automatiquement, y compris sur les endpoints nouvellement
 * proteges par AuthMiddleware.
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

    /**
     * A utiliser quand un ecran a besoin de timeouts differents du client partage
     * (par ex. l'upload d'un livre/PDF/audio, qui peut prendre plus de 20s) tout en
     * conservant l'ajout automatique du token et le renouvellement sur 401 : cette
     * Builder herite de la configuration du client partage (interceptor + authenticator
     * inclus, cf. OkHttpClient.newBuilder()) et peut ensuite surcharger uniquement les
     * timeouts avant .build().
     */
    public static OkHttpClient.Builder newBuilder(Context context) {
        return getInstance(context).newBuilder();
    }
}
