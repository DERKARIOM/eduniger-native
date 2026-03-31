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
import com.ninotech.eduniger.controleur.adapter.MessageAdapter;
import com.ninotech.eduniger.controleur.adapter.StatusBarAdapter;
import com.ninotech.eduniger.model.data.Message;

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
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatAiActivity extends AppCompatActivity {

    // ─── Constantes ───────────────────────────────────────────────────────────
    private static final String API_URL    = "http://78.46.46.154/eduniger/ai/eduna_unified.php";
    private static final String ACTION     = "ask_about_book";
    // Remplacez par le vrai id_number de l'utilisateur connecté
    private static final String ID_NUMBER  = "94961793";

    // ─── Vues ─────────────────────────────────────────────────────────────────
    private RecyclerView   recyclerView;
    private EditText       etMessage;
    private ImageButton    btnSend;
    private View           layoutTyping;   // indicateur « ... »

    // ─── Données ──────────────────────────────────────────────────────────────
    private MessageAdapter adapter;
    private List<Message>  messages = new ArrayList<>();
    private OkHttpClient   httpClient;
    private String         sessionId;      // conservé entre les envois

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_ai);

        httpClient = new OkHttpClient();
        sessionId  = generateSessionId();

        initViews();
        setupRecyclerView();
        setupListeners();

        // Message de bienvenue local (optionnel)
        addBotMessage("Bonjour ! Comment puis-je t'aider aujourd'hui ?");
    }

    // ─── Initialisation ───────────────────────────────────────────────────────

    private void initViews() {
        new StatusBarAdapter(getApplicationContext(),getWindow());
        recyclerView  = findViewById(R.id.recyclerViewMessages);
        etMessage     = findViewById(R.id.etMessage);
        btnSend       = findViewById(R.id.btnSend);
        layoutTyping  = findViewById(R.id.layoutTyping);
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(messages);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
    }

    // ─── Envoi d'un message ───────────────────────────────────────────────────

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        // 1. Afficher le message utilisateur
        addUserMessage(text);
        etMessage.setText("");

        // 2. Afficher l'indicateur de frappe
        showTyping(true);

        // 3. Appel réseau (thread background via OkHttp)
        RequestBody body = new FormBody.Builder()
                .add("id_number", ID_NUMBER)
                .add("request",   text)
                .add("action",    ACTION)
                .build();

        // Si on a déjà un session_id, on l'envoie pour garder le contexte
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
                            // Mémoriser le session_id retourné
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

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void addUserMessage(String text) {
        messages.add(new Message(text, Message.TYPE_USER));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
    }

    private void addBotMessage(String text) {
        messages.add(new Message(text, Message.TYPE_BOT));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
    }

    private void showTyping(boolean show) {
        layoutTyping.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}