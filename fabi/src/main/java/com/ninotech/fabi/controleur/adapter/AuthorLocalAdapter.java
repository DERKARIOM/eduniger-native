package com.ninotech.fabi.controleur.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ninotech.fabi.R;
import com.ninotech.fabi.model.data.Author;
import com.ninotech.fabi.model.data.OnlineBook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


public class AuthorLocalAdapter extends RecyclerView.Adapter<AuthorLocalAdapter.MyViewHolder> {
    List<Author> mAuthors;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public AuthorLocalAdapter(List<Author> authors) {
        mAuthors = authors;
    }
    @Override
    public AuthorLocalAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_author_verticale,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Author item = mAuthors.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mAuthors.get(position));

    }
    @Override
    public int getItemCount() {
        return mAuthors.size();
    }

    public Author getItem(int position) {
        return mAuthors.get(position);
    }

    public void Remove(int position){
        mAuthors.remove(position);
        notifyItemRemoved(position);
    }
    public void filterList(ArrayList<Author> filteredList) {
        mAuthors = filteredList;
        notifyDataSetChanged();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private ImageView mProfileImageView;
        private TextView mUsernameTextView;
        MyViewHolder(View itemView){
            super(itemView);
            mProfileImageView = itemView.findViewById(R.id.image_view_adapter_author_verticale_cover);
            mUsernameTextView = itemView.findViewById(R.id.text_view_adapter_author_verticale_name);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }
        void display(Author author){
            File file = new File(author.getProfile());
            Glide.with(itemView.getContext())
                    .load(file)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mProfileImageView);
            mUsernameTextView.setText(author.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(itemView.getContext(), "ok", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}