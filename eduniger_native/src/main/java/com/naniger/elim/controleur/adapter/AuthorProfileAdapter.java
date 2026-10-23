package com.naniger.elim.controleur.adapter;

import android.content.Context;
import android.net.Uri;
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
import com.naniger.elim.model.author.Publication;
import com.naniger.elim.model.data.Server;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Liste unique du profil d'auteur : [en-tête du profil] + [publications] + [pied].
 *
 * <p>Une seule RecyclerView pour toute la page : seules les publications visibles sont créées
 * et leurs couvertures chargées, même pour une très longue bibliographie (contrairement à une
 * liste imbriquée dans un NestedScrollView, qui dessine tout d'un coup).
 *
 * <p>L'en-tête est une vue unique fournie par l'écran (elle contient des champs interactifs :
 * recherche, filtres) ; elle n'est jamais recyclée, son état est donc conservé.
 */
public class AuthorProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener {
        void onPublicationClick(Publication publication);

        void onRetry();

        void onResetFilters();
    }

    /** État du pied de liste. */
    public enum Footer { NONE, LOADING_FIRST, LOADING_MORE, ERROR, EMPTY, NO_RESULTS, END }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_BOOK = 1;
    private static final int TYPE_FOOTER = 2;

    private final View mHeader;
    private final Listener mListener;
    private final List<Publication> mItems = new ArrayList<>();
    private Footer mFooter = Footer.LOADING_FIRST;
    private String mEmptyMessage;
    private String mEmptyTitle;
    private String mErrorText;
    private String mEndText;
    private boolean mShowAuthor;

    public AuthorProfileAdapter(View header, Listener listener) {
        mHeader = header;
        mListener = listener;
        setHasStableIds(true);
    }

    // ------------------------------------------------------------------ données

    /** Remplace toute la liste (nouvelle recherche, nouveau tri ou filtre, actualisation). */
    public void replaceAll(List<Publication> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    /** Ajoute la page suivante sans redessiner le reste de la liste. */
    public void append(List<Publication> all) {
        int before = mItems.size();
        if (all.size() <= before) {
            replaceAll(all);
            return;
        }
        mItems.addAll(all.subList(before, all.size()));
        notifyItemRangeInserted(1 + before, all.size() - before);
    }

    public void setFooter(Footer footer, String emptyMessage) {
        if (footer == mFooter && (emptyMessage == null || emptyMessage.equals(mEmptyMessage))) return;
        mFooter = footer;
        mEmptyMessage = emptyMessage;
        notifyItemChanged(getItemCount() - 1);
    }

    /**
     * Réutilisation pour les ressources d'une structure : nom de l'auteur affiché sur chaque
     * ligne et titre d'état vide adapté.
     */
    public void configureForStructure(String emptyTitle, String errorText, String endText) {
        mShowAuthor = true;
        mEmptyTitle = emptyTitle;
        mErrorText = errorText;
        mEndText = endText;
    }

    public int publicationCount() {
        return mItems.size();
    }

    @Override
    public int getItemCount() {
        return 1 + mItems.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        if (position == getItemCount() - 1) return TYPE_FOOTER;
        return TYPE_BOOK;
    }

    @Override
    public long getItemId(int position) {
        int type = getItemViewType(position);
        if (type == TYPE_HEADER) return Long.MIN_VALUE;
        if (type == TYPE_FOOTER) return Long.MIN_VALUE + 1;
        Publication p = mItems.get(position - 1);
        return p.id == null ? position : p.id.hashCode() & 0xffffffffL;
    }

    // ------------------------------------------------------------------ vues

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
        if (viewType == TYPE_FOOTER) {
            return new FooterHolder(inflater.inflate(R.layout.item_author_profile_footer, parent, false));
        }
        return new BookHolder(inflater.inflate(R.layout.item_author_publication, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BookHolder) ((BookHolder) holder).bind(mItems.get(position - 1));
        else if (holder instanceof FooterHolder) ((FooterHolder) holder).bind();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof BookHolder) Picasso.get().cancelRequest(((BookHolder) holder).cover);
    }

    final class BookHolder extends RecyclerView.ViewHolder {
        final ImageView cover;
        final TextView title, category, description, pdf, audio, physical, extra;

        BookHolder(View v) {
            super(v);
            cover = v.findViewById(R.id.image_view_author_publication_cover);
            title = v.findViewById(R.id.text_view_author_publication_title);
            category = v.findViewById(R.id.text_view_author_publication_category);
            description = v.findViewById(R.id.text_view_author_publication_description);
            pdf = v.findViewById(R.id.text_view_author_publication_pdf);
            audio = v.findViewById(R.id.text_view_author_publication_audio);
            physical = v.findViewById(R.id.text_view_author_publication_physical);
            extra = v.findViewById(R.id.text_view_author_publication_extra);
        }

        void bind(Publication p) {
            Context c = itemView.getContext();
            String t = p.title != null ? p.title : "";
            title.setText(t);

            String cats = ProfileText.join(p.categories, " · ");
            if (mShowAuthor && p.authorName != null) {
                cats = cats.isEmpty() ? p.authorName : p.authorName + " · " + cats;
            }
            category.setText(cats);
            category.setVisibility(cats.isEmpty() ? View.GONE : View.VISIBLE);

            description.setText(p.description != null ? p.description.replace('\n', ' ') : "");
            description.setVisibility(p.description == null ? View.GONE : View.VISIBLE);

            pdf.setVisibility(p.hasPdf ? View.VISIBLE : View.GONE);
            audio.setVisibility(p.isAudio ? View.VISIBLE : View.GONE);
            physical.setVisibility(p.isPhysical ? View.VISIBLE : View.GONE);

            // Informations complémentaires réelles : nombre de pages, sinon année d'ajout dans Elim.
            String pages = ProfileText.clean(p.pages);
            String extraText;
            if (pages != null && !"0".equals(pages)) extraText = c.getString(R.string.author_pub_pages, pages);
            else {
                String year = ProfileText.year(p.addedAt);
                extraText = year != null ? c.getString(R.string.author_pub_added, year) : "";
            }
            extra.setText(extraText);
            extra.setVisibility(extraText.isEmpty() ? View.GONE : View.VISIBLE);

            if (p.structureId != null && p.cover != null) {
                Picasso.get()
                        .load(Server.getUrlHostProd(c) + "/api/public/resource/" + Uri.encode(p.structureId)
                                + "/blanket/" + Uri.encode(p.cover))
                        .placeholder(R.drawable.img_wait_cover_book)
                        .error(R.drawable.img_wait_cover_book)
                        .fit()
                        .centerCrop()
                        .into(cover);
            } else {
                Picasso.get().cancelRequest(cover);
                cover.setImageResource(R.drawable.img_wait_cover_book);
            }

            // Une seule annonce claire pour le lecteur d'écran.
            List<String> a11y = new ArrayList<>();
            a11y.add(t);
            if (!cats.isEmpty()) a11y.add(cats);
            if (p.hasPdf) a11y.add(c.getString(R.string.author_stat_pdf));
            if (p.isAudio) a11y.add(c.getString(R.string.book_card_a11y_audio));
            if (p.isPhysical) a11y.add(c.getString(R.string.author_stat_physical));
            View row = itemView.findViewById(R.id.linear_layout_author_publication);
            row.setContentDescription(ProfileText.join(a11y, ", "));
            row.setOnClickListener(v -> mListener.onPublicationClick(p));
        }
    }

    final class FooterHolder extends RecyclerView.ViewHolder {
        final View skeleton, progress, error, empty, reset;
        final TextView end, emptyTitle, emptyMessage, errorText;

        FooterHolder(View v) {
            super(v);
            skeleton = v.findViewById(R.id.linear_layout_author_footer_skeleton);
            progress = v.findViewById(R.id.progress_bar_author_footer);
            error = v.findViewById(R.id.linear_layout_author_footer_error);
            empty = v.findViewById(R.id.linear_layout_author_footer_empty);
            reset = v.findViewById(R.id.text_view_author_footer_reset);
            end = v.findViewById(R.id.text_view_author_footer_end);
            emptyTitle = v.findViewById(R.id.text_view_author_footer_empty_title);
            emptyMessage = v.findViewById(R.id.text_view_author_footer_empty_message);
            errorText = v.findViewById(R.id.text_view_author_footer_error);
            if (mErrorText != null) errorText.setText(mErrorText);
            if (mEndText != null) end.setText(mEndText);
            v.findViewById(R.id.text_view_author_footer_retry).setOnClickListener(x -> mListener.onRetry());
            reset.setOnClickListener(x -> mListener.onResetFilters());
        }

        void bind() {
            Context c = itemView.getContext();
            skeleton.setVisibility(mFooter == Footer.LOADING_FIRST ? View.VISIBLE : View.GONE);
            progress.setVisibility(mFooter == Footer.LOADING_MORE ? View.VISIBLE : View.GONE);
            error.setVisibility(mFooter == Footer.ERROR ? View.VISIBLE : View.GONE);
            end.setVisibility(mFooter == Footer.END ? View.VISIBLE : View.GONE);
            boolean isEmpty = mFooter == Footer.EMPTY || mFooter == Footer.NO_RESULTS;
            empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            if (mFooter == Footer.EMPTY) {
                if (mEmptyTitle != null) emptyTitle.setText(mEmptyTitle);
                else emptyTitle.setText(R.string.author_no_books_title);
                emptyMessage.setText(mEmptyMessage != null ? mEmptyMessage : c.getString(R.string.author_no_books_message));
                reset.setVisibility(View.GONE);
            } else if (mFooter == Footer.NO_RESULTS) {
                emptyTitle.setText(R.string.author_bib_no_results_title);
                emptyMessage.setText(R.string.author_bib_no_results_message);
                reset.setVisibility(View.VISIBLE);
            }
            itemView.setVisibility(mFooter == Footer.NONE ? View.INVISIBLE : View.VISIBLE);
        }
    }
}
