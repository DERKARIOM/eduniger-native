package com.ninotech.eduniger.controleur.adapter;

import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.activity.AudioPlayerActivity;
import com.ninotech.eduniger.controleur.activity.PdfBoxViewerActivity;
import com.ninotech.eduniger.controleur.animation.RoundedTransformation;
import com.ninotech.eduniger.model.data.LocalBooks;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalBookAdapter extends RecyclerView.Adapter<LocalBookAdapter.MyViewHolder> {
    List<LocalBooks> mLocalBooks;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public LocalBookAdapter(List<LocalBooks> localBooks) {
        mLocalBooks = localBooks;
    }
    @Override
    public LocalBookAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_book_simple,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        LocalBooks item = mLocalBooks.get(position);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mLocalBooks.get(position));

    }
    @Override
    public int getItemCount() {
        return mLocalBooks.size();
    }

    public LocalBooks getItem(int position) {
        return mLocalBooks.get(position);
    }

    public void Remove(int position){
        mLocalBooks.remove(position);
        notifyItemRemoved(position);
    }

    public void filterList(ArrayList<LocalBooks> filteredList) {
        mLocalBooks = filteredList;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private ImageView mCoverImageView;
      private TextView mTitleTextView;
      private  TextView mCategoryTextView;
      private TextView mAuthorTextView;
        MyViewHolder(View itemView){
            super(itemView);
            mCoverImageView = itemView.findViewById(R.id.image_view_adapter_book_simple_cover);
            mTitleTextView = itemView.findViewById(R.id.text_view_adapter_book_simple_title);
            mCategoryTextView = itemView.findViewById(R.id.text_view_adapter_description_category);
            mAuthorTextView = itemView.findViewById(R.id.text_view_adapter_book_simple_author);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
        }
        void display(LocalBooks localBooks){
            File file = new File(localBooks.getCover());
            Picasso.get().load(file)
                    .placeholder(R.drawable.img_wait_cover_book)
                    .error(R.drawable.img_wait_cover_book)
                    .transform(new RoundedTransformation(15,4))
                    .resize(198,304)
                    .into(mCoverImageView);
            mTitleTextView.setText(localBooks.getTitle());
            mAuthorTextView.setText("De " + localBooks.getAuthor());
            mCategoryTextView.setText("Format : " + localBooks.getFormat());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (localBooks.getFormat())
                    {
                        case "Électronique":
                            // Ouvrir le PDF avec PDFBox
                            Intent intent = new Intent(itemView.getContext(), PdfBoxViewerActivity.class);
                            intent.putExtra("PDF_PATH", localBooks.getRessource());
                            intent.putExtra("PDF_TITLE", localBooks.getTitle());
                            itemView.getContext().startActivity(intent);
                            break;
                        case "Audio":
                            Intent audioPayerIntent = new Intent(itemView.getContext(), AudioPlayerActivity.class);
                            audioPayerIntent.putExtra("key_adapter_audio_book_id",localBooks.getId());
                            if (localBooks.getPage().equals("category"))
                            {
                                audioPayerIntent.putExtra("list_audio_source","category");
                                audioPayerIntent.putExtra("type",localBooks.getCategory());
                            }
                            else
                            {
                                audioPayerIntent.putExtra("list_audio_source","author");
                                audioPayerIntent.putExtra("type",localBooks.getAuthor());
                            }
                            itemView.getContext().startActivity(audioPayerIntent);
                            break;
                    }
                }
            });
        }
    }
}