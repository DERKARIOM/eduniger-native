package com.ninotech.fabi.controleur.adapter;

import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.ContainerActivity;
import com.ninotech.fabi.model.data.Category;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class CategoryLocalAdapter extends RecyclerView.Adapter<CategoryLocalAdapter.MyViewHolder> {
    List<Category> mCategories;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public CategoryLocalAdapter(List<Category> categories) {
        mCategories = categories;
    }
    @Override
    public CategoryLocalAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_category,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Category item = mCategories.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mCategories.get(position));

    }
    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    public Category getItem(int position) {
        return mCategories.get(position);
    }

    public void Remove(int position){
        mCategories.remove(position);
        notifyItemRemoved(position);
    }
    public void filterList(ArrayList<Category> filteredList) {
        mCategories = filteredList;
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private final ImageView mCoverImageView;
        private final TextView mTitleTextView;
        MyViewHolder(View itemView){
            super(itemView);
            mCoverImageView = itemView.findViewById(R.id.image_view_adapter_category_blanket);
            mTitleTextView = itemView.findViewById(R.id.text_view_adapter_category_title);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
        }
        void display(Category category){
            File file = new File(category.getCover());
            Picasso.get()
                    .load(file)
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .into(mCoverImageView);
            mTitleTextView.setText(category.getTitle());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent local = new Intent(itemView.getContext(), ContainerActivity.class);
                    local.putExtra("id",7);
                    local.putExtra("titleCategory",category.getTitle());
                    itemView.getContext().startActivity(local);
                }
            });
        }
    }
}