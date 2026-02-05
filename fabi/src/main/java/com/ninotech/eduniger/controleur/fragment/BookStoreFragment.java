package com.ninotech.eduniger.controleur.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.adapter.AccueilViewPagerAdapter;

public class BookStoreFragment extends Fragment {

    private static final int TAB_BOOKS = 0;
    private static final int TAB_CATEGORY = 1;
    private static final int TAB_STRUCTURE = 2;

    private TabLayoutMediator mTabLayoutMediator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_store, container, false);

        setupViewPager(view);

        return view;
    }

    private void setupViewPager(View view) {
        TabLayout tabLayout = view.findViewById(R.id.tab_layout_accuiel);
        ViewPager2 viewPager2 = view.findViewById(R.id.view_page_accuiel);

        AccueilViewPagerAdapter adapter = new AccueilViewPagerAdapter(this);
        viewPager2.setAdapter(adapter);

        mTabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> tab.setText(getTabTitle(position))
        );
        mTabLayoutMediator.attach();
    }

    private String getTabTitle(int position) {
        switch (position) {
            case TAB_BOOKS:
                return getString(R.string.books_title);
            case TAB_CATEGORY:
                return getString(R.string.category_title);
            case TAB_STRUCTURE:
                return getString(R.string.structure);
            default:
                return "";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mTabLayoutMediator != null) {
            mTabLayoutMediator.detach();
        }
    }
}