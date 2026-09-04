package com.ninotech.eduniger.model.data;
import android.content.Context;
import android.content.SharedPreferences;

import com.ninotech.eduniger.R;

/**
 * Point d'entree UNIQUE pour l'adresse du serveur EduNiger.
 *
 * Pour faire evoluer l'application d'un serveur local de test (http://192.168.49.1:2222)
 * vers un serveur distant securise (ex: https://server.eduniger.com), il suffit de modifier
 * les ressources correspondantes dans res/values/strings.xml : url_server, url_api, url_host,
 * url_host_prod, url_ai. Aucune autre modification n'est necessaire ailleurs dans le code :
 * toute URL serveur doit etre construite a partir des getters de cette classe (getUrlServer,
 * getUrlApi, getUrlHost, getUrlHostProd, getUrlAi) plutot que d'etre codee en dur.
 *
 * Ces valeurs peuvent aussi etre redefinies a l'execution (voir ServerActivity, ecran de
 * changement de serveur) et sont alors persistees dans les SharedPreferences ; les valeurs de
 * strings.xml ne servent que de repli tant qu'aucune valeur n'a ete enregistree.
 */
public class Server {
    private static final String PREFS_NAME = "server_prefs";
    // Ces constantes sont utilisees comme CLES de SharedPreferences (et non comme valeurs).
    // Elles sont volontairement laissees inchangees pour ne pas invalider les preferences deja
    // enregistrees sur les appareils des utilisateurs existants.
    private static final String URL_SERVER = "http://192.168.49.1:2222/eduniger/";
    private static final String URL_API = "http://192.168.49.1:2222/eduniger/api";
    private static final String IS_ACTIVATE = "pass";
    // Nouvelles cles de preferences (ajout sans danger, aucune donnee existante concernee).
    private static final String KEY_URL_HOST = "key_url_host";
    private static final String KEY_URL_HOST_PROD = "key_url_host_prod";
    private static final String KEY_URL_AI = "key_url_ai";

    // Presets utilises par l'ecran de changement de serveur (ServerActivity).
    public static final String PRESET_PROD_FABI = "http://78.46.46.154/fabi/";
    public static final String PRESET_DEV_FABI = "http://192.168.49.1:2222/fabi/";

    public Server()
    {

    }

    public static void saveServer(Context context, String urlServer , String urlApi) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(URL_SERVER, urlServer);
        editor.putString(URL_API,urlApi);
        editor.apply();
    }
    public static void saveUrlServer(Context context, String urlServer) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(URL_SERVER, urlServer);
        editor.apply();
    }
    public static void saveUrlApi(Context context, String urlApi) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(URL_API, urlApi);
        editor.apply();
    }

    public static void saveActivate(Context context, int pass) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(IS_ACTIVATE, pass);
        editor.apply();
    }
    public static int getPass(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(IS_ACTIVATE, 0);
    }
    public static String getUrlServer(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(URL_SERVER, context.getString(R.string.url_server));
    }
    public static String getUrlApi(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(URL_API, context.getString(R.string.url_api));
    }

    /**
     * Hote (protocole + domaine/IP + port, SANS chemin) du serveur de test/developpement.
     * Utilise pour les telechargements de fichiers (couvertures, audio) qui, historiquement,
     * etaient codes en dur separement dans plusieurs fichiers avec cette meme valeur.
     */
    public static String getUrlHost(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_URL_HOST, context.getString(R.string.url_host));
    }

    /**
     * Hote du serveur de stockage "production" (fichiers admin-api : couvertures, PDF...).
     * Correspond a l'ancienne ressource inutilisee "ip_eduna" (renommee ici en url_host_prod),
     * desormais effectivement utilisee pour centraliser cette valeur.
     */
    public static String getUrlHostProd(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_URL_HOST_PROD, context.getString(R.string.url_host_prod));
    }

    /** URL complete de l'endpoint de l'assistant IA (ChatAiActivity). */
    public static String getUrlAi(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_URL_AI, context.getString(R.string.url_ai));
    }
}
