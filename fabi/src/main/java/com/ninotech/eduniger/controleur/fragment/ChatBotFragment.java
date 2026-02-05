package com.ninotech.eduniger.controleur.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.eduniger.controleur.activity.SearchActivity;
import com.ninotech.eduniger.controleur.adapter.ChatAdapter;
import com.ninotech.eduniger.model.data.Arm;
import com.ninotech.eduniger.model.data.Chat;
import com.ninotech.eduniger.R;
import com.ninotech.eduniger.model.data.Server;
import com.ninotech.eduniger.model.table.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatBotFragment extends Fragment {

    private static final String TAG = "ChatBotFragment";
    private static final String ACTION_RECOVER_BOOK = "ACTION_RECOVER_BOOK";
    private static final String ACTION_GO_TO_END_CHAT = "GO_TO_END_CHAT";
    private static final String ACTION_RESPONSE_CHAT_OK = "RESPONSE_CHAT_OK";
    private static final int MAX_RESERVATION_DAYS = 5;

    private RecyclerView mRecyclerView;
    private ChatAdapter mChatAdapter;
    private ArrayList<Chat> mList;
    private ImageButton mEnvoie;
    private EditText mEditText;
    private Session mSession;
    private Arm mArm;
    private LinearLayoutManager mLayoutManager;

    private BroadcastReceiver mReceiverBook;
    private BroadcastReceiver mReceiverScroll;
    private OkHttpClient mHttpClient;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_bot, container, false);

        initializeViews(view);
        initializeData();
        setupReceivers();
        setupRecyclerView();
        setupTextWatcher();
        setupSendButton();

        return view;
    }

    private void initializeViews(View view) {
        mRecyclerView = view.findViewById(R.id.recylerDisscution);
        mEnvoie = view.findViewById(R.id.envoyer);
        mEditText = view.findViewById(R.id.messageNotif);
    }

    private void initializeData() {
        mSession = new Session(requireContext());
        mList = new ArrayList<>();
        mArm = new Arm();
        mHttpClient = new OkHttpClient();
        mLayoutManager = new LinearLayoutManager(requireContext());

        // Message d'accueil initial
        mList.add(new Chat("eduna.png", "Eduna", "Salut que puis-je faire pour vous ?", true));
    }

    private void setupReceivers() {
        mReceiverBook = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_RECOVER_BOOK.equals(intent.getAction())) {
                    handleBookRecovery(intent);
                }
            }
        };

        mReceiverScroll = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_GO_TO_END_CHAT.equals(intent.getAction())) {
                    scrollToBottom();
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().registerReceiver(mReceiverBook,
                    new IntentFilter(ACTION_RECOVER_BOOK), Context.RECEIVER_EXPORTED);
            requireContext().registerReceiver(mReceiverScroll,
                    new IntentFilter(ACTION_GO_TO_END_CHAT), Context.RECEIVER_EXPORTED);
        }
    }

    private void setupRecyclerView() {
        mChatAdapter = new ChatAdapter(mList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mChatAdapter);
        mRecyclerView.setHasFixedSize(true);
    }

    private void setupTextWatcher() {
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                handleSpecialCharacters(s);
            }
        });
    }

    private void handleSpecialCharacters(Editable s) {
        if (TextUtils.isEmpty(s)) return;

        char lastChar = s.charAt(s.length() - 1);
        switch (lastChar) {
            case '#':
                launchSearchActivity("FABIOLA_BOOK");
                break;
            case '@':
                launchSearchActivity("FABIOLA_BOOK_RESERVATION");
                break;
        }
    }

    private void launchSearchActivity(String searchKey) {
        Intent searchIntent = new Intent(requireContext(), SearchActivity.class);
        searchIntent.putExtra("search_key", searchKey);
        startActivity(searchIntent);
    }

    private void handleBookRecovery(Intent intent) {
        String titleBook = intent.getStringExtra("titleBook");
        String idBook = intent.getStringExtra("idBook");

        if (titleBook != null && idBook != null) {
            mArm.setTitle(titleBook);
            mArm.setId(idBook);

            String currentText = mEditText.getText().toString().replace('#', ' ');
            String newText = currentText + "\"" + mArm.getTitle() + "\" ";
            mEditText.setText(newText);
            mEditText.setSelection(mEditText.getText().length());
        }
    }

    private void setupSendButton() {
        mEnvoie.setOnClickListener(v -> handleSendMessage());
    }

    private void handleSendMessage() {
        String message = mEditText.getText().toString().trim();
        if (TextUtils.isEmpty(message)) return;

        mEditText.setText("");
        addUserMessage(message);

        if (mArm.containsReservation(message)) {
            handleReservation(message);
        } else {
            sendToEduna(message);
        }
    }

    private void addUserMessage(String message) {
        mList.add(new Chat("moi.png", "Vous", message, false));
        updateRecyclerView();
    }

    private void addEdunaMessage(String message) {
        mList.add(new Chat("fabiola.png", "Eduna", message, true));
        updateRecyclerView();
    }

    private void updateRecyclerView() {
        if (mChatAdapter != null) {
            mChatAdapter.notifyItemInserted(mList.size() - 1);
            scrollToBottom();
        }
    }

    private void scrollToBottom() {
        if (mChatAdapter != null && mChatAdapter.getItemCount() > 0) {
            mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount() - 1);
        }
    }

    private void handleReservation(String message) {
        int numberOfDays = mArm.extractDuration(message);
        mArm.setNumberOfDays(numberOfDays);

        if (numberOfDays <= MAX_RESERVATION_DAYS) {
            executeReservation();
        } else if (mArm.containsJournee(message)) {
            mArm.setNumberOfDays(1);
            executeReservation();
        } else {
            addEdunaMessage("je suis désolé la politique de la bibliothèque permet aux utilisateurs " +
                    "d'emprunter un livre pour une durée maximale de 5 jours. Merci pour votre compréhension.");
        }
    }

    private void executeReservation() {
        String url = Server.getUrlApi(requireContext()) + "Reservation.php";
        new ReservationTask(this).execute(url, mSession.getIdNumber(),
                mArm.getId(), String.valueOf(mArm.getNumberOfDays()));
    }

    private void sendToEduna(String message) {
        String url = getString(R.string.ip_eduna) + "/fabi/api/ask_eduna.php";
        new EdunaTask(this).execute(url, mSession.getIdNumber(), message);
    }

    private void handleReservationResponse(String response) {
        if (response == null) return;

        String message;
        if ("true".equals(response)) {
            message = buildReservationSuccessMessage();
        } else {
            message = "je suis désolé vous avez déjà effectué une réservation sur le livre \"" +
                    mArm.getTitle() + "\". Merci pour votre demande !";
        }
        addEdunaMessage(message);
    }

    private String buildReservationSuccessMessage() {
        int days = mArm.getNumberOfDays();
        String bookTitle = mArm.getTitle();

        if (days == 0) {
            return "Votre réservation sur place du livre \"" + bookTitle +
                    "\" a été enregistrée avec succès. Merci pour votre demande !";
        } else if (days == 1) {
            return "Votre réservation du livre \"" + bookTitle +
                    "\" pour une journée a été enregistrée avec succès. Merci pour votre demande !";
        } else {
            return "Votre réservation du livre \"" + bookTitle + "\" pour une durée de " +
                    days + " jours a été enregistrée avec succès. Merci pour votre demande !";
        }
    }

    private void handleEdunaResponse(String jsonData) {
        sendBroadcast(ACTION_RESPONSE_CHAT_OK);

        if (jsonData != null) {
            try {
                JSONObject jsonObject = new JSONObject(jsonData);
                String response = jsonObject.getString("response");
                addEdunaMessage(response);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON response", e);
                addEdunaMessage("❌ Erreur de traitement de la réponse");
            }
        } else {
            addEdunaMessage("❌ Connexion perdue\n");
        }
    }

    private void sendBroadcast(String action) {
        Intent intent = new Intent(action);
        requireContext().sendBroadcast(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterReceivers();
    }

    private void unregisterReceivers() {
        try {
            if (mReceiverBook != null) {
                requireContext().unregisterReceiver(mReceiverBook);
            }
            if (mReceiverScroll != null) {
                requireContext().unregisterReceiver(mReceiverScroll);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Receiver already unregistered", e);
        }
    }

    // AsyncTask optimisé avec WeakReference pour éviter les fuites mémoire
    private static class ReservationTask extends AsyncTask<String, Void, String> {
        private final WeakReference<ChatBotFragment> fragmentRef;

        ReservationTask(ChatBotFragment fragment) {
            this.fragmentRef = new WeakReference<>(fragment);
        }

        @Override
        protected String doInBackground(String... params) {
            ChatBotFragment fragment = fragmentRef.get();
            if (fragment == null || fragment.mHttpClient == null) return null;

            try {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber", params[1])
                        .addFormDataPart("idBook", params[2])
                        .addFormDataPart("numberOfDay", params[3])
                        .build();

                Request request = new Request.Builder()
                        .url(params[0])
                        .post(requestBody)
                        .build();

                try (Response response = fragment.mHttpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        return response.body().string();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Reservation request failed", e);
                }
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in reservation", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            ChatBotFragment fragment = fragmentRef.get();
            if (fragment != null) {
                fragment.handleReservationResponse(result);
            }
        }
    }

    private static class EdunaTask extends AsyncTask<String, Void, String> {
        private final WeakReference<ChatBotFragment> fragmentRef;

        EdunaTask(ChatBotFragment fragment) {
            this.fragmentRef = new WeakReference<>(fragment);
        }

        @Override
        protected String doInBackground(String... params) {
            ChatBotFragment fragment = fragmentRef.get();
            if (fragment == null || fragment.mHttpClient == null) return null;

            try {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber", params[1])
                        .addFormDataPart("request", params[2])
                        .build();

                Request request = new Request.Builder()
                        .url(params[0])
                        .post(requestBody)
                        .build();

                try (Response response = fragment.mHttpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        return response.body().string();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Eduna request failed", e);
                }
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in Eduna request", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            ChatBotFragment fragment = fragmentRef.get();
            if (fragment != null) {
                fragment.handleEdunaResponse(result);
            }
        }
    }
}