package com.ninotech.fabi.controleur.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.CategorieLocalAdapter;
import com.ninotech.fabi.controleur.adapter.ElectronicBookAdapter;
import com.ninotech.fabi.model.data.Book;
import com.ninotech.fabi.model.data.Category;
import com.ninotech.fabi.model.data.ElectronicBook;
import com.ninotech.fabi.model.table.ElectronicTable;
import com.ninotech.fabi.model.table.Session;

import java.util.ArrayList;
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
                mElectronicBookAdapter = new ElectronicBookAdapter(mElectronicBookList);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                mRecyclerView.setAdapter(mElectronicBookAdapter);
                break;
            case 2: // Coups de coeur
                break;
            case 3: // Playlists
                break;
            case 4: // Category
                mList4 = new ArrayList<>();
                Cursor cursor4 = mElectronicTable.getData(mSession.getIdNumber());
                cursor4.moveToFirst();
                do {
                    mList4.add(new Category(cursor4.getString(9),cursor4.getString(7)));
                }while(cursor4.moveToNext());
                mCategorieLocalAdapter = new CategorieLocalAdapter(mList4);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                mRecyclerView.setAdapter(mCategorieLocalAdapter);
                break;
            case 5: // Auteurs
                mList4 = new ArrayList<>();
                Cursor cursor5 = mElectronicTable.getData(mSession.getIdNumber());
                cursor5.moveToFirst();
                do {
                    mList4.add(new Category(cursor5.getString(10),cursor5.getString(4)));
                }while(cursor5.moveToNext());
                mCategorieLocalAdapter = new CategorieLocalAdapter(mList4);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                mRecyclerView.setAdapter(mCategorieLocalAdapter);
                break;
        }
    }
    private RecyclerView mRecyclerView;
    private ElectronicBookAdapter mElectronicBookAdapter;
    private List<ElectronicBook> mElectronicBookList;
    private List<Category> mList4;
    private ElectronicTable mElectronicTable;
    private Session mSession;
    private CategorieLocalAdapter mCategorieLocalAdapter;
}