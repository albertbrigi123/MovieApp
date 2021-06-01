package com.example.movieapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.movieapp.API.Client;
import com.example.movieapp.API.Constants;
import com.example.movieapp.API.GetMovie;
import com.example.movieapp.API.MovieImages;
import com.example.movieapp.API.MovieVideo;
import com.example.movieapp.API.MovieVideoResult;
import com.example.movieapp.API.Poster;
import com.example.movieapp.API.RelatedMovies;
import com.example.movieapp.API.RelatedMoviesResult;
import com.example.movieapp.Adapters.ImagesAdapter;
import com.example.movieapp.Adapters.RelatedMoviesAdapter;
import com.example.movieapp.R;
import com.example.movieapp.WebViewController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity {
    TextView titleTW, descriptionTW, ratingTW;
    WebView webView;
    ImageView thumbnail;
    RecyclerView relatedMovieRecyclerView, imageRecyclerView;
    RatingBar ratingBar;
    ImageButton addToWatchListBtn;

    RelatedMoviesAdapter adapter;
    ImagesAdapter imagesAdapter;

    protected List<RelatedMoviesResult> relatedMovieList;
    protected List<Poster> posterList;

    LinearLayoutManager relatedMoviesLayoutManager, imageLayoutManager;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    String movieName, movieDescription, movieImage, video_key, releaseDate, movieRating, userId;
    ArrayList<Integer> movieIds = new ArrayList<>();
    Integer movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        titleTW = findViewById(R.id.TitleAndReleaseDate);
        descriptionTW = findViewById(R.id.Description);
        thumbnail = findViewById(R.id.Thumbnail);
        webView = findViewById(R.id.TrailerWebView);
        ratingBar = findViewById(R.id.MovieRating);
        ratingTW = findViewById(R.id.RatingScore);
        addToWatchListBtn = findViewById(R.id.AddToWatchListBtn);

        Intent intent = getIntent();
        if (intent.hasExtra("originaltitle")) {
            movieId = getIntent().getExtras().getInt("id");
            movieName = getIntent().getExtras().getString("originaltitle");
            movieDescription = getIntent().getExtras().getString("shortdescription");
            movieImage = getIntent().getExtras().getString("image");
            releaseDate = getIntent().getExtras().getString("releasedate").substring(0, 4);
            movieRating = getIntent().getExtras().getString("vote_average");
            Glide.with(getApplicationContext()).load(movieImage).into(thumbnail);
            titleTW.setText(movieName + " (" + releaseDate + ")");
            ratingBar.setRating(Float.parseFloat(movieRating));
            ratingTW.setText(movieRating);
            descriptionTW.setText(movieDescription);
        } else {
            Toast.makeText(this, "NO API DATA", Toast.LENGTH_LONG).show();
        }
        getRelatedMovies();
        getVideo();
        getImages();

        Query query = fStore.collection("users").whereArrayContains("watchListMovieIds", movieId);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.getDocuments().toString().equals("[]")) {
                addToWatchListBtn.setVisibility(View.INVISIBLE);
            }
        });

        addToWatchListBtn.setOnClickListener(v -> {
            DocumentReference documentReference = fStore.collection("users").document(userId);
            documentReference.get().addOnCompleteListener(task -> {
                movieIds = (ArrayList<Integer>) task.getResult().get("watchListMovieIds");

                assert movieIds != null;
                movieIds.add(movieId);
                documentReference.update("watchListMovieIds", movieIds);
                Toast.makeText(DetailsActivity.this, "The movie added to your watchlist!", Toast.LENGTH_SHORT).show();
                addToWatchListBtn.setVisibility(View.INVISIBLE);
            });
        });
    }

    private void getRelatedMovies() {
        relatedMovieList = new ArrayList<>();
        adapter = new RelatedMoviesAdapter(this, relatedMovieList);

        relatedMovieRecyclerView = findViewById(R.id.RelatedMovieList);

        relatedMoviesLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        relatedMovieRecyclerView.setLayoutManager(relatedMoviesLayoutManager);

        relatedMovieRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        loadRelated();
    }

    private void loadRelated() {
        final int movie_id = getIntent().getExtras().getInt("id");
        try {
            GetMovie apiservice = Client.getClient().create(GetMovie.class);
            Call<RelatedMovies> call = apiservice.getRelatedMovies(movie_id, Constants.API_KEY);
            call.enqueue(new Callback<RelatedMovies>() {
                @Override
                public void onResponse(Call<RelatedMovies> call, Response<RelatedMovies> response) {
                    List<RelatedMoviesResult> moviesResults = response.body().getResults();
                    relatedMovieRecyclerView.setAdapter(new RelatedMoviesAdapter(getApplicationContext(), moviesResults));
                    relatedMovieRecyclerView.smoothScrollToPosition(0);
                }

                @Override
                public void onFailure(Call<RelatedMovies> call, Throwable t) {

                }
            });
        } catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getVideo() {
        final int movie_id = getIntent().getExtras().getInt("id");
        try {

            GetMovie apiservice = Client.getClient().create(GetMovie.class);
            Call<MovieVideo> call = apiservice.getTrailer(movie_id, Constants.API_KEY);
            call.enqueue(new Callback<MovieVideo>() {
                @SuppressLint("SetJavaScriptEnabled")
                @Override
                public void onResponse(Call<MovieVideo> call, Response<MovieVideo> response) {
                    List<MovieVideoResult> trailer = response.body().getResults();
                    webView.setWebViewClient(new WebViewController());
                    webView.getSettings().setJavaScriptEnabled(true);
                    video_key = trailer.get(0).getKey();
                    webView.loadUrl("https://www.youtube.com/watch?v=" + video_key);
                }

                @Override
                public void onFailure(Call<MovieVideo> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    Toast.makeText(DetailsActivity.this, "Error fetching trailer data", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();

        }
    }

    private void getImages() {
        posterList = new ArrayList<>();
        imagesAdapter = new ImagesAdapter(this, posterList);

        imageRecyclerView = findViewById(R.id.ImageList);

        imageLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        imageRecyclerView.setLayoutManager(imageLayoutManager);

        imageRecyclerView.setAdapter(imagesAdapter);
        imagesAdapter.notifyDataSetChanged();

        loadImages();
    }

    private void loadImages() {
        final int movie_id = getIntent().getExtras().getInt("id");
        try {
            GetMovie apiservice = Client.getClient().create(GetMovie.class);
            Call<MovieImages> call = apiservice.getMovieImages(movie_id, Constants.API_KEY);
            call.enqueue(new Callback<MovieImages>() {
                @Override
                public void onResponse(Call<MovieImages> call, Response<MovieImages> response) {
                    List<Poster> posters = response.body().getPosters();
                    imageRecyclerView.setAdapter(new ImagesAdapter(getApplicationContext(), posters));
                    imageRecyclerView.smoothScrollToPosition(0);
                }

                @Override
                public void onFailure(Call<MovieImages> call, Throwable t) {

                }
            });
        } catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}