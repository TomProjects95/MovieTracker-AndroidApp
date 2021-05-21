package com.agiledev.agiledevapp;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class FullCastDialog extends DialogFragment {

    public static String TAG = "FullCastDialog";
    public static String id;
    public Toolbar toolbar;
    RelativeLayout pageContent;
    MovieCredits credits;
    RecyclerView recyclerView;
    FullCastAdapter adapter;
    public static mediatype mediaT;

    ArrayList<MovieCredits.Cast> castList;


    public static enum mediatype {
        MOVIE,
        TV
    }


    public static FullCastDialog newInstance(String movieID, mediatype mt) {
        FullCastDialog fragment = new FullCastDialog();
        Bundle args = new Bundle();
        id = movieID;
        mediaT = mt;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_DayNight_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.full_cast_dialog, container, false);

        recyclerView = view.findViewById(R.id.full_cast_recycler_view);
        toolbar = view.findViewById(R.id.full_castDialogTool_Bar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        ((TextView)view.findViewById(R.id.castName)).setText("Cast");

        getFullCast(view);
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

    protected synchronized void getFullCast(View view){
        if (mediaT == mediatype.MOVIE) {
            TmdbClient.getMovieCast(id, null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    credits = new Gson().fromJson(response.toString(), MovieCredits.class);
                    if (credits == null)
                        return;
                    if (FullCastDialog.this.isAdded())
                        addCastToLayout(credits.getCast(), ((FragmentActivity) getActivity()).getSupportFragmentManager());
                }
            });
        }
        else if (mediaT == mediatype.TV) {
            TmdbClient.getTvCast(id, null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    credits = new Gson().fromJson(response.toString(), MovieCredits.class);
                    if (credits == null)
                        return;
                    if (FullCastDialog.this.isAdded())
                        addCastToLayout(credits.getCast(), ((FragmentActivity) getActivity()).getSupportFragmentManager());
                }
            });
        }
    }

    public void addCastToLayout(ArrayList<MovieCredits.Cast> castList, FragmentManager fragmentManager) {
        Context mContext = getActivity();

        adapter = new FullCastAdapter(mContext, castList, fragmentManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayout.VERTICAL, false));
    }
}
