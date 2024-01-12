package com.fabi.Model;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fabi.R;
import com.fabi.Controleur.LivreLocalActivity;

import java.util.List;

public class ElectroniqueAdapter extends RecyclerView.Adapter<ElectroniqueAdapter.MyViewHolder> {
    List<Electronique> mListElectronique;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public ElectroniqueAdapter(List<Electronique> listElectronique) {
        mListElectronique = listElectronique;
    }
    @Override
    public ElectroniqueAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.electronique_bloc,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Electronique item = mListElectronique.get(position);
        holder.display(mListElectronique.get(position));

    }
    @Override
    public int getItemCount() {
        return mListElectronique.size();
    }


//    public void Remove(int position){
//        mListNotif.remove(position);
//        notifyItemRemoved(position);
//    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView mLabel;
        private TextView mNbrLivre;
        MyViewHolder(View itemView){
            super(itemView);
            mLabel = (TextView) itemView.findViewById(R.id.label_mes_livres);
            mNbrLivre = (TextView) itemView.findViewById(R.id.nbr_livre);
        }
        //        @Override
//        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
////            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
////            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
////            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        void display(Electronique mesLivres2){
            mLabel.setText(mesLivres2.getLabel());
            mNbrLivre.setText("" + mesLivres2.getNbrLivre());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent local = new Intent(itemView.getContext(), LivreLocalActivity.class);
                    local.putExtra("id",mesLivres2.getId());
                    itemView.getContext().startActivity(local);
                }
            });
        }

    }
}
