package com.ninotech.eduniger.controleur.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.fragment.BookStoreFragment;
import com.ninotech.eduniger.controleur.fragment.HomeFragment;
import com.ninotech.eduniger.controleur.fragment.LibraryFragment;
import com.ninotech.eduniger.controleur.fragment.StructureFragment;
import com.ninotech.eduniger.model.data.Account;
import com.ninotech.eduniger.model.data.Initialization;
import com.ninotech.eduniger.model.data.NotifNumber;
import com.ninotech.eduniger.model.data.Themes;
import com.ninotech.eduniger.model.table.DigitalPrintTable;
import com.ninotech.eduniger.model.table.Session;
import com.ninotech.eduniger.model.table.UserTable;
import com.ninotech.eduniger.model.worker.NetworkCheckWorker;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String ACTION_UPDATE_BADGE = "ACTION_UPDATE_NOTIFICATION_BADGE";
    private static final String THEME_NOT_NIGHT = "notNight";
    private static final String THEME_NIGHT = "night";
    private static final String EXTRA_HORS_LINE = "HORS_LINE";
    private static final String HORS_LINE_ON = "ON";
    private static final int PERMISSION_REQUEST_CODE = 101;

    // Views
    private BottomNavigationView mBottomNavigationView;
    private EditText mEditText;
    private ImageView mProfileImageView;
    private TextView mBadgeTextView;

    // Fragments
    private Fragment mFragmentHome;
    private Fragment mFragmentStructure;
    private Fragment mFragmentLibrary;
    private Fragment mActiveFragment;

    // Data
    private Account mAccount;
    private DigitalPrintTable mDigitalPrintTable;
    private BroadcastReceiver mUpdateBadgeReceiver;
    private Fragment mFragmentBookStore;
    private com.google.android.material.floatingactionbutton.FloatingActionButton mFabAiAssistant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        initializeApp();

        if (!checkSession()) {
            navigateToLogin();
            return;
        }

        initializeViews();
        setupToolbar();
        setupNotificationBadge();
        setupNavigation();
        applyGlassmorphism();
        handleDeepLink();
        checkDigitalPrint();
        requestPermissions();
        startNetworkWorker();
    }

    private void applyGlassmorphism() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            View backgroundView = findViewById(R.id.backgroundView);
            if (backgroundView != null) {
                RenderEffect blurEffect = RenderEffect.createBlurEffect(
                        80f, 80f, Shader.TileMode.CLAMP);
                backgroundView.setRenderEffect(blurEffect);
            }
        } else {
            // Fallback API < 31
            View backgroundView = findViewById(R.id.backgroundView);
            if (backgroundView != null) {
                backgroundView.getBackground().setAlpha(180);
            }
        }
    }

    private void initializeApp() {
        configureStrictMode();
        applyTheme();
        new Initialization(this).onCreate(this);
        mAccount = new Account();
        mDigitalPrintTable = new DigitalPrintTable(this);
    }

    private void configureStrictMode() {
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll()
                    .build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    private void applyTheme() {
        String themeName = Themes.getName(this);
        if (THEME_NOT_NIGHT.equals(themeName)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (THEME_NIGHT.equals(themeName)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    private boolean checkSession() {
        return mAccount.isSession(this);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void initializeViews() {
        mBottomNavigationView = findViewById(R.id.bottom_navigation_main);
        mEditText = findViewById(R.id.edit_text_toolbar_search);
        mProfileImageView = findViewById(R.id.image_view_toolbar_main_profile);
        mFabAiAssistant       = findViewById(R.id.fab_ai_assistant);  // ← nouveau

        mEditText.setOnClickListener(v -> navigateToSearch());
        mFabAiAssistant.setOnClickListener(v -> navigateToChatBot());
    }

    private void navigateToChatBot() {
        Toast.makeText(this, "Eduna", Toast.LENGTH_SHORT).show();
    }

    private void navigateToSearch() {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("search_key", "ONLINE_BOOK");
        intent.putExtra("online_book_key", "MAIN_ACTIVITY");
        startActivity(intent);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_search);

        toolbar.getMenu().getItem(0).setOnMenuItemClickListener(item -> {
            refreshActivity();
            return true;
        });

        toolbar.getMenu().getItem(1).setOnMenuItemClickListener(item -> {
            navigateToNotifications();
            return true;
        });

        toolbar.getMenu().getItem(2).setOnMenuItemClickListener(item -> {
            navigateToSettings();
            return true;
        });

        toolbar.getMenu().getItem(3).setOnMenuItemClickListener(item -> {
            logout();
            return true;
        });

        loadUserProfile();
    }

    private void refreshActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToNotifications() {
        Intent intent = new Intent(this, NotificationActivity.class);
        startActivity(intent);
    }

    private void navigateToSettings() {
        Intent intent = new Intent(this, SettingV2Activity.class);
        startActivity(intent);
    }

    private void logout() {
        if (mAccount.logout(this)) {
            navigateToLogin();
        }
    }

    private void loadUserProfile() {
        try {
            Session session = new Session(this);
            UserTable userTable = new UserTable(this);
            Cursor cursor = userTable.getData(session.getIdNumber());

            if (cursor != null && cursor.moveToFirst()) {
                byte[] photoBytes = cursor.getBlob(6);
                if (photoBytes != null) {
                    Glide.with(this)
                            .load(photoBytes)
                            .apply(RequestOptions.circleCropTransform())
                            .into(mProfileImageView);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading user profile", e);
        }
    }

    private void setupNotificationBadge() {
        Toolbar toolbar = findViewById(R.id.toolbar_search);
        View actionView = toolbar.getMenu().getItem(1).getActionView();
        mBadgeTextView = actionView.findViewById(R.id.badge);

        updateBadgeCount(NotifNumber.getLastKnownLocation(this));

        actionView.setOnClickListener(v -> {
            mBadgeTextView.setVisibility(View.GONE);
            navigateToNotifications();
        });

        registerBadgeReceiver();
    }

    private void updateBadgeCount(int count) {
        if (count == 0) {
            mBadgeTextView.setVisibility(View.GONE);
        } else {
            mBadgeTextView.setVisibility(View.VISIBLE);
            mBadgeTextView.setText(String.valueOf(count));
        }
    }

    private void registerBadgeReceiver() {
        mUpdateBadgeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_UPDATE_BADGE.equals(intent.getAction())) {
                    int count = intent.getIntExtra("number", 0);
                    updateBadgeCount(count);
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(mUpdateBadgeReceiver,
                    new IntentFilter(ACTION_UPDATE_BADGE),
                    Context.RECEIVER_EXPORTED);
        }
    }

    // ==================== Navigation par fragments (hide/show) ====================

    private void setupNavigation() {
        // Créer les fragments une seule fois
        mFragmentHome    = new HomeFragment();
        mFragmentBookStore = new BookStoreFragment();
        mFragmentStructure = new StructureFragment();
        mFragmentLibrary = new LibraryFragment();

        // Les ajouter tous les 3, cacher chat et library
        getSupportFragmentManager().beginTransaction()
                .add(R.id.nav_host_fragment_activity_main, mFragmentLibrary, "library").hide(mFragmentLibrary)
                .add(R.id.nav_host_fragment_activity_main, mFragmentBookStore, "bookstore").hide(mFragmentBookStore)
                .add(R.id.nav_host_fragment_activity_main, mFragmentStructure,    "chat").hide(mFragmentStructure)
                .add(R.id.nav_host_fragment_activity_main, mFragmentHome,    "home")
                .commit();

        mActiveFragment = mFragmentHome;

        // Gérer le mode hors-ligne : démarrer sur Library
        String horsLine = getIntent().getStringExtra(EXTRA_HORS_LINE);
        if (HORS_LINE_ON.equals(horsLine)) {
            showFragment(mFragmentLibrary);
            mBottomNavigationView.setSelectedItemId(R.id.navigation_library);
        }

        // Écouter les clics sur la barre de navigation
        mBottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                showFragment(mFragmentHome);
            } else if (id == R.id.navigation_structure) {  // ← remplacez par le bon ID
                showFragment(mFragmentBookStore);
            } else if (id == R.id.navigation_suggestion) {        // ← remplacez par le bon ID
                showFragment(mFragmentStructure);
            } else if (id == R.id.navigation_library) {
                showFragment(mFragmentLibrary);
            }
            return true;
        });
    }

    private void showFragment(Fragment target) {
        if (target == mActiveFragment) return;
        getSupportFragmentManager().beginTransaction()
                .hide(mActiveFragment)
                .show(target)
                .commit();
        mActiveFragment = target;
    }

    // ==================== Deep link ====================

    private void handleDeepLink() {
        Uri data = getIntent().getData();
        if (data != null) {
            String id = data.getQueryParameter("id");
            String name = data.getQueryParameter("name");
            Log.d(TAG, "Deep link received - ID: " + id + ", Name: " + name);
        }
    }

    // ==================== Digital print ====================

    private void checkDigitalPrint() {
        try {
            if ("0".equals(mDigitalPrintTable.getPass())) {
                Intent intent = new Intent(this, LockActivity.class);
                startActivity(intent);
                finish();
            } else {
                mDigitalPrintTable.onUpdate("0");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking digital print", e);
        }
    }

    // ==================== Permissions ====================

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS,
                                Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC
                        },
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    // ==================== Worker réseau ====================

    private void startNetworkWorker() {
        OneTimeWorkRequest networkCheckRequest =
                new OneTimeWorkRequest.Builder(NetworkCheckWorker.class).build();
        WorkManager.getInstance(this).enqueue(networkCheckRequest);
    }

    // ==================== Menu ====================

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuHomeNotification) {
            mBadgeTextView.setVisibility(View.GONE);
            navigateToNotifications();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ==================== Utilitaires ====================

    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    public BottomNavigationView getBottomNavigationView() {
        return mBottomNavigationView;
    }

    public EditText getEditText() {
        return mEditText;
    }

    public void setEditText(EditText editText) {
        mEditText = editText;
    }

    // ==================== Cycle de vie ====================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUpdateBadgeReceiver != null) {
            try {
                unregisterReceiver(mUpdateBadgeReceiver);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver", e);
            }
        }
    }
}