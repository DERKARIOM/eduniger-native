package com.ninotech.fabi.controleur.fragment;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.controleur.adapter.ElectronicAdapter;
import com.ninotech.fabi.model.data.Electronic;
import com.ninotech.fabi.model.table.ElectronicTable;
import com.ninotech.fabi.model.data.SimilarBook;
import com.ninotech.fabi.controleur.adapter.RecentAdapter;
import com.ninotech.fabi.R;
import com.ninotech.fabi.model.table.Session;

import java.util.ArrayList;
import java.util.List;

public class ElectronicFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_electronique, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_electronic_fragment_media);
        RecyclerView recyclerView1 = view.findViewById(R.id.recycler_view_electronic_fragment_recent);
        List<Electronic> ElectronicList = new ArrayList<>();
        List<SimilarBook> similarBookList = new ArrayList<>();
        ElectronicTable electronicTable = new ElectronicTable(getContext());
        Session session = new Session(getContext());
        ElectronicAdapter electronicAdapter = new ElectronicAdapter(ElectronicList);
        RecentAdapter recentAdapter = new RecentAdapter(similarBookList);
        ElectronicList.add(new Electronic(1,getString(R.string.dawnloads_book), electronicTable.getNbrElectronic(session.getIdNumber())));
        ElectronicList.add(new Electronic(2,getString(R.string.favorites),0));
        ElectronicList.add(new Electronic(3,getString(R.string.playlists),0));
        ElectronicList.add(new Electronic(4,getString(R.string.cetegory), electronicTable.getNbrCategory(session.getIdNumber())));
        ElectronicList.add(new Electronic(5,getString(R.string.author), electronicTable.getNbrAuthor(session.getIdNumber())));
        try {
            Cursor cursor = electronicTable.getData(session.getIdNumber());
            cursor.moveToFirst();
            do {
                byte[] imageBytes = cursor.getBlob(5);
                // Convertir le tableau d'octets en Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                similarBookList.add(new SimilarBook(cursor.getString(2),bitmap,cursor.getString(6)));
            }while(cursor.moveToNext());
        }catch (Exception e)
        {
            Log.e("errElectronicFragment",e.getMessage());
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));
        recyclerView1.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(electronicAdapter);
        recyclerView1.setAdapter(recentAdapter);
        return view;
    }

}