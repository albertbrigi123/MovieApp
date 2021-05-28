package com.example.movieapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.movieapp.API.Client;
import com.example.movieapp.API.Constans;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity {
    TextView titleTW, descriptionTW, ratingTW;
    WebView webView;
    ImageView thumbnail;
    RecyclerView recyclerView, recyclerView2;
    RelatedMoviesAdapter adapter;
    ImagesAdapter imagesAdapter;
    private List<RelatedMoviesResult> relatedlist;
    private List<Poster> posterList;
    LinearLayoutManager layoutManager, layoutManager2;
    RatingBar ratingBar;
    ImageButton addToWatchListBtn;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String moviename, moviedescription, movieimage, video_key, releasedate, movieRating, userId;
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
        webView = findViewById(R.id.webView);
        ratingBar = findViewById(R.id.MovieRating);
        ratingTW = findViewById(R.id.Rating);
        addToWatchListBtn = findViewById(R.id.AddToWatchList);


        Intent intent = getIntent();
        if (intent.hasExtra("originaltitle")) {
            movieId = getIntent().getExtras().getInt("id");
            moviename = getIntent().getExtras().getString("originaltitle");
            moviedescription = getIntent().getExtras().getString("shortdescription");
            movieimage = getIntent().getExtras().getString("image");
            releasedate = getIntent().getExtras().getString("releasedate").substring(0, 4);
            movieRating = getIntent().getExtras().getString("vote_average");
            Glide.with(getApplicationContext()).load(movieimage).into(thumbnail);
            titleTW.setText(moviename + " (" + releasedate + ")");
            ratingBar.setRating(Float.parseFloat(movieRating));
            ratingTW.setText(movieRating);
            descriptionTW.setText(moviedescription);
        } else {
            Toast.makeText(this, "NO API DATA", Toast.LENGTH_LONG).show();
        }
        getRelatedModies();
        getVideo();
        getImages();

        DocumentReference ref = fStore.collection("users").document(userId);

        Query query = fStore.collection("users").whereArrayContains("watchListMovieIds", movieId);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                             @Override
                                             public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                 if(queryDocumentSnapshots.getDocuments().toString() != "[]")
                                                 {
                                                     addToWatchListBtn.setVisibility(View.INVISIBLE);
                                                 }
                                             }
                                         });

                addToWatchListBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DocumentReference documentReference = fStore.collection("users").document(userId);
                        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                movieIds = (ArrayList<Integer>) task.getResult().get("watchListMovieIds");

                                movieIds.add(movieId);
                                documentReference.update("watchListMovieIds", movieIds);
                                addToWatchListBtn.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                });




    }

    private void getRelatedModies() {
        relatedlist = new ArrayList<>();
        adapter = new RelatedMoviesAdapter(this, relatedlist);

        recyclerView = findViewById(R.id.recycler_view1);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        loadRelated();
    }

    private void loadRelated() {
        final int movie_id = getIntent().getExtras().getInt("id");
        try {
            if (Constans.API_KEY.isEmpty()) {
                Toast.makeText(getApplicationContext(), "No API key", Toast.LENGTH_SHORT).show();
                return;
            }
            Client client = new Client();
            GetMovie apiservice = Client.getClient().create(GetMovie.class);
            Call<RelatedMovies> call = apiservice.getRelatedMovies(movie_id, Constans.API_KEY);
            call.enqueue(new Callback<RelatedMovies>() {
                @Override
                public void onResponse(Call<RelatedMovies> call, Response<RelatedMovies> response) {
                    List<RelatedMoviesResult> moviesResults = response.body().getResults();
                    recyclerView.setAdapter(new RelatedMoviesAdapter(getApplicationContext(), moviesResults));
                    recyclerView.smoothScrollToPosition(0);
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
            if (Constans.API_KEY.isEmpty()) {
                Toast.makeText(getApplicationContext(), "No API key", Toast.LENGTH_SHORT).show();
                return;
            }
            Client client = new Client();
            GetMovie apiservice = Client.getClient().create(GetMovie.class);
            Call<MovieVideo> call = apiservice.getTrailer(movie_id, Constans.API_KEY);
            call.enqueue(new Callback<MovieVideo>() {
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

        recyclerView2 = findViewById(R.id.recycler_view2);

        layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView2.setLayoutManager(layoutManager2);

        recyclerView2.setAdapter(imagesAdapter);
        imagesAdapter.notifyDataSetChanged();

        loadImages();
    }

    private void loadImages() {
        final int movie_id = getIntent().getExtras().getInt("id");
        try {
            if (Constans.API_KEY.isEmpty()) {
                Toast.makeText(getApplicationContext(), "No API key", Toast.LENGTH_SHORT).show();
                return;
            }
            Client client = new Client();
            GetMovie apiservice = Client.getClient().create(GetMovie.class);
            Call<MovieImages> call = apiservice.getMovieImages(movie_id, Constans.API_KEY);
            call.enqueue(new Callback<MovieImages>() {
                @Override
                public void onResponse(Call<MovieImages> call, Response<MovieImages> response) {
                    List<Poster> posters = response.body().getPosters();
                    recyclerView2.setAdapter(new ImagesAdapter(getApplicationContext(), posters));
                    recyclerView2.smoothScrollToPosition(0);
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