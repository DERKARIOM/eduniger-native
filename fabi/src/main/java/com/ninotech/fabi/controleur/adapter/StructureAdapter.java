package com.ninotech.fabi.controleur.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.BookActivity;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.controleur.dialog.SimpleOkDialog;
import com.ninotech.fabi.controleur.dialog.StructDeleteDialog;
import com.ninotech.fabi.controleur.dialog.UpdateDialog;
import com.ninotech.fabi.model.data.PasswordUtil;
import com.ninotech.fabi.model.data.Structure;
import com.ninotech.fabi.model.table.Session;
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
        private final Button mAdhereButton;
        private Session mSession;
        MyViewHolder(View itemView){
            super(itemView);
            mBlanketImageView = itemView.findViewById(R.id.image_view_adapter_structure_cover);
            mTitleTextView = itemView.findViewById(R.id.text_view_adapter_structure_title);
            mAdhereButton = itemView.findViewById(R.id.button_adapter_structure);
            mSession = new Session(itemView.getContext());
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
        }
        void display(Structure structure){
            Picasso.get()
                    .load(itemView.getResources().getString(R.string.ip_server) + "ressources/cover/" + structure.getCover())
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .transform(new RoundedTransformation(1000,4))
                    .resize(284,284)
                    .into(mBlanketImageView);
            if(structure.isAdhere())
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mAdhereButton.setBackgroundTintList(ColorStateList.valueOf(itemView.getContext().getColor(R.color.rouge)));
                    mAdhereButton.setText("Détacher");
                }
            }
            mTitleTextView.setText(structure.getName());
            mAdhereButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (structure.getId())
                    {
                        case "1":
                            if (mAdhereButton.getText().toString().equals("Détacher"))
                                structDelete(structure.getId());
                            else
                            {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    mAdhereButton.setBackgroundTintList(ColorStateList.valueOf(itemView.getContext().getColor(R.color.rouge)));
                                }
                                mAdhereButton.setText("Détacher");
                                structure.setAdhere(false);
                                DetachStructSyn detachStructSyn = new DetachStructSyn();
                                detachStructSyn.execute(itemView.getContext().getString(R.string.ip_server_android) + "AdhererStruct.php",mSession.getIdNumber(),structure.getId());
                            }
                            break;
                        case "2":
                            simpleOkDialog(R.drawable.vector_purple_200_desole,"Structure Exclusive","Cette structure est exclusivement réservée aux étudiants de la FAST UAM. Veuillez vérifier que vous remplissez les critères d'adhésion puis contacter les numéro suivante :\n+22796627534 / +22794961793.");
                            break;

                    }

                }
            });
        }
        private void structDelete(String id){
            StructDeleteDialog structDeleteDialog = new StructDeleteDialog((Activity) itemView.getContext());
            structDeleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            structDeleteDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
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
                                mAdhereButton.setText("Adhérer");
                                DetachStructSyn detachStructSyn = new DetachStructSyn();
                                detachStructSyn.execute(itemView.getContext().getString(R.string.ip_server_android) + "DetachStruct.php",mSession.getIdNumber(),id);
                            }
                        }
                    }
                    password.setBackground(itemView.getContext().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
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