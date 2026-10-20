package com.naniger.elim.controleur.pdf;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

/**
 * Mémorise, pour chaque PDF lu, l'endroit exact où le lecteur s'est arrêté :
 * page, position dans la page, nombre total de pages et date de lecture.
 *
 * <p>Stockage local (SharedPreferences, privé à l'application), indexé par le chemin du
 * fichier téléchargé, qui est stable pour un livre donné. Compatible avec l'ancien format
 * (clé {@code page_<hash>} seule).
 */
public final class ReadingPositionStore {

    private static final String PREFS = "elim_pdf_reader";
    private static final String KEY_INVERT = "invert_colors";

    /** Dernière position connue d'un document. */
    public static final class Position {
        /** Page (0…n-1). */
        public final int page;
        /** Part de la page déjà défilée (0 = haut de la page, 1 = bas). */
        public final float offset;
        /** Nombre de pages du document lors de la sauvegarde (0 si inconnu). */
        public final int pageCount;
        /** Date de la dernière lecture (ms), 0 si inconnue. */
        public final long updatedAt;

        Position(int page, float offset, int pageCount, long updatedAt) {
            this.page = page;
            this.offset = offset;
            this.pageCount = pageCount;
            this.updatedAt = updatedAt;
        }

        /** Progression en pourcentage (0…100), -1 si le nombre de pages est inconnu. */
        public int percent() {
            return PageGeometry.readingPercent(page, offset, pageCount);
        }
    }

    private final SharedPreferences mPrefs;

    public ReadingPositionStore(Context context) {
        mPrefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Clé stable par document (chemin du fichier local). */
    private static String hash(String path) {
        return Integer.toHexString(path == null ? 0 : path.hashCode());
    }

    /** Position mémorisée, ou {@code null} si ce document n'a jamais été ouvert. */
    @Nullable
    public Position get(String path) {
        if (path == null) return null;
        String h = hash(path);
        if (!mPrefs.contains("page_" + h)) return null;
        return new Position(
                mPrefs.getInt("page_" + h, 0),
                mPrefs.getFloat("off_" + h, 0f),
                mPrefs.getInt("count_" + h, 0),
                mPrefs.getLong("ts_" + h, 0L));
    }

    /** Page mémorisée (0 si aucune). */
    public int lastPage(String path) {
        Position p = get(path);
        return p != null ? p.page : 0;
    }

    public void save(String path, int page, float offset, int pageCount) {
        if (path == null || pageCount <= 0) return;
        String h = hash(path);
        mPrefs.edit()
                .putInt("page_" + h, Math.max(0, Math.min(page, pageCount - 1)))
                .putFloat("off_" + h, Math.max(0f, Math.min(1f, offset)))
                .putInt("count_" + h, pageCount)
                .putLong("ts_" + h, System.currentTimeMillis())
                .apply();
    }

    public boolean invertColors() {
        return mPrefs.getBoolean(KEY_INVERT, false);
    }

    public void setInvertColors(boolean invert) {
        mPrefs.edit().putBoolean(KEY_INVERT, invert).apply();
    }
}
