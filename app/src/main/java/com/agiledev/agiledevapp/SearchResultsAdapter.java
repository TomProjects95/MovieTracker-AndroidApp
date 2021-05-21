package com.agiledev.agiledevapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.agiledev.agiledevapp.MediaTracking.Media;

import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.MyViewHolder> {

    private Context mContext;
    private Activity mActivity;
    private View mView;
    private List mediaList;
    private FragmentManager manager;
    private String type;

    private BasicMovieDetails movie;
    private BasicTvShowDetails tv;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, genres, release_date;
        ImageView poster;
        String id;
        RelativeLayout layout;

        MyViewHolder(View v) {
            super(v);

            title = v.findViewById(R.id.movieCardTitle);
            genres = v.findViewById(R.id.movieCardGenres);
            release_date = v.findViewById(R.id.movieCardReleaseDate);
            poster = v.findViewById(R.id.movieCardPoster);
            layout = v.findViewById(R.id.movieCard);
        }
    }

    SearchResultsAdapter(Context mContext, Activity mActivity, View mView, List mediaList, FragmentManager manager, String type) {
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.mView = mView;
        this.mediaList = mediaList;
        this.manager = manager;
        this.type = type;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_movie_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position)  {
        if (type.equals("Movie")) {
            movie = (BasicMovieDetails)mediaList.get(position);
            holder.title.setText(movie.getTitle());
            holder.genres.setText(movie.getGenreNames());
            holder.release_date.setText((movie.getRelease_date().equals("") ? "No Release" : mContext.getString(R.string.movie_card_released, movie.getRelease_date())));
            holder.id = movie.getId();
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MovieFullScreenDialog dialog = MovieFullScreenDialog.newInstance(holder.id);
                    dialog.show(manager, MovieFullScreenDialog.TAG);
                }
            });
            TmdbClient.loadImage(mContext, movie.getPoster_path(), holder.poster, TmdbClient.imageType.ICON, "movie");

        } else if (type.equals("TV")) {
            tv = (BasicTvShowDetails) mediaList.get(position);
            holder.title.setText(tv.getName());
            holder.genres.setText(tv.getGenreNames());
            holder.release_date.setText((tv.getFirst_air_date().equals("") ? "No Release" : mContext.getString(R.string.movie_card_released, tv.getFirst_air_date())));
            holder.id = tv.getId();
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TvShowFullScreenDialog dialog = TvShowFullScreenDialog.newInstance(holder.id);
                    dialog.show(manager, TvShowFullScreenDialog.TAG);
                }
            });
            TmdbClient.loadImage(mContext, tv.getPoster_path(), holder.poster, TmdbClient.imageType.ICON, "movie");
        }

    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public void trackItem(boolean tracking, Media type, final String username) {
        if (tracking && type == Media.MOVIE) {
            MediaTracking.trackMovie(mActivity, mActivity, username, movie.getId(), null).show();
        } else if (!tracking && type == Media.MOVIE) {
            MediaTracking.untrackMovie(mActivity, mActivity, username, movie.getId(), null).show();
        }
    }
}