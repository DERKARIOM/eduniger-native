package com.ninotech.fabi.controleur.adapter;

import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.BookActivity;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.OnlineBook;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FabiolaBookAdapter extends RecyclerView.Adapter<FabiolaBookAdapter.MyViewHolder> {
    List<OnlineBook> mOnlineBooks;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public FabiolaBookAdapter(List<OnlineBook> onlineBooks) {
        mOnlineBooks = onlineBooks;
    }
    @Override
    public FabiolaBookAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_book,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        OnlineBook item = mOnlineBooks.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mOnlineBooks.get(position));

    }
    @Override
    public int getItemCount() {
        return mOnlineBooks.size();
    }

    public OnlineBook getItem(int position) {
        return mOnlineBooks.get(position);
    }

    public void Remove(int position){
        mOnlineBooks.remove(position);
        notifyItemRemoved(position);
    }
    public void filterList(ArrayList<OnlineBook> filteredList) {
        mOnlineBooks = filteredList;
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private final ImageView mBlanketImageView;
        private final TextView mTitleTextView;
        private final TextView mCategoryTextView;
        private final ImageView mPysicalImageView;
        private final ImageView mElectronicImageView;
        private final ImageView mAudioImageView;
        private final TextView mNumberLikeTextView;
        private final TextView mNumberViewTextView;
        MyViewHolder(View itemView){
            super(itemView);
            mBlanketImageView = itemView.findViewById(R.id.image_view_adapter_book_audio_blanket);
            mTitleTextView = itemView.findViewById(R.id.text_view_adapter_book_title);
            mCategoryTextView = itemView.findViewById(R.id.text_view_adapter_book_category);
            mPysicalImageView = itemView.findViewById(R.id.image_view_adapter_book_physical);
            mElectronicImageView = itemView.findViewById(R.id.image_view_adapter_book_electronic);
            mAudioImageView = itemView.findViewById(R.id.image_view_adapter_book_audio);
            mNumberLikeTextView = itemView.findViewById(R.id.text_view_activity_book_number_like);
            mNumberViewTextView = itemView.findViewById(R.id.text_view_adapter_book_number_view);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
        }
        void display(OnlineBook onlineBook){
            Picasso.get()
                    .load(itemView.getResources().getString(R.string.ip_server) + "ressources/cover/" + onlineBook.getCover())
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .transform(new RoundedTransformation(15,4))
                    .resize(178,284)
                    .into(mBlanketImageView);
            mTitleTextView.setText(onlineBook.getTitle());
            mCategoryTextView.setText(onlineBook.getCategory());
            mNumberLikeTextView.setText(String.valueOf(onlineBook.getNumberLikes()));
            mNumberViewTextView.setText(String.valueOf(onlineBook.getNumberView()));
            if(onlineBook.getIsPhysic().equals("1"))
                mPysicalImageView.setVisibility(View.VISIBLE);
            if(!onlineBook.getElectronic().equals("null"))
                mElectronicImageView.setVisibility(View.VISIBLE);
            if(onlineBook.getIsAudio().equals("1"))
                mAudioImageView.setVisibility(View.VISIBLE);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent recoverBookIntent = new Intent("ACTION_RECOVER_BOOK");
                    recoverBookIntent.putExtra("idBook", onlineBook.getId());
                    recoverBookIntent.putExtra("titleBook", onlineBook.getTitle());
                    itemView.getContext().sendBroadcast(recoverBookIntent);
//                    Intent intentBook = new Intent(itemView.getContext(), BookActivity.class);
//                    intentBook.putExtra("intent_adapter_book_id", onlineBook.getId());
//                    itemView.getContext().startActivity(intentBook);
                }
            });
        }
    }
}