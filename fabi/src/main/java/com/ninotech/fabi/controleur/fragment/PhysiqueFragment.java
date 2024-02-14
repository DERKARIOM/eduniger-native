package com.ninotech.fabi.controleur.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.model.data.Physical;
import com.ninotech.fabi.controleur.adapter.PhysicalAdapter;
import com.ninotech.fabi.R;
import com.ninotech.fabi.model.table.LoandTable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PhysiqueFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_physique, container, false);
        mRecyclerView = view.findViewById(R.id.recylerPysique);
        mList = new ArrayList<>();
        mLoandTable = new LoandTable(getContext());
        long currentTimeMillis = System.currentTimeMillis();
        long currentTimeSeconds = currentTimeMillis / 1000;

        // Affichez le temps actuel en secondes
        Log.d("Current Time (Seconds)", String.valueOf(currentTimeSeconds));


        // Chaîne de caractères représentant la date
        String dateString = "2024-02-13 12:30:00";

        // Format de la date dans la chaîne de caractères
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            // Analyser la chaîne de caractères en objet Date
            Date date = dateFormat.parse(dateString);

            // Convertir la date en millisecondes
            long dateInMillis = date.getTime();

            // Convertir les millisecondes en secondes
            long dateInSeconds = dateInMillis / 1000;

            // Afficher la date en secondes
            Log.d("Date in Seconds", String.valueOf(dateInSeconds));
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Cursor cursor = mLoandTable.getData();
        cursor.moveToFirst();
        try {
            do {
                mList.add(new Physical(cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5)));
            }while (cursor.moveToNext());
        }catch (Exception e)
        {
            Log.e("errPhysicLoand",e.getMessage());
        }

        mPhysicalAdapter = new PhysicalAdapter(mList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mPhysicalAdapter);
        return view;
    }
    private RecyclerView mRecyclerView;
    private PhysicalAdapter mPhysicalAdapter;
    private ArrayList<Physical> mList;
    private LoandTable mLoandTable;
}