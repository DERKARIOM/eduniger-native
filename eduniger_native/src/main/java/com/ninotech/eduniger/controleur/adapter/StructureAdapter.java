package com.ninotech.eduniger.controleur.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.activity.AddBookActivity;
import com.ninotech.eduniger.controleur.activity.RegisterAuthorActivity;
import com.ninotech.eduniger.controleur.activity.StructureActivity;
import com.ninotech.eduniger.controleur.animation.RoundedTransformation;
import com.ninotech.eduniger.controleur.dialog.SimpleOkDialog;
import com.ninotech.eduniger.controleur.dialog.StructDeleteDialog;
import com.ninotech.eduniger.model.data.PasswordUtil;
import com.ninotech.eduniger.model.data.Server;
import com.ninotech.eduniger.model.data.Structure;
import com.ninotech.eduniger.model.table.Session;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class StructureAdapter extends RecyclerView.Adapter<StructureAdapter.MyViewHolder> {
    List<Structure> mStructures;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public StructureAdapter(List<Structure> structures) {
        mStructures = structures;
    }
    @Override
    public StructureAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_structure,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Structure item = mStructures.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mStructures.get(position));

    }
    @Override
    public int getItemCount() {
        return mStructures.size();
    }

    public Structure getItem(int position) {
        return mStructures.get(position);
    }

    public void Remove(int position){
        mStructures.remove(position);
        notifyItemRemoved(position);
    }

    public void filterList(ArrayList<Structure> filteredList) {
        mStructures = filteredList;
        notifyDataSetChanged();
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private final ImageView mBlanketImageView;
        private final TextView mTitleTextView;
        private final TextView mBookNumberTextView;
        private final Button mAdhereButton;
        private Session mSession;
        MyViewHolder(View itemView){
            super(itemView);
            mBlanketImageView = itemView.findViewById(R.id.image_view_adapter_structure_cover);
            mTitleTextView = itemView.findViewById(R.id.text_view_adapter_structure_title);
            mBookNumberTextView = itemView.findViewById(R.id.image_view_adapter_structure_book_number);
            mAdhereButton = itemView.findViewById(R.id.button_adapter_structure);
            mSession = new Session(itemView.getContext());
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
        }
        void display(Structure structure){
            switch (structure.getId())
            {
                case "AddBook":
                    mBookNumberTextView.setText(structure.getBookNumber());
                    Animation pulseAnimImg = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.slide_down_up);
                    // Lancer l'animation automatiquement
                    mBlanketImageView.startAnimation(pulseAnimImg);
                    mAdhereButton.setText("Ajouter");
                    mBlanketImageView.setImageResource(R.drawable.add_auteurs);
                    break;
                case "RegisterAuthor":
                    mBookNumberTextView.setText(structure.getBookNumber());
                    Animation pulseAnimImg2 = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.slide_down_up);
                    // Lancer l'animation automatiquement
                    mBlanketImageView.startAnimation(pulseAnimImg2);
                    mAdhereButton.setText("S'inscrire");
                    mBlanketImageView.setImageResource(R.drawable.add_auteurs);
                    break;
                default:
                    mBookNumberTextView.setText(structure.getBookNumber() + " Livres");
                    Picasso.get()
                            .load(Server.getUrlServer(itemView.getContext()) + "ressources/cover/" + structure.getCover())
                            .placeholder(R.drawable.img_wait_struct)
                            .error(R.drawable.img_wait_struct)
                            .transform(new RoundedTransformation(1000,4))
                            .resize(284,284)
                            .into(mBlanketImageView);
                    break;
            }
            if(structure.isAdhere())
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mAdhereButton.setBackgroundTintList(ColorStateList.valueOf(itemView.getContext().getColor(R.color.black3)));
                    mAdhereButton.setText("Détacher");
                }
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (structure.getId())
                    {
                        case "AddBook":
                            simpleOkDialog(R.drawable.add_auteurs,"Ajouter un contenu dans EduNiger" , "Partagez vos savoirs avec la communauté ! En devenant auteur, vous pouvez publier vos livres et documents. Chaque ajout est vérifié avant d’être validé et mis en ligne, puis vous recevez une notification de confirmation.");
                            break;
                        case "RegisterAuthor":
                            simpleOkDialog(R.drawable.add_auteurs,"Devenir Auteur Sur EduNiger" , "Vous avez un livre, un manuscrit ou une idée à partager ?\n\n" +
                                    "Cette section est faite pour vous ! Elle vous permet d’ajouter vos ouvrages à notre bibliothèque, de les rendre accessibles aux lecteurs et de partager vos connaissances, vos histoires ou vos créations avec la communauté.\n\n" +
                                    "En rejoignant les auteurs, vous contribuez à enrichir la plateforme et à inspirer des milliers de personnes.");
                            break;
                        default:
                            Intent structureIntent = new Intent(itemView.getContext(), StructureActivity.class);
                            structureIntent.putExtra("intent_structure_adapter_id", structure.getId());
                            structureIntent.putExtra("intent_structure_adapter_logo", structure.getCover());
                            structureIntent.putExtra("intent_structure_adapter_name", structure.getName());
                            structureIntent.putExtra("intent_structure_adapter_description", structure.getDescription());
                            structureIntent.putExtra("intent_structure_adapter_is_adhere", structure.isAdhere());
                            structureIntent.putExtra("intent_structure_adapter_banner", structure.getBanner());
                            structureIntent.putExtra("intent_structure_adapter_author", structure.getAuthor());
                            structureIntent.putExtra("intent_structure_adapter_adherer_number", structure.getAdhererNumber());
                            structureIntent.putExtra("intent_structure_adapter_book_number", structure.getBookNumber());
                            structureIntent.putExtra("intent_structure_adapter_admin", structure.getAdmin());
                            itemView.getContext().startActivity(structureIntent);
                            break;
                    }
                }
            });
            mTitleTextView.setText(structure.getName());
            mAdhereButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (structure.getId())
                    {
                        case "2":
                            simpleOkDialog(R.drawable.vector_purple_200_desole,"Structure Exclusive","Cette structure est exclusivement réservée aux étudiants de la FAST UAM. Veuillez vérifier que vous remplissez les critères d'adhésion puis contacter les numéro suivante :\n+22796627534 / +22794961793.");
                            break;
                        default:
                            if (mAdhereButton.getText().toString().equals("Détacher"))
                                structDelete(structure.getId());
                            else
                            {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    mAdhereButton.setBackgroundTintList(ColorStateList.valueOf(itemView.getContext().getColor(R.color.black3)));
                                }
                                mAdhereButton.setText("Détacher");
                                structure.setAdhere(false);
                                DetachStructSyn detachStructSyn = new DetachStructSyn();
                                detachStructSyn.execute(Server.getUrlApi(itemView.getContext()) + "AdhererStruct.php",mSession.getIdNumber(),structure.getId());
                            }
                            break;
                        case "AddBook","RegisterAuthor":
                            Intent registerAuthorIntent = new Intent(itemView.getContext(), AddBookActivity.class);
                            itemView.getContext().startActivity(registerAuthorIntent);
                            //simpleOkDialog(R.drawable.add_auteurs,"Ajouter un contenue dans EduNiger" , "La fonctionnalité Devenir Auteur est actuellement en cours de développement. Elle sera disponible dans la version officielle à venir, inchaAllah.");
                            break;
                    }

                }
            });
        }
        private void structDelete(String id){
            StructDeleteDialog structDeleteDialog = new StructDeleteDialog((Activity) itemView.getContext());
            structDeleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            structDeleteDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            TextView err = structDeleteDialog.findViewById(R.id.text_view_dialog_structure_delete_err);
            TextView no = structDeleteDialog.findViewById(R.id.no);
            TextView yes = structDeleteDialog.findViewById(R.id.yes);
            EditText password = structDeleteDialog.findViewById(R.id.edit_text_dialog_struct_delete_password);
            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    structDeleteDialog.cancel();
                }
            });

            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!password.getText().toString().isEmpty())
                    {
                        if (Objects.equals(PasswordUtil.hashPassword(password.getText().toString()), mSession.getPassword()))
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                structDeleteDialog.cancel();
                                mAdhereButton.setBackgroundTintList(ColorStateList.valueOf(itemView.getContext().getColor(R.color.purple_200)));
                                mAdhereButton.setText("S'adhérer");
                                DetachStructSyn detachStructSyn = new DetachStructSyn();
                                detachStructSyn.execute(Server.getUrlApi(itemView.getContext()) + "DetachStruct.php",mSession.getIdNumber(),id);
                            }
                        }
                    }
                    password.setBackground(itemView.getContext().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    err.setVisibility(View.VISIBLE);
                    err.setText("Mot de passe incorrect");
                }
            });
            structDeleteDialog.build();
        }
        private class DetachStructSyn extends AsyncTask<String,Void,String> {
            @Override
            protected String doInBackground(String... params) {

                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("idUser",params[1])
                            .addFormDataPart("idStruct",params[2])
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
                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }catch (Exception e)
                {
                    return null;
                }
                return null;
            }
            @Override
            protected void onPostExecute(String jsonData){
                //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
                if(jsonData != null)
                {
                    if(!jsonData.equals("RAS"))
                    {
                        if(jsonData.equals("true"))
                        {
                            Toast.makeText(itemView.getContext(), "Structure détacher", Toast.LENGTH_SHORT);
                        }
                    }
                }
            }
        }
        private class AdhererStructSyn extends AsyncTask<String,Void,String> {
            @Override
            protected String doInBackground(String... params) {

                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("idUser",params[1])
                            .addFormDataPart("idStruct",params[2])
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
                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }catch (Exception e)
                {
                    return null;
                }
                return null;
            }
            @Override
            protected void onPostExecute(String jsonData){
                //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
                if(jsonData != null)
                {
                    if(!jsonData.equals("RAS"))
                    {
                        if(jsonData.equals("true"))
                        {
                            Toast.makeText(itemView.getContext(), "Structure Adhérer", Toast.LENGTH_SHORT);
                        }
                    }
                }
            }
        }
        private void simpleOkDialog(int ico , String title , String message){
            SimpleOkDialog simpleOkDialog = new SimpleOkDialog((Activity) itemView.getContext());
            simpleOkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
                public void onClick(View view) {
                    simpleOkDialog.cancel();
                }
            });
            simpleOkDialog.build();
        }
    }
}