package com.fabi.Model;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.fabi.Controleur.CategorieFragment;
import com.fabi.Controleur.ClassementFragment;
import com.fabi.Controleur.RecomandeFragment;

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