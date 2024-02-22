package com.ninotech.fabi.controleur.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.model.data.Parametre;
import com.ninotech.fabi.controleur.adapter.ParametreAdapter;
import com.ninotech.fabi.R;

import java.util.ArrayList;
import java.util.Objects;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Objects.requireNonNull(getSupportActionBar()).hide();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView mSettingRecyclerView = findViewById(R.id.recycler_view_activity_setting);
        ArrayList<Parametre> mList = new ArrayList<>();
        mList.add(new Parametre(R.drawable.vector_purple_200_compte,getString(R.string.account),getString(R.string.change_password)));
        mList.add(new Parametre(R.drawable.vector_purple_200_digital,getString(R.string.digital_print),getString(R.string.secure_session)));
        mList.add(new Parametre(R.drawable.vector_purple_200_messagerie,getString(R.string.send_suggestion),getString(R.string.subject_suggestion)));
        mList.add(new Parametre(R.drawable.vector_purple_200_start,getString(R.string.evaluate_us),getString(R.string.opservation_you)));
        mList.add(new Parametre(R.drawable.vector_purple_200_phone,getString(R.string.contact_us),getString(R.string.call_number)));
        mList.add(new Parametre(R.drawable.vector_purple_200_video,getString(R.string.how_it_works),getString(R.string.tutorial_that_explains_you_from_a_z)));
        mList.add(new Parametre(R.drawable.vector_purple_200_information,getString(R.string.app_information),getString(R.string.sub_app_information)));
        ParametreAdapter mParametreAdapter = new ParametreAdapter(mList);
        mSettingRecyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        mSettingRecyclerView.setAdapter(mParametreAdapter);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Gérer les clics sur les éléments de l'action barre
        switch (item.getItemId()) {
            case android.R.id.home:
                // Appeler onBackPressed() lorsque le bouton de retour de l'action barre est pressé
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // Revenir en arrière tout simplement
        super.onBackPressed();
    }
}