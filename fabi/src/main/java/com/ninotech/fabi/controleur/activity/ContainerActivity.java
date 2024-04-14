package com.ninotech.fabi.controleur.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.AuthorLocalAdapter;
import com.ninotech.fabi.controleur.adapter.AudioBookAdapter;
import com.ninotech.fabi.controleur.adapter.CategoryLocalAdapter;
import com.ninotech.fabi.controleur.adapter.ElectronicBookAdapter;
import com.ninotech.fabi.controleur.adapter.VoidContainerAdapter;
import com.ninotech.fabi.controleur.adapter.LoandBookAdapter;
import com.ninotech.fabi.model.data.AuthorLocal;
import com.ninotech.fabi.model.data.AudioBook;
import com.ninotech.fabi.model.data.Category;
import com.ninotech.fabi.model.data.ElectronicBook;
import com.ninotech.fabi.model.data.Loand;
import com.ninotech.fabi.model.data.VoidContainer;
import com.ninotech.fabi.model.table.AudioTable;
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
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
        ab.setHomeAsUpIndicator(R.drawable.vector_back);
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.custom_action_bar);
        ab.setDisplayHomeAsUpEnabled(true);
        TextView actionBarTitle = ab.getCustomView().findViewById(R.id.action_bar_title);
        mRecyclerView = findViewById(R.id.recycler_view_activity_container);
        mSession = new Session(this);
        mElectronicTable = new ElectronicTable(this);
        Intent libraryIntent = getIntent();
        mId = libraryIntent.getIntExtra("id",0);
        switch (mId)
        {
            case 1: // Electronic Book
                actionBarTitle.setText(R.string.your_electronic_books);
               try {
                   mElectronicBookList = new ArrayList<>();
                   Cursor electronicCursor = mElectronicTable.getData(mSession.getIdNumber());
                   electronicCursor.moveToFirst();
                   do {
                       mElectronicBookList.add(new ElectronicBook(electronicCursor.getString(2),electronicCursor.getString(5),electronicCursor.getString(8),electronicCursor.getString(7),electronicCursor.getString(4),electronicCursor.getString(6)));
                   }while(electronicCursor.moveToNext());
                   ElectronicBookAdapter electronicBookAdapter = new ElectronicBookAdapter(mElectronicBookList);
                   mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                   mRecyclerView.setAdapter(electronicBookAdapter);
               }catch (Exception e)
               {
                   voidContainer(R.drawable.img_telecharge_local,getString(R.string.no_electronic_book));
                   Log.e("ErrorElectronic",e.getMessage());
               }
                break;
            case 2: // Audio Book
                try {
                    actionBarTitle.setText(R.string.your_audio_books);
                    ArrayList<AudioBook> audioBooks = new ArrayList<>();
                    AudioTable audioTable = new AudioTable(this);
                    Cursor audioCursor = audioTable.getData(mSession.getIdNumber());
                    audioCursor.moveToFirst();
                    do {
                        audioBooks.add(new AudioBook(audioCursor.getString(2),audioCursor.getString(5),audioCursor.getString(8),audioCursor.getString(4),audioCursor.getString(11),audioCursor.getString(6)));
                    }while (audioCursor.moveToNext());
                    AudioBookAdapter audioBookAdapter = new AudioBookAdapter(audioBooks);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                    mRecyclerView.setAdapter(audioBookAdapter);
                }catch (Exception e)
                {
                    voidContainer(R.drawable.img_playliste_local,getString(R.string.no_audio_book));
                    Log.e("ErrorAudio",e.getMessage());
                }
                break;
            case 3: // Loand Book
                actionBarTitle.setText(R.string.your_loand_books);
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
                    voidContainer(R.drawable.img_physical,getString(R.string.no_loand_book));
                }
                break;
            case 4: // Category
                try {
                    actionBarTitle.setText(R.string.category);
                    ArrayList<Category> categories = new ArrayList<>();
                    Cursor categoryCursor = mElectronicTable.getCategoryData(mSession.getIdNumber());
                    categoryCursor.moveToFirst();
                    do {
                        categories.add(new Category(categoryCursor.getString(0),categoryCursor.getString(1)));
                    }while (categoryCursor.moveToNext());
                    CategoryLocalAdapter categoryLoacalAdapter = new CategoryLocalAdapter(categories);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                    mRecyclerView.setAdapter(categoryLoacalAdapter);
                }
                catch (Exception e)
                {
                    voidContainer(R.drawable.img_categorie,getString(R.string.no_category));
                }
                break;
            case 5: // Auteurs
                actionBarTitle.setText(R.string.author);
                try {
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
                }catch (Exception e)
                {
                    voidContainer(R.drawable.img_auteur_local,getString(R.string.no_author));
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id)
        {
            case android.R.id.home:
                onBackPressed(); // Appel de la méthode onBackPressed() pour simuler le comportement du bouton retour
                return true;
            case R.id.item_menu_search:
                Intent searchIntent = new Intent(ContainerActivity.this,SearchActivity.class);
                switch (mId)
                {
                    case 1:
                        searchIntent.putExtra("search_key","ELECTRONIC_BOOK");
                        break;
                    case 2:
                        searchIntent.putExtra("search_key","AUDIO_BOOK");
                        break;
                    case 3:
                        searchIntent.putExtra("search_key","LOAND_BOOK");
                        break;
                    case 4:
                        searchIntent.putExtra("search_key","CATEGORY");
                        break;
                    case 5:
                        searchIntent.putExtra("search_key","AUTHOR");
                        break;
                }
                startActivity(searchIntent);
                return true;
        }
        Log.e("idRecherche",String.valueOf(id));
        return super.onOptionsItemSelected(item);
    }
    public void voidContainer(int image , String message)
    {
        ArrayList<VoidContainer> voidContainers = new ArrayList<>();
        voidContainers.add(new VoidContainer(image,message));
        VoidContainerAdapter voidContainerAdapter = new VoidContainerAdapter(voidContainers);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(voidContainerAdapter);
    }
    private RecyclerView mRecyclerView;
    private List<ElectronicBook> mElectronicBookList;
    private List<Category> mList4;
    private ElectronicTable mElectronicTable;
    private Session mSession;
    private int mId;
}