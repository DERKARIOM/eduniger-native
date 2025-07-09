package com.ninotech.fabi.controleur.activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.SettingAdapter;
import com.ninotech.fabi.controleur.adapter.StatusBarAdapter;
import com.ninotech.fabi.model.data.Account;
import com.ninotech.fabi.model.data.Server;
import com.ninotech.fabi.model.data.Setting;
import com.ninotech.fabi.model.table.Session;
import com.ninotech.fabi.model.table.UserTable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);
        StatusBarAdapter statusBarAdapter = new StatusBarAdapter(this,getWindow());
        mPhotoImageView = findViewById(R.id.image_view_setting_account_photo);
        mPhotoRelativeLayout = findViewById(R.id.relative_layout_activity_setting_account);
        // Activer le bouton de retour de l'action barre
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RecyclerView mSettingRecyclerView = findViewById(R.id.recycler_view_setting_account);
        mUserTable = new UserTable(getApplicationContext());
        mSession = new Session(getApplicationContext());
        Cursor userCursor = mUserTable.getData(mSession.getIdNumber());
        userCursor.moveToFirst();
        Account account = new Account(userCursor.getString(0),userCursor.getString(1),userCursor.getString(2),userCursor.getString(3),userCursor.getString(4),userCursor.getBlob(6),1);
        ArrayList<Setting> settings = new ArrayList<>();
        try {
            byte[] photoByte = userCursor.getBlob(6);
            if(photoByte != null)
            {
                Glide.with(this)
                        .load(photoByte)
                        .apply(RequestOptions.circleCropTransform())
                        .into(mPhotoImageView);
            }else
            {
                mPhotoImageView.setImageResource(R.drawable.user);
            }
        }catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        settings.add(new Setting(R.drawable.vector_black3_identify,"Nom",account.getName()));
        settings.add(new Setting(R.drawable.vector_family,"Prénom",account.getFirstName()));
        settings.add(new Setting(R.drawable.vector_black3_lock,"Modifier le mot de passe",null));
        settings.add(new Setting(R.drawable.img_google,"Identifiant de réseau social","Se connecter avec google"));
        settings.add(new Setting(R.drawable.vector_black3_email,account.getEmail(),null));
        settings.add(new Setting(R.drawable.vector_warning,"Supprimer le compte","La suppression de votre compte est permante et immédiate. Une fois votre compte supprimé, vous perdrez l'accès à vos service TeleSafe"));
        SettingAdapter mSettingAdapter = new SettingAdapter(settings);
        mSettingRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mSettingRecyclerView.setAdapter(mSettingAdapter);
        mPhotoRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }
    private byte[] compressImage(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Compression de l'image avec une qualité de 50 (modifiable)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null) {
            try {
                // Récupération de l'image sélectionnée depuis la galerie
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                mImageFile = convertBitmapToFile(imageBitmap);
                // Compression de l'image
                byte[] compressedImageBytes = compressImage(imageBitmap);
                // Affichage de l'image compressée
                Glide.with(this)
                        .load(compressedImageBytes)
                        .apply(RequestOptions.circleCropTransform())
                        .into(mPhotoImageView);
                mUserTable.setPhoto(compressedImageBytes);
                if(mImageFile != null)
                {
                    uploadImage(mImageFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Erreur lors du chargement de l'image." + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode ==RESULT_OK) {
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
    private File convertBitmapToFile(Bitmap bitmap) throws IOException {
        File file = new File(getCacheDir(),mSession.getIdNumber() + ".png");
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Vérifier si l'item sélectionné est le bouton de retour
        if (item.getItemId() == android.R.id.home) {
            // Appeler la méthode onBackPressed pour simuler un back press
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public boolean isAppInstalled(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return appInfo != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    private void uploadImage(File imageFile) {
        String serverUrl = Server.getIpServer(getApplicationContext()) + "ressources/uploadProfile.php"; // Remplace par ton URL de serveur

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
             runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Échec de l'upload : " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Image téléversée avec succès " + response.toString(), Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Erreur lors de l'upload " + response.toString(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private RelativeLayout mPhotoRelativeLayout;
    private ImageView mPhotoImageView;
    private UserTable mUserTable;
    private File mImageFile;
    private Session mSession;
}