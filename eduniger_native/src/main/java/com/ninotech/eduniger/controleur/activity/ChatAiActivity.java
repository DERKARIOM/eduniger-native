package com.ninotech.eduniger.controleur.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.adapter.ChatHistoryBottomSheet;
import com.ninotech.eduniger.controleur.adapter.MessageAdapter;
import com.ninotech.eduniger.controleur.adapter.StatusBarAdapter;
import com.ninotech.eduniger.model.data.ChatSession;
import com.ninotech.eduniger.model.data.Message;
import com.ninotech.eduniger.model.table.ChatDatabaseHelper;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import com.ninotech.eduniger.controleur.service.Audiotranscriptionservice;

// ── 2. Champ à ajouter dans la classe ────────────────────────────────────


public class ChatAiActivity extends AppCompatActivity {

    // ─── Constantes ───────────────────────────────────────────────────────────
    private static final String API_URL    = "http://78.46.46.154/eduniger/ai/eduna_gemma4.php";
    private static final String KAGGLE_URL = "https://lent-napkin-clergyman.ngrok-free.dev";
    private static final String ID_NUMBER  = "94961793";

    private static final int RC_BOOK  = 1001;  // résultats SearchActivity
    private static final int RC_IMAGE = 2001;  // galerie photo

    private String selectedBookId    = null;
    private String selectedBookTitle = null;

    // ─── Vues ─────────────────────────────────────────────────────────────────
    private RecyclerView  recyclerView;
    private EditText      etMessage;
    private ImageButton   btnSend, btnBack, btnMenu;
    private View          layoutTyping;
    private View          typingDot1, typingDot2, typingDot3;
    private android.animation.AnimatorSet typingAnimatorSet;
    private View          layoutEmptyState;
    private LinearLayout  btnAction1, btnAction2, btnAction3, btnAction4, btnAction5;

    // ─── Données ──────────────────────────────────────────────────────────────
    private MessageAdapter     adapter;
    private List<Message>      messages = new ArrayList<>();
    private OkHttpClient       httpClient;
    private ChatDatabaseHelper dbHelper;

