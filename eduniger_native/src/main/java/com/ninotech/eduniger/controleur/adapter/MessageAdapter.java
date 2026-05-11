package com.ninotech.eduniger.controleur.adapter;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.animation.RoundedTransformation;
import com.ninotech.eduniger.model.data.Message;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final long CHAR_DELAY_MS  = 30;
    private static final int  CHARS_PER_TICK = 20;

    private final List<Message> messages;
    private RecyclerView recyclerView;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView rv) {
        super.onAttachedToRecyclerView(rv);
        this.recyclerView = rv;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView rv) {
        super.onDetachedFromRecyclerView(rv);
        this.recyclerView = null;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == Message.TYPE_USER)
                ? R.layout.item_message_user
                : R.layout.item_message_bot;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new MessageViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = messages.get(position);
        holder.tvMessage.setText(msg.getText());
        holder.cancelAnimation();

        // ── AJOUT : affichage de la couverture pour les messages USER ─────────
        if (holder.ivBookCover != null) {
            String coverUrl = msg.getCoverUrl();
            if (coverUrl != null && !coverUrl.isEmpty()) {
                holder.ivBookCover.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(coverUrl)
                        .placeholder(R.drawable.img_wait_cover_book)
                        .error(R.drawable.img_wait_cover_book)
                        .transform(new RoundedTransformation(15, 4))
                        .resize(160, 240)
                        .centerCrop()
                        .into(holder.ivBookCover);
            } else {
                holder.ivBookCover.setVisibility(View.GONE);
                Picasso.get().cancelRequest(holder.ivBookCover); // nettoyage recycle
            }
        }
        // ─────────────────────────────────────────────────────────────────────
    }

    @Override
    public int getItemCount() { return messages.size(); }

    public void animateLastBotMessage(Message message) {
        if (recyclerView == null) return;
        int position = messages.size() - 1;
        recyclerView.scrollToPosition(position);
        recyclerView.post(() -> {
            RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(position);
            if (vh instanceof MessageViewHolder) {
                MessageViewHolder botVH = (MessageViewHolder) vh;
                botVH.animateText(message.getText(), recyclerView, position);
            }
        });
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────────
    static class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView  tvMessage;
        ImageView ivBookCover; // ← AJOUT (null pour TYPE_BOT)

        private Handler  animHandler;
        private Runnable animRunnable;
        private boolean  animating = false;

        MessageViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            tvMessage   = itemView.findViewById(R.id.tvMessage);
            // ── AJOUT : ivBookCover n'existe que dans item_message_user ───────
            ivBookCover = (viewType == Message.TYPE_USER)
                    ? itemView.findViewById(R.id.ivBookCover)
                    : null;
        }

        void animateText(String fullText, RecyclerView rv, int position) {
            cancelAnimation();
            tvMessage.setText("");
            animating   = true;
            animHandler = new Handler(Looper.getMainLooper());
            final int[] index = {0};
            animRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!animating) return;
                    if (index[0] <= fullText.length()) {
                        index[0] = Math.min(index[0] + CHARS_PER_TICK, fullText.length());
                        tvMessage.setText(fullText.substring(0, index[0]));
                        if (rv != null) rv.scrollToPosition(position);
                        animHandler.postDelayed(this, CHAR_DELAY_MS);
                    } else {
                        animating = false;
                    }
                }
            };
            animHandler.post(animRunnable);
        }

        void cancelAnimation() {
            animating = false;
            if (animHandler != null && animRunnable != null) {
                animHandler.removeCallbacks(animRunnable);
            }
            animHandler  = null;
            animRunnable = null;
        }
    }
}