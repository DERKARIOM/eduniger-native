package com.ninotech.fabi.controleur.fragment;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.ElectronicAdapter;
import com.ninotech.fabi.controleur.adapter.RecentAdapter;
import com.ninotech.fabi.model.data.Library;
import com.ninotech.fabi.model.data.SimilarBook;
import com.ninotech.fabi.model.table.ElectronicTable;
import com.ninotech.fabi.model.table.LoandTable;
import com.ninotech.fabi.model.table.Session;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_electronic_fragment_media);
        RecyclerView recentRecyclerView = view.findViewById(R.id.recycler_view_electronic_fragment_recent);
        List<Library> libraryList = new ArrayList<>();
        List<SimilarBook> similarBookList = new ArrayList<>();
        ElectronicTable electronicTable = new ElectronicTable(getContext());
        LoandTable loandTable = new LoandTable(getContext());
        Session session = new Session(getContext());
        ElectronicAdapter electronicAdapter = new ElectronicAdapter(libraryList);
        RecentAdapter recentAdapter = new RecentAdapter(similarBookList);
        libraryList.add(new Library(1,R.drawable.img_electronic_book,getString(R.string.dawnloads_book), electronicTable.getNbrElectronic(session.getIdNumber())));
        libraryList.add(new Library(2,R.drawable.img_audio_book,getString(R.string.favorites),0));
        libraryList.add(new Library(3,R.drawable.img_loand_book,getString(R.string.playlists),loandTable.getNbrLoand(session.getIdNumber())));
        libraryList.add(new Library(4,R.drawable.img_category,getString(R.string.cetegory), electronicTable.getNbrCategory(session.getIdNumber())));
        libraryList.add(new Library(5,R.drawable.img_author,getString(R.string.author), electronicTable.getNbrAuthor(session.getIdNumber())));
        try {
            Cursor cursor = electronicTable.getData(session.getIdNumber());
            cursor.moveToFirst();
            do {
                byte[] imageBytes = cursor.getBlob(5);
                // Convertir le tableau d'octets en Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                similarBookList.add(new SimilarBook(cursor.getString(2),bitmap,null));
            }while(cursor.moveToNext());
        }catch (Exception e)
        {
            TextView recentReadBookTextView;
            recentReadBookTextView = view.findViewById(R.id.text_view_fragment_library_recent_read_book);
            recentReadBookTextView.setVisibility(View.GONE);
            Log.e("errElectronicFragment",e.getMessage());
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));
        recentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(electronicAdapter);
        recentRecyclerView.setAdapter(recentAdapter);
        return view;
    }
}