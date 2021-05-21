package com.agiledev.agiledevapp;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.lang.Math.min;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static StorageReference avatarRef = FirebaseStorage.getInstance().getReference().child("avatars");

    static String username;

    boolean viewingSelf = false;

    static CircleImageView imgAvatar;
    static ProgressBar imgAvatarSpinner;

    public static HorizontalAdapter movieAdapter;
    public static HorizontalAdapter tvAdapter;

    RecyclerView movieRecycler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sharedPref = getContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        getActivity().setTitle("Profile");

        username = getArguments().getString("username");

        ((TextView)view.findViewById(R.id.profile_username)).setText(username);

        final TextView txtJoined = view.findViewById(R.id.profile_joined);
        TextView txtNoMoviesWatched = view.findViewById(R.id.profile_num_movies_watched);
        TextView txtNoTVShowsWatched = view.findViewById(R.id.profile_num_shows_watched);
        imgAvatar = view.findViewById(R.id.profile_avatar);
        final CircleImageView imgAvatarEdit = view.findViewById(R.id.profile_avatar_edit);
        imgAvatarSpinner = view.findViewById(R.id.profile_avatar_spinner);
        final TextView txtTimeWatched = view.findViewById(R.id.profile_time_watched);
        movieRecycler = view.findViewById(R.id.profile_last_movies_recycler);
        RecyclerView rcyLastShows = view.findViewById(R.id.profile_last_shows_recycler);
        final BarChart chart = view.findViewById(R.id.profile_genre_chart);

        if (username.equals(sharedPref.getString(getString(R.string.prefs_loggedin_username), null))) {
            viewingSelf = true;
            imgAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeAvatar();
                }
            });
            imgAvatarEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeAvatar();
                }
            });
        }

        db.collection("UserDetails").document(username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                DateFormat sdf = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
                                String joinDate = sdf.format(((Timestamp)doc.get("join_date")).toDate());
                                txtJoined.setText(joinDate);

                                avatarRef.child(username + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Glide.with(getActivity()).load(uri).placeholder(R.drawable.placeholder_med_cast).dontAnimate().listener(new RequestListener<Uri, GlideDrawable>() {
                                            @Override
                                            public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                                imgAvatarSpinner.setVisibility(View.GONE);
                                                Toast.makeText(getContext(), "Profile image failed to load!", Toast.LENGTH_SHORT).show();
                                                return false;
                                            }
                                            @Override
                                            public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                                if (viewingSelf) {
                                                    imgAvatarEdit.setVisibility(View.VISIBLE);
                                                    imgAvatarSpinner.setVisibility(View.GONE);
                                                }
                                                return false;
                                            }
                                        }).into(imgAvatar);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("Error", "-- Image Not Found --");
                                        if (viewingSelf)
                                            imgAvatarEdit.setVisibility(View.VISIBLE);
                                        imgAvatarSpinner.setVisibility(View.GONE);
                                    }
                                });
                                long minsWatched = ((Number)doc.get("timeWatched")).longValue();

                                long days = TimeUnit.MINUTES.toDays(minsWatched);
                                minsWatched -= TimeUnit.DAYS.toMinutes(days);

                                long hours = TimeUnit.MINUTES.toHours(minsWatched);
                                minsWatched -= TimeUnit.HOURS.toMinutes(hours);

                                long minutes = TimeUnit.MINUTES.toMinutes(minsWatched);

                                String timeWatchedString = days + " Days | " + hours + " Hours | " + minutes + " Minutes";
                                txtTimeWatched.setText(timeWatchedString);

                                Map<String, Long> genresWatched = (HashMap<String, Long>)doc.get("genresWatched");
                                List<Map.Entry<String, Long>> results = new ArrayList<>(genresWatched.entrySet());
                                Collections.sort(results, new Comparator<Map.Entry<String, Long>>() {
                                    @Override
                                    public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                                        if (o1.getValue() < o2.getValue())
                                            return 1;
                                        else if (o1.getValue() > o2.getValue())
                                            return -1;
                                        return 0;
                                    }
                                });
                                results = results.subList(0, min(5, results.size()));
                                final ArrayList<String> labels = new ArrayList<>();
                                ArrayList<BarEntry> entries = new ArrayList<>();

                                int i = 0;
                                for (Map.Entry<String, Long> e : results) {
                                    labels.add(e.getKey());
                                    BarEntry entry = new BarEntry(i, e.getValue().intValue());
                                    entries.add(entry);
                                    i++;
                                }

                                BarDataSet barDataSet = new BarDataSet(entries, "Genres");
                                barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                                barDataSet.setValueTextSize(15f);
                                BarData data = new BarData(barDataSet);
                                data.setBarWidth(0.9f);
                                data.setValueFormatter(new IValueFormatter() {
                                    @Override
                                    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                                        return String.valueOf(value).substring(0, String.valueOf(value).indexOf('.'));
                                    }
                                });
                                chart.setData(data);
                                chart.setFitBars(true);
                                chart.setDrawValueAboveBar(false);
                                chart.getAxisLeft().setAxisMinimum(0);
                                chart.getAxisLeft().setDrawGridLines(false);
                                chart.getAxisLeft().setDrawLabels(false);
                                chart.getAxisRight().setDrawGridLines(false);
                                chart.getAxisRight().setDrawLabels(false);
                                chart.getXAxis().setDrawGridLines(false);
                                Description desc = new Description();
                                desc.setEnabled(false);
                                chart.setDescription(desc);
                                chart.getLegend().setEnabled(false);
                                XAxis xAxis = chart.getXAxis();
                                xAxis.setGranularity(1f);
                                xAxis.setTextColor(getResources().getColor(R.color.colorPrimary));
                                xAxis.setValueFormatter(new IAxisValueFormatter() {
                                    @Override
                                    public String getFormattedValue(float value, AxisBase axis) {
                                        if (value >= 0) {
                                            if (value <= labels.size() - 1) {
                                                return labels.get((int)value);
                                            }
                                        }
                                        return "";
                                    }
                                });
                                chart.invalidate();
                            } else {
                                Log.e("Profile", "Document not found");
                            }
                        } else {
                            Log.e("Profile", task.getException().getMessage());
                        }
                    }
                });

        int noMovies = Globals.getTrackedMovies().size();
        int noShows = Globals.getTrackedTvShows().size();
        txtNoMoviesWatched.setText(String.valueOf(noMovies));
        txtNoTVShowsWatched.setText(String.valueOf(noShows));

        populateLastWatched(movieRecycler, rcyLastShows, view);

        TextView viewMoreMovies = view.findViewById(R.id.profile_view_more_movies);
        viewMoreMovies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogProfileViewMore dialog = DialogProfileViewMore.newInstance(DialogProfileViewMore.mediatype.MOVIE);
                dialog.show(getActivity().getSupportFragmentManager(), DialogProfileViewMore.TAG);
            }
        });
        TextView viewMoreTV = view.findViewById(R.id.profile_view_more_tv);
        viewMoreTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Show more tracked movies.
            }
        });

        return view;
    }

    void populateLastWatched(RecyclerView movieRecycler, RecyclerView tvRecycler, View view) {
        List<Globals.trackedMovie> lastMovies = Globals.getTrackedMovies().subList(0, min(10, Globals.getTrackedMovies().size()));
        List<Globals.trackedTV> lastShows = Globals.getTrackedTvShows().subList(0, min(10, Globals.getTrackedTvShows().size()));

        TextView viewMoreMovies = view.findViewById(R.id.profile_view_more_movies);
        TextView viewMoreTV = view.findViewById(R.id.profile_view_more_tv);

        if (Globals.getTrackedMovies().size() <= 10)
            viewMoreMovies.setEnabled(false);
        if (Globals.getTrackedTvShows().size() <= 10)
            viewMoreTV.setEnabled(false);

        RecyclerView.LayoutManager movieLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        movieRecycler.setLayoutManager(movieLayoutManager);
        RecyclerView.LayoutManager tvLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        tvRecycler.setLayoutManager(tvLayoutManager);

        movieAdapter = new HorizontalAdapter(getContext(), lastMovies, getActivity().getSupportFragmentManager(), HorizontalAdapter.MediaType.MOVIE, null, this);
        movieRecycler.setAdapter(movieAdapter);
        tvAdapter = new HorizontalAdapter(getContext(), lastShows, getActivity().getSupportFragmentManager(), HorizontalAdapter.MediaType.TV, null, this);
        tvRecycler.setAdapter(tvAdapter);
    }

    interface ReturnToProfileListener {
        void onDialogDismissed();
    }

    public void openMovieDialog(String id) {
        final ProfileFragment self = this;
        MovieFullScreenDialog dialog = MovieFullScreenDialog.newInstance(id, new ReturnToProfileListener() {
            @Override
            public void onDialogDismissed() {
                getActivity().getSupportFragmentManager().beginTransaction().detach(self).attach(self).commit();
            }
        });
        dialog.show(getActivity().getSupportFragmentManager(), MovieFullScreenDialog.TAG);
    }
    public void openTVDialog(String id) {
        final ProfileFragment self = this;
        TvShowFullScreenDialog dialog = TvShowFullScreenDialog.newInstance(id, new ReturnToProfileListener() {
            @Override
            public void onDialogDismissed() {
                getActivity().getSupportFragmentManager().beginTransaction().detach(self).attach(self).commit();
            }
        });
        dialog.show(getActivity().getSupportFragmentManager(), MovieFullScreenDialog.TAG);
    }

    void changeAvatar() {
        ProfileAvatarChangeDialog dialog = ProfileAvatarChangeDialog.newInstance(username);
        dialog.show(getActivity().getSupportFragmentManager(), ProfileAvatarChangeDialog.TAG);

        getActivity().getSupportFragmentManager().executePendingTransactions();
        dialog.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                updateAvatar();
            }
        });
    }

    void updateAvatar() {
        imgAvatarSpinner.setVisibility(View.VISIBLE);
        avatarRef.child(username + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getActivity()).load(uri).placeholder(R.drawable.placeholder_med_cast).dontAnimate().listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                        imgAvatarSpinner.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Profile image failed to load!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        imgAvatarSpinner.setVisibility(View.GONE);
                        return false;
                    }
                }).into(imgAvatar);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Error", "-- Image Not Found --");
            }
        });
    }
}
