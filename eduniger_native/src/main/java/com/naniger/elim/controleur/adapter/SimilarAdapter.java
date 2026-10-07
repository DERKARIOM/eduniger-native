package com.naniger.elim.controleur.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.naniger.elim.controleur.activity.BookActivity;
import com.naniger.elim.R;
import com.naniger.elim.controleur.animation.RoundedTransformation;
import com.naniger.elim.model.data.LocalBooks;
import com.naniger.elim.model.data.Server;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class SimilarAdapter extends RecyclerView.Adapter<SimilarAdapter.MyViewHolder> {
    List<LocalBooks> mListLocalBooks;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public SimilarAdapter(List<LocalBooks> listLocalBooks) {
        mListLocalBooks = listLocalBooks;
    }
    @Override
    public SimilarAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_similar,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        LocalBooks item = mListLocalBooks.get(position);
        try {
            holder.display(mListLocalBooks.get(position));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    public int getItemCount() {
        return mListLocalBooks.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private final ImageView mCoverImageView;
        MyViewHolder(View itemView){
            super(itemView);
            mCoverImageView = (ImageView) itemView.findViewById(R.id.image_view_adapter_similar_cover);
        }

        void display(LocalBooks localBooks) throws SQLException, IOException {
            Picasso.get()
                    .load(Server.getUrlServer(itemView.getContext()) + "ressources/cover/"  + localBooks.getCover())
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .transform(new RoundedTransformation(15,4))
                    .resize(178,284)
                    .into(mCoverImageView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intentLivre = new Intent(itemView.getContext(), BookActivity.class);
                    intentLivre.putExtra("idLivre", localBooks.getId());
                    itemView.getContext().startActivity(intentLivre);
                }
            });
        }

    }
}
