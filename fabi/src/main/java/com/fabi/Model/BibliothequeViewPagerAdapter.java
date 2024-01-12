package com.fabi.Model;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.fabi.Controleur.AudioFragment;
import com.fabi.Controleur.ElectroniqueFragment;
import com.fabi.Controleur.PhysiqueFragment;

public class BibliothequeViewPagerAdapter extends FragmentStateAdapter {

    public BibliothequeViewPagerAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Retournez le fragment associé à chaque onglet
        switch (position) {
            case 0:
                return new ElectroniqueFragment();
            case 1:
                return new AudioFragment();
            case 2:
                return new PhysiqueFragment();
        }
        return new ElectroniqueFragment(); // Fragment par défaut
    }

    @Override
    public int getItemCount() {
        // Nombre total d'onglets
        return 3;
    }
}