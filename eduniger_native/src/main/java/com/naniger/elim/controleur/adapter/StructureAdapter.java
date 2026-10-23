package com.naniger.elim.controleur.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.naniger.elim.R;
import com.naniger.elim.controleur.activity.AddBookActivity;
import com.naniger.elim.controleur.activity.RegisterAuthorActivity;
import com.naniger.elim.controleur.activity.StructureActivity;
import com.naniger.elim.controleur.dialog.SimpleOkDialog;
import com.naniger.elim.model.data.Server;
import com.naniger.elim.model.data.Structure;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import com.naniger.elim.model.net.ApiClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;


public class StructureAdapter extends RecyclerView.Adapter<StructureAdapter.MyViewHolder> {
    private static final java.util.concurrent.ExecutorService MEMBERSHIP =
            java.util.concurrent.Executors.newSingleThreadExecutor();
    List<Structure> mStructures;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public StructureAdapter(List<Structure> structures) {
        mStructures = structures;
    }
    @Override
    public StructureAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_structure,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Structure item = mStructures.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mStructures.get(position));

    }
    @Override
    public int getItemCount() {
        return mStructures.size();
    }

    public Structure getItem(int position) {
        return mStructures.get(position);
    }

    public void Remove(int position){
        mStructures.remove(position);
        notifyItemRemoved(position);
    }

    public void filterList(ArrayList<Structure> filteredList) {
        mStructures = filteredList;
        notifyDataSetChanged();
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private final ImageView mBlanketImageView;
        private final TextView mTitleTextView;
        private final TextView mBookNumberTextView;
        private final Button mAdhereButton;
        MyViewHolder(View itemView){
            super(itemView);
            mBlanketImageView = itemView.findViewById(R.id.image_view_adapter_structure_cover);
            mTitleTextView = itemView.findViewById(R.id.text_view_adapter_structure_title);
            mBookNumberTextView = itemView.findViewById(R.id.image_view_adapter_structure_book_number);
            mAdhereButton = itemView.findViewById(R.id.button_adapter_structure);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
        }
        private Structure mStructureRef;

        void display(Structure structure){
            mStructureRef = structure;
            applyLogoShape(isPseudoItem(structure));
            switch (structure.getId())
            {
                case "AddBook":
                    mBookNumberTextView.setText(structure.getBookNumber());
                    Animation pulseAnimImg = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.float_bob);
                    // Lancer l'animation automatiquement
                    mBlanketImageView.startAnimation(pulseAnimImg);
                    mAdhereButton.setText("Ajouter");
                    // Icône « document + » (publier un contenu) au lieu de la photo.
                    // Ligne recyclée : on annule un éventuel chargement de logo en cours.
                    Picasso.get().cancelRequest(mBlanketImageView);
                    mBlanketImageView.setImageResource(R.drawable.bg_add_content_avatar);
                    break;
                case "RegisterAuthor":
                    mBookNumberTextView.setText(structure.getBookNumber());
                    Animation pulseAnimImg2 = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.float_bob);
                    // Lancer l'animation automatiquement
                    mBlanketImageView.startAnimation(pulseAnimImg2);
                    mAdhereButton.setText("S'inscrire");
                    mBlanketImageView.setImageResource(R.drawable.add_auteurs);
                    break;
                default:
                    // Ligne recyclée : pas d'animation héritée de la carte « Ajouter ».
                    mBlanketImageView.clearAnimation();
                    mBookNumberTextView.setText(structure.getBookNumber() + " Livres");
                    // Logo réel seulement : la valeur par défaut « eduniger.svg » (illisible par
                    // Picasso) ne déclenche plus une requête vouée à l'échec.
                    String logoUrl = com.naniger.elim.model.structure.StructureText.logoUrl(
                            Server.getUrlHostProd(itemView.getContext()), structure.getId(), structure.getCover());
                    if (logoUrl == null) {
                        Picasso.get().cancelRequest(mBlanketImageView);
                        mBlanketImageView.setImageResource(R.drawable.img_wait_struct);
                    } else {
                        Picasso.get()
                                .load(logoUrl)
                                .placeholder(R.drawable.img_wait_struct)
                                .error(R.drawable.img_wait_struct)
                                .fit()
                                .centerCrop()
                                .into(mBlanketImageView);
                    }
                    break;
            }
            // État du bouton toujours recalculé : une ligne recyclée ne garde pas le libellé
            // « Détacher » d'une autre structure (bug corrigé).
            if (!isPseudoItem(structure)) {
                applyAdhereState(Boolean.TRUE.equals(structure.isAdhere()));
            } else {
                mAdhereButton.setEnabled(true);
                mAdhereButton.setAlpha(1f);
                mAdhereButton.setBackgroundTintList(ColorStateList.valueOf(
                        androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.elim_primary)));
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (structure.getId())
                    {
                        case "AddBook":
                            simpleOkDialog(R.drawable.add_auteurs,"Ajouter un contenu dans Elim" , "Partagez vos savoirs avec la communauté ! En devenant auteur, vous pouvez publier vos livres et documents. Chaque ajout est vérifié avant d’être validé et mis en ligne, puis vous recevez une notification de confirmation.");
                            break;
                        case "RegisterAuthor":
                            simpleOkDialog(R.drawable.add_auteurs,"Devenir Auteur Sur Elim" , "Vous avez un livre, un manuscrit ou une idée à partager ?\n\n" +
                                    "Cette section est faite pour vous ! Elle vous permet d’ajouter vos ouvrages à notre bibliothèque, de les rendre accessibles aux lecteurs et de partager vos connaissances, vos histoires ou vos créations avec la communauté.\n\n" +
                                    "En rejoignant les auteurs, vous contribuez à enrichir la plateforme et à inspirer des milliers de personnes.");
                            break;
                        default:
                            Intent structureIntent = new Intent(itemView.getContext(), StructureActivity.class);
                            structureIntent.putExtra("intent_structure_adapter_id", structure.getId());
                            structureIntent.putExtra("intent_structure_adapter_logo", structure.getCover());
                            structureIntent.putExtra("intent_structure_adapter_name", structure.getName());
                            structureIntent.putExtra("intent_structure_adapter_description", structure.getDescription());
                            structureIntent.putExtra("intent_structure_adapter_is_adhere", structure.isAdhere());
                            structureIntent.putExtra("intent_structure_adapter_banner", structure.getBanner());
                            structureIntent.putExtra("intent_structure_adapter_author", structure.getAuthor());
                            structureIntent.putExtra("intent_structure_adapter_adherer_number", structure.getAdhererNumber());
                            structureIntent.putExtra("intent_structure_adapter_book_number", structure.getBookNumber());
                            structureIntent.putExtra("intent_structure_adapter_admin", structure.getAdmin());
                            itemView.getContext().startActivity(structureIntent);
                            break;
                    }
                }
            });
            mTitleTextView.setText(structure.getName());
            mAdhereButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (structure.getId())
                    {
                        default:
                            if (mBusy) break;
                            if (Boolean.TRUE.equals(structure.isAdhere()))
                                structDelete(structure);
                            else
                                changeMembership(structure, true);
                            break;
                        case "AddBook","RegisterAuthor":
                            Intent registerAuthorIntent = new Intent(itemView.getContext(), AddBookActivity.class);
                            itemView.getContext().startActivity(registerAuthorIntent);
                            //simpleOkDialog(R.drawable.add_auteurs,"Ajouter un contenue dans Elim" , "La fonctionnalité Devenir Auteur est actuellement en cours de développement. Elle sera disponible dans la version officielle à venir, inchaAllah.");
                            break;
                    }

                }
            });
        }
        private boolean mBusy;

        /**
         * Forme du visuel : logo de structure en carré légèrement arrondi, pastille
         * « Ajouter un contenu » / « Devenir auteur » ronde (comme les portraits d'auteurs).
         * Réappliquée à chaque liaison : une ligne recyclée ne garde pas la forme précédente.
         */
        private void applyLogoShape(boolean round) {
            if (!(mBlanketImageView instanceof com.google.android.material.imageview.ShapeableImageView)) return;
            com.google.android.material.imageview.ShapeableImageView v =
                    (com.google.android.material.imageview.ShapeableImageView) mBlanketImageView;
            com.google.android.material.shape.CornerSize corner = round
                    ? new com.google.android.material.shape.RelativeCornerSize(0.5f)
                    : new com.google.android.material.shape.AbsoluteCornerSize(
                            18 * v.getResources().getDisplayMetrics().density);
            v.setShapeAppearanceModel(v.getShapeAppearanceModel().toBuilder().setAllCornerSizes(corner).build());
        }

        private boolean isPseudoItem(Structure structure) {
            return "AddBook".equals(structure.getId()) || "RegisterAuthor".equals(structure.getId());
        }

        /** Bouton selon l'état réel (couleurs du thème Elim, plus de couleurs codées en dur). */
        private void applyAdhereState(boolean adhered) {
            android.content.Context c = itemView.getContext();
            mAdhereButton.setText(adhered ? "Détacher" : "S'adhérer");
            mAdhereButton.setBackgroundTintList(ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(c,
                    adhered ? R.color.elim_on_surface_variant : R.color.elim_primary)));
            mAdhereButton.setEnabled(!mBusy);
            mAdhereButton.setAlpha(mBusy ? 0.6f : 1f);
        }

        /**
         * Confirmation simple avant de quitter. L'ancienne version exigeait le mot de passe,
         * comparé à une empreinte locale : aucun contrôle réel (l'API autorise via le jeton) et
         * impossible pour les comptes Google, qui n'ont pas de mot de passe local.
         */
        private void structDelete(Structure structure){
            android.content.Context c = itemView.getContext();
            new androidx.appcompat.app.AlertDialog.Builder(c)
                    .setTitle(c.getString(R.string.structure_leave_dialog_title, structure.getName()))
                    .setMessage(R.string.structure_leave_dialog_message)
                    .setNegativeButton(R.string.struct_leave_cancel, null)
                    .setPositiveButton(R.string.structure_leave_dialog_confirm, (d, w) -> changeMembership(structure, false))
                    .show();
        }

        /**
         * Adhésion / retrait via /api/structures/{id}/join|leave (idUser pris du jeton côté
         * serveur). L'état n'est changé qu'après la réponse : en cas d'échec, le bouton reste
         * juste et un message l'explique (avant : changement affiché même en cas d'échec, et
         * Toast lancé hors du fil principal en cas d'erreur réseau → plantage).
         */
        private void changeMembership(Structure structure, boolean join) {
            mBusy = true;
            applyAdhereState(Boolean.TRUE.equals(structure.isAdhere()));
            android.content.Context app = itemView.getContext().getApplicationContext();
            String url = Server.getUrlHostProd(app) + "/api/structures/" + structure.getId() + (join ? "/join" : "/leave");
            android.os.Handler main = new android.os.Handler(android.os.Looper.getMainLooper());
            MEMBERSHIP.execute(() -> {
                int code;
                String message = null;
                try {
                    Request request = new Request.Builder().url(url)
                            .post(RequestBody.create(new byte[0], null))
                            .header("Accept", "application/json").build();
                    try (Response response = ApiClient.getInstance(app).newCall(request).execute()) {
                        code = response.code();
                        String body = response.body() != null ? response.body().string() : null;
                        if (body != null) {
                            try {
                                message = new JSONObject(body).optString("message", null);
                            } catch (JSONException ignored) {
                                // réponse non JSON
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.w("StructureAdapter", "Adhésion : échec réseau", e);
                    code = -1;
                }
                final int status = code;
                final String serverMessage = message;
                main.post(() -> {
                    mBusy = false;
                    boolean nowMember;
                    if (join && (status == 200 || status == 201 || status == 409)) nowMember = true;
                    else if (!join && (status == 200 || (status == 404 && serverMessage != null && serverMessage.contains("membre")))) nowMember = false;
                    else {
                        nowMember = Boolean.TRUE.equals(structure.isAdhere());
                        Toast.makeText(app, status == 401 ? R.string.structure_membership_session
                                : R.string.structure_membership_error, Toast.LENGTH_LONG).show();
                    }
                    boolean changed = nowMember != Boolean.TRUE.equals(structure.isAdhere());
                    structure.setAdhere(nowMember);
                    if (changed) {
                        Toast.makeText(app, nowMember
                                ? app.getString(R.string.structure_join_success, structure.getName())
                                : app.getString(R.string.structure_leave_success, structure.getName()), Toast.LENGTH_SHORT).show();
                        // Liste et fiche à relire avec le nouvel état.
                        String host = Server.getUrlHostProd(app);
                        com.naniger.elim.model.cache.ApiCache.invalidate(app, host + "/api/structures");
                        com.naniger.elim.model.cache.ApiCache.invalidate(app, host + "/api/structures/" + structure.getId());
                    }
                    // La ligne a pu être recyclée pour une autre structure entre-temps.
                    if (getAdapterPosition() != RecyclerView.NO_POSITION && mStructureRef == structure) {
                        applyAdhereState(nowMember);
                    }
                });
            });
        }

        private void simpleOkDialog(int ico , String title , String message){
            SimpleOkDialog simpleOkDialog = new SimpleOkDialog((Activity) itemView.getContext());
            simpleOkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            simpleOkDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            ImageView icoImageView = simpleOkDialog.findViewById(R.id.image_view_dialog_simple_ok_icon);
            TextView titleTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok_title);
            TextView messageTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok_message);
            TextView okTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok);
            icoImageView.setImageResource(ico);
            titleTextView.setText(title);
            messageTextView.setText(message);
            okTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    simpleOkDialog.cancel();
                }
            });
            simpleOkDialog.build();
        }
    }
}