package com.ninotech.fabi.controleur.fragment;

import static android.view.View.GONE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.controleur.activity.SearchActivity;
import com.ninotech.fabi.controleur.adapter.ChatAdapter;
import com.ninotech.fabi.model.data.Arm;
import com.ninotech.fabi.model.data.Chat;
import com.ninotech.fabi.R;
import com.ninotech.fabi.model.data.Server;
import com.ninotech.fabi.model.table.Session;
import com.ninotech.fabi.model.table.UserTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import kotlinx.serialization.json.JsonObject;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatBotFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_bot, container, false);
        mRecyclerView = view.findViewById(R.id.recylerDisscution);
        mEnvoie = view.findViewById(R.id.envoyer);
        mEditText = view.findViewById(R.id.messageNotif);
        mSession = new Session(view.getContext());
        mList = new ArrayList<>();
        mArm = new Arm();
        BroadcastReceiver receiverFabiolaBookAdapter = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("ACTION_RECOVER_BOOK".equals(intent.getAction())) {
                    mArm.setTitle(intent.getStringExtra("titleBook"));
                    mArm.setId(intent.getStringExtra("idBook"));
                    mEditText.setText(mEditText.getText().toString().replace('#',' ') + "\"" + mArm.getTitle() + "\" ");
                    mEditText.setSelection(mEditText.getText().length());
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().registerReceiver(receiverFabiolaBookAdapter, new IntentFilter("ACTION_RECOVER_BOOK"),Context.RECEIVER_EXPORTED);
        }
        BroadcastReceiver receiverGoToEndChat = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("GO_TO_END_CHAT".equals(intent.getAction())) {
                    mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount()-1);
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().registerReceiver(receiverGoToEndChat, new IntentFilter("GO_TO_END_CHAT"),Context.RECEIVER_EXPORTED);
        }
        mList.add(new Chat("fabiola.png","duna","Salut que puis-je faire pour vous ?",true));
        mChatAdapter = new ChatAdapter(mList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mChatAdapter);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty())
                {
                    switch (s.charAt(s.length()-1))
                    {
                        case '#':
                            Intent searchIntent = new Intent(getContext(), SearchActivity.class);
                            searchIntent.putExtra("search_key","FABIOLA_BOOK");
                            startActivity(searchIntent);
                            break;
                        case '@':
                            Intent searchBookReservationIntent = new Intent(getContext(), SearchActivity.class);
                            searchBookReservationIntent.putExtra("search_key","FABIOLA_BOOK_RESERVATION");
                            startActivity(searchBookReservationIntent);
                            break;
                    }
                }
            }
        });
        mEnvoie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserTable userTable = new UserTable(getContext());
                Cursor userCursor = userTable.getData(mSession.getIdNumber());
                userCursor.moveToFirst();
                mRequete = mEditText.getText().toString();
                mEditText.setText("");
                mList.add(new Chat("moi.png",userCursor.getString(1),mRequete,false));
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mRecyclerView.setAdapter(mChatAdapter);
                mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount()-1);
                if(mArm.containsReservation(mRequete))
                {
                    mArm.setNumberOfDays(mArm.extractDuration(mRequete));
                    if(mArm.getNumberOfDays() <= 5)
                    {
                        Reservation reservationSyn = new Reservation();
                        reservationSyn.execute(Server.getIpServerAndroid(getContext()) + "Reservation.php",mSession.getIdNumber(),mArm.getId(),String.valueOf(mArm.getNumberOfDays()));
                    }
                    else
                    {
                        if(mArm.containsJournee(mRequete))
                        {
                            mArm.setNumberOfDays(1);
                            Reservation reservationSyn = new Reservation();
                            reservationSyn.execute(Server.getIpServerAndroid(getContext()) + "Reservation.php",mSession.getIdNumber(),mArm.getId(),String.valueOf(mArm.getNumberOfDays()));
                        }
                        else
                        {
                            mList.add(new Chat("fabiola.png","duna","je suis désolé la politique de la bibliothèque permet aux utilisateurs d'emprunter un livre pour une durée maximale de 5 jours. Merci pour votre compréhension.",true));
                            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            mRecyclerView.setAdapter(mChatAdapter);
                            mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount()-1);
                        }
                    }
                }
                else
                {
                    EdunaSyn edunaSyn = new EdunaSyn();
                    edunaSyn.execute(getString(R.string.ip_eduna) + ":2222/fabi/api/ask_eduna.php",mSession.getIdNumber(),mRequete);
                }
//                CallOpenAi callOpenAi = new CallOpenAi();
//                callOpenAi.execute("http://192.168.43.1:2222/fabi/android/callOpenAi.php");
            }
        });
        return view;
    }

    private class Reservation extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("idBook",params[2])
                        .addFormDataPart("numberOfDay",params[3])
                        .build();
                Request request = new Request.Builder()
                        .url(params[0])
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    assert response.body() != null;
                    return response.body().string();
                }catch (IOException e)
                {
                    Log.e("ReservationFabiola",e.getMessage());
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                return null;
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonData){
            if(jsonData != null)
            {
                if(jsonData.equals("true")) {
                    if (mArm.getNumberOfDays() != -1)
                        mList.add(new Chat("fabiola.png", "duna", "Votre réservation du livre \"" + mArm.getTitle() + "\" pour une durée de " + String.valueOf(mArm.getNumberOfDays()) + " jours a été enregistrée avec succès. Merci pour votre demande !", true));
                    else
                    {
                        if(mArm.getNumberOfDays() == 0)
                            mList.add(new Chat("fabiola.png", "duna", "Votre réservation sur place du livre \"" + mArm.getTitle() + "\" a été enregistrée avec succès. Merci pour votre demande !", true));
                        else
                            mList.add(new Chat("fabiola.png", "duna", "Votre réservation du livre \"" + mArm.getTitle() + "\"  pour une journée  a été enregistrée avec succès. Merci pour votre demande !", true));
                    }
                }
                else
                {
                    mList.add(new Chat("fabiola.png","duna","je suis désolé vous avez déjà effectué une réservation sur le livre \"" + mArm.getTitle() + "\". Merci pour votre demande !",true));
                }
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mRecyclerView.setAdapter(mChatAdapter);
                mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount()-1);
            }

        }
    }

    private class EdunaSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("request",params[2])
                        .build();
                Request request = new Request.Builder()
                        .url(params[0])
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    assert response.body() != null;
                    return response.body().string();
                }catch (IOException e)
                {
                    Log.e("ReservationFabiola",e.getMessage());
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                return null;
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonData){
            if(jsonData != null)
            {
                try {
                    Intent intent = new Intent("RESPONSE_CHAT_OK");
                    requireContext().sendBroadcast(intent);

                    JSONObject jsonObject = new JSONObject(jsonData);
                    mList.add(new Chat("fabiola.png","duna",jsonObject.getString("response"),true));
     //               mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//                    mRecyclerView.setAdapter(mChatAdapter);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            else
                Toast.makeText(getContext(), "Null", Toast.LENGTH_SHORT).show();

        }
    }
    private RecyclerView mRecyclerView;
    private ChatAdapter mChatAdapter;
    private ArrayList<Chat> mList;
    private Button mEnvoie;
    private EditText mEditText;
    private Button mMemorisation;
    private Session mSession;
    private String mRequete;
    private Arm mArm;
}