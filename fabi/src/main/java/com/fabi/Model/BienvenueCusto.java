package com.fabi.Model;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fabi.R;

import java.util.List;

public class BienvenueCusto extends RecyclerView.Adapter<BienvenueCusto.MyViewHolder> {
    List<String> mListNote;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public BienvenueCusto(List<String> listNote) {
        mListNote = listNote;
    }
    @Override
    public BienvenueCusto.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.bienvenue_block_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String item = mListNote.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mListNote.get(position));

    }
    @Override
    public int getItemCount() {
        return mListNote.size();
    }

    public String getItem(int position) {
        return mListNote.get(position);
    }

    public void Remove(int position){
        mListNote.remove(position);
        notifyItemRemoved(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private TextView mTextView;
        MyViewHolder(View itemView){
            super(itemView);
            mTextView = itemView.findViewById(R.id.textWellcom);
            //mButton = (Button) itemView.findViewById(R.id.bttAnex);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }
        void display(String ue){
            mTextView.setText(ue);
        }
    }
}