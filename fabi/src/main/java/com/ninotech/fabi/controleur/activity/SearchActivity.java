package com.ninotech.fabi.controleur.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.*;
import com.ninotech.fabi.model.data.*;
import com.ninotech.fabi.model.table.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    // Search Keys
    private enum SearchKey {
        ONLINE_BOOK, ELECTRONIC_BOOK, AUDIO_BOOK, LOAND_BOOK,
        FABIOLA_BOOK, CATEGORY, STRUCT_CATEGORY, STRUCTURE,
        AUTHOR, AUTHOR_ONLINE, NOTIFICATION, SETTING,
        BOOK_IN_CATEGORY, BOOK_IN_AUTHOR
    }

    // Views
    private RecyclerView mRecyclerView;
    private EditText mSearchEditText;
    private ImageView mBackImageView;

    // Data
    private Session mSession;
    private OkHttpClient mHttpClient;
    private LinearLayoutManager mLayoutManager;

    // Lists
    private ArrayList<OnlineBook> mOnlineBooks;
    private ArrayList<OnlineBook> mFilteredOnlineBookList;
    private ArrayList<ElectronicBook> mElectronicBooks;
    private ArrayList<ElectronicBook> mFilteredElectronicBooks;
    private ArrayList<AudioBook> mAudioBooks;
    private ArrayList<AudioBook> mFilteredAudioBook;
    private ArrayList<LoandBook> mLoandBooks;
    private ArrayList<LoandBook> mFilteredLoandBooks;
    private ArrayList<Structure> mStructures;
    private ArrayList<Structure> mFilterStructures;
    private ArrayList<Category> mCategorys;
    private ArrayList<Category> mCategoryList;
    private ArrayList<Category> mFilteredCategorys;
    private ArrayList<Author> mAuthors;
    private ArrayList<Author> mFilteredAuthors;
    private ArrayList<Notification> mNotifications;
    private ArrayList<Notification> mFilteredNotifications;
    private ArrayList<Setting> mSettings;
    private ArrayList<Setting> mFilteredSettings;
    private List<LocalBooks> mLocalBooks;
    private ArrayList<LocalBooks> mFilterLocalBooks;

    // Adapters
    private OnlineBookAdapter mOnlineBookAdapter;
    private FabiolaBookAdapter mFabiolaBookAdapter;
    private ElectronicBookAdapter mElectronicBookAdapter;
    private AudioBookAdapter mAudioBookAdapter;
    private LoandBookAdapter mLoandBookAdapter;
    private StructureAdapter mStructureAdapter;
    private CategoryLocalAdapter mCategoryLocalAdapter;
    private AuthorLocalAdapter mAuthorLocalAdapter;
    private AuthorVerticaleAdapter mAuthorVerticaleAdapter;
    private NotificationAdapter mNotificationAdapter;
    private SettingAdapter mSettingAdapter;
    private LocalBookAdapter mLocalBookAdapter;
    private NoConnectionAdapter mNoConnectionAdapter;

    // Receivers
    private final Set<BroadcastReceiver> mRegisteredReceivers = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        hideActionBar();
        initializeViews();
        initializeData();
        setupListeners();
        handleSearchIntent(getIntent());
    }

    private void hideActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void initializeViews() {
        mRecyclerView = findViewById(R.id.recycler_view_activity_search);
        mSearchEditText = findViewById(R.id.edit_text_toolbar_search);
        mBackImageView = findViewById(R.id.image_view_toolbar_search);
        mSearchEditText.requestFocus();
    }

    private void initializeData() {
        mSession = new Session(this);
        mHttpClient = new OkHttpClient();
        mLayoutManager = new LinearLayoutManager(this);
        mCategoryList = new ArrayList<>();
        mSearchEditText.clearFocus();
    }

    private void setupListeners() {
        mBackImageView.setOnClickListener(v -> onBackPressed());
        mSearchEditText.addTextChangedListener(createSearchTextWatcher());
        registerBookRecoveryReceiver();
    }

    private void registerBookRecoveryReceiver() {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("ACTION_RECOVER_BOOK".equals(intent.getAction())) {
                    finish();
                }
            }
        };
        registerReceiverSafely(receiver, new IntentFilter("ACTION_RECOVER_BOOK"));
    }

    private TextWatcher createSearchTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                handleSearchFilter(s.toString());
            }
        };
    }

    private void handleSearchFilter(String query) {
        Intent intent = getIntent();
        String searchKey = intent.getStringExtra("search_key");
        if (searchKey == null) return;

        try {
            SearchKey key = SearchKey.valueOf(searchKey);
            performFilter(key, query);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid search key: " + searchKey, e);
        }
    }

    private void performFilter(SearchKey key, String query) {
        switch (key) {
            case ONLINE_BOOK:
                if (!mOnlineBooks.isEmpty()) filterOnlineBook(query);
                break;
            case ELECTRONIC_BOOK:
                if (!mElectronicBooks.isEmpty()) filterElectronicBook(query);
                break;
            case AUDIO_BOOK:
                if (!mAudioBooks.isEmpty()) filterAudioBook(query);
                break;
            case LOAND_BOOK:
                if (!mLoandBooks.isEmpty()) filterLoandBook(query);
                break;
            case FABIOLA_BOOK:
                filterFabiolaBook(query);
                break;
            case CATEGORY:
                if (!mCategorys.isEmpty()) filterCategory(query);
                break;
            case STRUCT_CATEGORY:
                if (!mCategoryList.isEmpty()) filterStructCategory(query);
                break;
            case STRUCTURE:
                if (!mStructures.isEmpty()) filterOnlineStructure(query);
                break;
            case AUTHOR:
                if (!mAuthors.isEmpty()) filterAuthor(query);
                break;
            case AUTHOR_ONLINE:
                if (!mAuthors.isEmpty()) filterAuthorOnline(query);
                break;
            case NOTIFICATION:
                if (!mNotifications.isEmpty()) filterNotification(query);
                break;
            case SETTING:
                if (!mSettings.isEmpty()) filterSettings(query);
                break;
            case BOOK_IN_CATEGORY:
            case BOOK_IN_AUTHOR:
                if (!mLocalBooks.isEmpty()) filterBookInCategory(query);
                break;
        }
    }

    private void handleSearchIntent(Intent intent) {
        String searchKey = intent.getStringExtra("search_key");
        if (searchKey == null) return;

        try {
            SearchKey key = SearchKey.valueOf(searchKey);
            executeSearch(key, intent);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid search key: " + searchKey, e);
        }
    }

    private void executeSearch(SearchKey key, Intent intent) {
        switch (key) {
            case ONLINE_BOOK:
                handleOnlineBookSearch(intent);
                break;
            case STRUCTURE:
                handleStructureSearch();
                break;
            case ELECTRONIC_BOOK:
                searchElectronicBook();
                break;
            case AUDIO_BOOK:
                searchAudioBook();
                break;
            case LOAND_BOOK:
                searchLoandBook();
                break;
            case FABIOLA_BOOK:
                handleFabiolaBookSearch();
                break;
            case CATEGORY:
                searchCategory();
                break;
            case AUTHOR_ONLINE:
                handleAuthorOnlineSearch();
                break;
            case AUTHOR:
                searchAuthor();
                break;
            case NOTIFICATION:
                searchNotification();
                break;
            case SETTING:
                searchSetting();
                break;
            case BOOK_IN_CATEGORY:
                searchBookInCategory(intent.getStringExtra("category"));
                break;
            case BOOK_IN_AUTHOR:
                searchBookInAuthor(intent.getStringExtra("author"));
                break;
        }
    }

    // ==================== Online Book Search ====================

    private void handleOnlineBookSearch(Intent intent) {
        initializeOnlineBookLists();
        showWaitingState();

        String onlineBookKey = intent.getStringExtra("online_book_key");
        if (onlineBookKey == null) return;

        switch (onlineBookKey) {
            case "MAIN_ACTIVITY":
                searchOnLineBook();
                break;
            case "CATEGORY_ACTIVITY":
                onLineBookSwitchCategory(intent.getStringExtra("title_category"));
                break;
            case "STRUCTURE_ACTIVITY":
                searchStructBook(intent.getStringExtra("id_struct_key"));
                break;
            case "AUTHOR_ACTIVITY":
                searchAuthorBook("AuthorBook.php", intent.getStringExtra("id_author_key"));
                break;
            case "AUTHOR_FORMAT_BOOK_PDF_ADAPTER":
                searchAuthorBook("AuthorPDFBook.php", intent.getStringExtra("id_author_key"));
                break;
            case "AUTHOR_FORMAT_BOOK_AUDIO_ADAPTER":
                searchAuthorBook("AuthorAudioBook.php", intent.getStringExtra("id_author_key"));
                break;
            case "AUTHOR_FORMAT_BOOK_PHYSIC_ADAPTER":
                searchAuthorBook("AuthorPhysicBook.php", intent.getStringExtra("id_author_key"));
                break;
            case "STRUCTURE_CATEGORIE":
                searchStructCategorie("Category.php", mSession.getIdNumber());
                break;
        }
    }

    private void initializeOnlineBookLists() {
        mOnlineBooks = new ArrayList<>();
        mFilteredOnlineBookList = new ArrayList<>();
    }

    private void searchOnLineBook() {
        registerRefreshReceiver("BOOKS_SEARCH", () ->
                new NetworkTask<>(this, "Ranking.php").execute(mSession.getIdNumber())
        );
        new NetworkTask<OnlineBook>(this, "Ranking.php").execute(mSession.getIdNumber());
    }

    private void searchStructBook(String idStruct) {
        registerRefreshReceiver("STRUCT_SEARCH", () ->
                new NetworkTask<>(this, "StructBookMore.php").execute(mSession.getIdNumber(), idStruct)
        );
        new NetworkTask<OnlineBook>(this, "StructBookMore.php")
                .execute(mSession.getIdNumber(), idStruct);
    }

    private void searchAuthorBook(String fileName, String idAuthor) {
        registerRefreshReceiver("AUTHOR_SEARCH", () ->
                new NetworkTask<>(this, fileName).execute(mSession.getIdNumber(), idAuthor)
        );
        new NetworkTask<OnlineBook>(this, fileName).execute(mSession.getIdNumber(), idAuthor);
    }

    private void searchStructCategorie(String fileName, String idNumber) {
        registerRefreshReceiver("STRUCT_CATEGORIE", () ->
                new NetworkTask<>(this, fileName).execute(idNumber)
        );
        new NetworkTask<Category>(this, fileName).execute(idNumber);
    }

    // ==================== Structure Search ====================

    private void handleStructureSearch() {
        mStructures = new ArrayList<>();
        mFilterStructures = new ArrayList<>();
        mStructureAdapter = new StructureAdapter(mStructures);
        mSearchEditText.setHint(R.string.search_structure);
        showWaitingState();
        searchOnLineStructure();
    }

    private void searchOnLineStructure() {
        registerRefreshReceiver("STRUCT_SEARCH", () -> {
            new StructureTask(this, "Structure.php").execute(mSession.getIdNumber());
            new StructureTask2(this, "StructureMore.php").execute(mSession.getIdNumber());
        });

        new StructureTask(this, "Structure.php").execute(mSession.getIdNumber());
        new StructureTask2(this, "StructureMore.php").execute(mSession.getIdNumber());
    }

    // ==================== Fabiola Book Search ====================

    private void handleFabiolaBookSearch() {
        initializeOnlineBookLists();
        registerRefreshReceiver("RANKING_FRAGMENT", () ->
                new FabiolaBookTask(this).execute(mSession.getIdNumber())
        );
        new FabiolaBookTask(this).execute(mSession.getIdNumber());
    }

    private void onLineBookSwitchCategory(String category) {
        registerRefreshReceiver("CATEGORY_ACTIVITY", () ->
                new CategoryInTask(this).execute(mSession.getIdNumber(), category)
        );
        new CategoryInTask(this).execute(mSession.getIdNumber(), category);
    }

    // ==================== Author Online Search ====================

    private void handleAuthorOnlineSearch() {
        mAuthors = new ArrayList<>();
        mFilteredAuthors = new ArrayList<>();
        mSearchEditText.setHint(R.string.search_author);
        showWaitingState();
        searchAuthorOnline();
    }

    private void searchAuthorOnline() {
        registerRefreshReceiver("AUTHOR_SEARCH", () ->
                new AuthorTask(this).execute(mSession.getIdNumber())
        );
        new AuthorTask(this).execute(mSession.getIdNumber());
    }

    // ==================== Local Search Methods ====================

    private void searchElectronicBook() {
        try {
            mElectronicBooks = new ArrayList<>();
            mFilteredElectronicBooks = new ArrayList<>();

            ElectronicTable table = new ElectronicTable(this);
            try (Cursor cursor = table.getData(mSession.getIdNumber())) {
                if (cursor.moveToFirst()) {
                    do {
                        mElectronicBooks.add(new ElectronicBook(
                                cursor.getString(2), cursor.getString(5),
                                cursor.getString(8), cursor.getString(7),
                                cursor.getString(4), cursor.getString(6)
                        ));
                    } while (cursor.moveToNext());
                }
            }

            mElectronicBookAdapter = new ElectronicBookAdapter(mElectronicBooks);
            setupRecyclerView(mElectronicBookAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error searching electronic books", e);
            showEmptyState(R.drawable.img_telecharge_local,
                    getString(R.string.no_electronic_book));
        }
    }

    private void searchAudioBook() {
        try {
            mAudioBooks = new ArrayList<>();
            mFilteredAudioBook = new ArrayList<>();

            AudioTable table = new AudioTable(this);
            try (Cursor cursor = table.getData(mSession.getIdNumber())) {
                if (cursor.moveToFirst()) {
                    do {
                        mAudioBooks.add(new AudioBook(
                                cursor.getString(2), cursor.getString(5),
                                cursor.getString(8), cursor.getString(4),
                                cursor.getString(11), cursor.getString(6)
                        ));
                    } while (cursor.moveToNext());
                }
            }

            mAudioBookAdapter = new AudioBookAdapter(mAudioBooks);
            setupRecyclerView(mAudioBookAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error searching audio books", e);
            showEmptyState(R.drawable.img_playliste_local,
                    getString(R.string.no_audio_book));
        }
    }

    private void searchLoandBook() {
        try {
            mLoandBooks = new ArrayList<>();
            mFilteredLoandBooks = new ArrayList<>();

            LoandTable table = new LoandTable(this);
            try (Cursor cursor = table.getData()) {
                if (cursor.moveToFirst()) {
                    do {
                        long startDate = converterDate(cursor.getString(4));
                        long endDate = converterDate(cursor.getString(5));
                        long percentage = calculatePercentage(startDate, endDate, getCurrentTimeSeconds());

                        mLoandBooks.add(new LoandBook(
                                cursor.getString(2), cursor.getString(3),
                                cursor.getString(4), cursor.getString(5), percentage
                        ));
                    } while (cursor.moveToNext());
                }
            }

            mLoandBookAdapter = new LoandBookAdapter(mLoandBooks);
            setupRecyclerView(mLoandBookAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error searching loand books", e);
            showEmptyState(R.drawable.img_physical, getString(R.string.no_loand_book));
        }
    }

    private void searchCategory() {
        mSearchEditText.setHint(R.string.search_category);
        try {
            mCategorys = new ArrayList<>();
            mFilteredCategorys = new ArrayList<>();

            ElectronicTable table = new ElectronicTable(this);
            try (Cursor cursor = table.getCategoryData(mSession.getIdNumber())) {
                if (cursor.moveToFirst()) {
                    do {
                        mCategorys.add(new Category(
                                cursor.getString(0), cursor.getString(1)
                        ));
                    } while (cursor.moveToNext());
                }
            }

            mCategoryLocalAdapter = new CategoryLocalAdapter(mCategorys);
            setupRecyclerView(mCategoryLocalAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error searching categories", e);
            showEmptyState(R.drawable.img_categorie, getString(R.string.no_category));
        }
    }

    private void searchAuthor() {
        mSearchEditText.setHint(R.string.search_author);
        try {
            mAuthors = new ArrayList<>();
            mFilteredAuthors = new ArrayList<>();

            ElectronicTable table = new ElectronicTable(this);
            try (Cursor cursor = table.getAuthorData(mSession.getIdNumber())) {
                if (cursor.moveToFirst()) {
                    do {
                        mAuthors.add(new Author(
                                cursor.getString(0), cursor.getString(1)
                        ));
                    } while (cursor.moveToNext());
                }
            }

            mAuthorLocalAdapter = new AuthorLocalAdapter(mAuthors);
            setupRecyclerView(mAuthorLocalAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error searching authors", e);
            showEmptyState(R.drawable.img_auteur_local, getString(R.string.no_author));
        }
    }

    private void searchNotification() {
        mSearchEditText.setHint(R.string.search_notification);
        try {
            mNotifications = new ArrayList<>();
            mFilteredNotifications = new ArrayList<>();

            NotificationTable table = new NotificationTable(this);
            try (Cursor cursor = table.getData(mSession.getIdNumber())) {
                if (cursor.moveToFirst()) {
                    do {
                        mNotifications.add(new Notification(
                                cursor.getString(0), cursor.getString(2),
                                cursor.getString(3), cursor.getString(4), null
                        ));
                    } while (cursor.moveToNext());
                }
            }

            mNotificationAdapter = new NotificationAdapter(mNotifications);
            setupRecyclerView(mNotificationAdapter);
            mRecyclerView.smoothScrollToPosition(mNotificationAdapter.getItemCount() - 1);
        } catch (Exception e) {
            Log.e(TAG, "Error searching notifications", e);
            showEmptyState(R.drawable.img_message_suggestion,
                    getString(R.string.no_notification));
        }
    }

    private void searchSetting() {
        mSearchEditText.setHint(R.string.search_setting);
        mSettings = new ArrayList<>();
        mFilteredSettings = new ArrayList<>();

        mSettings.add(new Setting(R.drawable.vector_purple_200_compte,
                getString(R.string.account), getString(R.string.change_password)));
        mSettings.add(new Setting(R.drawable.vector_purple_200_digital,
                getString(R.string.digital_print), getString(R.string.secure_session)));
        mSettings.add(new Setting(R.drawable.vector_purple_200_messagerie,
                getString(R.string.send_suggestion), getString(R.string.subject_suggestion)));
        mSettings.add(new Setting(R.drawable.vector_purple_200_start,
                getString(R.string.evaluate_us), getString(R.string.opservation_you)));
        mSettings.add(new Setting(R.drawable.vector_purple_200_phone,
                getString(R.string.contact_us), getString(R.string.call_number)));
        mSettings.add(new Setting(R.drawable.vector_purple_200_video,
                getString(R.string.how_it_works), getString(R.string.tutorial_that_explains_you_from_a_z)));
        mSettings.add(new Setting(R.drawable.vector_purple_200_information,
                getString(R.string.app_information), getString(R.string.sub_app_information)));

        mSettingAdapter = new SettingAdapter(mSettings);
        setupRecyclerView(mSettingAdapter);
    }

    private void searchBookInCategory(String category) {
        searchBooksByFilter("category", category);
    }

    private void searchBookInAuthor(String author) {
        searchBooksByFilter("author", author);
    }

    private void searchBooksByFilter(String filterType, String filterValue) {
        mLocalBooks = new ArrayList<>();
        mFilterLocalBooks = new ArrayList<>();

        ElectronicTable electronicTable = new ElectronicTable(this);
        AudioTable audioTable = new AudioTable(this);
        int errorCount = 0;

        try {
            Cursor cursor = filterType.equals("category")
                    ? electronicTable.getDataC(mSession.getIdNumber(), filterValue)
                    : electronicTable.getDataA(mSession.getIdNumber(), filterValue);

            if (cursor.moveToFirst()) {
                do {
                    mLocalBooks.add(new LocalBooks(
                            cursor.getString(2), cursor.getString(5), cursor.getString(8),
                            cursor.getString(7), cursor.getString(4), cursor.getString(6),
                            cursor.getString(5), "Électronique", filterType
                    ));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            errorCount++;
            Log.e(TAG, "Error getting electronic books by " + filterType, e);
        }

        try {
            Cursor cursor = filterType.equals("category")
                    ? audioTable.getDataC(mSession.getIdNumber(), filterValue)
                    : audioTable.getDataA(mSession.getIdNumber(), filterValue);

            if (cursor.moveToFirst()) {
                do {
                    mLocalBooks.add(new LocalBooks(
                            cursor.getString(2), cursor.getString(5), cursor.getString(8),
                            cursor.getString(7), cursor.getString(4), cursor.getString(6),
                            cursor.getString(5), "Audio", filterType
                    ));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            errorCount++;
            Log.e(TAG, "Error getting audio books by " + filterType, e);
        }

        if (errorCount != 2) {
            mLocalBookAdapter = new LocalBookAdapter(mLocalBooks);
            setupRecyclerView(mLocalBookAdapter);
            registerForContextMenu(mRecyclerView);
        } else {
            showEmptyState(R.drawable.img_telecharge_local,
                    getString(R.string.no_electronic_book));
        }
    }

    // ==================== Filter Methods ====================

    private void filterOnlineBook(String query) {
        filterList(mOnlineBooks, mFilteredOnlineBookList, query,
                OnlineBook::getTitle, () -> mOnlineBookAdapter.filterList(mFilteredOnlineBookList));
    }

    private void filterFabiolaBook(String query) {
        filterList(mOnlineBooks, mFilteredOnlineBookList, query,
                OnlineBook::getTitle, () -> mFabiolaBookAdapter.filterList(mFilteredOnlineBookList));
    }

    private void filterOnlineStructure(String query) {
        filterList(mStructures, mFilterStructures, query,
                Structure::getName, () -> mStructureAdapter.filterList(mFilterStructures));
    }

    private void filterElectronicBook(String query) {
        filterList(mElectronicBooks, mFilteredElectronicBooks, query,
                ElectronicBook::getTitle, () -> mElectronicBookAdapter.filterList(mFilteredElectronicBooks));
    }

    private void filterAudioBook(String query) {
        filterList(mAudioBooks, mFilteredAudioBook, query,
                AudioBook::getTitle, () -> mAudioBookAdapter.filterList(mFilteredAudioBook));
    }

    private void filterLoandBook(String query) {
        filterList(mLoandBooks, mFilteredLoandBooks, query,
                LoandBook::getTitle, () -> mLoandBookAdapter.filterList(mFilteredLoandBooks));
    }

    private void filterCategory(String query) {
        filterList(mCategorys, mFilteredCategorys, query,
                Category::getTitle, () -> mCategoryLocalAdapter.filterList(mFilteredCategorys));
    }

    private void filterStructCategory(String query) {
        filterList(mCategoryList, mFilteredCategorys, query,
                Category::getTitle, () -> mCategoryLocalAdapter.filterList(mFilteredCategorys));
    }

    private void filterAuthor(String query) {
        filterList(mAuthors, mFilteredAuthors, query,
                Author::getName, () -> mAuthorLocalAdapter.filterList(mFilteredAuthors));
    }

    private void filterAuthorOnline(String query) {
        filterList(mAuthors, mFilteredAuthors, query,
                Author::getName, () -> mAuthorVerticaleAdapter.filterList(mFilteredAuthors));
    }

    private void filterNotification(String query) {
        filterList(mNotifications, mFilteredNotifications, query,
                Notification::getMessage, () -> mNotificationAdapter.filterList(mFilteredNotifications));
    }

    private void filterSettings(String query) {
        mFilteredSettings.clear();
        // Settings filter logic if needed
        mSettingAdapter.filterList(mFilteredSettings);
    }

    private void filterBookInCategory(String query) {
        filterList(mLocalBooks, mFilterLocalBooks, query,
                LocalBooks::getTitle, () -> mLocalBookAdapter.filterList(mFilterLocalBooks));
    }

    private <T> void filterList(List<T> source, List<T> destination, String query,
                                FilterPredicate<T> predicate, Runnable updateAction) {
        destination.clear();
        String lowerQuery = query.toLowerCase(Locale.getDefault());
        for (T item : source) {
            if (predicate.getValue(item).toLowerCase(Locale.getDefault()).contains(lowerQuery)) {
                destination.add(item);
            }
        }
        updateAction.run();
    }

    @FunctionalInterface
    private interface FilterPredicate<T> {
        String getValue(T item);
    }

    // ==================== UI Helper Methods ====================

    private void showWaitingState() {
        ArrayList<Connection> list = new ArrayList<>();
        list.add(new Connection(getString(R.string.wait), null, true));
        mNoConnectionAdapter = new NoConnectionAdapter(list);
        setupRecyclerView(mNoConnectionAdapter);
    }

    private void showEmptyState(int imageRes, String message) {
        ArrayList<VoidContainer> containers = new ArrayList<>();
        containers.add(new VoidContainer(imageRes, message));
        VoidContainerAdapter adapter = new VoidContainerAdapter(containers);
        setupRecyclerView(adapter);
    }

    private void setupRecyclerView(RecyclerView.Adapter<?> adapter) {
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(adapter);
    }

    // ==================== Utility Methods ====================

    private long calculatePercentage(long startDate, long endDate, long nowDate) {
        if (endDate <= startDate) return 0;
        return (long) (((float) (nowDate - startDate) / (float) (endDate - startDate)) * 100);
    }

    private long getCurrentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    private long converterDate(String dateString) {
        if (TextUtils.isEmpty(dateString)) return 0;
        try {
            Date date = DATE_FORMAT.parse(dateString);
            return date != null ? date.getTime() / 1000 : 0;
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + dateString, e);
            return 0;
        }
    }

    // ==================== Broadcast Receiver Management ====================

    private void registerRefreshReceiver(String action, Runnable refreshAction) {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (action.equals(intent.getAction())) {
                    try {
                        showWaitingState();
                        refreshAction.run();
                    } catch (Exception e) {
                        Log.e(TAG, "Error in refresh receiver for " + action, e);
                    }
                }
            }
        };
        registerReceiverSafely(receiver, new IntentFilter(action));
    }

    private void registerReceiverSafely(BroadcastReceiver receiver, IntentFilter filter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(receiver, filter);
        }
        mRegisteredReceivers.add(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterAllReceivers();
    }

    private void unregisterAllReceivers() {
        for (BroadcastReceiver receiver : mRegisteredReceivers) {
            try {
                unregisterReceiver(receiver);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Receiver already unregistered", e);
            }
        }
        mRegisteredReceivers.clear();
    }

    // ==================== AsyncTask Classes ====================

    private static class NetworkTask<T> extends AsyncTask<String, Void, String> {
        private final WeakReference<SearchActivity> activityRef;
        private final String fileName;

        NetworkTask(SearchActivity activity, String fileName) {
            this.activityRef = new WeakReference<>(activity);
            this.fileName = fileName;
        }

        @Override
        protected String doInBackground(String... params) {
            SearchActivity activity = activityRef.get();
            if (activity == null || activity.mHttpClient == null) return null;

            try {
                MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber", params[0]);

                if (params.length > 1) {
                    bodyBuilder.addFormDataPart("idStruct", params[1]);
                }
                if (params.length > 2) {
                    bodyBuilder.addFormDataPart("idAuthor", params[2]);
                }

                RequestBody requestBody = bodyBuilder.build();
                Request request = new Request.Builder()
                        .url(Server.getIpServerAndroid(activity) + fileName)
                        .post(requestBody)
                        .build();

                try (Response response = activity.mHttpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        return response.body().string();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Network request failed for " + fileName, e);
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in network task", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String jsonData) {
            SearchActivity activity = activityRef.get();
            if (activity == null) return;

            if (jsonData != null && !"RAS".equals(jsonData)) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    activity.parseOnlineBooks(jsonArray);
                    activity.mOnlineBookAdapter = new OnlineBookAdapter(activity.mOnlineBooks);
                    activity.setupRecyclerView(activity.mOnlineBookAdapter);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parsing error", e);
                    activity.showNoConnectionState("BOOKS_SEARCH");
                }
            } else {
                activity.showNoConnectionState("BOOKS_SEARCH");
            }
        }
    }

    private void parseOnlineBooks(JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            mOnlineBooks.add(new OnlineBook(
                    obj.getString("idBook"),
                    obj.getString("blanket"),
                    obj.getString("bookTitle"),
                    obj.optString("nameStruct", "") +
                            (obj.has("categoryTitle") ? " : " + obj.getString("categoryTitle") : obj.getString("categoryTitle")),
                    obj.getString("isPhysic"),
                    obj.getString("electronic"),
                    obj.getString("isAudio"),
                    obj.optInt("numberLike", 0),
                    obj.optInt("numberView", obj.optInt("numberNoLike", 0))
            ));
        }
    }

    private void showNoConnectionState(String action) {
        ArrayList<Connection> list = new ArrayList<>();
        list.add(new Connection(getString(R.string.no_connection_available), action, false));
        NoConnectionAdapter adapter = new NoConnectionAdapter(list);
        setupRecyclerView(adapter);
    }

    private static class FabiolaBookTask extends AsyncTask<String, Void, String> {
        private final WeakReference<SearchActivity> activityRef;

        FabiolaBookTask(SearchActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(String... params) {
            SearchActivity activity = activityRef.get();
            if (activity == null) return null;

            try {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber", params[0])
                        .build();

                Request request = new Request.Builder()
                        .url(Server.getIpServerAndroid(activity) + "FabiolaBook.php")
                        .post(requestBody)
                        .build();

                try (Response response = activity.mHttpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        return response.body().string();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Fabiola book request failed", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String jsonData) {
            SearchActivity activity = activityRef.get();
            if (activity == null) return;

            if (jsonData != null && !"RAS".equals(jsonData)) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    activity.parseOnlineBooks(jsonArray);
                    activity.mFabiolaBookAdapter = new FabiolaBookAdapter(activity.mOnlineBooks);
                    activity.setupRecyclerView(activity.mFabiolaBookAdapter);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parsing error", e);
                    activity.showNoConnectionState("RANKING_FRAGMENT");
                }
            } else {
                activity.showNoConnectionState("RANKING_FRAGMENT");
            }
        }
    }

    private static class CategoryInTask extends AsyncTask<String, Void, String> {
        private final WeakReference<SearchActivity> activityRef;

        CategoryInTask(SearchActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(String... params) {
            SearchActivity activity = activityRef.get();
            if (activity == null) return null;

            try {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber", params[0])
                        .addFormDataPart("categoryTitle", params[1])
                        .build();

                Request request = new Request.Builder()
                        .url(Server.getIpServerAndroid(activity) + "CategoryIn.php")
                        .post(requestBody)
                        .build();

                try (Response response = activity.mHttpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        return response.body().string();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Category request failed", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String jsonData) {
            SearchActivity activity = activityRef.get();
            if (activity == null) return;

            if (jsonData != null && !"RAS".equals(jsonData)) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    activity.parseOnlineBooks(jsonArray);
                    activity.mOnlineBookAdapter = new OnlineBookAdapter(activity.mOnlineBooks);
                    activity.setupRecyclerView(activity.mOnlineBookAdapter);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parsing error", e);
                    activity.showNoConnectionState("CATEGORY_ACTIVITY");
                }
            } else {
                activity.showNoConnectionState("CATEGORY_ACTIVITY");
            }
        }
    }

    private static class StructureTask extends AsyncTask<String, Void, String> {
        private final WeakReference<SearchActivity> activityRef;
        private final String fileName;

        StructureTask(SearchActivity activity, String fileName) {
            this.activityRef = new WeakReference<>(activity);
            this.fileName = fileName;
        }

        @Override
        protected String doInBackground(String... params) {
            SearchActivity activity = activityRef.get();
            if (activity == null) return null;

            try {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idUser", params[0])
                        .build();

                Request request = new Request.Builder()
                        .url(Server.getIpServerAndroid(activity) + fileName)
                        .post(requestBody)
                        .build();

                try (Response response = activity.mHttpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        return response.body().string();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Structure request failed", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String jsonData) {
            SearchActivity activity = activityRef.get();
            if (activity == null) return;

            if (jsonData != null && !"RAS".equals(jsonData)) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    activity.parseStructures(jsonArray, true);
                    activity.setupRecyclerView(activity.mStructureAdapter);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parsing error", e);
                    activity.showNoConnectionState("STRUCT_SEARCH");
                }
            } else {
                activity.showNoConnectionState("STRUCT_SEARCH");
            }
        }
    }

    private static class StructureTask2 extends AsyncTask<String, Void, String> {
        private final WeakReference<SearchActivity> activityRef;
        private final String fileName;

        StructureTask2(SearchActivity activity, String fileName) {
            this.activityRef = new WeakReference<>(activity);
            this.fileName = fileName;
        }

        @Override
        protected String doInBackground(String... params) {
            SearchActivity activity = activityRef.get();
            if (activity == null) return null;

            try {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idUser", params[0])
                        .build();

                Request request = new Request.Builder()
                        .url(Server.getIpServerAndroid(activity) + fileName)
                        .post(requestBody)
                        .build();

                try (Response response = activity.mHttpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        return response.body().string();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Structure request failed", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String jsonData) {
            SearchActivity activity = activityRef.get();
            if (activity == null) return;

            if (jsonData != null && !"RAS".equals(jsonData)) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    activity.parseStructures(jsonArray, false);
                    activity.setupRecyclerView(activity.mStructureAdapter);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parsing error", e);
                }
            }
        }
    }

    private void parseStructures(JSONArray jsonArray, boolean isAdmin) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            String id = obj.getString("id");

            if (!isAdmin && isStructureExists(mStructures, id)) {
                continue;
            }

            mStructures.add(new Structure(
                    id,
                    obj.getString("logo"),
                    obj.getString("nameStruct"),
                    obj.getString("description"),
                    isAdmin,
                    obj.getString("banner"),
                    obj.getString("author"),
                    obj.getString("adhererNumber"),
                    obj.getString("bookNumber"),
                    obj.optString("isAdmin", "0")
            ));
        }
    }

    private boolean isStructureExists(ArrayList<Structure> structures, String id) {
        for (Structure structure : structures) {
            if (structure.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private static class AuthorTask extends AsyncTask<String, Void, String> {
        private final WeakReference<SearchActivity> activityRef;

        AuthorTask(SearchActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(String... params) {
            SearchActivity activity = activityRef.get();
            if (activity == null) return null;

            try {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idUser", params[0])
                        .build();

                Request request = new Request.Builder()
                        .url(Server.getIpServerAndroid(activity) + "Author.php")
                        .post(requestBody)
                        .build();

                try (Response response = activity.mHttpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        return response.body().string();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Author request failed", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String jsonData) {
            SearchActivity activity = activityRef.get();
            if (activity == null) return;

            if (jsonData != null && !"RAS".equals(jsonData)) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        activity.mAuthors.add(new Author(
                                obj.getString("idAuthor"),
                                obj.getString("name"),
                                obj.getString("firstName"),
                                obj.getString("profile"),
                                obj.getString("profession"),
                                obj.getString("call"),
                                obj.getString("email"),
                                obj.getString("whatsapp")
                        ));
                    }
                    activity.mAuthorVerticaleAdapter = new AuthorVerticaleAdapter(activity.mAuthors);
                    activity.setupRecyclerView(activity.mAuthorVerticaleAdapter);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parsing error", e);
                    activity.showNoConnectionState("AUTHOR_SEARCH");
                }
            } else {
                activity.showNoConnectionState("AUTHOR_SEARCH");
            }
        }
    }
}