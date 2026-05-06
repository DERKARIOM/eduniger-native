package com.ninotech.eduniger.controleur.activity;

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

    // ─── Constantes ───────────────────────────────────────────────────────────
    private static final String API_URL   = "http://78.46.46.154/eduniger/ai/eduna_unified.php";
    private static final String ACTION    = "ask_about_book";
    private static final String ID_NUMBER = "94961793";

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
    private LinearLayout btnAction1, btnAction2, btnAction3, btnAction4;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_ai);

        httpClient = new OkHttpClient();
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

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnSend.setOnClickListener(v -> sendMessage());

        // Bouton menu → ouvre l'historique des discussions
        btnMenu.setOnClickListener(v -> openHistory());
        btnAction1.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra("search_key", "ONLINE_BOOK");
            intent.putExtra("online_book_key", "CHAT_AI_ACTIVITY");
            startActivityForResult(intent, 1001);
        });
        btnAction2.setOnClickListener(v -> Toast.makeText(this, "Action 2", Toast.LENGTH_SHORT).show());
        btnAction3.setOnClickListener(v -> Toast.makeText(this, "Action 3", Toast.LENGTH_SHORT).show());
        btnAction4.setOnClickListener(v -> Toast.makeText(this, "Action 4", Toast.LENGTH_SHORT).show());
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

        // Vider l'EditText immédiatement (UX fluide)
        etMessage.setText("");

        // Afficher l'indicateur de frappe
        showTyping(true);

        FormBody.Builder fb = new FormBody.Builder()
                .add("id_number", ID_NUMBER)
                .add("request",   text)
                .add("action",    ACTION);
        if (sessionId != null) fb.add("session_id", sessionId);

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
                    etMessage.setSelection(text.length());
                    showModernToast("Il y'a un souci, vérifiez votre connexion et réessayez");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // ── Erreur HTTP (4xx, 5xx, pas de corps) ──────────────────────────
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> {
                        showTyping(false);
                        etMessage.setText(text);
                        etMessage.setSelection(text.length());
                        showModernToast("Il y'a un souci, vérifiez votre connexion et réessayez");
                    });
                    return;
                }

                String rawBody = response.body().string();
                runOnUiThread(() -> {
                    showTyping(false);
                    try {
                        JSONObject json = new JSONObject(rawBody);
                        if (json.optBoolean("success", false)) {
                            String botResponse = json.getString("response");

                            // ── Détecter les erreurs déguisées en succès ──────────────────
                            if (botResponse.startsWith("Erreur") || botResponse.startsWith("Error")) {
                                etMessage.setText(text);
                                etMessage.setSelection(text.length());
                                showModernToast("Il y'a un souci, vérifiez votre connexion et réessayez");
                                return;
                            }

                            addUserMessage(text);
                            if (json.has("session_id")) {
                                sessionId = json.getString("session_id");
                            }
                            addBotMessage(botResponse);
                        }
                    } catch (Exception e) {
                        etMessage.setText(text);
                        etMessage.setSelection(text.length());
                        showModernToast("Il y'a un souci, vérifiez votre connexion et réessayez");
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
        persistMessage(msg);          // persiste le texte COMPLET en base
        messages.add(msg);
        adapter.notifyItemInserted(messages.size() - 1);
        updateEmptyState();
        adapter.animateLastBotMessage(msg); // ← déclenche le typewriter
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            String bookTitle = data.getStringExtra("book_title");
            if (bookTitle != null) {
                String query = "Explique moi ce livre : " + bookTitle;
                etMessage.setText(query);
                sendMessage();
            }
        }
    }
}