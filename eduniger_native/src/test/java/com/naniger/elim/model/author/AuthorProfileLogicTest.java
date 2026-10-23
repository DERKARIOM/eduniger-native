package com.naniger.elim.model.author;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Logique du profil d'auteur : requêtes, textes, pagination, auteurs similaires. */
public class AuthorProfileLogicTest {

    private static final String HOST = "https://api.kawtal-consulting.com/";

    // ------------------------------------------------------------- requêtes

    @Test
    public void firstPage_onlyPublishedBooksOfThisAuthor_sortedRecent() {
        String url = BibliographyQuery.forAuthor("108").pageUrl(HOST, 1);
        assertEquals("https://api.kawtal-consulting.com/api/book/associations?authorId=108&published=1"
                + "&page=1&limit=20&sort=recent", url);
    }

    @Test
    public void searchSortAndFilters_areEncodedInUrl() {
        BibliographyQuery q = BibliographyQuery.forAuthor("108")
                .withSearch("  Épreuve & Bac 2020 ")
                .withSort(BibliographyQuery.Sort.TITLE)
                .withFormat(BibliographyQuery.Format.AUDIO)
                .withCategory(12);
        String url = q.pageUrl(HOST, 3);
        assertTrue(url.contains("&page=3&limit=20&sort=title&format=audio&categoryId=12"));
        assertTrue(url.endsWith("&search=%C3%89preuve%20%26%20Bac%202020"));
        assertTrue(q.isFiltered());
    }

    @Test
    public void sortAloneIsNotAFilter() {
        assertFalse(BibliographyQuery.forAuthor("1").withSort(BibliographyQuery.Sort.TITLE_DESC).isFiltered());
        assertFalse(BibliographyQuery.forAuthor("1").withSearch("   ").isFiltered());
    }

    @Test
    public void invalidAuthorIds_areRejectedBeforeAnyRequest() {
        assertTrue(BibliographyQuery.isValidAuthorId("108"));
        assertTrue(BibliographyQuery.isValidAuthorId(" 7 "));
        assertFalse(BibliographyQuery.isValidAuthorId(null));
        assertFalse(BibliographyQuery.isValidAuthorId(""));
        assertFalse(BibliographyQuery.isValidAuthorId("null"));
        assertFalse(BibliographyQuery.isValidAuthorId("0"));
        assertFalse(BibliographyQuery.isValidAuthorId("-3"));
        assertFalse(BibliographyQuery.isValidAuthorId("12abc"));
        assertFalse(BibliographyQuery.isValidAuthorId("1 OR 1=1"));
        assertFalse(BibliographyQuery.isValidAuthorId("99999999999"));
    }

    @Test
    public void profileAndSimilarUrls() {
        assertEquals("https://api.kawtal-consulting.com/api/authors/108", BibliographyQuery.profileUrl(HOST, "108"));
        assertEquals("https://api.kawtal-consulting.com/api/authors/108/similar", BibliographyQuery.similarUrl(HOST, "108"));
    }

    @Test
    public void coverUrl_publicRoute_orNullWithoutCover() {
        assertEquals("https://api.kawtal-consulting.com/api/public/resource/0/author_cover/cover_ab%2012.jpg",
                BibliographyQuery.coverUrl(HOST, "cover_ab 12.jpg"));
        assertNull(BibliographyQuery.coverUrl(HOST, null));
        assertNull(BibliographyQuery.coverUrl(HOST, "null"));
        assertNull(BibliographyQuery.coverUrl(HOST, "  "));
    }

    // ------------------------------------------------------------- textes

    @Test
    public void clean_dropsNullLikeValues() {
        assertNull(ProfileText.clean(null));
        assertNull(ProfileText.clean("  "));
        assertNull(ProfileText.clean("null"));
        assertNull(ProfileText.clean("NULL"));
        assertEquals("Auteur", ProfileText.clean(" Auteur "));
    }

    @Test
    public void fullName_withMissingOrDuplicateParts() {
        assertEquals("Bachir Abdoul Kader", ProfileText.fullName("Bachir", "Abdoul Kader"));
        assertEquals("Elim", ProfileText.fullName("Elim", "elim"));
        assertEquals("Ridouane", ProfileText.fullName(null, "Ridouane"));
        assertEquals("Ridouane", ProfileText.fullName("Ridouane", "null"));
        assertEquals("", ProfileText.fullName("null", " "));
    }

    @Test
    public void biography_keepsParagraphs_removesNoise() {
        String raw = "Né à Niamey.  \r\nProfesseur.\r\n\r\n\r\n\r\nAuteur de 3 livres.\u0007\t ";
        assertEquals("Né à Niamey.\nProfesseur.\n\nAuteur de 3 livres.", ProfileText.biography(raw));
        assertNull(ProfileText.biography(" \n \n "));
        assertNull(ProfileText.biography("null"));
    }

    @Test
    public void biography_isPlainText_htmlIsNotInterpreted() {
        // Le texte est affiché tel quel (setText), aucune balise n'est exécutée ni retirée.
        assertEquals("<b>Gras</b> <script>x</script>", ProfileText.biography("<b>Gras</b> <script>x</script>"));
    }

    @Test
    public void formats_onlyRealOnes() {
        assertEquals("PDF · Audio", ProfileText.formats(3, 1, 0, "PDF", "Audio", "Papier"));
        assertEquals("", ProfileText.formats(0, 0, 0, "PDF", "Audio", "Papier"));
    }

    @Test
    public void distinctCategories() {
        assertEquals(Arrays.asList("Sciences", "Maths"),
                ProfileText.distinct(Arrays.asList("Sciences", "sciences", null, " ", "Maths", "null")));
    }

