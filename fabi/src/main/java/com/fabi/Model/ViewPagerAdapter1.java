package com.fabi.Model;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.fabi.Controleur.DiscussionFragment;
import com.fabi.Controleur.HistoriqueFragment;

public class ViewPagerAdapter1 extends FragmentStateAdapter {

    public ViewPagerAdapter1(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Retournez le fragment associé à chaque onglet
        switch (position) {
            case 0:
                return new DiscussionFragment();
            case 1:
                return new HistoriqueFragment();
        }
        return new DiscussionFragment(); // Fragment par défaut
    }

    @Override
    public int getItemCount() {
        // Nombre total d'onglets
        return 2;
    }
}