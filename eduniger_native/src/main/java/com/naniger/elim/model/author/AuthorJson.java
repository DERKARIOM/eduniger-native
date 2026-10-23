package com.naniger.elim.model.author;

import com.naniger.elim.model.data.Author;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Lecture défensive des réponses JSON de l'API pour le profil d'auteur : chaque champ manquant,
 * {@code null} ou d'un type inattendu est ignoré au lieu de faire échouer tout l'écran.
 */
public final class AuthorJson {

    private AuthorJson() {
    }

    /** Valeur texte d'un champ, {@code null} si absent ou JSON null. */
    static String str(JSONObject o, String key) {
        if (o == null || !o.has(key) || o.isNull(key)) return null;
        return ProfileText.clean(o.optString(key, null));
    }

    static int num(JSONObject o, String key) {
        if (o == null || !o.has(key) || o.isNull(key)) return 0;
        Object v = o.opt(key);
        if (v instanceof Number) return ((Number) v).intValue();
        try {
            return Integer.parseInt(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static boolean flag(JSONObject o, String key) {
        if (o == null || !o.has(key) || o.isNull(key)) return false;
        Object v = o.opt(key);
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof Number) return ((Number) v).intValue() != 0;
        String s = String.valueOf(v).trim();
        return s.equals("1") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("oui");
    }

    public static AuthorProfile profile(String body) throws JSONException {
        JSONObject o = new JSONObject(body);
        AuthorProfile p = new AuthorProfile();
        p.id = str(o, "idAuthor");
        p.firstName = str(o, "firstName");
        p.name = str(o, "name");
        p.profile = str(o, "profile");
        p.cover = str(o, "cover");
        p.profession = str(o, "profession");
        p.biography = ProfileText.biography(str(o, "biography"));
        p.phone = ProfileText.phone(str(o, "call"));
        p.email = ProfileText.email(str(o, "email"));
        p.whatsapp = ProfileText.whatsappDigits(str(o, "whatsapp"));

        JSONObject stats = o.optJSONObject("public_stats");
        if (stats != null) {
            p.hasPublicStats = true;
            p.publications = num(stats, "publications");
            p.electronic = num(stats, "electronic");
            p.audio = num(stats, "audio");
            p.physical = num(stats, "physical");
            p.formats = num(stats, "formats");
            p.firstAddedAt = str(stats, "first_added_at");
        } else {
            // API pas encore mise à jour : compteurs historiques (tous les livres).
            p.publications = num(o, "books_count");
            p.electronic = num(o, "books_count_electronic");
            p.audio = num(o, "books_count_audio");
            p.physical = num(o, "books_count_physical");
            p.formats = (p.electronic > 0 ? 1 : 0) + (p.audio > 0 ? 1 : 0) + (p.physical > 0 ? 1 : 0);
        }

        JSONArray cats = o.optJSONArray("categories");
        if (cats != null) {
            for (int i = 0; i < cats.length(); i++) {
                JSONObject c = cats.optJSONObject(i);
                String title = str(c, "title");
                int id = num(c, "idCategory");
                if (title != null && id > 0) p.categories.add(new AuthorProfile.CategoryCount(id, title, num(c, "books_count")));
            }
        }
        return p;
    }

    /**
     * Livres d'une page. Les livres d'un autre auteur (réponse incohérente) sont écartés :
     * un profil n'affiche que les ouvrages de l'auteur consulté.
     */
    public static List<Publication> publications(String body, String authorId) throws JSONException {
        JSONArray arr = new JSONArray(body);
        List<Publication> out = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;
            String owner = str(o, "idAuthor");
            if (owner == null) {
                JSONObject a = o.optJSONObject("author");
                owner = str(a, "idAuthor");
            }
            if (owner != null && authorId != null && !owner.equals(authorId.trim())) continue;

            Publication b = new Publication();
            b.id = str(o, "idBook");
            if (b.id == null) continue;
            b.title = str(o, "title");
            b.description = ProfileText.biography(str(o, "description"));
            b.cover = str(o, "blanket");
            String electronic = str(o, "electronic");
            b.hasPdf = electronic != null;
            b.isAudio = flag(o, "isAudio");
            b.isPhysical = flag(o, "isPhysic");
            b.pages = str(o, "nbrPage");
            b.addedAt = str(o, "created_at");
            JSONArray structures = o.optJSONArray("structures");
            if (structures != null && structures.length() > 0) {
                b.structureId = str(structures.optJSONObject(0), "id");
                for (int j = 0; j < structures.length(); j++) {
                    String sid = str(structures.optJSONObject(j), "id");
                    if (sid != null) b.structureIds.add(sid);
                }
            }
            JSONObject author = o.optJSONObject("author");
            if (author != null) {
                String full = ProfileText.fullName(str(author, "firstName"), str(author, "name"));
                b.authorName = full.isEmpty() ? null : full;
            }
            JSONArray cats = o.optJSONArray("categories");
            List<String> titles = new ArrayList<>();
            if (cats != null) {
                for (int j = 0; j < cats.length(); j++) titles.add(str(cats.optJSONObject(j), "title"));
            }
            b.categories.addAll(ProfileText.distinct(titles));
            out.add(b);
        }
        return out;
    }

    /**
     * Ressources d'une page pour une structure. Un livre dont la liste de structures est connue
     * et ne contient pas la structure consultée (réponse incohérente) est écarté.
     */
    public static List<Publication> structureResources(String body, String structureId) throws JSONException {
        List<Publication> all = publications(body, null);
        List<Publication> out = new ArrayList<>();
        String sid = structureId == null ? null : structureId.trim();
        for (Publication p : all) {
            if (sid != null && !p.structureIds.isEmpty() && !p.structureIds.contains(sid)) continue;
            out.add(p);
        }
        return out;
    }

    /** Auteurs similaires → modèle {@link Author} existant (réutilisé par AuthorHorizontaleAdapter). */
    public static List<Author> similar(String body) throws JSONException {
        JSONArray arr = new JSONArray(body);
        List<Author> out = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            String id = str(o, "idAuthor");
            if (id == null) continue;
            out.add(new Author(id, nz(str(o, "name")), nz(str(o, "firstName")), nz(str(o, "profile")),
                    nz(str(o, "profession")), "null", "null", "null"));
        }
        return out;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
