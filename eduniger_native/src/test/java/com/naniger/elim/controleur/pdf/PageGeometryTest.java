package com.naniger.elim.controleur.pdf;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Calculs du lecteur PDF : taille des rendus, bornes du zoom, page courante, saisie. */
public class PageGeometryTest {

    private static final long MAX = 32L * 1024 * 1024;

    @Test
    public void bitmapSize_respecteLeRatioDeLaPage() {
        int[] s = PageGeometry.bitmapSize(595, 842, 1080, 1f, MAX); // A4
        assertEquals(1080, s[0]);
        assertEquals(Math.round(1080 * 842 / 595f), s[1]);
    }

    @Test
    public void bitmapSize_zoomAugmenteLaDefinition() {
        int[] s = PageGeometry.bitmapSize(595, 842, 1000, 2f, MAX);
        assertEquals(2000, s[0]);
    }

    @Test
    public void bitmapSize_plafonneLaMemoire() {
        // Page très haute + zoom fort : ne doit jamais dépasser le plafond.
        int[] s = PageGeometry.bitmapSize(600, 6000, 1440, 4f, MAX);
        assertTrue((long) s[0] * s[1] * 4 <= MAX + 4L * (s[0] + s[1]));
        float ratio = s[1] / (float) s[0];
        assertEquals(10f, ratio, 0.05f);
    }

    @Test
    public void bitmapSize_zoomInferieurAUnIgnore() {
        assertArrayEquals(PageGeometry.bitmapSize(100, 100, 500, 1f, MAX),
                PageGeometry.bitmapSize(100, 100, 500, 0.3f, MAX));
    }

    @Test
    public void bitmapSize_entreesInvalides() {
        assertArrayEquals(new int[]{1, 1}, PageGeometry.bitmapSize(0, 100, 500, 1f, MAX));
        assertArrayEquals(new int[]{1, 1}, PageGeometry.bitmapSize(100, 100, 0, 1f, MAX));
    }

    @Test
    public void clampTranslation_sansZoomToujoursZero() {
        assertEquals(0f, PageGeometry.clampTranslation(-50f, 1000f, 1f), 0f);
        assertEquals(0f, PageGeometry.clampTranslation(80f, 1000f, 1f), 0f);
    }

    @Test
    public void clampTranslation_zoomeResteDansLeContenu() {
        // 2× sur 1000 px : déplacement autorisé entre -1000 et 0.
        assertEquals(-1000f, PageGeometry.clampTranslation(-5000f, 1000f, 2f), 0f);
        assertEquals(0f, PageGeometry.clampTranslation(300f, 1000f, 2f), 0f);
        assertEquals(-400f, PageGeometry.clampTranslation(-400f, 1000f, 2f), 0f);
    }

    @Test
    public void currentPage_premiereVisibleOuSuivante() {
        assertEquals(3, PageGeometry.currentPage(3, 900, 1500, 10));
        assertEquals(4, PageGeometry.currentPage(3, 200, 1500, 10)); // moins d'un tiers visible
        assertEquals(9, PageGeometry.currentPage(9, 10, 1500, 10));  // dernière page
        assertEquals(0, PageGeometry.currentPage(-1, 0, 1500, 10));
        assertEquals(0, PageGeometry.currentPage(0, 0, 1500, 0));
    }

    @Test
    public void parsePageInput_bornesEtErreurs() {
        assertEquals(0, PageGeometry.parsePageInput("1", 10));
        assertEquals(9, PageGeometry.parsePageInput(" 10 ", 10));
        assertEquals(-1, PageGeometry.parsePageInput("0", 10));
        assertEquals(-1, PageGeometry.parsePageInput("11", 10));
        assertEquals(-1, PageGeometry.parsePageInput("abc", 10));
        assertEquals(-1, PageGeometry.parsePageInput("", 10));
        assertEquals(-1, PageGeometry.parsePageInput(null, 10));
        assertEquals(-1, PageGeometry.parsePageInput("99999999999", 10));
    }

    @Test
    public void readingPercent_pagesLuesEtPartDeLaPage() {
        assertEquals(0, PageGeometry.readingPercent(0, 0f, 450));
        assertEquals(29, PageGeometry.readingPercent(131, 0f, 450));   // page 132 / 450
        assertEquals(50, PageGeometry.readingPercent(1, 0.5f, 3));
        assertEquals(100, PageGeometry.readingPercent(9, 1f, 10));
        assertEquals(100, PageGeometry.readingPercent(50, 3f, 10));    // bornes
        assertEquals(-1, PageGeometry.readingPercent(3, 0f, 0));       // inconnu
    }

    @Test
    public void resumePage_resteDansLeDocument() {
        assertEquals(131, PageGeometry.resumePage(131, 450));
        assertEquals(0, PageGeometry.resumePage(0, 450));
        assertEquals(0, PageGeometry.resumePage(-4, 450));
        assertEquals(99, PageGeometry.resumePage(300, 100));  // fichier remplacé, plus court
        assertEquals(0, PageGeometry.resumePage(5, 0));
    }

    @Test
    public void worthResuming_seulementSiOnAAvance() {
        assertFalse(PageGeometry.worthResuming(0, 0f));
        assertFalse(PageGeometry.worthResuming(0, 0.1f));
        assertTrue(PageGeometry.worthResuming(0, 0.6f));
        assertTrue(PageGeometry.worthResuming(12, 0f));
    }
}
