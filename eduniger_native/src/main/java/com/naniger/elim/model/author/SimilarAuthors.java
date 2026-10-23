package com.naniger.elim.model.author;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Filtrage des auteurs similaires : jamais l'auteur consulté, jamais de doublon. Java pur. */
public final class SimilarAuthors {

    public interface IdOf<T> {
        String id(T item);
    }

    private SimilarAuthors() {
    }

    public static <T> List<T> filter(List<T> candidates, String currentAuthorId, IdOf<T> idOf, int max) {
        List<T> out = new ArrayList<>();
        if (candidates == null) return out;
        String current = currentAuthorId == null ? "" : currentAuthorId.trim();
        Set<String> seen = new HashSet<>();
        for (T c : candidates) {
            String id = idOf.id(c);
            if (id == null) continue;
            id = id.trim();
            if (id.isEmpty() || id.equals(current) || !seen.add(id)) continue;
            out.add(c);
            if (out.size() >= max) break;
        }
        return out;
    }
}
