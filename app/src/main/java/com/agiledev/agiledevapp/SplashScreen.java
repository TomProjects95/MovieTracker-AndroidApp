package com.agiledev.agiledevapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

/**
 * Created by t7037453 on 26/02/19.
 */

public class SplashScreen extends Activity {

    private final int SPLASH_DISPLAY_LENGTH = 3000;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_screen);

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        TmdbClient.key = getResources().getString(R.string.tmdb_api_key);
        populateTrendingMovies();
        populateMovieGenreTags();
        populateTrendingTvShows();
        populateTvGenreTags();
        if (sharedPref.getBoolean(getString(R.string.prefs_loggedin_boolean), false)) {
            db.collection("UserDetails").document(sharedPref.getString(getString(R.string.prefs_loggedin_username), null))
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            getRecentMovies();
                            getRecentTvShows();
                        } else {
                            editor.putBoolean(getString(R.string.prefs_loggedin_boolean), false).apply();
                        }
                    }
                }
            });
        }

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent = new Intent(getBaseContext(), LoginRegisterActivity.class);
                SplashScreen.this.startActivity(mainIntent);
                SplashScreen.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    public synchronized void populateMovieGenreTags() {
        TmdbClient.getMovieGenres(null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray results = new JSONArray();
                try {
                    results = response.getJSONArray("genres");
                } catch (JSONException e) {
                    Log.e("JSON Error", e.getMessage());
                    e.printStackTrace();
                }
                SparseArray<String> genres = new SparseArray<>();
                for (int i = 0; i < results.length(); i++) {
                    try {
                        JSONObject genre = results.getJSONObject(i);
                        genres.put(genre.getInt("id"), genre.getString("name"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Globals.setMovieGenreTags(genres);
            }
        });
    }
    public synchronized void populateTvGenreTags() {
        TmdbClient.getTvGenres(null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray results = new JSONArray();
                try {
                    results = response.getJSONArray("genres");
                } catch (JSONException e) {
                    Log.e("JSON Error", e.getMessage());
                    e.printStackTrace();
                }
                SparseArray<String> genres = new SparseArray<>();
                for (int i = 0; i < results.length(); i++) {
                    try {
                        JSONObject genre = results.getJSONObject(i);
                        genres.put(genre.getInt("id"), genre.getString("name"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Globals.setTvGenreTags(genres);
            }
        });
    }

    public void getRecentMovies() {
        final ArrayList<Globals.trackedMovie> movieList = new ArrayList<>();
        db.collection("TrackedMovies").document(sharedPref.getString(getString(R.string.prefs_loggedin_username), null)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Map<String, Object> movies = doc.getData();
                        for (Map.Entry<String, Object> entry : movies.entrySet()) {
                            Globals.trackedMovie movie = new Globals.trackedMovie();
                            movie.id = entry.getKey();
                            Map<String, Object> field = (Map)entry.getValue();
                            Timestamp timestamp = (Timestamp)field.get("date");
                            movie.date = timestamp.toDate();
                            movie.name = (String)field.get("name");
                            movie.poster_path = (String)field.get("poster_path");
                            HashMap<String, String> genreMap = (HashMap)field.get("genres");
                            if (genreMap != null) {
                                for (HashMap.Entry<String, String> e : genreMap.entrySet()) {
                                    movie.genres.put(Integer.parseInt(e.getKey()), e.getValue());
                                }
                            }
                            movieList.add(movie);
                        }
                        Globals.setTrackedMovies(movieList);
                        Collections.sort(Globals.getTrackedMovies());
                    }
                }
            }
        });
    }

    public void getRecentTvShows() {
        final ArrayList<Globals.trackedTV> tvList = new ArrayList<>();
        db.collection("TrackedTV").document(sharedPref.getString(getString(R.string.prefs_loggedin_username), null)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Map<String, Object> tvshows = doc.getData();
                        for (Map.Entry<String, Object> entry : tvshows.entrySet()) {
                            Globals.trackedTV tv = new Globals.trackedTV();
                            tv.id = entry.getKey();
                            Map<String, Object> field = (HashMap<String, Object>)entry.getValue();
                            tv.name = field.get("name").toString();
                            Timestamp timestamp = (Timestamp)field.get("lastWatched");
                            tv.date = timestamp.toDate();
                            tv.poster_path = (String)field.get("poster_path");
                            SerializableSparseArray<String> genres = new SerializableSparseArray<>();
                            for (Map.Entry<String, String> gEntry : ((HashMap<String, String>)field.get("genres")).entrySet()) {
                                genres.put(Integer.parseInt(gEntry.getKey()), gEntry.getValue());
                            }
                            tv.genres = genres;
                            tv.totalSeasons = ((Long)field.get("totalSeasons")).intValue();
                            ArrayList<Globals.trackedTV.Season> seasons = new ArrayList<>();
                            for (Map.Entry<String, Object> e : field.entrySet()) {
                                if (e.getKey().contains("Season ")) {
                                    Globals.trackedTV.Season season = new Globals.trackedTV.Season();
                                    ArrayList<Globals.trackedTV.Episode> trackedEpisodes = new ArrayList<>();
                                    for (Map.Entry<String, Object> eEntry : ((HashMap<String, Object>)e.getValue()).entrySet()) {
                                        if (eEntry.getKey().contains("Episode ")) {
                                            Globals.trackedTV.Episode ep = new Globals.trackedTV.Episode();
                                            Timestamp ts = (Timestamp) ((HashMap)eEntry.getValue()).get("date");
                                            ep.date = ts.toDate();
                                            ep.episodeName = (String) ((HashMap)eEntry.getValue()).get("episodeName");
                                            ep.id = (String) ((HashMap)eEntry.getValue()).get("id");
                                            ep.seriesName = (String) ((HashMap)eEntry.getValue()).get("seriesName");
                                            ep.episodeNum = ((Long) ((HashMap)eEntry.getValue()).get("episodeNum")).intValue();
                                            ep.seasonNum = Integer.parseInt(e.getKey().replace("Season ", ""));
                                            trackedEpisodes.add(ep);
                                        } else {
                                            season.totalEpisodes = ((Long) eEntry.getValue()).intValue();
                                        }
                                    }
                                    season.trackedEpisodes = trackedEpisodes;
                                    season.seasonNum = Integer.parseInt(e.getKey().replace("Season ", ""));
                                    seasons.add(season);
                                }
                            }
                            tv.trackedSeasons = seasons;
                            tvList.add(tv);
                        }
                        Globals.setTrackedTvShows(tvList);
                    }
                }
            }
        });
    }

    private synchronized void populateTrendingMovies() {
        TmdbClient.getweektrendingmovies(null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray results;
                try {
                    results = response.getJSONArray("results");

                    for (int i = 0; i < 16; i++) {
                        try {
                            Globals.trendingMovie trendingMovie = new Globals.trendingMovie();
                            BasicMovieDetails movie = new Gson().fromJson(results.get(i).toString(), BasicMovieDetails.class);


                            trendingMovie.id = movie.getId();
                            trendingMovie.poster_path = movie.getPoster_path();
                            trendingMovie.vote_average = movie.getVote_average();

                            Globals.addToTrendingMovies(trendingMovie);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                } catch (JSONException e) {
                    Log.e("JSON Error", e.getMessage());

                }

            }
        });
    }
    private synchronized void populateTrendingTvShows() {
        TmdbClient.getweektrendingtvshows(null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray results;
                try {
                    results = response.getJSONArray("results");

                    for (int i = 0; i < 16; i++) {
                        try {
                            Globals.trendingTvShow trendingTvshow = new Globals.trendingTvShow();
                            BasicTvShowDetails tvshow = new Gson().fromJson(results.get(i).toString(), BasicTvShowDetails.class);

                            trendingTvshow.id = tvshow.getId();
                            trendingTvshow.poster_path = tvshow.getPoster_path();
                            trendingTvshow.vote_average = tvshow.getVote_average();

                            Globals.addToTrendingTvShows(trendingTvshow);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                } catch (JSONException e) {
                    Log.e("JSON Error", e.getMessage());

                }

            }
        });
    }
}

