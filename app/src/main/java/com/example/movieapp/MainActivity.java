package com.example.movieapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.movieapp.Fragments.LoginFragment;
import com.example.movieapp.ui.home.HomeFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        FragmentTransaction frag_trans = getSupportFragmentManager().beginTransaction();
        frag_trans.add(R.id.fragment_container,new LoginFragment());
        frag_trans.commit();
    }
}