package com.agiledev.agiledevapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import java.util.List;

public class SwipeToTrackCallback extends ItemTouchHelper.SimpleCallback {

    private SearchResultsAdapter mAdapter;
    private Context mContext;

    private Drawable icon;
    private ColorDrawable background;

    private boolean tracked = false;
    private List mediaList;
    private MediaTracking.Media mediaType;
    private String username;

    public SwipeToTrackCallback(SearchResultsAdapter adapter, Context context, List mediaList, MediaTracking.Media mediaType, String username) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.mAdapter = adapter;
        this.mContext = context;
        this.mediaList = mediaList;
        this.mediaType = mediaType;
        this.username = username;
        icon = ContextCompat.getDrawable(mContext, R.drawable.ic_track_white);
        background = new ColorDrawable(Color.GRAY);
    }



    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        View itemView = viewHolder.itemView;

        int position = viewHolder.getAdapterPosition();
        if (mediaType == MediaTracking.Media.MOVIE) {
            tracked = Globals.trackedMoviesContains(((BasicMovieDetails)mediaList.get(position)).getId());
            if (tracked)
                background = new ColorDrawable(Color.RED);
            else
                background = new ColorDrawable(Color.GREEN);
        } else if (mediaType == MediaTracking.Media.TV) {
            tracked = Globals.basicTvShowExists(((BasicTvShowDetails)mediaList.get(position)).getId());
            if (tracked)
                background = new ColorDrawable(Color.RED);
            else
                background = new ColorDrawable(Color.GREEN);
        }

        int backgroundCornerOffset = 20;

        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        if (dX > 0) { //Swiping right
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
        } else if (dX < 0) { //Swiping left
            int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else { //View is unswiped
            background.setBounds(0, 0, 0, 0);
        }
        background.draw(c);
        icon.draw(c);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        mAdapter.trackItem(!tracked, mediaType, username);
        mAdapter.notifyItemChanged(position);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }
}
