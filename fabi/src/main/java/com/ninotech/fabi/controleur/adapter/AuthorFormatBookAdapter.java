package com.ninotech.fabi.controleur.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.ContainerActivity;
import com.ninotech.fabi.controleur.activity.SearchActivity;
import com.ninotech.fabi.model.data.Library;

import java.util.List;

public class AuthorFormatBookAdapter extends RecyclerView.Adapter<AuthorFormatBookAdapter.MyViewHolder> {
    List<Library> mListLibrary;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public AuthorFormatBookAdapter(List<Library> listLibrary) {
        mListLibrary = listLibrary;
    }
    @Override
    public AuthorFormatBookAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
            mNbrLivre.setText(String.valueOf(library.getNumber()));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent searchIntent = new Intent(itemView.getContext(), SearchActivity.class);
                    searchIntent.putExtra("search_key", "ONLINE_BOOK");
                    searchIntent.putExtra("id_author_key",library.getIdAuthor());
                    switch (library.getId())
                    {
                        case 1:
                            searchIntent.putExtra("online_book_key", "AUTHOR_FORMAT_BOOK_PDF_ADAPTER");
                            break;
                        case 2:
                            searchIntent.putExtra("online_book_key", "AUTHOR_FORMAT_BOOK_AUDIO_ADAPTER");
                            break;
                        case 3:
                            searchIntent.putExtra("online_book_key", "AUTHOR_FORMAT_BOOK_PHYSIC_ADAPTER");
                            break;
                    }
                    itemView.getContext().startActivity(searchIntent);
                }
            });
        }

    }
}
