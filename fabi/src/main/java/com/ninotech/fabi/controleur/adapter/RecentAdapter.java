package com.ninotech.fabi.controleur.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.AudioPlayerActivity;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.LocalBooks;
import com.pspdfkit.configuration.activity.PdfActivityConfiguration;
import com.pspdfkit.configuration.page.PageScrollDirection;
import com.pspdfkit.configuration.page.PageScrollMode;
import com.pspdfkit.configuration.settings.SettingsMenuItemType;
import com.pspdfkit.configuration.sharing.ShareFeatures;
import com.pspdfkit.ui.PdfActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.EnumSet;
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
        MyViewHolder(View itemView){
            super(itemView);
            mCoverImageView = (ImageView) itemView.findViewById(R.id.image_view_adapter_similar_cover);
        }
        void display(LocalBooks localBooks) throws SQLException, IOException {
            // Convertir le Bitmap en un fichier
            File file = new File(localBooks.getCover());

            // Charger le fichier avec Picasso
            Picasso.get().load(file)
                    .placeholder(R.drawable.img_wait_cover_book)
                    .error(R.drawable.img_wait_cover_book)
                    .transform(new RoundedTransformation(15,4))
                    .resize(198,304)
                    .into(mCoverImageView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (localBooks.getFormat())
                    {
                        case "pdf":
                            File file = new File(localBooks.getPDF());
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
                            PdfActivity.showDocument(itemView.getContext(),uri,config);
                            break;
                        case "audio":
                            Intent audioPayerIntent = new Intent(itemView.getContext(), AudioPlayerActivity.class);
                            audioPayerIntent.putExtra("key_adapter_audio_book_id",localBooks.getId());
                            itemView.getContext().startActivity(audioPayerIntent);
                            break;
                    }
                }
            });
        }
    }
}
