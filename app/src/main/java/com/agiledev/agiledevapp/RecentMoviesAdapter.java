package com.agiledev.agiledevapp;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class RecentMoviesAdapter extends RecyclerView.Adapter<RecentMoviesAdapter.MyViewHolder> {

    private Context mContext;
    private List<Globals.trackedMovie> movieList;
    public FragmentManager manager;

    class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        ImageView poster;

        MyViewHolder(View view) {
            super(view);
            layout = view.findViewById(R.id.movieImageView);
            poster = view.findViewById(R.id.movieImageViewPoster);
        }
    }

    RecentMoviesAdapter(Context mContext, List<Globals.trackedMovie> movieList, FragmentManager manager) {
        this.mContext = mContext;
        this.movieList = movieList;
        this.manager = manager;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_vertical_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position)  {
        final Globals.trackedMovie movie = movieList.get(position);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MovieFullScreenDialog dialog = MovieFullScreenDialog.newInstance(movie.id);
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
