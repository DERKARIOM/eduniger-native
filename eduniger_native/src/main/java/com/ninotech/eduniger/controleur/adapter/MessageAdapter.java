package com.ninotech.eduniger.controleur.adapter;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.model.data.Message;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.html.HtmlPlugin;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final long CHAR_DELAY_MS  = 30;
    private static final int  CHARS_PER_TICK = 20;

    private final List<Message> messages;
    private Markwon markwon;  // instance Markwon partagée dans l'adapter

    private RecyclerView recyclerView;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    // ─── Attacher / détacher le RV ────────────────────────────────────────────

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView rv) {
        super.onAttachedToRecyclerView(rv);
        this.recyclerView = rv;

        // Initialiser Markwon une seule fois avec le contexte du RV
        this.markwon = Markwon.builder(rv.getContext())
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(TablePlugin.create(rv.getContext()))
                .usePlugin(HtmlPlugin.create())
                .build();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView rv) {
        super.onDetachedFromRecyclerView(rv);
        this.recyclerView = null;
    }

    // ─── Types de vues ────────────────────────────────────────────────────────

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    // ─── Inflation ────────────────────────────────────────────────────────────

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == Message.TYPE_USER)
                ? R.layout.item_message_user
                : R.layout.item_message_bot;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new MessageViewHolder(view);
    }

    // ─── Binding ──────────────────────────────────────────────────────────────

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = messages.get(position);
        holder.cancelAnimation();

        if (msg.getType() == Message.TYPE_BOT && markwon != null) {
            // ✅ Messages bot → rendu Markdown complet
            markwon.setMarkdown(holder.tvMessage, msg.getText());
        } else {
            // Messages user → texte brut
            holder.tvMessage.setText(msg.getText());
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    // ─── API publique ─────────────────────────────────────────────────────────

    public void animateLastBotMessage(Message message) {
        if (recyclerView == null) return;

        int position = messages.size() - 1;
        recyclerView.smoothScrollToPosition(position);

        recyclerView.postDelayed(() -> {
            RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(position);
            if (vh instanceof MessageViewHolder) {
                MessageViewHolder botVH = (MessageViewHolder) vh;
                botVH.animateText(message.getText(), recyclerView, position, markwon);
            }
        }, 150);
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────────

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView tvMessage;

        private Handler  animHandler;
        private Runnable animRunnable;
        private boolean  animating = false;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }

        void animateText(String fullText, RecyclerView rv, int position, Markwon markwon) {
            cancelAnimation();

            tvMessage.setText("");
            animating   = true;
            animHandler = new Handler(Looper.getMainLooper());

            final int[]     index        = {0};
            final boolean[] userScrolled = {false};

            RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView rv2, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        userScrolled[0] = true;
                    }
                }
            };
            if (rv != null) rv.addOnScrollListener(scrollListener);

            animRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!animating) {
                        if (rv != null) rv.removeOnScrollListener(scrollListener);
                        return;
                    }

                    if (index[0] <= fullText.length()) {
                        index[0] = Math.min(index[0] + CHARS_PER_TICK, fullText.length());
                        String chunk = fullText.substring(0, index[0]);

                        // ✅ Markwon appliqué à CHAQUE tick (pas de texte brut visible)
                        if (markwon != null) {
                            markwon.setMarkdown(tvMessage, chunk);
                        } else {
                            tvMessage.setText(chunk);
                        }

                        if (rv != null && !userScrolled[0]) {
                            rv.smoothScrollToPosition(position);
                        }

                        animHandler.postDelayed(this, CHAR_DELAY_MS);

                    } else {
                        // ✅ Rendu final propre
                        animating = false;
                        if (markwon != null) {
                            markwon.setMarkdown(tvMessage, fullText);
                        } else {
                            tvMessage.setText(fullText);
                        }
                        if (rv != null) {
                            rv.removeOnScrollListener(scrollListener);
                            if (!userScrolled[0]) rv.smoothScrollToPosition(position);
                        }
                    }
                }
            };

            animHandler.postDelayed(animRunnable, 100);
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