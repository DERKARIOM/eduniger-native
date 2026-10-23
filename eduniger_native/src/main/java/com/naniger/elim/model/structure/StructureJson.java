package com.naniger.elim.model.structure;

import com.naniger.elim.model.author.AuthorProfile;
import com.naniger.elim.model.author.ProfileText;
import com.naniger.elim.model.data.Author;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lecture défensive des réponses JSON des structures : un champ manquant, {@code null} ou d'un
 * type inattendu est ignoré au lieu de faire échouer l'écran. Java pur : testé sur la JVM.
 */
public final class StructureJson {

    private StructureJson() {
    }

    static String str(JSONObject o, String key) {
        if (o == null || !o.has(key) || o.isNull(key)) return null;
        return ProfileText.clean(o.optString(key, null));
    }

    static int num(JSONObject o, String key) {
        if (o == null || !o.has(key) || o.isNull(key)) return 0;
        Object v = o.opt(key);
        if (v instanceof Number) return Math.max(0, ((Number) v).intValue());
        try {
            return Math.max(0, Integer.parseInt(String.valueOf(v).trim()));
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
        return s.equals("1") || s.equalsIgnoreCase("true");
    }

    /** Champs communs à la liste et à la fiche. */
    static StructureInfo base(JSONObject o) {
        StructureInfo s = new StructureInfo();
        s.id = str(o, "id");
        s.name = str(o, "nameStruct");
        s.description = ProfileText.biography(str(o, "description"));
        s.logo = str(o, "logo");
        s.banner = str(o, "banner");
        s.location = str(o, "location");
        s.createdAt = str(o, "created_at");
        s.resources = num(o, "bookNumber");
        s.members = num(o, "adhererNumber");
        s.isMember = flag(o, "isAdhere");
        s.isMemberAdmin = flag(o, "isMemberAdmin");
        return s;
    }

    /** Liste {@code /api/structures} : entrées sans identifiant valide ou sans nom écartées, sans doublon. */
    public static List<StructureInfo> list(String body) throws JSONException {
        JSONArray arr = new JSONArray(body);
        List<StructureInfo> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;
            StructureInfo s = base(o);
            if (!StructureText.isValidId(s.id) || s.name == null || !seen.add(s.id)) continue;
            out.add(s);
        }
        return out;
    }

    /** Fiche {@code /api/structures/{id}}. Compatible avec l'ancienne réponse (sans statistiques). */
    public static StructureInfo detail(String body) throws JSONException {
        JSONObject o = new JSONObject(body);
        StructureInfo s = base(o);
        JSONObject stats = o.optJSONObject("public_stats");
        if (stats != null) {
            s.hasStats = true;
            s.resources = num(stats, "resources");
            s.electronic = num(stats, "electronic");
            s.audio = num(stats, "audio");
            s.physical = num(stats, "physical");
            s.firstAddedAt = str(stats, "first_added_at");
        }
        JSONArray cats = o.optJSONArray("categories");
        if (cats != null) {
            for (int i = 0; i < cats.length(); i++) {
                JSONObject c = cats.optJSONObject(i);
                String title = str(c, "title");
                int id = num(c, "idCategory");
                if (title != null && id > 0) {
                    s.categories.add(new AuthorProfile.CategoryCount(id, title, num(c, "books_count")));
                }
            }
        }
        JSONArray authors = o.optJSONArray("authors");
        if (authors != null) {
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < authors.length(); i++) {
                JSONObject a = authors.optJSONObject(i);
                String id = str(a, "idAuthor");
                if (id == null || !seen.add(id)) continue;
                s.authors.add(new Author(id, nz(str(a, "name")), nz(str(a, "firstName")), nz(str(a, "profile")),
                        nz(str(a, "profession")), "null", "null", "null"));
            }
        }
        return s;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
