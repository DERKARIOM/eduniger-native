package com.naniger.elim.controleur.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.naniger.elim.R;
import com.naniger.elim.controleur.activity.AddBookActivity;
import com.naniger.elim.controleur.activity.BookActivity;
import com.naniger.elim.model.data.Book;
import com.naniger.elim.model.data.OnlineBook;
import com.naniger.elim.model.data.Server;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class HorizontaleAdapter extends RecyclerView.Adapter<HorizontaleAdapter.MyViewHolder> {
    List<OnlineBook> mBooks;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public HorizontaleAdapter(List<OnlineBook> books) {
        mBooks = books;
    }
    @Override
    public HorizontaleAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_book_horizotal,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Book item = mBooks.get(position);
        try {
            holder.display(mBooks.get(position));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    /** Valeurs « oui » renvoyées selon les routes de l'API ("1", "true", "oui"). */
    static boolean isYes(String value) {
        if (value == null) return false;
        String v = value.trim().toLowerCase(java.util.Locale.ROOT);
        return v.equals("1") || v.equals("true") || v.equals("oui") || v.equals("yes");
    }

    static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty() && !"null".equalsIgnoreCase(value.trim());
    }

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

        void display(OnlineBook book) throws SQLException, IOException {
            Context context = itemView.getContext();
            boolean isAddCard = "add".equals(book.getId());
            boolean isAudio = !isAddCard && isYes(book.getIsAudio());
            boolean hasPdf = !isAddCard && hasText(book.getElectronic());

            // Couverture : recadrée pour remplir la carte, coins arrondis par la vue
            // (plus de transformation bitmap : moins de mémoire et de calcul).
            Picasso.get()
                    .load(Server.getUrlHostProd(context) + "/api/public/resource/" + book.getIdStruct() + "/blanket/" + book.getCover())
                    .placeholder(R.drawable.img_wait_cover_book)
                    .error(R.drawable.img_wait_cover_book)
                    .fit()
                    .centerCrop()
                    .into(mCoverImageView);

            String title = hasText(book.getTitle()) ? book.getTitle() : "";
            mTitleTextView.setText(title);

            // Ligne d'information : catégorie, sinon format disponible.
            String meta;
            if (isAddCard) {
                meta = "";
            } else if (hasText(book.getCategory())) {
                meta = book.getCategory();
            } else if (hasPdf) {
                meta = context.getString(R.string.book_card_format_pdf);
            } else if (isAudio) {
                meta = context.getString(R.string.book_card_format_audio);
            } else if (isYes(book.getIsPhysic())) {
                meta = context.getString(R.string.book_card_format_physical);
            } else {
                meta = "";
            }
            mMetaTextView.setText(meta);
            mMetaTextView.setVisibility(meta.isEmpty() ? View.GONE : View.VISIBLE);
            mBadgeTextView.setVisibility(isAudio ? View.VISIBLE : View.GONE);

            // Lecteur d'écran : une seule annonce claire pour toute la carte.
            StringBuilder a11y = new StringBuilder(title);
            if (!meta.isEmpty()) a11y.append(", ").append(meta);
            if (isAudio) a11y.append(", ").append(context.getString(R.string.book_card_a11y_audio));
            itemView.setContentDescription(a11y);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isAddCard)
                    {
                        Intent intentBook = new Intent(itemView.getContext(), BookActivity.class);
                        intentBook.putExtra("intent_adapter_book_id", book.getId());
                        itemView.getContext().startActivity(intentBook);
                    }
                    else
                    {
                        Intent intentAddBook = new Intent(itemView.getContext(), AddBookActivity.class);
                        itemView.getContext().startActivity(intentAddBook);
                    }
                }
            });
        }

    }
}
