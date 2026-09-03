package com.ninotech.eduniger.controleur.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.activity.AudioPlayerActivity;
import com.ninotech.eduniger.controleur.animation.RoundedTransformation;
import com.ninotech.eduniger.model.data.ContinueItem;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * Carrousel « Reprendre l'écoute » de l'Accueil.
 *
 * Un clic ouvre AudioPlayerActivity sur le livre concerné ; la reprise à la bonne
 * position est assurée par AudioPlayerService (PlaybackTable), il n'y a donc
 * aucune position à transmettre dans l'Intent.
 */
public class ContinueListeningAdapter
        extends RecyclerView.Adapter<ContinueListeningAdapter.ViewHolder> {

    private final List<ContinueItem> mItems;

    public ContinueListeningAdapter(List<ContinueItem> items) {
        mItems = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_continue_listening, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView   mCover;
        private final TextView    mTitle;
        private final TextView    mRemaining;
        private final ProgressBar mProgress;

        ViewHolder(View itemView) {
            super(itemView);
            mCover     = itemView.findViewById(R.id.image_view_continue_cover);
            mTitle     = itemView.findViewById(R.id.text_view_continue_title);
            mRemaining = itemView.findViewById(R.id.text_view_continue_remaining);
            mProgress  = itemView.findViewById(R.id.progress_bar_continue);
        }

        void bind(ContinueItem item) {
            Context context = itemView.getContext();

            mTitle.setText(item.getTitle());
            mProgress.setProgress(item.getProgressPerMille());

            int remaining = item.getRemainingMinutes();
            mRemaining.setText(remaining >= 0
                    ? context.getString(R.string.continue_remaining, remaining)
                    : item.getAuthor());

            String cover = item.getCover();
            if (cover != null && !cover.isEmpty()) {
                Picasso.get().load(new File(cover))
                        .placeholder(R.drawable.img_wait_cover_book)
                        .error(R.drawable.img_wait_cover_book)
                        .transform(new RoundedTransformation(12, 0))
                        .resize(300, 300)
                        .centerCrop()
                        .into(mCover);
            } else {
                mCover.setImageResource(R.drawable.img_wait_cover_book);
            }

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, AudioPlayerActivity.class);
                intent.putExtra("key_adapter_audio_book_id", item.getIdBook());
                intent.putExtra("list_audio_source", "all");
                context.startActivity(intent);
            });
        }
    }
}
