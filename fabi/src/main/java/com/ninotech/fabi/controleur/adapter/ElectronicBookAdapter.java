package com.ninotech.fabi.controleur.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.ElectronicBook;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
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
        View view = layoutInflater.inflate(R.layout.adapter_book_simple,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ElectronicBook item = mElectronicBookList.get(position);
        int i = position;
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

    public void Remove(int position){
        mElectronicBookList.remove(position);
        notifyItemRemoved(position);
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
            mCategoryTextView = itemView.findViewById(R.id.text_view_adapter_book_simple_category);
            mAuthorTextView = itemView.findViewById(R.id.text_view_adapter_book_simple_author);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
        }
        void display(ElectronicBook electronicBook){
            File file = bitmapToFile(itemView.getContext(), "image" + String.valueOf(electronicBook.getId()) + ".png", electronicBook.getCover());
            Picasso.get().load(file)
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .transform(new RoundedTransformation(15,4))
                    .resize(198,304)
                    .into(mCoverImageView);
            mTitleTextView.setText(electronicBook.getTile());
            mCategoryTextView.setText(electronicBook.getCategory());
            mAuthorTextView.setText(electronicBook.getAuthor());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPDFWithAdobeReader(new File(electronicBook.getPdf()));
                }
            });
        }
        private void openPDFWithAdobeReader(File pdfFile) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri pdfUri = FileProvider.getUriForFile(itemView.getContext(), itemView.getContext().getApplicationContext().getPackageName() + ".provider", pdfFile);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                itemView.getContext().startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(itemView.getContext(), "OpenPDF : " + e, Toast.LENGTH_LONG).show();

            }
        }
        public File bitmapToFile(Context context, String filename, Bitmap bitmap) {
            // Créer un fichier dans le répertoire de cache de l'application
            File file = new File(context.getCacheDir(), filename);
            try {
                // Convertir le Bitmap en un fichier de sortie
                file.createNewFile();
                FileOutputStream ostream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                ostream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return file;
        }
    }
}