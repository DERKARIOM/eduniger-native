package com.fabi.controleur.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fabi.controleur.adapter.HistoriqueAdapter;
import com.fabi.model.data.Historique;
import com.example.fabi.R;

import java.util.ArrayList;

public class HistoriqueFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historique, container, false);
        mRecyclerView = view.findViewById(R.id.recylerHistorique);
        mList = new ArrayList<>();
        mList.add(new Historique("Mémorisation de femme du silence","Voici le résumé du livre femme du silence"));
        mList.add(new Historique("Mémorisation de femme du silence","Voici le résumé du livre femme du silence"));
        mList.add(new Historique("Mémorisation de femme du silence","Voici le résumé du livre femme du silence"));
        mList.add(new Historique("Mémorisation de femme du silence","Voici le résumé du livre femme du silence"));
        mList.add(new Historique("Mémorisation de femme du silence","Voici le résumé du livre femme du silence"));
        mList.add(new Historique("Mémorisation de femme du silence","Voici le résumé du livre femme du silence"));
        mList.add(new Historique("Mémorisation de femme du silence","Voici le résumé du livre femme du silence"));
        mList.add(new Historique("Mémorisation de femme du silence","Voici le résumé du livre femme du silence"));
        mList.add(new Historique("Mémorisation de femme du silence","Voici le résumé du livre femme du silence"));
        mList.add(new Historique("Mémorisation de femme du silence","Voici le résumé du livre femme du silence"));
        mList.add(new Historique("Mémorisation de femme du silence","Voici le résumé du livre femme du silence"));
        mList.add(new Historique("Mémorisation de femme du silence","Voici le résumé du livre femme du silence"));

        mHistoriqueAdapter = new HistoriqueAdapter(mList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mHistoriqueAdapter);
        return view;
    }
    private RecyclerView mRecyclerView;
    private HistoriqueAdapter mHistoriqueAdapter;
    private ArrayList<Historique> mList;
}