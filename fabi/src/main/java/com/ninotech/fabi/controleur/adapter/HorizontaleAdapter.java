package com.ninotech.fabi.controleur.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.AddBookActivity;
import com.ninotech.fabi.controleur.activity.BookActivity;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.Book;
import com.ninotech.fabi.model.data.OnlineBook;
import com.ninotech.fabi.model.data.Structure;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class HorizontaleAdapter extends RecyclerView.Adapter<HorizontaleAdapter.MyViewHolder> {
    List<OnlineBook> mBooks;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public HorizontaleAdapter(List<OnlineBook> books) {
        mBooks = books;
    }
    @Override
    public HorizontaleAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_book_horizotal,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Book item = mBooks.get(position);
        try {
            holder.display(mBooks.get(position));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private final ImageView mCoverImageView;
        MyViewHolder(View itemView){
            super(itemView);
            mCoverImageView = (ImageView) itemView.findViewById(R.id.image_view_adapter_similar_cover);
        }

        void display(Book book) throws SQLException, IOException {
            Picasso.get()
                    .load(itemView.getContext().getString(R.string.ip_server) + "ressources/cover/"  + book.getCover())
                    .placeholder(R.drawable.img_wait_cover_book)
                    .error(R.drawable.img_wait_cover_book)
                    .transform(new RoundedTransformation(15,4))
                    .resize(260,394)
                    .into(mCoverImageView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!book.getId().equals("add"))
                    {
                        Intent intentBook = new Intent(itemView.getContext(), BookActivity.class);
                        intentBook.putExtra("intent_adapter_book_id", book.getId());
                        itemView.getContext().startActivity(intentBook);
                    }
                    else
                    {
                        Intent intentAddBook = new Intent(itemView.getContext(), AddBookActivity.class);
                        itemView.getContext().startActivity(intentAddBook);
                    }
                }
            });
        }

    }
}
