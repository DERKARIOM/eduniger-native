package com.fabi.Model;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fabi.R;

import java.util.List;

public class PysiqueAdapter extends RecyclerView.Adapter<PysiqueAdapter.MyViewHolder> {
    List<Pysique> mListPysique;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public PysiqueAdapter(List<Pysique> listPysique) {
        mListPysique = listPysique;
    }
    @Override
    public PysiqueAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.pysique_bloc,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Pysique item = mListPysique.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mListPysique.get(position));

    }
    @Override
    public int getItemCount() {
        return mListPysique.size();
    }

    public Pysique getItem(int position) {
        return mListPysique.get(position);
    }

    public void Remove(int position){
        mListPysique.remove(position);
        notifyItemRemoved(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private ImageView mCouverture;
        private TextView mTitre;
        private TextView mDateInit;
        private TextView mDateFinale;
        private ProgressBar mProgressBar;
        private TextView mPourcentage;
        MyViewHolder(View itemView){
            super(itemView);
            mCouverture = itemView.findViewById(R.id.couverture_livre_pysique);
            mTitre = itemView.findViewById(R.id.title_livre_pysique);
            mDateInit = itemView.findViewById(R.id.date_init);
            mDateFinale = itemView.findViewById(R.id.date_retoure);
            mProgressBar = itemView.findViewById(R.id.progress);
            mPourcentage = itemView.findViewById(R.id.pourcentage);
            itemView.setOnCreateContextMenuListener(this);

        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }
        void display(Pysique pysique){
            mCouverture.setImageResource(pysique.getCouverture());
            mTitre.setText(pysique.getTitre());
            mDateInit.setText(pysique.getDateInit());
            mDateFinale.setText(pysique.getDateFinale());
            mProgressBar.setProgress(Integer.parseInt(pysique.getPourcentage()));
            mPourcentage.setText(pysique.getPourcentage() + "%");
        }
    }
}