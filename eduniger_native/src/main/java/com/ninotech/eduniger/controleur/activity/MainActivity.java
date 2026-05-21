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
import com.ninotech.eduniger.model.data.Themes;
import com.ninotech.eduniger.model.table.DigitalPrintTable;
import com.ninotech.eduniger.model.table.NotificationTable;
import com.ninotech.eduniger.model.table.Session;
import com.ninotech.eduniger.model.table.UserTable;
import com.ninotech.eduniger.model.worker.NetworkCheckWorker;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG               = "MainActivity";
    private static final String ACTION_UPDATE_BADGE = "ACTION_UPDATE_NOTIFICATION_BADGE";
    private static final String THEME_NOT_NIGHT   = "notNight";
    private static final String THEME_NIGHT       = "night";
    private static final String EXTRA_HORS_LINE   = "HORS_LINE";
    private static final String HORS_LINE_ON      = "ON";
    private static final int    PERMISSION_REQUEST_CODE = 101;

    // Views
    private BottomNavigationView mBottomNavigationView;
    private EditText             mEditText;
    private ImageView            mProfileImageView;
    private TextView             mBadgeTextView;

    // Fragments
    private Fragment mFragmentHome;
    private Fragment mFragmentStructure;
    private Fragment mFragmentLibrary;
    private Fragment mActiveFragment;
    private Fragment mFragmentBookStore;

    // Data
    private Account              mAccount;
    private DigitalPrintTable    mDigitalPrintTable;
    private BroadcastReceiver    mUpdateBadgeReceiver;

    //private com.google.android.material.floatingactionbutton.FloatingActionButton mFabAiAssistant;

    // ================================================================
    // CYCLE DE VIE
    // ================================================================

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

    // ================================================================
    // INITIALISATION
    // ================================================================

    private void initializeApp() {
        configureStrictMode();
        applyTheme();
        new Initialization(this).onCreate(this);
        mAccount           = new Account();
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

    private void applyGlassmorphism() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            View backgroundView = findViewById(R.id.backgroundView);
            if (backgroundView != null) {
                RenderEffect blurEffect = RenderEffect.createBlurEffect(
                        80f, 80f, Shader.TileMode.CLAMP);
                backgroundView.setRenderEffect(blurEffect);
            }
        } else {
            View backgroundView = findViewById(R.id.backgroundView);
            if (backgroundView != null) {
                backgroundView.getBackground().setAlpha(180);
            }
        }
    }

    // ================================================================
    // SESSION
    // ================================================================

    private boolean checkSession() {
        return mAccount.isSession(this);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // ================================================================
    // VUES
    // ================================================================

    private void initializeViews() {
        mBottomNavigationView = findViewById(R.id.bottom_navigation_main);
        mEditText             = findViewById(R.id.edit_text_toolbar_search);
        mProfileImageView     = findViewById(R.id.image_view_toolbar_main_profile);
       // mFabAiAssistant       = findViewById(R.id.fab_ai_assistant);

        mEditText.setOnClickListener(v -> navigateToSearch());
       // mFabAiAssistant.setOnClickListener(v -> navigateToChatBot());
        requestNotificationPermission();
    }

    private void navigateToChatBot() {
        Intent intent = new Intent(this, ChatAiActivity.class);
        startActivity(intent);
    }

    private void navigateToSearch() {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("search_key", "ONLINE_BOOK");
        intent.putExtra("online_book_key", "MAIN_ACTIVITY");
        startActivity(intent);
    }

    // ================================================================
    // TOOLBAR
    // ================================================================

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
            Session   session   = new Session(this);
            UserTable userTable = new UserTable(this);
            Cursor    cursor    = userTable.getData(session.getIdNumber());

            Log.d(TAG, "Cursor: " + cursor + " | moveToFirst: " + (cursor != null && cursor.moveToFirst()));

            if (cursor != null && cursor.moveToFirst()) {
                byte[] photoBytes = cursor.getBlob(6);
                Log.d(TAG, "photoBytes: " + (photoBytes != null ? photoBytes.length + " bytes" : "NULL"));

                if (photoBytes != null) {
                    // Toolbar (inchangé)
                    Glide.with(this)
                            .load(photoBytes)
                            .apply(RequestOptions.circleCropTransform())
                            .into(mProfileImageView);

                    // Test sans Glide — chargement direct du Bitmap
                    // 2. Photo dans l'onglet Bibliothèque (icône circulaire)
                    android.graphics.Bitmap bmp = android.graphics.BitmapFactory
                            .decodeByteArray(photoBytes, 0, photoBytes.length);

                    if (bmp != null) {
                        int size = (int) (getResources().getDisplayMetrics().density * 28);

                        android.graphics.Bitmap scaled = android.graphics.Bitmap.createScaledBitmap(bmp, size, size, true);

                        android.graphics.Bitmap circleBmp = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888);
                        android.graphics.Canvas canvas = new android.graphics.Canvas(circleBmp);
                        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
                        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
                        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
                        canvas.drawBitmap(scaled, 0, 0, paint);

                        android.graphics.drawable.BitmapDrawable drawable =
                                new android.graphics.drawable.BitmapDrawable(getResources(), circleBmp);

                        mBottomNavigationView.getMenu()
                                .findItem(R.id.navigation_library)
                                .setIcon(drawable);

                        // ✅ Désactiver le tint coloré du BottomNavigationView
                        mBottomNavigationView.setItemIconTintList(null);
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading user profile", e);
        }
    }

    // ================================================================
    // BADGE DE NOTIFICATION
    // ================================================================

    private void setupNotificationBadge() {
        Toolbar toolbar    = findViewById(R.id.toolbar_search);
        View    actionView = toolbar.getMenu().getItem(1).getActionView();
        mBadgeTextView     = actionView.findViewById(R.id.badge);

        // Comptage initial depuis la BDD locale
        int initialCount = 0;
        try {
            Session           session    = new Session(this);
            NotificationTable notifTable = new NotificationTable(this);
            initialCount = notifTable.getUnreadCount(session.getIdNumber());
        } catch (Exception e) {
            Log.e(TAG, "Erreur comptage initial badge", e);
        }
        updateBadgeCount(initialCount);

        // Clic sur la cloche : masque le badge et ouvre les notifications
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
            registerReceiver(
                    mUpdateBadgeReceiver,
                    new IntentFilter(ACTION_UPDATE_BADGE),
                    Context.RECEIVER_EXPORTED
            );
        }
    }

    // ================================================================
    // NAVIGATION PAR FRAGMENTS (hide / show)
    // ================================================================

    private void setupNavigation() {
        mFragmentHome      = new HomeFragment();
        mFragmentBookStore = new BookStoreFragment();
        mFragmentStructure = new StructureFragment();
        mFragmentLibrary   = new LibraryFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.nav_host_fragment_activity_main, mFragmentLibrary,   "library").hide(mFragmentLibrary)
                .add(R.id.nav_host_fragment_activity_main, mFragmentBookStore, "bookstore").hide(mFragmentBookStore)
                .add(R.id.nav_host_fragment_activity_main, mFragmentStructure, "chat").hide(mFragmentStructure)
                .add(R.id.nav_host_fragment_activity_main, mFragmentHome,      "home")
                .commit();

        mActiveFragment = mFragmentHome;

        // Mode hors-ligne : démarrer sur Library
        String horsLine = getIntent().getStringExtra(EXTRA_HORS_LINE);
        if (HORS_LINE_ON.equals(horsLine)) {
            showFragment(mFragmentLibrary);
            mBottomNavigationView.setSelectedItemId(R.id.navigation_library);
        }

        mBottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                showFragment(mFragmentHome);
            } else if (id == R.id.navigation_structure) {
                showFragment(mFragmentBookStore);
            } else if (id == R.id.navigation_suggestion) {
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

    // ================================================================
    // DEEP LINK
    // ================================================================

    private void handleDeepLink() {
        Uri data = getIntent().getData();
        if (data != null) {
            String id   = data.getQueryParameter("id");
            String name = data.getQueryParameter("name");
            Log.d(TAG, "Deep link received - ID: " + id + ", Name: " + name);
        }
    }

    // ================================================================
    // DIGITAL PRINT
    // ================================================================

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

    // ================================================================
    // PERMISSIONS
    // ================================================================

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

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE
                );
            }
        }
    }

    // ================================================================
    // WORKER RÉSEAU
    // ================================================================

    private void startNetworkWorker() {
        OneTimeWorkRequest networkCheckRequest =
                new OneTimeWorkRequest.Builder(NetworkCheckWorker.class).build();
        WorkManager.getInstance(this).enqueue(networkCheckRequest);
    }

    // ================================================================
    // MENU
    // ================================================================

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuHomeNotification) {
            mBadgeTextView.setVisibility(View.GONE);
            navigateToNotifications();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ================================================================
    // UTILITAIRES
    // ================================================================

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
}