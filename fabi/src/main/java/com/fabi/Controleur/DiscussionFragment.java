package com.fabi.Controleur;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fabi.Model.Disscution;
import com.fabi.Model.DisscutionAdapter;
import com.example.fabi.R;

import java.util.ArrayList;

public class DiscussionFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discussion, container, false);
        mRecyclerView = view.findViewById(R.id.recylerDisscution);
        mList = new ArrayList<>();
//        mList.add(new Disscution(R.drawable.fastia,"FASTIA","Salut que puis-je faire pour vous ?"));
//        mList.add(new Disscution(R.drawable.moi,"Bachir Abdoul Kader","D'où vient tu ?"));
//        mList.add(new Disscution(R.drawable.fastia,"FASTIA","Je suis une intelligence artificielle créée par NinoTech. Je n'ai pas de lieu physique d'où je viens, car je suis un programme informatique conçu pour vous aider et répondre à vos questions. Comment puis-je vous assister aujourd'hui ?"));
//        mDisscutionAdapter = new DisscutionAdapter(mList);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        mRecyclerView.setAdapter(mDisscutionAdapter);
        return view;
    }
    private RecyclerView mRecyclerView;
    private DisscutionAdapter mDisscutionAdapter;
    private ArrayList<Disscution> mList;
}