    @Test
    public void year_fromApiDates() {
        assertEquals("2026", ProfileText.year("2026-10-12 15:00:00"));
        assertEquals("2025", ProfileText.year("2025-01-02T10:00:00.000000Z"));
        assertNull(ProfileText.year("abc"));
        assertNull(ProfileText.year(null));
    }

    @Test
    public void contacts_areValidatedBeforeShowingButtons() {
        assertEquals("+227 90 00 00 00", ProfileText.phone("+227 90 00 00 00"));
        assertNull(ProfileText.phone("null"));
        assertNull(ProfileText.phone("12"));
        assertEquals("22790000000", ProfileText.whatsappDigits("+227 90-00-00-00"));
        assertEquals("a@b.ne", ProfileText.email("a@b.ne"));
        assertNull(ProfileText.email("pas un mail"));
        assertNull(ProfileText.email("x@y"));
    }

    @Test
    public void shareMessage_onlyRealInformation() {
        String m = ProfileText.shareMessage("Bachir Abdoul Kader", "Co-fondateur", 12, "https://play.google.com/x");
        assertEquals("Découvrez Bachir Abdoul Kader sur Elim (Co-fondateur). 12 publications à lire dans la "
                + "bibliothèque numérique Elim.\n\nhttps://play.google.com/x", m);
        assertEquals("Découvrez Elim sur Elim.", ProfileText.shareMessage("Elim", "null", 0, null));
    }

    // ------------------------------------------------------------- pagination

    private static List<String> page(int from, int count) {
        List<String> l = new ArrayList<>();
        for (int i = 0; i < count; i++) l.add("b" + (from + i));
        return l;
    }

    private static PagedList<String> list() {
        return new PagedList<>(s -> s);
    }

    @Test
    public void longBibliography_loadsPageByPage() {
        PagedList<String> l = list();
        int g = l.reset();
        assertEquals(1, l.nextPageToLoad());
        l.markLoading();
        assertEquals(-1, l.nextPageToLoad());       // pas de double requête
        assertEquals(20, l.onPage(g, 1, page(0, 20), BibliographyQuery.mayHaveMore(20)));
        assertTrue(l.shouldPrefetch(16, 4));
        assertFalse(l.shouldPrefetch(5, 4));
        assertEquals(2, l.nextPageToLoad());
        l.markLoading();
        l.onPage(g, 2, page(20, 20), true);
        l.markLoading();
        l.onPage(g, 3, page(40, 7), BibliographyQuery.mayHaveMore(7));
        assertEquals(47, l.size());
        assertFalse(l.hasMore());
        assertEquals(-1, l.nextPageToLoad());       // fin de liste
    }

    @Test
    public void newSearch_ignoresLateAnswersOfPreviousSearch() {
        PagedList<String> l = list();
        int g1 = l.reset();
        l.markLoading();
        int g2 = l.reset();                          // l'utilisateur tape une autre recherche
        assertEquals(-1, l.onPage(g1, 1, page(0, 20), true));
        assertEquals(0, l.size());
        assertEquals(3, l.onPage(g2, 1, page(100, 3), false));
        assertEquals(Arrays.asList("b100", "b101", "b102"), l.items());
    }

    @Test
    public void duplicatesBetweenPages_areDropped() {
        PagedList<String> l = list();
        int g = l.reset();
        l.onPage(g, 1, page(0, 20), true);
        // Un livre ajouté entre-temps décale la pagination : b19 revient en page 2.
        assertEquals(19, l.onPage(g, 2, page(19, 20), true));
        assertEquals(39, l.size());
    }

    @Test
    public void cachedThenFreshFirstPage_replacesOnlyIfNoOtherPageLoaded() {
        PagedList<String> l = list();
        int g = l.reset();
        l.onPage(g, 1, page(0, 20), true);           // cache
        assertEquals(20, l.onPage(g, 1, page(50, 20), true)); // réseau : remplace
        assertEquals("b50", l.items().get(0));
        l.onPage(g, 2, page(70, 20), true);
        assertEquals(-1, l.onPage(g, 1, page(0, 20), true));  // ne casse pas la page 2
        assertEquals(40, l.size());
    }

    @Test
    public void errorThenRetry_keepsLoadedItems() {
        PagedList<String> l = list();
        int g = l.reset();
        l.onPage(g, 1, page(0, 20), true);
        l.markLoading();
        assertTrue(l.onError(g));
        assertTrue(l.hasError());
        assertEquals(-1, l.nextPageToLoad());
        assertEquals(20, l.size());
        l.clearError();
        assertEquals(2, l.nextPageToLoad());
    }

    @Test
    public void authorWithoutPublication_isEmptyResult() {
        PagedList<String> l = list();
        int g = l.reset();
        assertFalse(l.isEmptyResult());
        l.markLoading();
        l.onPage(g, 1, Collections.<String>emptyList(), false);
        assertTrue(l.isEmptyResult());
    }

    // ------------------------------------------------------------- similaires

    @Test
    public void similarAuthors_excludeCurrent_noDuplicates_capped() {
        List<String> ids = Arrays.asList("5", "108", "7", "5", " ", null, "9", "10");
        List<String> out = SimilarAuthors.filter(ids, "108", s -> s, 3);
        assertEquals(Arrays.asList("5", "7", "9"), out);
        assertTrue(SimilarAuthors.filter(Collections.singletonList("108"), "108", s -> s, 10).isEmpty());
        assertTrue(SimilarAuthors.filter(null, "108", (String s) -> s, 10).isEmpty());
    }
}
