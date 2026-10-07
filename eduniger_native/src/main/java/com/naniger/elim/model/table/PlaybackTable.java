package com.naniger.elim.model.table;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * État d'écoute local d'Elim : position de reprise, favoris, historique.
 *
 * Remplace le stockage en SharedPreferences utilisé jusqu'ici par AudioPlayerService,
 * qui présentait trois limites :
 *   1. AUCUN cloisonnement par utilisateur — deux comptes sur le même appareil
 *      partageaient leurs favoris et leurs positions de lecture ;
 *   2. pas d'horodatage, donc impossible de construire un historique ou un
 *      carrousel « Reprendre l'écoute » ;
 *   3. pas interrogeable (impossible de lister « tous les favoris »).
 *
 * Schéma : une ligne par couple (utilisateur, livre audio).
 *
 * NOTE SUR LA CRÉATION DE TABLE — le projet partage un seul fichier data.db entre
 * plusieurs SQLiteOpenHelper tous en version 1. Conséquence : onCreate() n'est
 * appelé QUE si le fichier n'existe pas encore, donc jamais pour un utilisateur
 * déjà installé. Le projet contourne cela via Initialization.onCreate() qui appelle
 * explicitement le onCreate de chaque table. On s'y enregistre, ET on se crée
 * défensivement à chaque ouverture (ensureTable) : le service audio peut être
 * relancé par le système sans que MainActivity — donc Initialization — soit passé.
 * CREATE TABLE IF NOT EXISTS rend l'opération idempotente et peu coûteuse.
 */
public class PlaybackTable extends SQLiteOpenHelper {

    private static final String TAG = "PlaybackTable";

    public static final String DATABASE_NAME = "data.db";
    public static final String NAME_TABLE    = "Playback";

    /** Une piste est considérée « terminée » à moins de 15 s de la fin. */
    private static final int COMPLETION_THRESHOLD_MS = 15_000;
    /** En deçà de ce seuil, inutile de proposer une reprise. */
    private static final int MIN_RESUME_MS = 5_000;

    public PlaybackTable(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + NAME_TABLE + "\n" +
                "(\n" +
                "    idPlayback INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                "    idNumberPlayback VARCHAR(100) NOT NULL,\n" +
                "    idBookPlayback VARCHAR(100) NOT NULL,\n" +
                "    positionPlayback INTEGER DEFAULT 0,\n" +
                "    durationPlayback INTEGER DEFAULT 0,\n" +
                "    favoritePlayback INTEGER DEFAULT 0,\n" +
                "    completedPlayback INTEGER DEFAULT 0,\n" +
                "    playCountPlayback INTEGER DEFAULT 0,\n" +
                "    updatedAtPlayback INTEGER DEFAULT 0,\n" +
                "    UNIQUE(idNumberPlayback,idBookPlayback)\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // Volontairement NON destructif : contrairement aux autres tables du projet,
        // on ne DROP pas — l'historique et les favoris de l'utilisateur seraient perdus.
        onCreate(db);
    }

    /** Garantit l'existence de la table même sur une base data.db déjà créée. */
    private SQLiteDatabase writable() {
        SQLiteDatabase db = getWritableDatabase();
        onCreate(db);
        return db;
    }

    private SQLiteDatabase readable() {
        SQLiteDatabase db = getReadableDatabase();
        onCreate(db); // getReadableDatabase renvoie une base inscriptible en pratique
        return db;
    }

    // ── Écriture ──────────────────────────────────────────────────────────────

    /**
     * Enregistre la progression d'écoute et met à jour l'horodatage (historique).
     * Insère la ligne si elle n'existe pas, la met à jour sinon, sans jamais
     * écraser l'état « favori » déjà stocké.
     */
    public void saveProgress(String idNumber, String idBook, int positionMs, int durationMs) {
        if (idNumber == null || idBook == null) return;
        try {
            SQLiteDatabase db = writable();
            boolean completed = durationMs > 0 && positionMs >= durationMs - COMPLETION_THRESHOLD_MS;

            ContentValues values = new ContentValues();
            values.put("positionPlayback",  completed ? 0 : positionMs);
            values.put("durationPlayback",  durationMs);
            values.put("completedPlayback", completed ? 1 : 0);
            values.put("updatedAtPlayback", System.currentTimeMillis());

            int updated = db.update(NAME_TABLE, values,
                    "idNumberPlayback=? AND idBookPlayback=?", new String[]{idNumber, idBook});

            if (updated == 0) {
                values.put("idNumberPlayback", idNumber);
                values.put("idBookPlayback",   idBook);
                values.put("favoritePlayback", 0);
                values.put("playCountPlayback", 1);
                db.insert(NAME_TABLE, null, values);
            }
        } catch (Exception e) {
            Log.e(TAG, "saveProgress failed", e);
        }
    }

