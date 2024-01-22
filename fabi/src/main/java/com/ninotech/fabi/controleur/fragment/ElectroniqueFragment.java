package com.ninotech.fabi.controleur.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.controleur.adapter.ElectroniqueAdapter;
import com.ninotech.fabi.model.data.Electronique;
import com.ninotech.fabi.model.table.ElectroniqueTable;
import com.ninotech.fabi.model.data.Recenmment;
import com.ninotech.fabi.controleur.adapter.RecenmmentAdapter;
import com.ninotech.fabi.R;
import com.ninotech.fabi.model.table.Session;

import java.util.ArrayList;
import java.util.List;

public class ElectroniqueFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_electronique, container, false);
        mRecyclerView = view.findViewById(R.id.recyler_media);
        mRecyclerView1 = view.findViewById(R.id.recyler_recent);
        mList = new ArrayList<>();
        mList1 = new ArrayList<>();
        mElectroniqueTable = new ElectroniqueTable(getContext());
        mSession = new Session(getContext());
        mElectroniqueAdapter = new ElectroniqueAdapter(mList);
        mRecenmmentAdapter = new RecenmmentAdapter(mList1);
        mList.add(new Electronique(1,"Les Livres télécharges",mElectroniqueTable.getNbrElectronique(mSession.getIdNumber())));
        mList.add(new Electronique(2,"Coups de coeur",0));
        mList.add(new Electronique(3,"playlists",0));
        mList.add(new Electronique(4,"Categorie",mElectroniqueTable.getNbrCategorie(mSession.getIdNumber())));
        mList.add(new Electronique(5,"Auteurs",mElectroniqueTable.getNbrAuteur(mSession.getIdNumber())));
        Cursor cursor = mElectroniqueTable.getData(mSession.getIdNumber());
        cursor.moveToFirst();
        do {
            mList1.add(new Recenmment(cursor.getString(2),cursor.getString(5),cursor.getString(6)));
        }while(cursor.moveToNext());
//
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));
        mRecyclerView1.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        mRecyclerView.setAdapter(mElectroniqueAdapter);
        mRecyclerView1.setAdapter(mRecenmmentAdapter);
        return view;
    }
    private RecyclerView mRecyclerView;
    private RecyclerView mRecyclerView1;
    private ElectroniqueAdapter mElectroniqueAdapter;
    private RecenmmentAdapter mRecenmmentAdapter;
    private List<Electronique> mList;
    private List<Recenmment> mList1;
    private ElectroniqueTable mElectroniqueTable;
    private Session mSession;

}