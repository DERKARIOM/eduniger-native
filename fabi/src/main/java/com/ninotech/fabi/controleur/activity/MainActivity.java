package com.ninotech.fabi.controleur.activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.ninotech.fabi.controleur.fragment.HomeFragment;
import com.ninotech.fabi.controleur.fragment.LibraryFragment;
import com.ninotech.fabi.controleur.fragment.SuggestionFragment;
import com.ninotech.fabi.model.data.Account;
import com.ninotech.fabi.model.data.Initialization;
import com.ninotech.fabi.R;
import com.ninotech.fabi.model.service.NotificationService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ninotech.fabi.model.table.DigitalPrintTable;
import com.ninotech.fabi.model.table.Session;
import com.ninotech.fabi.model.table.StudentTable;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Lock();
            }
        };
        Initialization initialization = new Initialization(getApplicationContext());
        initialization.onCreate(getApplicationContext());
        mBottomNavigationView = findViewById(R.id.bottom_navigation_main);
        Toolbar toolbar = findViewById(R.id.toolbar_search);
        mEditText = findViewById(R.id.edit_text_toolbar_search);
        SharedPreferences sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        boolean nightMODE = sharedPreferences.getBoolean("night", false);
        mHomeFragment = new HomeFragment();
        mSuggestionFragment = new SuggestionFragment();
        LibraryFragment libraryFragment = new LibraryFragment();
        MenuItem menuItem = toolbar.getMenu().findItem(R.id.menuHomeNotification);
        mDigitalPrintTable = new DigitalPrintTable(this);
        Intent reservationService = new Intent(this, NotificationService.class);
        mAccount = new Account();
        mEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(MainActivity.this,SearchActivity.class);
                searchIntent.putExtra("search_key","ONLINE_BOOK");
                searchIntent.putExtra("online_book_key","MAIN_ACTIVITY");
                startActivity(searchIntent);
            }
        });
        /* Detection de reseau */
        if(android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        /* Ouverteur de la session si ca existe  si non lancement de la page login */
        if(!mAccount.isSession(getApplicationContext()))
        {
            Intent login = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(login);
            finish();
        }
        try {
            if(mDigitalPrintTable.getPass().equals("0"))
            {
                Intent emprient = new Intent(MainActivity.this, LockActivity.class);
                startActivity(emprient);
                finish();
            }
            else
                mDigitalPrintTable.onUpdate("0");
        }catch (Exception e){
            Log.e("errorMainActivity",e.getMessage());
        };

        /* ########## Gestion du menu principale ########## */

        /* En cliquant sur "Actualiser" */
        toolbar.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent accuiel = new Intent(MainActivity.this, MainActivity.class);
                startActivity(accuiel);
                finish();
                return false;
            }
        });

        /* En cliquant sur "Message importants" */
        toolbar.getMenu().getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent importants = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(importants);
                return false;
            }
        });
        try {
            Session session = new Session(getApplicationContext());
            StudentTable studentTable = new StudentTable(getApplicationContext());
            if(studentTable.getIsDelegue(session.getIdNumber()).equals("0"))
                toolbar.getMenu().getItem(2).setVisible(false);
        }catch (Exception e)
        {
            Log.e("Err",e.getMessage());
        }

        toolbar.getMenu().getItem(2).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent preRegisterIntent = new Intent(MainActivity.this,PreRegistrationActivity.class);
                startActivity(preRegisterIntent);
                return false;
            }
        });

        /* En cliquant sur "Paramètres" */
        toolbar.getMenu().getItem(3).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent test = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(test);
                return false;
            }
        });

        /* En cliquant sur "Déconnecter" */
        toolbar.getMenu().getItem(4).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(mAccount.logout(getApplicationContext()))
                    reboot();
                return false;
            }
        });


        /* Activation du mode nuit et changement de couleur a la bar de navigation si le mode jour n' est pas activer */
        if(nightMODE)
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            getWindow().setNavigationBarColor(getResources().getColor(R.color.black2));
        }

        /* La mise en place du Fragment par defeaut */
      // getSupportFragmentManager().beginTransaction().replace(R.id.Top_comtainer, mHomeFragment).commit();

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_suggestion, R.id.navigation_library)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(mBottomNavigationView, navController);

        /* En Clikquant sur les boutton de la barre de navigation */
//        mBottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(MenuItem item) {
//                switch (item.getItemId())
//                {
//                    case R.id.accueil:
//                        if (mHomeFragment.isAdded()) {
//                            getSupportFragmentManager().beginTransaction().replace(R.id.Top_comtainer, mHomeFragment).commit();
//                        }
//                        else
//                        {
//                            getSupportFragmentManager().beginTransaction().replace(R.id.Top_comtainer,new HomeFragment()).commit();
//                        }
//                        return true;
//                    case R.id.assistance:
//                        if (mSuggestionFragment.isAdded()) {
//                            getSupportFragmentManager().beginTransaction().replace(R.id.Top_comtainer,mSuggestionFragment).commit();
//                        }
//                        else
//                        {
//                            getSupportFragmentManager().beginTransaction().replace(R.id.Top_comtainer,new SuggestionFragment()).commit();
//                        }
//                        return true;
//                    case R.id.bibliotheque:
//                        getSupportFragmentManager().beginTransaction().replace(R.id.Top_comtainer,new LibraryFragment()).commit();
//                        return true;
//                }
//                return false;
//            }
//        });
    }

    private void reboot() {
        Intent login = new Intent(MainActivity.this, MainActivity.class);
        startActivity(login);
        finish();
    }

    private void Lock() {
        //mEmpreiteTable.onUpdate("0");
    }
    public BottomNavigationView getBottomNavigationView() {
        return mBottomNavigationView;
    }

    /* Les attributs de la Classe MainActivity */
    private SQLiteDatabase mDatabase;
    private BottomNavigationView mBottomNavigationView;
    private HomeFragment mHomeFragment = new HomeFragment();
    private SuggestionFragment mSuggestionFragment = new SuggestionFragment();
    private Account mAccount;
    private DigitalPrintTable mDigitalPrintTable;

    public EditText getEditText() {
        return mEditText;
    }

    public void setEditText(EditText editText) {
        mEditText = editText;
    }

    private EditText mEditText;
}