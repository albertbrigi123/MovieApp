package com.example.movieapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieapp.API.Result;
import com.example.movieapp.Activities.DetailsActivity;
import com.example.movieapp.R;

import java.util.List;

public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.ViewHolder>  {
    List<Result> movielist;
    Context context;

    public DetailsAdapter(List<Result> s, Context context) {
        this.movielist = s;
        this.context=context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_details, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(movielist.get(position).getOriginalTitle());
        holder.shortdescription.setText(movielist.get(position).getOverview());
        String rate = Double.toString(movielist.get(position).getVoteAverage());
        holder.rating.setText(rate);
    }

    @Override
    public int getItemCount() {
        return movielist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, shortdescription, rating;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.TitleAndReleaseDate);
            shortdescription = itemView.findViewById(R.id.Description);
            image = itemView.findViewById(R.id.Thumbnail);
            rating = itemView.findViewById(R.id.MovieRating);
        }
    }

    public void add(Result movie){
        movielist.add(movie);
        notifyItemInserted(movielist.size()-1);
    }

    public void addAll(List<Result> movies){
        for(Result m: movies){
            add(m);
        }
    }
    //add empty item
    public void addBottemIttem(){
        add(new Result());
    }

    public void removedLastEmptyItem(){
        int position = movielist.size()-1;
        Result item =  getItem(position);
        if(item != null){
            movielist.remove(position);
            notifyItemRemoved(position);
        }
    }

    private Result getItem(int position){
        return movielist.get(position);
    }
}
