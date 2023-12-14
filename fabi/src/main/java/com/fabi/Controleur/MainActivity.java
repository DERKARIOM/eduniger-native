package com.fabi.Controleur;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.fabi.Model.ElectroniqueTable;
import com.fabi.Model.EmpreiteTable;
import com.fabi.Model.Session;
import com.fabi.Model.UtilisateurTable;
import com.example.fabi.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        /* Initialisation des attributs membre */
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                Lock();
            }
        };
        mBottomNavigationView = findViewById(R.id.bottom_menu);
        mSharedPreferences = getSharedPreferences("MODE",Context.MODE_PRIVATE);
        mNightMODE = mSharedPreferences.getBoolean("night",false);
        mDb = openOrCreateDatabase("data.db",MODE_PRIVATE,null);
        mAccueilFragment = new AccueilFragment();
        mAssistanceFragment = new AssistanceFragment();
        mBibliothequeFragment = new BibliothequeFragment();
        mSession = new Session(this);
        mElectroniqueTable = new ElectroniqueTable(this);
        mEmpreiteTable = new EmpreiteTable(this);
        mUtilisateur = new UtilisateurTable(this);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mUtilisateur.onCreate(mDb);
        mElectroniqueTable.onCreate(mDb);

        /* Detection de reseau */
        if(android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        /* Ouverteur de la session si ca existe  si non lancement de la page login */
        try {
            Toast.makeText(this, mSession.getMatricule(), Toast.LENGTH_SHORT);
//            startService(mNoteService);
        }
        catch (Exception e)
        {
            Intent login = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(login);
            finish();
        }

        try {
            if(mEmpreiteTable.getPasse().equals("0"))
            {
                Intent emprient = new Intent(MainActivity.this,EmpreinteActivity.class);
                startActivity(emprient);
                finish();
            }
            else
                mEmpreiteTable.onUpdate("0");
        }catch (Exception e){};

        /* ########## Gestion du menu principale ########## */

        /* En cliquant sur "Actualiser" */
        mToolbar.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent accuiel = new Intent(MainActivity.this, MainActivity.class);
                startActivity(accuiel);
                finish();
                return false;
            }
        });

        /* En cliquant sur "Message importants" */
        mToolbar.getMenu().getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent importants = new Intent(MainActivity.this,MessageImportantsActivity.class);
                startActivity(importants);
                return false;
            }
        });

        /* En cliquant sur "Archiver" */
        mToolbar.getMenu().getItem(3).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
//                Intent archive = new Intent(MainActivity.this,ArchiverActivity.class);
//                startActivity(archive);
                return false;
            }
        });

        /* En cliquant sur "Suggestion" */
        mToolbar.getMenu().getItem(4).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
//                Intent reclamation = new Intent(MainActivity.this, SuggestionActivity.class);
//                startActivity(reclamation);
                return false;
            }
        });

        /* En cliquant sur "Paramètres" */
        mToolbar.getMenu().getItem(5).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
//                Intent parametre = new Intent(MainActivity.this,ParametreActivity.class);
//                startActivity(parametre);
                return false;
            }
        });

        /* En cliquant sur "Restaurer mes notes" */
        mToolbar.getMenu().getItem(6).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
//                Restorer restorer = new Restorer();
//                restorer.execute("http://192.168.43.1:2222/android/restorer.php");
                return false;
            }
        });

        /* En cliquant sur "Déconnecter" */
        mToolbar.getMenu().getItem(7).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                deconnecter();
                return false;
            }
        });


        /* Activation du mode nuit et changement de couleur a la bar de navigation si le mode jour n' est pas activer */
        if(mNightMODE)
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            getWindow().setNavigationBarColor(getResources().getColor(R.color.black2));
        }

        /* La mise en place du Fragment par defeaut */
       getSupportFragmentManager().beginTransaction().replace(R.id.Top_comtainer,mAccueilFragment).commit();

        /* En Clikquant sur les boutton de la barre de navigation */
        mBottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.accueil:
                        if (mAccueilFragment.isAdded()) {
                            getSupportFragmentManager().beginTransaction().replace(R.id.Top_comtainer,mAccueilFragment).commit();
                        }
                        else
                        {
                            getSupportFragmentManager().beginTransaction().replace(R.id.Top_comtainer,new AccueilFragment()).commit();
                        }
                        return true;
                    case R.id.assistance:
                        if (mAssistanceFragment.isAdded()) {
                            getSupportFragmentManager().beginTransaction().replace(R.id.Top_comtainer,mAssistanceFragment).commit();
                        }
                        else
                        {
                            getSupportFragmentManager().beginTransaction().replace(R.id.Top_comtainer,new AssistanceFragment()).commit();
                        }
                        return true;
                    case R.id.bibliotheque:
                        getSupportFragmentManager().beginTransaction().replace(R.id.Top_comtainer,new BibliothequeFragment()).commit();
                        return true;
                }
                return false;
            }
        });
    }

    private void deconnecter() {
//        stopService(mNoteService);
        mSession.onUpgrade(mDb,0,1);
        Intent login = new Intent(MainActivity.this, MainActivity.class);
        startActivity(login);
        finish();
//        exit(1);
    }

    private void Lock() {
        mEmpreiteTable.onUpdate("0");
    }

    /* Les attributs de la Classe MainActivity */
    private SQLiteDatabase mDb;

    public BottomNavigationView getBottomNavigationView() {
        return mBottomNavigationView;
    }

    private BottomNavigationView mBottomNavigationView;
    private AccueilFragment mAccueilFragment = new AccueilFragment();
    private AssistanceFragment mAssistanceFragment = new AssistanceFragment();
    private BibliothequeFragment mBibliothequeFragment = new BibliothequeFragment();
    private boolean mNightMODE;
    private SharedPreferences mSharedPreferences;
    private Session mSession;
    private EmpreiteTable mEmpreiteTable;
    private UtilisateurTable mUtilisateur;
    private Toolbar mToolbar;
    private ElectroniqueTable mElectroniqueTable;
    private Handler mHandler;
    private Runnable mRunnable;
}