package com.ninotech.eduniger.controleur.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

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

    // ─── Données ──────────────────────────────────────────────────────────────
    private MessageAdapter     adapter;
    private List<Message>      messages = new ArrayList<>();
    private OkHttpClient       httpClient;
    private ChatDatabaseHelper dbHelper;

    // Session courante
    private String      sessionId;      // UUID envoyé à l'API
    private ChatSession currentSession; // ligne SQLite correspondante
    private boolean     sessionSaved;   // true dès que la session est en base

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

        if (!sessionSaved) {
            // Message de bienvenue uniquement pour une nouvelle conversation
            addBotMessage("Bonjour ! Comment puis-je t'aider aujourd'hui ?");
        }
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

        // Bouton menu → ouvre l'historique des discussions
        btnMenu.setOnClickListener(v -> openHistory());
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

        // 1. Afficher + persister le message utilisateur
        addUserMessage(text);
        etMessage.setText("");

        // 2. Afficher l'indicateur de frappe
        showTyping(true);

        // 3. Appel réseau
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
                    addBotMessage("Erreur réseau : " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String rawBody = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    showTyping(false);
                    try {
                        JSONObject json = new JSONObject(rawBody);
                        if (json.optBoolean("success", false)) {
                            if (json.has("session_id")) {
                                sessionId = json.getString("session_id");
                            }
                            String botResponse = json.getString("response");
                            addBotMessage(botResponse);
                        } else {
                            addBotMessage("Désolé, une erreur s'est produite.");
                        }
                    } catch (Exception e) {
                        addBotMessage("Réponse invalide du serveur.");
                    }
                });
            }
        });
    }

    // ─── Helpers messages ─────────────────────────────────────────────────────

    private void addUserMessage(String text) {
        Message msg = new Message(text, Message.TYPE_USER);
        persistMessage(msg);          // ← sauvegarde SQLite
        messages.add(msg);
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
    }

    private void addBotMessage(String text) {
        Message msg = new Message(text, Message.TYPE_BOT);
        persistMessage(msg);          // ← sauvegarde SQLite
        messages.add(msg);
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
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
            // L'utilisateur a tapé sur une session → on la charge
            messages.clear();
            List<Message> saved = dbHelper.getMessagesForSession(session.getId());
            messages.addAll(saved);
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(messages.size() - 1);

            currentSession = session;
            sessionId      = session.getSessionUuid();
            sessionSaved   = true;
        });
        sheet.show(getSupportFragmentManager(), "history");
    }

    // ─── Utilitaires ──────────────────────────────────────────────────────────

    private void showTyping(boolean show) {
        layoutTyping.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}