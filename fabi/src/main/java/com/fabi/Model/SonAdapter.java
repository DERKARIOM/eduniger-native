package com.fabi.Model;

import android.content.Intent;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fabi.R;

import java.util.List;

public class SonAdapter extends RecyclerView.Adapter<SonAdapter.MyViewHolder> {
    List<Son> mListSon;

    public String getAudio() {
        return mAudio;
    }

    public void setAudio(String audio) {
        mAudio = audio;
    }

    private String mAudio;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public SonAdapter(List<Son> listSon) {
        mListSon = listSon;
    }
    @Override
    public SonAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_son,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Son item = mListSon.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mListSon.get(position));

    }
    @Override
    public int getItemCount() {
        return mListSon.size();
    }

    public Son getItem(int position) {
        return mListSon.get(position);
    }

    public void Remove(int position){
        mListSon.remove(position);
        notifyItemRemoved(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private TextView mNum;
        private TextView mTitre;
        private int tmp=0;
        MyViewHolder(View itemView){
            super(itemView);
            mNum = itemView.findViewById(R.id.numSon);
            mTitre = itemView.findViewById(R.id.titre_son);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }
        void display(Son son){
            mNum.setText(String.valueOf(son.getNum()));
            mTitre.setText(son.getTitre());
            if(son.isPlaying())
                mTitre.setTextColor(Color.parseColor("#D8125CDC"));
            else
                mTitre.setTextColor(Color.parseColor("#DF444444"));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    son.setPlaying(true);
                    Intent intent3 = new Intent("ACTION_AUDIO");
                    intent3.putExtra("nomAudio", son.getAudio());
                    intent3.putExtra("position", getPosition());
                    itemView.getContext().sendBroadcast(intent3);
                }
            });
        }
    }
}