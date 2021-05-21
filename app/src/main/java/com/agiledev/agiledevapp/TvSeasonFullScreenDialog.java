package com.agiledev.agiledevapp;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class TvSeasonFullScreenDialog extends DialogFragment {
    public static String TAG = "TvSeasonFullScreenDialog";
    public static String seriesId;
    public static int seasonNum;
    public FullTvSeasonDetails tvSeasonDetails;
    public Toolbar toolbar;
    RecyclerView episodeRecycler;
    FloatingActionButton fab;
    MaterialProgressBar progressBar;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static TvSeasonFullScreenDialog newInstance(String id, int num) {
        TvSeasonFullScreenDialog fragment = new TvSeasonFullScreenDialog();
        seriesId = id;
        seasonNum = num;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.tvseason_dialog_layout, container, false);

        sharedPref = getContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        toolbar = view.findViewById(R.id.tvseason_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        episodeRecycler = view.findViewById(R.id.tvseason_episodes);

        progressBar = view.findViewById(R.id.tvseasonTrackingProgress);

        fab = view.findViewById(R.id.fabTrackTVSeason);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trackTV();
            }
        });

        getTvSeasonDetails(view);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    synchronized void getTvSeasonDetails(final View view) {
        TmdbClient.getTvSeasonDetails(seriesId, seasonNum, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                tvSeasonDetails = new Gson().fromJson(response.toString(), FullTvSeasonDetails.class);
                if (tvSeasonDetails == null)
                    return;

                final ImageView backdropImage = view.findViewById(R.id.tvseason_image);

                TmdbClient.getFullTvShowDetails(seriesId, null, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        FullTvShowDetails tvShow = new Gson().fromJson(response.toString(), FullTvShowDetails.class);
                        if (tvShow == null)
                            return;


                        TextView tvSeasonName = view.findViewById(R.id.tvseason_toolbar_title);
                        tvSeasonName.setText(tvSeasonDetails.getName() + " | " + tvShow.getName());

                        Uri uri = Uri.parse("https://image.tmdb.org/t/p/w1280" + tvShow.getBackdrop_path());
                        if (TvSeasonFullScreenDialog.this.isAdded()) {
                            Glide.with(getContext()).load(uri).listener(new RequestListener<Uri, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    backdropImage.setVisibility(View.VISIBLE);

                                    view.findViewById(R.id.tvseason_spinner).setVisibility(View.GONE);
                                    view.findViewById(R.id.tvseason_content).setVisibility(View.VISIBLE);
                                    fab.setVisibility(View.VISIBLE);
                                    return false;
                                }
                            }).into(backdropImage);
                        }
                    }
                });

                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                episodeRecycler.setLayoutManager(mLayoutManager);

                TextView tvSeasonFirstAir = view.findViewById(R.id.tvseason_firstAirDate);
                TextView tvSeasonNumEps = view.findViewById(R.id.tvseason_numOfEpisodes);
                TextView tvSeasonPlot = view.findViewById(R.id.tvseason_plot);

                String firstAirString = getResources().getString(R.string.first_released) + " <font color='#ffffff'>" + tvSeasonDetails.getAir_date() + "</font>";
                String numOfEpsString = getResources().getString(R.string.no_of_episodes) + " <font color='#ffffff'>" + tvSeasonDetails.getEpisodes().size() + "</font>";
                tvSeasonPlot.setText(tvSeasonDetails.getOverview() != null && !tvSeasonDetails.getOverview().trim().equals("") ? tvSeasonDetails.getOverview() : "No plot information found!");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tvSeasonFirstAir.setText(Html.fromHtml(firstAirString, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                    tvSeasonNumEps.setText(Html.fromHtml(numOfEpsString, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                } else {
                    tvSeasonFirstAir.setText(Html.fromHtml(firstAirString), TextView.BufferType.SPANNABLE);
                    tvSeasonNumEps.setText(Html.fromHtml(numOfEpsString), TextView.BufferType.SPANNABLE);
                }

                addEpisodesToLayout(tvSeasonDetails.getEpisodes(), getActivity().getSupportFragmentManager());
            }
        });
    }

    void addEpisodesToLayout(ArrayList<FullTvSeasonDetails.Episode> episodeList, FragmentManager fragmentManager) {
        Context mContext = getContext();
        RecyclerView.Adapter adapter = new EpisodeCardAdapter(mContext, episodeList, fragmentManager);
        episodeRecycler.setAdapter(adapter);
    }

    public void trackTV() {
        Globals.responseType response;
        response = Globals.trackedSeasonExists(seriesId, seasonNum);
        if (response == Globals.responseType.NONE) {
            MediaTracking.trackTV(getContext(), getActivity(), "season", sharedPref.getString(getString(R.string.prefs_loggedin_username), null), seriesId, seasonNum, null, progressBar);
        } else if (response == Globals.responseType.FULL) {
            MediaTracking.untrackTV(getContext(), getActivity(), "season", sharedPref.getString(getString(R.string.prefs_loggedin_username), null), seriesId, seasonNum, null, progressBar, false);
        } else {
            MediaTracking.untrackTV(getContext(), getActivity(), "season", sharedPref.getString(getString(R.string.prefs_loggedin_username), null), seriesId, seasonNum, null, progressBar, true);
        }
    }
}
