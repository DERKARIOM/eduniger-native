package com.ninotech.eduniger.controleur.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.adapter.ElectronicAdapter;
import com.ninotech.eduniger.controleur.adapter.RecentAdapter;
import com.ninotech.eduniger.model.data.Library;
import com.ninotech.eduniger.model.data.LocalBooks;
import com.ninotech.eduniger.model.data.Server;
import com.ninotech.eduniger.model.table.AudioTable;
import com.ninotech.eduniger.model.table.ElectronicTable;
import com.ninotech.eduniger.model.table.LoandTable;
import com.ninotech.eduniger.model.table.Session;
import com.ninotech.eduniger.model.table.UserTable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LibraryFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;

    private ImageView mPhotoImageView;
    private UserTable mUserTable;
    private View mView;
    private File mImageFile;
    private Session session;
    private SwipeRefreshLayout mSwipeRefreshLayout;  // ← nouveau

    // Garder les références pour le refresh
    private RecyclerView mRecyclerView;
    private RecyclerView mRecentRecyclerView;
    private TextView mUsernameTextView;
    private TextView mEmailTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        initializeViews(view);
        loadData(view);
        setupSwipeRefresh(view);  // ← nouveau

        return view;
    }

    private void initializeViews(View view) {
        mRecyclerView       = view.findViewById(R.id.recycler_view_electronic_fragment_media);
        mRecentRecyclerView = view.findViewById(R.id.recycler_view_electronic_fragment_recent);
        mUsernameTextView   = view.findViewById(R.id.text_view_fragment_library_username);
        mEmailTextView      = view.findViewById(R.id.text_view_fragment_library_email);
        mView               = view.findViewById(R.id.view_fragment_library_2);
        mPhotoImageView     = view.findViewById(R.id.image_view_structure_activity_profile);
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_library);

        session    = new Session(getContext());
        mUserTable = new UserTable(getContext());
    }

    private void loadData(View view) {
        List<Library> libraryList       = new ArrayList<>();
        List<LocalBooks> localBooksList = new ArrayList<>();

        ElectronicTable electronicTable = new ElectronicTable(getContext());
        LoandTable loandTable           = new LoandTable(getContext());
        AudioTable audioTable           = new AudioTable(getContext());

        // Profil utilisateur
        Cursor userCursor = mUserTable.getData(session.getIdNumber());
        userCursor.moveToFirst();

        mEmailTextView.setText(userCursor.getString(3));
        mUsernameTextView.setText(userCursor.getString(2) + " " + userCursor.getString(1));

        try {
            byte[] photoByte = userCursor.getBlob(6);
            if (photoByte != null) {
                Glide.with(this)
                        .load(photoByte)
                        .apply(RequestOptions.circleCropTransform())
                        .into(mPhotoImageView);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        mPhotoImageView.setOnClickListener(v -> openGallery());

        // Liste bibliothèque
        libraryList.add(new Library(1, R.drawable.fichier_pdf,
                getString(R.string.your_electronic_books),
                electronicTable.getNbrElectronic(session.getIdNumber())));
        libraryList.add(new Library(2, R.drawable.audio,
                getString(R.string.your_audio_books),
                audioTable.getNbrAudio(session.getIdNumber())));
        libraryList.add(new Library(3, R.drawable.books_emp,
                getString(R.string.your_loand_books),
                loandTable.getNbrLoand(session.getIdNumber())));
        libraryList.add(new Library(4, R.drawable.categorie,
                getString(R.string.cetegory),
                electronicTable.getNbrCategory(session.getIdNumber())));
        libraryList.add(new Library(5, R.drawable.auteurs,
                getString(R.string.author),
                electronicTable.getNbrAuthor(session.getIdNumber())));

        // Livres récents
        try {
            int i = 0;
            Cursor cursorPdf = electronicTable.getData(session.getIdNumber());
            if (cursorPdf.moveToFirst()) {
                i++;
                do {
                    localBooksList.add(new LocalBooks(
                            cursorPdf.getString(2),
                            cursorPdf.getString(5),
                            cursorPdf.getString(6),
                            "pdf"));
                } while (cursorPdf.moveToNext());
            }

            Cursor cursorAudio = audioTable.getData(session.getIdNumber());
            if (cursorAudio.moveToFirst()) {
                i++;
                do {
                    localBooksList.add(new LocalBooks(
                            cursorAudio.getString(2),
                            cursorAudio.getString(5),
                            cursorAudio.getString(6),
                            "audio"));
                } while (cursorAudio.moveToNext());
            }

            if (i == 0) {
                TextView recentReadBookTextView =
                        view.findViewById(R.id.text_view_fragment_library_recent_read_book);
                recentReadBookTextView.setVisibility(View.GONE);
                mView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e("errElectronicFragment", e.getMessage());
        }

        // Adapters
        ElectronicAdapter electronicAdapter = new ElectronicAdapter(libraryList);
        RecentAdapter recentAdapter         = new RecentAdapter(localBooksList);

        mRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext().getApplicationContext()));
        mRecentRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        mRecyclerView.setAdapter(electronicAdapter);
        mRecentRecyclerView.setAdapter(recentAdapter);
    }

    // ==================== SwipeRefresh ====================

    private void setupSwipeRefresh(View view) {
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.purple_200,
                android.R.color.holo_blue_light,
                android.R.color.holo_orange_light
        );

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            loadData(view);
            mSwipeRefreshLayout.setRefreshing(false);
        });
    }

    // ==================== Galerie / Caméra ====================

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GALLERY
                && resultCode == getActivity().RESULT_OK
                && data != null) {
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(
                        getActivity().getContentResolver(), data.getData());
                mImageFile = convertBitmapToFile(imageBitmap);
                byte[] compressedImageBytes = compressImage(imageBitmap);

                Glide.with(this)
                        .load(compressedImageBytes)
                        .apply(RequestOptions.circleCropTransform())
                        .into(mPhotoImageView);

                mUserTable.setPhoto(compressedImageBytes);

                if (mImageFile != null) {
                    uploadImage(mImageFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(),
                        "Erreur lors du chargement de l'image." + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE
                && resultCode == getActivity().RESULT_OK) {
            Bundle extras  = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            byte[] compressedImageBytes = compressImage(imageBitmap);

            Glide.with(this)
                    .load(compressedImageBytes)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mPhotoImageView);
        }
    }

    private byte[] compressImage(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
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
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
    }

    private void uploadImage(File imageFile) {
        String serverUrl = Server.getUrlApi(getContext()) + "uploadImg.php";
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", imageFile.getName(),
                        RequestBody.create(MediaType.parse("image/jpeg"), imageFile))
                .build();

        Request request = new Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(),
                                "Échec de l'upload : " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(),
                                    "Image téléversée avec succès",
                                    Toast.LENGTH_SHORT).show());
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(),
                                    "Erreur lors de l'upload " + response.toString(),
                                    Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private File convertBitmapToFile(Bitmap bitmap) throws IOException {
        File file = new File(getActivity().getCacheDir(), session.getIdNumber() + ".png");
        file.createNewFile();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapData = bos.toByteArray();

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bitmapData);
        fos.flush();
        fos.close();

        return file;
    }
}