package com.fabi.Model;

import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.fabi.Controleur.CategorieActivity;
import com.example.fabi.R;
import com.squareup.picasso.Picasso;

import java.util.List;


public class CategorieAdapter extends RecyclerView.Adapter<CategorieAdapter.MyViewHolder> {
    List<Categorie> mListCategorie;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public CategorieAdapter(List<Categorie> listCategorie) {
        mListCategorie = listCategorie;
    }
    @Override
    public CategorieAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.categorie_bloc,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Categorie item = mListCategorie.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mListCategorie.get(position));

    }
    @Override
    public int getItemCount() {
        return mListCategorie.size();
    }

    public Categorie getItem(int position) {
        return mListCategorie.get(position);
    }

    public void Remove(int position){
        mListCategorie.remove(position);
        notifyItemRemoved(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private ImageView mIco;
        private TextView mTitre;
        MyViewHolder(View itemView){
            super(itemView);
            mIco = itemView.findViewById(R.id.ico_cat);
            mTitre = itemView.findViewById(R.id.titre_cat);
            //mButton = (Button) itemView.findViewById(R.id.bttAnex);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }
        void display(Categorie categorie){
            Picasso.with(itemView.getContext())
                    .load("http://192.168.43.1:2222/fabi/couverture/" + categorie.getIco())
                    .placeholder(R.drawable.item)
                    .error(R.drawable.item)
                    .into(mIco);
            mTitre.setText(categorie.getTitre());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent categorie = new Intent(itemView.getContext(), CategorieActivity.class);
                    categorie.putExtra("nomCat",mTitre.getText().toString());
                    itemView.getContext().startActivity(categorie);
                }
            });
        }
    }
}