package com.ninotech.fabi.controleur.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.controleur.adapter.ImageAdapter;
import com.ninotech.fabi.model.data.Physical;
import com.ninotech.fabi.controleur.adapter.PhysicalAdapter;
import com.ninotech.fabi.R;
import com.ninotech.fabi.model.table.LoandTable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PhysicalFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_physique, container, false);
        mRecyclerView = view.findViewById(R.id.recylerPysique);
        mList = new ArrayList<>();
        mLoandTable = new LoandTable(getContext());
        Cursor cursor = mLoandTable.getData();
        cursor.moveToFirst();

        try {
            do {
                mList.add(new Physical(cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),percentage(converterDate(cursor.getString(4)),converterDate(cursor.getString(5)),getNowDate())));
            }while (cursor.moveToNext());
            mPhysicalAdapter = new PhysicalAdapter(mList);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mRecyclerView.setAdapter(mPhysicalAdapter);
        }catch (Exception e)
        {
            mImageList = new ArrayList<>();
            mImageList.add("Aucun livre emprunté");
            mImageAdapter = new ImageAdapter(mImageList);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mRecyclerView.setAdapter(mImageAdapter);
            Log.e("errPhysicLoand",e.getMessage());
        }

        return view;
    }
    public long percentage(long startDate , long endDate , long nowDate)
    {
        return (long) (((float)(nowDate - startDate)/(float) (endDate - startDate))*100);
    }
    public long getNowDate()
    {
        long currentTimeMillis = System.currentTimeMillis();
        long currentTimeSeconds = currentTimeMillis / 1000;

        // Affichez le temps actuel en secondes
        return currentTimeSeconds;
    }
    public long converterDate(String dateString)
    {
//        String dateString = "2024-02-13 12:30:00";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long dateInSeconds = 0;

        try {
            // Analyser la chaîne de caractères en objet Date
            Date date = dateFormat.parse(dateString);

            // Convertir la date en millisecondes
            long dateInMillis = date.getTime();

            // Convertir les millisecondes en secondes
            dateInSeconds = dateInMillis / 1000;

            // Afficher la date en secondes
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateInSeconds;
    }
    private RecyclerView mRecyclerView;
    private PhysicalAdapter mPhysicalAdapter;
    private ArrayList<Physical> mList;
    private ArrayList<String> mImageList;
    private LoandTable mLoandTable;
    private ImageAdapter mImageAdapter;

}