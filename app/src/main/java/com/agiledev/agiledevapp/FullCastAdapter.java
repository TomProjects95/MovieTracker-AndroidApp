package com.agiledev.agiledevapp;


import android.app.FragmentManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by t7037453 on 28/02/19.
 */

public class FullCastAdapter extends RecyclerView.Adapter<FullCastAdapter.MyViewHolder> {

    private Context mContext;
    private List<MovieCredits.Cast> castList;
    private android.support.v4.app.FragmentManager manager;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView realName, charName, gender, DOB, died;
        ImageView image;
        String id;
        RelativeLayout layout;
        MovieCastAdapter.Person person;

        MyViewHolder(View view) {
            super(view);
            realName = view.findViewById(R.id.movieCastCardName);
            charName = view.findViewById(R.id.movieCastCardCharacter);
            gender = view.findViewById(R.id.movieCastCardGender);
            image = view.findViewById(R.id.movieCastCardImage);

            layout = view.findViewById(R.id.movieCastCard);
        }
    }

    FullCastAdapter(Context mContext, List<MovieCredits.Cast> castList, android.support.v4.app.FragmentManager manager) {
        this.mContext = mContext;
        this.castList = castList;
        this.manager = manager;
    }

    @Override
    public FullCastAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cast_movie_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position)  {
        final MovieCredits.Cast cast = castList.get(position);

        holder.realName.setText(cast.getName());
        holder.charName.setText(cast.getCharacter());
        holder.gender.setText(cast.getGender() == 1 ? "Female" : "Male");

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CastDialog dialog = CastDialog.newInstance(cast.getId());

                dialog.show(manager, CastDialog.TAG);
            }
        });

        TmdbClient.loadImage(mContext, cast.getProfile_path(), holder.image, TmdbClient.imageType.ICON, "cast");
    }

    @Override
    public int getItemCount() {
        return castList.size();
    }
}