    private String      sessionId;
    private ChatSession currentSession;
    private boolean     sessionSaved;
    private BroadcastReceiver audioReceiver;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_ai);

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
        dbHelper = ChatDatabaseHelper.getInstance(this);

        long resumeSessionId = getIntent().getLongExtra("session_db_id", -1L);
        if (resumeSessionId != -1L) {
            loadExistingSession(resumeSessionId);
        } else {
            sessionId    = generateSessionId();
            sessionSaved = false;
        }

        // Enregistrer le receiver ici, pas dans onResume
        // pour ne pas rater un broadcast rapide apres onActivityResult
        registerAudioReceiver();

        initViews();
        setupRecyclerView();
        setupListeners();
        updateEmptyState();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterAudioReceiver(); // on desinscrit proprement a la destruction
    }

    // ─── Initialisation ───────────────────────────────────────────────────────

    private void initViews() {
        new StatusBarAdapter(getApplicationContext(), getWindow());
        recyclerView     = findViewById(R.id.recyclerViewMessages);
        etMessage        = findViewById(R.id.etMessage);
        btnSend          = findViewById(R.id.btnSend);
        btnBack          = findViewById(R.id.btnBack);
        btnMenu          = findViewById(R.id.btnMenu);
        layoutTyping     = findViewById(R.id.layoutTyping);
        typingDot1       = findViewById(R.id.typing_dot_1);
        typingDot2       = findViewById(R.id.typing_dot_2);
        typingDot3       = findViewById(R.id.typing_dot_3);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnAction1       = findViewById(R.id.action1);
        btnAction2       = findViewById(R.id.action2);
        btnAction3       = findViewById(R.id.action3);
        btnAction4       = findViewById(R.id.action4);
        btnAction5       = findViewById(R.id.action5);
        findViewById(R.id.btnAttach).setOnClickListener(v -> showAttachMenu());
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(messages);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnSend.setOnClickListener(v -> sendMessage());
        btnMenu.setOnClickListener(v -> openHistory());

        btnAction1.setOnClickListener(v -> launchBookSearch("explain"));
        btnAction2.setOnClickListener(v -> launchBookSearch("summarize"));
        btnAction3.setOnClickListener(v -> launchBookSearch("question"));
        btnAction4.setOnClickListener(v -> pickImage());
        btnAction5.setOnClickListener(v -> launchBookSearch("audio"));
    }

    private void launchBookSearch(String action) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("search_key",         "ONLINE_BOOK");
        intent.putExtra("online_book_key",    "CHAT_AI_ACTIVITY");
        intent.putExtra("online_book_action", action);
        startActivityForResult(intent, RC_BOOK);
    }

    private void pickImage() {
        startActivityForResult(
                new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                RC_IMAGE);
    }

    // ─── BottomSheet d'attachement ────────────────────────────────────────────

    private void showAttachMenu() {
        String[] labels = {"Expliquer un livre", "Résumer un livre", "Poser une question", "Envoyer une image", "Transcrire un audio"};
        String[] actions = {"explain", "summarize", "question", "image", "audio"};
        int[]    icons  = {R.drawable.books_emp, R.drawable.books_emp, R.drawable.books_emp, R.drawable.books_emp, R.drawable.books_emp};

        android.widget.LinearLayout root = new android.widget.LinearLayout(this);
        root.setOrientation(android.widget.LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(20), dp(16), dp(36));
        android.graphics.drawable.GradientDrawable sheetBg = new android.graphics.drawable.GradientDrawable();
        sheetBg.setCornerRadii(new float[]{dp(24), dp(24), dp(24), dp(24), 0, 0, 0, 0});
        sheetBg.setColor(android.graphics.Color.parseColor("#1C1C1E"));
        root.setBackground(sheetBg);

        com.google.android.material.bottomsheet.BottomSheetDialog sheet =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);

        for (int i = 0; i < labels.length; i++) {
            android.widget.LinearLayout row = new android.widget.LinearLayout(this);
            row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(dp(8), dp(14), dp(8), dp(14));

            android.widget.FrameLayout iconContainer = new android.widget.FrameLayout(this);
            android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(dp(52), dp(52));
            lp.rightMargin = dp(16);
            iconContainer.setLayoutParams(lp);
            android.graphics.drawable.GradientDrawable iconBg = new android.graphics.drawable.GradientDrawable();
            iconBg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            iconBg.setColor(android.graphics.Color.parseColor("#2C2C2E"));
            iconContainer.setBackground(iconBg);
            android.widget.ImageView iv = new android.widget.ImageView(this);
            android.widget.FrameLayout.LayoutParams ivLp = new android.widget.FrameLayout.LayoutParams(dp(24), dp(24));
            ivLp.gravity = android.view.Gravity.CENTER;
            iv.setLayoutParams(ivLp);
            iv.setImageResource(icons[i]);
            iv.setColorFilter(android.graphics.Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
            iconContainer.addView(iv);
            row.addView(iconContainer);

            android.widget.TextView tv = new android.widget.TextView(this);
            tv.setText(labels[i]);
            tv.setTextColor(android.graphics.Color.WHITE);
            tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            row.addView(tv);
            root.addView(row);

            if (i < labels.length - 1) {
                android.view.View sep = new android.view.View(this);
                sep.setBackgroundColor(android.graphics.Color.parseColor("#2C2C2E"));
                android.widget.LinearLayout.LayoutParams sepLp = new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
                sepLp.leftMargin = dp(68);
                root.addView(sep, sepLp);
            }

            final String finalAction = actions[i];
            row.setOnClickListener(v -> {
                sheet.dismiss();
                if ("image".equals(finalAction)) {
                    pickImage();
                } else {
                    launchBookSearch(finalAction);
                }
            });
        }

        sheet.setContentView(root);
        if (sheet.getWindow() != null) {
            sheet.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet)
                    .setBackgroundResource(android.R.color.transparent);
        }
        com.google.android.material.bottomsheet.BottomSheetBehavior<?> behavior =
                com.google.android.material.bottomsheet.BottomSheetBehavior.from((View) root.getParent());
        behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
        sheet.show();
    }

    // ─── onActivityResult ─────────────────────────────────────────────────────

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // ─── Résultat depuis SearchActivity (livre ou audio) ─────────────────
        if (requestCode == RC_BOOK && resultCode == RESULT_OK && data != null) {
            String bookTitle = data.getStringExtra("book_title");
            String bookId    = data.getStringExtra("book_id");
            String action    = data.getStringExtra("online_book_action");

            Log.d("ChatAi", "RC_BOOK → title=" + bookTitle + " id=" + bookId + " action=" + action);

            if (bookTitle == null) return;

            selectedBookTitle = bookTitle;
            selectedBookId    = bookId;

            switch (action != null ? action : "") {
                case "explain":
                    addUserMessage("Explique-moi ce livre : " + bookTitle);
                    sendBookRequest("Explique en détail le livre \"" + bookTitle + "\". "
                            + "Présente les thèmes principaux, l'auteur, le résumé et ce qu'on peut retenir.", bookId);
                    break;

                case "summarize":
                    addUserMessage("Résume ce livre : " + bookTitle);
                    sendBookRequest("Fais un résumé complet du livre \"" + bookTitle + "\". "
                            + "Présente l'histoire, les personnages clés, les thèmes et les points essentiels.", bookId);
                    break;

                case "question":
                    // Livre chargé, l'utilisateur pose sa question librement
                    addBotMessage("📚 **" + bookTitle + "** chargé.\nQue voulez-vous savoir sur ce livre ?");
                    break;

                case "audio":
                    // ✅ Audio depuis EduNiger — transcription via PHP → Kaggle
                    addUserMessage("🎙️ Transcription de : " + bookTitle);
                    sendAudioRequest(bookId, bookTitle);
                    break;

                default:
                    addBotMessage("📚 **" + bookTitle + "** chargé.\nPosez votre question !");
            }
        }

        // ─── Image depuis la galerie téléphone ───────────────────────────────
        if (requestCode == RC_IMAGE && resultCode == RESULT_OK && data != null) {
            android.net.Uri imageUri = data.getData();
            if (imageUri != null) {
                addUserMessage("🖼️ Image envoyée pour analyse.");
                showTyping(true);
                sendImageToKaggle(imageUri);
            }
        }
    }

    // ─── Envoi d'un message texte simple ──────────────────────────────────────

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        etMessage.setText("");
        addUserMessage(text);
        showTyping(true);

        FormBody.Builder fb = new FormBody.Builder()
                .add("id_number", ID_NUMBER)
                .add("id_user",   ID_NUMBER)
                .add("request",   text)
                .add("message",   text)
                .add("action",    selectedBookId != null ? "ask_about_book" : "chat");

        if (selectedBookId != null) fb.add("id_book", selectedBookId);
        if (sessionId      != null) fb.add("session_id", sessionId);

        enqueueRequest(new Request.Builder().url(API_URL).post(fb.build()).build());
    }

    // ─── Envoi d'une demande sur un livre (expliquer / résumer) ──────────────

    private void sendBookRequest(String question, String bookId) {
        showTyping(true);

        FormBody body = new FormBody.Builder()
                .add("id_number",  ID_NUMBER)
                .add("id_user",    ID_NUMBER)
                .add("request",    question)
                .add("message",    question)
                .add("action",     "ask_about_book")
                .add("id_book",    bookId != null ? bookId : "")
                .add("session_id", sessionId != null ? sessionId : "")
                .build();

        enqueueRequest(new Request.Builder().url(API_URL).post(body).build());
    }

    // ─── Envoi de la demande audio au PHP (qui enverra à Kaggle) ─────────────
    // PHP route : action=transcribe_audio → cherche le fichier dans AUDIO_PATH
    // et l'envoie à /ask/audio via multipart

