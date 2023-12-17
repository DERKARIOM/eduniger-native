package com.fabi.Controleur;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.example.fabi.R;
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
        Intent livreLocal = getIntent();
        int id = livreLocal.getIntExtra("id",0);

        switch (id)
        {
            case 1:
                mList1 = new ArrayList<>();
                Cursor cursor = mElectroniqueTable.getData(mSession.getMatricule());
                cursor.moveToFirst();
                do {
                    mList1.add(new LivreLocal(mSession.getMatricule(),cursor.getString(5),cursor.getString(8),cursor.getString(7),cursor.getString(4),cursor.getString(6)));
                }while(cursor.moveToNext());
                mLivreLocalAdapter = new LivreLocalAdapter(mList1);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                mRecyclerView.setAdapter(mLivreLocalAdapter);
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
        }
    }
    private RecyclerView mRecyclerView;
    private LivreLocalAdapter mLivreLocalAdapter;
    private List<LivreLocal> mList1;
    private ElectroniqueTable mElectroniqueTable;
    private Session mSession;
}