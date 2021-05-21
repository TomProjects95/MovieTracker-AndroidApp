package com.agiledev.agiledevapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
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

public class MovieFragment extends Fragment implements MainActivity.PermissionCallback {

    View view;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    private FusedLocationProviderClient fusedLocationClient;
    String countryCode = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_movies, container, false);

        getActivity().setTitle(getString(R.string.movies_name));
        sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        populateRecentMovies();
        populateRecommendedForUser();
        permsCheck();

        final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.moviesRefreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame
                                , new MovieFragment())
                        .commit();
            }
        });
        return view;
    }

    public synchronized void populateRecentMovies() {
        List<Globals.trackedMovie> recentMovies = Globals.getTrackedMovies();
        List<Globals.trackedMovie> nineRecentMovies = new ArrayList<>(recentMovies.subList(0, min(recentMovies.size(), 9)));

        RecyclerView recyclerView = view.findViewById(R.id.moviesHomeRecentlyWatchedRecycler);

        RecentMoviesAdapter adapter = new RecentMoviesAdapter(getActivity(), nineRecentMovies, getActivity().getSupportFragmentManager());

        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setVisibility(View.VISIBLE);
    }

    private synchronized void populateRecommendedForUser() {
        if (Globals.getTrackedMovies().size() <= 0)
            return;
        List<Globals.trackedMovie> trackedMovies = Globals.getTrackedMovies();
        trackedMovies = new ArrayList<>(trackedMovies.subList(0, min(trackedMovies.size(), 9)));
        final Globals.trackedMovie randomMovie = trackedMovies.get(new Random().nextInt(trackedMovies.size()));

        TextView title = view.findViewById(R.id.moviesHomeRecommendedTitle);
        String recTitle = "Recommended because you watched: <font color='#ec2734'>" + randomMovie.name + "</font>";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            title.setText(Html.fromHtml(recTitle, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
        } else {
            title.setText(Html.fromHtml(recTitle), TextView.BufferType.SPANNABLE);
        }

        TmdbClient.getRelatedMovies(randomMovie.id, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray results = new JSONArray();
                try {
                    results = response.getJSONArray("results");
                } catch (JSONException e) {
                    Log.e("JSON Error", e.getMessage());
                    e.printStackTrace();
                }

                List<Globals.trackedMovie> bmd = new ArrayList<>();
                for (int i = 0; i < results.length(); i++) {
                    try {
                        BasicMovieDetails movie = new Gson().fromJson(results.getJSONObject(i).toString(), BasicMovieDetails.class);
                        Globals.trackedMovie m = new Globals.trackedMovie();
                        if (movie.getId().equals(randomMovie.id))
                            continue;
                        m.id = movie.getId();
                        m.poster_path = movie.getPoster_path();
                        m.name = movie.getTitle();
                        bmd.add(m);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                bmd = new ArrayList<>(bmd.subList(0, min(bmd.size(), 9)));
                RecyclerView recyclerView = view.findViewById(R.id.moviesHomeRecommendedRecycler);

                RecentMoviesAdapter adapter = new RecentMoviesAdapter(getActivity(), bmd, getActivity().getSupportFragmentManager());

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

    public void populateRecommendedInArea() {
        TmdbClient.getPopularMoviesInRegion(countryCode, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray results = new JSONArray();
                try {
                    results = response.getJSONArray("results");
                } catch (JSONException e) {
                    Log.e("JSON Error", e.getMessage());
                    e.printStackTrace();
                }

                List<Globals.trackedMovie> bmd = new ArrayList<>();
                for (int i = 0; i < results.length(); i++) {
                    try {
                        BasicMovieDetails movie = new Gson().fromJson(results.getJSONObject(i).toString(), BasicMovieDetails.class);
                        Globals.trackedMovie m = new Globals.trackedMovie();
                        m.id = movie.getId();
                        m.poster_path = movie.getPoster_path();
                        m.name = movie.getTitle();
                        bmd.add(m);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                bmd = new ArrayList<>(bmd.subList(0, min(bmd.size(), 9)));
                RecyclerView recyclerView = view.findViewById(R.id.moviesRegionRecommendedRecycler);

                RecentMoviesAdapter adapter = new RecentMoviesAdapter(getActivity(), bmd, getActivity().getSupportFragmentManager());

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
        Toast.makeText(getContext(), "Locale", Toast.LENGTH_LONG).show();
        return Locale.getDefault().getCountry();
    }
}
