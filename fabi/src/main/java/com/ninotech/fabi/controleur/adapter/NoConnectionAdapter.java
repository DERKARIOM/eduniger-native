package com.ninotech.fabi.controleur.adapter;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.model.data.Connection;

import java.util.List;

public class NoConnectionAdapter extends RecyclerView.Adapter<NoConnectionAdapter.MyViewHolder> {
    List<Connection> mListConnection;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public NoConnectionAdapter(List<Connection> listConnection) {
        mListConnection = listConnection;
    }
    @Override
    public NoConnectionAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_no_connection,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Connection item = mListConnection.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mListConnection.get(position));

    }
    @Override
    public int getItemCount() {
        return mListConnection.size();
    }

    public Connection getItem(int position) {
        return mListConnection.get(position);
    }

    public void Remove(int position){
        mListConnection.remove(position);
        notifyItemRemoved(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
       private final TextView mMessageTextView;
       private final Button mTryButton;
       private final ProgressBar mWaitProgressBar;
        MyViewHolder(View itemView){
            super(itemView);
           mMessageTextView = itemView.findViewById(R.id.text_view_adapter_no_connection_message);
           mTryButton = itemView.findViewById(R.id.button_adapter_no_connection_try);
           mWaitProgressBar = itemView.findViewById(R.id.progress_bar_adapter_no_connection);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
        }
        void display(Connection connection){
            if(connection.isWait())
            {
                mWaitProgressBar.setVisibility(View.VISIBLE);
                mTryButton.setVisibility(View.INVISIBLE);
            }
            else
            {
                mTryButton.setVisibility(View.VISIBLE);
                mWaitProgressBar.setVisibility(View.INVISIBLE);
            }
            mTryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(connection.getSource());
                    itemView.getContext().sendBroadcast(intent);
                }
            });
           mMessageTextView.setText(connection.getMessage());
        }
    }
}