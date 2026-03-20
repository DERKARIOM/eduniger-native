package com.ninotech.eduniger.controleur.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ninotech.eduniger.R;
import com.ninotech.eduniger.model.data.Server;
import com.ninotech.eduniger.model.data.StructureStory;

import java.util.List;

public class StructureStoryAdapter extends RecyclerView.Adapter<StructureStoryAdapter.ViewHolder> {

    public interface OnStoryClickListener {
        void onStoryClick(StructureStory story);
    }

    private final List<StructureStory> mStories;
    private final OnStoryClickListener mListener;

    public StructureStoryAdapter(List<StructureStory> stories, OnStoryClickListener listener) {
        mStories  = stories;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_structure_story, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StructureStory story = mStories.get(position);
        Context context      = holder.itemView.getContext();

        // Nom
        holder.mNameTextView.setText(story.getName());

        // Anneau coloré ou gris selon si nouvelle story
        holder.mRingView.setBackgroundResource(
                story.hasNewStory()
                        ? R.drawable.story_ring_new
                        : R.drawable.story_ring_seen
        );

        // Logo de la structure
        String logoUrl = Server.getUrlServer(context) + "ressources/logo/" + story.getLogo();
        Glide.with(context)
                .load(logoUrl)
                .apply(RequestOptions.circleCropTransform()
                        .placeholder(R.drawable.user)
                        .error(R.drawable.user))
                .into(holder.mLogoImageView);

        // Clic
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) mListener.onStoryClick(story);
        });
    }

    @Override
    public int getItemCount() { return mStories.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mLogoImageView;
        TextView  mNameTextView;
        View      mRingView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mLogoImageView = itemView.findViewById(R.id.image_view_story_logo);
            mNameTextView  = itemView.findViewById(R.id.text_view_story_name);
            mRingView      = itemView.findViewById(R.id.view_story_ring);
        }
    }
}