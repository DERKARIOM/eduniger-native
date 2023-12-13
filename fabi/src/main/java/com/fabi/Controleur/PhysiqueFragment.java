package com.fabi.Controleur;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fabi.Model.Pysique;
import com.fabi.Model.PysiqueAdapter;
import com.example.fabi.R;

import java.util.ArrayList;

public class PhysiqueFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_physique, container, false);
        mRecyclerView = view.findViewById(R.id.recylerPysique);
        mList = new ArrayList<>();
        mList.add(new Pysique(R.drawable.l1,"Femmes du silenc","17/010/2023","20/10/2023","20"));
        mList.add(new Pysique(R.drawable.l2,"Dans mes reves","17/010/2023","20/10/2023","60"));
        mList.add(new Pysique(R.drawable.l3,"Perdu dans les bois","17/010/2023","20/10/2023","30"));
        mPysiqueAdapter = new PysiqueAdapter(mList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mPysiqueAdapter);
        return view;
    }
    private RecyclerView mRecyclerView;
    private PysiqueAdapter mPysiqueAdapter;
    private ArrayList<Pysique> mList;
}