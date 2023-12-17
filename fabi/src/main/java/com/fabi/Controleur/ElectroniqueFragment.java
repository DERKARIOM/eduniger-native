package com.fabi.Controleur;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fabi.Model.Electronique;
import com.fabi.Model.ElectroniqueAdapter;
import com.fabi.Model.ElectroniqueTable;
import com.fabi.Model.Recenmment;
import com.fabi.Model.RecenmmentAdapter;
import com.example.fabi.R;
import com.fabi.Model.Session;

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
        mList.add(new Electronique(1,"Les Livres télécharges",mElectroniqueTable.getNbrElectronique(mSession.getMatricule())));
        mList.add(new Electronique(2,"Coups de coeur",0));
        mList.add(new Electronique(3,"playlists",0));
        mList.add(new Electronique(4,"Categorie",mElectroniqueTable.getNbrCategorie(mSession.getMatricule())));
        mList.add(new Electronique(5,"Auteurs",mElectroniqueTable.getNbrAuteur(mSession.getMatricule())));
        Cursor cursor = mElectroniqueTable.getData(mSession.getMatricule());
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