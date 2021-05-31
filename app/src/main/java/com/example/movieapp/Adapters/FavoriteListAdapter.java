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

import com.bumptech.glide.Glide;
import com.example.movieapp.API.Result;
import com.example.movieapp.Activities.DetailsActivity;
import com.example.movieapp.R;

import java.util.List;

public class FavoriteListAdapter extends RecyclerView.Adapter<FavoriteListAdapter.ViewHolder> {
    List<Result> watchList;
    Context context;

    public FavoriteListAdapter(List<Result> s, Context context) {
        this.watchList = s;
        this.context = context;
    }

    @NonNull
    @Override
    public FavoriteListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_movie_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(watchList.get(position).getOriginalTitle());
        Glide.with(context).load(watchList.get(position).getPosterPath()).into(holder.image);
    }


    @Override
    public int getItemCount() {
        return watchList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.TitleAndReleaseDate);
            image = itemView.findViewById(R.id.Thumbnail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION){
                        Result clickedDataItem = watchList.get(pos);
                        Intent intent = new Intent(context, DetailsActivity.class);
                        intent.putExtra("id",watchList.get(pos).getId());
                        intent.putExtra("originaltitle", watchList.get(pos).getOriginalTitle());
                        intent.putExtra("shortdescription", watchList.get(pos).getOverview());
                        intent.putExtra("image",watchList.get(pos).getPosterPath());
                        intent.putExtra("releasedate", watchList.get(pos).getReleaseDate());
                        intent.putExtra("vote_average", Double.toString(watchList.get(pos).getVoteAverage()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }
            });

        }

        public void add(Result movie){
            watchList.add(movie);
            notifyItemInserted(watchList.size()-1);
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
            int position = watchList.size()-1;
            Result item =  getItem(position);
            if(item != null){
                watchList.remove(position);
                notifyItemRemoved(position);
            }
        }

        private Result getItem(int position){
            return watchList.get(position);
        }
    }
}







