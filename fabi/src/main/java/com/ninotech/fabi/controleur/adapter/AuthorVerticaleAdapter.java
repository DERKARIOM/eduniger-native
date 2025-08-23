package com.ninotech.fabi.controleur.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.AuthorActivity;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.Author;
import com.ninotech.fabi.model.data.Server;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuthorVerticaleAdapter extends RecyclerView.Adapter<AuthorVerticaleAdapter.MyViewHolder> {
    List<Author> mAuthors;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public AuthorVerticaleAdapter(List<Author> authors) {
        mAuthors = authors;
    }
    @Override
    public AuthorVerticaleAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_author_verticale,parent,false);
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
    public void filterList(ArrayList<Author> filteredList) {
        mAuthors = filteredList;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private final ImageView mProfileImageView;
        private final TextView mNameTextView;
        MyViewHolder(View itemView){
            super(itemView);
            mProfileImageView = itemView.findViewById(R.id.image_view_adapter_author_verticale_cover);
            mNameTextView = itemView.findViewById(R.id.text_view_adapter_author_verticale_name);
        }

        void display(Author author) throws SQLException, IOException {
            Picasso.get()
                    .load(Server.getIpServer(itemView.getContext()) + "ressources/profile/" + author.getProfile())
                    .placeholder(R.drawable.img_wait_profile)
                    .error(R.drawable.img_wait_profile)
                    .transform(new RoundedTransformation(1000,4))
                    .resize(284,284)
                    .into(mProfileImageView);
            mNameTextView.setText(author.getName() + " " + author.getFirstName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent authorIntent = new Intent(itemView.getContext(), AuthorActivity.class);
                    authorIntent.putExtra("intent_author_adapter_id",author.getIdNumber());
                    authorIntent.putExtra("intent_author_adapter_name",author.getName());
                    authorIntent.putExtra("intent_author_adapter_first_name",author.getFirstName());
                    authorIntent.putExtra("intent_author_adapter_profile",author.getProfile());
                    authorIntent.putExtra("intent_author_adapter_profession",author.getProfession());
                    authorIntent.putExtra("intent_author_adapter_call",author.getCall());
                    authorIntent.putExtra("intent_author_adapter_email",author.getEmail());
                    authorIntent.putExtra("intent_author_adapter_whatsapp",author.getWhatsapp());
                    itemView.getContext().startActivity(authorIntent);
                }
            });
        }
    }
}
