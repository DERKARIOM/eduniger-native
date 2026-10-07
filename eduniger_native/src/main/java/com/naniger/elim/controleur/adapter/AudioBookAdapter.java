package com.naniger.elim.controleur.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.naniger.elim.R;
import com.naniger.elim.controleur.activity.AudioPlayerActivity;
import com.naniger.elim.controleur.animation.RoundedTransformation;
import com.naniger.elim.model.data.AudioBook;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AudioBookAdapter extends RecyclerView.Adapter<AudioBookAdapter.MyViewHolder> {

    List<AudioBook> mAudioBooks;
    private int mPosition;

    private ItemTouchHelper mItemTouchHelper;

    public void setItemTouchHelper(ItemTouchHelper helper) {
        mItemTouchHelper = helper;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public AudioBookAdapter(List<AudioBook> audioBooks) {
        mAudioBooks = audioBooks;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_book_audio, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.itemView.setOnLongClickListener(v -> {
            mPosition = holder.getAdapterPosition();
            v.showContextMenu();
            return true;
        });

        AudioBook audioBook = mAudioBooks.get(position);

        // Handle visible uniquement dans la file de lecture (isPlayerList = true)
        if (audioBook.isPlayerList()) {
            holder.mDragHandleImageView.setVisibility(View.VISIBLE);
            holder.mDragHandleImageView.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN && mItemTouchHelper != null) {
                    mItemTouchHelper.startDrag(holder);
                }
                return false;
            });
        } else {
            holder.mDragHandleImageView.setVisibility(View.GONE);
        }

        holder.display(audioBook);

        // ── Fix : la sélection doit refléter l'ORDRE AFFICHÉ (après glisser-déposer) ──
        // On envoie la liste complète des identifiants dans leur ordre courant plutôt
        // qu'un simple index, pour que le Service rejoue bien la file réordonnée.
        if (audioBook.isPlayerList()) {
            holder.itemView.setOnClickListener(v -> {
                ArrayList<String> orderedIds = new ArrayList<>();
                for (AudioBook b : mAudioBooks) orderedIds.add(b.getId());

                Intent intent = new Intent("SELECT_LIST_PLAYER");
                intent.putStringArrayListExtra("ordered_ids", orderedIds);
                intent.putExtra("selected_id", audioBook.getId());
                v.getContext().sendBroadcast(intent);
                try {
                    ((Activity) v.getContext()).finish();
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mAudioBooks.size();
    }

    public AudioBook getItem(int position) {
        return mAudioBooks.get(position);
    }

    public void Remove(int position) {
        mAudioBooks.remove(position);
        notifyItemRemoved(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        Collections.swap(mAudioBooks, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void filterList(ArrayList<AudioBook> filteredList) {
        mAudioBooks = filteredList;
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private final ImageView mCoverImageView;
        final ImageView         mDragHandleImageView;
        private final TextView  mTitleTextView;
        private final TextView  mAuthorTextView;
        private final TextView  mDurationTextView;
        private final TextView  mExplicitTextView;

        MyViewHolder(View itemView) {
            super(itemView);
            mCoverImageView      = itemView.findViewById(R.id.image_view_adapter_book_audio_blanket);
            mDragHandleImageView = itemView.findViewById(R.id.image_view_adapter_book_audio_drag_handle);
            mTitleTextView       = itemView.findViewById(R.id.text_view_adapter_book_audio_title);
            mAuthorTextView      = itemView.findViewById(R.id.text_view_adapter_book_audio_author);
            mDurationTextView    = itemView.findViewById(R.id.text_view_adapter_book_audio_duration);
            mExplicitTextView    = itemView.findViewById(R.id.text_view_adapter_book_audio_explicit);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        }

        void display(AudioBook audioBook) {
            File file = new File(audioBook.getCover());
            Picasso.get().load(file)
                    .placeholder(R.drawable.img_wait_cover_book)
                    .error(R.drawable.img_wait_cover_book)
                    .transform(new RoundedTransformation(8, 4))
                    .resize(200, 200)
                    .centerCrop()
                    .into(mCoverImageView);

            mTitleTextView.setText(audioBook.getTitle());
            mAuthorTextView.setText("Par " + audioBook.getAuthor());
            mDurationTextView.setText(audioBook.getDuration());

            boolean isExplicit = audioBook.getTitle() != null
                    && audioBook.getTitle().toLowerCase().contains("explicit");
            mExplicitTextView.setVisibility(isExplicit ? View.VISIBLE : View.GONE);

            // Couleurs issues des jetons de thème : lisibles en mode clair ET sombre
            // (auparavant codées en dur en blanc => invisibles sur fond clair).
            Context ctx = itemView.getContext();
            if (audioBook.isPlayer()) {
                int accent = ContextCompat.getColor(ctx, R.color.player_accent);
                mTitleTextView.setTextColor(accent);
                mAuthorTextView.setTextColor(accent);
                mDurationTextView.setTextColor(accent);
            } else {
                mTitleTextView.setTextColor(ContextCompat.getColor(ctx, R.color.player_text_primary));
                mAuthorTextView.setTextColor(ContextCompat.getColor(ctx, R.color.player_text_secondary));
                mDurationTextView.setTextColor(ContextCompat.getColor(ctx, R.color.player_control_inactive));
            }

            itemView.setOnClickListener(v -> {
                if (audioBook.isPlayerList()) {
                    Intent intent = new Intent("SELECT_LIST_PLAYER");
                    intent.putExtra("position", getAdapterPosition());
                    itemView.getContext().sendBroadcast(intent);
                    try {
                        ((Activity) itemView.getContext()).finish();
                    } catch (Exception e) {
                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent audioPlayerIntent = new Intent(itemView.getContext(), AudioPlayerActivity.class);
                    audioPlayerIntent.putExtra("key_adapter_audio_book_id", audioBook.getId());
                    audioPlayerIntent.putExtra("list_audio_source", "all");
                    itemView.getContext().startActivity(audioPlayerIntent);
                }
            });
        }

        public File bitmapToFile(Context context, String filename, Bitmap bitmap) {
            File file = new File(context.getCacheDir(), filename);
            try {
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