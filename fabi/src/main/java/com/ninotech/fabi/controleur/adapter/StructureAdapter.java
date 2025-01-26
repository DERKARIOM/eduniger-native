package com.ninotech.fabi.controleur.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.controleur.dialog.StructDeleteDialog;
import com.ninotech.fabi.controleur.dialog.UpdateDialog;
import com.ninotech.fabi.model.data.PasswordUtil;
import com.ninotech.fabi.model.data.Structure;
import com.ninotech.fabi.model.table.Session;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


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
        MyViewHolder(View itemView){
            super(itemView);
            mBlanketImageView = itemView.findViewById(R.id.image_view_adapter_structure_cover);
            mTitleTextView = itemView.findViewById(R.id.text_view_adapter_structure_title);
            mAdhereButton = itemView.findViewById(R.id.button_adapter_structure);
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
                            if ((structure.isAdhere()))
                                structDelete();
                    }

                }
            });
        }
        private void structDelete(){
            StructDeleteDialog structDeleteDialog = new StructDeleteDialog((Activity) itemView.getContext());
            structDeleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            structDeleteDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            TextView no = structDeleteDialog.findViewById(R.id.no);
            TextView yes = structDeleteDialog.findViewById(R.id.yes);
            EditText password = structDeleteDialog.findViewById(R.id.edit_text_dialog_struct_delete_password);
            Session session = new Session(itemView.getContext());
            Toast.makeText(itemView.getContext(), session.getPassword(), Toast.LENGTH_SHORT).show();
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
                        if (Objects.equals(PasswordUtil.hashPassword(password.getText().toString()), session.getPassword()))
                        {
                            Toast.makeText(itemView.getContext(), "Delete", Toast.LENGTH_SHORT).show();
                            structDeleteDialog.cancel();
                        }
                    }
                    password.setBackground(itemView.getContext().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                }
            });

            structDeleteDialog.build();
        }
    }
}