package com.example.movieapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.movieapp.API.Client;
import com.example.movieapp.API.Constants;
import com.example.movieapp.API.Example;
import com.example.movieapp.API.GetMovie;
import com.example.movieapp.API.Result;
import com.example.movieapp.Adapters.MovieListAdapter;
import com.example.movieapp.PaginationScrollListener;
import com.example.movieapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Result> movieList;
    GridLayoutManager layoutManager;
    MovieListAdapter movieListAdapter;
    EditText searchMovieET;
    Button searchButton;

    private static final int START_PAGE = 1;
    private final int TOTAL_PAGE = 20;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int CURRENT_PAGE = START_PAGE;

    GetMovie getMovie = Client.getClient().create(GetMovie.class);

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();

        recyclerView = findViewById(R.id.MovieList);
        searchMovieET = findViewById(R.id.SearchMovieText);
        searchButton = findViewById(R.id.SearchButton);

        movieList = new ArrayList<>();
        movieListAdapter = new MovieListAdapter(movieList, this);

        layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        }

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(movieListAdapter);
        movieListAdapter.notifyDataSetChanged();

        recyclerView.addOnScrollListener(new PaginationScrollListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                CURRENT_PAGE += 1;
                new Handler().postDelayed(() -> loadNextPage(), 1000);
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGE;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        Call<Example> call = getMovie.getPopularMovies(Constants.API_KEY, START_PAGE);
        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {
                List<Result> movies = response.body().getResults();
                recyclerView.setAdapter(new MovieListAdapter(movies, HomeActivity.this));

                if (CURRENT_PAGE <= TOTAL_PAGE) {
                    movieListAdapter.addBottomItem();
                } else {
                    isLastPage = true;
                }
            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {
                Log.d("error", t.getMessage());
                Toast.makeText(HomeActivity.this, "Error Fetching data", Toast.LENGTH_SHORT).show();
            }
        });

        searchButton.setOnClickListener(v -> {
            final String searchedMovie = searchMovieET.getText().toString();
            Call<Example> call1 = getMovie.getAllData(searchedMovie);
            call1.enqueue(new Callback<Example>() {
                @Override
                public void onResponse(Call<Example> call1, Response<Example> response) {
                    List<Result> movie = response.body().getResults();
                    recyclerView.setAdapter(new MovieListAdapter(movie, getApplicationContext()));
                }

                @Override
                public void onFailure(Call<Example> call1, Throwable t) {
                    Log.d("error", t.getMessage());
                    Toast.makeText(HomeActivity.this, "Error Fetching data", Toast.LENGTH_SHORT).show();
                }
            });

        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
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

    private void loadNextPage() {

        Call<Example> call = getMovie.getPopularMovies(Constants.API_KEY, CURRENT_PAGE);
        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {
                List<Result> movies = response.body().getResults();
                movieListAdapter.removedLastEmptyItem();
                isLoading = false;
                movieListAdapter.addAll(movies);

                if (CURRENT_PAGE != TOTAL_PAGE) {
                    movieListAdapter.addBottomItem();
                } else {
                    isLastPage = true;
                }

            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {
                Log.d("error", t.getMessage());
                Toast.makeText(HomeActivity.this, "Error Fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}