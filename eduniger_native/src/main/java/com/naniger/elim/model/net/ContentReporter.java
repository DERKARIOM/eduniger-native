package com.naniger.elim.model.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.naniger.elim.model.data.Server;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Signalement et blocage de contenus (politiques Google Play « contenu genere par
 * les utilisateurs » et « IA generative ») :
 * <ul>
 *   <li>{@link #report} : signale un commentaire, une reponse de l'IA ou un
 *   utilisateur a l'equipe de moderation (POST /api/reports) ;</li>
 *   <li>{@link #blockUser} / {@link #isBlocked} : masque localement tous les
 *   commentaires d'un utilisateur.</li>
 * </ul>
 */
public final class ContentReporter {

    public static final String TYPE_COMMENT = "comment";
    public static final String TYPE_AI = "ai";
    public static final String TYPE_USER = "user";

    private static final String TAG = "ContentReporter";
    private static final String PREFS = "moderation_prefs";
    private static final String KEY_BLOCKED = "blocked_users";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    // Libelles affiches -> codes attendus par le serveur (ContentReportController::REASONS)
    private static final String[] REASON_LABELS = {
            "Contenu offensant ou insultant",
            "Discours haineux ou discriminatoire",
            "Contenu sexuel",
            "Violence ou contenu dangereux",
            "Spam ou publicité",
            "Information fausse ou trompeuse",
            "Autre raison"
    };
    private static final String[] REASON_CODES = {
            "offensant", "haineux", "sexuel", "violent", "spam", "faux", "autre"
    };

    private ContentReporter() { }

    /** Affiche le choix du motif puis envoie le signalement. */
    public static void report(Context context, String type, String targetId, String excerpt) {
        String title = TYPE_AI.equals(type) ? "Signaler cette réponse"
                : TYPE_USER.equals(type) ? "Signaler cet utilisateur" : "Signaler ce commentaire";
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setItems(REASON_LABELS, (dialog, which) ->
                        send(context.getApplicationContext(), type, targetId, REASON_CODES[which], excerpt))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private static void send(Context appContext, String type, String targetId, String reason, String excerpt) {
        Handler main = new Handler(Looper.getMainLooper());
        EXECUTOR.execute(() -> {
            boolean ok = false;
            try {
                JSONObject payload = new JSONObject();
                payload.put("type", type);
                if (targetId != null) payload.put("targetId", targetId);
                payload.put("reason", reason);
                if (excerpt != null) {
                    payload.put("excerpt", excerpt.length() > 5000 ? excerpt.substring(0, 5000) : excerpt);
                }
                Request request = new Request.Builder()
                        .url(Server.getUrlHostProd(appContext) + "/api/reports")
                        .header("Accept", "application/json")
                        .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                                payload.toString()))
                        .build();
                try (Response response = ApiClient.getInstance(appContext).newCall(request).execute()) {
                    ok = response.isSuccessful();
                }
            } catch (Exception e) {
                Log.e(TAG, "Echec de l'envoi du signalement", e);
            }
            final boolean success = ok;
            main.post(() -> Toast.makeText(appContext,
                    success ? "Merci, votre signalement a été transmis à notre équipe."
                            : "Signalement impossible pour le moment. Réessayez plus tard.",
                    Toast.LENGTH_LONG).show());
        });
    }

    /** Masque desormais tous les commentaires de cet utilisateur sur cet appareil. */
    public static void blockUser(Context context, String idUser) {
        if (idUser == null || idUser.isEmpty()) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Set<String> blocked = new HashSet<>(prefs.getStringSet(KEY_BLOCKED, new HashSet<>()));
        blocked.add(idUser);
        prefs.edit().putStringSet(KEY_BLOCKED, blocked).apply();
    }

    public static boolean isBlocked(Context context, String idUser) {
        if (idUser == null || idUser.isEmpty()) return false;
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getStringSet(KEY_BLOCKED, new HashSet<>()).contains(idUser);
    }
}
