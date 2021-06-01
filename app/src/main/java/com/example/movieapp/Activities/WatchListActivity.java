package com.example.movieapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.movieapp.API.Client;
import com.example.movieapp.API.Constants;
import com.example.movieapp.API.GetMovie;
import com.example.movieapp.API.Result;
import com.example.movieapp.Adapters.WatchListAdapter;
import com.example.movieapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WatchListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Result> watchList;
    LinearLayoutManager layoutManager;
    WatchListAdapter favoriteListAdapter;

    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    String userId;

    ArrayList<Long> movieIds = new ArrayList<>();
    GetMovie getMovie = Client.getClient().create(GetMovie.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_list);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();
        recyclerView = findViewById(R.id.MovieList);
        watchList = new ArrayList<>();
        favoriteListAdapter = new WatchListAdapter(watchList, this);

        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(favoriteListAdapter);

        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.get().addOnCompleteListener(task -> {
            movieIds = (ArrayList<Long>) task.getResult().get("watchListMovieIds");
            for (Long movieId : movieIds) {
                Call<Result> call = getMovie.getMovie((int) (long) movieId, Constants.API_KEY);
                call.enqueue(new Callback<Result>() {
                    @Override
                    public void onResponse(Call<Result> call, Response<Result> response) {
                        Result movie = response.body();
                        System.out.println(movie.getOriginalTitle());
                        watchList.add(movie);
                        favoriteListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<Result> call, Throwable t) {
                        Toast.makeText(WatchListActivity.this, "Error Fetching data", Toast.LENGTH_SHORT).show();
                    }


                });
            }

        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(R.id.nav_watchList);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    break;
                case R.id.nav_profile:
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    break;
                case R.id.nav_watchList:
                    startActivity(new Intent(getApplicationContext(), WatchListActivity.class));
                    break;
            }
            return true;
        });
    }
}