package com.example.fabi.Controleur.Model;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.fabi.Controleur.AudioFragment;
import com.example.fabi.Controleur.ElectroniqueFragment;
import com.example.fabi.Controleur.PhysiqueFragment;

public class ViewPagerAdapter2 extends FragmentStateAdapter {

    public ViewPagerAdapter2(Fragment fragment) {
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