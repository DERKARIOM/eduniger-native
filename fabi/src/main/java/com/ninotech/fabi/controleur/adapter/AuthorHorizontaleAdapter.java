package com.ninotech.fabi.controleur.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.BookActivity;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.Author;
import com.ninotech.fabi.model.data.Book;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AuthorHorizontaleAdapter extends RecyclerView.Adapter<AuthorHorizontaleAdapter.MyViewHolder> {
    List<Author> mAuthors;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public AuthorHorizontaleAdapter(List<Author> authors) {
        mAuthors = authors;
    }
    @Override
    public AuthorHorizontaleAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_author_horizotal,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Author item = mAuthors.get(position);
        try {
            holder.display(mAuthors.get(position));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public int getItemCount() {
        return mAuthors.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private final ImageView mProfileImageView;
        private final TextView mNameTextView;
        MyViewHolder(View itemView){
            super(itemView);
            mProfileImageView = itemView.findViewById(R.id.image_view_adapter_author_horizontal_profile);
            mNameTextView = itemView.findViewById(R.id.text_view_adapter_author_horizontal_name);
        }

        void display(Author author) throws SQLException, IOException {
            Glide.with(itemView.getContext())
                    .load(author.getPhoto())
                    .apply(RequestOptions.circleCropTransform())
                    .into(mProfileImageView);
            mNameTextView.setText(author.getName());
        }

    }
}
