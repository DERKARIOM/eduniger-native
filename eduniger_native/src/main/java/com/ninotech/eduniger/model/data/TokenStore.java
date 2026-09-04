package com.ninotech.eduniger.model.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Stockage securise des tokens d'authentification (Access Token + Refresh Token) sur
 * l'appareil, a l'aide d'EncryptedSharedPreferences (chiffrement AES256, cle protegee par
 * l'Android Keystore materiel quand disponible).
 *
 * Pourquoi pas les SharedPreferences classiques ("server_prefs" utilisees par Server.java,
 * ou la table SQLite locale "Session") : ces mecanismes stockent les donnees en clair sur le
 * disque de l'appareil. Un refresh token vole (appareil root/compromis, backup non chiffre,
 * autre app malveillante avec acces au stockage) permettrait a un attaquant d'obtenir des
 * access tokens indefiniment jusqu'a expiration/revocation du refresh token. Le stockage
 * chiffre reduit significativement ce risque.
 *
 *Utilisation :
 *   TokenStore store = new TokenStore(context);
 *   store.saveTokens(accessToken, refreshToken, expiresInSeconds);
 *   String access = store.getAccessToken();
 *   store.clear(); // logout
 */
public class TokenStore {
    private static final String TAG = "TokenStore";
    private static final String PREFS_FILE_NAME = "eduniger_secure_auth_prefs";

    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_ACCESS_TOKEN_EXPIRES_AT = "access_token_expires_at_millis";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences mPrefs;

    public TokenStore(Context context) {
        SharedPreferences prefs;
        try {
            // API moderne (androidx.security.crypto >= 1.1.0-alpha03) : la classe MasterKeys
            // et EncryptedSharedPreferences.create(fileName, masterKeyAlias, ...) sont
            // supprimees au profit de MasterKey.Builder + une surcharge de create() qui prend
            // l'objet MasterKey (et le Context en premier parametre).
            MasterKey masterKey = new MasterKey.Builder(context.getApplicationContext())
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            prefs = EncryptedSharedPreferences.create(
                    context.getApplicationContext(),
                    PREFS_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            // En dernier recours (ne devrait pas arriver en pratique sur API 28+), on retombe
            // sur des SharedPreferences classiques plutot que de planter l'app : mieux vaut
            // une session non chiffree que pas de session du tout. Journalise pour visibilite.
            Log.e(TAG, "Impossible d'initialiser le stockage chiffre des tokens, repli sur SharedPreferences non chiffrees", e);
            prefs = context.getApplicationContext().getSharedPreferences(PREFS_FILE_NAME + "_fallback", Context.MODE_PRIVATE);
        }
        mPrefs = prefs;
    }

    public synchronized void saveTokens(String accessToken, String refreshToken, long expiresInSeconds) {
        long expiresAtMillis = System.currentTimeMillis() + (expiresInSeconds * 1000L);
        mPrefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putLong(KEY_ACCESS_TOKEN_EXPIRES_AT, expiresAtMillis)
                .apply();
    }

    /** Met a jour uniquement l'access token (et son expiration), typiquement apres un refresh
     *  qui ne fait pas tourner le refresh token (ne devrait pas arriver avec notre backend qui
     *  fait toujours tourner les deux ensemble, mais garde l'API flexible). */
    public synchronized void updateAccessToken(String accessToken, long expiresInSeconds) {
        long expiresAtMillis = System.currentTimeMillis() + (expiresInSeconds * 1000L);
        mPrefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putLong(KEY_ACCESS_TOKEN_EXPIRES_AT, expiresAtMillis)
                .apply();
    }

    public synchronized void saveRole(String role) {
        mPrefs.edit().putString(KEY_ROLE, role).apply();
    }

    public synchronized String getAccessToken() {
        return mPrefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public synchronized String getRefreshToken() {
        return mPrefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public synchronized String getRole() {
        return mPrefs.getString(KEY_ROLE, "USER");
    }

    /** Vrai si un access token est present et n'est pas (encore) expire cote client. Une marge
     *  de 10s est appliquee pour eviter d'envoyer un token qui expirerait pendant le transit. */
    public synchronized boolean hasValidAccessToken() {
        String token = getAccessToken();
        if (token == null) return false;
        long expiresAt = mPrefs.getLong(KEY_ACCESS_TOKEN_EXPIRES_AT, 0);
        return System.currentTimeMillis() < (expiresAt - 10_000L);
    }

    public synchronized boolean isLoggedIn() {
        return getRefreshToken() != null;
    }

    /** Supprime tous les tokens stockes localement (a appeler au logout, ou lorsque le refresh
     *  token est detecte comme invalide/revoque et que l'utilisateur doit etre deconnecte). */
    public synchronized void clear() {
        mPrefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_ACCESS_TOKEN_EXPIRES_AT)
                .remove(KEY_ROLE)
                .apply();
    }
}
