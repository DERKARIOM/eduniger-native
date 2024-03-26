package com.ninotech.fabi.controleur.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.AuthorLocalAdapter;
import com.ninotech.fabi.controleur.adapter.CategorieLocalAdapter;
import com.ninotech.fabi.controleur.adapter.CategoryLocalAdapter;
import com.ninotech.fabi.controleur.adapter.ElectronicBookAdapter;
import com.ninotech.fabi.controleur.adapter.ImageAdapter;
import com.ninotech.fabi.controleur.adapter.LoandBookAdapter;
import com.ninotech.fabi.model.data.AuthorLocal;
import com.ninotech.fabi.model.data.Category;
import com.ninotech.fabi.model.data.CategoryLocal;
import com.ninotech.fabi.model.data.ElectronicBook;
import com.ninotech.fabi.model.data.Loand;
import com.ninotech.fabi.model.table.ElectronicTable;
import com.ninotech.fabi.model.table.LoandTable;
import com.ninotech.fabi.model.table.Session;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ContainerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        getSupportActionBar().hide();
        mRecyclerView = findViewById(R.id.recycler_view_activity_container);
        mSession = new Session(this);
        mElectronicTable = new ElectronicTable(this);
        Intent libraryIntent = getIntent();
        int id = libraryIntent.getIntExtra("id",0);

        switch (id)
        {
            case 1: // Electronic Book
                mElectronicBookList = new ArrayList<>();
                Cursor electronicCursor = mElectronicTable.getData(mSession.getIdNumber());
                electronicCursor.moveToFirst();
                do {
                    byte[] imageBytes = electronicCursor.getBlob(5);
                    // Convertir le tableau d'octets en Bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    mElectronicBookList.add(new ElectronicBook(electronicCursor.getString(2),bitmap,electronicCursor.getString(8),electronicCursor.getString(7),electronicCursor.getString(4),null));
                }while(electronicCursor.moveToNext());
                ElectronicBookAdapter electronicBookAdapter = new ElectronicBookAdapter(mElectronicBookList);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                mRecyclerView.setAdapter(electronicBookAdapter);
                break;
            case 2: // Audio Book
                break;
            case 3: // Loand Book
                LoandTable loandTable = new LoandTable(this);
                ArrayList<Loand> loandList = new ArrayList<>();
                Cursor LoandCursor = loandTable.getData();
                LoandCursor.moveToFirst();
                try {
                    do {
                        loandList.add(new Loand(LoandCursor.getString(2),LoandCursor.getString(3),LoandCursor.getString(4),LoandCursor.getString(5),percentage(converterDate(LoandCursor.getString(4)),converterDate(LoandCursor.getString(5)),getNowDate())));
                    }while (LoandCursor.moveToNext());
                    LoandBookAdapter loandBookAdapter = new LoandBookAdapter(loandList);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                    mRecyclerView.setAdapter(loandBookAdapter);
                }catch (Exception e)
                {
                    ArrayList<String> imageList = new ArrayList<>();
                    imageList.add("Aucun livre emprunté");
                    ImageAdapter imageAdapter = new ImageAdapter(imageList);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                    mRecyclerView.setAdapter(imageAdapter);
                    Log.e("errPhysicLoand",e.getMessage());
                }
                break;
            case 4: // Category
                ArrayList<CategoryLocal> categoryLocals = new ArrayList<>();
                Cursor categoryCursor = mElectronicTable.getCategoryData(mSession.getIdNumber());
                categoryCursor.moveToFirst();
                do {
                    byte[] imageBytes = categoryCursor.getBlob(0);
                    // Convertir le tableau d'octets en Bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    categoryLocals.add(new CategoryLocal(bitmap,categoryCursor.getString(1)));
                }while (categoryCursor.moveToNext());
                CategoryLocalAdapter categoryLoacalAdapter = new CategoryLocalAdapter(categoryLocals);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                mRecyclerView.setAdapter(categoryLoacalAdapter);
                break;
            case 5: // Auteurs
                ArrayList<AuthorLocal> authorLocals = new ArrayList<>();
                Cursor authorCursor = mElectronicTable.getAuthorData(mSession.getIdNumber());
                authorCursor.moveToFirst();
                do {
                    byte[] imageBytes = authorCursor.getBlob(0);
                    // Convertir le tableau d'octets en Bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    authorLocals.add(new AuthorLocal(bitmap,authorCursor.getString(1)));
                }while (authorCursor.moveToNext());
                AuthorLocalAdapter authorLocalAdapter = new AuthorLocalAdapter(authorLocals);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                mRecyclerView.setAdapter(authorLocalAdapter);
                break;
        }
    }
    public long percentage(long startDate , long endDate , long nowDate)
    {
        return (long) (((float)(nowDate - startDate)/(float) (endDate - startDate))*100);
    }
    public long getNowDate()
    {
        long currentTimeMillis = System.currentTimeMillis();
        long currentTimeSeconds = currentTimeMillis / 1000;

        // Affichez le temps actuel en secondes
        return currentTimeSeconds;
    }
    public long converterDate(String dateString)
    {
//        String dateString = "2024-02-13 12:30:00";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long dateInSeconds = 0;

        try {
            // Analyser la chaîne de caractères en objet Date
            Date date = dateFormat.parse(dateString);

            // Convertir la date en millisecondes
            long dateInMillis = date.getTime();

            // Convertir les millisecondes en secondes
            dateInSeconds = dateInMillis / 1000;

            // Afficher la date en secondes
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateInSeconds;
    }
    private RecyclerView mRecyclerView;
    private List<ElectronicBook> mElectronicBookList;
    private List<Category> mList4;
    private ElectronicTable mElectronicTable;
    private Session mSession;
    private CategorieLocalAdapter mCategorieLocalAdapter;
}