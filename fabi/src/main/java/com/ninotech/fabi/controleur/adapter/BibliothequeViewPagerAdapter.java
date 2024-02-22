package com.ninotech.fabi.controleur.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ninotech.fabi.controleur.fragment.ElectronicFragment;
import com.ninotech.fabi.controleur.fragment.PhysicalFragment;
import com.ninotech.fabi.controleur.fragment.AudioFragment;

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
                return new ElectronicFragment();
            case 1:
                return new AudioFragment();
            case 2:
                return new PhysicalFragment();
        }
        return new ElectronicFragment(); // Fragment par défaut
    }

    @Override
    public int getItemCount() {
        // Nombre total d'onglets
        return 3;
    }
}