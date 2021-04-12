package com.example.movieapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.movieapp.API.Client;
import com.example.movieapp.API.Constans;
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
    private int TOTAL_PAGE = 20;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int CURRENT_PAGE = START_PAGE;

    Client client = new Client();
    GetMovie getMovie = client.getClient().create(GetMovie.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();

        recyclerView = findViewById(R.id.listmovies);
        searchMovieET = findViewById(R.id.SearchMovieText);
        searchButton = findViewById(R.id.SearchButton);

        movieList = new ArrayList<>();
        movieListAdapter = new MovieListAdapter(movieList, this);

        layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
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
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextPage();
                    }
                }, 1000);
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

        Call<Example> call = getMovie.getPopularMovies(Constans.API_KEY, START_PAGE);
        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {
                List<Result> movies = response.body().getResults();
                recyclerView.setAdapter(new MovieListAdapter(movies, HomeActivity.this));

                if(CURRENT_PAGE <= TOTAL_PAGE ){
                    movieListAdapter.addBottemIttem();
                }else{
                    isLastPage = true;
                }
            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {
                Log.d("error", t.getMessage());
                Toast.makeText(HomeActivity.this, "Error Fetching data", Toast.LENGTH_SHORT).show();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String searchedMovie = searchMovieET.getText().toString();
                Call<Example> call = getMovie.getAllData(searchedMovie);
                call.enqueue(new Callback<Example>() {
                    @Override
                    public void onResponse(Call<Example> call, Response<Example> response) {
                        List<Result> movie = response.body().getResults();
                        recyclerView.setAdapter(new MovieListAdapter(movie, getApplicationContext()));
                    }

                    @Override
                    public void onFailure(Call<Example> call, Throwable t) {
                        Log.d("error", t.getMessage());
                        Toast.makeText(HomeActivity.this, "Error Fetching data", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                        break;
                    case R.id.nav_profile :
                        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
                        break;
                    case R.id.nav_watchList:
                        startActivity(new Intent(getApplicationContext(),WatchListActivity.class));
                        break;
                }
                return true;
            }
        });
    }

    private void loadNextPage() {

        Call<Example> call = getMovie.getPopularMovies(Constans.API_KEY,CURRENT_PAGE);
        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {
                List<Result> movies = response.body().getResults();
                movieListAdapter.removedLastEmptyItem();
                isLoading=false;
                movieListAdapter.addAll(movies);

                if(CURRENT_PAGE != TOTAL_PAGE ){
                    movieListAdapter.addBottemIttem();
                }else{
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