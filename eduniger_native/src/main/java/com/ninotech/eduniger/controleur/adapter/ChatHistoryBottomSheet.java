package com.ninotech.eduniger.controleur.adapter;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ninotech.eduniger.R;
import com.ninotech.eduniger.model.data.ChatSession;
import com.ninotech.eduniger.model.table.ChatDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * BottomSheet qui affiche l'historique des conversations sauvegardées.
 * Chaque entrée peut être ouverte (tap) ou supprimée (icône corbeille).
 */
public class ChatHistoryBottomSheet extends BottomSheetDialogFragment {

    // ─── Interface callback ───────────────────────────────────────────────────
    public interface OnSessionSelectedListener {
        void onSessionSelected(ChatSession session);
    }

    private final OnSessionSelectedListener listener;

    public ChatHistoryBottomSheet(OnSessionSelectedListener listener) {
        this.listener = listener;
    }

    // ─── Vue ──────────────────────────────────────────────────────────────────
    private RecyclerView      recyclerView;
    private SessionAdapter    adapter;
    private List<ChatSession> sessions;
    private ChatDatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Layout minimal en code (pas besoin d'un fichier XML supplémentaire)
        View root = inflater.inflate(R.layout.bottom_sheet_history, container, false);
        recyclerView = root.findViewById(R.id.recyclerHistory);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper  = ChatDatabaseHelper.getInstance(requireContext());
        sessions  = dbHelper.getAllSessions();
        adapter   = new SessionAdapter(sessions);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    // ─── Adapter interne ──────────────────────────────────────────────────────

    private class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.VH> {

        private final List<ChatSession> data;

        SessionAdapter(List<ChatSession> data) { this.data = data; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_session, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ChatSession s = data.get(position);

            holder.tvTitle.setText(s.getTitle());

            // Format date lisible
            String date = new SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.getDefault())
                    .format(new Date(s.getUpdatedAt()));
            holder.tvDate.setText(date);

            // Tap → charger la session
            holder.itemView.setOnClickListener(v -> {
                dismiss();
                listener.onSessionSelected(s);
            });

            // Suppression avec confirmation
            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Supprimer la discussion")
                        .setMessage("Cette conversation sera définitivement supprimée.")
                        .setPositiveButton("Supprimer", (dialog, which) -> {
                            int pos = holder.getAdapterPosition();
                            dbHelper.deleteSession(s.getId());
                            data.remove(pos);
                            notifyItemRemoved(pos);
                        })
                        .setNegativeButton("Annuler", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView    tvTitle, tvDate;
            ImageButton btnDelete;

            VH(@NonNull View itemView) {
                super(itemView);
                tvTitle   = itemView.findViewById(R.id.tvSessionTitle);
                tvDate    = itemView.findViewById(R.id.tvSessionDate);
                btnDelete = itemView.findViewById(R.id.btnDeleteSession);
            }
        }
    }
}