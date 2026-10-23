package com.naniger.elim.model.structure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.naniger.elim.model.author.AuthorJson;
import com.naniger.elim.model.author.BibliographyQuery;
import com.naniger.elim.model.author.Publication;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Liste, fiche et ressources des structures : lecture JSON, images, recherche, tri, filtres. */
public class StructureLogicTest {

    private static final String HOST = "https://api.kawtal-consulting.com/";

    private static final String LIST = "["
            + "{\"id\":2,\"nameStruct\":\"Kit TD\",\"description\":\"Kit TD est une structure d'EduNiger dédiée aux candidats.\\r\\n\\r\\nExercices corrigés.\","
            + "\"logo\":\"logo_1777389030.png\",\"banner\":\"banner_1777389030.png\",\"location\":\"Niamey, Niger\",\"adhererNumber\":12,\"bookNumber\":40,"
            + "\"isAdhere\":true,\"created_at\":null},"
            + "{\"id\":\"3\",\"nameStruct\":\"Université Abdou Moumouni\",\"description\":null,\"logo\":\"eduniger.svg\",\"banner\":\"banner.png\","
            + "\"location\":\"niamey, niger\",\"adhererNumber\":\"250\",\"bookNumber\":3,\"isAdhere\":false,\"created_at\":\"2026-05-01 10:00:00\"},"
            + "{\"id\":7,\"nameStruct\":\"École Élan\",\"logo\":null,\"banner\":null,\"location\":\"Zinder\",\"adhererNumber\":0,\"bookNumber\":0,"
            + "\"isAdhere\":0,\"created_at\":\"2026-09-01 10:00:00\"},"
            + "{\"id\":\"abc\",\"nameStruct\":\"Identifiant invalide\"},"
            + "{\"id\":9,\"nameStruct\":null},"
            + "{\"id\":2,\"nameStruct\":\"Doublon\"},"
            + "null]";

    private List<StructureInfo> list() throws Exception {
        return StructureJson.list(LIST);
    }

    private static List<String> names(List<StructureInfo> l) {
        List<String> out = new ArrayList<>();
        for (StructureInfo s : l) out.add(s.name);
        return out;
    }

    // ------------------------------------------------------------------ JSON

    @Test
    public void listKeepsOnlyValidDistinctStructures() throws Exception {
        List<StructureInfo> l = list();
        assertEquals(Arrays.asList("Kit TD", "Université Abdou Moumouni", "École Élan"), names(l));
        StructureInfo kit = l.get(0);
        assertEquals("2", kit.id);
        assertEquals(40, kit.resources);
        assertEquals(12, kit.members);
        assertTrue(kit.isMember);
        assertEquals("Kit TD est une structure d'EduNiger dédiée aux candidats.\n\nExercices corrigés.", kit.description);
        assertEquals(250, l.get(1).members); // nombre envoyé en texte
        assertFalse(l.get(2).isMember);       // 0 → non membre
        assertNull(l.get(1).description);
        assertNull(kit.createdAt);
    }

    @Test(expected = org.json.JSONException.class)
    public void listRejectsNonArray() throws Exception {
        StructureJson.list("{\"message\":\"Unauthenticated.\"}");
    }

    @Test
    public void detailWithStatsDomainsAndAuthors() throws Exception {
        String body = "{\"id\":2,\"nameStruct\":\"Kit TD\",\"description\":\"Desc\",\"logo\":\"l.png\",\"banner\":\"b.jpg\","
                + "\"adhererNumber\":2,\"bookNumber\":5,\"location\":\"Niger\",\"isAdhere\":true,\"isMemberAdmin\":true,"
                + "\"public_stats\":{\"resources\":5,\"electronic\":4,\"audio\":1,\"physical\":0,\"formats\":2,\"first_added_at\":\"2026-04-28 15:46:41\"},"
                + "\"categories\":[{\"idCategory\":4,\"title\":\"Sciences\",\"books_count\":3},{\"idCategory\":0,\"title\":\"Invalide\"},{\"idCategory\":9,\"title\":null}],"
                + "\"authors\":[{\"idAuthor\":108,\"name\":\"Abdoul Kader\",\"firstName\":\"Bachir\",\"profile\":\"p.png\",\"profession\":null,\"books_count\":4},"
                + "{\"idAuthor\":108,\"name\":\"Doublon\"},{\"name\":\"Sans identifiant\"}],"
                + "\"created_at\":\"2025-01-01 00:00:00\"}";
        StructureInfo s = StructureJson.detail(body);
        assertTrue(s.hasStats);
        assertEquals(5, s.resources);
        assertEquals(4, s.electronic);
        assertEquals(1, s.audio);
        assertEquals(0, s.physical);
        assertTrue(s.isMember);
        assertTrue(s.isMemberAdmin);
        assertEquals(1, s.categories.size());
        assertEquals("Sciences", s.categories.get(0).title);
        assertEquals(1, s.authors.size());
        assertEquals("108", s.authors.get(0).getIdNumber());
        assertEquals("2026-04-28 15:46:41", s.firstAddedAt);
    }

