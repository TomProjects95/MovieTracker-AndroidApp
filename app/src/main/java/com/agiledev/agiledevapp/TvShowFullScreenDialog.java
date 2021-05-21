package com.agiledev.agiledevapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import static java.lang.Math.min;

public class TvShowFullScreenDialog extends DialogFragment {

    public static String TAG = "TvShowFullScreenDialog";
    public String id, poster_path, backdrop_path;
    public FullTvShowDetails tvshowDetails;
    public Toolbar toolbar;
    public ImageView trailerVideoImage, trailerVideoPlayImage;
    NestedScrollView pageContent;
    RecyclerView recyclerView;
    RecyclerView seasonRecycler;
    TvShowCastAdapter adapter;
    MaterialProgressBar trackingProgress;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    static ProfileFragment.ReturnToProfileListener listener;

    public static TvShowFullScreenDialog newInstance(String id) {
        TvShowFullScreenDialog fragment = new TvShowFullScreenDialog();
        Bundle args = new Bundle();
        args.putString("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    public static TvShowFullScreenDialog newInstance(String id, ProfileFragment.ReturnToProfileListener returnListener) {
        TvShowFullScreenDialog fragment = new TvShowFullScreenDialog();
        Bundle args = new Bundle();
        args.putString("id", id);
        fragment.setArguments(args);
        listener = returnListener;
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
        View view = inflater.inflate(R.layout.tvshow_dialog_layout, container, false);

        pageContent = view.findViewById(R.id.tvshowContent);

        toolbar = view.findViewById(R.id.tvshowDialogTool_Bar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        Button tvshowCastMore = view.findViewById(R.id.tvshowInfoCastMore);
        tvshowCastMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewMoreCast();
            }
        });

        recyclerView = view.findViewById(R.id.tvshowcast_recycler_view);
        seasonRecycler = view.findViewById(R.id.tvshowSeasonRecycler);

        trailerVideoImage = view.findViewById(R.id.tvshowTrailerImage);
        trailerVideoPlayImage = view.findViewById(R.id.tvshowTrailerPlayIcon);

        trackingProgress = view.findViewById(R.id.tvshowTrackingProgress); //TODO: Make progress bar persist even when dialog is closed.

        sharedPref = getContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        this.id = getArguments().getString("id", "No Title Found");

