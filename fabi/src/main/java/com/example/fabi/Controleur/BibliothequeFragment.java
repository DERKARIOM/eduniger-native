package com.example.fabi.Controleur;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fabi.Controleur.Model.ViewPagerAdapter2;
import com.example.fabi.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class BibliothequeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bibliotheque, container, false);
        mTabLayout = view.findViewById(R.id.tablayout3);
        mViewPager = view.findViewById(R.id.viewPage3);
        mViewPagerAdapter = new ViewPagerAdapter2(this);
        mViewPager.setAdapter(mViewPagerAdapter);
        new TabLayoutMediator(mTabLayout,mViewPager,(tab, position) -> {
            switch (position){
                case 0:
                    tab.setText("Électronique");
                    break;
                case 1:
                    tab.setText("Audio");
                    break;
                case 2:
                    tab.setText("Physique");
                    break;
            }
        }).attach();
        return view;
    }
    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;
    private ViewPagerAdapter2 mViewPagerAdapter;
}