package com.agiledev.agiledevapp;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by s6104158 on 07/02/19.
 */

public class HomeFragment extends Fragment {

    View myView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        myView = inflater.inflate(R.layout.fragment_home, container, false);

        ViewPager viewPager = myView.findViewById(R.id.viewPager);
        TabLayout tabLayout = myView.findViewById(R.id.tabLayout);
        TabAdapter Adapter = new TabAdapter((getActivity()).getSupportFragmentManager());
        Adapter.addFragment(new trendingMoviesFragment(), "Movies");
        Adapter.addFragment(new trendingTvShowsFragment(), "Tv Shows");
        viewPager.setAdapter(Adapter);
        tabLayout.setupWithViewPager(viewPager);

        getActivity().setTitle(R.string.Trending_name);

        return myView;
    }
}
