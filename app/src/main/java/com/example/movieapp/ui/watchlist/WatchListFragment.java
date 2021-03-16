package com.example.movieapp.ui.watchlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.movieapp.R;
import com.example.movieapp.ui.home.HomeFragment;
import com.example.movieapp.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class WatchListFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_watchlist,container, false);

        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(R.id.nav_watchList);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment=null;
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.nav_watchList :
                        selectedFragment=new WatchListFragment();
                        break;
                    case R.id.nav_profile:
                        selectedFragment=new ProfileFragment();
                        break;
                }
                getActivity().getSupportFragmentManager().
                        beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();
                return true;
            }
        });

        return view;
    }
}
