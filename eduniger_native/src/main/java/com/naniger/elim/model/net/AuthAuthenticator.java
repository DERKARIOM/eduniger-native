package com.naniger.elim.model.net;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.naniger.elim.model.data.Server;
import com.naniger.elim.model.data.TokenStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Renouvellement automatique de l'Access Token lorsqu'un appel API echoue avec 401.
 *
 * Cycle : requete -> 401 -> authenticate() appele automatiquement par OkHttp -> on appelle
 * refresh.php avec le refresh token stocke -> si succes, on sauvegarde le nouveau couple de
 * tokens et on rejoue la requete originale avec le nouvel access token -> si echec (refresh
 * token invalide/expire/deja utilise, ou pas de connexion), on efface la session locale et on
 * abandonne (pas de nouvelle tentative).
 *
 * PROTECTION ANTI BOUCLE INFINIE (exigence explicite du projet) : si la requete qui a recu ce
 * 401 est deja elle-meme une tentative issue d'un refresh precedent (response.priorResponse()
 * non nul), on abandonne immediatement sans re-essayer. Sans cette garde, un serveur qui
 * renverrait 401 de maniere persistante (ex: horloge serveur desynchronisee, bug) provoquerait
 * une boucle infinie 401 -> refresh -> 401 -> refresh -> ...
 */
public class AuthAuthenticator implements Authenticator {
    private static final String TAG = "AuthAuthenticator";

    private final Context mAppContext;
    private final TokenStore mTokenStore;
    // Client HTTP separe et minimal, dedie a l'appel de refresh.php : evite toute recursion
    // avec l'intercepteur/authenticator du client principal.
    private final OkHttpClient mRefreshClient = new OkHttpClient();

    public AuthAuthenticator(Context context, TokenStore tokenStore) {
        mAppContext = context.getApplicationContext();
        mTokenStore = tokenStore;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
        // GARDE CRITIQUE : ce client OkHttp est partage par TOUS les appels reseau de l'app,
        // y compris ceux qui visent encore l'ancien backend PHP non migre (recommended.php,
        // author_top.php, Pub.php, ask_eduna.php...), dont l'auth JWT est totalement
        // incompatible avec les tokens Sanctum stockes ici. Un 401 de l'ancien backend ne dit
        // RIEN sur la validite de la session Laravel/Sanctum en cours.
        // BUG REEL trouve en test : sans ce garde, un 401 legacy declenchait quand meme une
        // tentative de refresh via refresh.php (ancien backend), qui echoue systematiquement
        // (le token Sanctum n'a pas le format attendu), et le code ci-dessous effacait alors
        // la session Sanctum pourtant valide - quelques secondes apres une connexion reussie,
        // des le premier ecran (Accueil) qui appelle un endpoint legacy encore actif.
        String requestHost = response.request().url().host();
        okhttp3.HttpUrl laravelUrl = okhttp3.HttpUrl.parse(Server.getUrlHostProd(mAppContext));
        if (laravelUrl == null || !laravelUrl.host().equalsIgnoreCase(requestHost)) {
            return null;
        }

        // Garde anti-boucle : une seule tentative de refresh par requete originale.
        if (responseCount(response) >= 2) {
            Log.w(TAG, "Echec persistant apres tentative de refresh, abandon (pas de nouvelle boucle).");
            return null;
        }

        String refreshToken = mTokenStore.getRefreshToken();
        if (refreshToken == null) {
            // Pas de session locale : rien a rafraichir, on laisse le 401 remonter tel quel.
            return null;
        }

        synchronized (AuthAuthenticator.class) {
            // Un autre thread a peut-etre deja rafraichi entre-temps (ex: deux appels API
            // paralleles ont recu 401 en meme temps) : dans ce cas, on rejoue simplement avec
            // le token deja renouvele, sans re-appeler refresh.php inutilement.
            String currentAccessToken = mTokenStore.getAccessToken();
            String triedToken = response.request().header("Authorization");
            String currentBearer = currentAccessToken != null ? "Bearer " + currentAccessToken : null;
            if (currentBearer != null && !currentBearer.equals(triedToken)) {
                return response.request().newBuilder()
                        .header("Authorization", currentBearer)
                        .build();
            }

            String[] newTokens = performRefresh(refreshToken);
            if (newTokens == null) {
                // Refresh token invalide/expire/deja utilise, ou erreur reseau : on met fin a
                // la session locale plutot que de re-essayer indefiniment. L'app doit, a son
                // prochain point de controle (ex: ecran principal), constater l'absence de
                // session (TokenStore.isLoggedIn() == false) et rediriger vers la connexion.
                mTokenStore.clear();
                return null;
            }

            String newAccessToken = newTokens[0];
            String newRefreshToken = newTokens[1];
            long expiresIn = Long.parseLong(newTokens[2]);
            mTokenStore.saveTokens(newAccessToken, newRefreshToken, expiresIn);

            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + newAccessToken)
                    .build();
        }
    }

    /** @return [accessToken, refreshToken, expiresIn] ou null en cas d'echec. */
    @Nullable
    private String[] performRefresh(String refreshToken) {
        try {
            String url = Server.getUrlApi(mAppContext) + "refresh.php";
            RequestBody body = new FormBody.Builder()
                    .add("refresh_token", refreshToken)
                    .build();
            Request request = new Request.Builder().url(url).post(body).build();

            try (Response response = mRefreshClient.newCall(request).execute()) {
                String json = response.body() != null ? response.body().string() : null;
                if (!response.isSuccessful() || json == null) {
                    return null;
                }
                JSONObject obj = new JSONObject(json);
                if (!obj.has("accessToken") || !obj.has("refreshToken")) {
                    return null;
                }
                return new String[]{
                        obj.getString("accessToken"),
                        obj.getString("refreshToken"),
                        String.valueOf(obj.optLong("expiresIn", 900))
                };
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Echec du renouvellement du token (reseau ou reponse invalide)", e);
            return null;
        }
    }

    private int responseCount(Response response) {
        int count = 1;
        while ((response = response.priorResponse()) != null) {
            count++;
        }
        return count;
    }
}
