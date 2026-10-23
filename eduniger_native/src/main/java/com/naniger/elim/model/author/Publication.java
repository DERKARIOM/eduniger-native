package com.naniger.elim.model.author;

import java.util.ArrayList;
import java.util.List;

/** Livre de la bibliographie (une ligne de {@code /api/book/associations}). */
public final class Publication {
    public String id;
    public String title;
    public String description;
    public String cover;
    /** Première structure du livre : sert à construire l'URL de la couverture. */
    public String structureId;
    public final List<String> categories = new ArrayList<>();
    public boolean hasPdf;
    public boolean isAudio;
    public boolean isPhysical;
    public String pages;
    public String addedAt;
}
