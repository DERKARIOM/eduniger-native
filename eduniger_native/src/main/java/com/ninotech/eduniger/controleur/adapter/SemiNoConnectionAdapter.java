package com.ninotech.eduniger.controleur.adapter;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.model.data.Connection;

import java.util.List;

public class SemiNoConnectionAdapter extends RecyclerView.Adapter<SemiNoConnectionAdapter.MyViewHolder> {
    List<Connection> mListConnection;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public SemiNoConnectionAdapter(List<Connection> listConnection) {
        mListConnection = listConnection;
    }
    @Override
    public SemiNoConnectionAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_semi_no_connection,parent,false);
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
        private final RelativeLayout mWaitRelativeLayout;
        private final ImageView mNoConnectionImageView;
        private final TextView mTitleTextView;
       private final Button mTryButton;
        MyViewHolder(View itemView){
            super(itemView);
            mWaitRelativeLayout = itemView.findViewById(R.id.relative_adapter_semi_no_connection_wait);
            mTryButton = itemView.findViewById(R.id.button_adapter_semi_no_connection_try);
            mNoConnectionImageView = itemView.findViewById(R.id.image_view_adapter_semi_no_connection);
            mTitleTextView = itemView.findViewById(R.id.text_view_adapter_semi_no_connection_title);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
        }
        void display(Connection connection){
            if(connection.isWait())
            {
                mWaitRelativeLayout.setVisibility(View.VISIBLE);
                mTryButton.setVisibility(View.GONE);
                mNoConnectionImageView.setVisibility(View.GONE);
                mTitleTextView.setVisibility(View.GONE);
            }
            else
            {
                mWaitRelativeLayout.setVisibility(View.GONE);
                mTryButton.setVisibility(View.VISIBLE);
                mNoConnectionImageView.setVisibility(View.VISIBLE);
                mTitleTextView.setVisibility(View.VISIBLE);
            }
            mTryButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(connection.getSource());
                    itemView.getContext().sendBroadcast(intent);
                }
            });
        }
    }
}