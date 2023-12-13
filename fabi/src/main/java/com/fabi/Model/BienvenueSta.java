package com.fabi.Model;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fabi.R;

import java.util.List;

public class BienvenueSta extends RecyclerView.Adapter<BienvenueSta.MyViewHolder> {
    List<String> mListNote;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public BienvenueSta(List<String> listNote) {
        mListNote = listNote;
    }
    @Override
    public BienvenueSta.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.bienvenue_block_sta,parent,false);
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
        //private TextView mTextView;
        MyViewHolder(View itemView){
            super(itemView);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
        }
        void display(String ue){

        }
    }
}