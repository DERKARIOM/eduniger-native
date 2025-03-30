package com.ninotech.fabi.controleur.adapter;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ninotech.fabi.controleur.activity.AccountActivity;
import com.ninotech.fabi.controleur.activity.ChangePasswordActivity;
import com.ninotech.fabi.controleur.activity.LoginActivity;
import com.ninotech.fabi.controleur.dialog.EvaluezVousDialog;
import com.ninotech.fabi.controleur.activity.FingerPrintActivity;
import com.ninotech.fabi.controleur.activity.InfosActivity;
import com.ninotech.fabi.controleur.activity.SuggestionActivity;
import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.dialog.OneEditTextDialog;
import com.ninotech.fabi.controleur.dialog.SimpleOkDialog;
import com.ninotech.fabi.model.data.Lock;
import com.ninotech.fabi.model.data.PasswordUtil;
import com.ninotech.fabi.model.data.Setting;
import com.ninotech.fabi.model.data.Update;
import com.ninotech.fabi.model.table.Session;
import com.ninotech.fabi.controleur.dialog.SucceSuggesionDialog;
import com.ninotech.fabi.model.table.UserTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.MyViewHolder> {
    List<Setting> mSettings;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public SettingAdapter(List<Setting> settings) {
        mSettings = settings;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_setting,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Setting item = mSettings.get(position);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mSettings.get(position));

    }
    @Override
    public int getItemCount() {
        return mSettings.size();
    }

    public Setting getItem(int position) {
        return mSettings.get(position);
    }

    public void Remove(int position){
        mSettings.remove(position);
        notifyItemRemoved(position);
    }
    public void filterList(ArrayList<Setting> filteredList) {
        mSettings = filteredList;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private ImageView mIconeImageView;
        private TextView mTitleTextView;
        private TextView mSubTitleTextView;
        private Session mSession;
        private String mNote;
        private String mObservation;
        private UserTable mUserTable;
        private Update mUpdate;
        private OneEditTextDialog mOneEditTextDialog;
        private ProgressBar mProgressBarDialog;
        private TextView mOkTextView;
        MyViewHolder(View itemView){
            super(itemView);
            mIconeImageView = (ImageView)itemView.findViewById(R.id.image_view_adapter_setting_icon);
            mTitleTextView = (TextView)itemView.findViewById(R.id.text_view_adapter_setting_title);
            mSubTitleTextView = (TextView)itemView.findViewById(R.id.text_view_adapter_setting_sub_title) ;
            mSession = new Session(itemView.getContext());
            mUserTable = new UserTable(itemView.getContext());
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }
        void display(Setting setting){
            mIconeImageView.setImageResource(setting.getIcone());
            mTitleTextView.setText(setting.getTitle());
            if(!setting.getTitle().equals("Compte"))
            {
                mIconeImageView.setImageResource(setting.getIcone());
            }
            else
            {
                UserTable userTable = new UserTable(itemView.getContext());
                try {
                    byte[] photoByte = userTable.getPhoto(mSession.getIdNumber());
                    if(photoByte != null)
                    {
                        Glide.with(itemView.getContext())
                                .load(photoByte)
                                .apply(RequestOptions.circleCropTransform())
                                .into(mIconeImageView);
                    }else
                    {
                        mIconeImageView.setImageResource(R.drawable.user);
                    }
                }catch (Exception e)
                {
                    Log.e("errorPhoto",e.getMessage());
                }
            }
            if (setting.getSubTitle() != null)
            {
                mSubTitleTextView.setVisibility(View.VISIBLE);
                mSubTitleTextView.setText(setting.getSubTitle());
            }
            else
            {
                mSubTitleTextView.setVisibility(View.GONE);
            }
            if(!setting.getTitle().equals("Supprimer le compte"))
            {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (setting.getTitle())
                        {
                            case "Compte":
                                Intent accountIntent = new Intent(itemView.getContext(), AccountActivity.class);
                               itemView.getContext().startActivity(accountIntent);
                                break;
                            case "Envoyer une suggestion":
                                Intent suggestion = new Intent(itemView.getContext(), SuggestionActivity.class);
                                itemView.getContext().startActivity(suggestion);
                                break;
                            case "Infos de l'application":
                                Intent infoAppIntent = new Intent(itemView.getContext(), InfosActivity.class);
                               itemView.getContext().startActivity(infoAppIntent);
//                            Intent infos = new Intent(itemView.getContext(), InfosActivity.class);
//                            itemView.getContext().startActivity(infos);
                                break;
                            case "Evaluez-nous":
                                oneEditTextDialog(setting.getTitle(),null, InputType.TYPE_CLASS_NUMBER,"Votre note /20");
//                            EvaluezDialog();
                                break;
                            case "Quoi de neuf ?":
                                String newUrl = "https://telesafe.net";
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(newUrl));
                                if (intent.resolveActivity(itemView.getContext().getPackageManager()) != null) {
                                    itemView.getContext().startActivity(intent);
                                }
                                break;
                            case "Comment ça marche":
                                String ccmUrl = "https://www.youtube.com/playlist?list=PL9OgjL2isuO_lWGCR9rem2qKig6m8CPzK";
                                Intent ccmIntent = new Intent(Intent.ACTION_VIEW);
                                ccmIntent.setData(Uri.parse(ccmUrl));
                                if (ccmIntent.resolveActivity(itemView.getContext().getPackageManager()) != null) {
                                    itemView.getContext().startActivity(ccmIntent);
                                }
                                break;
                            case "Empreinte digitale":
                                Intent emreinte = new Intent(itemView.getContext(), FingerPrintActivity.class);
                                itemView.getContext().startActivity(emreinte);
                                break;
                            case "Nom":
                                oneEditTextDialog(setting.getTitle(),setting.getSubTitle(),InputType.TYPE_CLASS_TEXT,"Entrer votre nom");
                                break;
                            case "Prénom":
                                oneEditTextDialog(setting.getTitle(),setting.getSubTitle(),InputType.TYPE_CLASS_TEXT,"Entrer votre Prénom");
                                break;
                            case "Modifier le mot de passe":
                                oneEditTextDialog(setting.getTitle(),setting.getSubTitle(),InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,itemView.getContext().getString(R.string.edit_text_hint_password));
                                break;
                            case "Identifiant de réseau social":
                                // Crée un Intent pour lancer les paramètres des comptes
                                Intent intentYoutube = new Intent(Settings.ACTION_SYNC_SETTINGS);
                                // Si tu veux directement ouvrir les paramètres du compte spécifique, utilise :
                                // Intent intent = new Intent(Settings.ACTION_ACCOUNT_SYNC_SETTINGS);
                                // Vérifie si l'activité peut être lancée pour éviter les plantages
                                if (intentYoutube.resolveActivity(itemView.getContext().getPackageManager()) != null) {
                                    itemView.getContext().startActivity(intentYoutube);
                                }
                                break;
                            case "Supprimer le compte":
                                Toast.makeText(itemView.getContext(), "Supprimer le compte", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                oneEditTextDialog("Email",setting.getTitle(),InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,itemView.getContext().getString(R.string.edit_text_hint_email));
                                break;
                        }
                    }
                });
            }
            else {
                itemView.setOnClickListener(v -> {
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Confirmation")
                            .setMessage("Êtes-vous sûr de vouloir supprimer définitivement votre compte ?")
                            .setPositiveButton("Oui", (dialog, which) -> {
                                // Passez à l'Activity suivante après l'acceptation
                                Log.e("Confirmation","no");
                                DeleteAccountSyn deleteAccountSyn = new DeleteAccountSyn();
                                deleteAccountSyn.execute(itemView.getContext().getString(R.string.ip_server_android) + "DeleteAccountSyn.php",mSession.getIdNumber());
                            })
                            .setNegativeButton("Non", (dialog, which) -> {
                                Log.e("Confirmation","no");
                            })
                            .show();
                });
            }
        }
        private void updateDate(String idNumber , String column , String newValues)
        {
            UpdateSyn updateSyn = new UpdateSyn();
            updateSyn.execute(itemView.getContext().getString(R.string.ip_server_android) + "UpdateSyn.php",idNumber,column,newValues);
        }
        private void oneEditTextDialog(String label , String message,int inputType , String hint){
            mOneEditTextDialog = new OneEditTextDialog((Activity) itemView.getContext());
            Objects.requireNonNull(mOneEditTextDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mOneEditTextDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            TextView labelTextView = mOneEditTextDialog.findViewById(R.id.text_view_dialog_edit_text_one_label);
            TextView cancelTextView = mOneEditTextDialog.findViewById(R.id.text_view_dialog_edit_text_one_cancel);
            mOkTextView = mOneEditTextDialog.findViewById(R.id.text_view_dialog_edit_text_one_ok);
            EditText editText = mOneEditTextDialog.findViewById(R.id.edit_text_dialog_edit_text_one);
            mProgressBarDialog = mOneEditTextDialog.findViewById(R.id.progress_bar_dialog_edit_text_one);
            editText.setText(message);
            labelTextView.setText(label);
            editText.setInputType(inputType);
            editText.setHint(hint);
            cancelTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOneEditTextDialog.cancel();
                }
            });
            mOkTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!editText.getText().toString().isEmpty())
                    {
                        mProgressBarDialog.setVisibility(View.VISIBLE);
                        mOkTextView.setVisibility(View.INVISIBLE);
                        switch (label)
                        {
                            case "Nom":
                                mUpdate = new Update(mSession.getIdNumber(),"nameUser",editText.getText().toString());
                                updateDate(mSession.getIdNumber(),"name",editText.getText().toString());
                                break;
                            case "Prénom":
                                mUpdate = new Update(mSession.getIdNumber(),"firstNameUser",editText.getText().toString());
                                updateDate(mSession.getIdNumber(),"firstName",editText.getText().toString());
                                break;
                            case "Modifier le mot de passe":
                                mUpdate = new Update(mSession.getIdNumber(),"password", PasswordUtil.hashPassword(editText.getText().toString()));
                                updateDate(mSession.getIdNumber(),"password",PasswordUtil.hashPassword(editText.getText().toString()));
                                break;
                            case "Email":
                                mUpdate = new Update(mSession.getIdNumber(),"emailUser",editText.getText().toString());
                                updateDate(mSession.getIdNumber(),"email",editText.getText().toString());
                                break;
                            case "Evaluez-nous":
                                GradeSyn gradeSyn = new GradeSyn();
                                gradeSyn.execute(itemView.getContext().getString(R.string.ip_server_android) + "GradeSyn.php",mSession.getIdNumber(),editText.getText().toString());
                                break;
                        }
                    }
                    else
                    {
                        editText.setBackground(itemView.getContext().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    }
                }
            });
            mOneEditTextDialog.build();
        }
        private class UpdateSyn extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {

                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("idNumber", params[1])
                            .addFormDataPart("column", params[2])
                            .addFormDataPart("newValues", params[3])
                            .build();
                    Request request = new Request.Builder()
                            .url(params[0])
                            .post(requestBody)
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        assert response.body() != null;
                        return response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    Log.e("errOkhttp", Objects.requireNonNull(e.getMessage()));
                }
                return null;
            }

            @Override
            protected void onPostExecute(String response) {
                // Log.e("JsonTest",response);
                if(response != null)
                {
                    if(response.equals("true"))
                    {
                        mProgressBarDialog.setVisibility(View.INVISIBLE);
                        mOkTextView.setVisibility(View.VISIBLE);
                        if(mUserTable.update(mUpdate.getIdNumber(),mUpdate.getColumn(),mUpdate.getNewValues()))
                        {
                            if(!mUpdate.getColumn().equals("emailUs"))
                                mSubTitleTextView.setText(mUpdate.getNewValues());
                            else
                                mTitleTextView.setText(mUpdate.getNewValues());
                            mOneEditTextDialog.cancel();
                        }

                    }
                    else
                    {
                        mProgressBarDialog.setVisibility(View.VISIBLE);
                        mOkTextView.setVisibility(View.INVISIBLE);
                        Toast.makeText(itemView.getContext(),itemView.getContext().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        private class DeleteAccountSyn extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {

                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("idNumber", params[1])
                            .build();
                    Request request = new Request.Builder()
                            .url(params[0])
                            .post(requestBody)
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        assert response.body() != null;
                        return response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    Log.e("errOkhttp", Objects.requireNonNull(e.getMessage()));
                }
                return null;
            }

            @Override
            protected void onPostExecute(String response) {
                // Log.e("JsonTest",response);
                if(response != null)
                {
                    Toast.makeText(itemView.getContext(), response, Toast.LENGTH_SHORT).show();
                    if(response.equals("true"))
                    {
                        mSession.delete();
                        mUserTable.delete();
                        Lock.savePass(itemView.getContext(),0);
                        Intent loginIntent = new Intent(itemView.getContext(), LoginActivity.class);
                        itemView.getContext().startActivity(loginIntent);
                        ((Activity)itemView.getContext()).finish();
                    }
                }
            }

        }

        public boolean isAppInstalled(String packageName) {
            PackageManager packageManager = itemView.getContext().getPackageManager();
            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
                return appInfo != null;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }
        private class GradeSyn extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {

                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("idNumber", params[1])
                            .addFormDataPart("values", params[2])
                            .build();
                    Request request = new Request.Builder()
                            .url(params[0])
                            .post(requestBody)
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        assert response.body() != null;
                        return response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    Log.e("errOkhttp", Objects.requireNonNull(e.getMessage()));
                }
                return null;
            }

            @Override
            protected void onPostExecute(String response) {
                // Log.e("JsonTest",response);
                if (response != null) {
                    if(response.equals("true"))
                    {
                        mOneEditTextDialog.cancel();
                        simpleDialogOk(R.drawable.vector_emoji_success,itemView.getContext().getString(R.string.success),"Nous tenons à vous exprimer notre sincère gratitude pour avoir pris le temps de noter TeleSafe");
                    }
                    else
                    {
                        mOneEditTextDialog.cancel();
                        simpleDialogOk(R.drawable.vector_sorry,"Desoler","Nous tenons à vous informer qu'il n'est possible de laisser qu'une seule évaluation par utilisateur. Cette limitation est en place pour garantir l'équité et la transparence dans les évaluations de l'application.");
                    }
                }

            }
        }
        private void simpleDialogOk(int ico , String title , String message){
            SimpleOkDialog simpleOkDialog = new SimpleOkDialog((Activity) itemView.getContext());
            Objects.requireNonNull(simpleOkDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            simpleOkDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            ImageView icoImageView = simpleOkDialog.findViewById(R.id.image_view_dialog_simple_ok_icon);
            TextView titleTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok_title);
            TextView messageTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok_message);
            TextView okTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok);
            icoImageView.setImageResource(ico);
            titleTextView.setText(title);
            messageTextView.setText(message);
            okTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    simpleOkDialog.cancel();
                }
            });
            simpleOkDialog.build();
        }
    }

}
