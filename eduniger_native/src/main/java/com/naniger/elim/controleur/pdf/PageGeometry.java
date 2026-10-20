package com.naniger.elim.controleur.pdf;

/**
 * Calculs purs du lecteur PDF (sans Android, testés en JVM).
 */
public final class PageGeometry {

    private PageGeometry() {
    }

    /**
     * Taille du bitmap à produire pour afficher une page de {@code pageW × pageH} points
     * sur {@code targetWidthPx} pixels de large, à l'échelle {@code zoom}, sans dépasser
     * {@code maxBytes} (ARGB_8888 = 4 octets par pixel).
     *
     * @return {largeur, hauteur} en pixels, au moins 1×1.
     */
    public static int[] bitmapSize(float pageW, float pageH, int targetWidthPx, float zoom, long maxBytes) {
        if (pageW <= 0 || pageH <= 0 || targetWidthPx <= 0) return new int[]{1, 1};
        float z = Math.max(1f, zoom);
        double w = targetWidthPx * (double) z;
        double h = w * pageH / pageW;
        double bytes = w * h * 4d;
        if (bytes > maxBytes) {
            double k = Math.sqrt(maxBytes / bytes);
            w *= k;
            h *= k;
        }
        return new int[]{Math.max(1, (int) Math.round(w)), Math.max(1, (int) Math.round(h))};
    }

    /** Borne un déplacement : le contenu agrandi ne doit pas laisser de vide sur les bords. */
    public static float clampTranslation(float t, float viewSize, float scale) {
        float min = Math.min(0f, viewSize - viewSize * scale);
        return Math.max(min, Math.min(0f, t));
    }

    /**
     * Page considérée comme « courante » : la première visible, sauf si elle n'occupe plus
     * qu'un tiers ou moins du haut de l'écran (on passe alors à la suivante).
     */
    public static int currentPage(int firstVisible, int firstBottomPx, int viewportHeightPx, int pageCount) {
        if (pageCount <= 0 || firstVisible < 0) return 0;
        int page = firstVisible;
        if (firstBottomPx < viewportHeightPx / 3 && page + 1 < pageCount) page++;
        return Math.min(page, pageCount - 1);
    }

    /** Numéro de page saisi par l'utilisateur (1…n) → index (0…n-1), ou -1 si invalide. */
    public static int parsePageInput(String raw, int pageCount) {
        if (raw == null) return -1;
        try {
            int p = Integer.parseInt(raw.trim());
            return (p >= 1 && p <= pageCount) ? p - 1 : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
