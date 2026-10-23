package com.naniger.elim.model.author;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.naniger.elim.model.data.Author;

import org.junit.Test;

import java.util.List;

/**
 * Lecture des réponses réelles de l'API Laravel (forme de AuthorController::show/similar et
 * BookAssociationController::getBooksWithAssociations) pour les cas de test de la mission.
 */
public class AuthorJsonTest {

    /** Auteur avec portrait, biographie, contacts et plusieurs livres. */
    private static final String FULL = "{\"idAuthor\":108,\"name\":\"Abdoul Kader\",\"firstName\":\"Bachir\","
            + "\"profile\":\"derkariom.png\",\"cover\":\"cover_ab12.jpg\",\"level\":2,\"profession\":\"CO-FONDATEUR NINOTECH\","
            + "\"biography\":\"Ingénieur nigérien.\\r\\nFondateur d'Elim.\\r\\n\\r\\n\\r\\nAuteur de manuels.\","
            + "\"call\":\"+227 90 00 00 00\",\"email\":\"auteur@elim.ne\",\"whatsapp\":\"+227 90 00 00 00\","
            + "\"books_count\":14,\"books_count_electronic\":12,\"books_count_physical\":1,\"books_count_audio\":2,"
            + "\"public_stats\":{\"publications\":12,\"electronic\":10,\"audio\":2,\"physical\":1,\"formats\":3,"
            + "\"first_added_at\":\"2025-03-02 10:00:00\",\"last_added_at\":\"2026-10-01 09:00:00\"},"
            + "\"categories\":[{\"idCategory\":4,\"title\":\"Sciences\",\"books_count\":7},"
            + "{\"idCategory\":9,\"title\":\"Mathématiques\",\"books_count\":5},{\"idCategory\":0,\"title\":\"x\"}]}";

    /** Auteur sans portrait, sans biographie, sans contact ni publication. */
    private static final String BARE = "{\"idAuthor\":122,\"name\":\"Abdelkader\",\"firstName\":\"ALIO SANDA\","
            + "\"profile\":null,\"level\":0,\"profession\":null,\"biography\":null,\"call\":null,\"email\":\"null\","
            + "\"whatsapp\":\"\",\"books_count\":0,\"books_count_electronic\":0,\"books_count_physical\":0,"
            + "\"books_count_audio\":0,\"public_stats\":{\"publications\":0,\"electronic\":0,\"audio\":0,"
            + "\"physical\":0,\"formats\":0,\"first_added_at\":null,\"last_added_at\":null},\"categories\":[]}";

    /** API pas encore déployée (sans public_stats ni categories). */
    private static final String LEGACY = "{\"idAuthor\":\"109\",\"name\":\"Ridouane\",\"firstName\":\"Aboubacar\","
            + "\"profile\":\"109.png\",\"profession\":\"AUTEUR\",\"books_count\":\"3\","
            + "\"books_count_electronic\":3,\"books_count_physical\":0,\"books_count_audio\":\"1\"}";

    @Test
    public void authorWithPortraitBioAndBooks() throws Exception {
        AuthorProfile p = AuthorJson.profile(FULL);
        assertEquals("108", p.id);
        assertEquals("Bachir Abdoul Kader", p.fullName());
        assertEquals("derkariom.png", p.profile);
        assertEquals("cover_ab12.jpg", p.cover);
        assertEquals("CO-FONDATEUR NINOTECH", p.profession);
        assertEquals("Ingénieur nigérien.\nFondateur d'Elim.\n\nAuteur de manuels.", p.biography);
        assertEquals("+227 90 00 00 00", p.phone);
        assertEquals("auteur@elim.ne", p.email);
        assertEquals("22790000000", p.whatsapp);
        assertTrue(p.hasPublicStats);
        assertEquals(12, p.publications);          // livres publiés, pas les 14 au total
        assertEquals(10, p.electronic);
        assertEquals(3, p.formats);
        assertEquals("2025", ProfileText.year(p.firstAddedAt));
        assertEquals(2, p.categories.size());       // catégorie sans identifiant écartée
        assertEquals("Sciences", p.categories.get(0).title);
        assertEquals(7, p.categories.get(0).count);
    }

