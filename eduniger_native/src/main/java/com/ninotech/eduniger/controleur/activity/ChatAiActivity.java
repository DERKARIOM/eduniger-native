package com.ninotech.eduniger.controleur.activity;
import android.util.Log;
import java.util.concurrent.TimeUnit;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;


import android.widget.LinearLayout;
import android.widget.TextView;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChatAiActivity extends AppCompatActivity {

    // ─── Constantes ───────────────────────────────────────────────────────────// ─── Constantes (Mise à jour pour Gemma 4 / FastAPI) ──────────────────────────
    private static final String API_URL   = "http://78.46.46.154/eduniger/ai/eduna_gemma3.php";
    private static final String KAGGLE_URL = "https://lent-napkin-clergyman.ngrok-free.dev";
    private static final String ID_NUMBER = "94961793";
    private String selectedBookId    = null;
    private String selectedBookTitle = null;

    // ─── Vues ─────────────────────────────────────────────────────────────────
    private RecyclerView recyclerView;
    private EditText     etMessage;
    private ImageButton  btnSend, btnBack, btnMenu;
    private View         layoutTyping;
    private View typingDot1, typingDot2, typingDot3;
    private android.animation.AnimatorSet typingAnimatorSet;

    // ─── Données ──────────────────────────────────────────────────────────────
    private MessageAdapter     adapter;
    private List<Message>      messages = new ArrayList<>();
    private OkHttpClient       httpClient;
    private ChatDatabaseHelper dbHelper;

    // Session courante
    private String      sessionId;      // UUID envoyé à l'API
    private ChatSession currentSession; // ligne SQLite correspondante
    private boolean     sessionSaved;   // true dès que la session est en base
    private View layoutEmptyState;
    private LinearLayout btnAction1, btnAction2, btnAction3, btnAction4, btnAction5;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_ai);

        // Importez java.util.concurrent.TimeUnit en haut du fichier
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // 1 min pour trouver le serveur
                .writeTimeout(60, TimeUnit.SECONDS)   // 1 min pour envoyer la question
                .readTimeout(90, TimeUnit.SECONDS)    // 1 min 30 pour attendre la réponse de l'IA
                .build();
        dbHelper   = ChatDatabaseHelper.getInstance(this);

        // Vérifie si on reprend une session existante (passée par Intent)
        long resumeSessionId = getIntent().getLongExtra("session_db_id", -1L);

        if (resumeSessionId != -1L) {
            // ── Reprise d'une conversation existante ──────────────────────
            loadExistingSession(resumeSessionId);
        } else {
            // ── Nouvelle conversation ─────────────────────────────────────
            sessionId    = generateSessionId();
            sessionSaved = false;
        }

        initViews();
        setupRecyclerView();
        setupListeners();
        updateEmptyState();
    }

    // ─── Initialisation ───────────────────────────────────────────────────────

    private void initViews() {
        new StatusBarAdapter(getApplicationContext(), getWindow());
        recyclerView = findViewById(R.id.recyclerViewMessages);
        etMessage    = findViewById(R.id.etMessage);
        btnSend      = findViewById(R.id.btnSend);
        btnBack      = findViewById(R.id.btnBack);
        btnMenu      = findViewById(R.id.btnMenu);
        layoutTyping = findViewById(R.id.layoutTyping);
        typingDot1 = findViewById(R.id.typing_dot_1);
        typingDot2 = findViewById(R.id.typing_dot_2);
        typingDot3 = findViewById(R.id.typing_dot_3);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnAction1 = findViewById(R.id.action1);
        btnAction2 = findViewById(R.id.action2);
        btnAction3 = findViewById(R.id.action3);
        btnAction4 = findViewById(R.id.action4);
        btnAction5 = findViewById(R.id.action5);
        findViewById(R.id.btnAttach).setOnClickListener(v -> showAttachMenu());
    }

    private void showAttachMenu() {
        // ── Root BottomSheet ─────────────────────────────────────────────
        android.widget.LinearLayout root = new android.widget.LinearLayout(this);
        root.setOrientation(android.widget.LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(20), dp(16), dp(36));

        android.graphics.drawable.GradientDrawable sheetBg =
                new android.graphics.drawable.GradientDrawable();
        sheetBg.setCornerRadii(new float[]{
                dp(24), dp(24), dp(24), dp(24), 0, 0, 0, 0 // coins haut arrondis
        });
        sheetBg.setColor(android.graphics.Color.parseColor("#1C1C1E"));
        root.setBackground(sheetBg);

        // ── Items ────────────────────────────────────────────────────────
        int[][] items = {
                { android.R.drawable.ic_menu_camera,      R.string.attach_camera   },
                { android.R.drawable.ic_menu_gallery,     R.string.attach_photos   },
                { android.R.drawable.ic_menu_add,         R.string.attach_fichiers },
                { android.R.drawable.ic_media_play,       R.string.attach_videos   },
        };
// ─── Labels — 5 actions ──────────────────────────────────────────────────
        String[] labels = {
                "Expliquer un livre",       // case 0
                "Résumer un livre",         // case 1
                "Poser une question",       // case 2
                "Visualiser une image",     // case 3
                "Transcrire un audio"       // case 4
        };        int[] icons = {
                R.drawable.books_emp,
                R.drawable.books_emp,
                R.drawable.books_emp,
                R.drawable.books_emp
        };

        com.google.android.material.bottomsheet.BottomSheetDialog sheet =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);

        for (int i = 0; i < labels.length; i++) {
            android.widget.LinearLayout row = new android.widget.LinearLayout(this);
            row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(dp(8), dp(14), dp(8), dp(14));

            // Icône ronde
            android.widget.FrameLayout iconContainer = new android.widget.FrameLayout(this);
            android.widget.LinearLayout.LayoutParams containerLp =
                    new android.widget.LinearLayout.LayoutParams(dp(52), dp(52));
            containerLp.rightMargin = dp(16);
            iconContainer.setLayoutParams(containerLp);

            android.graphics.drawable.GradientDrawable iconBg =
                    new android.graphics.drawable.GradientDrawable();
            iconBg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            iconBg.setColor(android.graphics.Color.parseColor("#2C2C2E"));
            iconContainer.setBackground(iconBg);

            android.widget.ImageView iv = new android.widget.ImageView(this);
            android.widget.FrameLayout.LayoutParams ivLp =
                    new android.widget.FrameLayout.LayoutParams(dp(24), dp(24));
            ivLp.gravity = android.view.Gravity.CENTER;
            iv.setLayoutParams(ivLp);
            iv.setImageResource(icons[i]);
            iv.setColorFilter(android.graphics.Color.WHITE,
                    android.graphics.PorterDuff.Mode.SRC_IN);
            iconContainer.addView(iv);
            row.addView(iconContainer);

            // Label
            android.widget.TextView tv = new android.widget.TextView(this);
            tv.setText(labels[i]);
            tv.setTextColor(android.graphics.Color.WHITE);
            tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            row.addView(tv, new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));

            root.addView(row);

            // Séparateur (sauf dernier)
            if (i < labels.length - 1) {
                android.view.View sep = new android.view.View(this);
                sep.setBackgroundColor(android.graphics.Color.parseColor("#2C2C2E"));
                android.widget.LinearLayout.LayoutParams sepLp =
                        new android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
                sepLp.leftMargin  = dp(68); // aligner avec le texte
                root.addView(sep, sepLp);
            }

            final int index = i;
            row.setOnClickListener(v -> {
                sheet.dismiss();
                switch (index) {
                    // ─── Switch dans showAttachMenu ──────────────────────────────────────────
                    case 0:  // Expliquer un livre
                        Intent i0 = new Intent(this, SearchActivity.class);
                        i0.putExtra("search_key",         "ONLINE_BOOK");
                        i0.putExtra("online_book_key",    "CHAT_AI_ACTIVITY");
                        i0.putExtra("online_book_action", "explain");
                        startActivityForResult(i0, 1001);
                        break;

                    case 1:  // Résumer un livre
                        Intent i1 = new Intent(this, SearchActivity.class);
                        i1.putExtra("search_key",         "ONLINE_BOOK");
                        i1.putExtra("online_book_key",    "CHAT_AI_ACTIVITY");
                        i1.putExtra("online_book_action", "summarize");
                        startActivityForResult(i1, 1001);
                        break;

                    case 2:  // Poser une question sur un livre
                        Intent i2 = new Intent(this, SearchActivity.class);
                        i2.putExtra("search_key",         "ONLINE_BOOK");
                        i2.putExtra("online_book_key",    "CHAT_AI_ACTIVITY");
                        i2.putExtra("online_book_action", "question");
                        startActivityForResult(i2, 1001);
                        break;

                    case 3:  // Visualiser une image — galerie téléphone
                        startActivityForResult(
                                new Intent(Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 2001);
                        break;

                    case 4:  // Transcrire un audio — depuis EduNiger
                        Intent i4 = new Intent(this, SearchActivity.class);
                        i4.putExtra("search_key",         "ONLINE_BOOK");
                        i4.putExtra("online_book_key",    "CHAT_AI_ACTIVITY");
                        i4.putExtra("online_book_action", "audio");
                        startActivityForResult(i4, 1001);
                        break;
                }
            });
        }

        sheet.setContentView(root);

        if (sheet.getWindow() != null) {
            sheet.getWindow().findViewById(
                            com.google.android.material.R.id.design_bottom_sheet)
                    .setBackgroundResource(android.R.color.transparent);
        }

        com.google.android.material.bottomsheet.BottomSheetBehavior<?> behavior =
                com.google.android.material.bottomsheet.BottomSheetBehavior.from(
                        (android.view.View) root.getParent());
        behavior.setState(
                com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);

        sheet.show();
    }

    private void pickImage() {
        startActivityForResult(
                new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 2001);
    }

    private void pickFile() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(i, "Choisir"), 2002);
    }

    private void pickVideo() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("video/*");
        startActivityForResult(i, 2003);
    }

    private void updateEmptyState() {
        boolean isEmpty = messages.isEmpty();
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(messages);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);
        updateEmptyState();
    }

    // ─── setupListeners — 5 boutons ──────────────────────────────────────────
    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnSend.setOnClickListener(v -> sendMessage());
        btnMenu.setOnClickListener(v -> openHistory());

        btnAction1.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra("search_key",         "ONLINE_BOOK");
            intent.putExtra("online_book_key",    "CHAT_AI_ACTIVITY");
            intent.putExtra("online_book_action", "explain");
            startActivityForResult(intent, 1001);
        });

        btnAction2.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra("search_key",         "ONLINE_BOOK");
            intent.putExtra("online_book_key",    "CHAT_AI_ACTIVITY");
            intent.putExtra("online_book_action", "summarize");
            startActivityForResult(intent, 1001);
        });

        btnAction3.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra("search_key",         "ONLINE_BOOK");
            intent.putExtra("online_book_key",    "CHAT_AI_ACTIVITY");
            intent.putExtra("online_book_action", "question");
            startActivityForResult(intent, 1001);
        });

        btnAction4.setOnClickListener(v -> {
            // Galerie pour image
            startActivityForResult(
                    new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 2001);
        });
        btnAction5.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra("search_key",         "ONLINE_BOOK");
            intent.putExtra("online_book_key",    "CHAT_AI_ACTIVITY");
            intent.putExtra("online_book_action", "audio");
            startActivityForResult(intent, 1001);
        });
    }

    // ─── Reprise d'une session existante ──────────────────────────────────────

    private void loadExistingSession(long sessionDbId) {
        // Chargement des messages depuis SQLite
        List<Message> saved = dbHelper.getMessagesForSession(sessionDbId);
        messages.addAll(saved);

        // Retrouver la session pour récupérer son UUID réseau
        // (on passe par getAllSessions ou on stocke l'uuid dans l'Intent)
        String uuid = getIntent().getStringExtra("session_uuid");
        sessionId = (uuid != null) ? uuid : generateSessionId();

        currentSession = new ChatSession(
                sessionDbId,
                sessionId,
                getIntent().getStringExtra("session_title"),
                0L, 0L
        );
        sessionSaved = true;
    }

    // ─── Envoi d'un message ───────────────────────────────────────────────────

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        etMessage.setText("");
        showTyping(true);

        FormBody.Builder fb = new FormBody.Builder()
                .add("id_number", ID_NUMBER)
                .add("request",   text)
                .add("action",    "ask_about_book");

        // Livre sélectionné
        if (selectedBookId != null) {
            fb.add("id_book", selectedBookId);
        }
        if (sessionId != null) {
            fb.add("session_id", sessionId);
        }

        Request request = new Request.Builder()
                .url(API_URL)
                .post(fb.build())
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showTyping(false);
                    etMessage.setText(text);
                    showModernToast("Connexion impossible — vérifiez votre connexion");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {

                    String errorBody = "";

                    try {
                        if (response.body() != null) {
                            errorBody = response.body().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Log.e("ChatAi",
                            "HTTP CODE: " + response.code()
                                    + " | BODY: " + errorBody);

                    runOnUiThread(() -> {
                        showTyping(false);
                        showModernToast("Erreur HTTP: " + response.code());
                    });

                    return;
                }

                String rawBody = response.body().string();
                Log.d("ChatAi", "RAW: " + rawBody);

                runOnUiThread(() -> {
                    showTyping(false);
                    try {
                        JSONObject json = new JSONObject(rawBody);
                        if (json.optBoolean("success", false)) {
                            String botResponse = json.optString("response", "");
                            if (botResponse.isEmpty()) {
                                showModernToast("Réponse vide");
                                return;
                            }
                            addUserMessage(text);
                            if (json.has("session_id")) {
                                sessionId = json.getString("session_id");
                            }
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

    private void showModernToast(String message) {
        android.view.LayoutInflater inflater = getLayoutInflater();

        // Container
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        layout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        layout.setPadding(dp(16), dp(14), dp(20), dp(14));

        android.graphics.drawable.GradientDrawable bg =
                new android.graphics.drawable.GradientDrawable();
        bg.setCornerRadius(dp(50));
        bg.setColor(android.graphics.Color.parseColor("#1E1E1E"));
        layout.setBackground(bg);

        // Icône warning
        android.widget.ImageView icon = new android.widget.ImageView(this);
        icon.setImageResource(android.R.drawable.ic_dialog_alert);
        icon.setColorFilter(android.graphics.Color.parseColor("#FFA500"),
                android.graphics.PorterDuff.Mode.SRC_IN);
        android.widget.LinearLayout.LayoutParams iconLp =
                new android.widget.LinearLayout.LayoutParams(dp(18), dp(18));
        iconLp.rightMargin = dp(10);
        layout.addView(icon, iconLp);

        // Texte
        android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText(message);
        tv.setTextColor(android.graphics.Color.WHITE);
        tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13f);
        layout.addView(tv, new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL,
                0, dp(80));
        toast.show();
    }

    private int dp(float v) {
        return Math.round(android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics()));
    }

    // ─── Helpers messages ─────────────────────────────────────────────────────

    private void addUserMessage(String text) {
        Message msg = new Message(text, Message.TYPE_USER);
        persistMessage(msg);
        messages.add(msg);
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
        updateEmptyState(); // ← ajouter
    }

    private void addBotMessage(String text) {
        Message msg = new Message(text, Message.TYPE_BOT);
        persistMessage(msg);
        messages.add(msg);
        adapter.notifyItemInserted(messages.size() - 1);
        updateEmptyState();

        // ✅ Scroll APRÈS l'insertion, pas pendant l'animation
        recyclerView.post(() ->
                recyclerView.smoothScrollToPosition(messages.size() - 1)
        );

        adapter.animateLastBotMessage(msg);
    }


    // ─── Persistance SQLite ───────────────────────────────────────────────────

    /**
     * Sauvegarde un message en base.
     * Crée la session SQLite au premier message si elle n'existe pas encore.
     */
    private void persistMessage(Message msg) {
        // Créer la session en base au 1er message réel (pas le message de bienvenue)
        if (!sessionSaved && msg.getType() == Message.TYPE_USER) {
            String title = msg.getText().length() > 50
                    ? msg.getText().substring(0, 50) + "…"
                    : msg.getText();
            currentSession = new ChatSession(sessionId, title);
            dbHelper.insertSession(currentSession);
            sessionSaved = true;
        }

        if (!sessionSaved) return; // on ne persiste pas le message de bienvenue

        msg.setSessionDbId(currentSession.getId());
        dbHelper.insertMessage(msg);

        // Mettre à jour la date de la session
        dbHelper.updateSession(currentSession.getId(), currentSession.getTitle());
    }

    // ─── Historique ───────────────────────────────────────────────────────────

    /**
     * Ouvre le BottomSheet (ou Activity) d'historique des discussions.
     * Implémentation minimale : utilise ChatHistoryBottomSheet.
     */
    private void openHistory() {
        ChatHistoryBottomSheet sheet = new ChatHistoryBottomSheet(session -> {
            messages.clear();
            List<Message> saved = dbHelper.getMessagesForSession(session.getId());
            messages.addAll(saved);
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(messages.size() - 1);

            currentSession = session;
            sessionId      = session.getSessionUuid();
            sessionSaved   = true;

            updateEmptyState(); // ← ajouter cette ligne
        });
        sheet.show(getSupportFragmentManager(), "history");
    }

    // ─── Utilitaires ──────────────────────────────────────────────────────────

    private void showTyping(boolean show) {
        layoutTyping.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) startTypingAnimation();
        else      stopTypingAnimation();
    }

    private void startTypingAnimation() {
        if (typingDot1 == null || typingDot2 == null || typingDot3 == null) return;
        stopTypingAnimation();

        android.animation.ObjectAnimator a1 = buildDotAnimator(typingDot1, 0);
        android.animation.ObjectAnimator a2 = buildDotAnimator(typingDot2, 150);
        android.animation.ObjectAnimator a3 = buildDotAnimator(typingDot3, 300);

        typingAnimatorSet = new android.animation.AnimatorSet();
        typingAnimatorSet.playTogether(a1, a2, a3);
        typingAnimatorSet.start();
    }

    private void stopTypingAnimation() {
        if (typingAnimatorSet != null) {
            typingAnimatorSet.cancel();
            typingAnimatorSet = null;
        }
        if (typingDot1 != null) typingDot1.setTranslationY(0f);
        if (typingDot2 != null) typingDot2.setTranslationY(0f);
        if (typingDot3 != null) typingDot3.setTranslationY(0f);
    }

    private android.animation.ObjectAnimator buildDotAnimator(View dot, int startDelay) {
        android.animation.ObjectAnimator anim =
                android.animation.ObjectAnimator.ofFloat(dot, "translationY", 0f, -8f, 0f);
        anim.setDuration(600);
        anim.setStartDelay(startDelay);
        anim.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        anim.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        return anim;
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    // ─── onActivityResult complet ────────────────────────────────────────────
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // ─── Livre / Audio depuis EduNiger ───────────────────────────────
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            String bookTitle = data.getStringExtra("book_title");
            String bookId    = data.getStringExtra("book_id");
            String action    = data.getStringExtra("online_book_action");

            Log.d("ChatAi", "book_title = " + bookTitle);
            Log.d("ChatAi", "book_id    = " + bookId);
            Log.d("ChatAi", "action     = " + action);

            if (bookTitle != null) {
                selectedBookTitle = bookTitle;
                selectedBookId    = bookId;

                if ("explain".equals(action)) {
                    addUserMessage("Explique moi ce livre : " + bookTitle);
                    sendBookRequest("Explique en détail le livre \"" + bookTitle + "\". "
                            + "Présente les thèmes principaux, les personnages, "
                            + "le résumé et ce qu'on peut retenir.");

                } else if ("summarize".equals(action)) {
                    addUserMessage("Résume ce livre : " + bookTitle);
                    sendBookRequest("Fais un résumé complet du livre \"" + bookTitle + "\". "
                            + "Présente l'histoire, les personnages clés, "
                            + "les thèmes et les points essentiels.");

                } else if ("question".equals(action)) {
                    // ✅ Livre chargé — l'utilisateur pose sa propre question
                    addBotMessage("📚 **" + bookTitle + "** chargé.\n"
                            + "Posez votre question sur ce livre !");

                } else if ("audio".equals(action)) {
                    // ✅ Audio depuis EduNiger — envoyer directement à Kaggle
                    addUserMessage("🎙️ Audio : " + bookTitle);
                    showTyping(true);
                    sendAudioFromEduNiger(bookId, bookTitle);

                } else {
                    addBotMessage("📚 **" + bookTitle + "** chargé.\n"
                            + "Que voulez-vous savoir ?");
                }
            }
        }

        // ─── Image depuis galerie téléphone ──────────────────────────────
        if (requestCode == 2001 && resultCode == RESULT_OK && data != null) {
            android.net.Uri imageUri = data.getData();
            if (imageUri != null) {
                addUserMessage("🖼️ Image envoyée pour analyse.");
                showTyping(true);
                sendImageToKaggle(imageUri);
            }
        }
    }

    // ─── Envoyer image à Kaggle ──────────────────────────────────────────────
    private void sendImageToKaggle(android.net.Uri imageUri) {
        new Thread(() -> {
            try {
                java.io.InputStream      is  = getContentResolver().openInputStream(imageUri);
                java.io.File             tmp = new java.io.File(getCacheDir(),
                        "img_" + System.currentTimeMillis() + ".jpg");
                java.io.FileOutputStream fos = new java.io.FileOutputStream(tmp);
                byte[] buf = new byte[4096]; int len;
                while ((len = is.read(buf)) != -1) fos.write(buf, 0, len);
                fos.close(); is.close();

                byte[] bytes = java.nio.file.Files.readAllBytes(tmp.toPath());
                String b64   = android.util.Base64.encodeToString(bytes,
                        android.util.Base64.NO_WRAP);

                org.json.JSONObject json = new org.json.JSONObject();
                json.put("image_base64", b64);
                json.put("question", "Décris et analyse cette image en détail.");

                okhttp3.RequestBody body = okhttp3.RequestBody.create(
                        okhttp3.MediaType.parse("application/json"),
                        json.toString());

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(KAGGLE_URL + "/ask/image")
                        .post(body)
                        .addHeader("ngrok-skip-browser-warning", "true")
                        .addHeader("User-Agent", "EduNigerApp/1.0")
                        .build();

                okhttp3.Response response = httpClient.newCall(request).execute();
                String rawBody = response.body() != null ? response.body().string() : "";
                Log.d("ChatAi", "Image RAW: " + rawBody);

                runOnUiThread(() -> {
                    showTyping(false);
                    try {
                        org.json.JSONObject result = new org.json.JSONObject(rawBody);
                        if ("ok".equals(result.optString("status"))) {
                            addBotMessage(result.getString("response"));
                        } else {
                            showModernToast("Erreur image : " + result.optString("message"));
                        }
                    } catch (Exception e) {
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

    // ─── Envoyer audio depuis EduNiger à Kaggle via PHP ──────────────────────
    private void sendAudioFromEduNiger(String audioId, String audioTitle) {
        // Le PHP récupère l'audio depuis la BDD et l'envoie à Kaggle
        new Thread(() -> {
            try {
                okhttp3.RequestBody body = new okhttp3.FormBody.Builder()
                        .add("id_number", ID_NUMBER)
                        .add("id_user",   ID_NUMBER)
                        .add("id_book",   audioId != null ? audioId : "")
                        .add("request",   "Transcris et résume cet audio : " + audioTitle)
                        .add("message",   "Transcris et résume cet audio : " + audioTitle)
                        .add("action",    "transcribe_audio")
                        .add("session_id", sessionId != null ? sessionId : "")
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(API_URL)
                        .post(body)
                        .build();

                okhttp3.Response response = httpClient.newCall(request).execute();
                String rawBody = response.body() != null ? response.body().string() : "";
                Log.d("ChatAi", "Audio EduNiger RAW: " + rawBody);

                runOnUiThread(() -> {
                    showTyping(false);
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(rawBody);
                        if (json.optBoolean("success", false)) {
                            String botResponse = json.optString("response", "");
                            if (json.has("session_id")) sessionId = json.getString("session_id");
                            if (!botResponse.isEmpty()) addBotMessage(botResponse);
                        } else {
                            showModernToast("Erreur : " + json.optString("error", "Inconnue"));
                        }
                    } catch (Exception e) {
                        showModernToast("Erreur de lecture");
                    }
                });

            } catch (Exception e) {
                Log.e("ChatAi", "Audio EduNiger error: " + e.getMessage());
                runOnUiThread(() -> {
                    showTyping(false);
                    showModernToast("Erreur audio");
                });
            }
        }).start();
    }
    private void sendBookRequest(String question) {
        showTyping(true);

        FormBody body = new FormBody.Builder()
                .add("id_number", ID_NUMBER)
                .add("id_user",   ID_NUMBER)
                .add("request",   question)
                .add("message",   question)
                .add("action",    "ask_about_book")
                .add("id_book",   selectedBookId  != null ? selectedBookId  : "")
                .add("session_id", sessionId      != null ? sessionId       : "")
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showTyping(false);
                    showModernToast("Connexion impossible");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String rawBody = response.body() != null ? response.body().string() : "";
                Log.d("ChatAi", "BookRequest RAW: " + rawBody);
                runOnUiThread(() -> {
                    showTyping(false);
                    try {
                        JSONObject json = new JSONObject(rawBody);
                        if (json.optBoolean("success", false)) {
                            String botResponse = json.optString("response", "");
                            if (json.has("session_id")) sessionId = json.getString("session_id");
                            if (!botResponse.isEmpty()) addBotMessage(botResponse);
                        } else {
                            showModernToast("Erreur : " + json.optString("error", "Inconnue"));
                        }
                    } catch (Exception e) {
                        Log.e("ChatAi", "Parse error: " + e.getMessage());
                        showModernToast("Erreur de lecture");
                    }
                });
            }
        });
    }
}