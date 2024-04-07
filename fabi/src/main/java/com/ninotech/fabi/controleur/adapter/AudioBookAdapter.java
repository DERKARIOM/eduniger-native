package com.ninotech.fabi.controleur.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.AudioPlayerActivity;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.AudioBook;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class AudioBookAdapter extends RecyclerView.Adapter<AudioBookAdapter.MyViewHolder> {
    List<AudioBook> mAudioBooks;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public AudioBookAdapter(List<AudioBook> audioBooks) {
        mAudioBooks = audioBooks;
    }
    @Override
    public AudioBookAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_book_audio,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AudioBook item = mAudioBooks.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mAudioBooks.get(position));

    }
    @Override
    public int getItemCount() {
        return mAudioBooks.size();
    }

    public AudioBook getItem(int position) {
        return mAudioBooks.get(position);
    }

    public void Remove(int position){
        mAudioBooks.remove(position);
        notifyItemRemoved(position);
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private final ImageView mCoverImageView;
        private final TextView mTitleTextView;
        private final TextView mAuthorTextView;
        private TextView mDurationTextView;
        MyViewHolder(View itemView){
            super(itemView);
           mCoverImageView = itemView.findViewById(R.id.image_view_adapter_book_audio_blanket);
           mTitleTextView = itemView.findViewById(R.id.text_view_adapter_book_audio_title);
           mAuthorTextView = itemView.findViewById(R.id.text_view_adapter_book_audio_author);
           mDurationTextView = itemView.findViewById(R.id.text_view_adapter_book_audio_duration);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
        }
        void display(AudioBook audioBook){
            File file = new File(audioBook.getCover());
            Picasso.get().load(file)
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .transform(new RoundedTransformation(15,4))
                    .resize(210,304)
                    .into(mCoverImageView);
            mTitleTextView.setText(audioBook.getTitle());
            mAuthorTextView.setText(audioBook.getAuthor());
            mDurationTextView.setText(audioBook.getDuration());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent audioPayerIntent = new Intent(itemView.getContext(), AudioPlayerActivity.class);
                    audioPayerIntent.putExtra("key_adapter_audio_book_id",audioBook.getId());
                    itemView.getContext().startActivity(audioPayerIntent);
//                    Toast.makeText(itemView.getContext(), audioBook.getAudio(), Toast.LENGTH_SHORT).show();
//                    MediaPlayer mediaPlayer = new MediaPlayer();
//                    try {
//                        // Spécifie le chemin d'accès au fichier audio dans le stockage interne
//                        mediaPlayer.setDataSource(audioBook.getAudio());
//                        mediaPlayer.prepare();
//                        mediaPlayer.start();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }
            });
        }
        public File bitmapToFile(Context context, String filename, Bitmap bitmap) {
            // Créer un fichier dans le répertoire de cache de l'application
            File file = new File(context.getCacheDir(), filename);
            try {
                // Convertir le Bitmap en un fichier de sortie
                file.createNewFile();
                FileOutputStream ostream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                ostream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return file;
        }
    }
}