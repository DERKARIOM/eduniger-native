package com.ninotech.fabi.controleur.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.controleur.activity.BookActivity;
import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.RecentBook;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class SimulaireAdapter extends RecyclerView.Adapter<SimulaireAdapter.MyViewHolder> {
    List<RecentBook> mListRecentBook;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public SimulaireAdapter(List<RecentBook> listRecentBook) {
        mListRecentBook = listRecentBook;
    }
    @Override
    public SimulaireAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_similaire,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        RecentBook item = mListRecentBook.get(position);
        try {
            holder.display(mListRecentBook.get(position));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    public int getItemCount() {
        return mListRecentBook.size();
    }


//    public void Remove(int position){
//        mListNotification.remove(position);
//        notifyItemRemoved(position);
//    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView mCouverture;
        MyViewHolder(View itemView){
            super(itemView);
            mCouverture = (ImageView) itemView.findViewById(R.id.couverteur_recent);
        }
        //        @Override
//        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
////            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
////            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
////            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        void display(RecentBook recentBook) throws SQLException, IOException {
            Picasso.with(itemView.getContext())
                    .load("http://192.168.43.1:2222/fabi/couverture/" + recentBook.getCouverteur())
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .transform(new RoundedTransformation(15,4))
                    .resize(178,284)
                    .into(mCouverture);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intentLivre = new Intent(itemView.getContext(), BookActivity.class);
                    intentLivre.putExtra("idLivre", recentBook.getIdLivre());
                    itemView.getContext().startActivity(intentLivre);
                }
            });
        }

    }
}
