package com.ninotech.eduniger.controleur.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.animation.RoundedTransformation;
import com.ninotech.eduniger.controleur.dialog.SimpleOkDialog;
import com.ninotech.eduniger.model.data.Book;
import com.ninotech.eduniger.model.data.Server;
import com.ninotech.eduniger.model.table.Session;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class RegisterAuthorActivity extends AppCompatActivity {

    private static final String TAG = "RegisterAuthorActivity";
    private static final int IMAGE_COMPRESSION_QUALITY = 50;
    private static final MediaType MEDIA_TYPE_PDF = MediaType.parse("application/pdf");
    private static final MediaType MEDIA_TYPE_AUDIO = MediaType.parse("audio/*");

    // Optimisation pour grands fichiers
    private static final int CHUNK_SIZE = 8192; // 8KB chunks pour lecture
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100 MB
    private static final int TIMEOUT_SECONDS = 300; // 5 minutes timeout

    // Views
    private ImageView mCoverImageView;
    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private EditText mCategorieEditText;
    private CheckBox mIsPhysiqueCheckBox;
    private CheckBox mIsPdfCheckBox;
    private CheckBox mIsAudioCheckBox;
    private CheckBox mCertificatCheckBox;
    private TextView mErrorTextView;
    private ProgressBar mWaitProgressBar;
    private ProgressBar mUploadProgressBar;
    private TextView mUploadProgressText;
    private Button mAddButton;
    private LinearLayout mSettingPdfLinearLayout;
    private LinearLayout mSettingAudioLinearLayout;
    private Button btnSelectPdf;
    private TextView tvFileName;
    private Button btnSelectAudio;
    private TextView tvFileAudioName;

    // Data
    private Session mSession;
    private Book mBook;
    private Uri selectedPdfUri;
    private Uri selectedAudioUri;
    private OkHttpClient mHttpClient;
    private Animation pulseAnimImg;
    private boolean isUploading = false;

    // Activity Result Launchers - Utilisation des APIs modernes
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> pdfPickerLauncher;
    private ActivityResultLauncher<Intent> audioPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_author);

        hideActionBar();
        initializeViews();
        initializeData();
        initializeActivityLaunchers();
        setupViewListeners();
        loadDefaultCoverImage();
    }

    private void hideActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void initializeViews() {
        mCoverImageView = findViewById(R.id.image_view_activity_add_book_cover);
        mTitleEditText = findViewById(R.id.edit_text_activity_register_author_title);
        mDescriptionEditText = findViewById(R.id.edit_text_activity_register_author_description);
        mCategorieEditText = findViewById(R.id.edit_text_activity_register_author_categorie);
        mIsPhysiqueCheckBox = findViewById(R.id.check_box_activity_register_author_is_physique);
        mIsPdfCheckBox = findViewById(R.id.check_box_activity_register_author_is_pdf);
        mIsAudioCheckBox = findViewById(R.id.check_box_activity_add_book_is_audio);
        mCertificatCheckBox = findViewById(R.id.check_box_activity_add_book_engagement);
        mErrorTextView = findViewById(R.id.text_view_activity_add_book_error);
        mWaitProgressBar = findViewById(R.id.progress_bar_activity_add_book_whait);
        mAddButton = findViewById(R.id.button_activity_add_book);
        mSettingPdfLinearLayout = findViewById(R.id.linear_layout_activity_register_author_setting_pdf);
        mSettingAudioLinearLayout = findViewById(R.id.linear_layout_activity_add_book_setting_audio);
        btnSelectPdf = findViewById(R.id.btnSelectPdf);
        tvFileName = findViewById(R.id.tvFileName);
        btnSelectAudio = findViewById(R.id.btnSelectAudio);
        tvFileAudioName = findViewById(R.id.tvAudioFileName);
        mUploadProgressBar = findViewById(R.id.progress_bar_upload);
        mUploadProgressText = findViewById(R.id.text_view_upload_progress);
    }

    private void initializeData() {
        mSession = new Session(getApplicationContext());

        // Client HTTP optimisé pour grands fichiers
        mHttpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        mAddButton.setEnabled(false);
        pulseAnimImg = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pulse);
        mCoverImageView.startAnimation(pulseAnimImg);
    }

    private void initializeActivityLaunchers() {
        // Launcher pour la sélection d'image - Utilise ACTION_OPEN_DOCUMENT au lieu de ACTION_PICK
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            handleImageSelection(imageUri);
                        } else {
                            showToast("Erreur lors de la sélection de l'image");
                        }
                    }
                }
        );

        // Launcher pour PDF - Utilise ACTION_OPEN_DOCUMENT
        pdfPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedPdfUri = result.getData().getData();
                        if (selectedPdfUri != null) {
                            // Persister l'accès au fichier
                            persistUriPermission(selectedPdfUri);
                            if (validateFileSize(selectedPdfUri)) {
                                displayPdfInfo(selectedPdfUri);
                            } else {
                                showToast("Fichier trop volumineux (max 100 MB)");
                                selectedPdfUri = null;
                            }
                        } else {
                            showToast("Erreur lors de la sélection du fichier");
                        }
                    }
                }
        );

        // Launcher pour Audio - Utilise ACTION_OPEN_DOCUMENT
        audioPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedAudioUri = result.getData().getData();
                        if (selectedAudioUri != null) {
                            // Persister l'accès au fichier
                            persistUriPermission(selectedAudioUri);
                            if (validateFileSize(selectedAudioUri)) {
                                displayAudioInfo(selectedAudioUri);
                            } else {
                                showToast("Fichier audio trop volumineux (max 100 MB)");
                                selectedAudioUri = null;
                            }
                        } else {
                            showToast("Erreur lors de la sélection du fichier audio");
                        }
                    }
                }
        );
    }

    /**
     * Persiste les permissions d'accès à l'URI
     * Important pour Android 10+ (Scoped Storage)
     */
    private void persistUriPermission(Uri uri) {
        try {
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
        } catch (SecurityException e) {
            // Permission déjà accordée ou non nécessaire
            Log.d(TAG, "URI permission already granted or not needed");
        }
    }

    private void setupViewListeners() {
        mCoverImageView.setOnClickListener(v -> openImagePicker());

        mIsPdfCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                mSettingPdfLinearLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE)
        );

        mIsAudioCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                mSettingAudioLinearLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE)
        );

        mCertificatCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                mAddButton.setEnabled(isChecked && !isUploading)
        );

        btnSelectPdf.setOnClickListener(v -> openPdfPicker());
        btnSelectAudio.setOnClickListener(v -> openAudioPicker());
        mAddButton.setOnClickListener(v -> handleAddBookClick());
    }

    private void loadDefaultCoverImage() {
        Picasso.get()
                .load(R.drawable.img_add_cover)
                .placeholder(R.drawable.img_wait_cover_book)
                .error(R.drawable.img_add_cover)
                .transform(new RoundedTransformation(15, 4))
                .resize(360, 494)
                .into(mCoverImageView);
    }

    private void handleAddBookClick() {
        if (!validateInputs()) return;
        if (isUploading) {
            showToast("Upload en cours, veuillez patienter...");
            return;
        }

        setLoadingState(true);

        mBook = new Book(
                null,
                mTitleEditText.getText().toString().trim(),
                mCategorieEditText.getText().toString().trim(),
                null,
                mDescriptionEditText.getText().toString().trim()
        );

        new AddBookTask(this).execute(
                Server.getUrlApi(getApplicationContext()) + "RegisterAuthor.php",
                mSession.getIdNumber(),
                mBook.getTitle(),
                mBook.getDescription(),
                mBook.getCategory(),
                boolToString(mIsPhysiqueCheckBox.isChecked()),
                tvFileName.getText().toString(),
                tvFileAudioName.getText().toString()
        );
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(mTitleEditText.getText())) {
            mErrorTextView.setText("Le titre est requis");
            return false;
        }
        if (TextUtils.isEmpty(mDescriptionEditText.getText())) {
            mErrorTextView.setText("La description est requise");
            return false;
        }
        if (TextUtils.isEmpty(mCategorieEditText.getText())) {
            mErrorTextView.setText("La catégorie est requise");
            return false;
        }
        mErrorTextView.setText("");
        return true;
    }

    private boolean validateFileSize(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            if (inputStream == null) return false;

            long fileSize = inputStream.available();
            inputStream.close();

            return fileSize <= MAX_FILE_SIZE;
        } catch (Exception e) {
            Log.e(TAG, "Error validating file size", e);
            return false;
        }
    }

    private void setLoadingState(boolean loading) {
        if (loading) {
            mAddButton.setText("");
            mWaitProgressBar.setVisibility(View.VISIBLE);
            mAddButton.setEnabled(false);
            isUploading = true;
        } else {
            mAddButton.setText("Demander à publier");
            mWaitProgressBar.setVisibility(View.INVISIBLE);
            mAddButton.setEnabled(mCertificatCheckBox.isChecked());
            isUploading = false;
        }
    }

    // ==================== Image Handling (Sans permissions) ====================

    /**
     * Ouvre le sélecteur d'image moderne avec ACTION_OPEN_DOCUMENT
     * Ne nécessite AUCUNE permission pour Android 10+
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        // Permet de persister l'accès à l'URI
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        try {
            imagePickerLauncher.launch(Intent.createChooser(intent, "Sélectionner une image"));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e(TAG, "No image picker found", ex);
            showToast("Impossible d'ouvrir le sélecteur d'images");
        }
    }

    /**
     * Traite la sélection d'image via ContentResolver (sans permissions)
     */
    private void handleImageSelection(Uri imageUri) {
        try {
            // Persister les permissions
            persistUriPermission(imageUri);

            // Charger l'image via ContentResolver
            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(
                    getContentResolver(), imageUri);

            mCoverImageView.setImageBitmap(imageBitmap);

            // Arrêter l'animation de pulse
            if (pulseAnimImg != null) {
                mCoverImageView.clearAnimation();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading image", e);
            showToast("Erreur lors du chargement de l'image");
        }
    }

    private byte[] compressImage(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_COMPRESSION_QUALITY,
                byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    // ==================== File Picking (Sans permissions) ====================

    /**
     * Ouvre le sélecteur de PDF avec ACTION_OPEN_DOCUMENT
     * Ne nécessite AUCUNE permission sur Android 10+
     */
    private void openPdfPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Permet de persister l'accès
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        try {
            pdfPickerLauncher.launch(Intent.createChooser(intent, "Sélectionner un PDF"));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e(TAG, "No file manager found", ex);
            showToast("Veuillez installer un gestionnaire de fichiers");
        }
    }

    /**
     * Ouvre le sélecteur audio avec ACTION_OPEN_DOCUMENT
     * Ne nécessite AUCUNE permission sur Android 10+
     */
    private void openAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Permet de persister l'accès
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        try {
            audioPickerLauncher.launch(Intent.createChooser(intent,
                    "Sélectionner un fichier audio"));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e(TAG, "No file manager found", ex);
            showToast("Veuillez installer un gestionnaire de fichiers");
        }
    }

    // ==================== File Info Display ====================

    private void displayPdfInfo(Uri uri) {
        String fileName = getFileName(uri);
        long fileSize = getFileSize(uri);
        String fileSizeStr = formatFileSize(fileSize);

        tvFileName.setText("Fichier: " + fileName + " (" + fileSizeStr + ")");
        showToast("PDF sélectionné: " + fileSizeStr);
        Log.d(TAG, "PDF selected: " + fileName + " - " + fileSizeStr);
        uploadPdf(uri);
    }

    private void displayAudioInfo(Uri uri) {
        String fileName = getFileName(uri);
        long fileSize = getFileSize(uri);
        String fileSizeStr = formatFileSize(fileSize);

        tvFileAudioName.setText("Fichier: " + fileName + " (" + fileSizeStr + ")");
        showToast("Audio sélectionné: " + fileSizeStr);
        Log.d(TAG, "Audio selected: " + fileName + " - " + fileSizeStr);
    }

    @NonNull
    private String getFileName(Uri uri) {
        String result = null;

        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file name", e);
            }
        }

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

    private long getFileSize(Uri uri) {
        long size = 0;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file size", e);
        }
        return size;
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        return String.format("%.2f MB", size / (1024.0 * 1024.0));
    }

    // ==================== File Upload Optimisé ====================

    private void uploadPdf(Uri pdfUri) {
        if (isUploading) {
            showToast("Upload déjà en cours");
            return;
        }

        String serverUrl = Server.getUrlServer(this) + "upload.php";
        isUploading = true;

        runOnUiThread(() -> {
            if (mUploadProgressBar != null) {
                mUploadProgressBar.setVisibility(View.VISIBLE);
                mUploadProgressBar.setProgress(0);
            }
            if (mUploadProgressText != null) {
                mUploadProgressText.setVisibility(View.VISIBLE);
                mUploadProgressText.setText("Préparation...");
            }
        });

        new Thread(() -> {
            try {
                String fileName = getFileName(pdfUri);
                long fileSize = getFileSize(pdfUri);

                RequestBody fileBody = createStreamingRequestBody(pdfUri, MEDIA_TYPE_PDF, fileSize);

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("pdf", fileName, fileBody)
                        .build();

                Request request = new Request.Builder()
                        .url(serverUrl)
                        .post(requestBody)
                        .build();

                mHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e(TAG, "Upload failed", e);
                        isUploading = false;
                        runOnUiThread(() -> {
                            hideUploadProgress();
                            showToast("Échec de l'upload : " + e.getMessage());
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response)
                            throws IOException {
                        isUploading = false;
                        runOnUiThread(() -> hideUploadProgress());
                        handleUploadResponse(response);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error uploading PDF", e);
                isUploading = false;
                runOnUiThread(() -> {
                    hideUploadProgress();
                    showToast("Erreur: " + e.getMessage());
                });
            }
        }).start();
    }

    private RequestBody createStreamingRequestBody(Uri uri, MediaType mediaType, long fileSize) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public long contentLength() {
                return fileSize;
            }

            @Override
            public void writeTo(@NonNull BufferedSink sink) throws IOException {
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(uri);
                    if (inputStream == null) {
                        throw new IOException("Cannot open input stream");
                    }

                    byte[] buffer = new byte[CHUNK_SIZE];
                    long totalBytesRead = 0;
                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        sink.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        final long finalTotalBytesRead = totalBytesRead;

                        runOnUiThread(() -> updateUploadProgress(finalTotalBytesRead, fileSize));

                        sink.flush();
                    }

                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Error closing input stream", e);
                        }
                    }
                }
            }
        };
    }

    private void updateUploadProgress(long uploaded, long total) {
        if (mUploadProgressBar != null && mUploadProgressText != null) {
            int progress = (int) ((uploaded * 100) / total);
            mUploadProgressBar.setProgress(progress);
            mUploadProgressText.setText(String.format("Upload: %d%%", progress));
        }
    }

    private void hideUploadProgress() {
        if (mUploadProgressBar != null) {
            mUploadProgressBar.setVisibility(View.GONE);
        }
        if (mUploadProgressText != null) {
            mUploadProgressText.setVisibility(View.GONE);
        }
    }

    private void handleUploadResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            runOnUiThread(() -> showToast("Erreur HTTP: " + response.code()));
            return;
        }

        String responseBody = response.body() != null ? response.body().string() : "";
        Log.d(TAG, "Upload response: " + responseBody);

        try {
            JSONObject json = new JSONObject(responseBody);

            if (json.has("success") && json.has("message")) {
                boolean success = json.getBoolean("success");
                String message = json.getString("message");

                runOnUiThread(() -> {
                    String prefix = success ? "✅ " : "❌ ";
                    showToast(prefix + message);
                });
            } else {
                throw new JSONException("Missing fields in JSON response");
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error", e);
            runOnUiThread(() -> handleInvalidResponse(responseBody));
        }
    }

    private void handleInvalidResponse(String responseBody) {
        if (responseBody.contains("error") || responseBody.contains("Warning") ||
                responseBody.contains("Notice")) {
            showToast("Erreur PHP détectée");
        } else {
            String preview = responseBody.substring(0, Math.min(50, responseBody.length()));
            showToast("Réponse serveur invalide: " + preview);
        }
    }

    // ==================== AsyncTask ====================

    private static class AddBookTask extends AsyncTask<String, Void, String> {
        private final WeakReference<RegisterAuthorActivity> activityRef;

        AddBookTask(RegisterAuthorActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(String... params) {
            RegisterAuthorActivity activity = activityRef.get();
            if (activity == null || activity.mHttpClient == null) return null;

            try {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber", params[1])
                        .addFormDataPart("title", params[2])
                        .addFormDataPart("description", params[3])
                        .addFormDataPart("category", params[4])
                        .addFormDataPart("isPhisic", params[5])
                        .addFormDataPart("pdf", params[6])
                        .addFormDataPart("audio", params[7])
                        .build();

                Request request = new Request.Builder()
                        .url(params[0])
                        .post(requestBody)
                        .build();

                try (Response response = activity.mHttpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        return response.body().string();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Request failed", e);
                }

            } catch (Exception e) {
                Log.e(TAG, "Unexpected error", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            RegisterAuthorActivity activity = activityRef.get();
            if (activity == null) return;

            activity.setLoadingState(false);

            if (response != null && "true".equals(response)) {
                activity.showSuccessDialog();
            } else {
                activity.mErrorTextView.setText(R.string.no_connection);
            }
        }
    }

    // ==================== Dialogs ====================

    private void showSuccessDialog() {
        SimpleOkDialog dialog = new SimpleOkDialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        TextView messageTextView = dialog.findViewById(R.id.text_view_dialog_simple_ok_message);
        messageTextView.setText("Votre livre sera vérifié dans les plus brefs délais. " +
                "Vous recevrez une notification dès sa validation.");

        TextView okTextView = dialog.findViewById(R.id.text_view_dialog_simple_ok);
        okTextView.setOnClickListener(v -> {
            mErrorTextView.setText("");
            dialog.dismiss();
        });

        dialog.build();
    }

    // ==================== Utilities ====================

    private String boolToString(boolean bool) {
        return bool ? "1" : "0";
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCoverImageView != null) {
            mCoverImageView.clearAnimation();
        }
    }
}