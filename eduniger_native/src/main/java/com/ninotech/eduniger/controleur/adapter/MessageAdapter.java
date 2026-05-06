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

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    // Délai entre chaque caractère (ms) — ajuste selon le goût
    private static final long CHAR_DELAY_MS  = 30;  // ~60 fps
    private static final int  CHARS_PER_TICK = 20;   // 3 caractères/tick ≈ ~180 chars/s

    private final List<Message> messages;

    // Référence au RecyclerView parent pour le scroll auto
    private RecyclerView recyclerView;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    // ─── Attacher / détacher le RV ────────────────────────────────────────────
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

    // ─── Types de vues ────────────────────────────────────────────────────────
    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType(); // 0 = USER, 1 = BOT
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
        // Le texte final est TOUJOURS affiché tel quel au bind (scroll, recycle, etc.)
        // L'animation n'est déclenchée qu'explicitement via addBotMessageAnimated()
        holder.tvMessage.setText(msg.getText());
        holder.cancelAnimation(); // stoppe toute animation résiduelle en cas de recycle
    }

    @Override
    public int getItemCount() { return messages.size(); }

    // ─── API publique ─────────────────────────────────────────────────────────

    /**
     * Ajoute un message bot et démarre l'animation typewriter sur son ViewHolder.
     * À appeler depuis le UI thread.
     *
     * @param message L'objet Message déjà ajouté à la liste {@code messages}
     *                et inséré via notifyItemInserted AVANT d'appeler cette méthode.
     */
    public void animateLastBotMessage(Message message) {
        if (recyclerView == null) return;

        int position = messages.size() - 1;

        // On scroll d'abord pour que le VH soit créé/visible
        recyclerView.scrollToPosition(position);

        // Petit délai pour laisser le layout passer avant de récupérer le VH
        recyclerView.post(() -> {
            RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(position);
            if (vh instanceof MessageViewHolder) {
                MessageViewHolder botVH = (MessageViewHolder) vh;
                botVH.animateText(message.getText(), recyclerView, position);
            }
            // Sinon le texte complet est déjà affiché par onBindViewHolder → pas de perte
        });
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────────
    static class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView tvMessage;

        // État de l'animation en cours
        private Handler  animHandler;
        private Runnable animRunnable;
        private boolean  animating = false;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }

        /**
         * Lance l'animation typewriter caractère par caractère.
         */
        void animateText(String fullText, RecyclerView rv, int position) {
            cancelAnimation(); // sécurité : annule toute animation précédente

            tvMessage.setText(""); // repart de zéro
            animating   = true;
            animHandler = new Handler(Looper.getMainLooper());

            final int[] index = {0};

            animRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!animating) return;

                    if (index[0] <= fullText.length()) {
                        // Avancer de CHARS_PER_TICK caractères par tick
                        index[0] = Math.min(index[0] + CHARS_PER_TICK, fullText.length());
                        tvMessage.setText(fullText.substring(0, index[0]));

                        // Scroll fluide à chaque tick
                        if (rv != null) rv.scrollToPosition(position);

                        animHandler.postDelayed(this, CHAR_DELAY_MS);
                    } else {
                        animating = false; // terminé
                    }
                }
            };
            animHandler.post(animRunnable);
        }

        /** Stoppe proprement l'animation (recycle, navigation, etc.) */
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