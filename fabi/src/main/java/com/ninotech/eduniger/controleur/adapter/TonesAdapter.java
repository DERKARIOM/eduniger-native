package com.ninotech.eduniger.controleur.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.model.data.Tones;

import java.util.List;

public class TonesAdapter extends RecyclerView.Adapter<TonesAdapter.MyViewHolder> {
    List<Tones> mListTones;

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
    public TonesAdapter(List<Tones> listTones) {
        mListTones = listTones;
    }
    @Override
    public TonesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_tones,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Tones item = mListTones.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mListTones.get(position));

    }
    @Override
    public int getItemCount() {
        return mListTones.size();
    }

    public Tones getItem(int position) {
        return mListTones.get(position);
    }

    public void Remove(int position){
        mListTones.remove(position);
        notifyItemRemoved(position);
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private final TextView mNumberTextView;
        private final TextView mTitleTextView;
        MyViewHolder(View itemView){
            super(itemView);
            mNumberTextView = itemView.findViewById(R.id.text_view_adapter_tones_number);
            mTitleTextView = itemView.findViewById(R.id.text_view_adapter_tones_title);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
        }
        void display(Tones tones){
            mNumberTextView.setText(String.valueOf(tones.getNumber()));
            mTitleTextView.setText(tones.getTitle());
            if(tones.isPlaying())
                mTitleTextView.setTextColor(itemView.getResources().getColor(R.color.purple2_200));
            else
                mTitleTextView.setTextColor(itemView.getResources().getColor(R.color.black3));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        tones.setPlaying(true);
                        Intent intent3 = new Intent("ACTION_AUDIO");
                        intent3.putExtra("intent_adapter_tones_title", tones.getAudio());
                        intent3.putExtra("intent_adapter_tones_position", getPosition());
                        itemView.getContext().sendBroadcast(intent3);
                    }catch (ExceptionInInitializerError e)
                    {
                        Log.e("errTonesAdapter",e.getMessage());
                    }catch (Exception e)
                    {
                        Log.e("errorTonesAdapter",e.getMessage());
                    }
                }
            });
        }
    }
}