    /** Incrémente le compteur d'écoutes (appelé au démarrage d'une piste). */
    public void markPlayed(String idNumber, String idBook) {
        if (idNumber == null || idBook == null) return;
        try {
            SQLiteDatabase db = writable();
            int updated = db.compileStatement(
                    "UPDATE " + NAME_TABLE + " SET playCountPlayback = playCountPlayback + 1," +
                    " updatedAtPlayback = " + System.currentTimeMillis() +
                    " WHERE idNumberPlayback='" + idNumber + "' AND idBookPlayback='" + idBook + "'")
                    .executeUpdateDelete();
            if (updated == 0) {
                ContentValues values = new ContentValues();
                values.put("idNumberPlayback",  idNumber);
                values.put("idBookPlayback",    idBook);
                values.put("playCountPlayback", 1);
                values.put("updatedAtPlayback", System.currentTimeMillis());
                db.insert(NAME_TABLE, null, values);
            }
        } catch (Exception e) {
            Log.e(TAG, "markPlayed failed", e);
        }
    }

    /** Bascule l'état favori et renvoie la nouvelle valeur. */
    public boolean toggleFavorite(String idNumber, String idBook) {
        boolean newValue = !isFavorite(idNumber, idBook);
        setFavorite(idNumber, idBook, newValue);
        return newValue;
    }

    public void setFavorite(String idNumber, String idBook, boolean favorite) {
        if (idNumber == null || idBook == null) return;
        try {
            SQLiteDatabase db = writable();
            ContentValues values = new ContentValues();
            values.put("favoritePlayback", favorite ? 1 : 0);

            int updated = db.update(NAME_TABLE, values,
                    "idNumberPlayback=? AND idBookPlayback=?", new String[]{idNumber, idBook});
            if (updated == 0) {
                values.put("idNumberPlayback",  idNumber);
                values.put("idBookPlayback",    idBook);
                values.put("updatedAtPlayback", System.currentTimeMillis());
                db.insert(NAME_TABLE, null, values);
            }
        } catch (Exception e) {
            Log.e(TAG, "setFavorite failed", e);
        }
    }

    /** Efface la position de reprise (piste terminée ou relancée depuis le début). */
    public void clearPosition(String idNumber, String idBook) {
        if (idNumber == null || idBook == null) return;
        try {
            ContentValues values = new ContentValues();
            values.put("positionPlayback",  0);
            values.put("completedPlayback", 1);
            values.put("updatedAtPlayback", System.currentTimeMillis());
            writable().update(NAME_TABLE, values,
                    "idNumberPlayback=? AND idBookPlayback=?", new String[]{idNumber, idBook});
        } catch (Exception e) {
            Log.e(TAG, "clearPosition failed", e);
        }
    }

    /** Supprime une entrée de l'historique (action utilisateur). */
    public void removeFromHistory(String idNumber, String idBook) {
        if (idNumber == null || idBook == null) return;
        try {
            writable().delete(NAME_TABLE,
                    "idNumberPlayback=? AND idBookPlayback=?", new String[]{idNumber, idBook});
        } catch (Exception e) {
            Log.e(TAG, "removeFromHistory failed", e);
        }
    }

    public void clearHistory(String idNumber) {
        if (idNumber == null) return;
        try {
            // On conserve les favoris : on n'efface que la trace d'écoute.
            ContentValues values = new ContentValues();
            values.put("updatedAtPlayback", 0);
            values.put("positionPlayback",  0);
            values.put("playCountPlayback", 0);
            writable().update(NAME_TABLE, values,
                    "idNumberPlayback=? AND favoritePlayback=1", new String[]{idNumber});
            writable().delete(NAME_TABLE,
                    "idNumberPlayback=? AND favoritePlayback=0", new String[]{idNumber});
        } catch (Exception e) {
            Log.e(TAG, "clearHistory failed", e);
        }
    }

    // ── Lecture ───────────────────────────────────────────────────────────────

