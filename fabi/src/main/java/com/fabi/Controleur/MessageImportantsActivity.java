package com.fabi.Controleur;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;

import com.fabi.Model.NotifCusto;
import com.fabi.Model.Notification;
import com.fabi.Model.StatusBarCusto;
import com.example.fabi.R;

import java.util.ArrayList;

public class MessageImportantsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_importants);
        StatusBarCusto statusBarCusto = new StatusBarCusto(this,getWindow());
        // Activer le bouton de retour de l'action barre
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyler2);
        mList = new ArrayList<Notification>();
        mList.add(new Notification("23","Test","62331","Information ! \nL' examen de base de de donnee est prevue pour ce mardi 25/04/2023 dans L' anphi DDK a partir de 10h00","10h23","12/5/2023","no"));
        mNotifCusto = new NotifCusto(mList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        mRecyclerView.setAdapter(mNotifCusto);
        mRecyclerView.smoothScrollToPosition(mNotifCusto.getItemCount()-1);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Gérer les clics sur les éléments de l'action barre
        switch (item.getItemId()) {
            case android.R.id.home:
                // Appeler onBackPressed() lorsque le bouton de retour de l'action barre est pressé
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // Revenir en arrière tout simplement
        super.onBackPressed();
    }
    private RecyclerView mRecyclerView;
    private NotifCusto mNotifCusto;
    private ArrayList<Notification> mList;
}