package com.ninotech.fabi.controleur.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.SimilarBook;
import com.pspdfkit.configuration.activity.PdfActivityConfiguration;
import com.pspdfkit.ui.PdfActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.MyViewHolder> {
    List<SimilarBook> mListSimilarBook;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public RecentAdapter(List<SimilarBook> listSimilarBook) {
        mListSimilarBook = listSimilarBook;
    }
    @Override
    public RecentAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_similar,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SimilarBook item = mListSimilarBook.get(position);
        try {
            holder.display(mListSimilarBook.get(position));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    public int getItemCount() {
        return mListSimilarBook.size();
    }


//    public void Remove(int position){
//        mListNotification.remove(position);
//        notifyItemRemoved(position);
//    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        private final ImageView mCoverImageView;
        MyViewHolder(View itemView){
            super(itemView);
            mCoverImageView = (ImageView) itemView.findViewById(R.id.image_view_adapter_similar_cover);
        }
        void display(SimilarBook similarBook) throws SQLException, IOException {
            //mCoverImageView.setImageBitmap(similarBook.getCover());
            // Convertir le Bitmap en un fichier
            File file = new File(similarBook.getCover());

// Charger le fichier avec Picasso
            Picasso.get().load(file)
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .transform(new RoundedTransformation(15,4))
                    .resize(198,304)
                    .into(mCoverImageView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    File file = new File(similarBook.getPDF());
                    Uri uri = Uri.parse(Uri.fromFile(file).toString());
                    PdfActivityConfiguration config = new PdfActivityConfiguration.Builder(itemView.getContext()).build();
                    PdfActivity.showDocument(itemView.getContext(),uri,config);
                }
            });
        }
    }
}
