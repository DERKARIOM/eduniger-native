package com.naniger.elim.model.structure;

import com.naniger.elim.model.author.ProfileText;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Règles d'affichage des structures (images, textes, partage). Java pur : testé sur la JVM.
 */
public final class StructureText {

    /** Valeurs par défaut posées par l'API quand aucune image n'a été choisie dans l'admin. */
    private static final String[] DEFAULT_IMAGES = {"eduniger.svg", "eduniger.png", "banner.png", "banner.svg"};

    private StructureText() {
    }

    /** Identifiant de structure valide (entier positif) : sinon aucune requête n'est envoyée. */
    public static boolean isValidId(String id) {
        if (id == null) return false;
        String t = id.trim();
        if (t.isEmpty() || t.length() > 10) return false;
        for (int i = 0; i < t.length(); i++) {
            if (!Character.isDigit(t.charAt(i))) return false;
        }
        return Long.parseLong(t) > 0;
    }

    /**
     * Image réellement choisie pour la structure ? Les valeurs par défaut de l'API
     * (« eduniger.svg », « banner.png »…) et les SVG (illisibles par Picasso) ne comptent pas :
     * l'écran affiche alors son propre visuel de remplacement au lieu d'une image cassée.
     */
    public static boolean isCustomImage(String file) {
        String f = ProfileText.clean(file);
        if (f == null) return false;
        String lower = f.toLowerCase(Locale.ROOT);
        for (String d : DEFAULT_IMAGES) if (lower.equals(d)) return false;
        return !lower.endsWith(".svg");
    }

    /** URL publique du logo, {@code null} s'il n'y en a pas. */
    public static String logoUrl(String host, String structureId, String logo) {
        return imageUrl(host, structureId, "logo", logo);
    }

    /** URL publique de la bannière, {@code null} s'il n'y en a pas. */
    public static String bannerUrl(String host, String structureId, String banner) {
        return imageUrl(host, structureId, "banner", banner);
    }

    private static String imageUrl(String host, String structureId, String type, String file) {
        if (!isValidId(structureId) || !isCustomImage(file)) return null;
        return trimSlash(host) + "/api/public/resource/" + structureId.trim() + "/" + type + "/" + enc(file.trim());
    }

    public static String detailUrl(String host, String structureId) {
        return trimSlash(host) + "/api/structures/" + structureId.trim();
    }

    public static String listUrl(String host) {
        return trimSlash(host) + "/api/structures";
    }

    public static String membershipUrl(String host, String structureId, boolean join) {
        return trimSlash(host) + "/api/structures/" + structureId.trim() + (join ? "/join" : "/leave");
    }

    /** Initiales affichées à la place d'un logo absent : « Kit TD » → « KT », « UAZ » → « UA ». */
    public static String initials(String name) {
        String n = ProfileText.clean(name);
        if (n == null) return "?";
        StringBuilder sb = new StringBuilder();
        for (String part : n.split("[\\s\\-_'’.]+")) {
            if (part.isEmpty()) continue;
            int cp = part.codePointAt(0);
            if (!Character.isLetterOrDigit(cp)) continue;
            sb.appendCodePoint(Character.toUpperCase(cp));
            if (sb.length() >= 2) break;
        }
        if (sb.length() == 1) {
            // Un seul mot : deux premières lettres.
            String letters = n.replaceAll("[^\\p{L}\\p{N}]", "");
            if (letters.length() >= 2) return letters.substring(0, 2).toUpperCase(Locale.ROOT);
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }

    /** Résumé d'une ligne pour les cartes : paragraphes mis bout à bout, espaces normalisés. */
    public static String summary(String description) {
        String d = ProfileText.clean(description);
        if (d == null) return null;
        return d.replaceAll("\\s+", " ").trim();
    }

    /** Texte de partage : uniquement des informations réelles. */
    public static String shareMessage(String name, String location, int resources, String appUrl) {
        StringBuilder sb = new StringBuilder("Découvrez ").append(ProfileText.clean(name) == null ? "cette structure" : name.trim())
                .append(" sur Elim");
        String l = ProfileText.clean(location);
        if (l != null) sb.append(" (").append(l).append(")");
        sb.append('.');
        if (resources == 1) sb.append(" 1 ressource à consulter dans la bibliothèque numérique Elim.");
        else if (resources > 1) sb.append(' ').append(resources).append(" ressources à consulter dans la bibliothèque numérique Elim.");
        if (ProfileText.clean(appUrl) != null) sb.append("\n\n").append(appUrl.trim());
        return sb.toString();
    }

    /** Clé de comparaison : minuscules, sans accents, espaces normalisés (« Niamey » ≈ « niaméy »). */
    public static String fold(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return n.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    private static String enc(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    private static String trimSlash(String host) {
        String h = host == null ? "" : host.trim();
        while (h.endsWith("/")) h = h.substring(0, h.length() - 1);
        return h;
    }
}
