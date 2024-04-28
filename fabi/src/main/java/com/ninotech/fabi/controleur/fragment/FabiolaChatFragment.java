package com.ninotech.fabi.controleur.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
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

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.controleur.activity.SearchActivity;
import com.ninotech.fabi.controleur.adapter.ChatAdapter;
import com.ninotech.fabi.model.data.Arm;
import com.ninotech.fabi.model.data.Chat;
import com.ninotech.fabi.R;
import com.ninotech.fabi.model.table.Session;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FabiolaChatFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fabiola_chat, container, false);
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
        getContext().registerReceiver(receiverFabiolaBookAdapter, new IntentFilter("ACTION_RECOVER_BOOK"));
        mList.add(new Chat("fabiola.png","abiola","Salut que puis-je faire pour vous ?",true));
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
                    }
                }
            }
        });
        mEnvoie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRequete = mEditText.getText().toString();
                mEditText.setText("");
                mList.add(new Chat("moi.png","Derkariom",mRequete,false));
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mRecyclerView.setAdapter(mChatAdapter);
                mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount()-1);
                if(mArm.containsReservation(mRequete))
                {
                    mArm.setNumberOfDays(mArm.extractDuration(mRequete));
                    Reservation reservationSyn = new Reservation();
                    reservationSyn.execute(getString(R.string.ip_server_android) + "Reservation.php",mSession.getIdNumber(),mArm.getId(),String.valueOf(mArm.getNumberOfDays()));
                }
//                CallOpenAi callOpenAi = new CallOpenAi();
//                callOpenAi.execute("http://192.168.43.1:2222/fabi/android/callOpenAi.php");
            }
        });
        return view;
    }
    private class CallOpenAi extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("matricule",mSession.getIdNumber())
                    .addFormDataPart("message",mRequete)
                    .build();
            Request request = new Request.Builder()
                    .url(params[0])
                    .post(requestBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            }catch (IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String response){
            Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
            if(response != null)
            {
                Log.e("resChat",response);
                mList.add(new Chat("fabiola.png","abila",response,true));
            }
            else
            {
                mList.add(new Chat("fabiola.png","abila","Aucune connexion",true));
            }
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mRecyclerView.setAdapter(mChatAdapter);
            mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount()-1);
        }
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
                if(jsonData.equals("true"))
                {
                    mList.add(new Chat("fabiola.png","abiola","Votre réservation du livre \"" + mArm.getTitle() + "\" pour une durée de " + String.valueOf(mArm.getNumberOfDays())  + " jours a été enregistrée avec succès. Merci pour votre demande !",true));
                }
                else
                {
                    mList.add(new Chat("fabiola.png","abiola","Je suis désolé il semble que le livre n'existe dans Fabi. Merci pour votre demande !",true));
                }
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mRecyclerView.setAdapter(mChatAdapter);
                mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount()-1);
            }

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