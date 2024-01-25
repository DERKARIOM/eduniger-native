package com.ninotech.fabi.controleur.adapter;

import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.LivreActivity;
import com.ninotech.fabi.model.data.Book;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.MyViewHolder> {
    List<Book> mListBook;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public BookAdapter(List<Book> listBook) {
        mListBook = listBook;
    }
    @Override
    public BookAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_book,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Book item = mListBook.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mListBook.get(position));

    }
    @Override
    public int getItemCount() {
        return mListBook.size();
    }

    public Book getItem(int position) {
        return mListBook.get(position);
    }

    public void Remove(int position){
        mListBook.remove(position);
        notifyItemRemoved(position);
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private final ImageView mBlanketImageView;
        private final TextView mTitleTextView;
        private final TextView mCategoryTextView;
        private final ImageView mPycicalImageView;
        private final ImageView mElectronicImageView;
        private final ImageView mAudioImageView;
        private final TextView mNumberLikeTextView;
        private final TextView mNumberViewTextView;
        MyViewHolder(View itemView){
            super(itemView);
            mBlanketImageView = itemView.findViewById(R.id.couverture_livre);
            mTitleTextView = itemView.findViewById(R.id.title_livre);
            mCategoryTextView = itemView.findViewById(R.id.categorie_livre);
            mPycicalImageView = itemView.findViewById(R.id.ico_pysique);
            mElectronicImageView = itemView.findViewById(R.id.ico_pdf);
            mAudioImageView = itemView.findViewById(R.id.ico_audio);
            mNumberLikeTextView = itemView.findViewById(R.id.nbr_like);
            mNumberViewTextView = itemView.findViewById(R.id.nbr_vue);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
        }
        void display(Book book){
            Picasso.with(itemView.getContext())
                    .load("http://192.168.43.1:2222/fabi/couverture/" + book.getBlanket())
                    .placeholder(R.drawable.img_default_livre)
                    .error(R.drawable.img_default_livre)
                    .transform(new RoundedTransformation(15,4))
                    .resize(178,284)
                    .into(mBlanketImageView);
            mTitleTextView.setText(book.getTitle());
            StringBuilder category = new StringBuilder(".");
            for(int i=0;i<book.getCategory().size();i++)
                category.append(book.getCategory().get(i));
            mCategoryTextView.setText(category);
            mNumberLikeTextView.setText(book.getNumberLikes());
            mNumberViewTextView.setText(book.getNumberView());
            if(book.getIsPysical().equals("1"))
                mPycicalImageView.setVisibility(View.VISIBLE);
            if(!book.getElectronic().equals("null"))
                mElectronicImageView.setVisibility(View.VISIBLE);
            if(book.getIsAudio().equals("1"))
                mAudioImageView.setVisibility(View.VISIBLE);
            mNumberLikeTextView.setText(book.getNumberLikes());
            mNumberViewTextView.setText(book.getNumberView());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intentLivre = new Intent(itemView.getContext(), LivreActivity.class);
                    intentLivre.putExtra("idLivre",book.getId());
                    itemView.getContext().startActivity(intentLivre);
                }
            });
        }
    }
}