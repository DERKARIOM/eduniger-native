package com.ninotech.fabi.controleur.adapter;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.model.data.Category;
import com.squareup.picasso.Picasso;

import java.util.List;


public class CategorieLocalAdapter extends RecyclerView.Adapter<CategorieLocalAdapter.MyViewHolder> {
    List<Category> mListCategory;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public CategorieLocalAdapter(List<Category> listCategory) {
        mListCategory = listCategory;
    }
    @Override
    public CategorieLocalAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_category,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Category item = mListCategory.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mListCategory.get(position));

    }
    @Override
    public int getItemCount() {
        return mListCategory.size();
    }

    public Category getItem(int position) {
        return mListCategory.get(position);
    }

    public void Remove(int position){
        mListCategory.remove(position);
        notifyItemRemoved(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private ImageView mIco;
        private TextView mTitre;
        MyViewHolder(View itemView){
            super(itemView);
            mIco = itemView.findViewById(R.id.image_view_adapter_category_blanket);
            mTitre = itemView.findViewById(R.id.text_view_adapter_category_title);
            //mButton = (Button) itemView.findViewById(R.id.bttAnex);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }
        void display(Category category){
            Picasso.with(itemView.getContext())
                    .load("http://192.168.43.1:2222/fabi/couverture/" + category.getBlanket())
                    .placeholder(R.drawable.img_default_livre)
                    .error(R.drawable.img_default_livre)
                    .into(mIco);
            mTitre.setText(category.getTitle());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(itemView.getContext(), "OK", Toast.LENGTH_SHORT).show();
//                    Intent category = new Intent(itemView.getContext(), CategorieActivity.class);
//                    category.putExtra("nomCat",mTitre.getText().toString());
//                    itemView.getContext().startActivity(category);
                }
            });
        }
    }
}