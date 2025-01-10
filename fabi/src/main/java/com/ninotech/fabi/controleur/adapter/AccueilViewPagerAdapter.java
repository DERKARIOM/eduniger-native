package com.ninotech.fabi.controleur.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ninotech.fabi.controleur.fragment.RankingFragment;
import com.ninotech.fabi.controleur.fragment.RecommendedFragment;
import com.ninotech.fabi.controleur.fragment.CategoryFragment;
import com.ninotech.fabi.controleur.fragment.StructureFragment;

public class AccueilViewPagerAdapter extends FragmentStateAdapter {

    public AccueilViewPagerAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Retournez le fragment associé à chaque onglet
        switch (position) {
            case 0:
                return new RankingFragment();
            case 1:
                return new CategoryFragment();
            case 2:
                return new StructureFragment();
        }
        return new RecommendedFragment(); // Fragment par défaut
    }

    @Override
    public int getItemCount() {
        // Nombre total d'onglets
        return 3;
    }
}