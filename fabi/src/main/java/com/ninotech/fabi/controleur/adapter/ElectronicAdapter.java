package com.ninotech.fabi.controleur.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.LivreLocalActivity;
import com.ninotech.fabi.model.data.Electronic;

import java.util.List;

public class ElectronicAdapter extends RecyclerView.Adapter<ElectronicAdapter.MyViewHolder> {
    List<Electronic> mListElectronic;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public ElectronicAdapter(List<Electronic> listElectronic) {
        mListElectronic = listElectronic;
    }
    @Override
    public ElectronicAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_electronic,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Electronic item = mListElectronic.get(position);
        holder.display(mListElectronic.get(position));

    }
    @Override
    public int getItemCount() {
        return mListElectronic.size();
    }


//    public void Remove(int position){
//        mListNotification.remove(position);
//        notifyItemRemoved(position);
//    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView mIcoImageView;
        private final TextView mLabel;
        private final TextView mNbrLivre;
        MyViewHolder(View itemView){
            super(itemView);
            mIcoImageView = itemView.findViewById(R.id.image_view_adapter_electronic);
            mLabel = (TextView) itemView.findViewById(R.id.text_view_adapter_electronic);
            mNbrLivre = (TextView) itemView.findViewById(R.id.text_view_adapter_electronic_number);
        }
        //        @Override
//        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
////            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
////            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
////            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        void display(Electronic mesLivres2){
            mIcoImageView.setImageResource(mesLivres2.getId());
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
