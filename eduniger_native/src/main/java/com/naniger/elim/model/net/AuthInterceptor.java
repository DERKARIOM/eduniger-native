package com.naniger.elim.model.net;

import androidx.annotation.NonNull;

import com.naniger.elim.model.data.TokenStore;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Ajoute automatiquement l'en-tete "Authorization: Bearer <accessToken>" a chaque requete
 * sortante, si un token est present localement. Les endpoints qui n'en ont pas besoin
 * (login, register, refresh) ignorent simplement un header absent ou surnumeraire cote
 * serveur (AuthMiddleware n'est pas applique dessus), donc il est sans danger de l'ajouter
 * systematiquement plutot que de devoir le faire manuellement a chaque appel.
 */
public class AuthInterceptor implements Interceptor {
    private final TokenStore mTokenStore;

    public AuthInterceptor(TokenStore tokenStore) {
        mTokenStore = tokenStore;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {        Request original = chain.request();
        String accessToken = mTokenStore.getAccessToken();

        if (accessToken == null || original.header("Authorization") != null) {
            return chain.proceed(original);
        }

        Request withAuth = original.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .build();
        return chain.proceed(withAuth);
    }
}