    @Test
    public void detailFromOldApiHasNoStats() throws Exception {
        StructureInfo s = StructureJson.detail("{\"id\":2,\"nameStruct\":\"Kit TD\",\"bookNumber\":7,\"adhererNumber\":1,\"isAdhere\":false}");
        assertFalse(s.hasStats);
        assertEquals(7, s.resources);
        assertTrue(s.categories.isEmpty());
        assertTrue(s.authors.isEmpty());
        assertFalse(s.isMemberAdmin);
    }

    @Test
    public void negativeOrGarbageCountsBecomeZero() throws Exception {
        StructureInfo s = StructureJson.detail("{\"id\":2,\"nameStruct\":\"X\",\"bookNumber\":-3,\"adhererNumber\":\"beaucoup\"}");
        assertEquals(0, s.resources);
        assertEquals(0, s.members);
    }

    // ------------------------------------------------------------------ images & textes

    @Test
    public void defaultAndSvgImagesAreNotShown() {
        assertFalse(StructureText.isCustomImage(null));
        assertFalse(StructureText.isCustomImage("null"));
        assertFalse(StructureText.isCustomImage("eduniger.svg"));
        assertFalse(StructureText.isCustomImage("banner.png"));
        assertFalse(StructureText.isCustomImage("Banner.SVG"));
        assertFalse(StructureText.isCustomImage("logo_1.svg"));
        assertTrue(StructureText.isCustomImage("logo_1777389030.png"));
        assertNull(StructureText.logoUrl(HOST, "2", "eduniger.svg"));
        assertNull(StructureText.bannerUrl(HOST, "abc", "b.png"));
        assertEquals("https://api.kawtal-consulting.com/api/public/resource/2/logo/logo%201.png",
                StructureText.logoUrl(HOST, "2", "logo 1.png"));
        assertEquals("https://api.kawtal-consulting.com/api/public/resource/2/banner/b.jpg",
                StructureText.bannerUrl(HOST, " 2 ", "b.jpg"));
    }

    @Test
    public void validIds() {
        assertTrue(StructureText.isValidId("2"));
        assertTrue(StructureText.isValidId(" 15 "));
        assertFalse(StructureText.isValidId("0"));
        assertFalse(StructureText.isValidId("-1"));
        assertFalse(StructureText.isValidId("3abc"));
        assertFalse(StructureText.isValidId("AddBook"));
        assertFalse(StructureText.isValidId(""));
        assertFalse(StructureText.isValidId(null));
        assertFalse(StructureText.isValidId("12345678901"));
    }

    @Test
    public void initials() {
        assertEquals("KT", StructureText.initials("Kit TD"));
        assertEquals("UA", StructureText.initials("UAZ"));
        assertEquals("ÉÉ", StructureText.initials("École Élan"));
        assertEquals("SB", StructureText.initials("SOS-BAC"));
        assertEquals("?", StructureText.initials("  "));
        assertEquals("?", StructureText.initials(null));
    }

    @Test
    public void summaryAndShare() {
        assertEquals("A B C", StructureText.summary("A\n\nB   C "));
        assertNull(StructureText.summary("  "));
        assertEquals("Découvrez Kit TD sur Elim (Niger). 40 ressources à consulter dans la bibliothèque numérique Elim.\n\nhttps://play",
                StructureText.shareMessage("Kit TD", "Niger", 40, "https://play"));
        assertEquals("Découvrez Kit TD sur Elim.", StructureText.shareMessage("Kit TD", null, 0, null));
        assertEquals("Découvrez Kit TD sur Elim. 1 ressource à consulter dans la bibliothèque numérique Elim.",
                StructureText.shareMessage("Kit TD", " ", 1, ""));
    }

    @Test
    public void urls() {
        assertEquals("https://api.kawtal-consulting.com/api/structures/2", StructureText.detailUrl(HOST, "2"));
        assertEquals("https://api.kawtal-consulting.com/api/structures", StructureText.listUrl(HOST));
        assertEquals("https://api.kawtal-consulting.com/api/structures/2/join", StructureText.membershipUrl(HOST, "2", true));
        assertEquals("https://api.kawtal-consulting.com/api/structures/2/leave", StructureText.membershipUrl(HOST, "2", false));
    }

    // ------------------------------------------------------------------ recherche, filtres, tri

    @Test
    public void searchByNameIgnoresCaseAndAccents() throws Exception {
        List<StructureInfo> l = list();
        assertEquals(Arrays.asList("École Élan"), names(StructureListQuery.initial().withSearch("ecole").apply(l)));
        assertEquals(Arrays.asList("Université Abdou Moumouni"), names(StructureListQuery.initial().withSearch("UNIVERSITE moumouni").apply(l)));
        assertTrue(StructureListQuery.initial().withSearch("introuvable").apply(l).isEmpty());
    }

