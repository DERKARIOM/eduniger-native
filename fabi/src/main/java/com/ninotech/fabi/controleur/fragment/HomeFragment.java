package com.ninotech.fabi.controleur.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.AccueilViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accueil, container, false);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout_accuiel);
        ViewPager2 viewPager2 = view.findViewById(R.id.view_page_accuiel);
        AccueilViewPagerAdapter accueilViewPagerAdapter = new AccueilViewPagerAdapter(this);
        viewPager2.setAdapter(accueilViewPagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager2,(tab, position) -> {
            switch (position){
                case 0:
                    tab.setText(getString(R.string.recomended_title));
                    break;
                case 1:
                    tab.setText(getString(R.string.category_title));
                    break;
                case 2:
                    tab.setText(R.string.ranking);
                    break;
            }
        }).attach();
        return view;
    }

}