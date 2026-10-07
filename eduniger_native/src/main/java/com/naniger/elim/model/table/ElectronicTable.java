package com.naniger.elim.model.table;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ElectronicTable extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "data.db";
    public static final String NAME_TABLE = "Electronic";

    // ── États de téléchargement (cf. audit "Recommandé"/téléchargement) ──────
    // IDLE n'est jamais persisté : l'absence de ligne EST l'état IDLE.
    // PAUSED/CANCELLED sont définis pour un usage futur (aucune action UI ne
    // les déclenche pour l'instant) ; DELETED n'est pas non plus persisté, la
    // suppression retire directement la ligne (cf. remove()).
    public static final String STATUS_DOWNLOADING = "DOWNLOADING";
    public static final String STATUS_COMPLETED   = "COMPLETED";
    public static final String STATUS_FAILED      = "FAILED";
    public static final String STATUS_PAUSED      = "PAUSED";
    public static final String STATUS_CANCELLED   = "CANCELLED";

    // IMPORTANT : ne PAS augmenter ce numéro de version. "data.db" est un
    // fichier SQLite PARTAGÉ entre toutes les classes SQLiteOpenHelper du
    // projet (AudioTable, PlaybackTable, NotificationTable, UserTable...),
    // chacune ouverte indépendamment avec sa PROPRE version alors que
    // PRAGMA user_version est UNIQUE pour tout le fichier. Si cette classe
    // ouvrait la base avec version=2 alors qu'une autre l'ouvre encore avec
    // version=1, SQLiteOpenHelper lève "Can't downgrade database from
    // version 2 to 1" dès que l'autre table est utilisée -> crash garanti.
    // (NotificationTable porte encore la trace de ce piège : un commentaire
    // "version 2 pour déclencher onUpgrade" à côté d'un DB_VERSION resté à 1.)
    // Les nouvelles colonnes ci-dessous sont donc ajoutées de façon idempotente
    // via ensureSchema()/PRAGMA table_info, indépendamment du mécanisme
    // version/onUpgrade de SQLiteOpenHelper.
    private static volatile boolean sSchemaChecked = false;

    public ElectronicTable(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + NAME_TABLE +
                "(\n" +
                "    idElectronic INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                "    idNumberElectronic VARCHAR(100) NOT NULL,\n" +
                "    idBookElectronic VARCHAR(100) NOT NULL,\n" +
                "    descriptionElectronic VARCHAR(100) NOT NULL,\n" +
                "    authorElectronic VARCHAR(100) NOT NULL,\n" +
                "    coverElectronic VARCHAR(100) NOT NULL,\n" +
                "    electronic VARCHAR(100) NOT NULL,\n" +
                "    categoryElectronic VARCHAR(100),\n" +
                "    titleElectronic VARCHAR(100),\n" +
                "    coverCategoryElectronic VARCHAR(100) NOT NULL,\n" +
                "    profileAuthorElectronic VARCHAR(100)," +
                "    dateDownload DATE NOT NULL,\n" +
                "    statusElectronic VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',\n" +
                "    fileSizeElectronic INTEGER NOT NULL DEFAULT 0,\n" +
                "    checksumElectronic VARCHAR(100),\n" +
                "    idStructElectronic VARCHAR(100),\n" +
                "    UNIQUE(idNumberElectronic,idBookElectronic)\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // Ne PAS DROP : un DROP ici supprimerait les métadonnées de tous les
        // livres déjà téléchargés (fichiers PDF orphelins sur le disque, plus
        // aucune trace dans "Mes téléchargements") à la moindre évolution de
        // schéma. Conservé pour compatibilité mais ne devrait plus être
        // atteint tant que la version SQLiteOpenHelper reste à 1 (cf. note
        // ci-dessus) : ensureSchema() gère désormais les évolutions.
        ensureSchema(db);
    }

    /**
     * Ajoute de façon idempotente les colonnes introduites après la création
     * initiale de la table (statut de téléchargement, taille, checksum,
     * structure d'origine), sans dépendre du mécanisme version/onUpgrade de
     * SQLiteOpenHelper (cf. note sur DATABASE_NAME partagé ci-dessus).
     * Sans danger à ré-exécuter : ALTER TABLE ADD COLUMN échoue proprement
     * (SQLiteException interceptée) si la colonne existe déjà.
     */
    private void ensureSchema(SQLiteDatabase db) {
        Set<String> existing = new HashSet<>();
        Cursor info = null;
        try {
            info = db.rawQuery("PRAGMA table_info(" + NAME_TABLE + ")", null);
            int nameIdx = info.getColumnIndex("name");
            while (info.moveToNext()) {
                existing.add(info.getString(nameIdx));
            }
        } catch (Exception e) {
            // Table pas encore créée ou illisible : rien à faire ici, onCreate()
            // s'en charge le cas échéant.
            return;
        } finally {
            if (info != null) info.close();
        }

        if (existing.isEmpty()) return;

        addColumnIfMissing(db, existing, "statusElectronic", "VARCHAR(20) NOT NULL DEFAULT 'COMPLETED'");
        addColumnIfMissing(db, existing, "fileSizeElectronic", "INTEGER NOT NULL DEFAULT 0");
        addColumnIfMissing(db, existing, "checksumElectronic", "VARCHAR(100)");
        addColumnIfMissing(db, existing, "idStructElectronic", "VARCHAR(100)");
    }

    private void addColumnIfMissing(SQLiteDatabase db, Set<String> existingColumns,
                                     String column, String definition) {
        if (existingColumns.contains(column)) return;
        try {
            db.execSQL("ALTER TABLE " + NAME_TABLE + " ADD COLUMN " + column + " " + definition);
        } catch (SQLiteException e) {
            // Déjà ajoutée par un appel concurrent, ou colonne effectivement
            // déjà présente : sans conséquence, on continue.
        }
    }

    private SQLiteDatabase openForRead() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (!sSchemaChecked) {
            ensureSchema(db);
            sSchemaChecked = true;
        }
        return db;
    }

    private SQLiteDatabase openForWrite() {
        SQLiteDatabase db = this.getWritableDatabase();
        if (!sSchemaChecked) {
            ensureSchema(db);
            sSchemaChecked = true;
        }
        return db;
    }

    public Cursor getData()
    {
        SQLiteDatabase db = openForRead();
        return db.rawQuery("SELECT * FROM " + NAME_TABLE, null);
    }

    public Cursor getData(String idNumber)
    {
        SQLiteDatabase db = openForRead();
        return db.query(NAME_TABLE,
                null, // toutes les colonnes
                "idNumberElectronic = ?",
                new String[]{idNumber},
                null, null,
                "dateDownload DESC"); // ORDER BY
    }

    // Filtrees sur STATUS_COMPLETED (comme getCategoryData/getAuthorData/getNbr*) :
    // ces listes locales par categorie/auteur supposaient historiquement que
    // toute ligne presente = un livre entierement telecharge. Depuis que
    // insertPending() cree une ligne DOWNLOADING des le debut du telechargement
    // (necessaire pour "Mes telechargements", cf. getData(idNumber) qui reste
    // volontairement non filtree), ne pas filtrer ici ferait apparaitre des
    // telechargements en cours/echoues dans ces ecrans qui n'ont ni badge d'etat
    // ni action retry (audit section "coherence des etats").
    public Cursor getDataC(String idNumber , String category)
    {
        SQLiteDatabase db = openForRead();
        return db.query(NAME_TABLE, null,
                "categoryElectronic = ? AND idNumberElectronic = ? AND statusElectronic = ?",
                new String[]{category, idNumber, STATUS_COMPLETED}, null, null, null);
    }
    public Cursor getDataA(String idNumber , String author)
    {
        SQLiteDatabase db = openForRead();
        return db.query(NAME_TABLE, null,
                "authorElectronic = ? AND idNumberElectronic = ? AND statusElectronic = ?",
                new String[]{author, idNumber, STATUS_COMPLETED}, null, null, null);
    }
    public Cursor getCategoryData(String idNumber)
    {
        SQLiteDatabase db = openForRead();
        return db.rawQuery("SELECT DISTINCT coverCategoryElectronic,categoryElectronic FROM "
                + NAME_TABLE + " WHERE idNumberElectronic=? AND statusElectronic=?",
                new String[]{idNumber, STATUS_COMPLETED});
    }
    public Cursor getAuthorData(String idNumber)
    {
        SQLiteDatabase db = openForRead();
        return db.rawQuery("SELECT DISTINCT profileAuthorElectronic,authorElectronic FROM "
                + NAME_TABLE + " WHERE idNumberElectronic=? AND statusElectronic=?",
                new String[]{idNumber, STATUS_COMPLETED});
    }

    /**
     * Chemin local du PDF pour un livre donné (utilisé une fois le
     * téléchargement terminé). Retourne null si aucune ligne ne correspond
     * (au lieu de lever une CursorIndexOutOfBoundsException comme
     * auparavant lorsque le curseur était vide).
     */
    public String getPdf(String idBook)
    {
        SQLiteDatabase db = openForRead();
        Cursor res = db.query(NAME_TABLE, new String[]{"electronic"},
                "idBookElectronic = ?", new String[]{idBook}, null, null, null);
        try {
            if (res.moveToFirst()) return res.getString(0);
            return null;
        } finally {
            res.close();
        }
    }

    public String isExist(String idNumber , String idBook)
    {
        SQLiteDatabase db = openForRead();
        Cursor res = db.query(NAME_TABLE, new String[]{"electronic", "statusElectronic"},
                "idNumberElectronic = ? AND idBookElectronic = ?",
                new String[]{idNumber, idBook}, null, null, null);
        try {
            if (res.moveToFirst() && STATUS_COMPLETED.equals(res.getString(1)))
                return res.getString(0);
            return "false";
        } finally {
            res.close();
        }
    }

    /**
     * Statut de téléchargement pour ce livre (DOWNLOADING/COMPLETED/FAILED),
     * ou null si aucun téléchargement n'a jamais été démarré pour ce livre
     * (état IDLE implicite).
     */
    public String getStatus(String idNumber, String idBook) {
        SQLiteDatabase db = openForRead();
        Cursor res = db.query(NAME_TABLE, new String[]{"statusElectronic"},
                "idNumberElectronic = ? AND idBookElectronic = ?",
                new String[]{idNumber, idBook}, null, null, null);
        try {
            if (res.moveToFirst()) return res.getString(0);
            return null;
        } finally {
            res.close();
        }
    }

    public int getNbrElectronic(String idNumber)
    {
        SQLiteDatabase db = openForRead();
        Cursor res = db.rawQuery("SELECT COUNT(*) FROM " + NAME_TABLE
                + " WHERE idNumberElectronic=? AND statusElectronic=?",
                new String[]{idNumber, STATUS_COMPLETED});
        try {
            res.moveToFirst();
            return res.getInt(0);
        } finally {
            res.close();
        }
    }

    public int getNbrAuthor(String idNumber)
    {
        SQLiteDatabase db = openForRead();
        Cursor res = db.rawQuery("SELECT COUNT(DISTINCT authorElectronic) FROM " + NAME_TABLE
                + " WHERE idNumberElectronic=? AND statusElectronic=?",
                new String[]{idNumber, STATUS_COMPLETED});
        try {
            res.moveToFirst();
            return res.getInt(0);
        } finally {
            res.close();
        }
    }

    public int getNbrCategory(String idNumber)
    {
        SQLiteDatabase db = openForRead();
        Cursor res = db.rawQuery("SELECT COUNT(DISTINCT categoryElectronic) FROM " + NAME_TABLE
                + " WHERE idNumberElectronic=? AND statusElectronic=?",
                new String[]{idNumber, STATUS_COMPLETED});
        try {
            res.moveToFirst();
            return res.getInt(0);
        } finally {
            res.close();
        }
    }

    public boolean remove(String idNumber , String idBook)
    {
        SQLiteDatabase db = openForWrite();
        db.delete(NAME_TABLE, "idNumberElectronic = ? AND idBookElectronic = ?",
                new String[]{idNumber, idBook});
        return true;
    }

    /**
     * Crée (ou remplace, si une tentative précédente pour le même livre
     * existait déjà - retéléchargement après échec par exemple) une ligne à
     * l'état DOWNLOADING, AVANT même que le réseau ne soit sollicité. C'est
     * cette ligne qui permet à "Mes téléchargements" et à l'écran du livre
     * de refléter un téléchargement réellement en cours, y compris après un
     * redémarrage de l'app pendant lequel le service de téléchargement
     * aurait été tué (cf. audit : auparavant aucune ligne n'existait avant
     * la fin - succès ou échec - du téléchargement).
     *
     * cover/electronic/coverCategory/profileAuthor doivent être les chemins
     * locaux CIBLES (déterministes, cf. DownloadFile) même si les fichiers
     * ne sont pas encore intégralement écrits.
     */
    public boolean insertPending(String idNumber, String idBook, String description, String author,
                                  String cover, String electronic, String category, String title,
                                  String coverCategory, String profileAuthor, String idStruct)
    {
        SQLiteDatabase db = openForWrite();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        ContentValues values = new ContentValues();
        values.put("idNumberElectronic", idNumber);
        values.put("idBookElectronic", idBook);
        values.put("descriptionElectronic", description);
        values.put("authorElectronic", author);
        values.put("coverElectronic", cover);
        values.put("electronic", electronic);
        values.put("categoryElectronic", category);
        values.put("titleElectronic", title);
        values.put("coverCategoryElectronic", coverCategory);
        values.put("profileAuthorElectronic", profileAuthor);
        values.put("dateDownload", sdf.format(new Date()));
        values.put("statusElectronic", STATUS_DOWNLOADING);
        values.put("fileSizeElectronic", 0);
        values.put("idStructElectronic", idStruct);
        try {
            // CONFLICT_REPLACE : une tentative précédente (FAILED, ou même
            // COMPLETED que l'utilisateur retélécharge) pour ce couple
            // (idNumber, idBook) est remplacée plutôt que de violer la
            // contrainte UNIQUE(idNumberElectronic, idBookElectronic).
            return db.insertWithOnConflict(NAME_TABLE, null, values,
                    SQLiteDatabase.CONFLICT_REPLACE) != -1;
        } catch (SQLiteException e) {
            return false;
        }
    }

    /** Marque le téléchargement en cours comme terminé avec succès. */
    public boolean markCompleted(String idNumber, String idBook, long fileSize, String checksum)
    {
        SQLiteDatabase db = openForWrite();
        ContentValues values = new ContentValues();
        values.put("statusElectronic", STATUS_COMPLETED);
        values.put("fileSizeElectronic", fileSize);
        values.put("checksumElectronic", checksum);
        int rows = db.update(NAME_TABLE, values,
                "idNumberElectronic = ? AND idBookElectronic = ?",
                new String[]{idNumber, idBook});
        return rows > 0;
    }

    /**
     * Marque le téléchargement en cours comme échoué : la ligne est
     * CONSERVÉE (pas supprimée) pour que "Mes téléchargements" puisse
     * afficher "Échec" et proposer de réessayer, au lieu de faire
     * disparaître silencieusement toute trace de la tentative.
     */
    public boolean markFailed(String idNumber, String idBook)
    {
        SQLiteDatabase db = openForWrite();
        ContentValues values = new ContentValues();
        values.put("statusElectronic", STATUS_FAILED);
        int rows = db.update(NAME_TABLE, values,
                "idNumberElectronic = ? AND idBookElectronic = ?",
                new String[]{idNumber, idBook});
        return rows > 0;
    }

    /**
     * Ancienne méthode d'insertion, conservée pour compatibilité de
     * signature mais qui délègue désormais à markCompleted() après un
     * insertPending() implicite (insère directement à l'état COMPLETED :
     * utile pour un appelant qui ne suit pas encore le cycle
     * pending -> completed/failed).
     */
    public boolean insert (String idNumber , String idBook , String description , String author ,String cover, String electronic , String category , String title ,String coverCategory,String profileAuthor)
    {
        insertPending(idNumber, idBook, description, author, cover, electronic, category, title,
                coverCategory, profileAuthor, null);
        return markCompleted(idNumber, idBook, 0, null);
    }
}
