package com.agiledev.agiledevapp;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class TvEpisodeFullScreenDialog extends DialogFragment {
    public static String TAG = "TvEpisodeFullScreenDialog";
    public static String seriesId;
    public static int seasonNum, episodeNum;
    public FullTvEpisodeDetails tvEpisodeDetails;
    public Toolbar toolbar;
    FloatingActionButton fab;
    MaterialProgressBar progressBar;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static TvEpisodeFullScreenDialog newInstance(String id, int sNum, int eNum) {
        TvEpisodeFullScreenDialog fragment = new TvEpisodeFullScreenDialog();
        seriesId = id;
        seasonNum = sNum;
        episodeNum = eNum;
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
        View view = inflater.inflate(R.layout.tvepisode_dialog_layout, container, false);

        sharedPref = getContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        toolbar = view.findViewById(R.id.tvepisode_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        progressBar = view.findViewById(R.id.tvepisodeTrackingProgress);

        fab = view.findViewById(R.id.fabTrackTVEpisode);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trackEpisode();
            }
        });

        getTvEpisodeDetails(view);

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

    synchronized void getTvEpisodeDetails(final View view) {
        TmdbClient.getTvEpisodeDetails(seriesId, seasonNum, episodeNum, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                tvEpisodeDetails = new Gson().fromJson(response.toString(), FullTvEpisodeDetails.class);
                if (tvEpisodeDetails == null)
                    return;

                tvEpisodeDetails.setSeriesId(seriesId);

                ImageView tvStillImage = view.findViewById(R.id.tvepisode_image);
                Uri uri = Uri.parse("https://image.tmdb.org/t/p/w1280" + tvEpisodeDetails.getStill_path());

                String airDateString = "";
                if (TvEpisodeFullScreenDialog.this.isAdded()) {
                    Glide.with(getContext()).load(uri).listener(new RequestListener<Uri, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                            view.findViewById(R.id.tvepisode_spinner).setVisibility(View.GONE);
                            view.findViewById(R.id.tvepisode_content).setVisibility(View.VISIBLE);
                            fab.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            view.findViewById(R.id.tvepisode_spinner).setVisibility(View.GONE);
                            view.findViewById(R.id.tvepisode_content).setVisibility(View.VISIBLE);
                            fab.setVisibility(View.VISIBLE);
                            return false;
                        }
                    }).into(tvStillImage);

                    airDateString = getResources().getString(R.string.release_date) + " <font color='#ffffff'>" + tvEpisodeDetails.getAir_date() + "</font>";
                }

                TextView tvEpisodeName = view.findViewById(R.id.tvepisode_toolbar_title);
                TextView tvEpisodeAirDate = view.findViewById(R.id.tvepisode_airDate);
                TextView tvEpisodePlot = view.findViewById(R.id.tvepisode_plot);

                tvEpisodeName.setText(tvEpisodeDetails.getName() + " | " + seasonNum + "x" + episodeNum);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tvEpisodeAirDate.setText(Html.fromHtml(airDateString, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                } else {
                    tvEpisodeAirDate.setText(Html.fromHtml(airDateString), TextView.BufferType.SPANNABLE);
                }
                tvEpisodePlot.setText(tvEpisodeDetails.getOverview());
            }
        });
    }

    void trackEpisode() {
        if (Globals.trackedEpisodeExists(seriesId, seasonNum, episodeNum).equals(Globals.responseType.NONE))
            MediaTracking.trackTV(getContext(), getActivity(),"episode", sharedPref.getString(getString(R.string.prefs_loggedin_username), null), seriesId, seasonNum, episodeNum, progressBar);
        else
            MediaTracking.untrackTV(getContext(), getActivity(), "episode", sharedPref.getString(getString(R.string.prefs_loggedin_username), null), seriesId, seasonNum, episodeNum, progressBar, false);
    }
}
