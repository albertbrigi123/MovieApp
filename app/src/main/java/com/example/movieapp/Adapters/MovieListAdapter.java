package com.example.movieapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movieapp.API.Result;
import com.example.movieapp.R;

import java.util.List;

public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.ViewHolder> {
    List<Result> movielist;
    Context context;

    public MovieListAdapter(List<Result> s, Context context) {
        this.movielist = s;
        this.context=context;
    }

    @NonNull
    @Override
    public MovieListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieListAdapter.ViewHolder holder, int position) {
        holder.title.setText(movielist.get(position).getOriginalTitle());
        Glide.with(context).load(movielist.get(position).getPosterPath()).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return movielist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            image = itemView.findViewById(R.id.thumbnail);
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
