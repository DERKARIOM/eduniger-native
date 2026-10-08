package com.naniger.elim.model.data;

public class ElectronicBook extends Book {
    public ElectronicBook(String id, String cover, String tile, String category, String author, String pdf) {
        super(id,tile,category,author,null);
        mCover = cover;
        mPdf = pdf;
    }
    public ElectronicBook()
    {
        super(null,null);
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getPdf() {
        return mPdf;
    }

    public void setPdf(String pdf) {
        mPdf = pdf;
    }

    /**
     * Etat de telechargement (ElectronicTable.STATUS_DOWNLOADING/COMPLETED/
     * FAILED), ou null si non renseigne (retro-compatibilite : constructeurs
     * appeles avant l'ajout de ce champ). "Mes telechargements" (ContainerActivity
     * case 1) l'utilise pour afficher l'etat reel de chaque livre au lieu de les
     * traiter tous comme entierement telecharges (cf. audit sections 4/6).
     */
    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    private String mPdf;
    private String mStatus;

}
