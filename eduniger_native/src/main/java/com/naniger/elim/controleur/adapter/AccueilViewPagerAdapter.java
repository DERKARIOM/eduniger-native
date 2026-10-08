package com.naniger.elim.controleur.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.naniger.elim.controleur.fragment.BooksFragment;
import com.naniger.elim.controleur.fragment.HomeFragment;
import com.naniger.elim.controleur.fragment.CategoryFragment;
import com.naniger.elim.controleur.fragment.StructureFragment;

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
                return new BooksFragment();
            case 1:
                return new CategoryFragment();
            case 2:
                return new StructureFragment();
        }
        return new HomeFragment(); // Fragment par défaut
    }

    @Override
    public int getItemCount() {
        // Nombre total d'onglets
        return 3;
    }
}