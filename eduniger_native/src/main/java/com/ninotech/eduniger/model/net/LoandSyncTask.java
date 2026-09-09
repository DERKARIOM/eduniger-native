package com.ninotech.eduniger.model.net;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ninotech.eduniger.model.data.DownloadFile;
import com.ninotech.eduniger.model.data.Server;
import com.ninotech.eduniger.model.table.LoandTable;
import com.ninotech.eduniger.model.table.Session;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Synchronisation a la demande du cache local des emprunts (LoandTable) avec
 * GET /api/loans/mine (emprunts actifs de l'utilisateur courant).
 *
 * Complete la synchro partielle declenchee par notification push
 * (MyFirebaseMessagingService.syncUnreadLoands, /api/loans/unread) : celle-ci
 * n'ajoute que les emprunts non lus recus via une notification. Celle-ci
 * reconcilie completement le cache a l'ouverture d'un ecran :
 *  - ajoute les emprunts actifs presents cote serveur mais absents en local
 *    (telecharge leur couverture au passage) ;
 *  - retire du cache local les emprunts qui n'apparaissent plus dans la
 *    reponse serveur (typiquement : livre rendu / emprunt cloture), ce
 *    qu'aucun mecanisme existant ne faisait auparavant.
 *
 * Usage : new LoandSyncTask(context, () -> { ... rafraichir l'UI ... }).execute();
 * Le callback est toujours appele (succes ou echec reseau) pour que l'appelant
 * puisse relire le cache local, qui reste valide meme si la synchro a echoue.
 */
public class LoandSyncTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "LoandSyncTask";

    public interface OnSyncComplete {
        void onSyncComplete();
    }

    private final Context mContext;
    private final OnSyncComplete mCallback;

    public LoandSyncTask(Context context, OnSyncComplete callback) {
        mContext = context.getApplicationContext();
        mCallback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            Session session = new Session(mContext);
            String idUser = session.getIdNumber();
            if (idUser == null || idUser.isEmpty() || "null".equals(idUser)) {
                return false;
            }

            OkHttpClient client = ApiClient.getInstance(mContext);
            Request request = new Request.Builder()
                    .url(Server.getUrlHostProd(mContext) + "/api/loans/mine")
                    .get()
                    .build();

            String jsonResponse;
            try (Response response = client.newCall(request).execute()) {
                if (response.body() == null) return false;
                jsonResponse = response.body().string();
            }

            JSONObject jsonObject = new JSONObject(jsonResponse);
            if (!jsonObject.optBoolean("success", false)) {
                Log.e(TAG, "Erreur API /api/loans/mine : " + jsonObject.optString("error"));
                return false;
            }

            JSONArray data = jsonObject.getJSONArray("data");
            LoandTable loandTable = new LoandTable(mContext);
            DownloadFile downloader = new DownloadFile(mContext);

            Set<String> serverIds = new HashSet<>();
            Set<String> localIds = loandTable.getAllIds();

            for (int i = 0; i < data.length(); i++) {
                JSONObject loand = data.getJSONObject(i);
                String idLoand = loand.getString("idLoand");
                serverIds.add(idLoand);

                if (localIds.contains(idLoand)) continue; // deja en cache local

                String bookTitle      = loand.optString("bookTitle", "");
                String bookCover      = loand.optString("bookCover", "");
                String dateLoand      = loand.optString("dateLoand", "");
                String realReturnDate = loand.optString("realReturnDate", "");

                String localCoverPath = "";
                if (!bookCover.isEmpty() && !"null".equals(bookCover)) {
                    try {
                        String coverUrl = Server.getUrlHostProd(mContext)
                                + "/api/public/resource/1/blanket/" + bookCover;
                        localCoverPath = downloader.start(coverUrl, bookCover, null).path;
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur téléchargement couverture : " + e.getMessage());
                        localCoverPath = bookCover;
                    }
                }

                loandTable.insert(idLoand, idUser, localCoverPath, bookTitle, dateLoand, realReturnDate);
            }

            for (String localId : localIds) {
                if (!serverIds.contains(localId)) {
                    loandTable.remove(localId);
                }
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Erreur LoandSyncTask : " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (mCallback != null) mCallback.onSyncComplete();
    }
}
