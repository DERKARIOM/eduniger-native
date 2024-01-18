package com.ninotech.fabi.controleur.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.controleur.adapter.ChatAdapter;
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

public class DiscussionFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discussion, container, false);
        mRecyclerView = view.findViewById(R.id.recylerDisscution);
        mEnvoie = view.findViewById(R.id.envoyer);
        mEditText = view.findViewById(R.id.messageNotif);
        mSession = new Session(view.getContext());
        mList = new ArrayList<>();
        mList.add(new Chat("fabiola.png","abiola","Salut que puis-je faire pour vous ?",true));
//        mList.add(new Disscution(R.drawable.moi,"Bachir Abdoul Kader","D'où vient tu ?"));
//        mList.add(new Disscution(R.drawable.fastia,"FASTIA","Je suis une intelligence artificielle créée par NinoTech. Je n'ai pas de lieu physique d'où je viens, car je suis un programme informatique conçu pour vous aider et répondre à vos questions. Comment puis-je vous assister aujourd'hui ?"));
        mChatAdapter = new ChatAdapter(mList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mChatAdapter);
        mEnvoie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRequete = mEditText.getText().toString();
                mEditText.setText("");
                mList.add(new Chat("moi.png","Derkariom",mEditText.getText().toString(),false));
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mRecyclerView.setAdapter(mChatAdapter);
                mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount()-1);
                CallOpenAi callOpenAi = new CallOpenAi();
                callOpenAi.execute("http://192.168.43.1:2222/fabi/android/callOpenAi.php");
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
                    .addFormDataPart("matricule",mSession.getMatricule())
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
            //Toast.makeText(getContext(),response, Toast.LENGTH_SHORT).show();

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
    private RecyclerView mRecyclerView;
    private ChatAdapter mChatAdapter;
    private ArrayList<Chat> mList;
    private Button mEnvoie;
    private EditText mEditText;
    private Button mMemorisation;
    private Session mSession;
    private String mRequete;
}