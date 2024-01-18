package com.fabi.controleur.adapter;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fabi.R;
import com.fabi.model.data.Livres;

import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.MyViewHolder> {
    List<Livres> mListLivre;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public AudioAdapter(List<Livres> listLivre) {
        mListLivre = listLivre;
    }
    @Override
    public AudioAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_livre,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Livres item = mListLivre.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mListLivre.get(position));

    }
    @Override
    public int getItemCount() {
        return mListLivre.size();
    }

    public Livres getItem(int position) {
        return mListLivre.get(position);
    }

    public void Remove(int position){
        mListLivre.remove(position);
        notifyItemRemoved(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private ImageView mCouverture;
        private TextView mTitre;
        private TextView mCategorie;
        private ImageView mIcoPysique;
        private ImageView mIcoPdf;
        private ImageView mIcoAudio;
        private TextView mNbrLike;
        private TextView mNbrVue;
        private ImageView mIco1;
        private  ImageView mIco2;
        MyViewHolder(View itemView){
            super(itemView);
            mCouverture = itemView.findViewById(R.id.couverture_livre);
            mTitre = itemView.findViewById(R.id.title_livre);
            mCategorie = itemView.findViewById(R.id.categorie_livre);
            mIcoPysique = itemView.findViewById(R.id.ico_pysique);
            mIcoPdf = itemView.findViewById(R.id.ico_pdf);
            mIcoAudio = itemView.findViewById(R.id.ico_audio);
            mNbrLike = itemView.findViewById(R.id.nbr_like);
            mNbrVue = itemView.findViewById(R.id.nbr_vue);
            mIco1 = itemView.findViewById(R.id.ico1);
            mIco2 = itemView.findViewById(R.id.ico2);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }
        void display(Livres livre){
//            mCouverture.setImageResource(livre.getCouverture());
//            mTitre.setText(livre.getTitre());
//            mCategorie.setText(livre.getCategorie());
//            mNbrLike.setText(livre.getNbrLike());
//            mNbrVue.setText(livre.getNbrVue());
//            if(livre.isPysique())
//                mIcoPysique.setVisibility(View.VISIBLE);
//            if(livre.isPdf())
//                mIcoPdf.setVisibility(View.VISIBLE);
//            if(livre.isEstAudio())
//                mIcoAudio.setVisibility(View.VISIBLE);
//            mIco1.setImageResource(R.drawable.audio);
//            mIco2.setImageResource(R.drawable.time);
        }
    }
}