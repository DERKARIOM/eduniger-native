package com.ninotech.fabi.controleur.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.model.data.Setting;
import com.ninotech.fabi.controleur.adapter.SettingAdapter;
import com.ninotech.fabi.R;

import java.util.ArrayList;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
        ab.setHomeAsUpIndicator(R.drawable.vector_back);
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.custom_action_bar);
        ab.setDisplayHomeAsUpEnabled(true);
        TextView actionBarTitle = ab.getCustomView().findViewById(R.id.action_bar_title);
        actionBarTitle.setText(getString(R.string.setting));
        RecyclerView mSettingRecyclerView = findViewById(R.id.recycler_view_activity_setting);
        ArrayList<Setting> mList = new ArrayList<>();
        mList.add(new Setting(R.drawable.vector_purple_200_compte,getString(R.string.account),getString(R.string.change_password)));
        mList.add(new Setting(R.drawable.vector_purple_200_digital,getString(R.string.digital_print),getString(R.string.secure_session)));
        mList.add(new Setting(R.drawable.vector_purple_200_messagerie,getString(R.string.send_suggestion),getString(R.string.subject_suggestion)));
        mList.add(new Setting(R.drawable.vector_purple_200_start,getString(R.string.evaluate_us),getString(R.string.opservation_you)));
        mList.add(new Setting(R.drawable.vector_purple_200_phone,getString(R.string.contact_us),getString(R.string.call_number)));
        mList.add(new Setting(R.drawable.vector_purple_200_video,getString(R.string.how_it_works),getString(R.string.tutorial_that_explains_you_from_a_z)));
        mList.add(new Setting(R.drawable.vector_purple_200_information,getString(R.string.app_information),getString(R.string.sub_app_information)));
        SettingAdapter mSettingAdapter = new SettingAdapter(mList);
        mSettingRecyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        mSettingRecyclerView.setAdapter(mSettingAdapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id)
        {
            case android.R.id.home:
                onBackPressed(); // Appel de la méthode onBackPressed() pour simuler le comportement du bouton retour
                return true;
            case R.id.item_menu_search:
                Intent searchIntent = new Intent(SettingActivity.this,SearchActivity.class);
                searchIntent.putExtra("search_key","SETTING");
                startActivity(searchIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}