package com.ninotech.fabi.controleur.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.model.data.Parametre;
import com.ninotech.fabi.controleur.adapter.ParametreAdapter;
import com.ninotech.fabi.R;

import java.util.ArrayList;

public class ParametreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametre);
        getSupportActionBar().hide();
        //StatusBarCusto statusBarCusto = new StatusBarCusto(this,getWindow());
        // Activer le bouton de retour de l'action barre
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        FingerprintManager fingerprintManager = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            fingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!fingerprintManager.isHardwareDetected()) {
//                // Le capteur d'empreintes digitales n'est pas disponible sur cet appareil
//                Toast.makeText(this, "Le capteur d'empreintes digitales n'est pas disponible sur cet appareil", Toast.LENGTH_SHORT).show();
//            } else if (!fingerprintManager.hasEnrolledFingerprints()) {
//                // Aucune empreinte digitale n'est enregistrée sur cet appareil
//                Toast.makeText(this, "Aucune empreinte digitale n'est enregistrée sur cet appareil", Toast.LENGTH_SHORT).show();
//
//            } else {
//                // L'appareil prend en charge la fonctionnalité de l'empreinte digitale
//                Toast.makeText(this, "L'appareil prend en charge la fonctionnalité de l'empreinte digitale", Toast.LENGTH_SHORT).show();
//            }
//        }


        mRecyclerView = (RecyclerView) findViewById(R.id.recylerParametre);
        mList = new ArrayList<>();
        mList.add(new Parametre(R.drawable.vector_purple_200_compte,"Compte","Changer votre mot de passe , email..."));
        mList.add(new Parametre(R.drawable.vector_purple_200_digital,"Empreinte digitale","Sécurisé votre session"));
        mList.add(new Parametre(R.drawable.vector_purple_200_messagerie,"Envoyer une suggestion","L'objet de votre suggestion"));
        mList.add(new Parametre(R.drawable.vector_purple_200_start,"Evaluez-nous","Votre note , Observation (facultative)"));
        mList.add(new Parametre(R.drawable.vector_purple_200_phone,"Contactez-nous","Appelez +22794961793"));
        mList.add(new Parametre(R.drawable.vector_purple_200_video,"Comment ça marche","Tutoriel qui vous explique de A-Z..."));
        mList.add(new Parametre(R.drawable.vector_purple_200_information,"Infos de l'application","Auteurs , Version et Licence"));
        mParametreAdapter = new ParametreAdapter(mList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        mRecyclerView.setAdapter(mParametreAdapter);
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
    private ArrayList<Parametre> mList;
    private ParametreAdapter mParametreAdapter;
}