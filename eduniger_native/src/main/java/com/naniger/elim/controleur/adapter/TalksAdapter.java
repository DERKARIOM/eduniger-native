package com.naniger.elim.controleur.adapter;

import android.annotation.SuppressLint;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.naniger.elim.R;
import com.naniger.elim.controleur.animation.RoundedTransformation;
import com.naniger.elim.model.data.Server;
import com.naniger.elim.model.data.Talks;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TalksAdapter extends RecyclerView.Adapter<TalksAdapter.MyViewHolder> {
    List<Talks> mListTalks;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public TalksAdapter(List<Talks> listTalks) {
        mListTalks = listTalks;
    }
    @Override
    public TalksAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_talks,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Talks item = mListTalks.get(position);
        int i = position;
        // Appui long : signaler le commentaire ou masquer son auteur (exigence Google Play
        // pour les contenus publies par les utilisateurs). Pas pour ses propres messages
        // tout juste envoyes (sans identifiant serveur).
        holder.itemView.setOnLongClickListener(view -> {
            mPosition = holder.getAdapterPosition();
            if (mPosition == RecyclerView.NO_POSITION) return false;
            Talks talks = mListTalks.get(mPosition);
            if (talks.getIdComment() == null) return false;
            String[] actions = {"Signaler ce commentaire", "Masquer les commentaires de cet utilisateur"};
            new androidx.appcompat.app.AlertDialog.Builder(view.getContext())
                    .setItems(actions, (d, which) -> {
                        if (which == 0) {
                            com.naniger.elim.model.net.ContentReporter.report(view.getContext(),
                                    com.naniger.elim.model.net.ContentReporter.TYPE_COMMENT,
                                    talks.getIdComment(), talks.getMessage());
                        } else {
                            com.naniger.elim.model.net.ContentReporter.blockUser(view.getContext(), talks.getIdUser());
                            hideCommentsFrom(talks.getIdUser());
                            android.widget.Toast.makeText(view.getContext(),
                                    "Les commentaires de cet utilisateur sont masqués.",
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
            return true;
        });
        holder.display(mListTalks.get(position));

    }
    @Override
    public int getItemCount() {
        return mListTalks.size();
    }

    public Talks getItem(int position) {
        return mListTalks.get(position);
    }


    /** Retire de la liste affichee tous les commentaires de cet utilisateur. */
    @SuppressLint("NotifyDataSetChanged")
    public void hideCommentsFrom(String idUser) {
        if (idUser == null) return;
        for (int i = mListTalks.size() - 1; i >= 0; i--) {
            if (idUser.equals(mListTalks.get(i).getIdUser())) mListTalks.remove(i);
        }
        notifyDataSetChanged();
    }

    public void remove(int position){
        mListTalks.remove(position);
        notifyItemRemoved(position);
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private final ImageView mPhotoProfilImageView;
        private final TextView mUsernameTextView;
        private final TextView mMessageTextView;
        MyViewHolder(View itemView){
            super(itemView);
            mPhotoProfilImageView = itemView.findViewById(R.id.image_view_adapter_talks_profile);
            mUsernameTextView = itemView.findViewById(R.id.text_view_adapter_talks_user_name);
            mMessageTextView = itemView.findViewById(R.id.text_view_adapter_talks_message);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
        }
        void display(Talks talks){
            Picasso.get()
                    .load(Server.getUrlServer(itemView.getContext()) + "ressources/profile/" + talks.getProfil())
                    .placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .transform(new RoundedTransformation(1000,4))
                    .resize(400,400)
                    .into(mPhotoProfilImageView);
            mUsernameTextView.setText(talks.getUsername());
            mMessageTextView.setText(talks.getMessage());
        }
    }
}