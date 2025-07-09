package com.ninotech.fabi.controleur.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.controleur.adapter.AudioAdapter;
import com.ninotech.fabi.model.data.OnlineBook;
import com.ninotech.fabi.R;

import java.util.ArrayList;

public class AudioFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio, container, false);
        mRecyclerView = view.findViewById(R.id.recylerAudio);
        mList = new ArrayList<>();
//        mList.add(new Book(R.drawable.l1,"Femmes du silence","Littérature",false,false,false,"5","2h00mn"));
//        mList.add(new Book(R.drawable.l2,"Dans mes reves","Littérature",false,false,false,"6","3h15mn"));
//        mList.add(new Book(R.drawable.l3,"Perdu dans les bois","Entreprenariat",false,false,false,"1","1h10mn"));
//        mList.add(new Book(R.drawable.l4,"Je suis le dernier","Développement personnel ",false,false,false,"10","8h30mn"));
//        mList.add(new Book(R.drawable.l5,"La brume des bois","Littérature",false,false,false,"5","5h30mn"));
        mAudioAdapter = new AudioAdapter(mList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAudioAdapter);
        return view;
    }
    private RecyclerView mRecyclerView;
    private AudioAdapter mAudioAdapter;
    private ArrayList<OnlineBook> mList;
}