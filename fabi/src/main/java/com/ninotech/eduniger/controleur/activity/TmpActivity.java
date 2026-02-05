package com.ninotech.eduniger.controleur.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ninotech.eduniger.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class TmpActivity extends AppCompatActivity {

    private static final String TAG = "TmpActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private TextView tvFileName;
    private Button btnSelectPdf;
    private Button btnUpload;
    private Uri selectedPdfUri;

    // Lanceur pour sélectionner un fichier PDF
    private final ActivityResultLauncher<Intent> pdfPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            selectedPdfUri = result.getData().getData();
                            if (selectedPdfUri != null) {
                                displayPdfInfo(selectedPdfUri);
                            } else {
                                Toast.makeText(this, "Erreur lors de la sélection du fichier",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tmp);

        // Initialisation des vues
        initViews();

        // Configuration des listeners
        setupListeners();
    }

    /**
     * Initialise les vues de l'interface
     */
    private void initViews() {
        tvFileName = findViewById(R.id.tvFileName);
        btnSelectPdf = findViewById(R.id.btnSelectPdf);
        btnUpload = findViewById(R.id.btnUpload);

        // Vérification que toutes les vues sont présentes
        if (tvFileName == null) {
            Log.e(TAG, "tvFileName est null ! Vérifiez l'ID dans le XML");
        }
        if (btnSelectPdf == null) {
            Log.e(TAG, "btnSelectPdf est null ! Vérifiez l'ID dans le XML");
        }
        if (btnUpload == null) {
            Log.e(TAG, "btnUpload est null ! Vérifiez l'ID dans le XML");
        }
    }

    /**
     * Configure les écouteurs d'événements pour les boutons
     */
    private void setupListeners() {
        if (btnSelectPdf != null) {
            btnSelectPdf.setOnClickListener(v -> checkPermissionAndOpenPicker());
        }

        if (btnUpload != null) {
            btnUpload.setOnClickListener(v -> uploadPdf());
        }
    }

    /**
     * Vérifie les permissions et ouvre le sélecteur de fichiers
     */
    private void checkPermissionAndOpenPicker() {
        // Pour Android 13+ (API 33+), les permissions de stockage ont changé
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Pas besoin de permission pour ACTION_GET_CONTENT sur Android 13+
            openFilePicker();
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Pour Android 6 à 12, vérifier la permission
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Demander la permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                openFilePicker();
            }
        } else {
            // Pour Android 5 et inférieur, pas de permission runtime nécessaire
            openFilePicker();
        }
    }

    /**
     * Ouvre le sélecteur de fichiers pour choisir un PDF
     */
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            pdfPickerLauncher.launch(
                    Intent.createChooser(intent, "Sélectionner un PDF")
            );
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this,
                    "Veuillez installer un gestionnaire de fichiers",
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Aucun gestionnaire de fichiers trouvé", ex);
        }
    }

    /**
     * Affiche les informations du PDF sélectionné
     */
    private void displayPdfInfo(Uri uri) {
        String fileName = getFileName(uri);
        if (tvFileName != null) {
            tvFileName.setText("Fichier sélectionné: " + fileName);
        }
        Toast.makeText(this, "PDF sélectionné avec succès", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "PDF sélectionné: " + fileName + " | URI: " + uri.toString());
    }

    /**
     * Récupère le nom du fichier à partir de son URI
     */
    private String getFileName(Uri uri) {
        String result = null;

        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de la récupération du nom du fichier", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        // Si on n'a pas pu récupérer le nom via le content resolver
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }

        return result != null ? result : "fichier.pdf";
    }

    /**
     * Téléverse le PDF sélectionné
     */
    private void uploadPdf() {
        if (selectedPdfUri == null) {
            Toast.makeText(this, "Veuillez d'abord sélectionner un PDF",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        InputStream inputStream = null;
        try {
            // Lire le fichier PDF
            inputStream = getContentResolver().openInputStream(selectedPdfUri);
            if (inputStream == null) {
                Toast.makeText(this, "Impossible de lire le fichier",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] pdfBytes = new byte[inputStream.available()];
            inputStream.read(pdfBytes);

            String fileName = getFileName(selectedPdfUri);
            long fileSize = pdfBytes.length;

            Log.d(TAG, "Fichier prêt pour l'upload: " + fileName +
                    " (" + fileSize + " octets)");

            // OPTION 1: Sauvegarder localement (pour test)
            saveFileLocally(pdfBytes, fileName);

            // OPTION 2: Téléverser vers un serveur (décommentez et implémentez)
            // uploadToServer(pdfBytes, fileName);

            Toast.makeText(this, "PDF téléversé avec succès!",
                    Toast.LENGTH_SHORT).show();

            // Réinitialiser l'interface
            resetSelection();

        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors du téléversement: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            Log.e(TAG, "Erreur lors du téléversement", e);
        } finally {
            // Fermer le flux
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    Log.e(TAG, "Erreur lors de la fermeture du flux", e);
                }
            }
        }
    }

    /**
     * Sauvegarde le fichier localement (pour test)
     */
    private void saveFileLocally(byte[] data, String fileName) throws Exception {
        File file = new File(getFilesDir(), fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
            Log.d(TAG, "Fichier sauvegardé localement: " + file.getAbsolutePath());
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * Méthode pour téléverser vers un serveur (à implémenter selon vos besoins)
     */
    private void uploadToServer(byte[] pdfBytes, String fileName) {
        // TODO: Implémenter l'upload vers votre serveur
        // Exemple avec Retrofit, OkHttp, ou HttpURLConnection

        Log.d(TAG, "Upload vers serveur à implémenter");

        // Exemple de structure:
        // 1. Créer une requête multipart
        // 2. Ajouter le fichier PDF
        // 3. Envoyer vers votre API
        // 4. Gérer la réponse
    }

    /**
     * Réinitialise la sélection après upload
     */
    private void resetSelection() {
        selectedPdfUri = null;
        if (tvFileName != null) {
            tvFileName.setText("Aucun fichier sélectionné");
        }
    }

    /**
     * Gère le résultat de la demande de permission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée
                Log.d(TAG, "Permission accordée");
                openFilePicker();
            } else {
                // Permission refusée
                Toast.makeText(this,
                        "Permission refusée. Impossible d'accéder aux fichiers.",
                        Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Permission refusée par l'utilisateur");
            }
        }
    }
}