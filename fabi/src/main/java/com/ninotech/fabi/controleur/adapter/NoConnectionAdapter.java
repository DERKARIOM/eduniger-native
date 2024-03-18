package com.ninotech.fabi.controleur.adapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;

import java.util.List;

public class NoConnectionAdapter extends RecyclerView.Adapter<NoConnectionAdapter.MyViewHolder> {
    List<String> mListMessage;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public NoConnectionAdapter(List<String> listMessage) {
        mListMessage = listMessage;
    }
    @Override
    public NoConnectionAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_no_connection,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String item = mListMessage.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mListMessage.get(position));

    }
    @Override
    public int getItemCount() {
        return mListMessage.size();
    }

    public String getItem(int position) {
        return mListMessage.get(position);
    }

    public void Remove(int position){
        mListMessage.remove(position);
        notifyItemRemoved(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
       private final TextView mMessageTextView;
       private final Button mTryButton;
        MyViewHolder(View itemView){
            super(itemView);
           mMessageTextView = itemView.findViewById(R.id.text_view_adapter_no_connection_message);
           mTryButton = itemView.findViewById(R.id.button_adapter_no_connection_try);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
        }
        void display(String message){
           mMessageTextView.setText(message);

        }
    }
}