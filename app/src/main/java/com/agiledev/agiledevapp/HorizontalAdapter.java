package com.agiledev.agiledevapp;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

/**
 * Created by t7097354 on 02/04/19.
 */

public class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.MyViewHolder> {

    private Context mContext;
    private CopyOnWriteArrayList mediaList;
    private FragmentManager manager;
    private MediaType mediaType;
    private String string;
    private ProfileFragment fragment;

    class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        ImageView poster;
        TextView textView;

        MyViewHolder(View view) {
            super(view);
            layout = view.findViewById(R.id.movieImageView);
            poster = view.findViewById(R.id.movieImageViewPoster);
            textView = view.findViewById(R.id.movieImageViewText);
        }
    }

    HorizontalAdapter(Context mContext, List mediaList, FragmentManager manager, MediaType mediaType, @Nullable String string, @Nullable ProfileFragment fragment) {
        this.mContext = mContext;
        this.mediaList = new CopyOnWriteArrayList(mediaList);
        this.manager = manager;
        this.mediaType = mediaType;
        this.string = string;
        this.fragment = fragment;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_vertical_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        switch (mediaType) {
            case MOVIE:
                final Globals.trackedMovie movie = (Globals.trackedMovie)mediaList.get(position);
                holder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fragment.openMovieDialog(movie.id);
                    }
                });
                Glide.with(mContext).load(mContext.getResources().getString(R.string.poster_icon_base_url) + movie.poster_path).placeholder(R.drawable.placeholder_med_movie).override((int)(92 * mContext.getResources().getDisplayMetrics().density), (int)(154 * mContext.getResources().getDisplayMetrics().density)).dontAnimate().into(holder.poster);
                break;
            case TV:
                final Globals.trackedTV tv = (Globals.trackedTV)mediaList.get(position);
                holder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fragment.openTVDialog(tv.id);
                    }
                });
                Glide.with(mContext).load(mContext.getResources().getString(R.string.poster_icon_base_url) + tv.poster_path).placeholder(R.drawable.placeholder_med_movie).override((int)(92 * mContext.getResources().getDisplayMetrics().density), (int)(154 * mContext.getResources().getDisplayMetrics().density)).dontAnimate().into(holder.poster);
                break;
            case SEASON:
                final FullTvShowDetails.season season = (FullTvShowDetails.season)mediaList.get(position);
                if (season.episode_count <= 0)
                    break;
                holder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TvSeasonFullScreenDialog dialog = TvSeasonFullScreenDialog.newInstance(string, season.season_number);
                        dialog.show(manager, TvSeasonFullScreenDialog.TAG);
                    }
                });
                Glide.with(mContext).load(mContext.getResources().getString(R.string.poster_icon_base_url_large) + season.poster_path).placeholder(R.drawable.placeholder_med_movie).override((int)(92 * mContext.getResources().getDisplayMetrics().density), (int)(154 * mContext.getResources().getDisplayMetrics().density)).dontAnimate().into(holder.poster);
                holder.textView.setVisibility(View.VISIBLE);
                holder.textView.setText(season.name);
                break;
        }
    }

    enum MediaType {
        MOVIE,
        TV,
        SEASON
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public void updateList(List mediaList) {
        this.mediaList.clear();
        this.mediaList.addAll(mediaList);
        notifyDataSetChanged();
    }
}
