package com.naniger.elim.controleur.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.naniger.elim.R;
import com.naniger.elim.controleur.activity.AudioPlayerActivity;
import com.naniger.elim.controleur.activity.PdfBoxViewerActivity;
import com.naniger.elim.model.data.LocalBooks;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.MyViewHolder> {
    List<LocalBooks> mListLocalBooks;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public RecentAdapter(List<LocalBooks> listLocalBooks) {
        mListLocalBooks = listLocalBooks;
    }
    @Override
    public RecentAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_book_horizotal,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        LocalBooks item = mListLocalBooks.get(position);
        try {
            holder.display(mListLocalBooks.get(position));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    public int getItemCount() {
        return mListLocalBooks.size();
    }


//    public void Remove(int position){
//        mListNotification.remove(position);
//        notifyItemRemoved(position);
//    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        private final ImageView mCoverImageView;
        private final TextView mTitleTextView;
        private final TextView mMetaTextView;
        private final TextView mBadgeTextView;

        MyViewHolder(View itemView){
            super(itemView);
            mCoverImageView = itemView.findViewById(R.id.image_view_adapter_similar_cover);
            mTitleTextView = itemView.findViewById(R.id.text_view_adapter_book_title);
            mMetaTextView = itemView.findViewById(R.id.text_view_adapter_book_meta);
            mBadgeTextView = itemView.findViewById(R.id.text_view_adapter_book_badge);
        }
        void display(LocalBooks localBooks) throws SQLException, IOException {
            // Même carte que les livres en ligne (HorizontaleAdapter) : couverture
            // arrondie par la vue, titre, catégorie et pastille « Audio ».
            Picasso.get().load(new File(localBooks.getCover()))
                    .placeholder(R.drawable.img_wait_cover_book)
                    .error(R.drawable.img_wait_cover_book)
                    .fit()
                    .centerCrop()
                    .into(mCoverImageView);
            boolean isAudio = "audio".equals(localBooks.getFormat());
            String title = HorizontaleAdapter.hasText(localBooks.getTitle()) ? localBooks.getTitle() : "";
            String meta = HorizontaleAdapter.hasText(localBooks.getCategory()) ? localBooks.getCategory()
                    : itemView.getContext().getString(isAudio ? R.string.book_card_format_audio : R.string.book_card_format_pdf);
            mTitleTextView.setText(title);
            mMetaTextView.setText(meta);
            mBadgeTextView.setVisibility(isAudio ? View.VISIBLE : View.GONE);
            itemView.setContentDescription(title + ", " + meta);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (localBooks.getFormat())
                    {
                        case "pdf":
                            Intent intent = new Intent(itemView.getContext(), PdfBoxViewerActivity.class);
                            intent.putExtra("PDF_PATH", localBooks.getRessource());
                            intent.putExtra("PDF_TITLE", localBooks.getTitle());
                            itemView.getContext().startActivity(intent);
                            break;
                        case "audio":
                            Intent audioPayerIntent = new Intent(itemView.getContext(), AudioPlayerActivity.class);
                            audioPayerIntent.putExtra("key_adapter_audio_book_id",localBooks.getId());
                            audioPayerIntent.putExtra("list_audio_source","all");
                            itemView.getContext().startActivity(audioPayerIntent);
                            break;
                    }
                }
            });
        }
    }
}
