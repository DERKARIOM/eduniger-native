package com.naniger.elim.model.author;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * État d'une liste paginée (bibliographie) : pages chargées, chargement en cours, fin de liste,
 * erreur. Chaque nouvelle requête (recherche, tri, filtre) incrémente une « génération » : une
 * réponse arrivée pour une ancienne génération est ignorée (pas de mélange de résultats).
 * Les doublons éventuels entre pages sont écartés. Java pur : testé sur la JVM.
 *
 * @param <T> élément de la liste
 */
public final class PagedList<T> {

    public interface KeyOf<T> {
        String key(T item);
    }

    private final KeyOf<T> mKeyOf;
    private final List<T> mItems = new ArrayList<>();
    private final Set<String> mKeys = new HashSet<>();
    private int mGeneration = 0;
    private int mLoadedPages = 0;
    private boolean mLoading = false;
    private boolean mHasMore = true;
    private boolean mError = false;

    public PagedList(KeyOf<T> keyOf) {
        mKeyOf = keyOf;
    }

    /** Nouvelle requête : vide la liste. @return la génération à joindre aux réponses. */
    public int reset() {
        mGeneration++;
        mItems.clear();
        mKeys.clear();
        mLoadedPages = 0;
        mLoading = false;
        mHasMore = true;
        mError = false;
        return mGeneration;
    }

    public int generation() {
        return mGeneration;
    }

    /** Page suivante à demander, ou -1 s'il ne faut rien demander. */
    public int nextPageToLoad() {
        if (mLoading || !mHasMore || mError) return -1;
        return mLoadedPages + 1;
    }

    public void markLoading() {
        mLoading = true;
        mError = false;
    }

    /**
     * Réponse d'une page.
     *
     * @return le nombre d'éléments réellement ajoutés, ou -1 si la réponse est périmée.
     */
    public int onPage(int generation, int page, List<T> items, boolean mayHaveMore) {
        if (generation != mGeneration) return -1;
        if (page == 1 && mLoadedPages >= 1) {
            // Version plus récente de la page 1 (cache puis réseau) : on ne la remplace
            // que si l'utilisateur n'a pas encore chargé d'autres pages.
            if (mLoadedPages > 1) return -1;
            mItems.clear();
            mKeys.clear();
            mLoadedPages = 0;
        } else if (page != mLoadedPages + 1) {
            return -1;
        }
        int added = 0;
        for (T item : items) {
            String k = mKeyOf.key(item);
            if (k == null || mKeys.add(k)) {
                mItems.add(item);
                added++;
            }
        }
        mLoadedPages = page;
        mLoading = false;
        mError = false;
        mHasMore = mayHaveMore;
        return added;
    }

    /** Échec réseau d'une page : la liste affichée est conservée, « Réessayer » est proposé. */
    public boolean onError(int generation) {
        if (generation != mGeneration) return false;
        mLoading = false;
        mError = true;
        return true;
    }

    /** « Réessayer » : autorise une nouvelle tentative de la même page. */
    public void clearError() {
        mError = false;
    }

    /** Faut-il charger la suite ? (dernier élément visible proche de la fin) */
    public boolean shouldPrefetch(int lastVisibleIndex, int threshold) {
        return nextPageToLoad() > 0 && mLoadedPages > 0 && lastVisibleIndex >= mItems.size() - 1 - threshold;
    }

    public List<T> items() {
        return Collections.unmodifiableList(mItems);
    }

    public int size() {
        return mItems.size();
    }

    public boolean isLoading() {
        return mLoading;
    }

    public boolean hasMore() {
        return mHasMore;
    }

    public boolean hasError() {
        return mError;
    }

    public int loadedPages() {
        return mLoadedPages;
    }

    /** Premier chargement terminé sans aucun résultat. */
    public boolean isEmptyResult() {
        return mLoadedPages >= 1 && mItems.isEmpty() && !mLoading;
    }
}
