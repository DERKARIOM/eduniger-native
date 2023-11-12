package com.example.fabi.Controleur;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fabi.Controleur.Model.Electronique;
import com.example.fabi.Controleur.Model.ElectroniqueAdapter;
import com.example.fabi.Controleur.Model.Recenmment;
import com.example.fabi.Controleur.Model.RecenmmentAdapter;
import com.example.fabi.R;

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
        mElectroniqueAdapter = new ElectroniqueAdapter(mList);
        mRecenmmentAdapter = new RecenmmentAdapter(mList1);
        mList.add(new Electronique("Les Livres téléchargée",0));
        mList.add(new Electronique("Coups de coeur",0));
        mList.add(new Electronique("playlists",0));
        mList.add(new Electronique("Categorie",0));
        mList.add(new Electronique("Auteurs",0));
//        mList1.add(new Recenmment(R.drawable.l1));
//        mList1.add(new Recenmment(R.drawable.l2));
//        mList1.add(new Recenmment(R.drawable.l3));
//        mList1.add(new Recenmment(R.drawable.l4));
//        mList1.add(new Recenmment(R.drawable.l5));
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));
//        mRecyclerView1.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
//        mRecyclerView.setAdapter(mElectroniqueAdapter);
//        mRecyclerView1.setAdapter(mRecenmmentAdapter);
        return view;
    }
    private RecyclerView mRecyclerView;
    private RecyclerView mRecyclerView1;
    private ElectroniqueAdapter mElectroniqueAdapter;
    private RecenmmentAdapter mRecenmmentAdapter;
    private List<Electronique> mList;
    private List<Recenmment> mList1;

}