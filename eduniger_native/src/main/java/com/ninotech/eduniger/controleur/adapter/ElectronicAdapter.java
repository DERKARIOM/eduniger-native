package com.ninotech.eduniger.controleur.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.activity.AddBookActivity;
import com.ninotech.eduniger.controleur.activity.ContainerActivity;
import com.ninotech.eduniger.model.data.Library;

import java.util.List;

public class ElectronicAdapter extends RecyclerView.Adapter<ElectronicAdapter.MyViewHolder> {
    List<Library> mListLibrary;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public ElectronicAdapter(List<Library> listLibrary) {
        mListLibrary = listLibrary;
    }
    @Override
    public ElectronicAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_electronic,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Library item = mListLibrary.get(position);
        holder.display(mListLibrary.get(position));

    }
    @Override
    public int getItemCount() {
        return mListLibrary.size();
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
        void display(Library library){
            mIcoImageView.setImageResource(library.getIcon());
            mLabel.setText(library.getLabel());
            if (library.getNumber() == -1)
               mNbrLivre.setText("Admine");
            else
                mNbrLivre.setText(String.valueOf(library.getNumber()));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (library.getId() != 6)
                    {
                        Intent local = new Intent(itemView.getContext(), ContainerActivity.class);
                        local.putExtra("id",library.getId());
                        itemView.getContext().startActivity(local);
                    }
                    else
                    {
                        Intent addBook = new Intent(itemView.getContext(), AddBookActivity.class);
                        itemView.getContext().startActivity(addBook);
                    }

                }
            });
        }

    }
}