    @Test
    public void authorWithoutPortraitBioOrPublication() throws Exception {
        AuthorProfile p = AuthorJson.profile(BARE);
        assertEquals("ALIO SANDA Abdelkader", p.fullName());
        assertNull(p.profile);       // → image de remplacement
        assertNull(p.cover);         // → couleur Elim du bandeau
        assertNull(p.profession);    // → ligne masquée
        assertNull(p.biography);     // → état « aucune biographie »
        assertNull(p.phone);
        assertNull(p.email);         // « null » texte ignoré
        assertNull(p.whatsapp);
        assertEquals(0, p.publications);
        assertTrue(p.categories.isEmpty());
    }

    @Test
    public void olderApi_fallsBackToHistoricalCounters() throws Exception {
        AuthorProfile p = AuthorJson.profile(LEGACY);
        assertFalse(p.hasPublicStats);
        assertEquals(3, p.publications);
        assertEquals(1, p.audio);
        assertEquals(2, p.formats);
    }

    @Test(expected = org.json.JSONException.class)
    public void invalidBody_isRejected() throws Exception {
        AuthorJson.profile("<html>Erreur 500</html>");
    }

    private static final String PAGE = "["
            + "{\"idBook\":\"B1\",\"title\":\"Le cycle de l'eau\",\"description\":\"Évaporation,\\nnuages, pluie\","
            + "\"idAuthor\":108,\"blanket\":\"cycle.png\",\"electronic\":null,\"isPhysic\":0,\"isAudio\":1,"
            + "\"nbrPage\":null,\"created_at\":\"2026-05-01T10:00:00.000000Z\","
            + "\"categories\":[{\"idCategory\":4,\"title\":\"Sciences\"},{\"idCategory\":4,\"title\":\"sciences\"}],"
            + "\"author\":{\"idAuthor\":108},\"structures\":[{\"id\":3,\"name\":\"Aika\"},{\"id\":7}],\"audioFiles\":[]},"
            + "{\"idBook\":\"B2\",\"title\":\"Épreuve de Maths 2020\",\"description\":null,\"idAuthor\":\"108\","
            + "\"blanket\":\"maths.jpg\",\"electronic\":\"maths.pdf\",\"isPhysic\":\"1\",\"isAudio\":0,"
            + "\"nbrPage\":\"12\",\"categories\":[],\"structures\":[]},"
            + "{\"idBook\":\"X9\",\"title\":\"Livre d'un autre auteur\",\"idAuthor\":200,\"blanket\":\"x.png\"},"
            + "{\"title\":\"Sans identifiant\"}"
            + "]";

    @Test
    public void publications_onlyThisAuthor_withRealFields() throws Exception {
        List<Publication> list = AuthorJson.publications(PAGE, "108");
        assertEquals(2, list.size());                          // X9 (autre auteur) et sans id écartés

        Publication audio = list.get(0);
        assertEquals("B1", audio.id);
        assertEquals("3", audio.structureId);                  // 1re structure seulement (URL de couverture)
        assertEquals(1, audio.categories.size());              // doublon de catégorie retiré
        assertTrue(audio.isAudio);
        assertFalse(audio.hasPdf);
        assertEquals("Évaporation,\nnuages, pluie", audio.description);
        assertEquals("2026", ProfileText.year(audio.addedAt));

        Publication pdf = list.get(1);
        assertTrue(pdf.hasPdf);
        assertTrue(pdf.isPhysical);
        assertNull(pdf.structureId);                           // pas de structure → couverture par défaut
        assertNull(pdf.description);
        assertEquals("12", pdf.pages);
    }

    @Test
    public void similar_mapsToExistingAuthorModel() throws Exception {
        String body = "[{\"idAuthor\":5,\"name\":\"Ridouane\",\"firstName\":\"Hamidou\",\"profile\":null,"
                + "\"profession\":\"Professeur\",\"books_count\":4,\"shared_books_count\":3,"
                + "\"shared_categories\":[\"Sciences\"]},{\"idAuthor\":108,\"name\":\"Moi\",\"firstName\":\"Moi\"},"
                + "{\"idAuthor\":5,\"name\":\"Ridouane\",\"firstName\":\"Hamidou\"},{\"name\":\"sans id\"}]";
        List<Author> all = AuthorJson.similar(body);
        assertEquals(3, all.size());
        List<Author> shown = SimilarAuthors.filter(all, "108", Author::getIdNumber, 12);
        assertEquals(1, shown.size());                         // ni l'auteur consulté, ni le doublon
        assertEquals("5", shown.get(0).getIdNumber());
        assertEquals("", shown.get(0).getProfile());          // portrait absent → remplacement à l'affichage
    }

    @Test
    public void emptySimilarList() throws Exception {
        assertTrue(AuthorJson.similar("[]").isEmpty());
    }
}
