package com.naniger.elim.model.data;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.naniger.elim.model.table.PlaybackTable;
import com.naniger.elim.model.table.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * Point d'accès unique à l'état d'écoute pour la couche UI.
 *
 * Les trois vues de la phase Bibliothèque — « Reprendre l'écoute », Favoris et
 * Historique — projettent exactement les mêmes colonnes ; ce dépôt centralise la
 * conversion Cursor → objets afin que la correspondance des index de colonnes ne
 * soit écrite qu'à un seul endroit (le projet mappe ailleurs les colonnes par
 * index numérique, ce qui est fragile dès qu'un schéma bouge).
 *
 * Ordre des colonnes défini par PlaybackTable :
 *   0 idBook · 1 cover · 2 title · 3 author · 4 audio
 *   5 durationLabel · 6 positionMs · 7 durationMs · 8 favorite · 9 updatedAt
 */
public class PlaybackRepository {

    private static final String TAG = "PlaybackRepository";
    private static final int DEFAULT_CONTINUE_LIMIT = 12;

    private final PlaybackTable mTable;
    private final String        mUserId;

    public PlaybackRepository(Context context) {
        mTable  = new PlaybackTable(context);
        mUserId = new Session(context).getIdNumber();
    }

    public List<ContinueItem> getContinueListening() {
        return read(safeCursor(Query.CONTINUE));
    }

    public List<ContinueItem> getFavorites() {
        return read(safeCursor(Query.FAVORITES));
    }

    public List<ContinueItem> getHistory() {
        return read(safeCursor(Query.HISTORY));
    }

    public int getNbrFavorites() { return mUserId == null ? 0 : mTable.getNbrFavorites(mUserId); }
    public int getNbrHistory()   { return mUserId == null ? 0 : mTable.getNbrHistory(mUserId); }

    public void removeFromHistory(String idBook) {
        if (mUserId != null) mTable.removeFromHistory(mUserId, idBook);
    }

    public void setFavorite(String idBook, boolean favorite) {
        if (mUserId != null) mTable.setFavorite(mUserId, idBook, favorite);
    }

    private enum Query { CONTINUE, FAVORITES, HISTORY }

    private Cursor safeCursor(Query query) {
        if (mUserId == null) return null;
        try {
            switch (query) {
                case CONTINUE:  return mTable.getContinueListening(mUserId, DEFAULT_CONTINUE_LIMIT);
                case FAVORITES: return mTable.getFavorites(mUserId);
                case HISTORY:   return mTable.getHistory(mUserId);
                default:        return null;
            }
        } catch (Exception e) {
            // Table absente sur une très ancienne base, ou jointure impossible :
            // on renvoie une liste vide plutôt que de faire planter l'écran d'accueil.
            Log.e(TAG, "query " + query + " failed", e);
            return null;
        }
    }

    private List<ContinueItem> read(Cursor cursor) {
        List<ContinueItem> items = new ArrayList<>();
        if (cursor == null) return items;
        try {
            if (cursor.moveToFirst()) {
                do {
                    items.add(new ContinueItem(
                            cursor.getString(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getString(5),
                            cursor.getInt(6),
                            cursor.getInt(7),
                            cursor.getInt(8) == 1,
                            cursor.getLong(9)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "cursor read failed", e);
        } finally {
            cursor.close();
        }
        return items;
    }
}
