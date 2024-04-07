package com.ninotech.fabi.controleur.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.ElectronicAdapter;
import com.ninotech.fabi.controleur.adapter.RecentAdapter;
import com.ninotech.fabi.model.data.Library;
import com.ninotech.fabi.model.data.SimilarBook;
import com.ninotech.fabi.model.table.AudioTable;
import com.ninotech.fabi.model.table.ElectronicTable;
import com.ninotech.fabi.model.table.LoandTable;
import com.ninotech.fabi.model.table.Session;
import com.ninotech.fabi.model.table.StudentTable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_electronic_fragment_media);
        RecyclerView recentRecyclerView = view.findViewById(R.id.recycler_view_electronic_fragment_recent);
        TextView usernameTextView = view.findViewById(R.id.text_view_fragment_library_username);
        TextView emailTextView = view.findViewById(R.id.text_view_fragment_library_email);
        List<Library> libraryList = new ArrayList<>();
        List<SimilarBook> similarBookList = new ArrayList<>();
        ElectronicTable electronicTable = new ElectronicTable(getContext());
        LoandTable loandTable = new LoandTable(getContext());
        AudioTable audioTable = new AudioTable(getContext());
        Session session = new Session(getContext());
        mStudentTable = new StudentTable(getContext());
        Cursor studentCursor = mStudentTable.getData(session.getIdNumber());
        studentCursor.moveToFirst();
        mPhotoImageView = view.findViewById(R.id.image_view_fragment_library_photo);
        usernameTextView.setText(studentCursor.getString(1) + "" + studentCursor.getString(2));
        emailTextView.setText(studentCursor.getString(5));
        try {
            byte[] photoByte = studentCursor.getBlob(6);
            if(photoByte != null)
            {
                Glide.with(this)
                        .load(photoByte)
                        .apply(RequestOptions.circleCropTransform())
                        .into(mPhotoImageView);
            }else
            {
                mPhotoImageView = view.findViewById(R.id.image_view_fragment_library_photo);
            }

        }catch (Exception e)
        {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        mPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
                //dispatchTakePictureIntent();
            }
        });
        ElectronicAdapter electronicAdapter = new ElectronicAdapter(libraryList);
        RecentAdapter recentAdapter = new RecentAdapter(similarBookList);
        libraryList.add(new Library(1,R.drawable.img_electronic_book,getString(R.string.dawnloads_book), electronicTable.getNbrElectronic(session.getIdNumber())));
        libraryList.add(new Library(2,R.drawable.img_audio_book,getString(R.string.favorites),audioTable.getNbrAudio(session.getIdNumber())));
        libraryList.add(new Library(3,R.drawable.img_loand_book,getString(R.string.playlists),loandTable.getNbrLoand(session.getIdNumber())));
        libraryList.add(new Library(4,R.drawable.img_category,getString(R.string.cetegory), electronicTable.getNbrCategory(session.getIdNumber())));
        libraryList.add(new Library(5,R.drawable.img_author,getString(R.string.author), electronicTable.getNbrAuthor(session.getIdNumber())));
        try {
            Cursor cursor = electronicTable.getData(session.getIdNumber());
            cursor.moveToFirst();
            do {
                similarBookList.add(new SimilarBook(cursor.getString(2),cursor.getString(5),null));
            }while(cursor.moveToNext());
        }catch (Exception e)
        {
            TextView recentReadBookTextView;
            recentReadBookTextView = view.findViewById(R.id.text_view_fragment_library_recent_read_book);
            recentReadBookTextView.setVisibility(View.GONE);
            Log.e("errElectronicFragment",e.getMessage());
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));
        recentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(electronicAdapter);
        recentRecyclerView.setAdapter(recentAdapter);
        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == getActivity().RESULT_OK && data != null) {
            try {
                // Récupération de l'image sélectionnée depuis la galerie
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());

                // Compression de l'image
                byte[] compressedImageBytes = compressImage(imageBitmap);
                // Affichage de l'image compressée
                Glide.with(this)
                        .load(compressedImageBytes)
                        .apply(RequestOptions.circleCropTransform())
                        .into(mPhotoImageView);
                mStudentTable.setPhoto(compressedImageBytes);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Erreur lors du chargement de l'image." + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            // Compression de l'image
            byte[] compressedImageBytes = compressImage(imageBitmap);

            // Affichage de l'image compressée
            Glide.with(this)
                    .load(compressedImageBytes)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mPhotoImageView);
        }
    }

    private byte[] compressImage(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Compression de l'image avec une qualité de 50 (modifiable)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
    }
    private ImageView mPhotoImageView;
    private StudentTable mStudentTable;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
}