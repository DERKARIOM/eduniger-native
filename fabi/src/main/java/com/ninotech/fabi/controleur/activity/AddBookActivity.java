package com.ninotech.fabi.controleur.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

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
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private ImageView mCoverImageView;
    private Spinner mStructureSpinner;
    private Spinner mCategorySpinner;
}