        FloatingActionButton fab = view.findViewById(R.id.fabTrackTV);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trackTvShow();
            }
        });


        getTvShowDetails(view);

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

    protected synchronized void getTvShowDetails(final View view) {
        TmdbClient.getFullTvShowDetails(id, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                tvshowDetails = new Gson().fromJson(response.toString(), FullTvShowDetails.class);
                if (tvshowDetails == null)
                    return;
                Uri uri = Uri.parse("https://image.tmdb.org/t/p/w1280" + tvshowDetails.getBackdrop_path());

                poster_path = tvshowDetails.getPoster_path();
                backdrop_path = tvshowDetails.getBackdrop_path();

                String nextEpString = "", firstReleased = "";
                if (TvShowFullScreenDialog.this.isAdded()) {
                    if (tvshowDetails.getBackdrop_path() != null) {
                        Glide.with(TvShowFullScreenDialog.this).load(uri).listener(new RequestListener<Uri, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                trailerVideoPlayImage.setVisibility(View.VISIBLE);

                                final FullTvShowDetails.Video tempVideo;
                                if (tvshowDetails.getVideos() != null && tvshowDetails.getVideos().size() > 0) {
                                    tempVideo = tvshowDetails.getVideos().get(0);
                                    trailerVideoPlayImage.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            openYoutubeVideo(getContext(), tempVideo.getKey());
                                        }
                                    });
                                } else {
                                    trailerVideoPlayImage.setVisibility(View.GONE);
                                }

                                view.findViewById(R.id.tvshowLoadingSpinner).setVisibility(View.GONE);
                                view.findViewById(R.id.fabTrackTV).setVisibility(View.VISIBLE);
                                pageContent.setVisibility(View.VISIBLE);
                                return false;
                            }
                        }).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).dontAnimate().into(trailerVideoImage);
                    } else {
                        view.findViewById(R.id.tvshowLoadingSpinner).setVisibility(View.GONE);
                        view.findViewById(R.id.fabTrackTV).setVisibility(View.VISIBLE);
                        pageContent.setVisibility(View.VISIBLE);
                    }

                    firstReleased = getResources().getString(R.string.release_date) + " <font color='#ffffff'>" + tvshowDetails.getFirst_air_date() + "</font>";
                    nextEpString = tvshowDetails.getNext_episode_to_air() == null ? getResources().getString(R.string.nextep) + " <font color='#ffffff'>N/A</font>" : getResources().getString(R.string.nextep) + " <font color='#ffffff'>" + tvshowDetails.getNext_episode_to_air().air_date + "</font>";

                    addCastToLayout(tvshowDetails.getCast(), getActivity().getSupportFragmentManager());
                    addSeasonsToLayout(tvshowDetails.getSeason(), getActivity().getSupportFragmentManager());
                }

                RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 1);
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());

                mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                seasonRecycler.setLayoutManager(mLayoutManager);

                TextView tvshowName = view.findViewById(R.id.tvshowTitle);
                TextView tvshowPlot = view.findViewById(R.id.tvshowInfoPlot);
                TextView tvshowReleaseDate = view.findViewById(R.id.tvshowInfoReleaseDate);
                TextView tvshowNextEpisode = view.findViewById(R.id.tvshowNextEp);
                TextView tvshowGenres = view.findViewById(R.id.tvshowInfoGenres);

                tvshowName.setText(tvshowDetails.getName());
                tvshowPlot.setText(tvshowDetails.getOverview());

                tvshowNextEpisode.setText(tvshowDetails.getNext_episode_to_air() == null ? " " : tvshowDetails.getNext_episode_to_air().air_date);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tvshowReleaseDate.setText(Html.fromHtml(firstReleased, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                    tvshowNextEpisode.setText(Html.fromHtml(nextEpString, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                } else {
                    tvshowReleaseDate.setText(Html.fromHtml(firstReleased), TextView.BufferType.SPANNABLE);
                    tvshowNextEpisode.setText(Html.fromHtml(nextEpString), TextView.BufferType.SPANNABLE);
                }

                tvshowGenres.setText(tvshowDetails.getGenresString());
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(String.valueOf(statusCode), throwable.getMessage());
            }
        });
    }

    public void openYoutubeVideo(Context context, String id) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException e) {
            context.startActivity(webIntent);
        }
    }

    public void viewMoreCast() {
        FullCastDialog dialog = FullCastDialog.newInstance(id, FullCastDialog.mediatype.TV);
        dialog.show(getActivity().getFragmentManager(), FullCastDialog.TAG);
    }

    public void addCastToLayout(ArrayList<FullTvShowDetails.Cast> castList, FragmentManager fragmentManager) {
        Context mContext = getContext();
        List<FullTvShowDetails.Cast> top3Cast = new ArrayList<>();

        for (int i = 0; i < min(3,castList.size()); i++) {
            top3Cast.add(castList.get(i));
        }

        adapter = new TvShowCastAdapter(mContext, top3Cast, fragmentManager);
        recyclerView.setAdapter(adapter);
    }

    public void addSeasonsToLayout(ArrayList<FullTvShowDetails.season> seasonList, FragmentManager fragmentManager) {
        Context mContext = getContext();
        RecyclerView.Adapter adapter = new HorizontalAdapter(mContext, seasonList, fragmentManager, HorizontalAdapter.MediaType.SEASON, id, null);
        seasonRecycler.setAdapter(adapter);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null)
            listener.onDialogDismissed();
    }

    void trackTvShow() {
        if (Globals.trackedShowExists(id).equals(Globals.responseType.NONE))
            MediaTracking.trackTV(getContext(), getActivity(), "series", sharedPref.getString(getString(R.string.prefs_loggedin_username), null), id, null, null, trackingProgress);
        else if (Globals.trackedShowExists(id).equals(Globals.responseType.PARTIAL))
            MediaTracking.untrackTV(getContext(), getActivity(), "series", sharedPref.getString(getString(R.string.prefs_loggedin_username), null), id, null, null, trackingProgress, true);
        else
            MediaTracking.untrackTV(getContext(), getActivity(), "series", sharedPref.getString(getString(R.string.prefs_loggedin_username), null), id, null, null, trackingProgress, false);
    }
}
