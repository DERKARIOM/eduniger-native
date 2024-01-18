package com.fabi.controleur.adapter;

import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fabi.R;
import com.fabi.controleur.activity.LivreActivity;
import com.fabi.model.data.Livres;
import com.fabi.controleur.animation.RoundedTransformation;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ClassementAdapter extends RecyclerView.Adapter<ClassementAdapter.MyViewHolder> {
    List<Livres> mListLivre;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public ClassementAdapter(List<Livres> listLivre) {
        mListLivre = listLivre;
    }
    @Override
    public ClassementAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }
        void display(Livres livre){
            Picasso.with(itemView.getContext())
                    .load("http://192.168.43.1:2222/fabi/couverture/" + livre.getCouverture())
                    .placeholder(R.drawable.img_default_livre)
                    .error(R.drawable.img_default_livre)
                    .transform(new RoundedTransformation(15,4))
                    .resize(178,284)
                    .into(mCouverture);
            mTitre.setText(livre.getTitre());
            mCategorie.setText(livre.getCategorie());
            mNbrLike.setText(livre.getNbrLike());
            mNbrVue.setText(livre.getNbrVue());
            if(livre.getIsPysique().equals("1"))
                mIcoPysique.setVisibility(View.VISIBLE);
            if(!livre.getNomPdf().equals("null"))
                mIcoPdf.setVisibility(View.VISIBLE);
            if(livre.isAudio().equals("1"))
                mIcoAudio.setVisibility(View.VISIBLE);
            mNbrLike.setText(livre.getNbrLike());
            mNbrVue.setText(livre.getNbrVue());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intentLivre = new Intent(itemView.getContext(), LivreActivity.class);
                    intentLivre.putExtra("idLivre",livre.getId());
                    itemView.getContext().startActivity(intentLivre);
                }
            });
        }
    }
}