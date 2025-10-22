package com.ninotech.fabi.controleur.activity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.controleur.dialog.SimpleOkDialog;
import com.ninotech.fabi.model.data.Book;
import com.ninotech.fabi.model.table.Session;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterAuthorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_author);
        Objects.requireNonNull(getSupportActionBar()).hide();
        mCoverImageView = findViewById(R.id.image_view_activity_add_book_cover);
        mTitleEditText = findViewById(R.id.edit_text_activity_register_author_title);
        mDescriptionEditText = findViewById(R.id.edit_text_activity_register_author_description);
        mIsPhysiqueCheckBox = findViewById(R.id.check_box_activity_register_author_is_physique);
        mIsPdfCheckBox = findViewById(R.id.check_box_activity_register_author_is_pdf);
        mIsAudioCheckBox = findViewById(R.id.check_box_activity_add_book_is_audio);
        mErrorTextView = findViewById(R.id.text_view_activity_add_book_error);
        mWaitProgressBar = findViewById(R.id.progress_bar_activity_add_book_whait);
        mAddButton = findViewById(R.id.button_activity_add_book);
        mSettingPdfLinearLayout = findViewById(R.id.linear_layout_activity_register_author_setting_pdf);
        mSettingAudioLinearLayout = findViewById(R.id.linear_layout_activity_add_book_setting_audio);
        btnSelectPdf = findViewById(R.id.btnSelectPdf);
        tvFileName = findViewById(R.id.tvFileName);
        btnSelectAudio = findViewById(R.id.btnSelectAudio);
        tvFileAudioName = findViewById(R.id.tvAudioFileName);
        mSession = new Session(getApplicationContext());
        Picasso.get()
                .load(R.drawable.img_add_cover)
                .placeholder(R.drawable.img_wait_cover_book)
                .error(R.drawable.img_add_cover)
                .transform(new RoundedTransformation(15, 4))
                .resize(360, 494)
                .into(mCoverImageView);

        ArrayAdapter<CharSequence> categotyAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.array_categoty, android.R.layout.simple_spinner_item);
        categotyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> structureAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.array_structure, android.R.layout.simple_spinner_item);
        structureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mCoverImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        mIsPdfCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsPdfCheckBox.isChecked())
                    mSettingPdfLinearLayout.setVisibility(View.VISIBLE);
                else
                    mSettingPdfLinearLayout.setVisibility(View.GONE);
            }
        });

        mIsAudioCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsAudioCheckBox.isChecked())
                    mSettingAudioLinearLayout.setVisibility(View.VISIBLE);
                else
                    mSettingAudioLinearLayout.setVisibility(View.GONE);
            }
        });
        btnSelectPdf.setOnClickListener(v -> checkPermissionAndOpenPicker());
        btnSelectAudio.setOnClickListener(v -> checkPermissionAndOpenPickerAudio());
