package com.ninotech.fabi.controleur.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.AuthorLocal;
import com.ninotech.fabi.model.data.Category;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;


public class AuthorLocalAdapter extends RecyclerView.Adapter<AuthorLocalAdapter.MyViewHolder> {
    List<AuthorLocal> mAuthorLocals;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public AuthorLocalAdapter(List<AuthorLocal> authorLocals) {
        mAuthorLocals = authorLocals;
    }
    @Override
    public AuthorLocalAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_category,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AuthorLocal item = mAuthorLocals.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mAuthorLocals.get(position));

    }
    @Override
    public int getItemCount() {
        return mAuthorLocals.size();
    }

    public AuthorLocal getItem(int position) {
        return mAuthorLocals.get(position);
    }

    public void Remove(int position){
        mAuthorLocals.remove(position);
        notifyItemRemoved(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private ImageView mProfileImageView;
        private TextView mUsernameTextView;
        MyViewHolder(View itemView){
            super(itemView);
            mProfileImageView = itemView.findViewById(R.id.image_view_adapter_category_blanket);
            mUsernameTextView = itemView.findViewById(R.id.text_view_adapter_category_title);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }
        void display(AuthorLocal authorLocal){
            File file = bitmapToFile(itemView.getContext(), authorLocal.getName() + ".png", authorLocal.getProfile());
            Picasso.get().load(file)
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .transform(new RoundedTransformation(10000,4))
                    .resize(200,200)
                    .into(mProfileImageView);
            mUsernameTextView.setText(authorLocal.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(itemView.getContext(), "OK", Toast.LENGTH_SHORT).show();
//                    Intent category = new Intent(itemView.getContext(), CategoryActivity.class);
//                    category.putExtra("nomCat",mTitre.getText().toString());
//                    itemView.getContext().startActivity(category);
                }
            });
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