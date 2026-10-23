package com.naniger.elim.model.structure;

import com.naniger.elim.model.author.ProfileText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Recherche, filtres et tri de la liste des structures, appliqués sur la liste déjà chargée
 * (une seule requête {@code /api/structures}, partagée avec l'accueil via le cache) : aucune
 * requête réseau par frappe. Java pur : testé sur la JVM.
 */
public final class StructureListQuery {

    public enum Sort { NAME, RESOURCES, MEMBERS, RECENT }

    public final String search;
    public final Sort sort;
    /** Localisation choisie (valeur exacte de l'API), {@code null} = toutes. */
    public final String location;
    public final boolean onlyMine;

    public StructureListQuery(String search, Sort sort, String location, boolean onlyMine) {
        this.search = search == null ? "" : search.trim();
        this.sort = sort == null ? Sort.NAME : sort;
        this.location = ProfileText.clean(location);
        this.onlyMine = onlyMine;
    }

    public static StructureListQuery initial() {
        return new StructureListQuery("", Sort.NAME, null, false);
    }

    public StructureListQuery withSearch(String s) { return new StructureListQuery(s, sort, location, onlyMine); }
    public StructureListQuery withSort(Sort s) { return new StructureListQuery(search, s, location, onlyMine); }
    public StructureListQuery withLocation(String l) { return new StructureListQuery(search, sort, l, onlyMine); }
    public StructureListQuery withOnlyMine(boolean m) { return new StructureListQuery(search, sort, location, m); }

    /** Un filtre réduit-il la liste ? (le tri n'en est pas un) */
    public boolean isFiltered() {
        return !search.isEmpty() || location != null || onlyMine;
    }

    /** Recherche par nom ou localisation (insensible à la casse et aux accents, tous les mots). */
    public boolean matches(StructureInfo s) {
        if (onlyMine && !s.isMember) return false;
        if (location != null && (s.location == null
                || !StructureText.fold(s.location).equals(StructureText.fold(location)))) return false;
        if (search.isEmpty()) return true;
        String hay = StructureText.fold(s.name) + " " + StructureText.fold(s.location);
        for (String word : StructureText.fold(search).split(" ")) {
            if (!word.isEmpty() && !hay.contains(word)) return false;
        }
        return true;
    }

    public List<StructureInfo> apply(List<StructureInfo> all) {
        List<StructureInfo> out = new ArrayList<>();
        for (StructureInfo s : all) if (matches(s)) out.add(s);
        Comparator<StructureInfo> byName = (a, b) -> StructureText.fold(a.name).compareTo(StructureText.fold(b.name));
        Comparator<StructureInfo> c;
        switch (sort) {
            case RESOURCES:
                c = Comparator.<StructureInfo>comparingInt(s -> -s.resources).thenComparing(byName);
                break;
            case MEMBERS:
                c = Comparator.<StructureInfo>comparingInt(s -> -s.members).thenComparing(byName);
                break;
            case RECENT:
                // Date ISO/SQL : l'ordre alphabétique est l'ordre chronologique. Sans date : à la fin.
                c = (a, b) -> {
                    String x = a.createdAt, y = b.createdAt;
                    if (x == null && y == null) return byName.compare(a, b);
                    if (x == null) return 1;
                    if (y == null) return -1;
                    int r = y.compareTo(x);
                    return r != 0 ? r : byName.compare(a, b);
                };
                break;
            default:
                c = byName;
        }
        Collections.sort(out, c);
        return out;
    }

    /**
     * Localisations proposées en filtre : seulement celles réellement renseignées, regroupées
     * sans tenir compte de la casse ni des accents, par nombre de structures décroissant.
     * Moins de deux localisations distinctes : liste vide (un filtre n'apporterait rien).
     */
    public static List<String> locations(List<StructureInfo> all) {
        Map<String, String> label = new LinkedHashMap<>();
        Map<String, Integer> count = new LinkedHashMap<>();
        for (StructureInfo s : all) {
            String l = ProfileText.clean(s.location);
            if (l == null) continue;
            String k = StructureText.fold(l);
            if (!label.containsKey(k)) label.put(k, l);
            count.put(k, count.getOrDefault(k, 0) + 1);
        }
        if (label.size() < 2) return new ArrayList<>();
        List<String> keys = new ArrayList<>(label.keySet());
        keys.sort((a, b) -> {
            int r = count.get(b) - count.get(a);
            return r != 0 ? r : a.compareTo(b);
        });
        List<String> out = new ArrayList<>();
        for (String k : keys) out.add(label.get(k));
        return out;
    }

    public static int memberCount(List<StructureInfo> all) {
        int n = 0;
        for (StructureInfo s : all) if (s.isMember) n++;
        return n;
    }
}
