package com.fabi.Controleur;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.fabi.Model.AssistanceViewPagerAdapter;
import com.example.fabi.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AssistanceFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assistance, container, false);
        mTabLayout = view.findViewById(R.id.tablayout_assistance);
        mViewPager = view.findViewById(R.id.view_page_assist);
        mViewPagerAdapter = new AssistanceViewPagerAdapter(this);
        mViewPager.setAdapter(mViewPagerAdapter);
        new TabLayoutMediator(mTabLayout,mViewPager,(tab, position) -> {
            switch (position){
                case 0:
                    tab.setText("Discussion");
                    break;
                case 1:
                    tab.setText("Historique");
                    break;
            }
        }).attach();
        return view;
    }
    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;
    private AssistanceViewPagerAdapter mViewPagerAdapter;
}