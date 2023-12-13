package com.fabi.Model;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fabi.R;

import java.util.List;

public class ArchiverCusto extends RecyclerView.Adapter<ArchiverCusto.MyViewHolder> {
    List<Archiver> mArchivers;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public ArchiverCusto(List<Archiver> archivers) {
        mArchivers = archivers;
    }
    @Override
    public ArchiverCusto.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.block_archiver,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Archiver item = mArchivers.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mArchivers.get(position));

    }
    @Override
    public int getItemCount() {
        return mArchivers.size();
    }

    public Archiver getItem(int position) {
        return mArchivers.get(position);
    }

    public void Remove(int position){
        mArchivers.remove(position);
        notifyItemRemoved(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private TextView semestre;
        private TextView mMoy;
        private ProgressBar mProgressBar;
        private AnimationProgresseBar mAnimationProgresseBar;

        private
        MyViewHolder(View itemView){
            super(itemView);
            semestre = itemView.findViewById(R.id.semestreAr);
 //           mUEStatisque = (TextView) itemView.findViewById(R.id.UEStatisque);
            mMoy = (TextView) itemView.findViewById(R.id.moy);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progressMoySect);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
        }
        void display(Archiver archiver){
            semestre.setText("Semestre " + archiver.getSemestre());

//            mUEStatisque.setText(statistique.getUeStatistique());
            mAnimationProgresseBar = new AnimationProgresseBar(mProgressBar,mMoy,50);
        }

    }
}