//    private void sendAudioRequest(String audioId, String audioTitle) {
//        showTyping(true);
//
//        FormBody body = new FormBody.Builder()
//                .add("id_number",  ID_NUMBER)
//                .add("id_user",    ID_NUMBER)
//                .add("action",     "transcribe_audio")          // ← route PHP dédiée audio
//                .add("id_book",    audioId != null ? audioId : "")
//                .add("message",    "Transcris et résume : " + audioTitle)
//                .add("request",    "Transcris et résume : " + audioTitle)
//                .add("session_id", sessionId != null ? sessionId : "")
//                .build();
//
//        enqueueRequest(new Request.Builder().url(API_URL).post(body).build());
//    }
private void sendAudioRequest(String audioId, String audioTitle) {
    showTyping(true);

    // Lancer le Service (survit à l'Activity)
    Intent serviceIntent = new Intent(this, Audiotranscriptionservice.class);
    serviceIntent.putExtra("audio_id",    audioId);
    serviceIntent.putExtra("audio_title", audioTitle);
    serviceIntent.putExtra("session_id",  sessionId);
    serviceIntent.putExtra("id_user",     ID_NUMBER);
    serviceIntent.putExtra("api_url",     API_URL);
    startService(serviceIntent);

    Log.d("ChatAi", "Service audio démarré pour: " + audioTitle);
}

    // ─── Envoi d'image directement à Kaggle depuis Android ───────────────────
    // On encode l'image en base64 puis on envoie au PHP (action=ask_image)
    // Le PHP proxye vers /ask/image de Kaggle

    private void sendImageToKaggle(android.net.Uri imageUri) {
        new Thread(() -> {
            try {
                // Lire l'image depuis la galerie
                InputStream is = getContentResolver().openInputStream(imageUri);
                File tmp = new File(getCacheDir(), "img_" + System.currentTimeMillis() + ".jpg");
                FileOutputStream fos = new FileOutputStream(tmp);
                byte[] buf = new byte[4096]; int len;
                while ((len = is.read(buf)) != -1) fos.write(buf, 0, len);
                fos.close(); is.close();

                // Encoder en base64
                byte[] bytes = java.nio.file.Files.readAllBytes(tmp.toPath());
                String b64   = Base64.encodeToString(bytes, Base64.NO_WRAP);
                tmp.delete(); // nettoyer le cache

                // Envoyer au PHP via FormBody (le PHP proxye vers Kaggle)
                FormBody body = new FormBody.Builder()
                        .add("id_number",    ID_NUMBER)
                        .add("id_user",      ID_NUMBER)
                        .add("action",       "ask_image")          // ← route PHP dédiée image
                        .add("image_base64", b64)
                        .add("message",      "Décris et analyse cette image en détail.")
                        .add("request",      "Décris et analyse cette image en détail.")
                        .add("session_id",   sessionId != null ? sessionId : "")
                        .build();

                Request request = new Request.Builder().url(API_URL).post(body).build();
                Response response = httpClient.newCall(request).execute();
                String rawBody = response.body() != null ? response.body().string() : "";
                Log.d("ChatAi", "Image RAW: " + rawBody.substring(0, Math.min(300, rawBody.length())));

                runOnUiThread(() -> {
                    showTyping(false);
                    try {
                        JSONObject json = new JSONObject(rawBody);
                        if (json.optBoolean("success", false)) {
                            String resp = json.optString("response", "");
                            if (json.has("session_id")) sessionId = json.getString("session_id");
                            if (!resp.isEmpty()) addBotMessage(resp);
                            else showModernToast("Réponse image vide");
                        } else {
                            showModernToast("Erreur image: " + json.optString("error", "Inconnue"));
                        }
                    } catch (Exception e) {
                        Log.e("ChatAi", "Image parse error: " + e.getMessage());
                        showModernToast("Erreur de lecture");
                    }
                });

            } catch (Exception e) {
                Log.e("ChatAi", "Image error: " + e.getMessage());
                runOnUiThread(() -> {
                    showTyping(false);
                    showModernToast("Erreur envoi image");
                });
            }
        }).start();
    }

    // ─── Helper générique pour envoyer une requête et traiter la réponse ─────

    private void enqueueRequest(Request request) {
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showTyping(false);
                    showModernToast("Connexion impossible — vérifiez votre connexion");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) {
                    runOnUiThread(() -> { showTyping(false); showModernToast("Réponse vide du serveur"); });
                    return;
                }
                String rawBody = response.body().string();
                Log.d("ChatAi", "RAW: " + rawBody.substring(0, Math.min(500, rawBody.length())));

                runOnUiThread(() -> {
                    showTyping(false);
                    try {
                        JSONObject json = new JSONObject(rawBody);
                        if (json.optBoolean("success", false)) {
                            String botResponse = json.optString("response", "");
                            if (botResponse.isEmpty()) { showModernToast("Réponse vide"); return; }
                            if (json.has("session_id")) sessionId = json.getString("session_id");
                            addBotMessage(botResponse);
                        } else {
                            String err = json.optString("error", json.optString("message", "Erreur inconnue"));
                            Log.e("ChatAi", "Erreur serveur: " + err);
                            showModernToast("Erreur: " + err);
                        }
                    } catch (Exception e) {
                        Log.e("ChatAi", "Parse error: " + e.getMessage() + " | body: " + rawBody);
                        showModernToast("Erreur de lecture");
                    }
                });
            }
        });
    }

    // ─── Messages ─────────────────────────────────────────────────────────────

    private void addUserMessage(String text) {
        Message msg = new Message(text, Message.TYPE_USER);
        persistMessage(msg);
        messages.add(msg);
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
        updateEmptyState();
    }

    private void addBotMessage(String text) {
        Message msg = new Message(text, Message.TYPE_BOT);
        persistMessage(msg);
        messages.add(msg);
        adapter.notifyItemInserted(messages.size() - 1);
        updateEmptyState();
        recyclerView.post(() -> recyclerView.smoothScrollToPosition(messages.size() - 1));
        adapter.animateLastBotMessage(msg);
    }

    private void updateEmptyState() {
        boolean empty = messages.isEmpty();
        layoutEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // ─── Persistance SQLite ───────────────────────────────────────────────────

    private void persistMessage(Message msg) {
        if (!sessionSaved && msg.getType() == Message.TYPE_USER) {
            String title = msg.getText().length() > 50 ? msg.getText().substring(0, 50) + "…" : msg.getText();
            currentSession = new ChatSession(sessionId, title);
            dbHelper.insertSession(currentSession);
            sessionSaved = true;
        }
        if (!sessionSaved) return;
        msg.setSessionDbId(currentSession.getId());
        dbHelper.insertMessage(msg);
        dbHelper.updateSession(currentSession.getId(), currentSession.getTitle());
    }

    private void loadExistingSession(long sessionDbId) {
        List<Message> saved = dbHelper.getMessagesForSession(sessionDbId);
        messages.addAll(saved);
        String uuid = getIntent().getStringExtra("session_uuid");
        sessionId = (uuid != null) ? uuid : generateSessionId();
        currentSession = new ChatSession(sessionDbId, sessionId, getIntent().getStringExtra("session_title"), 0L, 0L);
        sessionSaved = true;
    }

    // ─── Historique ───────────────────────────────────────────────────────────

    private void openHistory() {
        ChatHistoryBottomSheet sheet = new ChatHistoryBottomSheet(session -> {
            messages.clear();
            messages.addAll(dbHelper.getMessagesForSession(session.getId()));
            adapter.notifyDataSetChanged();
            if (!messages.isEmpty()) recyclerView.scrollToPosition(messages.size() - 1);
            currentSession = session;
            sessionId      = session.getSessionUuid();
            sessionSaved   = true;
            updateEmptyState();
        });
        sheet.show(getSupportFragmentManager(), "history");
    }

    // ─── Typing animation ─────────────────────────────────────────────────────

    private void showTyping(boolean show) {
        layoutTyping.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) startTypingAnimation();
        else      stopTypingAnimation();
    }

    private void startTypingAnimation() {
        if (typingDot1 == null) return;
        stopTypingAnimation();
        android.animation.ObjectAnimator a1 = buildDotAnimator(typingDot1, 0);
        android.animation.ObjectAnimator a2 = buildDotAnimator(typingDot2, 150);
        android.animation.ObjectAnimator a3 = buildDotAnimator(typingDot3, 300);
        typingAnimatorSet = new android.animation.AnimatorSet();
        typingAnimatorSet.playTogether(a1, a2, a3);
        typingAnimatorSet.start();
    }

    private void stopTypingAnimation() {
        if (typingAnimatorSet != null) { typingAnimatorSet.cancel(); typingAnimatorSet = null; }
        if (typingDot1 != null) typingDot1.setTranslationY(0f);
        if (typingDot2 != null) typingDot2.setTranslationY(0f);
        if (typingDot3 != null) typingDot3.setTranslationY(0f);
    }

    private android.animation.ObjectAnimator buildDotAnimator(View dot, int delay) {
        android.animation.ObjectAnimator anim = android.animation.ObjectAnimator.ofFloat(dot, "translationY", 0f, -8f, 0f);
        anim.setDuration(600);
        anim.setStartDelay(delay);
        anim.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        anim.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        return anim;
    }

    // ─── Toast moderne ────────────────────────────────────────────────────────

    private void showModernToast(String message) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        layout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        layout.setPadding(dp(16), dp(14), dp(20), dp(14));
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setCornerRadius(dp(50));
        bg.setColor(android.graphics.Color.parseColor("#1E1E1E"));
        layout.setBackground(bg);

        android.widget.ImageView icon = new android.widget.ImageView(this);
        icon.setImageResource(android.R.drawable.ic_dialog_alert);
        icon.setColorFilter(android.graphics.Color.parseColor("#FFA500"), android.graphics.PorterDuff.Mode.SRC_IN);
        android.widget.LinearLayout.LayoutParams iconLp = new android.widget.LinearLayout.LayoutParams(dp(18), dp(18));
        iconLp.rightMargin = dp(10);
        layout.addView(icon, iconLp);

        android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText(message);
        tv.setTextColor(android.graphics.Color.WHITE);
        tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13f);
        layout.addView(tv);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL, 0, dp(80));
        toast.show();
    }

    private int dp(float v) {
        return Math.round(android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()));
    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//        registerAudioReceiver();
