package com.example.movieapp.API;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GetMovie {
    @GET("search/movie?api_key=324b959266462db939fe21fe4607f79e")

    Call<Example> getAllData(@Query("query") String query);

    @GET("movie/popular")
    Call<Example> getPopularMovies(@Query("api_key") String apiKey, @Query("page") int page);

    @GET("movie/{movie_id}/videos")
    Call<MovieVideo> getTrailer(@Path("movie_id") int id, @Query("api_key") String apiKey);

    @GET("movie/{movie_id}/similar")
    Call<RelatedMovies> getRelatedMovies(@Path("movie_id") int id, @Query("api_key") String apiKey);

    @GET("movie/{movie_id}/images")
    Call<MovieImages> getMovieImages(@Path("movie_id") int id, @Query("api_key") String apiKey);

    @GET("movie/{movie_id}")
    Call<Result> getMovie(@Path("movie_id") int id, @Query("api_key") String apiKey);
}
