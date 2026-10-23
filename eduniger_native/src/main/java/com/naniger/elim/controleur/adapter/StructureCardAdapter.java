package com.naniger.elim.controleur.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.naniger.elim.R;
import com.naniger.elim.model.author.ProfileText;
import com.naniger.elim.model.data.Server;
import com.naniger.elim.model.structure.StructureInfo;
import com.naniger.elim.model.structure.StructureText;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Liste de l'onglet Structures : [en-tête] + [cartes] + [état].
 * L'en-tête (recherche, filtres) est une vue unique jamais recyclée : le champ de recherche
 * garde le focus et le clavier pendant que les cartes sont filtrées.
 */
public class StructureCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener {
        void onStructureClick(StructureInfo structure);

        void onStateAction(State state);
    }

    /** État affiché sous les cartes. */
    public enum State { NONE, LOADING, ERROR, EMPTY, NO_RESULTS }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CARD = 1;
    private static final int TYPE_STATE = 2;

    private final View mHeader;
    private final Listener mListener;
    private final List<StructureInfo> mItems = new ArrayList<>();
    private State mState = State.LOADING;

    public StructureCardAdapter(View header, Listener listener) {
        mHeader = header;
        mListener = listener;
        setHasStableIds(true);
    }

    public void submit(List<StructureInfo> items, State state) {
        mItems.clear();
        mItems.addAll(items);
        mState = state;
        // Identifiants stables : l'en-tête (jamais recyclé, rien à lier) reste en place et le
        // champ de recherche garde le focus et le clavier.
        notifyDataSetChanged();
    }

    public int size() {
        return mItems.size();
    }

    @Override
    public int getItemCount() {
        return 1 + mItems.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        if (position == getItemCount() - 1) return TYPE_STATE;
        return TYPE_CARD;
    }

    @Override
    public long getItemId(int position) {
        int type = getItemViewType(position);
        if (type == TYPE_HEADER) return Long.MIN_VALUE;
        if (type == TYPE_STATE) return Long.MIN_VALUE + 1;
        String id = mItems.get(position - 1).id;
        try {
            return Long.parseLong(id);
        } catch (Exception e) {
            return position;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            ViewParent p = mHeader.getParent();
            if (p instanceof ViewGroup) ((ViewGroup) p).removeView(mHeader);
            if (mHeader.getLayoutParams() == null) {
                mHeader.setLayoutParams(new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            RecyclerView.ViewHolder h = new RecyclerView.ViewHolder(mHeader) { };
            h.setIsRecyclable(false);
            return h;
        }
        if (viewType == TYPE_STATE) {
            return new StateHolder(inflater.inflate(R.layout.item_structures_state, parent, false));
        }
        return new CardHolder(inflater.inflate(R.layout.item_structure_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CardHolder) ((CardHolder) holder).bind(mItems.get(position - 1));
        else if (holder instanceof StateHolder) ((StateHolder) holder).bind();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof CardHolder) Picasso.get().cancelRequest(((CardHolder) holder).logo);
    }

    final class CardHolder extends RecyclerView.ViewHolder {
        final View card;
        final ImageView logo;
        final TextView initials, name, badge, location, description, meta;

        CardHolder(View v) {
            super(v);
            card = v.findViewById(R.id.linear_layout_structure_card);
            logo = v.findViewById(R.id.image_view_structure_card_logo);
            initials = v.findViewById(R.id.text_view_structure_card_initials);
            name = v.findViewById(R.id.text_view_structure_card_name);
            badge = v.findViewById(R.id.text_view_structure_card_badge);
            location = v.findViewById(R.id.text_view_structure_card_location);
            description = v.findViewById(R.id.text_view_structure_card_description);
            meta = v.findViewById(R.id.text_view_structure_card_meta);
        }

        void bind(StructureInfo s) {
            Context c = itemView.getContext();
            name.setText(s.name);
            initials.setText(StructureText.initials(s.name));

            // Logo réel seulement ; sinon (absent, valeur par défaut, SVG, erreur) les initiales.
            String url = StructureText.logoUrl(Server.getUrlHostProd(c), s.id, s.logo);
            Picasso.get().cancelRequest(logo);
            logo.setVisibility(View.GONE);
            if (url != null) {
                Picasso.get().load(url).fit().centerCrop().into(logo, new Callback() {
                    @Override
                    public void onSuccess() {
                        logo.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Exception e) {
                        logo.setVisibility(View.GONE);
                    }
                });
            }

            badge.setVisibility(s.isMember ? View.VISIBLE : View.GONE);

            location.setText(s.location != null ? s.location : "");
            location.setVisibility(s.location != null ? View.VISIBLE : View.GONE);

            String summary = StructureText.summary(s.description);
            description.setText(summary != null ? summary : "");
            description.setVisibility(summary != null ? View.VISIBLE : View.GONE);

            List<String> parts = new ArrayList<>();
            parts.add(s.resources > 0
                    ? c.getResources().getQuantityString(R.plurals.structure_resources, s.resources, s.resources)
                    : c.getString(R.string.structure_no_resource));
            if (s.members > 0) {
                parts.add(c.getResources().getQuantityString(R.plurals.structure_members, s.members, s.members));
            }
            String metaText = ProfileText.join(parts, " · ");
            meta.setText(metaText);

            // Une seule annonce TalkBack par carte.
            List<String> a11y = new ArrayList<>();
            a11y.add(s.name);
            if (s.isMember) a11y.add(c.getString(R.string.structure_badge_member));
            if (s.location != null) a11y.add(s.location);
            a11y.add(metaText);
            card.setContentDescription(ProfileText.join(a11y, ", "));
            card.setOnClickListener(v -> mListener.onStructureClick(s));
        }
    }

    final class StateHolder extends RecyclerView.ViewHolder {
        final View skeleton, message;
        final ImageView icon;
        final TextView title, text, action;

        StateHolder(View v) {
            super(v);
            skeleton = v.findViewById(R.id.linear_layout_structures_skeleton);
            message = v.findViewById(R.id.linear_layout_structures_message);
            icon = v.findViewById(R.id.image_view_structures_state_icon);
            title = v.findViewById(R.id.text_view_structures_state_title);
            text = v.findViewById(R.id.text_view_structures_state_message);
            action = v.findViewById(R.id.text_view_structures_state_action);
            action.setOnClickListener(x -> mListener.onStateAction(mState));
        }

        void bind() {
            skeleton.setVisibility(mState == State.LOADING ? View.VISIBLE : View.GONE);
            boolean hasMessage = mState == State.ERROR || mState == State.EMPTY || mState == State.NO_RESULTS;
            message.setVisibility(hasMessage ? View.VISIBLE : View.GONE);
            switch (mState) {
                case ERROR:
                    icon.setImageResource(R.drawable.ic_structure_cloud_off);
                    title.setText(R.string.structures_error_title);
                    text.setText(R.string.structures_error_message);
                    action.setText(R.string.author_retry);
                    action.setVisibility(View.VISIBLE);
                    break;
                case NO_RESULTS:
                    icon.setImageResource(R.drawable.ic_profile_search);
                    title.setText(R.string.structures_no_results_title);
                    text.setText(R.string.structures_no_results_message);
                    action.setText(R.string.author_bib_reset_filters);
                    action.setVisibility(View.VISIBLE);
                    break;
                case EMPTY:
                    icon.setImageResource(R.drawable.ic_structure_building);
                    title.setText(R.string.structures_empty_title);
                    text.setText(R.string.structures_empty_message);
                    action.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
            itemView.setVisibility(mState == State.NONE ? View.GONE : View.VISIBLE);
            ViewGroup.LayoutParams lp = itemView.getLayoutParams();
            if (lp != null) {
                lp.height = mState == State.NONE ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
                itemView.setLayoutParams(lp);
            }
        }
    }
}
