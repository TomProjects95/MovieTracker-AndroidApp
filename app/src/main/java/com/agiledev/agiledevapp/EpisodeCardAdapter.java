package com.agiledev.agiledevapp;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class EpisodeCardAdapter extends RecyclerView.Adapter<EpisodeCardAdapter.MyViewHolder> {
    private Context mContext;
    private ArrayList<FullTvSeasonDetails.Episode> mediaList;
    private FragmentManager fragmentManager;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, eNo, airDate;
        ImageView stillImage;
        int epNo;
        RelativeLayout layout;

        MyViewHolder(View v) {
            super(v);

            layout = v.findViewById(R.id.episode_card_layout);
            stillImage = v.findViewById(R.id.episode_card_image);
            title = v.findViewById(R.id.episode_card_title);
            eNo = v.findViewById(R.id.episode_card_sNo_eNo);
            airDate = v.findViewById(R.id.episode_card_airDate);
        }
    }

    EpisodeCardAdapter(Context mContext, ArrayList<FullTvSeasonDetails.Episode> mediaList, FragmentManager fragmentManager) {
        this.mContext = mContext;
        this.mediaList = mediaList;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.episode_basic_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final FullTvSeasonDetails.Episode episode = mediaList.get(position);
        holder.title.setText(episode.getName());
        holder.eNo.setText(episode.getSeason_number() + "x" + episode.getEpisode_number());
        holder.airDate.setText(episode.getAir_date());
        holder.epNo = episode.getEpisode_number();
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TvEpisodeFullScreenDialog dialog = TvEpisodeFullScreenDialog.newInstance(episode.getShow_id(), episode.getSeason_number(), episode.getEpisode_number());
                dialog.show(fragmentManager, TvEpisodeFullScreenDialog.TAG);
            }
        });
        Glide.with(mContext).load("http://image.tmdb.org/t/p/w185/" + episode.getStill_path()).placeholder(R.drawable.placeholder_large_movie).into(holder.stillImage);
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }
}