    public int getPosition(String idNumber, String idBook) {
        if (idNumber == null || idBook == null) return 0;
        try (Cursor c = readable().rawQuery(
                "SELECT positionPlayback FROM " + NAME_TABLE +
                " WHERE idNumberPlayback=? AND idBookPlayback=?", new String[]{idNumber, idBook})) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } catch (Exception e) {
            Log.e(TAG, "getPosition failed", e);
            return 0;
        }
    }

    public boolean isFavorite(String idNumber, String idBook) {
        if (idNumber == null || idBook == null) return false;
        try (Cursor c = readable().rawQuery(
                "SELECT favoritePlayback FROM " + NAME_TABLE +
                " WHERE idNumberPlayback=? AND idBookPlayback=?", new String[]{idNumber, idBook})) {
            return c.moveToFirst() && c.getInt(0) == 1;
        } catch (Exception e) {
            Log.e(TAG, "isFavorite failed", e);
            return false;
        }
    }

    /**
     * « Reprendre l'écoute » : jointure avec la table Audio pour ne renvoyer que les
     * livres réellement présents sur l'appareil, commencés mais non terminés,
     * du plus récent au plus ancien.
     *
     * Colonnes projetées (indices utilisés par l'appelant) :
     *   0 idBook, 1 cover, 2 title, 3 author, 4 audio, 5 duration(texte),
     *   6 position(ms), 7 durationMs, 8 favorite, 9 updatedAt
     */
    public Cursor getContinueListening(String idNumber, int limit) {
        return readable().rawQuery(
                "SELECT a.idBookAudio, a.coverAudio, a.titleAudio, a.authorAudio, a.audio," +
                "       a.durationAudio, p.positionPlayback, p.durationPlayback," +
                "       p.favoritePlayback, p.updatedAtPlayback" +
                "  FROM " + NAME_TABLE + " p" +
                "  INNER JOIN " + AudioTable.NAME_TABLE + " a" +
                "     ON a.idBookAudio = p.idBookPlayback AND a.idNumberAudio = p.idNumberPlayback" +
                " WHERE p.idNumberPlayback=?" +
                "   AND p.positionPlayback > " + MIN_RESUME_MS +
                "   AND p.completedPlayback = 0" +
                " ORDER BY p.updatedAtPlayback DESC" +
                " LIMIT " + limit, new String[]{idNumber});
    }

    /** Favoris de l'utilisateur, présents sur l'appareil. Mêmes colonnes que ci-dessus. */
    public Cursor getFavorites(String idNumber) {
        return readable().rawQuery(
                "SELECT a.idBookAudio, a.coverAudio, a.titleAudio, a.authorAudio, a.audio," +
                "       a.durationAudio, p.positionPlayback, p.durationPlayback," +
                "       p.favoritePlayback, p.updatedAtPlayback" +
                "  FROM " + NAME_TABLE + " p" +
                "  INNER JOIN " + AudioTable.NAME_TABLE + " a" +
                "     ON a.idBookAudio = p.idBookPlayback AND a.idNumberAudio = p.idNumberPlayback" +
                " WHERE p.idNumberPlayback=? AND p.favoritePlayback = 1" +
                " ORDER BY p.updatedAtPlayback DESC", new String[]{idNumber});
    }

    /** Historique complet (tout ce qui a déjà été lancé). Mêmes colonnes. */
    public Cursor getHistory(String idNumber) {
        return readable().rawQuery(
                "SELECT a.idBookAudio, a.coverAudio, a.titleAudio, a.authorAudio, a.audio," +
                "       a.durationAudio, p.positionPlayback, p.durationPlayback," +
                "       p.favoritePlayback, p.updatedAtPlayback" +
                "  FROM " + NAME_TABLE + " p" +
                "  INNER JOIN " + AudioTable.NAME_TABLE + " a" +
                "     ON a.idBookAudio = p.idBookPlayback AND a.idNumberAudio = p.idNumberPlayback" +
                " WHERE p.idNumberPlayback=? AND p.updatedAtPlayback > 0" +
                " ORDER BY p.updatedAtPlayback DESC", new String[]{idNumber});
    }

    public int getNbrFavorites(String idNumber) { return count(idNumber, "p.favoritePlayback = 1"); }
    public int getNbrHistory(String idNumber)   { return count(idNumber, "p.updatedAtPlayback > 0"); }

    private int count(String idNumber, String condition) {
        if (idNumber == null) return 0;
        try (Cursor c = readable().rawQuery(
                "SELECT COUNT(*) FROM " + NAME_TABLE + " p" +
                " INNER JOIN " + AudioTable.NAME_TABLE + " a" +
                "    ON a.idBookAudio = p.idBookPlayback AND a.idNumberAudio = p.idNumberPlayback" +
                " WHERE p.idNumberPlayback=? AND " + condition, new String[]{idNumber})) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } catch (Exception e) {
            Log.e(TAG, "count failed", e);
            return 0;
        }
    }
}
