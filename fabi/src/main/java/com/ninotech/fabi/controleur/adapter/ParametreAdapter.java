package com.ninotech.fabi.controleur.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.controleur.activity.ChangePasswordActivity;
import com.ninotech.fabi.controleur.dialog.EvaluezVousDialog;
import com.ninotech.fabi.controleur.activity.FingerPrintActivity;
import com.ninotech.fabi.controleur.activity.InfosActivity;
import com.ninotech.fabi.controleur.activity.SuggestionActivity;
import com.ninotech.fabi.R;
import com.ninotech.fabi.model.table.Session;
import com.ninotech.fabi.controleur.dialog.SucceSuggesionDialog;
import com.ninotech.fabi.model.data.Parametre;

import java.io.IOException;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ParametreAdapter extends RecyclerView.Adapter<ParametreAdapter.MyViewHolder> {
    List<Parametre> mListParametre;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public ParametreAdapter(List<Parametre> listParametre) {
        mListParametre = listParametre;
    }
    @Override
    public ParametreAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_parametre,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Parametre item = mListParametre.get(position);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mListParametre.get(position));

    }
    @Override
    public int getItemCount() {
        return mListParametre.size();
    }

    public Parametre getItem(int position) {
        return mListParametre.get(position);
    }

    public void Remove(int position){
        mListParametre.remove(position);
        notifyItemRemoved(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private ImageView mIcone;
        private TextView mTitre;
        private TextView mSousTitre;
        private Session mSession;
        private String mNote;
        private String mObservation;
        EvaluezVousDialog mEvaluez = new EvaluezVousDialog((Activity) itemView.getContext());
        EditText notre_note = mEvaluez.findViewById(R.id.notre_note);
        EditText notre_observation = mEvaluez.findViewById(R.id.notre_observation);
        Button notre_envoi = mEvaluez.findViewById(R.id.notre_envoi);
        TextView err_evaluez = mEvaluez.findViewById(R.id.err_evaluez);
        ProgressBar circulaire = mEvaluez.findViewById(R.id.progress_circularEvaluez);
        MyViewHolder(View itemView){
            super(itemView);
            mIcone = (ImageView)itemView.findViewById(R.id.icon);
            mTitre = (TextView)itemView.findViewById(R.id.titre);
            mSousTitre = (TextView)itemView.findViewById(R.id.sousTitre);
            mSession = new Session(itemView.getContext());
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }
        void display(Parametre parametre){
            mIcone.setImageResource(parametre.getIcone());
            mTitre.setText(parametre.getTritre());
            mSousTitre.setText(parametre.getSousTritre());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (parametre.getTritre())
                    {
                        case "Compte":
                            Intent password = new Intent(itemView.getContext(),ChangePasswordActivity.class);
                            itemView.getContext().startActivity(password);
                            break;
                        case "Envoyer une suggestion":
                            Intent suggestion = new Intent(itemView.getContext(), SuggestionActivity.class);
                            itemView.getContext().startActivity(suggestion);
                            break;
                        case "Infos de l'application":
                            Intent infos = new Intent(itemView.getContext(), InfosActivity.class);
                            itemView.getContext().startActivity(infos);
                            break;
                        case "Evaluez-nous":
                            EvaluezDialog();
                            break;
                        case "Contactez-nous":
                            makePhoneCall();
                            break;
                        case "Empreinte digitale":
                            Intent emreinte = new Intent(itemView.getContext(), FingerPrintActivity.class);
                            itemView.getContext().startActivity(emreinte);
                            break;
                    }
                }
            });
        }
        public void EvaluezDialog() {
            mEvaluez.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mEvaluez.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

            notre_envoi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(notre_note.getText().toString().equals(""))
                    {
                        notre_note.setBackground(view.getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                        err_evaluez.setText("Votre note svp");
                    }
                    else
                    {
                        circulaire.setVisibility(View.VISIBLE);
                        notre_envoi.setText("");
                        Http http = new Http();
                        http.execute("http://192.168.43.1:2222/android/evaluez_nous.php",mSession.getMatricule(),notre_note.getText().toString(),notre_observation.getText().toString());
                    }
                }
            });
            mEvaluez.build();
        }
        private class Http extends AsyncTask<String,Void,String> {
            @Override
            protected String doInBackground(String... params) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("matricule",params[1])
                            .addFormDataPart("note",params[2])
                            .addFormDataPart("observation",params[3])
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
                        err_evaluez.setText("Aucune connexion");
                        circulaire.setVisibility(View.INVISIBLE);
                        notre_envoi.setText("Envoyer");
                        // Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }catch (Exception e)
                {
                    err_evaluez.setText("Aucune connexion");
                    circulaire.setVisibility(View.INVISIBLE);
                    notre_envoi.setText("Envoyer");
                    //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return null;
            }
            @Override
            protected void onPostExecute(String response){
                //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
                if(response != null)
                {
                    if(response.equals("true"))
                    {
                        mEvaluez.cancel();
                        notre_note.setText("");
                        notre_observation.setText("");
                        circulaire.setVisibility(View.INVISIBLE);
                        notre_envoi.setText("Envoyer");
                        SucceSuggestionDialog(R.drawable.vector_emoji_succes,"Succès","Nous tenons à vous exprimer notre sincère gratitude pour avoir pris le temps de noter fastpv");
                    }
                    else
                    {
                        mEvaluez.cancel();
                        notre_note.setText("");
                        notre_observation.setText("");
                        circulaire.setVisibility(View.INVISIBLE);
                        notre_envoi.setText("Envoyer");
                        SucceSuggestionDialog(R.drawable.vector_purple_200_desole,"Désolé","Nous tenons à vous informer qu'il n'est possible de laisser qu'une seule évaluation par utilisateur. Cette limitation est en place pour garantir l'équité et la transparence dans les évaluations de l'application. ");
                    }

                }
                else
                {
                    err_evaluez.setText("Aucune connexion");
                    circulaire.setVisibility(View.INVISIBLE);
                    notre_envoi.setText("Envoyer");
                }
            }
        }
        private void SucceSuggestionDialog(int id_ico , String titre , String messager){
            SucceSuggesionDialog succeSuggesionDialog = new SucceSuggesionDialog((Activity) itemView.getContext());
            succeSuggesionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            succeSuggesionDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            ImageView ico = succeSuggesionDialog.findViewById(R.id.popo_ico);
            TextView title = succeSuggesionDialog.findViewById(R.id.popo_titre);
            TextView message = succeSuggesionDialog.findViewById(R.id.popo_message);
            ico.setImageResource(id_ico);
            title.setText(titre);
            message.setText(messager);
            TextView ok = succeSuggesionDialog.findViewById(R.id.ok);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    succeSuggesionDialog.cancel();
                }
            });
            succeSuggesionDialog.build();
        }
        private void makePhoneCall() {
            String phoneNumber = "+22794961793";

            if (ContextCompat.checkSelfPermission(itemView.getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) itemView.getContext(), new String[]{Manifest.permission.CALL_PHONE},1);
            } else {
                String dial = "tel:" + phoneNumber;
                itemView.getContext().startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }
        }
    }

}
