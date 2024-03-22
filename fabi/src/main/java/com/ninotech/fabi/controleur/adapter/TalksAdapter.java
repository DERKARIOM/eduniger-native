package com.ninotech.fabi.controleur.adapter;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.Talks;
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
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
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

    public void Remove(int position){
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
                    .load(itemView.getResources().getString(R.string.ip_server) + "ressources/profile/" + talks.getProfil())
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .transform(new RoundedTransformation(10000,4))
                    .resize(200,200)
                    .into(mPhotoProfilImageView);
            mUsernameTextView.setText(talks.getUsername());
            mMessageTextView.setText(talks.getMessage());
        }
    }
}