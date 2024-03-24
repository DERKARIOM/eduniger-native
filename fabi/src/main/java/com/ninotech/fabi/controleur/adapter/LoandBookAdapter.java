package com.ninotech.fabi.controleur.adapter;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.Loand;
import com.squareup.picasso.Picasso;

import java.util.List;

public class LoandBookAdapter extends RecyclerView.Adapter<LoandBookAdapter.MyViewHolder> {
    List<Loand> mListLoand;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public LoandBookAdapter(List<Loand> listLoand) {
        mListLoand = listLoand;
    }
    @Override
    public LoandBookAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_physical,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Loand item = mListLoand.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mListLoand.get(position));

    }
    @Override
    public int getItemCount() {
        return mListLoand.size();
    }

    public Loand getItem(int position) {
        return mListLoand.get(position);
    }

    public void Remove(int position){
        mListLoand.remove(position);
        notifyItemRemoved(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private final ImageView mBlanketImageView;
        private final TextView mTitleTextView;
        private final TextView mDateInitTextView;
        private final TextView mDateFinaleTextView;
        private final ProgressBar mProgressBar;
        private final TextView mPercentageTextView;
        MyViewHolder(View itemView){
            super(itemView);
            mBlanketImageView = itemView.findViewById(R.id.image_view_adapter_physical_blanket);
            mTitleTextView = itemView.findViewById(R.id.text_view_adapter_physical_title);
            mDateInitTextView = itemView.findViewById(R.id.text_view_adapter_physical_date_init);
            mDateFinaleTextView = itemView.findViewById(R.id.date_return);
            mProgressBar = itemView.findViewById(R.id.progress_bar_adapter_physical);
            mPercentageTextView = itemView.findViewById(R.id.text_view_adapter_physical_pourcentage);
            itemView.setOnCreateContextMenuListener(this);

        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }
        void display(Loand loand){
            Picasso.get()
                    .load(itemView.getResources().getString(R.string.ip_server) + "ressources/cover/" + loand.getBlanket())
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .transform(new RoundedTransformation(15,4))
                    .resize(178,284)
                    .into(mBlanketImageView);
            mTitleTextView.setText(loand.getTitle());
            mTitleTextView.setText(loand.getTitle());
            mDateInitTextView.setText(loand.getDateStart());
            mDateFinaleTextView.setText(loand.getDateEnd());
            if(loand.getPercentage() > 100)
            {
                mProgressBar.setProgress(100);
                mPercentageTextView.setText("100%");
            }
            else
            {
                if(loand.getPercentage() < 0)
                {
                    mProgressBar.setProgress(0);
                    mPercentageTextView.setText("0%");
                }
                else
                {
                    mProgressBar.setProgress((int) loand.getPercentage());
                    mPercentageTextView.setText(String.valueOf(loand.getPercentage()) + "%");
                }
            }
        }
    }
}