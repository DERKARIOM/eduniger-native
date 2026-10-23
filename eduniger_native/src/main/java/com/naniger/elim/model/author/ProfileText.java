package com.naniger.elim.model.author;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Nettoyage et mise en forme des textes reçus de l'API pour le profil d'auteur.
 * Les textes sont toujours affichés comme du texte brut (jamais interprétés en HTML).
 * Java pur : testé sur la JVM.
 */
public final class ProfileText {

    private ProfileText() {
    }

    /** {@code null}, vide, « null » (valeur JSON sérialisée) → {@code null}. Sinon le texte rogné. */
    public static String clean(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty() || "null".equalsIgnoreCase(t) || "undefined".equalsIgnoreCase(t)) return null;
        return t;
    }

    public static boolean has(String s) {
        return clean(s) != null;
    }

    /** « Prénom Nom », sans doublon si les deux champs sont identiques, sans espace parasite. */
    public static String fullName(String firstName, String name) {
        String f = clean(firstName);
        String n = clean(name);
        if (f == null) return n == null ? "" : n;
        if (n == null || n.equalsIgnoreCase(f)) return f;
        return f + " " + n;
    }

    /**
     * Biographie : fins de ligne unifiées, caractères de contrôle retirés, espaces de fin de
     * ligne supprimés, pas plus d'une ligne vide entre deux paragraphes. Les paragraphes et
     * retours à la ligne voulus par l'auteur sont conservés.
     */
    public static String biography(String raw) {
        String s = clean(raw);
        if (s == null) return null;
        s = s.replace("\r\n", "\n").replace('\r', '\n');
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n' || c == '\t' || !Character.isISOControl(c)) out.append(c == '\t' ? ' ' : c);
        }
        String[] lines = out.toString().split("\n", -1);
        StringBuilder sb = new StringBuilder();
        int blank = 0;
        for (String line : lines) {
            String l = stripTrailing(line);
            if (l.trim().isEmpty()) {
                blank++;
                continue;
            }
            if (sb.length() > 0) sb.append(blank > 0 ? "\n\n" : "\n");
            sb.append(l);
            blank = 0;
        }
        String result = sb.toString().trim();
        return result.isEmpty() ? null : result;
    }

    /** « PDF · Audio · Papier » d'après les compteurs réels (0 → format absent). */
    public static String formats(int pdf, int audio, int physical, String pdfLabel, String audioLabel,
                                 String physicalLabel) {
        List<String> parts = new ArrayList<>();
        if (pdf > 0) parts.add(pdfLabel);
        if (audio > 0) parts.add(audioLabel);
        if (physical > 0) parts.add(physicalLabel);
        return join(parts, " · ");
    }

    /** Liste sans doublon (insensible à la casse), sans valeur vide, dans l'ordre d'origine. */
    public static List<String> distinct(List<String> values) {
        Set<String> seen = new LinkedHashSet<>();
        List<String> out = new ArrayList<>();
        if (values == null) return out;
        for (String v : values) {
            String c = clean(v);
            if (c != null && seen.add(c.toLowerCase(java.util.Locale.ROOT))) out.add(c);
        }
        return out;
    }

    public static String join(List<String> parts, String sep) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null || p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(sep);
            sb.append(p);
        }
        return sb.toString();
    }

    /** Année (« 2026 ») d'une date API « 2026-10-12 15:00:00 » ou ISO ; {@code null} si illisible. */
    public static String year(String apiDate) {
        String d = clean(apiDate);
        if (d == null || d.length() < 4) return null;
        String y = d.substring(0, 4);
        for (int i = 0; i < 4; i++) if (!Character.isDigit(y.charAt(i))) return null;
        return y;
    }

    /** Numéro de téléphone exploitable (au moins 6 chiffres), sinon {@code null}. */
    public static String phone(String raw) {
        String c = clean(raw);
        if (c == null) return null;
        int digits = 0;
        for (int i = 0; i < c.length(); i++) if (Character.isDigit(c.charAt(i))) digits++;
        return digits >= 6 ? c : null;
    }

    /** Numéro au format attendu par wa.me (chiffres seulement). */
    public static String whatsappDigits(String raw) {
        String c = phone(raw);
        if (c == null) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < c.length(); i++) if (Character.isDigit(c.charAt(i))) sb.append(c.charAt(i));
        return sb.toString();
    }

    /** Adresse e-mail plausible (contrôle simple), sinon {@code null}. */
    public static String email(String raw) {
        String c = clean(raw);
        if (c == null) return null;
        int at = c.indexOf('@');
        return (at > 0 && at < c.length() - 3 && c.indexOf('.', at) > at + 1 && !c.contains(" ")) ? c : null;
    }

    /** Texte de partage du profil : uniquement des informations réelles. */
    public static String shareMessage(String fullName, String profession, int publications, String appUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("Découvrez ").append(fullName).append(" sur Elim");
        String p = clean(profession);
        if (p != null) sb.append(" (").append(p).append(")");
        sb.append('.');
        if (publications == 1) sb.append(" 1 publication à lire dans la bibliothèque numérique Elim.");
        else if (publications > 1) sb.append(' ').append(publications)
                .append(" publications à lire dans la bibliothèque numérique Elim.");
        if (clean(appUrl) != null) sb.append("\n\n").append(appUrl.trim());
        return sb.toString();
    }

    private static String stripTrailing(String s) {
        int end = s.length();
        while (end > 0 && Character.isWhitespace(s.charAt(end - 1))) end--;
        return s.substring(0, end);
    }
}
