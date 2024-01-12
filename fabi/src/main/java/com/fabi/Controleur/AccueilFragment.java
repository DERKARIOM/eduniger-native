package com.fabi.Controleur;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.fabi.Model.AccueilViewPagerAdapter;
import com.example.fabi.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AccueilFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_accueil, container, false);
        mTabLayout = view.findViewById(R.id.tablayout);
        mViewPager = view.findViewById(R.id.viewPage);
        mAccueilViewPagerAdapter = new AccueilViewPagerAdapter(this);
        mViewPager.setAdapter(mAccueilViewPagerAdapter);
        new TabLayoutMediator(mTabLayout,mViewPager,(tab, position) -> {
            switch (position){
                case 0:
                    tab.setText("Recommandé");
                    break;
                case 1:
                    tab.setText("Catégorie");
                    break;
                case 2:
                    tab.setText("Classement");
                    break;
            }
        }).attach();
        return view;
    }
    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;
    private AccueilViewPagerAdapter mAccueilViewPagerAdapter;
}