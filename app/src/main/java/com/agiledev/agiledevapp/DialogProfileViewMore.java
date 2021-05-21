package com.agiledev.agiledevapp;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by t7097354 on 04/04/19.
 */

public class DialogProfileViewMore extends DialogFragment {

    public static String TAG = "DialogProfileViewMoreDialog";
    TextView totalCount;
    RecyclerView viewMoreRecycler;
    Toolbar toolbar;
    static mediatype mediaType;

    public static enum mediatype {
        MOVIE,
        TV
    }

    public static DialogProfileViewMore newInstance(mediatype mt) {
        DialogProfileViewMore fragment = new DialogProfileViewMore();
        Bundle args = new Bundle();
        mediaType = mt;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_DayNight_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialogfrag_profile_more, container, false);

        viewMoreRecycler = view.findViewById(R.id.profile_view_more_recycler);
        toolbar = view.findViewById(R.id.profile_view_more_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        if (mediaType == mediatype.MOVIE) {
            toolbar.setTitle("Tracked Movies");
        } else if (mediaType == mediatype.TV) {
            toolbar.setTitle("Tracked TV Shows");
        }

        splitAllTrackedByDate(view);

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

    void splitAllTrackedByDate(View view) {
        if (mediaType == mediatype.MOVIE) {
            Map<Date, List<Globals.trackedMovie>> subLists = new HashMap<>();
            for (Globals.trackedMovie m : Globals.getTrackedMovies()) {
                List<Globals.trackedMovie> temp = subLists.get(m.date);
                if (temp == null) {
                    temp = new ArrayList<>();
                    subLists.put(m.date, temp);
                }
                temp.add(m);
            }

            populateRecyclerWithMovies(subLists);
        } else if (mediaType == mediatype.TV) {
            Map<Date, List<Globals.trackedTV>> subLists = new HashMap<>();
            for (Globals.trackedTV t : Globals.getTrackedTvShows()) {
                List<Globals.trackedTV> temp = subLists.get(t.date);
                if (temp == null) {
                    temp = new ArrayList<>();
                    subLists.put(t.date, temp);
                }
                temp.add(t);
            }
            populateRecyclerWithShows(subLists);
        }
    }

    void populateRecyclerWithMovies(Map<Date, List<Globals.trackedMovie>> lists) {

    }
    void populateRecyclerWithShows(Map<Date, List<Globals.trackedTV>> lists) {

    }
}
