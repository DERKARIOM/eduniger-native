package com.naniger.elim.model.author;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Requête d'une page de la bibliographie d'un auteur sur l'API Laravel
 * ({@code GET /api/book/associations}).
 *
 * <p>Seuls les livres PUBLIÉS (rattachés à une structure) sont demandés : ce sont les seuls que
 * le lecteur peut ouvrir, et ce sont ceux comptés par {@code /api/authors/{id}} (public_stats).
 * Java pur : testé sur la JVM.
 */
public final class BibliographyQuery {

    public static final int PAGE_SIZE = 20;

    /** Tris acceptés par l'API. */
    public enum Sort {
        RECENT("recent"), OLDEST("oldest"), TITLE("title"), TITLE_DESC("title_desc");

        public final String api;

        Sort(String api) {
            this.api = api;
        }
    }

    /** Formats acceptés par l'API ({@code null} = tous). */
    public enum Format {
        PDF("pdf"), AUDIO("audio"), PHYSICAL("physical");

        public final String api;

        Format(String api) {
            this.api = api;
        }
    }

    public final String authorId;
    public final String search;
    public final Sort sort;
    public final Format format;
    public final Integer categoryId;

    public BibliographyQuery(String authorId, String search, Sort sort, Format format, Integer categoryId) {
        this.authorId = authorId;
        this.search = search == null ? "" : search.trim();
        this.sort = sort == null ? Sort.RECENT : sort;
        this.format = format;
        this.categoryId = categoryId;
    }

    public static BibliographyQuery forAuthor(String authorId) {
        return new BibliographyQuery(authorId, "", Sort.RECENT, null, null);
    }

    public BibliographyQuery withSearch(String s) {
        return new BibliographyQuery(authorId, s, sort, format, categoryId);
    }

    public BibliographyQuery withSort(Sort s) {
        return new BibliographyQuery(authorId, search, s, format, categoryId);
    }

    public BibliographyQuery withFormat(Format f) {
        return new BibliographyQuery(authorId, search, sort, f, categoryId);
    }

    public BibliographyQuery withCategory(Integer c) {
        return new BibliographyQuery(authorId, search, sort, format, c);
    }

    /** Un filtre réduit-il la liste (recherche, format ou catégorie) ? Le tri n'en est pas un. */
    public boolean isFiltered() {
        return !search.isEmpty() || format != null || categoryId != null;
    }

    /** Identifiant d'auteur valide (entier positif) : sinon aucune requête n'est envoyée. */
    public static boolean isValidAuthorId(String id) {
        if (id == null) return false;
        String t = id.trim();
        if (t.isEmpty() || t.length() > 10) return false;
        for (int i = 0; i < t.length(); i++) {
            if (!Character.isDigit(t.charAt(i))) return false;
        }
        return Long.parseLong(t) > 0;
    }

    /** URL complète d'une page (1…n). */
    public String pageUrl(String host, int page) {
        StringBuilder sb = new StringBuilder(trimSlash(host))
                .append("/api/book/associations?authorId=").append(enc(authorId.trim()))
                .append("&published=1")
                .append("&page=").append(Math.max(1, page))
                .append("&limit=").append(PAGE_SIZE)
                .append("&sort=").append(sort.api);
        if (format != null) sb.append("&format=").append(format.api);
        if (categoryId != null) sb.append("&categoryId=").append(categoryId);
        if (!search.isEmpty()) sb.append("&search=").append(enc(search));
        return sb.toString();
    }

    public static String profileUrl(String host, String authorId) {
        return trimSlash(host) + "/api/authors/" + enc(authorId.trim());
    }

    public static String similarUrl(String host, String authorId) {
        return trimSlash(host) + "/api/authors/" + enc(authorId.trim()) + "/similar";
    }

    /** L'API renvoie au plus {@link #PAGE_SIZE} livres : une page pleine laisse supposer une suite. */
    public static boolean mayHaveMore(int receivedCount) {
        return receivedCount >= PAGE_SIZE;
    }

    static String enc(String s) {
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
