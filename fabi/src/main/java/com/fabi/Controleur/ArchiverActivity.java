package com.fabi.Controleur;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.fabi.Model.Archiver;
import com.fabi.Model.ArchiverCusto;
import com.fabi.Model.StatusBarCusto;
import com.example.fabi.R;

import java.util.List;

public class ArchiverActivity extends AppCompatActivity {
    private TextView mTextView;
    private Button mButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archiver);
        StatusBarCusto statusBarCusto = new StatusBarCusto(this,getWindow());
        // Activer le bouton de retour de l'action barre
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        // Revenir en arrière tout simplement
        super.onBackPressed();
    }
    private RecyclerView mRecyclerView;
    private List<Archiver> mList;
    private ArchiverCusto mArchiverCusto;
}