    @Test
    public void searchByLocation() throws Exception {
        List<StructureInfo> l = list();
        assertEquals(Arrays.asList("Kit TD", "Université Abdou Moumouni"),
                names(StructureListQuery.initial().withSearch("niamey").apply(l)));
        assertEquals(Arrays.asList("École Élan"), names(StructureListQuery.initial().withSearch("zinder").apply(l)));
    }

    @Test
    public void locationFilterGroupsCaseAndAccents() throws Exception {
        List<StructureInfo> l = list();
        assertEquals(Arrays.asList("Niamey, Niger", "Zinder"), StructureListQuery.locations(l));
        assertEquals(Arrays.asList("Kit TD", "Université Abdou Moumouni"),
                names(StructureListQuery.initial().withLocation("NIAMEY, NIGER").apply(l)));
    }

    @Test
    public void locationFilterHiddenWhenUseless() throws Exception {
        List<StructureInfo> one = StructureJson.list("[{\"id\":1,\"nameStruct\":\"A\",\"location\":\"Niger\"},{\"id\":2,\"nameStruct\":\"B\"}]");
        assertTrue(StructureListQuery.locations(one).isEmpty());
    }

    @Test
    public void onlyMine() throws Exception {
        List<StructureInfo> l = list();
        assertEquals(Arrays.asList("Kit TD"), names(StructureListQuery.initial().withOnlyMine(true).apply(l)));
        assertEquals(1, StructureListQuery.memberCount(l));
        assertTrue(StructureListQuery.initial().withOnlyMine(true).isFiltered());
        assertFalse(StructureListQuery.initial().withSort(StructureListQuery.Sort.MEMBERS).isFiltered());
    }

    @Test
    public void sorts() throws Exception {
        List<StructureInfo> l = list();
        assertEquals(Arrays.asList("École Élan", "Kit TD", "Université Abdou Moumouni"),
                names(StructureListQuery.initial().apply(l))); // A→Z sans tenir compte des accents
        assertEquals(Arrays.asList("Kit TD", "Université Abdou Moumouni", "École Élan"),
                names(StructureListQuery.initial().withSort(StructureListQuery.Sort.RESOURCES).apply(l)));
        assertEquals(Arrays.asList("Université Abdou Moumouni", "Kit TD", "École Élan"),
                names(StructureListQuery.initial().withSort(StructureListQuery.Sort.MEMBERS).apply(l)));
        assertEquals(Arrays.asList("École Élan", "Université Abdou Moumouni", "Kit TD"),
                names(StructureListQuery.initial().withSort(StructureListQuery.Sort.RECENT).apply(l))); // sans date : à la fin
    }

    @Test
    public void filtersCombine() throws Exception {
        List<StructureInfo> l = list();
        StructureListQuery q = StructureListQuery.initial().withLocation("Niamey, Niger").withSearch("kit");
        assertEquals(Arrays.asList("Kit TD"), names(q.apply(l)));
        assertTrue(q.withOnlyMine(true).withSearch("universite").apply(l).isEmpty());
    }

    // ------------------------------------------------------------------ ressources

    @Test
    public void resourcesQueryUsesStructureId() {
        BibliographyQuery q = BibliographyQuery.forStructure("2").withSearch("maths").withFormat(BibliographyQuery.Format.PDF);
        assertEquals("https://api.kawtal-consulting.com/api/book/associations?structureId=2&published=1&page=2&limit=20&sort=recent&format=pdf&search=maths",
                q.pageUrl(HOST, 2));
        // Le profil d'auteur est inchangé.
        assertEquals("https://api.kawtal-consulting.com/api/book/associations?authorId=5&published=1&page=1&limit=20&sort=title",
                BibliographyQuery.forAuthor("5").withSort(BibliographyQuery.Sort.TITLE).pageUrl(HOST, 1));
        assertEquals("structureId", q.withCategory(4).withSort(BibliographyQuery.Sort.OLDEST).ownerParam);
    }

    @Test
    public void resourcesKeepOnlyBooksOfTheStructure() throws Exception {
        String body = "["
                + "{\"idBook\":\"TD1\",\"title\":\"Épreuve\",\"blanket\":\"c.png\",\"electronic\":\"e.pdf\",\"isAudio\":0,\"isPhysic\":0,"
                + "\"author\":{\"idAuthor\":5,\"name\":\"Hamidou\",\"firstName\":\"Ridouane\"},"
                + "\"structures\":[{\"id\":3},{\"id\":2}],\"categories\":[{\"title\":\"Maths\"}]},"
                + "{\"idBook\":\"X\",\"title\":\"Autre structure\",\"structures\":[{\"id\":7}]},"
                + "{\"idBook\":\"TD2\",\"title\":\"Sans relation chargée\",\"author\":null}"
                + "]";
        List<Publication> p = AuthorJson.structureResources(body, "2");
        assertEquals(2, p.size());
        assertEquals("TD1", p.get(0).id);
        assertEquals("Ridouane Hamidou", p.get(0).authorName);
        assertEquals("3", p.get(0).structureId); // couverture : dossier de la 1re structure
        assertEquals(Arrays.asList("3", "2"), p.get(0).structureIds);
        assertNull(p.get(1).authorName);
    }
}
