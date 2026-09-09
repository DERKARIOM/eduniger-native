package com.ninotech.eduniger.controleur.activity;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.adapter.SemiNoConnectionAdapter;
import com.ninotech.eduniger.controleur.adapter.TalksAdapter;
import com.ninotech.eduniger.controleur.animation.RoundedTransformation;
import com.ninotech.eduniger.controleur.dialog.ReservationDialog;
import com.ninotech.eduniger.controleur.dialog.SimpleOkDialog;
import com.ninotech.eduniger.model.data.Author;
import com.ninotech.eduniger.model.data.Category;
import com.ninotech.eduniger.model.data.Chat;
import com.ninotech.eduniger.model.data.Connection;
import com.ninotech.eduniger.model.data.OnlineBook;
import com.ninotech.eduniger.model.data.PasswordUtil;
import com.ninotech.eduniger.model.data.Server;
import com.ninotech.eduniger.model.data.Talks;
import com.ninotech.eduniger.model.data.Tones;
import com.ninotech.eduniger.model.service.AudioDownloadService;
import com.ninotech.eduniger.model.service.PdfDownloadService;
import com.ninotech.eduniger.model.table.AudioTable;
import com.ninotech.eduniger.model.table.ElectronicTable;
import com.ninotech.eduniger.model.table.Session;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import com.ninotech.eduniger.model.net.ApiClient;
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
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mSkeletonLoadingContainer;
    private View mNoConnectionContainer;
    private RecyclerView mCommentsRecyclerView;
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
   // private ProgressBar downloadAudioProgressBar;
    private LinearLayout mAudioDownloadProgressContainer;
    private ProgressBar  mAudioDownloadProgressBar;
    private TextView     mAudioDownloadPercentText;
    private BroadcastReceiver mAudioProgressReceiver;
    private LinearLayout mPdfDownloadProgressContainer;
    private ProgressBar  mPdfDownloadProgressBar;
    private TextView     mPdfDownloadPercentText;
    private BroadcastReceiver mPdfProgressReceiver;
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
    private ValueAnimator mShimmerAnimator;
    private ValueAnimator mArrowAnimator;

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
        setupSwipeRefresh();
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
        mHttpClient = ApiClient.getInstance(this);
    }

    private void initializeViews() {
        mAudioDownloadProgressContainer = findViewById(R.id.audio_download_progress_container);
        mAudioDownloadProgressBar       = findViewById(R.id.audio_download_progress_bar);
        mAudioDownloadPercentText       = findViewById(R.id.audio_download_percent_text);
        mAudioDownloadPercentText       = findViewById(R.id.audio_download_percent_text);
        mNestedScrollView         = findViewById(R.id.nested_scroll_view_activity_book);
        mSwipeRefreshLayout       = findViewById(R.id.swipe_refresh_book);
        mSkeletonLoadingContainer = findViewById(R.id.skeleton_loading_container);
        mNoConnectionContainer    = findViewById(R.id.no_connection_container);
        mCommentsRecyclerView     = findViewById(R.id.recycler_view_activity_book_Comments);
        mCommentRelativeLayout    = findViewById(R.id.relative_layout_activity_book_comment);
        mBlanketImageView         = findViewById(R.id.image_view_adapter_book_simple_cover);
        mTitleTextView            = findViewById(R.id.text_view_adapter_book_simple_title);
        mCategoryTextView         = findViewById(R.id.text_view_adapter_description_category);
        mDescriptionTextView      = findViewById(R.id.text_view_activity_book_description);
        mTimeNowTextView          = findViewById(R.id.text_view_activity_book_time_now);
        mReservationButton        = findViewById(R.id.button_activity_book_reservation);
        audioButton               = findViewById(R.id.button_activity_book_audio);
        downloadPDFButton         = findViewById(R.id.button_activity_book_download_pdf);
        mMessageTextView          = findViewById(R.id.text_view_activity_book_message);
        mNumberLikeTextView       = findViewById(R.id.text_view_activity_book_number_like);
        mNumberNoLikeTextView     = findViewById(R.id.text_view_activity_book_number_no_like);
        mNumberSubscribeTextView  = findViewById(R.id.text_view_activity_book_number_subscribe);
        mCote                     = findViewById(R.id.text_view_adapter_book_simple_id_book);
        mLikeImageView            = findViewById(R.id.image_view_activity_book_like);
        mNoLikeImageView          = findViewById(R.id.image_view_activity_book_no_like);
        mPlayerImageView          = findViewById(R.id.image_view_activity_book_player);
        mSubscribeImageView       = findViewById(R.id.image_view_activity_book_subscribe);
        mSeekBar                  = findViewById(R.id.seekbar_activity_book);
        mReservationLinearLayout  = findViewById(R.id.linear_layout_activity_book_reservation);
        mAudioLinearLayout        = findViewById(R.id.linear_layout_activity_book_audio);
        mElectronicLinearLayout   = findViewById(R.id.linear_layout_activity_book_electronic);
        mBackImageView            = findViewById(R.id.image_view_toolbar_book);
        mNameAuthor               = findViewById(R.id.text_view_adapter_book_simple_author_name);
        mPdfDownloadProgressContainer = findViewById(R.id.pdf_download_progress_container);
        mPdfDownloadProgressBar       = findViewById(R.id.pdf_download_progress_bar);
        mPdfDownloadPercentText       = findViewById(R.id.pdf_download_percent_text);
        mWaitPlayerProgressBar    = findViewById(R.id.progress_bar_activity_book_wait_player);
        mAudioSizeLinearLayout    = findViewById(R.id.linear_layout_activity_book_audio_size);
        mAudioSizeTextView        = findViewById(R.id.text_view_activity_book_audio_size);
        mMaxTimeLinearLayout      = findViewById(R.id.linear_layout_activity_book_maxTime);
        mMaxTimeTextView          = findViewById(R.id.text_view_activity_book_audio_max_time);
        mPdfSizeLinearLayout      = findViewById(R.id.linear_layout_activity_book_pdf_size);
        mPdfSizeTextView          = findViewById(R.id.text_view_activity_book_pdf_size);
        mNbrPageLinearLayout      = findViewById(R.id.linear_layout_activity_book_nbr_page);
        mNbrPageTextView          = findViewById(R.id.text_view_activity_book_pdf_max_page);
        mNbrView                  = findViewById(R.id.text_view_activity_book_view);

        mPlayerImageView.setVisibility(View.GONE);
        audioButton.setEnabled(false);

        startSkeletonShimmer(mSkeletonLoadingContainer);
        startArrowAnimation();
    }

    private void setupRecyclerViews() {
        List<Connection> waitList = new ArrayList<>();
        waitList.add(new Connection(getString(R.string.wait), null, true));

        SemiNoConnectionAdapter semiNoConnectionAdapter = new SemiNoConnectionAdapter(waitList);
        mCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCommentsRecyclerView.setAdapter(semiNoConnectionAdapter);
    }

    private void setupClickListeners() {
        mBackImageView.setOnClickListener(v -> onBackPressed());

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mMediaPlayer != null) mMediaPlayer.seekTo(progress);
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

    // ==================== SwipeRefresh ====================

    private void setupSwipeRefresh() {
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.purple_200,
                android.R.color.holo_blue_light,
                android.R.color.holo_orange_light
        );

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mNoConnectionContainer.setVisibility(View.GONE);
            showLoadingState();
            loadBookData();
        });

        mNestedScrollView.setOnScrollChangeListener(
                (NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) ->
                        mSwipeRefreshLayout.setEnabled(scrollY == 0));
    }

    private void stopRefreshing() {
        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    // ==================== États ====================

    private void showLoadingState() {
        mNestedScrollView.setVisibility(View.GONE);
        mNoConnectionContainer.setVisibility(View.GONE);
        mSkeletonLoadingContainer.setVisibility(View.VISIBLE);
        startSkeletonShimmer(mSkeletonLoadingContainer);
    }

    private void showContentState() {
        mSkeletonLoadingContainer.setVisibility(View.GONE);
        stopSkeletonShimmer(mSkeletonLoadingContainer);
        mNoConnectionContainer.setVisibility(View.GONE);
        mNestedScrollView.setVisibility(View.VISIBLE);
    }

    void showNoConnectionError() {
        stopRefreshing();
        stopSkeletonShimmer(mSkeletonLoadingContainer);
        mSkeletonLoadingContainer.setVisibility(View.GONE);
        mNestedScrollView.setVisibility(View.GONE);
        mNoConnectionContainer.setVisibility(View.VISIBLE);
    }

    // ==================== BroadcastReceivers ====================

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
        mAudioProgressReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int progress = intent.getIntExtra("progress", 0);
                mAudioDownloadProgressBar.setProgress(progress);
                mAudioDownloadPercentText.setText(progress + "%");
            }
        };

        mPdfProgressReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int progress = intent.getIntExtra("progress", 0);
                mPdfDownloadProgressBar.setProgress(progress);
                mPdfDownloadPercentText.setText(progress + "%");
            }
        };
        // registerReceiver(receiver, filter, int flags) n'existe que depuis l'API 33
        // (TIRAMISU) : le garder derriere Build.VERSION_CODES.O (API 26) comme
        // precedemment provoquait un NoSuchMethodError au runtime sur les appareils
        // API 28-32, pourtant explicitement supportes par l'app (minSdk 28). Meme
        // correction et meme repli 2-arguments que AudioPlayerActivity/AudioPlayerService.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mPdfProgressReceiver,
                    new IntentFilter("ACTION_PDF_DOWNLOAD_PROGRESS"),
                    Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mPdfProgressReceiver, new IntentFilter("ACTION_PDF_DOWNLOAD_PROGRESS"));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mFinishDownloadReceiver,
                    new IntentFilter(ACTION_FINISH_DOWNLOAD), Context.RECEIVER_NOT_EXPORTED);
            registerReceiver(mNoConnectionReceiver,
                    new IntentFilter(ACTION_BOOK), Context.RECEIVER_NOT_EXPORTED);
            registerReceiver(mAudioProgressReceiver,
                    new IntentFilter("ACTION_AUDIO_DOWNLOAD_PROGRESS"),
                    Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mFinishDownloadReceiver, new IntentFilter(ACTION_FINISH_DOWNLOAD));
            registerReceiver(mNoConnectionReceiver, new IntentFilter(ACTION_BOOK));
            registerReceiver(mAudioProgressReceiver, new IntentFilter("ACTION_AUDIO_DOWNLOAD_PROGRESS"));
        }
    }

    private void handleDownloadFinished(Intent intent) {
        String format = intent.getStringExtra("format");
        // Par defaut "true" pour ne pas casser un eventuel emetteur qui n'enverrait pas cet extra.
        boolean success = intent.getBooleanExtra("success", true);
        if ("audio".equals(format)) {
            mAudioDownloadProgressContainer.setVisibility(View.GONE);
            audioButton.setVisibility(View.VISIBLE);
            audioButton.setText(success ? "Lire" : "Format Audio");
        } else if ("pdf".equals(format)) {
            if (success) {
                mSourcePdf = mElectronicTable.getPdf(mOnlineBook.getId());
            }
            mPdfDownloadProgressContainer.setVisibility(View.GONE);
            downloadPDFButton.setVisibility(View.VISIBLE);
            // "Reessayer" (et non "Format PDF") sur echec : le fichier partiel local,
            // s'il existe, sera repris via Range au prochain tap (handlePdfDownload
            // accepte desormais les deux libelles), au lieu de laisser croire qu'aucun
            // telechargement n'a jamais ete tente (cf. audit sections 6/8).
            downloadPDFButton.setText(success ? "Ouvrir" : "Réessayer");
        }
        if (success) {
            Toast.makeText(this, mOnlineBook.getTitle() + " Téléchargé avec succès", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Échec du téléchargement, veuillez réessayer", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBookData() {
        // Migration Laravel : le detail du livre (idStruct, audioFiles, compteurs...) vient
        // desormais de /api/book/{idBook}/details ; le statut de reservation en depend
        // (idStruct) et n'est donc plus lance ici mais depuis processBookData(), une fois
        // ce detail recupere (voir checkReservationStatus()).
        String hostUrl = Server.getUrlHostProd(this);
        String bookId  = mOnlineBook.getId();

        new RecoveryBook(this).execute(hostUrl + "/api/book/" + bookId + "/details");
        new InsertViewSyn().execute(hostUrl + "/api/book/" + bookId + "/view");
        new IsSubscribeBookSyn().execute(hostUrl + "/api/book/" + bookId + "/subscription");
        new IsLikeSyn().execute(hostUrl + "/api/book/" + bookId + "/like");
        new IsNoLikeSyn().execute(hostUrl + "/api/book/" + bookId + "/dislike");
        new ReceiveComments().execute(hostUrl + "/api/book/" + bookId + "/comments");
    }

    // ==================== Click Handlers ====================

    private void handleReservationClick() {
        String buttonText = mReservationButton.getText().toString();
        if (buttonText.equals(getString(R.string.reservation_book))) {
            showReservationDialog();
        } else if (buttonText.equals(getString(R.string.cancel_reservation))) {
            showCancelConfirmationDialog(); // ← remplace l'appel direct
        }
    }

    private void showCancelConfirmationDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Annuler la réservation")
                .setMessage("Êtes-vous sûr de vouloir annuler votre réservation pour \""
                        + mOnlineBook.getTitle() + "\" ?")
                .setPositiveButton("Oui, annuler", (dialog, which) -> {
                    new CancelReservationSyn().execute(
                            Server.getUrlHostProd(this) + "/api/reservations/cancel",
                            mOnlineBook.getId(),
                            mSession.getIdNumber(),
                            mOnlineBook.getIdStruct()
                    );
                })
                .setNegativeButton("Non, garder", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    private void handlePdfDownload() {
        String buttonText = downloadPDFButton.getText().toString();
        if ("Format PDF".equals(buttonText) || "Réessayer".equals(buttonText)) {
            // Garde anti double-tap : si un téléchargement est déjà en cours pour ce
            // livre (ligne DOWNLOADING en base), on évite d'en déclencher un second en
            // parallèle vers le même fichier local (cf. audit section "états").
            if (ElectronicTable.STATUS_DOWNLOADING.equals(
                    mElectronicTable.getStatus(mSession.getIdNumber(), mOnlineBook.getId()))) {
                Toast.makeText(this, "Téléchargement déjà en cours", Toast.LENGTH_SHORT).show();
                return;
            }
            downloadPDFButton.setVisibility(View.GONE);
            mPdfDownloadProgressContainer.setVisibility(View.VISIBLE);
            mPdfDownloadProgressBar.setProgress(0);
            mPdfDownloadPercentText.setText("0%");
            Toast.makeText(this, "Téléchargement démarré", Toast.LENGTH_SHORT).show();
            startPdfDownloadService();
        } else if ("Ouvrir".equals(buttonText)) {
            openPdfDocument();
        }
    }

    private void startPdfDownloadService() {
        Intent intent = new Intent(this, PdfDownloadService.class);
        intent.putExtra("fileNames", new String[]{
                mOnlineBook.getCover(), mOnlineBook.getElectronic(),
                mCategory.getCover(), mAuthor.getProfile(),
                mSession.getIdNumber(), mOnlineBook.getId(),
                mOnlineBook.getDescription(), mOnlineBook.getAuthor(),
                mOnlineBook.getCategory(), mOnlineBook.getTitle(),
                mOnlineBook.getIdStruct()
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
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
            audioButton.setVisibility(View.GONE);
            mAudioDownloadProgressContainer.setVisibility(View.VISIBLE);
            mAudioDownloadProgressBar.setProgress(0);
            mAudioDownloadPercentText.setText("0%");
            startAudioDownloadService();
        } else if ("Lire".equals(buttonText)) {
            navigateToAudioPlayer();
        }
    }

    private void startAudioDownloadService() {
        Intent intent = new Intent(this, AudioDownloadService.class);
        intent.putExtra("fileNames", new String[]{
                mOnlineBook.getCover(), mOnlineBook.getElectronic(),
                mCategory.getCover(), mAuthor.getProfile(), mTones.getAudio(),
                mSession.getIdNumber(), mOnlineBook.getId(),
                mOnlineBook.getDescription(), mOnlineBook.getAuthor(),
                mOnlineBook.getCategory(), mOnlineBook.getTitle(), mTones.getDuration(),mOnlineBook.getCover(),
                mOnlineBook.getIdStruct()
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
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
        new InsertLikeSyn().execute(Server.getUrlHostProd(this) + "/api/book/" + mOnlineBook.getId() + "/like",
                mSession.getIdNumber(), mOnlineBook.getId());
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
        new InsertNoLikeSyn().execute(Server.getUrlHostProd(this) + "/api/book/" + mOnlineBook.getId() + "/dislike",
                mSession.getIdNumber(), mOnlineBook.getId());
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
        new InsertSubscribeBookSyn().execute(Server.getUrlHostProd(this) + "/api/book/" + mOnlineBook.getId() + "/subscription",
                mSession.getIdNumber(), mOnlineBook.getId());
    }

    private void sendComment() {
        String message = mMessageTextView.getText().toString();
        if (!"null".equals(message) && !message.isEmpty()) {
            Chat chat = new Chat(mSession.getIdNumber(), this, message);
            mMessageTextView.setText("");
            mTalksList.add(new Talks(mSession.getIdNumber() + ".png", chat.getUserName(), chat.getMessage()));
            talksAdapter = new TalksAdapter(mTalksList);
            mCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mCommentsRecyclerView.setAdapter(talksAdapter);
            mCommentsRecyclerView.smoothScrollToPosition(talksAdapter.getItemCount() - 1);
            new SendComments().execute(Server.getUrlHostProd(this) + "/api/book/" + mOnlineBook.getId() + "/comments",
                    chat.getMessage());
        }
    }

    // ==================== Book Data Processing ====================

    void processBookData(String jsonData) {
        stopRefreshing();
        showContentState();

        try {
            JSONObject obj = new JSONObject(jsonData);
            updateBookDetails(obj);
            loadBookCoverImage();
            updateStatistics();
            configureBookFormats();
            processAudioFiles(obj.optJSONArray("audioFiles"));
            checkReservationStatus();
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing book data", e);
        }
    }

    /**
     * Migration Laravel : consomme desormais /api/book/{idBook}/details
     * (BookAssociationController::getBookDetails), dont la forme differe de
     * l'ancien book.php : categories/author/structures/audioFiles sont des
     * objets/tableaux imbriques plutot que des colonnes GROUP_CONCAT a plat,
     * et l'auteur n'expose pas de call/email/whatsapp (champs absents du modele
     * Laravel Author) -> mis a "null" comme ailleurs dans l'appli pour masquer
     * les boutons de contact correspondants.
     */
    private void updateBookDetails(JSONObject obj) throws JSONException {
        mOnlineBook.setCover(obj.optString("blanket", "null"));
        mOnlineBook.setTitle(obj.optString("title", ""));
        mOnlineBook.setIsPhysic(obj.optString("isPhysic", "0"));
        mOnlineBook.setIsAudio(obj.optString("isAudio", "0"));
        mOnlineBook.setElectronic(obj.optString("electronic", "null"));
        mOnlineBook.setDescription(obj.optString("description", ""));
        mOnlineBook.setIsAvailable(obj.optString("available", "0"));
        mOnlineBook.setSize(obj.optString("size", "null"));
        mOnlineBook.setNbrPage(obj.optString("nbrPage", "null"));
        mOnlineBook.setNumberLikes(obj.optInt("numberLike", 0));
        mOnlineBook.setNumberNoLikes(obj.optInt("numberNoLike", 0));
        mOnlineBook.setNumberSubscribe(obj.optInt("numberSubscribe", 0));
        mOnlineBook.setNumberView(obj.optInt("numberView", 0));

        // Categories : peut desormais en avoir plusieurs -> jointes pour l'affichage,
        // comme le faisait l'ancien GROUP_CONCAT cote SQL.
        JSONArray categoriesArray = obj.optJSONArray("categories");
        StringBuilder titles = new StringBuilder();
        StringBuilder blankets = new StringBuilder();
        if (categoriesArray != null) {
            for (int i = 0; i < categoriesArray.length(); i++) {
                JSONObject cat = categoriesArray.getJSONObject(i);
                if (i > 0) { titles.append(", "); blankets.append(", "); }
                titles.append(cat.optString("title", ""));
                blankets.append(cat.optString("blanket", ""));
            }
        }
        String categoryTitle = titles.toString();
        String categoryBlanket = blankets.toString();
        mOnlineBook.setCategory(categoryTitle);
        mCategory = new Category(categoryBlanket, categoryTitle);

        // Auteur
        JSONObject authorObj = obj.optJSONObject("author");
        String authorName = authorObj != null ? authorObj.optString("name", "") : "";
        String authorFirstName = authorObj != null ? authorObj.optString("firstName", "") : "";
        String authorId = authorObj != null ? authorObj.optString("idAuthor", "") : "";
        String authorProfile = authorObj != null ? authorObj.optString("profile", "null") : "null";
        String authorProfession = authorObj != null ? authorObj.optString("profession", "null") : "null";
        mOnlineBook.setAuthor(authorFirstName + " " + authorName);
        mAuthor = new Author(authorId, authorName, authorFirstName, authorProfile, authorProfession,
                "null", "null", "null");

        // Structure : premiere structure associee (meme convention que pour BooksFragment).
        JSONArray structuresArray = obj.optJSONArray("structures");
        String idStruct = "0";
        if (structuresArray != null && structuresArray.length() > 0) {
            idStruct = structuresArray.getJSONObject(0).optString("id", "0");
        }
        mOnlineBook.setIdStruct(idStruct);

        mTitleTextView.setText(mOnlineBook.getTitle());
        mNameAuthor.setText("De " + authorName + " " + authorFirstName);
        mCote.setText("Cote : " + mOnlineBook.getId());
        mCategoryTextView.setText("Catégorie : " + mOnlineBook.getCategory());
        mDescriptionTextView.setText(mOnlineBook.getDescription());
    }

    /**
     * Remplace l'ancien Tones.php : les metadonnees audio sont desormais incluses
     * dans la reponse de /api/book/{idBook}/details (relation audioFiles).
     */
    private void processAudioFiles(JSONArray audioFilesArray) {
        if (audioFilesArray == null || audioFilesArray.length() == 0) return;
        try {
            mListTones.clear();
            for (int i = 0; i < audioFilesArray.length(); i++) {
                JSONObject audioObj = audioFilesArray.getJSONObject(i);
                String audio = audioObj.optString("audio", "");
                String title = audioObj.optString("title", "");
                String size = audioObj.optString("size", "null");
                String maxTime = audioObj.has("maxTime")
                        ? audioObj.optString("maxTime", "null")
                        : audioObj.optString("maxtime", "null");
                mListTones.add(new Tones(i + 1, audio, title, 0, false));
                if (i == 0) mTones = new Tones(0, audio, size, maxTime);
            }
            if (mTones != null) {
                if (!"null".equals(mTones.getSize())) {
                    mAudioSizeTextView.setText(mTones.getSize());
                    mAudioSizeLinearLayout.setVisibility(View.VISIBLE);
                }
                audioButton.setEnabled(true);
                mMaxTimeTextView.setText(mTones.getDuration());
                mMaxTimeLinearLayout.setVisibility(View.VISIBLE);
                setupMediaPlayer();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing audio files", e);
        }
    }

    /**
     * Statut de reservation pour l'utilisateur courant. Ne peut etre lance qu'une fois
     * mOnlineBook.getIdStruct() renseigne par updateBookDetails() (remplace l'ancien
     * is_reservation.php, appele en parallele du detail livre car il ne dependait pas
     * de idStruct).
     */
    private void checkReservationStatus() {
        String url = Server.getUrlHostProd(this) + "/api/reservations/check"
                + "?idStruct=" + mOnlineBook.getIdStruct()
                + "&idUser=" + mSession.getIdNumber()
                + "&idBook=" + mOnlineBook.getId();
        new IsReservationSyn().execute(url);
    }

    private void loadBookCoverImage() {
        Picasso.get()
                .load(Server.getUrlHostProd(BookActivity.this) +
                        "/api/public/resource/" + mOnlineBook.getIdStruct() + "/blanket/" + mOnlineBook.getCover())
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
                    mReservationButton.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(BookActivity.this, R.color.whiteSombre)));
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

            // Etat reel du telechargement (COMPLETED/DOWNLOADING/FAILED, ou null si
            // jamais tente) plutot que le seul isExist()==COMPLETED utilise auparavant :
            // sans cela, relancer l'app apres un telechargement interrompu (processus
            // tue) affichait de nouveau "Format PDF" comme si rien n'avait ete tente,
            // sans aucune trace de l'echec ni moyen de reprendre (audit sections 4/6/8).
            String electronicStatus = mElectronicTable.getStatus(mSession.getIdNumber(), mOnlineBook.getId());
            if (ElectronicTable.STATUS_COMPLETED.equals(electronicStatus)) {
                mSourcePdf = mElectronicTable.isExist(mSession.getIdNumber(), mOnlineBook.getId());
                downloadPDFButton.setText("Ouvrir");
            } else if (ElectronicTable.STATUS_DOWNLOADING.equals(electronicStatus)
                    || ElectronicTable.STATUS_FAILED.equals(electronicStatus)) {
                // Si un service de telechargement est reellement encore actif, la
                // prochaine diffusion de progression/fin (receivers deja enregistres
                // dans registerBroadcastReceivers) corrige l'affichage immediatement.
                // Sinon (telechargement interrompu par la fermeture de l'app), ce
                // bouton permet de reprendre : le fichier partiel local, s'il existe,
                // sera repris via Range (DownloadFile.start()).
                downloadPDFButton.setText("Réessayer");
            } else {
                downloadPDFButton.setText("Format PDF");
            }

//            if (!"null".equals(mOnlineBook.getSize())) {
//                mPdfSizeTextView.setText(mOnlineBook.getSize());
//                mPdfSizeLinearLayout.setVisibility(View.VISIBLE);
//            } else {
//                downloadPDFButton.setEnabled(false);
//                downloadPDFButton.setText("Bientôt");
//            }

            if (!"null".equals(mOnlineBook.getNbrPage())) {
                mNbrPageTextView.setText(mOnlineBook.getNbrPage());
                mNbrPageLinearLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    // ==================== AsyncTask Classes ====================

    private static class RecoveryBook extends AsyncTask<String, Void, String> {
        private final WeakReference<BookActivity> activityRef;

        RecoveryBook(BookActivity activity) { this.activityRef = new WeakReference<>(activity); }

        @Override
        protected String doInBackground(String... params) {
            BookActivity activity = activityRef.get();
            if (activity == null) return null;
            // params[0] = URL complete /api/book/{idBook}/details (plus de query string
            // id_number/id_book a construire : idBook est deja dans le chemin).
            return activity.executeGetRequest(params[0]);
        }

        @Override
        protected void onPostExecute(String jsonData) {
            BookActivity activity = activityRef.get();
            if (activity == null) return;
            if (jsonData != null) activity.processBookData(jsonData);
            else activity.showNoConnectionError();
        }
    }

    private class ReceiveComments extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executeGetRequest(params[0]);
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null) processComments(jsonData);
            else showCommentLoadError();
        }

        private void processComments(String jsonData) {
            try {
                JSONObject root = new JSONObject(jsonData);
                JSONArray jsonArray = root.optJSONArray("data");
                mTalksList.clear();
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String fullName = obj.optString("name", "") + " " + obj.optString("firstName", "");
                        mTalksList.add(new Talks(obj.optString("idUser", "") + ".png", fullName, obj.optString("message", "")));
                    }
                }
            } catch (JSONException e) { Log.e(TAG, "Error parsing comments", e); }
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

    /**
     * Configure le lecteur audio a partir de la premiere piste de mListTones/mTones
     * (alimentes par processAudioFiles(), lui-meme nourri par la reponse de
     * /api/book/{idBook}/details). Anciennement le onPostExecute de RecoveryTones,
     * devenu une methode normale puisque Tones.php n'existe plus.
     */
    private void setupMediaPlayer() {
            String url = Server.getUrlHostProd(getApplicationContext()) + "/api/public/resource/" + mOnlineBook.getIdStruct() + "/audio/" + mTones.getAudio();
            try {
                mMediaPlayer.setDataSource(url);
                mMediaPlayer.prepare();
                mSeekBar.setMax(mMediaPlayer.getDuration());
                mTones.setDuration(convertDurationToString(mMediaPlayer.getDuration()));
                mWaitPlayerProgressBar.setVisibility(View.GONE);
                mPlayerImageView.setVisibility(View.VISIBLE);
                mMediaPlayerThread = new Thread(() -> {
                    while (mMediaPlayer != null && !Thread.currentThread().isInterrupted()) {
                        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
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
            } catch (IOException e) { Log.e(TAG, "Error setting up media player", e); }
    }

    private class InsertLikeSyn extends AsyncTask<String, Void, String> {
        @Override protected String doInBackground(String... p) { return executePostRequest(p[0], createIdBookRequestBody(p[1], p[2])); }
        @Override protected void onPostExecute(String d) {}
    }

    private class InsertNoLikeSyn extends AsyncTask<String, Void, String> {
        @Override protected String doInBackground(String... p) { return executePostRequest(p[0], createIdBookRequestBody(p[1], p[2])); }
        @Override protected void onPostExecute(String d) {}
    }

    private class InsertSubscribeBookSyn extends AsyncTask<String, Void, String> {
        @Override protected String doInBackground(String... p) { return executePostRequest(p[0], createIdBookRequestBody(p[1], p[2])); }
        @Override protected void onPostExecute(String d) {}
    }

    private class InsertViewSyn extends AsyncTask<String, Void, String> {
        @Override protected String doInBackground(String... p) { return executePostRequest(p[0], RequestBody.create(new byte[0], null)); }
        @Override protected void onPostExecute(String d) {}
    }

    private class IsLikeSyn extends AsyncTask<String, Void, String> {
        @Override protected String doInBackground(String... p) { return executeGetRequest(p[0]); }
        @Override protected void onPostExecute(String jsonData) {
            if (jsonData == null) return;
            try {
                boolean liked = new JSONObject(jsonData).optBoolean("liked", false);
                isLike = liked;
                mLikeImageView.setImageResource(liked ? R.drawable.vector_purple2_200_on_like : R.drawable.vector_black3_off_like);
            } catch (JSONException e) { Log.e(TAG, "Error parsing like status", e); }
        }
    }

    private class IsNoLikeSyn extends AsyncTask<String, Void, String> {
        @Override protected String doInBackground(String... p) { return executeGetRequest(p[0]); }
        @Override protected void onPostExecute(String jsonData) {
            if (jsonData == null) return;
            try {
                boolean disliked = new JSONObject(jsonData).optBoolean("disliked", false);
                isNoLike = disliked;
                mNoLikeImageView.setImageResource(disliked ? R.drawable.vector_rouge_on_nolike : R.drawable.vector_black3_off_no_like);
            } catch (JSONException e) { Log.e(TAG, "Error parsing dislike status", e); }
        }
    }

    private class IsSubscribeBookSyn extends AsyncTask<String, Void, String> {
        @Override protected String doInBackground(String... p) { return executeGetRequest(p[0]); }
        @Override protected void onPostExecute(String jsonData) {
            if (jsonData == null) return;
            try {
                boolean subscribed = new JSONObject(jsonData).optBoolean("subscribed", false);
                isSubscribe = subscribed;
                mSubscribeImageView.setImageResource(subscribed ? R.drawable.vector_purple2_200_suscribe : R.drawable.vector_black3_off_subscribe);
            } catch (JSONException e) { Log.e(TAG, "Error parsing subscription status", e); }
        }
    }

    private class IsReservationSyn extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // params[0] = URL complete (idStruct/idUser/idBook deja en query string,
            // construite par checkReservationStatus() une fois idStruct connu).
            return executeGetRequest(params[0]);
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData == null) return;

            try {
                JSONObject obj = new JSONObject(jsonData);

                // Format : { "success": true, "data": { "state": X, "treat": X }, "message": "" }
                // (endpoint /api/reservations/check, meme enveloppe que l'ancien is_reservation.php)
                if (!obj.optBoolean("success", false)) return;
                JSONObject data  = obj.getJSONObject("data");
                String     state = data.getString("state");
                String     treat = data.getString("treat");
                if ("1".equals(state) && "0".equals(treat)) {
                    // Réservation active → bouton Annuler en rouge
                    mReservationButton.setText(R.string.cancel_reservation);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        mReservationButton.setBackgroundTintList(
                                ColorStateList.valueOf(
                                        ContextCompat.getColor(BookActivity.this, R.color.rouge)));

                } else if ("2".equals(state) && "1".equals(treat)) {
                    // Livre en cours de consultation → bouton désactivé
                    mReservationButton.setText("Venez récupérer le livre");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mReservationButton.setBackgroundTintList(
                                ColorStateList.valueOf(
                                        ContextCompat.getColor(BookActivity.this, R.color.whiteSombre)));
                        mReservationButton.setEnabled(false);
                    }
                }else if ("4".equals(state) && "1".equals(treat)) {
                    // Livre en cours de consultation → bouton désactivé
                    mReservationButton.setText("En cours de consultation");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mReservationButton.setBackgroundTintList(
                                ColorStateList.valueOf(
                                        ContextCompat.getColor(BookActivity.this, R.color.whiteSombre)));
                        mReservationButton.setEnabled(false);
                    }
                }
                // state: 5 → réservation annulée/expirée, aucun changement d'UI nécessaire

            } catch (JSONException e) {
                Log.e(TAG, "Error parsing reservation status", e);
            }
        }
    }

    private class CancelReservationSyn extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // params[0] = url  |  params[1] = idBook  |  params[2] = idUser  |  params[3] = idStruct
            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("idBook",   params[1])
                    .addFormDataPart("idUser",   params[2])
                    .addFormDataPart("idStruct", params[3])
                    .build();
            return executePostRequest(params[0], body);
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData == null) return;
            try {
                JSONObject obj = new JSONObject(jsonData);
                if (obj.optBoolean("success", false)) {
                    // Réinitialiser le bouton
                    mReservationButton.setText(R.string.reservation_book);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        mReservationButton.setBackgroundTintList(
                                ContextCompat.getColorStateList(BookActivity.this, R.color.black3));
                    Toast.makeText(BookActivity.this,
                            "Réservation annulée avec succès", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BookActivity.this,
                            obj.optString("message", "Erreur lors de l'annulation"),
                            Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) { Log.e(TAG, "Error parsing cancel response", e); }
        }
    }

    private class Reservation extends AsyncTask<String, Void, String> {
        private final Button mSendButton;
        private final ProgressBar mProgressBar;

        Reservation(Button sendButton, ProgressBar progressBar) { this.mSendButton = sendButton; this.mProgressBar = progressBar; }

        @Override
        protected String doInBackground(String... params) {
            // params[0] = idUser  |  params[1] = idBook  |  params[2] = numberOfDays  |  params[3] = idStruct
            return executePostRequest(Server.getUrlHostProd(getApplicationContext()) + "/api/reservations",
                    new MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("idStruct", params[3])
                            .addFormDataPart("idUser", params[0])
                            .addFormDataPart("idBook", params[1])
                            .addFormDataPart("numberOfDays", params[2]).build());
        }

        @Override
        protected void onPostExecute(String jsonData) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mSendButton.setEnabled(true);
            mSendButton.setText("Envoyer");
            if (jsonData == null) {
                Toast.makeText(BookActivity.this, "Erreur réseau, veuillez réessayer", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                JSONObject obj = new JSONObject(jsonData);
                String message = obj.optString("message", "");
                if ("Réservation créée ".equals(message)) {
                    mReservationDialog.cancel();
                    showSuccessReservationDialog("Merci d'avoir réservé \"" + mTitleTextView.getText().toString() + "\" sur fabi; nous traitons votre demande et vous confirmerons la disponibilité bientôt.");
                    mReservationButton.setText(R.string.cancel_reservation);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        mReservationButton.setBackgroundTintList(ContextCompat.getColorStateList(BookActivity.this, R.color.rouge));
                } else {
                    // Erreur de validation/metier renvoyee par ReservationController::store()
                    // (livre deja reserve, indisponible, etc.) desormais affichee a l'utilisateur
                    // plutot que silencieusement ignoree.
                    Toast.makeText(BookActivity.this,
                            message.isEmpty() ? "Erreur lors de la réservation" : message,
                            Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) { Log.e(TAG, "Error parsing reservation response", e); }
        }
    }

    private class SendComments extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // params[0] = url  |  params[1] = message (idBook est dans l'URL, idNumber vient du token)
            return executePostRequest(params[0], new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("message", params[1]).build());
        }
        @Override protected void onPostExecute(String d) {}
    }

    // ==================== Dialogs ====================

    private void showReservationDialog() {
        Spinner timeLimitSpinner = mReservationDialog.findViewById(R.id.spinner_dialog_reservation_time_limit);
        CheckBox localConsultationCheckBox = mReservationDialog.findViewById(R.id.check_box_dialog_reservation_local_consultation);
        Button sendButton = mReservationDialog.findViewById(R.id.button_dialog_reservation_send);
        EditText passwordEditText = mReservationDialog.findViewById(R.id.edit_text_dialog_reservation_password);
        TextView errorTextView = mReservationDialog.findViewById(R.id.text_view_dialog_reservation_error);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.delait_reservation, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeLimitSpinner.setAdapter(adapter);
        sendButton.setBackground(getDrawable(R.drawable.form_purple_200_radius_10dp));
        localConsultationCheckBox.setOnClickListener(v -> timeLimitSpinner.setEnabled(!localConsultationCheckBox.isChecked()));
        sendButton.setOnClickListener(v -> handleReservationSubmit(passwordEditText, errorTextView, timeLimitSpinner));
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
            mNbrJour = timeLimitSpinner.isEnabled() ? String.valueOf(timeLimitSpinner.getSelectedItemPosition() + 1) : String.valueOf(-1);
            Button sendButton = mReservationDialog.findViewById(R.id.button_dialog_reservation_send);
            ProgressBar progressBar = mReservationDialog.findViewById(R.id.progress_circularEvaluez);
            sendButton.setEnabled(false); sendButton.setText(""); progressBar.setVisibility(View.VISIBLE);
            // "Consultation locale" (mNbrJour = "-1") n'a pas d'equivalent cote Laravel
            // (numberOfDays exige un entier entre 1 et 30) -> on envoie la duree minimale.
            String numberOfDaysForServer = "-1".equals(mNbrJour) ? "1" : mNbrJour;
            new Reservation(sendButton, progressBar).execute(
                    mSession.getIdNumber(), mOnlineBook.getId(), numberOfDaysForServer, mOnlineBook.getIdStruct());
        }
    }

    private void showSuccessReservationDialog(String message) {
        SimpleOkDialog dialog = new SimpleOkDialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        ((TextView) dialog.findViewById(R.id.text_view_dialog_simple_ok_message)).setText(message);
        dialog.findViewById(R.id.text_view_dialog_simple_ok).setOnClickListener(v -> dialog.cancel());
        dialog.build();
    }

    // ==================== Context Menu ====================

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_item, menu);
        mTalksSelect = talksAdapter.getItem(talksAdapter.getPosition());
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_delete) { talksAdapter.remove(talksAdapter.getPosition()); return true; }
        return super.onContextItemSelected(item);
    }

    // ==================== Shimmer ====================

    private void startSkeletonShimmer(View container) {
        if (!(container instanceof ViewGroup)) return;
        List<View> skeletonViews = new ArrayList<>();
        collectSkeletonViews((ViewGroup) container, skeletonViews);

        mShimmerAnimator = ValueAnimator.ofFloat(0f, 1f);
        mShimmerAnimator.setDuration(1200);
        mShimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mShimmerAnimator.setRepeatMode(ValueAnimator.RESTART);
        mShimmerAnimator.addUpdateListener(anim -> {
            float fraction = (float) anim.getAnimatedValue();
            float alpha = 0.4f + 0.6f * (float)(0.5 + 0.5 * Math.sin(fraction * 2 * Math.PI));
            for (View v : skeletonViews) v.setAlpha(alpha);
        });
        mShimmerAnimator.start();
    }

    private void stopSkeletonShimmer(View container) {
        if (mShimmerAnimator != null) { mShimmerAnimator.cancel(); mShimmerAnimator = null; }
        if (container instanceof ViewGroup) {
            List<View> views = new ArrayList<>();
            collectSkeletonViews((ViewGroup) container, views);
            for (View v : views) v.setAlpha(1f);
        }
    }

    private void collectSkeletonViews(ViewGroup parent, List<View> out) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof ViewGroup) collectSkeletonViews((ViewGroup) child, out);
            else out.add(child);
        }
    }

    // ==================== Arrow Animation ====================

    private void startArrowAnimation() {
        if (mNoConnectionContainer == null) return;
        View arrow1 = mNoConnectionContainer.findViewById(R.id.arrow_1);
        View arrow2 = mNoConnectionContainer.findViewById(R.id.arrow_2);
        View arrow3 = mNoConnectionContainer.findViewById(R.id.arrow_3);
        if (arrow1 == null || arrow2 == null || arrow3 == null) return;

        mArrowAnimator = ValueAnimator.ofFloat(0f, 1f);
        mArrowAnimator.setDuration(1000);
        mArrowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mArrowAnimator.setRepeatMode(ValueAnimator.RESTART);
        mArrowAnimator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        mArrowAnimator.addUpdateListener(anim -> {
            float f = (float) anim.getAnimatedValue();
            float dp = getResources().getDisplayMetrics().density;
            float t1 = bounce(f), t2 = bounce((f + 0.33f) % 1f), t3 = bounce((f + 0.66f) % 1f);
            float max = 10f;
            arrow1.setTranslationY(t1 * max * dp); arrow2.setTranslationY(t2 * max * dp); arrow3.setTranslationY(t3 * max * dp);
            arrow1.setAlpha(0.25f + t1 * 0.3f); arrow2.setAlpha(0.55f + t2 * 0.25f); arrow3.setAlpha(0.85f + t3 * 0.15f);
        });
        mArrowAnimator.start();
    }

    private float bounce(float t) { return (float) Math.sin(t * Math.PI); }

    // ==================== Helper Methods ====================

    String executeGetRequest(String url) {
        try {
            Request request = new Request.Builder().url(url).get().build();
            try (Response response = mHttpClient.newCall(request).execute()) {
                if (response.body() != null) return response.body().string();
            }
        } catch (IOException e) { Log.e(TAG, "Network error: " + e.getMessage(), e); }
        catch (Exception e) { Log.e(TAG, "Unexpected error: " + e.getMessage(), e); }
        return null;
    }

    String executePostRequest(String url, RequestBody requestBody) {
        try {
            Request request = new Request.Builder().url(url).post(requestBody).build();
            try (Response response = mHttpClient.newCall(request).execute()) {
                if (response.body() != null) return response.body().string();
            }
        } catch (IOException e) { Log.e(TAG, "Network error: " + e.getMessage(), e); }
        catch (Exception e) { Log.e(TAG, "Unexpected error: " + e.getMessage(), e); }
        return null;
    }

    private RequestBody createIdBookRequestBody(String idNumber, String idBook) {
        return new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("idNumber", idNumber)
                .addFormDataPart("idBook", idBook).build();
    }

    private String convertDurationToString(int duration) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

    // ==================== Cycle de vie ====================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mShimmerAnimator != null) { mShimmerAnimator.cancel(); mShimmerAnimator = null; }
        if (mArrowAnimator != null)   { mArrowAnimator.cancel();   mArrowAnimator = null;   }
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mMediaPlayerThread != null && mMediaPlayerThread.isAlive()) mMediaPlayerThread.interrupt();
        try {
            if (mFinishDownloadReceiver != null) unregisterReceiver(mFinishDownloadReceiver);
            if (mNoConnectionReceiver != null) unregisterReceiver(mNoConnectionReceiver);
        } catch (Exception e) { Log.e(TAG, "Error unregistering receivers", e); }
        try {
            if (mAudioProgressReceiver != null) unregisterReceiver(mAudioProgressReceiver);
        } catch (Exception e) { Log.e(TAG, "Error unregistering audio progress receiver", e); }
        try {
            if (mPdfProgressReceiver != null) unregisterReceiver(mPdfProgressReceiver);
        } catch (Exception e) { Log.e(TAG, "Error unregistering pdf progress receiver", e); }
    }
}