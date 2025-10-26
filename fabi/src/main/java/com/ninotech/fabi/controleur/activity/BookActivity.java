package com.ninotech.fabi.controleur.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.NoConnectionAdapter;
import com.ninotech.fabi.controleur.adapter.SemiNoConnectionAdapter;
import com.ninotech.fabi.controleur.adapter.TalksAdapter;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.controleur.dialog.ReservationDialog;
import com.ninotech.fabi.controleur.dialog.SimpleOkDialog;
import com.ninotech.fabi.model.data.Author;
import com.ninotech.fabi.model.data.Category;
import com.ninotech.fabi.model.data.Chat;
import com.ninotech.fabi.model.data.Connection;
import com.ninotech.fabi.model.data.OnlineBook;
import com.ninotech.fabi.model.data.PasswordUtil;
import com.ninotech.fabi.model.data.Server;
import com.ninotech.fabi.model.data.Talks;
import com.ninotech.fabi.model.data.Tones;
import com.ninotech.fabi.model.service.AudioDownloadService;
import com.ninotech.fabi.model.service.PdfDownloadService;
import com.ninotech.fabi.model.table.AudioTable;
import com.ninotech.fabi.model.table.ElectronicTable;
import com.ninotech.fabi.model.table.Session;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BookActivity extends AppCompatActivity {

    private static final String TAG = "BookActivity";
    private static final String ACTION_BOOK = "BOOK_ACTIVITY";
    private static final String ACTION_FINISH_DOWNLOAD = "ACTION_FINISH_DOWNLOAD";
    private static final String RESPONSE_RAS = "RAS";

    // Views
    private NestedScrollView mNestedScrollView;
    private RecyclerView mCommentsRecyclerView;
    private RecyclerView mNoConnectionRecyclerView;
    private RelativeLayout mCommentRelativeLayout;
    private ImageView mBlanketImageView;
    private ImageView mLikeImageView;
    private ImageView mNoLikeImageView;
    private ImageView mSubscribeImageView;
    private ImageView mPlayerImageView;
    private ImageView mBackImageView;
    private TextView mTitleTextView;
    private TextView mCategoryTextView;
    private TextView mDescriptionTextView;
    private TextView mTimeNowTextView;
    private TextView mNumberLikeTextView;
    private TextView mNumberNoLikeTextView;
    private TextView mNumberSubscribeTextView;
    private TextView mCote;
    private TextView mNameAuthor;
    private TextView mNbrView;
    private TextView mAudioSizeTextView;
    private TextView mMaxTimeTextView;
    private TextView mPdfSizeTextView;
    private TextView mNbrPageTextView;
    private EditText mMessageTextView;
    private Button mReservationButton;
    private Button audioButton;
    private Button downloadPDFButton;
    private SeekBar mSeekBar;
    private LinearLayout mReservationLinearLayout;
    private LinearLayout mAudioLinearLayout;
    private LinearLayout mElectronicLinearLayout;
    private LinearLayout mAudioSizeLinearLayout;
    private LinearLayout mMaxTimeLinearLayout;
    private LinearLayout mPdfSizeLinearLayout;
    private LinearLayout mNbrPageLinearLayout;
    private ProgressBar downloadAudioProgressBar;
    private ProgressBar downloadPdfProgressBar;
    private ProgressBar mWaitPlayerProgressBar;

    // Data
    private final List<Talks> mTalksList = new ArrayList<>();
    private final List<Tones> mListTones = new ArrayList<>();
    private OnlineBook mOnlineBook;
    private Category mCategory;
    private Author mAuthor;
    private Tones mTones;
    private Session mSession;
    private String mSourcePdf;
    private String mNbrJour;

    // Utils
    private MediaPlayer mMediaPlayer;
    private Handler mHandler;
    private ElectronicTable mElectronicTable;
    private AudioTable mAudioTable;
    private ReservationDialog mReservationDialog;
    private TalksAdapter talksAdapter;
    private Talks mTalksSelect;
    private OkHttpClient mHttpClient;
    private BroadcastReceiver mFinishDownloadReceiver;
    private BroadcastReceiver mNoConnectionReceiver;

    // State
    private boolean isLike = false;
    private boolean isNoLike = false;
    private boolean isSubscribe = false;
    private Thread mMediaPlayerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        Objects.requireNonNull(getSupportActionBar()).hide();

        initializeComponents();
        initializeViews();
        setupRecyclerViews();
        setupClickListeners();
        registerBroadcastReceivers();
        loadBookData();
    }

    private void initializeComponents() {
        mSession = new Session(this);
        String bookId = getIntent().getStringExtra("intent_adapter_book_id");
        mOnlineBook = new OnlineBook(bookId);
        mElectronicTable = new ElectronicTable(this);
        mAudioTable = new AudioTable(this);
        mReservationDialog = new ReservationDialog(this);
        mHandler = new Handler();
        mMediaPlayer = new MediaPlayer();
        mHttpClient = new OkHttpClient();
    }

    private void initializeViews() {
        mNestedScrollView = findViewById(R.id.nested_scroll_view_activity_book);
        mCommentsRecyclerView = findViewById(R.id.recycler_view_activity_book_Comments);
        mNoConnectionRecyclerView = findViewById(R.id.recycler_view_activity_book_wait);
        mCommentRelativeLayout = findViewById(R.id.relative_layout_activity_book_comment);
        mBlanketImageView = findViewById(R.id.image_view_adapter_book_simple_cover);
        mTitleTextView = findViewById(R.id.text_view_adapter_book_simple_title);
        mCategoryTextView = findViewById(R.id.text_view_adapter_description_category);
        mDescriptionTextView = findViewById(R.id.text_view_activity_book_description);
        mTimeNowTextView = findViewById(R.id.text_view_activity_book_time_now);
        mReservationButton = findViewById(R.id.button_activity_book_reservation);
        audioButton = findViewById(R.id.button_activity_book_audio);
        downloadPDFButton = findViewById(R.id.button_activity_book_download_pdf);
        mMessageTextView = findViewById(R.id.text_view_activity_book_message);
        mNumberLikeTextView = findViewById(R.id.text_view_activity_book_number_like);
        mNumberNoLikeTextView = findViewById(R.id.text_view_activity_book_number_no_like);
        mNumberSubscribeTextView = findViewById(R.id.text_view_activity_book_number_subscribe);
        mCote = findViewById(R.id.text_view_adapter_book_simple_id_book);
        mLikeImageView = findViewById(R.id.image_view_activity_book_like);
        mNoLikeImageView = findViewById(R.id.image_view_activity_book_no_like);
        mPlayerImageView = findViewById(R.id.image_view_activity_book_player);
        mSubscribeImageView = findViewById(R.id.image_view_activity_book_subscribe);
        mSeekBar = findViewById(R.id.seekbar_activity_book);
        mReservationLinearLayout = findViewById(R.id.linear_layout_activity_book_reservation);
        mAudioLinearLayout = findViewById(R.id.linear_layout_activity_book_audio);
        mElectronicLinearLayout = findViewById(R.id.linear_layout_activity_book_electronic);
        mBackImageView = findViewById(R.id.image_view_toolbar_book);
        mNameAuthor = findViewById(R.id.text_view_adapter_book_simple_author_name);
        downloadAudioProgressBar = findViewById(R.id.progress_bar_download_audio);
        downloadPdfProgressBar = findViewById(R.id.progress_bar_download_pdf);
        mWaitPlayerProgressBar = findViewById(R.id.progress_bar_activity_book_wait_player);
        mAudioSizeLinearLayout = findViewById(R.id.linear_layout_activity_book_audio_size);
        mAudioSizeTextView = findViewById(R.id.text_view_activity_book_audio_size);
        mMaxTimeLinearLayout = findViewById(R.id.linear_layout_activity_book_maxTime);
        mMaxTimeTextView = findViewById(R.id.text_view_activity_book_audio_max_time);
        mPdfSizeLinearLayout = findViewById(R.id.linear_layout_activity_book_pdf_size);
        mPdfSizeTextView = findViewById(R.id.text_view_activity_book_pdf_size);
        mNbrPageLinearLayout = findViewById(R.id.linear_layout_activity_book_nbr_page);
        mNbrPageTextView = findViewById(R.id.text_view_activity_book_pdf_max_page);
        mNbrView = findViewById(R.id.text_view_activity_book_view);

        mPlayerImageView.setVisibility(View.GONE);
        audioButton.setEnabled(false);
    }

    private void setupRecyclerViews() {
        List<Connection> waitList = new ArrayList<>();
        waitList.add(new Connection(getString(R.string.wait), null, true));

        NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(waitList);
        mNoConnectionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mNoConnectionRecyclerView.setAdapter(noConnectionAdapter);

        SemiNoConnectionAdapter semiNoConnectionAdapter = new SemiNoConnectionAdapter(waitList);
        mCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCommentsRecyclerView.setAdapter(semiNoConnectionAdapter);
    }

    private void setupClickListeners() {
        mBackImageView.setOnClickListener(v -> onBackPressed());

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mMediaPlayer != null) {
                    mMediaPlayer.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        mReservationButton.setOnClickListener(v -> handleReservationClick());
        downloadPDFButton.setOnClickListener(v -> handlePdfDownload());
        audioButton.setOnClickListener(v -> handleAudioClick());

        mPlayerImageView.setOnClickListener(v -> toggleMediaPlayer());
        findViewById(R.id.image_view_activity_book_stop).setOnClickListener(v -> stopMediaPlayer());

        findViewById(R.id.linear_layout_activity_book_like).setOnClickListener(v -> handleLike());
        findViewById(R.id.linear_layout_activiry_book_nolike).setOnClickListener(v -> handleNoLike());
        findViewById(R.id.linear_layout_activity_book_subscribe).setOnClickListener(v -> handleSubscribe());
        findViewById(R.id.image_view_activity_book_add_comments).setOnClickListener(v -> sendComment());
    }

    private void registerBroadcastReceivers() {
        mFinishDownloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_FINISH_DOWNLOAD.equals(intent.getAction())) {
                    handleDownloadFinished(intent);
                }
            }
        };

        mNoConnectionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_BOOK.equals(intent.getAction())) {
                    showLoadingState();
                    loadBookData();
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(mFinishDownloadReceiver,
                    new IntentFilter(ACTION_FINISH_DOWNLOAD), Context.RECEIVER_EXPORTED);
            registerReceiver(mNoConnectionReceiver,
                    new IntentFilter(ACTION_BOOK), Context.RECEIVER_EXPORTED);
        }
    }

    private void handleDownloadFinished(Intent intent) {
        String format = intent.getStringExtra("format");
        if ("audio".equals(format)) {
            audioButton.setText("Lire");
            downloadAudioProgressBar.setVisibility(View.GONE);
        } else if ("pdf".equals(format)) {
            mSourcePdf = mElectronicTable.getPdf(mOnlineBook.getId());
            downloadPDFButton.setText("Ouvrir");
            downloadPdfProgressBar.setVisibility(View.GONE);
        }
        Toast.makeText(this, mOnlineBook.getTitle() + " Téléchargé avec succès", Toast.LENGTH_SHORT).show();
    }

    private void showLoadingState() {
        mNestedScrollView.setVisibility(View.GONE);
        mNoConnectionRecyclerView.setVisibility(View.VISIBLE);

        List<Connection> list = new ArrayList<>();
        list.add(new Connection(getString(R.string.wait), null, true));
        NoConnectionAdapter adapter = new NoConnectionAdapter(list);
        mNoConnectionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mNoConnectionRecyclerView.setAdapter(adapter);
    }

    private void loadBookData() {
        String baseUrl = Server.getIpServerAndroid(this);
        String idNumber = mSession.getIdNumber();
        String bookId = mOnlineBook.getId();

        new RecoveryBook().execute(baseUrl + "Book.php", idNumber, bookId);
        new IsReservationSyn().execute(baseUrl + "IsReservation.php", idNumber, bookId);
        new InsertViewSyn().execute(baseUrl + "InsertView.php", idNumber, bookId);
        new IsSubscribeBookSyn().execute(baseUrl + "IsSubscribeBook.php", idNumber, bookId);
        new IsLikeSyn().execute(baseUrl + "IsLike.php", idNumber, bookId);
        new IsNoLikeSyn().execute(baseUrl + "IsNoLike.php", idNumber, bookId);
        new ReceiveComments().execute(baseUrl + "ReceiveComments.php", idNumber, bookId);
        new RecoveryTones().execute(baseUrl + "Tones.php");
    }

    // ==================== Click Handlers ====================

    private void handleReservationClick() {
        String buttonText = mReservationButton.getText().toString();
        if (buttonText.equals(getString(R.string.reservation_book))) {
            showReservationDialog();
        } else if (buttonText.equals(getString(R.string.cancel_reservation))) {
            new CancelReservationSyn().execute(
                    Server.getIpServerAndroid(this) + "CancelReservation.php",
                    mSession.getIdNumber(),
                    mOnlineBook.getId()
            );
        }
    }

    private void handlePdfDownload() {
        String buttonText = downloadPDFButton.getText().toString();

        if ("Format PDF".equals(buttonText)) {
            downloadPDFButton.setText("En Cours...");
            Toast.makeText(this, "Téléchargement démarré", Toast.LENGTH_SHORT).show();
            startPdfDownloadService();
        } else if ("Ouvrir".equals(buttonText)) {
            openPdfDocument();
        }
    }

    private void startPdfDownloadService() {
        Intent intent = new Intent(this, PdfDownloadService.class);
        intent.putExtra("fileNames", new String[]{
                mOnlineBook.getCover(),
                mOnlineBook.getElectronic(),
                mCategory.getCover(),
                mAuthor.getProfile(),
                mSession.getIdNumber(),
                mOnlineBook.getId(),
                mOnlineBook.getDescription(),
                mOnlineBook.getAuthor(),
                mOnlineBook.getCategory(),
                mOnlineBook.getTitle()
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }
    }

    private void openPdfDocument() {
        Intent intent = new Intent(getApplicationContext(), PdfBoxViewerActivity.class);
        intent.putExtra("PDF_PATH", mSourcePdf);
        intent.putExtra("PDF_TITLE", mOnlineBook.getTitle());
        startActivity(intent);
    }

    private void handleAudioClick() {
        String buttonText = audioButton.getText().toString();

        if ("Format Audio".equals(buttonText)) {
            audioButton.setText("En Cours...");
            downloadAudioProgressBar.setVisibility(View.GONE);
            startAudioDownloadService();
        } else if ("Lire".equals(buttonText)) {
            navigateToAudioPlayer();
        }
    }

    private void startAudioDownloadService() {
        Intent intent = new Intent(this, AudioDownloadService.class);
        intent.putExtra("fileNames", new String[]{
                mOnlineBook.getCover(),
                mOnlineBook.getElectronic(),
                mCategory.getCover(),
                mAuthor.getProfile(),
                mTones.getAudio(),
                mSession.getIdNumber(),
                mOnlineBook.getId(),
                mOnlineBook.getDescription(),
                mOnlineBook.getAuthor(),
                mOnlineBook.getCategory(),
                mOnlineBook.getTitle(),
                mTones.getDuration()
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }
    }

    private void navigateToAudioPlayer() {
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.putExtra("key_adapter_audio_book_id", mOnlineBook.getId());
        intent.putExtra("list_audio_source", "all");
        startActivity(intent);
    }

    private void toggleMediaPlayer() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mPlayerImageView.setImageResource(R.drawable.vector_black3_pause);
                mMediaPlayer.pause();
            } else {
                mPlayerImageView.setImageResource(R.drawable.vector_black3_play);
                mMediaPlayer.start();
            }
        }
    }

    private void stopMediaPlayer() {
        if (mMediaPlayer != null) {
            mPlayerImageView.setImageResource(R.drawable.vector_black3_pause);
            mSeekBar.setProgress(0);
            mMediaPlayer.pause();
            mTimeNowTextView.setText(R.string.default_time);
        }
    }

    private void handleLike() {
        if (isLike) {
            mOnlineBook.disLike();
            mLikeImageView.setImageResource(R.drawable.vector_black3_off_like);
            isLike = false;
        } else {
            mOnlineBook.like();
            mLikeImageView.setImageResource(R.drawable.vector_purple2_200_on_like);

            if (isNoLike) {
                mOnlineBook.disNoLike();
                mNoLikeImageView.setImageResource(R.drawable.vector_black3_off_no_like);
                isNoLike = false;
                mNumberNoLikeTextView.setText(String.valueOf(mOnlineBook.getNumberNoLikes()));
            }
            isLike = true;
        }

        mNumberLikeTextView.setText(String.valueOf(mOnlineBook.getNumberLikes()));
        new InsertLikeSyn().execute(
                Server.getIpServerAndroid(this) + "InsertLike.php",
                mSession.getIdNumber(),
                mOnlineBook.getId()
        );
    }

    private void handleNoLike() {
        if (isNoLike) {
            mOnlineBook.disNoLike();
            mNoLikeImageView.setImageResource(R.drawable.vector_black3_off_no_like);
            isNoLike = false;
        } else {
            mOnlineBook.noLike();
            mNoLikeImageView.setImageResource(R.drawable.vector_rouge_on_nolike);

            if (isLike) {
                mOnlineBook.disLike();
                mLikeImageView.setImageResource(R.drawable.vector_black3_off_like);
                isLike = false;
                mNumberLikeTextView.setText(String.valueOf(mOnlineBook.getNumberLikes()));
            }
            isNoLike = true;
        }

        mNumberNoLikeTextView.setText(String.valueOf(mOnlineBook.getNumberNoLikes()));
        new InsertNoLikeSyn().execute(
                Server.getIpServerAndroid(this) + "InsertNoLike.php",
                mSession.getIdNumber(),
                mOnlineBook.getId()
        );
    }

    private void handleSubscribe() {
        if (isSubscribe) {
            mOnlineBook.desSubscribe();
            mSubscribeImageView.setImageResource(R.drawable.vector_black3_off_subscribe);
            isSubscribe = false;
        } else {
            mOnlineBook.subscribe();
            mSubscribeImageView.setImageResource(R.drawable.vector_purple2_200_suscribe);
            isSubscribe = true;
        }

        mNumberSubscribeTextView.setText(String.valueOf(mOnlineBook.getNumberSubscribe()));
        new InsertSubscribeBookSyn().execute(
                Server.getIpServerAndroid(this) + "InsertSubscribeBook.php",
                mSession.getIdNumber(),
                mOnlineBook.getId()
        );
    }

    private void sendComment() {
        String message = mMessageTextView.getText().toString();
        if (!"null".equals(message) && !message.isEmpty()) {
            Chat chat = new Chat(mSession.getIdNumber(), this, message);
            mMessageTextView.setText("");

            mTalksList.add(new Talks(
                    mSession.getIdNumber() + ".png",
                    chat.getUserName(),
                    chat.getMessage()
            ));

            talksAdapter = new TalksAdapter(mTalksList);
            mCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mCommentsRecyclerView.setAdapter(talksAdapter);
            mCommentsRecyclerView.smoothScrollToPosition(talksAdapter.getItemCount() - 1);

            new SendComments().execute(
                    Server.getIpServerAndroid(this) + "SendComments.php",
                    mSession.getIdNumber(),
                    mOnlineBook.getId(),
                    chat.getMessage()
            );
        }
    }

    // ==================== AsyncTask Classes ====================

    private class RecoveryBook extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0],
                    new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("idNumber", params[1])
                            .addFormDataPart("idBook", params[2])
                            .build());
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null) {
                processBookData(jsonData);
            } else {
                showNoConnectionError();
            }
        }

        private void processBookData(String jsonData) {
            mNoConnectionRecyclerView.setVisibility(View.GONE);
            mNestedScrollView.setVisibility(View.VISIBLE);

            try {
                JSONObject obj = new JSONObject(jsonData);
                updateBookDetails(obj);
                loadBookCoverImage();
                updateStatistics();
                configureBookFormats();
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing book data", e);
            }
        }

        private void updateBookDetails(JSONObject obj) throws JSONException {
            mOnlineBook.setCover(obj.getString("bookBlanket"));
            mOnlineBook.setTitle(obj.getString("bookTitle"));
            mOnlineBook.setIsPhysic(obj.getString("isPhysic"));
            mOnlineBook.setIsAudio(obj.getString("isAudio"));
            mOnlineBook.setElectronic(obj.getString("electronic"));
            mOnlineBook.setDescription(obj.getString("description"));
            mOnlineBook.setCategory(obj.getString("categoryTitle"));
            mOnlineBook.setIsAvailable(obj.getString("available"));
            mOnlineBook.setSize(obj.getString("size"));
            mOnlineBook.setNbrPage(obj.getString("nbrPage"));
            mOnlineBook.setNumberLikes(Integer.parseInt(obj.getString("numberLike")));
            mOnlineBook.setNumberNoLikes(Integer.parseInt(obj.getString("numberNoLike")));
            mOnlineBook.setNumberSubscribe(Integer.parseInt(obj.getString("numberSubscribe")));
            mOnlineBook.setNumberView(Integer.parseInt(obj.getString("numberView")));
            mOnlineBook.setAuthor(obj.getString("firstName") + " " + obj.getString("name"));

            mCategory = new Category(
                    obj.getString("categoryBlanket"),
                    obj.getString("categoryTitle")
            );

            mAuthor = new Author(
                    obj.getString("idAuthor"),
                    obj.getString("name"),
                    obj.getString("firstName"),
                    obj.getString("profile"),
                    obj.getString("profession"),
                    obj.getString("call"),
                    obj.getString("email"),
                    obj.getString("whatsapp")
            );

            mTitleTextView.setText(mOnlineBook.getTitle());
            mNameAuthor.setText("De " + obj.getString("name") + " " + obj.getString("firstName"));
            mCote.setText("Cote : " + mOnlineBook.getId());
            mCategoryTextView.setText("Catégorie : " + mOnlineBook.getCategory());
            mDescriptionTextView.setText(mOnlineBook.getDescription());
        }

        private void loadBookCoverImage() {
            Picasso.get()
                    .load(Server.getIpServer(BookActivity.this) +
                            "ressources/cover/" + mOnlineBook.getCover())
                    .placeholder(R.drawable.img_wait_cover_book)
                    .error(R.drawable.img_wait_cover_book)
                    .transform(new RoundedTransformation(15, 4))
                    .resize(270, 404)
                    .into(mBlanketImageView);
        }

        private void updateStatistics() {
            mNumberLikeTextView.setText(String.valueOf(mOnlineBook.getNumberLikes()));
            mNumberNoLikeTextView.setText(String.valueOf(mOnlineBook.getNumberNoLikes()));
            mNumberSubscribeTextView.setText(String.valueOf(mOnlineBook.getNumberSubscribe()));
            mNbrView.setText(String.valueOf(mOnlineBook.getNumberView()));
        }

        private void configureBookFormats() {
            configurePhysicalFormat();
            configureAudioFormat();
            configureElectronicFormat();
        }

        private void configurePhysicalFormat() {
            if ("1".equals(mOnlineBook.getIsPhysic())) {
                mReservationLinearLayout.setVisibility(View.VISIBLE);
                if ("0".equals(mOnlineBook.getIsAvailable())) {
                    mReservationButton.setText("En cours de consultation");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mReservationButton.setBackgroundTintList(
                                ColorStateList.valueOf(ContextCompat.getColor(BookActivity.this, R.color.whiteSombre)));
                        mReservationButton.setEnabled(false);
                    }
                }
            }
        }

        private void configureAudioFormat() {
            if ("1".equals(mOnlineBook.getIsAudio())) {
                mAudioLinearLayout.setVisibility(View.VISIBLE);
                if (mAudioTable.isExist(mSession.getIdNumber(), mOnlineBook.getId())) {
                    audioButton.setText("Lire");
                }
            }
        }

        private void configureElectronicFormat() {
            if (!"null".equals(mOnlineBook.getElectronic())) {
                mElectronicLinearLayout.setVisibility(View.VISIBLE);
                mSourcePdf = mElectronicTable.isExist(mSession.getIdNumber(), mOnlineBook.getId());

                if (!"false".equals(mSourcePdf)) {
                    downloadPDFButton.setText("Ouvrir");
                }

                if (!"null".equals(mOnlineBook.getSize())) {
                    mPdfSizeTextView.setText(mOnlineBook.getSize());
                    mPdfSizeLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    downloadPDFButton.setEnabled(false);
                    downloadPDFButton.setText("Bientôt");
                }

                if (!"null".equals(mOnlineBook.getNbrPage())) {
                    mNbrPageTextView.setText(mOnlineBook.getNbrPage());
                    mNbrPageLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private class ReceiveComments extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0],
                    new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("idNumber", params[1])
                            .addFormDataPart("idBook", params[2])
                            .build());
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null) {
                processComments(jsonData);
            } else {
                showCommentLoadError();
            }
        }

        private void processComments(String jsonData) {
            if (!RESPONSE_RAS.equals(jsonData)) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    mTalksList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String fullName = obj.getString("name") + " " + obj.getString("firstName");
                        mTalksList.add(new Talks(
                                obj.getString("idUser") + ".png",
                                fullName,
                                obj.getString("message")
                        ));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing comments", e);
                }
            }

            talksAdapter = new TalksAdapter(mTalksList);
            mCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(BookActivity.this));
            registerForContextMenu(mCommentsRecyclerView);
            mCommentsRecyclerView.setAdapter(talksAdapter);
            mCommentRelativeLayout.setVisibility(View.VISIBLE);
        }

        private void showCommentLoadError() {
            List<Connection> list = new ArrayList<>();
            list.add(new Connection(getString(R.string.no_connection_available), ACTION_BOOK, false));
            SemiNoConnectionAdapter adapter = new SemiNoConnectionAdapter(list);
            mCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(BookActivity.this));
            mCommentsRecyclerView.setAdapter(adapter);
        }
    }

    private class RecoveryTones extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0],
                    new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("idNumber", mSession.getIdNumber())
                            .addFormDataPart("idBook", mOnlineBook.getId())
                            .build());
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null && !RESPONSE_RAS.equals(jsonData)) {
                processTones(jsonData);
                setupMediaPlayer();
            }
        }

        private void processTones(String jsonData) {
            try {
                JSONArray jsonArray = new JSONArray(jsonData);
                mListTones.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    mListTones.add(new Tones(
                            i + 1,
                            obj.getString("audio"),
                            obj.getString("title"),
                            0,
                            false
                    ));

                    if (i == 0) {
                        mTones = new Tones(
                                0,
                                obj.getString("audio"),
                                obj.getString("size"),
                                obj.getString("maxTime")
                        );
                    }
                }

                displayAudioInfo();
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing tones", e);
            }
        }

        private void displayAudioInfo() {
            if (!"null".equals(mTones.getSize())) {
                mAudioSizeTextView.setText(mTones.getSize());
                mAudioSizeLinearLayout.setVisibility(View.VISIBLE);
            }

            if (!"null".equals(mTones.getDuration())) {
                audioButton.setEnabled(true);
                mMaxTimeTextView.setText(mTones.getDuration());
                mMaxTimeLinearLayout.setVisibility(View.VISIBLE);
            } else {
                audioButton.setEnabled(false);
                audioButton.setText("Bientôt");
            }
        }

        private void setupMediaPlayer() {
            String url = Server.getIpServer(BookActivity.this) +
                    "ressources/audio/" + mTones.getAudio();

            try {
                mMediaPlayer.setDataSource(url);
                mMediaPlayer.prepare();
                mSeekBar.setMax(mMediaPlayer.getDuration());
                mTones.setDuration(convertDurationToString(mMediaPlayer.getDuration()));
                mWaitPlayerProgressBar.setVisibility(View.GONE);
                mPlayerImageView.setVisibility(View.VISIBLE);

                startMediaPlayerThread();
            } catch (IOException e) {
                Log.e(TAG, "Error setting up media player", e);
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in media player setup", e);
            }
        }

        private void startMediaPlayerThread() {
            mMediaPlayerThread = new Thread(() -> {
                while (mMediaPlayer != null && !Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    mHandler.post(() -> {
                        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                            int currentTime = mMediaPlayer.getCurrentPosition();
                            mSeekBar.setProgress(currentTime);
                            mTimeNowTextView.setText(convertDurationToString(currentTime));
                        }
                    });
                }
            });
            mMediaPlayerThread.start();
        }
    }

    // ==================== Simple AsyncTasks ====================

    private class InsertLikeSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0], createIdBookRequestBody(params[1], params[2]));
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null && "true".equals(jsonData)) {
                Log.d(TAG, "Like inserted successfully");
            }
        }
    }

    private class InsertNoLikeSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0], createIdBookRequestBody(params[1], params[2]));
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null && "true".equals(jsonData)) {
                Log.d(TAG, "NoLike inserted successfully");
            }
        }
    }

    private class InsertSubscribeBookSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0], createIdBookRequestBody(params[1], params[2]));
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null && "true".equals(jsonData)) {
                Log.d(TAG, "Subscribe inserted successfully");
            }
        }
    }

    private class InsertViewSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0], createIdBookRequestBody(params[1], params[2]));
        }

        @Override
        protected void onPostExecute(String jsonData) {
            // View count updated
        }
    }

    private class IsLikeSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0], createIdBookRequestBody(params[1], params[2]));
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null && !RESPONSE_RAS.equals(jsonData)) {
                if (jsonData.equals(mSession.getIdNumber())) {
                    isLike = true;
                    mLikeImageView.setImageResource(R.drawable.vector_purple2_200_on_like);
                } else {
                    mLikeImageView.setImageResource(R.drawable.vector_black3_off_like);
                }
            }
        }
    }

    private class IsNoLikeSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0], createIdBookRequestBody(params[1], params[2]));
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null && !RESPONSE_RAS.equals(jsonData)) {
                if (jsonData.equals(mSession.getIdNumber())) {
                    isNoLike = true;
                    mNoLikeImageView.setImageResource(R.drawable.vector_rouge_on_nolike);
                } else {
                    mNoLikeImageView.setImageResource(R.drawable.vector_black3_off_no_like);
                }
            }
        }
    }

    private class IsSubscribeBookSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0], createIdBookRequestBody(params[1], params[2]));
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null && !RESPONSE_RAS.equals(jsonData)) {
                if (jsonData.equals(mSession.getIdNumber())) {
                    isSubscribe = true;
                    mSubscribeImageView.setImageResource(R.drawable.vector_purple2_200_suscribe);
                } else {
                    mSubscribeImageView.setImageResource(R.drawable.vector_black3_off_subscribe);
                }
            }
        }
    }

    private class IsReservationSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0], createIdBookRequestBody(params[1], params[2]));
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null && !"ras".equals(jsonData)) {
                processReservationStatus(jsonData);
            }
        }

        private void processReservationStatus(String jsonData) {
            try {
                JSONObject obj = new JSONObject(jsonData);
                String state = obj.getString("state");
                String treat = obj.getString("treat");

                if ("1".equals(state) && "1".equals(treat)) {
                    mReservationButton.setText(R.string.cancel_reservation);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mReservationButton.setBackgroundTintList(
                                ColorStateList.valueOf(ContextCompat.getColor(BookActivity.this, R.color.rouge)));
                    }
                } else if ("2".equals(state) && "1".equals(treat)) {
                    mReservationButton.setText("En cours de consultation");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mReservationButton.setBackgroundTintList(
                                ColorStateList.valueOf(ContextCompat.getColor(BookActivity.this, R.color.whiteSombre)));
                        mReservationButton.setEnabled(false);
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing reservation status", e);
            }
        }
    }

    private class CancelReservationSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0], createIdBookRequestBody(params[1], params[2]));
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null && !RESPONSE_RAS.equals(jsonData) && "true".equals(jsonData)) {
                mReservationButton.setText(R.string.reservation_book);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mReservationButton.setBackgroundTintList(
                            ContextCompat.getColorStateList(BookActivity.this, R.color.black3));
                }
            }
        }
    }

    private class Reservation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0],
                    new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("idNumber", params[1])
                            .addFormDataPart("idBook", params[2])
                            .addFormDataPart("numberOfDay", params[3])
                            .build());
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null && "true".equals(jsonData)) {
                mReservationDialog.cancel();
                showSuccessReservationDialog(
                        "Merci d'avoir réservé \"" + mTitleTextView.getText().toString() +
                                "\" sur fabi; nous traitons votre demande et vous confirmerons la disponibilité bientôt."
                );
                mReservationButton.setText(R.string.cancel_reservation);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mReservationButton.setBackgroundTintList(
                            ContextCompat.getColorStateList(BookActivity.this, R.color.rouge));
                }
            }
        }
    }

    private class SendComments extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0],
                    new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("idNumber", params[1])
                            .addFormDataPart("idBook", params[2])
                            .addFormDataPart("message", params[3])
                            .build());
        }

        @Override
        protected void onPostExecute(String jsonData) {
            // Comment sent
        }
    }

    // ==================== Dialogs ====================

    private void showReservationDialog() {
        Spinner timeLimitSpinner = mReservationDialog.findViewById(R.id.spinner_dialog_reservation_time_limit);
        CheckBox localConsultationCheckBox = mReservationDialog.findViewById(R.id.check_box_dialog_reservation_local_consultation);
        Button sendButton = mReservationDialog.findViewById(R.id.button_dialog_reservation_send);
        EditText passwordEditText = mReservationDialog.findViewById(R.id.edit_text_dialog_reservation_password);
        TextView errorTextView = mReservationDialog.findViewById(R.id.text_view_dialog_reservation_error);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.delait_reservation, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeLimitSpinner.setAdapter(adapter);

        sendButton.setBackground(getDrawable(R.drawable.form_purple_200_radius_10dp));

        localConsultationCheckBox.setOnClickListener(v ->
                timeLimitSpinner.setEnabled(!localConsultationCheckBox.isChecked()));

        sendButton.setOnClickListener(v ->
                handleReservationSubmit(passwordEditText, errorTextView, timeLimitSpinner));

        mReservationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mReservationDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        mReservationDialog.build();
    }

    private void handleReservationSubmit(EditText passwordEditText, TextView errorTextView, Spinner timeLimitSpinner) {
        String password = passwordEditText.getText().toString();

        if (password.isEmpty()) {
            errorTextView.setText(R.string.edit_text_hint_password);
            passwordEditText.setBackground(getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
        } else if (!PasswordUtil.hashPassword(password).equals(mSession.getPassword())) {
            errorTextView.setText(R.string.incorrect_password);
            passwordEditText.setBackground(getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
        } else {
            mNbrJour = timeLimitSpinner.isEnabled()
                    ? String.valueOf(timeLimitSpinner.getSelectedItemPosition() + 1)
                    : String.valueOf(-1);

            new Reservation().execute(
                    Server.getIpServerAndroid(this) + "Reservation.php",
                    mSession.getIdNumber(),
                    mOnlineBook.getId(),
                    mNbrJour
            );
        }
    }

    private void showSuccessReservationDialog(String message) {
        SimpleOkDialog dialog = new SimpleOkDialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        TextView messageTextView = dialog.findViewById(R.id.text_view_dialog_simple_ok_message);
        TextView okTextView = dialog.findViewById(R.id.text_view_dialog_simple_ok);

        messageTextView.setText(message);
        okTextView.setOnClickListener(v -> dialog.cancel());

        dialog.build();
    }

    // ==================== Context Menu ====================

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_item, menu);
        mTalksSelect = talksAdapter.getItem(talksAdapter.getPosition());
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_delete) {
            talksAdapter.remove(talksAdapter.getPosition());
            return true;
        }
        return super.onContextItemSelected(item);
    }

    // ==================== Helper Methods ====================

    private String executePostRequest(String url, RequestBody requestBody) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            try (Response response = mHttpClient.newCall(request).execute()) {
                if (response.body() != null) {
                    return response.body().string();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage(), e);
        }
        return null;
    }

    private RequestBody createIdBookRequestBody(String idNumber, String idBook) {
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("idNumber", idNumber)
                .addFormDataPart("idBook", idBook)
                .build();
    }

    private void showNoConnectionError() {
        mNestedScrollView.setVisibility(View.GONE);
        mNoConnectionRecyclerView.setVisibility(View.VISIBLE);

        List<Connection> list = new ArrayList<>();
        list.add(new Connection(getString(R.string.no_connection_available), ACTION_BOOK, false));
        NoConnectionAdapter adapter = new NoConnectionAdapter(list);
        mNoConnectionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mNoConnectionRecyclerView.setAdapter(adapter);
    }

    private String convertDurationToString(int duration) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up media player
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        // Stop media player thread
        if (mMediaPlayerThread != null && mMediaPlayerThread.isAlive()) {
            mMediaPlayerThread.interrupt();
        }

        // Unregister receivers
        try {
            if (mFinishDownloadReceiver != null) {
                unregisterReceiver(mFinishDownloadReceiver);
            }
            if (mNoConnectionReceiver != null) {
                unregisterReceiver(mNoConnectionReceiver);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receivers", e);
        }
    }
}