package com.fabi.controleur.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.fabi.controleur.fragment.DiscussionFragment;
import com.fabi.controleur.fragment.HistoriqueFragment;

public class AssistanceViewPagerAdapter extends FragmentStateAdapter {

    public AssistanceViewPagerAdapter(Fragment fragment) {
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