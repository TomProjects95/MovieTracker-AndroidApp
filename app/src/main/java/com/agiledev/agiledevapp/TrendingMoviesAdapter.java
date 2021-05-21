package com.agiledev.agiledevapp;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class TrendingMoviesAdapter extends RecyclerView.Adapter<TrendingMoviesAdapter.MyViewHolder> {

    private Context mContext;
    private List<Globals.trendingMovie> movieList;
    public FragmentManager manager;

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        ImageView poster;
        String id;
        TrendingMoviesAdapter adapter;
        RelativeLayout layout;
        RatingBar rating;

        MyViewHolder(View view) {
            super(view);

            poster = view.findViewById(R.id.trendingmoviesimg);
            rating = view.findViewById(R.id.trendingmovierating);
            layout = view.findViewById(R.id.trendingmovieslayout);
        }
    }

    TrendingMoviesAdapter(Context mContext, List<Globals.trendingMovie> movieList, FragmentManager manager) {
        this.mContext = mContext;
        this.movieList = movieList;
        this.manager = manager;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.trending_movieimage, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position)  {
        Globals.trendingMovie movie = movieList.get(position);
        holder.id = movie.id;
        holder.rating.setRating(movie.vote_average);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MovieFullScreenDialog dialog = MovieFullScreenDialog.newInstance(holder.id);
                dialog.show(manager, MovieFullScreenDialog.TAG);
            }
        });
        TmdbClient.loadImage(mContext, movie.poster_path, holder.poster, TmdbClient.imageType.LARGEICON, "movie");

    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }
}
