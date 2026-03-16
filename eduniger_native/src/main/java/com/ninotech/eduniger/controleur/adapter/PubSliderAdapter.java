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
import com.ninotech.eduniger.controleur.animation.RoundedTransformation;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PubSliderAdapter extends RecyclerView.Adapter<PubSliderAdapter.SlideViewHolder> {

    private final List<String> mImageUrls;
    private final int mTargetWidth;
    private final int mTargetHeight;

    public PubSliderAdapter(Context context, List<String> imageUrls) {
        this.mImageUrls = imageUrls;

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int marginPx   = (int) (15 * metrics.density * 2); // paddingStart + paddingEnd
        mTargetWidth   = metrics.widthPixels - marginPx;
        mTargetHeight  = (int) (160 * metrics.density);    // correspond au 160dp du XML
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pub_slide, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        Picasso.get()
                .load(mImageUrls.get(position))
                .transform(new RoundedTransformation(16, 0))
                .resize(mTargetWidth, mTargetHeight)
                .centerCrop()
                .placeholder(R.drawable.img_wait_pub)
                .error(R.drawable.img_wait_pub)
                .into(holder.imageView);
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