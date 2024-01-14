package com.fabi.Controleur;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.fabi.R;
import com.fabi.Model.Categorie;
import com.fabi.Model.CategorieLocalAdapter;
import com.fabi.Model.ElectroniqueTable;
import com.fabi.Model.LivreLocal;
import com.fabi.Model.LivreLocalAdapter;
import com.fabi.Model.Session;

import java.util.ArrayList;
import java.util.List;

public class LivreLocalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livre_local);
        getSupportActionBar().hide();
        mRecyclerView = findViewById(R.id.recylerLivreLocal);
        mSession = new Session(this);
        mElectroniqueTable = new ElectroniqueTable(this);
        mItemLocal = findViewById(R.id.item_local);
        Intent livreLocal = getIntent();
        int id = livreLocal.getIntExtra("id",0);

        switch (id)
        {
            case 1: // Les Livre Telechages
                mItemLocal.setImageResource(R.drawable.img_telecharge_local);
                mList1 = new ArrayList<>();
                Cursor cursor1 = mElectroniqueTable.getData(mSession.getMatricule());
                cursor1.moveToFirst();
                do {
                    mList1.add(new LivreLocal(mSession.getMatricule(),cursor1.getString(5),cursor1.getString(8),cursor1.getString(7),cursor1.getString(4),cursor1.getString(6)));
                }while(cursor1.moveToNext());
                mLivreLocalAdapter = new LivreLocalAdapter(mList1);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                mRecyclerView.setAdapter(mLivreLocalAdapter);
                break;
            case 2: // Coups de coeur
                mItemLocal.setImageResource(R.drawable.img_love_livre);
                break;
            case 3: // Playlists
                mItemLocal.setImageResource(R.drawable.img_playliste_local);
                break;
            case 4: // Categorie
                mItemLocal.setImageResource(R.drawable.img_categorie);
                mList4 = new ArrayList<>();
                Cursor cursor4 = mElectroniqueTable.getData(mSession.getMatricule());
                cursor4.moveToFirst();
                do {
                    mList4.add(new Categorie(cursor4.getString(9),cursor4.getString(7)));
                }while(cursor4.moveToNext());
                mCategorieLocalAdapter = new CategorieLocalAdapter(mList4);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                mRecyclerView.setAdapter(mCategorieLocalAdapter);
                break;
            case 5: // Auteurs
                mItemLocal.setImageResource(R.drawable.img_auteur_local);
                mList4 = new ArrayList<>();
                Cursor cursor5 = mElectroniqueTable.getData(mSession.getMatricule());
                cursor5.moveToFirst();
                do {
                    mList4.add(new Categorie(cursor5.getString(10),cursor5.getString(4)));
                }while(cursor5.moveToNext());
                mCategorieLocalAdapter = new CategorieLocalAdapter(mList4);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                mRecyclerView.setAdapter(mCategorieLocalAdapter);
                break;
        }
    }
    private RecyclerView mRecyclerView;
    private LivreLocalAdapter mLivreLocalAdapter;
    private List<LivreLocal> mList1;
    private List<Categorie> mList4;
    private ElectroniqueTable mElectroniqueTable;
    private Session mSession;
    private ImageView mItemLocal;
    private CategorieLocalAdapter mCategorieLocalAdapter;
}