package com.ninotech.eduniger.controleur.adapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.model.data.VoidContainer;

import java.util.List;

public class VoidContainerAdapter extends RecyclerView.Adapter<VoidContainerAdapter.MyViewHolder> {
    List<VoidContainer> mVoidContainers;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public VoidContainerAdapter(List<VoidContainer> voidContainers) {
        mVoidContainers = voidContainers;
    }
    @Override
    public VoidContainerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_void_container,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        VoidContainer item = mVoidContainers.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mVoidContainers.get(position));

    }
    @Override
    public int getItemCount() {
        return mVoidContainers.size();
    }

    public VoidContainer getItem(int position) {
        return mVoidContainers.get(position);
    }

    public void Remove(int position){
        mVoidContainers.remove(position);
        notifyItemRemoved(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private ImageView mImageView;
        private TextView mMessageTextView;
        MyViewHolder(View itemView){
            super(itemView);
            mImageView = itemView.findViewById(R.id.image_view_adapter_void_container);
            mMessageTextView = itemView.findViewById(R.id.text_view_adapter_container_message);
            //mButton = (Button) itemView.findViewById(R.id.bttAnex);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }
        void display(VoidContainer voidContainer){
            mImageView.setImageResource(voidContainer.getImage());
            mMessageTextView.setText(voidContainer.getMessage());
        }
    }
}