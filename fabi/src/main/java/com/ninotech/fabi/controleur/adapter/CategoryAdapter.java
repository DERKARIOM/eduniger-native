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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {
    List<Category> mCategorys;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public CategoryAdapter(List<Category> categorys) {
        mCategorys = categorys;
    }
    @Override
    public CategoryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_category,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Category item = mCategorys.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mCategorys.get(position));

    }
    @Override
    public int getItemCount() {
        return mCategorys.size();
    }

    public Category getItem(int position) {
        return mCategorys.get(position);
    }

    public void Remove(int position){
        mCategorys.remove(position);
        notifyItemRemoved(position);
    }

    public void filterList(ArrayList<Category> filteredList) {
        mCategorys = filteredList;
        notifyDataSetChanged();
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
        void display(Category category){
            Picasso.get()
                    .load(itemView.getResources().getString(R.string.ip_server) + "ressources/cover/" + category.getCover())
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .into(mBlanketImageView);
            mTitleTextView.setText(category.getTitle());
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