//    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//        unregisterAudioReceiver();
//    }

    // ── 6. Ajouter ces deux méthodes helper ──────────────────────────────────
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerAudioReceiver() {
        if (audioReceiver != null) return; // déjà enregistré

        audioReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(android.content.Context ctx, Intent intent) {
                boolean success  = intent.getBooleanExtra("success", false);
                String  response = intent.getStringExtra("response");
                String  newSid   = intent.getStringExtra("session_id");

                showTyping(false);

                if (newSid != null && !newSid.isEmpty()) {
                    sessionId = newSid;
                }

                if (success && response != null && !response.isEmpty()) {
                    addBotMessage(response);
                } else {
                    String err = response != null ? response : "Erreur de transcription";
                    showModernToast("Audio: " + err);
                    Log.e("ChatAi", "Erreur audio service: " + err);
                }
            }
        };

        IntentFilter filter = new IntentFilter(Audiotranscriptionservice.ACTION_RESULT);
        registerReceiver(audioReceiver, filter);
        Log.d("ChatAi", "AudioReceiver enregistré");
    }

    private void unregisterAudioReceiver() {
        if (audioReceiver != null) {
            try {
                unregisterReceiver(audioReceiver);
            } catch (Exception ignored) {}
            audioReceiver = null;
            Log.d("ChatAi", "AudioReceiver désenregistré");
        }
    }


    private String generateSessionId() { return UUID.randomUUID().toString(); }
}