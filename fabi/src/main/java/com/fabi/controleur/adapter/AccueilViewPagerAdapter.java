package com.fabi.controleur.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.fabi.controleur.fragment.ClassementFragment;
import com.fabi.controleur.fragment.RecomandeFragment;
import com.fabi.controleur.fragment.CategorieFragment;

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
                return new RecomandeFragment();
            case 1:
                return new CategorieFragment();
            case 2:
                return new ClassementFragment();
        }
        return new RecomandeFragment(); // Fragment par défaut
    }

    @Override
    public int getItemCount() {
        // Nombre total d'onglets
        return 3;
    }
}