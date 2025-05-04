package com.ninotech.fabi.controleur.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.controleur.dialog.SimpleOkDialog;
import com.ninotech.fabi.model.data.Book;
import com.ninotech.fabi.model.data.Server;
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

public class AddBookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        Objects.requireNonNull(getSupportActionBar()).hide();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mStructureSpinner = findViewById(R.id.spinner_activity_add_book_structure);
        mCategorySpinner = findViewById(R.id.spinner_activity_add_book_cotegory);
        mCoverImageView = findViewById(R.id.image_view_activity_add_book_cover);
        mIdBookEditText = findViewById(R.id.edit_text_activity_add_book_id_book);
        mTitleEditText = findViewById(R.id.edit_text_activity_add_book_title);
        mDescriptionEditText = findViewById(R.id.edit_text_activity_add_book_description);
        mIdAuthorEditText = findViewById(R.id.edit_text_activity_add_book_id_author);
        mIsPhysiqueCheckBox = findViewById(R.id.check_box_activity_add_book_is_physique);
        mIsPdfCheckBox = findViewById(R.id.check_box_activity_add_book_is_pdf);
        mIsAudioCheckBox = findViewById(R.id.check_box_activity_add_book_is_audio);
        mErrorTextView = findViewById(R.id.text_view_activity_add_book_error);
        mWaitProgressBar = findViewById(R.id.progress_bar_activity_add_book_whait);
        mAddButton = findViewById(R.id.button_activity_add_book);
        mSettingPdfLinearLayout = findViewById(R.id.linear_layout_activity_add_book_setting_pdf);
        mSettingAudioLinearLayout = findViewById(R.id.linear_layout_activity_add_book_setting_audio);
        mPdfSizeEditText = findViewById(R.id.edit_text_activity_add_book_pdf_size);
        mAudioSizeEditText = findViewById(R.id.edit_text_activity_add_book_audio_size);
        mNbrPageEditText = findViewById(R.id.edit_text_activity_add_book_pdf_nbr_page);
        mTimeMaxEditText = findViewById(R.id.edit_text_activity_add_book_audio_max_time);
        mSession = new Session(getApplicationContext());
        Picasso.get()
                .load(R.drawable.img_add_cover)
                .placeholder(R.drawable.img_default_book)
                .error(R.drawable.img_add_cover)
                .transform(new RoundedTransformation(15,4))
                .resize(360,494)
                .into(mCoverImageView);

        ArrayAdapter<CharSequence> categotyAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.array_categoty, android.R.layout.simple_spinner_item);
        categotyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(categotyAdapter);

        ArrayAdapter<CharSequence> structureAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.array_structure, android.R.layout.simple_spinner_item);
        structureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStructureSpinner.setAdapter(structureAdapter);

        mCoverImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        mIsPdfCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsPdfCheckBox.isChecked())
                    mSettingPdfLinearLayout.setVisibility(View.VISIBLE);
                else
                    mSettingPdfLinearLayout.setVisibility(View.GONE);
            }
        });

        mIsAudioCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsAudioCheckBox.isChecked())
                    mSettingAudioLinearLayout.setVisibility(View.VISIBLE);
                else
                    mSettingAudioLinearLayout.setVisibility(View.GONE);
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddButton.setText("");
                mWaitProgressBar.setVisibility(View.VISIBLE);
                mBook = new Book(mIdBookEditText.getText().toString(),
                        mTitleEditText.getText().toString(),
                        String.valueOf(mCategorySpinner.getSelectedItemPosition()),
                        mIdAuthorEditText.getText().toString(),
                        mDescriptionEditText.getText().toString());
                AddBookSyn addBookSyn = new AddBookSyn();
                addBookSyn.execute(Server.getIpServerAndroid(getApplicationContext()) + "AddBook.php",
                        mSession.getIdNumber(),
                        mBook.getId(),
                        mBook.getTitle(),
                        mBook.getDescription(),
                        mBook.getAuthor(),
                        boolToString(mIsPhysiqueCheckBox.isChecked()),
                        boolToString(mIsPdfCheckBox.isChecked()),
                        boolToString(mIsAudioCheckBox.isChecked()),
                        String.valueOf(mCategorySpinner.getSelectedItemPosition()),
                        String.valueOf(mStructureSpinner.getSelectedItemPosition()),
                        mPdfSizeEditText.getText().toString(),
                        mNbrPageEditText.getText().toString(),
                        mAudioSizeEditText.getText().toString(),
                        mTimeMaxEditText.getText().toString());
            }
        });
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
    private class AddBookSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("idBook", params[2])
                        .addFormDataPart("title", params[3])
                        .addFormDataPart("description",params[4])
                        .addFormDataPart("idAuthor",params[5])
                        .addFormDataPart("isPhysique",params[6])
                        .addFormDataPart("isPdf",params[7])
                        .addFormDataPart("isAudio",params[8])
                        .addFormDataPart("idCategory",params[9])
                        .addFormDataPart("idStruct",params[10])
                        .addFormDataPart("pdfSize",params[11])
                        .addFormDataPart("nbrPage",params[12])
                        .addFormDataPart("audioSize",params[13])
                        .addFormDataPart("timeMax",params[14])
                        .build();
                Request request = new Request.Builder()
                        .url(params[0])
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    assert response.body() != null;
                    return response.body().string();
                }catch (IOException e)
                {
                    Log.e("errorAddBookActivity",e.getMessage());
                }

            }catch (Exception e)
            {
                Log.e("errorAddBookActivity",e.getMessage());
            }
            return null;
        }
        @Override
        protected void onPostExecute(String response){
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
           // Toast.makeText(AddBookActivity.this, response, Toast.LENGTH_SHORT).show();
            if(response != null)
            {
                if(response.equals("true"))
                {
                    SuccessSuggestionDialog();
                    mWaitProgressBar.setVisibility(View.INVISIBLE);
                    mAddButton.setText("Ajouter");
                }
            }
            else
            {
                mErrorTextView.setText(R.string.no_connection);
                mWaitProgressBar.setVisibility(View.INVISIBLE);
                mAddButton.setText("Ajouter");
            }

        }
    }
    private void SuccessSuggestionDialog(){
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
    String boolToString(boolean bool)
    {
        if (bool)
            return "1";
        else
            return "0";
    }
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private ImageView mCoverImageView;
    private Spinner mStructureSpinner;
    private Spinner mCategorySpinner;
    private EditText mIdBookEditText;
    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private EditText mIdAuthorEditText;
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
    private EditText mPdfSizeEditText;
    private EditText mNbrPageEditText;
    private EditText mAudioSizeEditText;
    private EditText mTimeMaxEditText;
}