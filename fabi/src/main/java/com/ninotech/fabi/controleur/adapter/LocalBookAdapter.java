package com.ninotech.fabi.controleur.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.AudioPlayerActivity;
import com.ninotech.fabi.controleur.activity.PdfNinoView;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.ElectronicBook;
import com.ninotech.fabi.model.data.LocalBooks;
import com.pspdfkit.configuration.activity.PdfActivityConfiguration;
import com.pspdfkit.configuration.page.PageScrollDirection;
import com.pspdfkit.configuration.page.PageScrollMode;
import com.pspdfkit.configuration.settings.SettingsMenuItemType;
import com.pspdfkit.configuration.sharing.ShareFeatures;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
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
                            File file = new File(localBooks.getRessource());
                            Uri uri = Uri.parse(Uri.fromFile(file).toString());
                            PdfActivityConfiguration config = new PdfActivityConfiguration.Builder(itemView.getContext())
                                    .hideThumbnailGrid().setEnabledShareFeatures(ShareFeatures.none())
                                    .disablePrinting()
                                    .disablePrinting()
                                    .disableAnnotationEditing()
                                    .disableBookmarkEditing()
                                    .disableDocumentEditor()
                                    .disableAnnotationList()
                                    .scrollDirection(PageScrollDirection.VERTICAL)
                                    .scrollMode(PageScrollMode.CONTINUOUS)
                                    .disableAnnotationLimitedToPageBounds()
                                    .disableCopyPaste()
                                    .disableFormEditing()
                                    .disableContentEditing()
                                    .textSelectionEnabled(false)
                                    .enableDocumentInfoView()
                                    .setSettingsMenuItems(EnumSet.of(
                                            SettingsMenuItemType.THEME,
                                            SettingsMenuItemType.PAGE_LAYOUT,
                                            SettingsMenuItemType.PAGE_TRANSITION,
                                            SettingsMenuItemType.PRESETS
                                    ))
                                    .build();
                            PdfNinoView.showDocument(itemView.getContext(),uri,config);
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