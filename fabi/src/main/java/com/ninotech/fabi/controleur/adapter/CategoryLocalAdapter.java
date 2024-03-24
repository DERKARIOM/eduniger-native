package com.ninotech.fabi.controleur.adapter;

import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.CategoryActivity;
import com.ninotech.fabi.model.data.Category;
import com.ninotech.fabi.model.data.CategoryLocal;
import com.squareup.picasso.Picasso;

import java.util.List;


public class CategoryLocalAdapter extends RecyclerView.Adapter<CategoryLocalAdapter.MyViewHolder> {
    List<CategoryLocal> mCategoryLocalList;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public CategoryLocalAdapter(List<CategoryLocal> categoryLocalList) {
        mCategoryLocalList = categoryLocalList;
    }
    @Override
    public CategoryLocalAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_category,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        CategoryLocal item = mCategoryLocalList.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mCategoryLocalList.get(position));

    }
    @Override
    public int getItemCount() {
        return mCategoryLocalList.size();
    }

    public CategoryLocal getItem(int position) {
        return mCategoryLocalList.get(position);
    }

    public void Remove(int position){
        mCategoryLocalList.remove(position);
        notifyItemRemoved(position);
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private final ImageView mBlanketImageView;
        private final TextView mTitleTextView;
        MyViewHolder(View itemView){
            super(itemView);
            mBlanketImageView = itemView.findViewById(R.id.image_view_adapter_category_blanket);
            mTitleTextView = itemView.findViewById(R.id.text_view_adapter_category_title);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
        }
        void display(CategoryLocal categoryLocal){
            mBlanketImageView.setImageBitmap(categoryLocal.getCover());
            mTitleTextView.setText(categoryLocal.getTitle());
        }
    }
}