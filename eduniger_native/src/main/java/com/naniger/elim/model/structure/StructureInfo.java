package com.naniger.elim.model.structure;

import com.naniger.elim.model.author.AuthorProfile;
import com.naniger.elim.model.data.Author;

import java.util.ArrayList;
import java.util.List;

/**
 * Structure telle que renvoyée par l'API ({@code /api/structures} et {@code /api/structures/{id}}).
 * Les champs absents restent {@code null} / 0 : rien n'est inventé à l'affichage.
 */
public final class StructureInfo {
    public String id;
    public String name;
    public String description;
    public String logo;
    public String banner;
    public String location;
    public String createdAt;
    /** Ressources rattachées (nombre réel calculé par l'API). */
    public int resources;
    /** Membres (nombre réel calculé par l'API). */
    public int members;
    public boolean isMember;
    /** L'utilisateur courant administre cette structure (fiche uniquement). */
    public boolean isMemberAdmin;

    // Fiche uniquement (public_stats, categories, authors)
    public boolean hasStats;
    public int electronic;
    public int audio;
    public int physical;
    public String firstAddedAt;
    public final List<AuthorProfile.CategoryCount> categories = new ArrayList<>();
    public final List<Author> authors = new ArrayList<>();
}
