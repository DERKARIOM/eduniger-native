package com.ninotech.eduniger.controleur.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ninotech.eduniger.R;
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

    // Data
    private Account mAccount;
    private DigitalPrintTable mDigitalPrintTable;
    private BroadcastReceiver mUpdateBadgeReceiver;

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
        handleDeepLink();
        checkDigitalPrint();
        requestPermissions();
        startNetworkWorker();
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

        mEditText.setOnClickListener(v -> navigateToSearch());
    }

    private void navigateToSearch() {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("search_key", "ONLINE_BOOK");
        intent.putExtra("online_book_key", "MAIN_ACTIVITY");
        startActivity(intent);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_search);

        // Refresh button
        toolbar.getMenu().getItem(0).setOnMenuItemClickListener(item -> {
            refreshActivity();
            return true;
        });

        // Notifications button
        toolbar.getMenu().getItem(1).setOnMenuItemClickListener(item -> {
            navigateToNotifications();
            return true;
        });

        // Settings button
        toolbar.getMenu().getItem(2).setOnMenuItemClickListener(item -> {
            navigateToSettings();
            return true;
        });

        // Logout button
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

    private void setupNavigation() {
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_suggestion,
                R.id.navigation_library
        ).build();

        NavController navController = Navigation.findNavController(
                this, R.id.nav_host_fragment_activity_main);

        // Handle offline mode
        String horsLine = getIntent().getStringExtra(EXTRA_HORS_LINE);
        if (HORS_LINE_ON.equals(horsLine)) {
            navigateToLibrary(navController);
        }

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(mBottomNavigationView, navController);
    }

    private void navigateToLibrary(NavController navController) {
        navController.navigate(R.id.navigation_library, null,
                new NavOptions.Builder()
                        .setPopUpTo(navController.getGraph().getStartDestinationId(), true)
                        .build());
    }

    private void handleDeepLink() {
        Uri data = getIntent().getData();
        if (data != null) {
            String id = data.getQueryParameter("id");
            String name = data.getQueryParameter("name");
            Log.d(TAG, "Deep link received - ID: " + id + ", Name: " + name);
            // Handle deep link data as needed
        }
    }

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

    private void startNetworkWorker() {
        OneTimeWorkRequest networkCheckRequest =
                new OneTimeWorkRequest.Builder(NetworkCheckWorker.class).build();
        WorkManager.getInstance(this).enqueue(networkCheckRequest);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuHomeNotification) {
            mBadgeTextView.setVisibility(View.GONE);
            navigateToNotifications();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ==================== Utility Methods ====================

    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    // ==================== Getters/Setters ====================

    public BottomNavigationView getBottomNavigationView() {
        return mBottomNavigationView;
    }

    public EditText getEditText() {
        return mEditText;
    }

    public void setEditText(EditText editText) {
        mEditText = editText;
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
}