package com.naniger.elim.controleur.pdf;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Dernière page lue et préférences de lecture, par document.
 * Quelques octets par livre en SharedPreferences (aucune donnée volumineuse).
 */
public final class ReadingPositionStore {

    private static final String PREFS = "elim_pdf_reader";
    private static final String KEY_INVERT = "invert_colors";

    private final SharedPreferences mPrefs;

    public ReadingPositionStore(Context context) {
        mPrefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Clé stable par document (chemin du fichier local). */
    private static String key(String path) {
        return "page_" + Integer.toHexString(path == null ? 0 : path.hashCode());
    }

    public int lastPage(String path) {
        return mPrefs.getInt(key(path), 0);
    }

    public void savePage(String path, int page) {
        if (path == null) return;
        mPrefs.edit().putInt(key(path), Math.max(0, page)).apply();
    }

    public boolean invertColors() {
        return mPrefs.getBoolean(KEY_INVERT, false);
    }

    public void setInvertColors(boolean invert) {
        mPrefs.edit().putBoolean(KEY_INVERT, invert).apply();
    }
}
