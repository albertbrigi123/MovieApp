package com.example.movieapp.API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieVideo {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("results")
    @Expose
    private List<MovieVideoResult> results = null;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<MovieVideoResult> getResults() {
        return results;
    }

    public void setResults(List<MovieVideoResult> results) {
        this.results = results;
    }

    public MovieVideo(){}

    public MovieVideo(Integer id, List<MovieVideoResult> results) {
        this.id = id;
        this.results = results;
    }
}
