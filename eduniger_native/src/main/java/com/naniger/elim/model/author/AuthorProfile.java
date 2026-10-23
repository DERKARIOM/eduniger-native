package com.naniger.elim.model.author;

import java.util.ArrayList;
import java.util.List;

/**
 * Profil d'auteur tel que renvoyé par {@code GET /api/authors/{id}}. Les champs absents ou
 * « null » restent {@code null} : l'écran masque alors la ligne correspondante, rien n'est inventé.
 */
public final class AuthorProfile {

    /** Catégorie et nombre de livres publiés de l'auteur dans celle-ci. */
    public static final class CategoryCount {
        public final int id;
        public final String title;
        public final int count;

        public CategoryCount(int id, String title, int count) {
            this.id = id;
            this.title = title;
            this.count = count;
        }
    }

    public String id;
    public String firstName;
    public String name;
    public String profile;
    public String profession;
    public String biography;
    public String phone;
    public String email;
    public String whatsapp;

    public int publications;
    public int electronic;
    public int audio;
    public int physical;
    public int formats;
    public String firstAddedAt;
    /** Statistiques « publiées » fournies par l'API (sinon : ancienne API, chiffres partiels). */
    public boolean hasPublicStats;

    public final List<CategoryCount> categories = new ArrayList<>();

    public String fullName() {
        return ProfileText.fullName(firstName, name);
    }
}