//        mAddButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mAddButton.setText("");
//                mWaitProgressBar.setVisibility(View.VISIBLE);
//                mBook = new Book(mIdBookEditText.getText().toString(),
//                        mTitleEditText.getText().toString(),
//                        String.valueOf(mCategorySpinner.getSelectedItemPosition()),
//                        mIdAuthorEditText.getText().toString(),
//                        mDescriptionEditText.getText().toString());
//                AddBookSyn addBookSyn = new AddBookSyn();
//                addBookSyn.execute(Server.getIpServerAndroid(getApplicationContext()) + "AddBook.php",
//                        mSession.getIdNumber(),
//                        mBook.getId(),
//                        mBook.getTitle(),
//                        mBook.getDescription(),
//                        mBook.getAuthor(),
//                        boolToString(mIsPhysiqueCheckBox.isChecked()),
//                        boolToString(mIsPdfCheckBox.isChecked()),
//                        boolToString(mIsAudioCheckBox.isChecked()),
//                        String.valueOf(mCategorySpinner.getSelectedItemPosition()),
//                        String.valueOf(mStructureSpinner.getSelectedItemPosition()),
//                        mPdfSizeEditText.getText().toString(),
//                        mNbrPageEditText.getText().toString(),
//                        mAudioSizeEditText.getText().toString(),
//                        mTimeMaxEditText.getText().toString());
//            }
//        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
    }

    private byte[] compressImage(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Compression de l'image avec une qualité de 50 (modifiable)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null) {
            try {
                // Récupération de l'image sélectionnée depuis la galerie
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                // Compression de l'image
                mCoverImageView.setImageBitmap(imageBitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Erreur lors du chargement de l'image." + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            // Compression de l'image
            mCoverImageView.setImageBitmap(imageBitmap);
        }
    }

    private class AddBookSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber", params[1])
                        .addFormDataPart("idBook", params[2])
                        .addFormDataPart("title", params[3])
                        .addFormDataPart("description", params[4])
                        .addFormDataPart("idAuthor", params[5])
                        .addFormDataPart("isPhysique", params[6])
                        .addFormDataPart("isPdf", params[7])
                        .addFormDataPart("isAudio", params[8])
                        .addFormDataPart("idCategory", params[9])
                        .addFormDataPart("idStruct", params[10])
                        .addFormDataPart("pdfSize", params[11])
                        .addFormDataPart("nbrPage", params[12])
                        .addFormDataPart("audioSize", params[13])
                        .addFormDataPart("timeMax", params[14])
                        .build();
                Request request = new Request.Builder()
                        .url(params[0])
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    assert response.body() != null;
                    return response.body().string();
                } catch (IOException e) {
                    Log.e("errorAddBookActivity", e.getMessage());
                }

            } catch (Exception e) {
                Log.e("errorAddBookActivity", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
            // Toast.makeText(AddBookActivity.this, response, Toast.LENGTH_SHORT).show();
            if (response != null) {
                if (response.equals("true")) {
                    SuccessSuggestionDialog();
                    mWaitProgressBar.setVisibility(View.INVISIBLE);
                    mAddButton.setText("Ajouter");
                }
            } else {
                mErrorTextView.setText(R.string.no_connection);
                mWaitProgressBar.setVisibility(View.INVISIBLE);
                mAddButton.setText("Ajouter");
            }

        }
    }

    private void SuccessSuggestionDialog() {
        SimpleOkDialog simpleOkDialog = new SimpleOkDialog(this);
        simpleOkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        simpleOkDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        TextView okTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok);
        TextView messageTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok_message);
        messageTextView.setText(R.string.suggestion_message_dialog);
        okTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mErrorTextView.setText("");
                simpleOkDialog.cancel();
            }
        });
        simpleOkDialog.build();
    }

    String boolToString(boolean bool) {
        if (bool)
            return "1";
        else
            return "0";
    }

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
    private void displayPdfInfo(Uri uri) {
        String fileName = getFileName(uri);
        if (tvFileName != null) {
            tvFileName.setText("Fichier sélectionné: " + fileName);
        }
        Toast.makeText(this, "PDF sélectionné avec succès", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "PDF sélectionné: " + fileName + " | URI: " + uri.toString());
    }

    private void displayAudioInfo(Uri uri) {
        String fileName = getFileName(uri);
        if (tvFileName != null) {
            tvFileAudioName.setText("Fichier sélectionné: " + fileName);
        }
        Toast.makeText(this, "PDF sélectionné avec succès", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "PDF sélectionné: " + fileName + " | URI: " + uri.toString());
    }
    private Uri selectedPdfUri;
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
    private final ActivityResultLauncher<Intent> audioPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            selectedPdfUri = result.getData().getData();
                            if (selectedPdfUri != null) {
                                displayAudioInfo(selectedPdfUri);
                            } else {
                                Toast.makeText(this, "Erreur lors de la sélection du fichier",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );
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
    private void openAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        // Option 1: Accepter tous les types audio
        intent.setType("audio/*");

        // Option 2: Si vous voulez être plus spécifique (décommentez si besoin)
        // String[] mimeTypes = {"audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg", "audio/aac", "audio/m4a"};
        // intent.setType("audio/*");
        // intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            audioPickerLauncher.launch(
                    Intent.createChooser(intent, "Sélectionner un fichier audio")
            );
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this,
                    "Veuillez installer un gestionnaire de fichiers",
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Aucun gestionnaire de fichiers trouvé", ex);
        }
    }
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_tmp);
//
//        // Initialisation des vues
//        initViews();
//
//        // Configuration des listeners
//        setupListeners();
//    }

    /**
     * Initialise les vues de l'interface
     */
//    private void initViews() {
//        tvFileName = findViewById(R.id.tvFileName);
//        btnSelectPdf = findViewById(R.id.btnSelectPdf);
//        btnUpload = findViewById(R.id.btnUpload);
//
//        // Vérification que toutes les vues sont présentes
//        if (tvFileName == null) {
//            Log.e(TAG, "tvFileName est null ! Vérifiez l'ID dans le XML");
//        }
//        if (btnSelectPdf == null) {
//            Log.e(TAG, "btnSelectPdf est null ! Vérifiez l'ID dans le XML");
//        }
//        if (btnUpload == null) {
//            Log.e(TAG, "btnUpload est null ! Vérifiez l'ID dans le XML");
//        }
//    }
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
    private void checkPermissionAndOpenPickerAudio() {
        // Pour Android 13+ (API 33+), les permissions de stockage ont changé
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Pas besoin de permission pour ACTION_GET_CONTENT sur Android 13+
            openAudioPicker();
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
                openAudioPicker();
            }
        } else {
            // Pour Android 5 et inférieur, pas de permission runtime nécessaire
            openFilePicker();
        }
    }
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private ImageView mCoverImageView;
    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private CheckBox mIsPhysiqueCheckBox;
    private CheckBox mIsPdfCheckBox;
    private CheckBox mIsAudioCheckBox;
    private TextView mErrorTextView;
    private ProgressBar mWaitProgressBar;
    private Button mAddButton;
    private Book mBook;
    private Session mSession;
    private LinearLayout mSettingPdfLinearLayout;
    private LinearLayout mSettingAudioLinearLayout;
    private Button btnSelectPdf;
    private TextView tvFileName;
    private Button btnSelectAudio;
    private TextView tvFileAudioName;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "RegisterAuthorActivity";
}