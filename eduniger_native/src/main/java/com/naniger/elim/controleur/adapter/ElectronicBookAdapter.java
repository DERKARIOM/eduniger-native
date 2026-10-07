package com.naniger.elim.controleur.adapter;

import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.naniger.elim.R;
import com.naniger.elim.controleur.activity.BookActivity;
import com.naniger.elim.controleur.activity.PdfBoxViewerActivity;
import com.naniger.elim.controleur.animation.RoundedTransformation;
import com.naniger.elim.model.data.ElectronicBook;
import com.naniger.elim.model.table.ElectronicTable;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ElectronicBookAdapter extends RecyclerView.Adapter<ElectronicBookAdapter.MyViewHolder> {
    List<ElectronicBook> mElectronicBookList;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;

    public ElectronicBookAdapter(List<ElectronicBook> electronicBooks) {
        mElectronicBookList = electronicBooks;
    }

    @Override
    public ElectronicBookAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_book_simple, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ElectronicBook item = mElectronicBookList.get(position);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mElectronicBookList.get(position));
    }

    @Override
    public int getItemCount() {
        return mElectronicBookList.size();
    }

    public ElectronicBook getItem(int position) {
        return mElectronicBookList.get(position);
    }

    public void Remove(int position) {
        mElectronicBookList.remove(position);
        notifyItemRemoved(position);
    }

    public void filterList(ArrayList<ElectronicBook> filteredList) {
        mElectronicBookList = filteredList;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private ImageView mCoverImageView;
        private TextView mTitleTextView;
        private TextView mCategoryTextView;
        private TextView mAuthorTextView;

        MyViewHolder(View itemView) {
            super(itemView);
            mCoverImageView = itemView.findViewById(R.id.image_view_adapter_book_simple_cover);
            mTitleTextView = itemView.findViewById(R.id.text_view_adapter_book_simple_title);
            mCategoryTextView = itemView.findViewById(R.id.text_view_adapter_description_category);
            mAuthorTextView = itemView.findViewById(R.id.text_view_adapter_book_simple_author);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        }

        void display(ElectronicBook electronicBook) {
            File file = new File(electronicBook.getCover());
            Picasso.get().load(file)
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .transform(new RoundedTransformation(15, 4))
                    .resize(198, 304)
                    .into(mCoverImageView);
            mTitleTextView.setText(electronicBook.getTitle());
            mAuthorTextView.setText("De " + electronicBook.getAuthor());

            // Etat reel du telechargement (cf. ElectronicTable.STATUS_*, propage
            // par ContainerActivity depuis la colonne statusElectronic) : "Mes
            // telechargements" doit refleter DOWNLOADING/FAILED et pas seulement
            // ouvrir un PDF potentiellement absent/partiel comme si tout etait
            // termine (audit sections 4/6/8). null = ancienne ligne ecrite avant
            // l'ajout du statut -> traitee comme COMPLETED par retro-compatibilite.
            String status = electronicBook.getStatus();
            if (ElectronicTable.STATUS_DOWNLOADING.equals(status)) {
                mCategoryTextView.setText("Téléchargement en cours...");
                itemView.setOnClickListener(v -> Toast.makeText(
                        itemView.getContext(), "Téléchargement en cours...", Toast.LENGTH_SHORT).show());
            } else if (ElectronicTable.STATUS_FAILED.equals(status)) {
                mCategoryTextView.setText("Échec — appuyez pour réessayer");
                itemView.setOnClickListener(v -> {
                    // Reouvre la fiche du livre (meme flux que le catalogue) plutot
                    // que de dupliquer ici la logique de relance du telechargement :
                    // BookActivity connait deja l'etat reel et propose "Réessayer"
                    // (cf. configureElectronicFormat()/handlePdfDownload()).
                    Intent intent = new Intent(itemView.getContext(), BookActivity.class);
                    intent.putExtra("intent_adapter_book_id", electronicBook.getId());
                    itemView.getContext().startActivity(intent);
                });
            } else {
                mCategoryTextView.setText("Catégorie : " + electronicBook.getCategory());
                itemView.setOnClickListener(v -> {
                    // Ouvrir le PDF avec PDFBox
                    Intent intent = new Intent(itemView.getContext(), PdfBoxViewerActivity.class);
                    intent.putExtra("PDF_PATH", electronicBook.getPdf());
                    intent.putExtra("PDF_TITLE", electronicBook.getTitle());
                    itemView.getContext().startActivity(intent);
                });
            }
        }
    }
}
