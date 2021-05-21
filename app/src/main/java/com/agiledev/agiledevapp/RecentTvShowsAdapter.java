package com.agiledev.agiledevapp;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.List;

public class RecentTvShowsAdapter extends RecyclerView.Adapter<RecentTvShowsAdapter.MyViewHolder> {

    private Context mContext;
    private List<Globals.trackedTV> tvList;
    public FragmentManager manager;

    class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        ImageView poster;

        MyViewHolder(View view) {
            super(view);
            poster = view.findViewById(R.id.movieImageViewPoster);
            layout = view.findViewById(R.id.movieImageView);
        }
    }

    RecentTvShowsAdapter(Context mContext, List<Globals.trackedTV> tvList, FragmentManager manager) {
        this.mContext = mContext;
        this.tvList = tvList;
        this.manager = manager;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_vertical_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position)  {
        final Globals.trackedTV TV = tvList.get(position);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TvShowFullScreenDialog dialog = TvShowFullScreenDialog.newInstance(TV.id);
                dialog.show(manager, TvShowFullScreenDialog.TAG);
            }
        });

        TmdbClient.loadImage(mContext, TV.poster_path, holder.poster, TmdbClient.imageType.LARGEICON, "movie");
    }

    @Override
    public int getItemCount() {
        return tvList.size();
    }
}
