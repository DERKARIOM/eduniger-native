package com.ninotech.eduniger.controleur.adapter;
import android.app.Activity;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.activity.MainActivity;
import com.ninotech.eduniger.model.data.Connection;

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
        private final RelativeLayout mWaitRelativeLayout;
        private final ImageView mNoConnectionImageView;
        private final TextView mTitleTextView;
        private final TextView mDescTextView;
        private final TextView mOffLineButton;
        private final Button mReloadButton;
        private final LinearLayout mActionLinearLayout;


        MyViewHolder(View itemView){
            super(itemView);
            mWaitRelativeLayout = itemView.findViewById(R.id.relative_adapter_no_connection_wait);
            mNoConnectionImageView = itemView.findViewById(R.id.image_view_adapter_no_connection);
            mTitleTextView = itemView.findViewById(R.id.text_view_adapter_no_connection_title);
            mDescTextView = itemView.findViewById(R.id.text_view_adapter_no_connection_desc);
            mOffLineButton = itemView.findViewById(R.id.button_adapter_no_connection_off_line);
            mReloadButton = itemView.findViewById(R.id.button_adapter_no_connection_re_load);
            mActionLinearLayout = itemView.findViewById(R.id.linear_layout_adapter_no_connection_action);

            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
        }
        void display(Connection connection){
            if(connection.isWait())
            {
                mWaitRelativeLayout.setVisibility(View.VISIBLE);
                mNoConnectionImageView.setVisibility(View.GONE);
                mTitleTextView.setVisibility(View.GONE);
                mDescTextView.setVisibility(View.GONE);
                mActionLinearLayout.setVisibility(View.GONE);
            }
            else
            {
                mWaitRelativeLayout.setVisibility(View.GONE);
                mNoConnectionImageView.setVisibility(View.VISIBLE);
                mTitleTextView.setVisibility(View.VISIBLE);
                mDescTextView.setVisibility(View.VISIBLE);
                mActionLinearLayout.setVisibility(View.VISIBLE);
            }
            mReloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(connection.getSource());
                    itemView.getContext().sendBroadcast(intent);
                }
            });
            mOffLineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mainIntent = new Intent(itemView.getContext(), MainActivity.class);
                    mainIntent.putExtra("HORS_LINE","ON");
                    itemView.getContext().startActivity(mainIntent);
                    ((Activity)itemView.getContext()).finish();
                }
            });
        }
    }
}