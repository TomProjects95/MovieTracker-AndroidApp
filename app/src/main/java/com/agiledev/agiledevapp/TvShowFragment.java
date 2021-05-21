package com.agiledev.agiledevapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import cz.msebera.android.httpclient.Header;

import static com.agiledev.agiledevapp.MainActivity.PERMS_LOCATION;
import static com.agiledev.agiledevapp.MainActivity.locationBool;
import static java.lang.Math.min;

/**
 * Created by s6104158 on 07/02/19.
 */

public class TvShowFragment extends Fragment implements MainActivity.PermissionCallback {

    View view;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    private FusedLocationProviderClient fusedLocationClient;
    String countryCode = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tvshow, container, false);
        getActivity().setTitle(R.string.tvshows_name);
        sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        populateRecentTvShows();
        populateRecommendedForUser();
        permsCheck();

        final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.tvShowsRefreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame
                                ,new TvShowFragment())
                        .commit();
            }
        });
        return view;
    }

    public void populateRecentTvShows() {
        List<Globals.trackedTV> recentTvShows = Globals.getTrackedTvShows();
        List<Globals.trackedTV> nineRecentTvShows = new ArrayList<>(recentTvShows.subList(0, min(recentTvShows.size(), 9)));

        RecyclerView recyclerView = view.findViewById(R.id.tvShowsHomeRecentlyWatchedRecycler);

        RecentTvShowsAdapter adapter = new RecentTvShowsAdapter(getActivity(), nineRecentTvShows, getActivity().getSupportFragmentManager());

        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setVisibility(View.VISIBLE);
    }

    private void populateRecommendedForUser() {
        if (Globals.getTrackedTvShows().size() <= 0)
            return;
        List<Globals.trackedTV> trackedTVList = Globals.getTrackedTvShows();
        trackedTVList = new ArrayList<>(trackedTVList.subList(0, min(trackedTVList.size(), 9)));
        final Globals.trackedTV randomTv = trackedTVList.get(new Random().nextInt(trackedTVList.size()));

        TextView title = view.findViewById(R.id.tvShowsHomeRecommendedTitle);
        String recTitle = "Recommended because you watched: <font color='#ec2734'>" + randomTv.name + "</font>";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            title.setText(Html.fromHtml(recTitle, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
        } else {
            title.setText(Html.fromHtml(recTitle), TextView.BufferType.SPANNABLE);
        }

        TmdbClient.getRelatedTV(randomTv.id, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray results = new JSONArray();
                try {
                    results = response.getJSONArray("results");
                } catch (JSONException e) {
                    Log.e("JSON Error", e.getMessage());
                }

                List<Globals.trackedTV> bmd = new ArrayList<>();
                for (int i = 0; i < results.length(); i++) {
                    try {
                        BasicTvShowDetails tv = new Gson().fromJson(results.getJSONObject(i).toString(), BasicTvShowDetails.class);
                        Globals.trackedTV t = new Globals.trackedTV();
                        if (tv.getId().equals(randomTv.id))
                            continue;
                        t.id = tv.getId();
                        t.poster_path = tv.getPoster_path();
                        t.name = tv.getName();
                        bmd.add(t);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                bmd = new ArrayList<>(bmd.subList(0, min(bmd.size(), 9)));
                RecyclerView recyclerView = view.findViewById(R.id.tvShowsHomeRecommendedRecycler);

                RecentTvShowsAdapter adapter = new RecentTvShowsAdapter(getActivity(), bmd, getActivity().getSupportFragmentManager());
                recyclerView.setAdapter(adapter);
                RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 3);
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private synchronized void permsCheck() {
        if ((locationBool == null || !locationBool) && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMS_LOCATION);
            ((MainActivity) getActivity()).setCallback(this);
        } else {
            locationBool = true;
        }

        if (locationBool != null && !locationBool) {
            countryCode = getCountryCodeFromLocale();
            populateRecommendedInArea();
        } else if (locationBool != null && locationBool) {
            getCountryCodeFromGPS();
        }
    }

    private void populateRecommendedInArea() {
        TmdbClient.getPopularTvInRegion(countryCode, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray results = new JSONArray();
                try {
                    results = response.getJSONArray("results");
                } catch (JSONException e) {
                    Log.e("JSON Error", e.getMessage());
                    e.printStackTrace();
                }

                List<Globals.trackedTV> bmd = new ArrayList<>();
                for (int i = 0; i < results.length(); i++) {
                    try {
                        BasicTvShowDetails tv = new Gson().fromJson(results.getJSONObject(i).toString(), BasicTvShowDetails.class);
                        Globals.trackedTV t = new Globals.trackedTV();
                        t.id = tv.getId();
                        t.poster_path = tv.getPoster_path();
                        t.name = tv.getName();
                        bmd.add(t);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                bmd = new ArrayList<>(bmd.subList(0, min(bmd.size(), 9)));

                RecyclerView recyclerView = view.findViewById(R.id.tvShowsRegionRecommendedRecycler);
                RecentTvShowsAdapter adapter = new RecentTvShowsAdapter(getActivity(), bmd, getActivity().getSupportFragmentManager());
                recyclerView.setAdapter(adapter);
                RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 3);
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onPermissionGranted() {
        getCountryCodeFromGPS();
    }

    @Override
    public void onPermissionDenied() {
        countryCode = getCountryCodeFromLocale();
        populateRecommendedInArea();
    }

    @SuppressLint("MissingPermission")
    private void getCountryCodeFromGPS() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                final Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                if (location != null) {
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        Address obj = addresses.get(0);
                        countryCode = obj.getCountryCode();
                        populateRecommendedInArea();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    LocationRequest locationRequest = LocationRequest.create();
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    locationRequest.setInterval(20 * 1000);
                    final LocationCallback locationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult == null) {
                                countryCode = getCountryCodeFromLocale();
                                populateRecommendedInArea();
                                return;
                            }
                            try {
                                List<Address> addresses = geocoder.getFromLocation(locationResult.getLocations().get(0).getLatitude(), locationResult.getLocations().get(0).getLongitude(), 1);
                                Address obj = addresses.get(0);
                                countryCode = obj.getCountryCode();
                                populateRecommendedInArea();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                }
            }
        }).addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.err.println(e);
            }
        });
    }

    private String getCountryCodeFromLocale() {
        return Locale.getDefault().getCountry();
    }
}