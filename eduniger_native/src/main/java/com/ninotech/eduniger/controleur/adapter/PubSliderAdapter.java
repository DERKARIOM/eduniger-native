package com.ninotech.eduniger.controleur.adapter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.eduniger.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PubSliderAdapter extends RecyclerView.Adapter<PubSliderAdapter.SlideViewHolder> {

    private final List<String> mImageUrls;
    private final int mTargetWidth;
    private final int mTargetHeight;

    public PubSliderAdapter(Context context, List<String> imageUrls) {
        this.mImageUrls = imageUrls;

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int marginPx  = (int) (15 * metrics.density * 2);
        mTargetWidth  = metrics.widthPixels - marginPx;
        mTargetHeight = (int) (160 * metrics.density);
    }

    // ← Un type de vue unique par position = jamais de recyclage croisé
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return mImageUrls.get(position).hashCode();
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pub_slide, parent, false);
        SlideViewHolder holder = new SlideViewHolder(view);

        // ← Charger l'image dès la création du ViewHolder via le viewType = position
        loadImage(holder.imageView, mImageUrls.get(viewType));

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        // ← Vide intentionnellement : le chargement est fait dans onCreateViewHolder
        // grâce au viewType = position, chaque ViewHolder est unique et jamais recyclé
    }

    @Override
    public void onViewRecycled(@NonNull SlideViewHolder holder) {
        super.onViewRecycled(holder);
        // ← Annuler toute requête Picasso en cours sur ce holder
        Picasso.get().cancelRequest(holder.imageView);
    }

    private void loadImage(ImageView imageView, String url) {
        Picasso.get()
                .load(url)
                .resize(6200, 3333)
                .centerInside()
                .placeholder(R.drawable.img_wait_pub)
                .error(R.drawable.img_wait_pub)
                .into(imageView);
    }

    @Override
    public int getItemCount() {
        return mImageUrls.size();
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_slide_pub);
        }
    }
}