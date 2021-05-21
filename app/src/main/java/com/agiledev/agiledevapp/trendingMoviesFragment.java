package com.agiledev.agiledevapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class trendingMoviesFragment extends Fragment
{
    ProgressBar spinner;
    RecyclerView recyclerView;
    TrendingMoviesAdapter adapter;
    View v;
    LinearLayout trendingMovieResults;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        v = inflater.inflate(R.layout.fragment_trendingmovies, container, false);

        recyclerView = v.findViewById(R.id.movietrending_recycler_view);
        trendingMovieResults = v.findViewById(R.id.movietrendingresults);

        getTrendingMovies();
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        return v;
    }

    private void getTrendingMovies()
    {
        adapter = new TrendingMoviesAdapter(getContext(), Globals.getTrendingMovies(), getFragmentManager());
        recyclerView.setAdapter(adapter);

    }
}
