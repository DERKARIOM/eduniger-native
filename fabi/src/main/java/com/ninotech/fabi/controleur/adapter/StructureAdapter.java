package com.ninotech.fabi.controleur.adapter;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.CategoryActivity;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.Category;
import com.ninotech.fabi.model.data.Structure;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


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
            if(structure.getAdhere())
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mAdhereButton.setBackgroundTintList(ColorStateList.valueOf(itemView.getContext().getColor(R.color.rouge)));
                    mAdhereButton.setText("Détacher");
                }
            }
            Picasso.get()
                    .load(itemView.getResources().getString(R.string.ip_server) + "ressources/cover/" + structure.getCover())
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .transform(new RoundedTransformation(1000,4))
                    .resize(284,284)
                    .into(mBlanketImageView);
            mTitleTextView.setText(structure.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent category = new Intent(itemView.getContext(), CategoryActivity.class);
                    category.putExtra("intent_adapter_category_title", mTitleTextView.getText().toString());
                    itemView.getContext().startActivity(category);
                }
            });
        }
    }
}