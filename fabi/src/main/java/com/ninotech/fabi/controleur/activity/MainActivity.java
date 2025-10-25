package com.ninotech.fabi.controleur.activity;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ninotech.fabi.controleur.custo.StatusBarCusto;
import com.ninotech.fabi.controleur.fragment.BookStoreFragment;
import com.ninotech.fabi.controleur.fragment.LibraryFragment;
import com.ninotech.fabi.controleur.fragment.ChatBotFragment;
import com.ninotech.fabi.model.data.Account;
import com.ninotech.fabi.model.data.Initialization;
import com.ninotech.fabi.R;
import com.ninotech.fabi.model.data.NotifNumber;
import com.ninotech.fabi.model.data.Themes;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ninotech.fabi.model.table.DigitalPrintTable;
import com.ninotech.fabi.model.table.Session;
import com.ninotech.fabi.model.table.UserTable;
import com.ninotech.fabi.model.worker.NetworkCheckWorker;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Lock();
            }
        };
        Initialization initialization = new Initialization(getApplicationContext());
        initialization.onCreate(getApplicationContext());
        Intent intent = getIntent();
        Uri data = getIntent().getData();
        if (data != null) {
            String id = data.getQueryParameter("id");
            String name = data.getQueryParameter("name");
        }
        if(Themes.getName(getApplicationContext()).equals("notNight"))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else if (Themes.getName(getApplicationContext()).equals("night"))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        mBottomNavigationView = findViewById(R.id.bottom_navigation_main);
        Toolbar toolbar = findViewById(R.id.toolbar_search);
        mEditText = findViewById(R.id.edit_text_toolbar_search);
        mProfileImageView = findViewById(R.id.image_view_toolbar_main_profile);
        mBookStoreFragment = new BookStoreFragment();
        mChatBotFragment = new ChatBotFragment();
        LibraryFragment libraryFragment = new LibraryFragment();
        MenuItem menuItem = toolbar.getMenu().findItem(R.id.menuHomeNotification);
        mDigitalPrintTable = new DigitalPrintTable(this);
        mAccount = new Account();
        View actionView = toolbar.getMenu().getItem(1).getActionView();
        mBadgeTextView = actionView.findViewById(R.id.badge);
        int notifCount = (int) NotifNumber.getLastKnownLocation(getApplicationContext());
        if (notifCount == 0) {
            mBadgeTextView.setVisibility(View.GONE);
        } else {
            mBadgeTextView.setVisibility(View.VISIBLE);
            mBadgeTextView.setText(String.valueOf(notifCount));
        }
        actionView.setOnClickListener(v -> onOptionsItemSelected(toolbar.getMenu().getItem(1)));
        BroadcastReceiver updateBadgeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("ACTION_UPDATE_NOTIFICATION_BADGE".equals(intent.getAction())) {
                    if(mBadgeTextView != null)
                    {
                        mBadgeTextView.setVisibility(View.VISIBLE);
                        mBadgeTextView.setText(String.valueOf(intent.getIntExtra("number", 0)));
                    }
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(updateBadgeReceiver, new IntentFilter("ACTION_UPDATE_NOTIFICATION_BADGE"),Context.RECEIVER_EXPORTED);
        }

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
        if(Build.VERSION.SDK_INT > 9)
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
        //networkCheckWorker(getApplicationContext());
        //startService(reservationService);
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
            UserTable userTable = new UserTable(getApplicationContext());
            Cursor userCursor = userTable.getData(session.getIdNumber());
            userCursor.moveToFirst();
            try {
                byte[] photoByte = userCursor.getBlob(6);
                if(photoByte != null)
                {
                    Glide.with(this)
                            .load(photoByte)
                            .apply(RequestOptions.circleCropTransform())
                            .into(mProfileImageView);
                }
            }catch (Exception e)
            {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e)
        {
            Log.e("Err",e.getMessage());
        }

        /* En cliquant sur "Paramètres" */
        toolbar.getMenu().getItem(2).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent test = new Intent(MainActivity.this, SettingV2Activity.class);
                startActivity(test);
                return false;
            }
        });

        /* En cliquant sur "Déconnecter" */
        toolbar.getMenu().getItem(3).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(mAccount.logout(getApplicationContext()))
                    reboot();
                return false;
            }
        });

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS,android.Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC}, 101);
            }
        }

        /* La mise en place du Fragment par defeaut */
      // getSupportFragmentManager().beginTransaction().replace(R.id.Top_comtainer, mHomeFragment).commit();
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_suggestion, R.id.navigation_library)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

// Vérifier la connexion avant d'initialiser la navigation
       // if (isFirstLaunch && !isConnectedToInternet()) {
         //   navController.navigate(R.id.navigation_library);
           // Toast.makeText(this, "Pas de connexion", Toast.LENGTH_SHORT).show();
        //}

        if (getIntent().getStringExtra("HORS_LINE") != null)
        {
            if (getIntent().getStringExtra("HORS_LINE").equals("ON"))
            {
                navController.navigate(R.id.navigation_library, null, new NavOptions.Builder()
                        .setPopUpTo(navController.getGraph().getStartDestinationId(), true)
                        .build());
            }
               // navController.navigate(R.id.navigation_library);
        }

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(mBottomNavigationView, navController);
       // AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
               // R.id.navigation_home, R.id.navigation_suggestion, R.id.navigation_library)
              //  .build();
        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        //NavigationUI.setupWithNavController(mBottomNavigationView, navController);

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
    private BookStoreFragment mBookStoreFragment = new BookStoreFragment();
    private ChatBotFragment mChatBotFragment = new ChatBotFragment();
    private Account mAccount;
    private DigitalPrintTable mDigitalPrintTable;

    public EditText getEditText() {
        return mEditText;
    }

    public void setEditText(EditText editText) {
        mEditText = editText;
    }
    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }
    public void networkCheckWorker(Context context)
    {
        OneTimeWorkRequest networkCheckRequest = new OneTimeWorkRequest.Builder(NetworkCheckWorker.class).build();
        WorkManager.getInstance(this).enqueue(networkCheckRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menuHomeNotification)
        {
            Intent notificationIntent = new Intent(MainActivity.this,NotificationActivity.class);
            mBadgeTextView.setVisibility(View.GONE);
            startActivity(notificationIntent);
            return true;
        }
        return false;
    }

    private EditText mEditText;
    private ImageView mProfileImageView;
    private TextView mBadgeTextView;
}