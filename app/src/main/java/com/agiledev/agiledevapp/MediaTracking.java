package com.agiledev.agiledevapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class MediaTracking {

    public static enum Media {
        MOVIE,
        TV
    }

    static String title, poster_path;
    private static int runtime;
    private static Map<FullTvShowDetails.season, Boolean> flags = new HashMap<>();
    static final Map<String, Object> trackedSeasons = new HashMap<>();
    static int alreadyTrackedAmountShow = 0, totalEpsShow = 0;

    public static AlertDialog trackMovie(final Context mContext, final Activity mActivity, final String username, final String id, final MaterialProgressBar progressBar) {
        final AlertDialog dialog = SimpleDialog.create(DialogOption.YesCancel, mContext, "Track Movie", "Are you sure you want to track this movie?");
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (progressBar != null)
                    progressBar.setVisibility(View.VISIBLE);
                final DocumentReference movieRef = FirebaseFirestore.getInstance().collection("TrackedMovies").document(username);
                movieRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            final DocumentSnapshot doc = task.getResult();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    SyncHttpClient client = new SyncHttpClient();
                                    String url = "https://api.themoviedb.org/3/" + "movie/" + id + "?api_key=" + TmdbClient.key;
                                    client.get(url, null, new JsonHttpResponseHandler() {
                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                            FullMovieDetails movieDetails = new Gson().fromJson(response.toString(), FullMovieDetails.class);
                                            if (movieDetails == null)
                                                return;
                                            title = movieDetails.getTitle();
                                            poster_path = movieDetails.getPoster_path();
                                            final ArrayList<FullMovieDetails.Genre> genreList = movieDetails.getGenres();
                                            runtime = movieDetails.getRuntime();

                                            Map<String, Object> trackedMovie = new HashMap<>();
                                            Map<String, Object> trackData = new HashMap<>();
                                            trackData.put("date", new Date());
                                            trackData.put("name", title);
                                            trackData.put("poster_path", poster_path);

                                            Map<String, String> genres = new HashMap<>();
                                            for (FullMovieDetails.Genre g : genreList) {
                                                genres.put(String.valueOf(g.id), g.name);
                                            }
                                            trackData.put("genres", genres);

                                            trackedMovie.put(id, trackData);
                                            if (!doc.exists()) {
                                                movieRef.set(trackedMovie);
                                            } else {
                                                movieRef.update(trackedMovie);
                                            }
                                            Globals.trackedMovie movie = new Globals.trackedMovie();
                                            movie.id = id;
                                            movie.date = new Date();
                                            movie.poster_path = poster_path;
                                            movie.name = title;
                                            for (HashMap.Entry<String, String> e : genres.entrySet()) {
                                                movie.genres.put(Integer.parseInt(e.getKey()), e.getValue());
                                            }
                                            Globals.addToTrackedMovies(movie);
                                            Globals.sortTrackedMovies();

                                            final DocumentReference userRef = FirebaseFirestore.getInstance().collection("UserDetails").document(username);
                                            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot doc = task.getResult();
                                                        Map<String, Object> userData = new HashMap<>();
                                                        userData.put("timeWatched", Integer.valueOf(doc.get("timeWatched").toString()) + runtime);
                                                        Map<String, Long> userGenres = (Map<String, Long>)doc.get("genresWatched");
                                                        for (FullMovieDetails.Genre g : genreList) {
                                                            if (userGenres.containsKey(g.name)) {
                                                                userGenres.put(g.name, userGenres.get(g.name) + 1);
                                                            } else {
                                                                userGenres.put(g.name, 1L);
                                                            }
                                                        }
                                                        userData.put("genresWatched", userGenres);
                                                        userRef.update(userData);

                                                        mActivity.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if (progressBar != null)
                                                                    progressBar.setVisibility(View.GONE);
                                                                Toast.makeText(mContext.getApplicationContext(), "Movie Tracked", Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    }
                                                }
                                            });

                                        }
                                    });
                                }
                            }).start();

                        }
                    }
                });
            }
        });

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public static void trackTV(Context mContext, Activity mActivity, String type, String username, String seriesId, @Nullable Integer seasonNum, @Nullable Integer episodeNum, MaterialProgressBar progressBar) {
        switch (type) {
            case "series":
                trackTVShowDialog(mContext, mActivity, username, seriesId, progressBar, true);
            case "season":
                if (seasonNum != null)
                    trackTVSeasonDialog(mContext, mActivity, username, seriesId, seasonNum, progressBar, true);
                else
                    Log.e("Tracking Season", "SeasonNum was null.");
                break;
            case "episode":
                if (seasonNum != null && episodeNum != null)
                    trackTVEpisodeDialog(mContext, mActivity, username, seriesId, seasonNum, episodeNum, progressBar, true);
                else
                    Log.e("Tracking Episode", "SeasonNum or EpisodeNum was null.");
                break;
            default:
                Log.e("Tracking", "Type was invalid.");
        }
    }

    private static void trackTVEpisodeDialog(final Context mContext, final Activity mActivity, final String username, final String seriesId, final int seasonNum, final int episodeNum,final MaterialProgressBar progressBar, boolean showDialog) {
        if (showDialog) {
            final AlertDialog dialog = SimpleDialog.create(DialogOption.YesCancel, mContext, "Track Episode", "Are you sure you want to track this episode?");
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    trackTVEpisode(mContext, mActivity, username, seriesId, seasonNum, episodeNum, progressBar);
                }
            });
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        } else
            trackTVEpisode(mContext, mActivity, username, seriesId, seasonNum, episodeNum, progressBar);
    }

    private static void trackTVEpisode(final Context mContext, final Activity mActivity, final String username, final String seriesId, final int seasonNum, final int episodeNum, final MaterialProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        final DocumentReference ref = FirebaseFirestore.getInstance().collection("TrackedTV").document(username);
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot doc = task.getResult();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final SyncHttpClient client = new SyncHttpClient();
                            String url = "https://api.themoviedb.org/3/" + "tv/" + seriesId + "/season/" + seasonNum + "/episode/" + episodeNum + "?api_key=" + TmdbClient.key;
                            client.get(url, null, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    final FullTvEpisodeDetails episode = new Gson().fromJson(response.toString(), FullTvEpisodeDetails.class);
                                    if (episode == null)
                                        return;
                                    final Map<String, Object> trackedTV = new HashMap<>();
                                    final Map<String, Object> trackedSeason = new HashMap<>();
                                    final Map<String, Object> trackedEpisode = new HashMap<>();
                                    final Map<String, Object> trackData = new HashMap<>();

                                    final Date date = new Date();
                                    trackData.put("date", date);
                                    trackData.put("episodeName", episode.getName());
                                    trackData.put("episodeNum", episode.getEpisode_number());

                                    String url = "https://api.themoviedb.org/3/" + "tv/" + seriesId + "?api_key=" + TmdbClient.key;
                                    client.get(url, null, new JsonHttpResponseHandler() {
                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                            final FullTvShowDetails show = new Gson().fromJson(response.toString(), FullTvShowDetails.class);
                                            if (show == null)
                                                return;

                                            trackData.put("id", episode.getId());
                                            trackData.put("seriesName", show.getName());

                                            final String episodeString = "Episode " + episode.getEpisode_number();
                                            trackedEpisode.put(episodeString, trackData);

                                            String url = "https://api.themoviedb.org/3/" + "tv/" + seriesId + "/season/" + seasonNum + "?api_key=" + TmdbClient.key;
                                            client.get(url, null, new JsonHttpResponseHandler() {
                                                @Override
                                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                                    final FullTvSeasonDetails seasonDetails = new Gson().fromJson(response.toString(), FullTvSeasonDetails.class);
                                                    if (seasonDetails == null)
                                                        return;

                                                    int totalEps = 0;
                                                    for (final FullTvSeasonDetails.Episode e : seasonDetails.getEpisodes()) {
                                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                                        try {
                                                            Date episodeDate = sdf.parse(e.getAir_date());
                                                            if (episodeDate.before(date)) {
                                                                totalEps++;
                                                            }
                                                        } catch (ParseException ex) {
                                                            ex.printStackTrace();
                                                        }
                                                    }
                                                    trackedEpisode.put("totalEpisodes", totalEps);

                                                    String seasonString = "Season " + episode.getSeason_number();
                                                    trackedSeason.put(seasonString, trackedEpisode);
                                                    trackedSeason.put("name", show.getName());
                                                    trackedSeason.put("poster_path", show.getPoster_path());
                                                    trackedSeason.put("lastWatched", date);
                                                    final ArrayList<FullTvShowDetails.Genre> genreList = show.getGenres();
                                                    Map<String, String> genres = new HashMap<>();
                                                    for (FullTvShowDetails.Genre g : genreList) {
                                                        genres.put(String.valueOf(g.id), g.name);
                                                    }
                                                    trackedSeason.put("genres", genres);
                                                    trackedSeason.put("totalSeasons", show.getSeason().size());
                                                    trackedTV.put(show.getId(), trackedSeason);

                                                    Globals.trackedTV tv = new Globals.trackedTV();
                                                    if (Globals.basicTvShowExists(show.getId())) {
                                                        for (Globals.trackedTV t : Globals.getTrackedTvShows()) {
                                                            if (t.id.equals(seriesId)) {
                                                                tv = (Globals.trackedTV) cloneObject(t);
                                                                break;
                                                            }
                                                        }
                                                    } else {
                                                        tv.date = date;
                                                        tv.id = show.getId();
                                                        tv.name = show.getName();
                                                        tv.poster_path = show.getPoster_path();
                                                        SerializableSparseArray<String> trackedGenres = new SerializableSparseArray<>();
                                                        for (FullTvShowDetails.Genre g : genreList) {
                                                            trackedGenres.put(g.id, g.name);
                                                        }
                                                        tv.genres = trackedGenres;
                                                    }

                                                    Globals.trackedTV.Season s = new Globals.trackedTV.Season();
                                                    if (Globals.trackedSeasonExists(seriesId, seasonNum) == Globals.responseType.PARTIAL || Globals.trackedSeasonExists(seriesId, seasonNum) == Globals.responseType.FULL) {
                                                        for (Globals.trackedTV t : Globals.getTrackedTvShows()) {
                                                            if (t.id.equals(seriesId)) {
                                                                for (Globals.trackedTV.Season season : t.trackedSeasons) {
                                                                    if (season.seasonNum == seasonNum) {
                                                                        s = (Globals.trackedTV.Season) cloneObject(season);
                                                                        tv.removeSeason(s.seasonNum);
                                                                        break;
                                                                    }
                                                                }
                                                                break;
                                                            }
                                                        }
                                                    } else {
                                                        s.seasonNum = seasonNum;
                                                    }

                                                    Globals.removeTrackedShow(tv.id);

                                                    Globals.trackedTV.Episode e = new Globals.trackedTV.Episode();
                                                    e.date = date;
                                                    e.episodeName = episode.getName();
                                                    e.id = episode.getId();
                                                    e.seriesName = show.getName();
                                                    e.episodeNum = episode.getEpisode_number();
                                                    e.seasonNum = seasonNum;
                                                    s.totalEpisodes = totalEps;
                                                    s.addEpisode(e);

                                                    tv.trackedSeasons.add(s);
                                                    Globals.addToTrackedTvShows(tv);

                                                    if (!doc.exists()) {
                                                        ref.set(trackedTV);
                                                    } else {
                                                        if (doc.contains(show.getId())) { // Show exists
                                                            if (((HashMap)doc.get(show.getId())).containsKey(seasonString)) { // Season exists
                                                                if (!(((HashMap)((HashMap)doc.get(show.getId())).get(seasonString)).containsKey(episodeString))) { // Episode doesn't exist
                                                                    HashMap currentEps = ((HashMap)((HashMap)doc.get(show.getId())).get(seasonString));
                                                                    currentEps.put(episodeString, trackedEpisode.get(episodeString));
                                                                    ref.update(show.getId() + "." + seasonString, currentEps);
                                                                    ref.update(show.getId() + ".lastWatched", trackedSeason.get("lastWatched"));
                                                                }
                                                            } else {
                                                                HashMap currentSeasons = ((HashMap)doc.get(show.getId()));
                                                                currentSeasons.put(seasonString, trackedSeason.get(seasonString));
                                                                ref.update(show.getId(), currentSeasons);
                                                                ref.update(show.getId() + ".lastWatched", trackedSeason.get("lastWatched"));
                                                            }
                                                        } else {
                                                            ref.update(trackedTV);
                                                        }
                                                    }

                                                    final DocumentReference userRef = FirebaseFirestore.getInstance().collection("UserDetails").document(username);
                                                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                DocumentSnapshot doc = task.getResult();
                                                                Map<String, Object> userData = new HashMap<>();
                                                                Map<String, Long> userGenres = (Map<String, Long>)doc.get("genresWatched");
                                                                for (FullTvShowDetails.Genre g : genreList) {
                                                                    if (userGenres.containsKey(g.name)) {
                                                                        userGenres.put(g.name, userGenres.get(g.name) + 1);
                                                                    } else {
                                                                        userGenres.put(g.name, 1L);
                                                                    }
                                                                }
                                                                userData.put("genresWatched", userGenres);
                                                                userRef.update(userData);

                                                                mActivity.runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        progressBar.setVisibility(View.GONE);
                                                                        Toast.makeText(mContext.getApplicationContext(), "TV Episode Tracked!", Toast.LENGTH_LONG).show();
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    }).start();
                }
            }
        });
    }

    private static void trackTVSeasonDialog(final Context mContext, final Activity mActivity, final String username, final String seriesId, final int seasonNum, final MaterialProgressBar progressBar, boolean showDialog) {
        if (showDialog) {
            final AlertDialog dialog = SimpleDialog.create(DialogOption.YesCancel, mContext, "Track TV", "Are you sure you want to track this entire tv season?");
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    trackTVSeason(mContext, mActivity, username, seriesId, seasonNum, progressBar);
                }
            });
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        } else
            trackTVSeason(mContext, mActivity, username, seriesId, seasonNum, progressBar);
    }

    private static void trackTVSeason(final Context mContext, final Activity mActivity, final String username, final String seriesId, final int seasonNum, final MaterialProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        final DocumentReference ref = FirebaseFirestore.getInstance().collection("TrackedTV").document(username);
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot doc = task.getResult();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final SyncHttpClient client = new SyncHttpClient();
                            String url = "https://api.themoviedb.org/3/" + "tv/"+ seriesId + "/season/" + seasonNum + "?api_key=" + TmdbClient.key;
                            client.get(url, null, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    final FullTvSeasonDetails season = new Gson().fromJson(response.toString(), FullTvSeasonDetails.class);
                                    if (season == null)
                                        return;

                                    String url = "https://api.themoviedb.org/3/" + "tv/" + seriesId + "?api_key=" + TmdbClient.key;
                                    client.get(url, null, new JsonHttpResponseHandler() {
                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                            final FullTvShowDetails show = new Gson().fromJson(response.toString(), FullTvShowDetails.class);
                                            if (show == null)
                                                return;

                                            ArrayList<FullTvSeasonDetails.Episode> episodes = season.getEpisodes();
                                            final Map<String, Object> trackedTV = new HashMap<>();
                                            final Map<String, Object> trackedSeason = new HashMap<>();
                                            final Map<String, Object> trackedEpisode = new HashMap<>();

                                            Date date = new Date();
                                            final ArrayList<FullTvShowDetails.Genre> genreList = show.getGenres();
                                            Map<String, String> genres = new HashMap<>();
                                            for (FullTvShowDetails.Genre g : genreList) {
                                                genres.put(String.valueOf(g.id), g.name);
                                            }

                                            Globals.trackedTV tv = new Globals.trackedTV();
                                            if (Globals.basicTvShowExists(show.getId())) {
                                                for (Globals.trackedTV t : Globals.getTrackedTvShows()) {
                                                    if (t.id.equals(seriesId)) {
                                                        tv = (Globals.trackedTV) cloneObject(t);
                                                        break;
                                                    }
                                                }
                                            } else {
                                                tv.date = date;
                                                tv.id = show.getId();
                                                tv.name = show.getName();
                                                tv.poster_path = show.getPoster_path();
                                                SerializableSparseArray<String> trackedGenres = new SerializableSparseArray<>();
                                                for (FullTvShowDetails.Genre g : genreList) {
                                                    trackedGenres.put(g.id, g.name);
                                                }
                                                tv.genres = trackedGenres;
                                            }

                                            Globals.trackedTV.Season s = new Globals.trackedTV.Season();
                                            if (Globals.trackedSeasonExists(seriesId, seasonNum) == Globals.responseType.PARTIAL || Globals.trackedSeasonExists(seriesId, seasonNum) == Globals.responseType.FULL) {
                                                for (Globals.trackedTV t : Globals.getTrackedTvShows()) {
                                                    if (t.id.equals(seriesId)) {
                                                        for (Globals.trackedTV.Season season : t.trackedSeasons) {
                                                            if (season.seasonNum == seasonNum) {
                                                                s = (Globals.trackedTV.Season) cloneObject(season);
                                                                tv.removeSeason(s.seasonNum);
                                                                break;
                                                            }
                                                        }
                                                        break;
                                                    }
                                                }
                                            } else {
                                                s.seasonNum = seasonNum;
                                            }

                                            int alreadyTrackedAmount = 0;
                                            if (s.trackedEpisodes != null && !s.trackedEpisodes.isEmpty()) {
                                                alreadyTrackedAmount = s.trackedEpisodes.size();
                                                s.trackedEpisodes.clear();
                                            }

                                            Globals.removeTrackedShow(tv.id);

                                            int totalEps = 0;
                                            for (final FullTvSeasonDetails.Episode e : episodes) {
                                                final Map<String, Object> trackData = new HashMap<>();

                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                                try {
                                                    Date episodeDate = sdf.parse(e.getAir_date());
                                                    if (episodeDate.before(date)) {
                                                        totalEps++;
                                                    }
                                                } catch (ParseException ex) {
                                                    ex.printStackTrace();
                                                }

                                                trackData.put("date", date);
                                                trackData.put("episodeName", e.getName());
                                                trackData.put("episodeNum", e.getEpisode_number());

                                                trackData.put("id", e.getId());
                                                trackData.put("seriesName", show.getName());

                                                String episodeString = "Episode " + e.getEpisode_number();
                                                trackedEpisode.put(episodeString, trackData);

                                                Globals.trackedTV.Episode ep = new Globals.trackedTV.Episode();
                                                ep.date = date;
                                                ep.episodeName = e.getName();
                                                ep.id = e.getId();
                                                ep.seriesName = show.getName();
                                                ep.episodeNum = e.getEpisode_number();
                                                s.addEpisode(ep);
                                            }
                                            s.totalEpisodes = totalEps;
                                            trackedEpisode.put("totalEpisodes", totalEps);

                                            tv.trackedSeasons.add(s);
                                            Globals.addToTrackedTvShows(tv);

                                            String seasonString = "Season " + seasonNum;
                                            trackedSeason.put(seasonString, trackedEpisode);
                                            trackedSeason.put("name", show.getName());
                                            trackedSeason.put("poster_path", show.getPoster_path());
                                            trackedSeason.put("lastWatched", date);
                                            trackedSeason.put("genres", genres);
                                            trackedSeason.put("totalSeasons", show.getSeason().size());
                                            trackedTV.put(show.getId(), trackedSeason);

                                            if (!doc.exists()) {
                                                ref.set(trackedTV);
                                            } else {
                                                if (doc.contains(show.getId())) { // Show exists
                                                    if (!(((HashMap)doc.get(show.getId())).containsKey(seasonString))) { // Season doesn't exist
                                                        HashMap<String, Object> currentSeasons = ((HashMap<String, Object>)doc.get(show.getId()));
                                                        currentSeasons.put(seasonString, trackedSeason.get(seasonString));
                                                        ref.update(show.getId(), currentSeasons);
                                                        ref.update(show.getId() + ".lastWatched", trackedSeason.get("lastWatched"));
                                                    } else { //Season exists
                                                        HashMap<String, Object> currentSeasons = ((HashMap<String, Object>)doc.get(show.getId()));
                                                        HashMap<String, Object> currentSeason = (HashMap<String, Object>)currentSeasons.get(seasonString);
                                                        for (Map.Entry<String, Object> entry : trackedEpisode.entrySet()) {
                                                            if (!currentSeason.containsKey(entry.getKey())) {
                                                                currentSeason.put(entry.getKey(), entry.getValue());
                                                            }
                                                        }
                                                        currentSeasons.put(seasonString, currentSeason);
                                                        ref.update(show.getId(), currentSeasons);
                                                        ref.update(show.getId() + ".lastWatched", trackedSeason.get("lastWatched"));
                                                    }
                                                } else {
                                                    ref.update(trackedTV);
                                                }
                                            }

                                            final DocumentReference userRef = FirebaseFirestore.getInstance().collection("UserDetails").document(username);
                                            final int finalAlreadyTrackedAmount = alreadyTrackedAmount;
                                            final int finalTotalEps = totalEps;
                                            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot doc = task.getResult();
                                                        Map<String, Object> userData = new HashMap<>();
                                                        Map<String, Long> userGenres = (Map<String, Long>)doc.get("genresWatched");
                                                        for (int i = 0; i < finalTotalEps - finalAlreadyTrackedAmount; i++) {
                                                            for (FullTvShowDetails.Genre g : genreList) {
                                                                if (userGenres.containsKey(g.name)) {
                                                                    userGenres.put(g.name, userGenres.get(g.name) + 1);
                                                                } else {
                                                                    userGenres.put(g.name, 1L);
                                                                }
                                                            }
                                                        }
                                                        userData.put("genresWatched", userGenres);
                                                        userRef.update(userData);

                                                        mActivity.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                progressBar.setVisibility(View.GONE);
                                                                Toast.makeText(mContext.getApplicationContext(), "TV Season Tracked!", Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    }).start();
                }
            }
        });
    }

    private static void trackTVShowDialog(final Context mContext, final Activity mActivity, final String username, final String seriesId, final MaterialProgressBar progressBar, boolean showDialog) {
        if (showDialog) {
            final AlertDialog dialog = SimpleDialog.create(DialogOption.YesCancel, mContext, "Track TV", "Are you sure you want to track this entire tv show?");
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    trackTVShow(mContext, mActivity, username, seriesId, progressBar);
                }
            });
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        } else {
            trackTVShow(mContext, mActivity, username, seriesId, progressBar);
        }
    }

    private static void trackTVShow(final Context mContext, final Activity mActivity, final String username, final String seriesId, final MaterialProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        final DocumentReference ref = FirebaseFirestore.getInstance().collection("TrackedTV").document(username);
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot doc = task.getResult();
                    TmdbClient.getFullTvShowDetails(seriesId, null, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            final FullTvShowDetails show = new Gson().fromJson(response.toString(), FullTvShowDetails.class);
                            if (show == null)
                                return;

                            final Map<String, Object> trackedTV = new HashMap<>();

                            final ArrayList<FullTvShowDetails.Genre> genreList = show.getGenres();

                            for (final FullTvShowDetails.season s : show.getSeason()) {
                                flags.put(s, false);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Map.Entry<String, Object> entry = trackSeasonForShowTrack(seriesId, s, show, genreList);
                                        trackedSeasons.put(entry.getKey(), entry.getValue());
                                    }
                                }).start();
                            }

                            final Thread flagCheck = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    while (checkForNotCompletes(flags)) {

                                    }
                                    trackedSeasons.put("name", show.getName());
                                    trackedSeasons.put("poster_path", show.getPoster_path());
                                    trackedSeasons.put("lastWatched", new Date());
                                    Map<String, String> genres = new HashMap<>();
                                    for (FullTvShowDetails.Genre g : genreList) {
                                        genres.put(String.valueOf(g.id), g.name);
                                    }
                                    trackedSeasons.put("genres", genres);
                                    trackedSeasons.put("totalSeasons", show.getSeason().size());
                                    trackedTV.put(show.getId(), trackedSeasons);

                                    if (!doc.exists()) {
                                        ref.set(trackedTV);
                                    } else {
                                        if (doc.contains(show.getId())) { // Show exists
                                            HashMap currentStoredSeasons = ((HashMap)doc.get(show.getId()));
                                            for (Map.Entry<String, Object> entry : trackedSeasons.entrySet()) {
                                                String seasonString;
                                                if (entry.getKey().contains("Season ")) {
                                                    seasonString = entry.getKey();
                                                    if ((((HashMap)doc.get(show.getId())).containsKey(seasonString))) { // Season exists
                                                        HashMap currentStoredSeason = ((HashMap)((HashMap)doc.get(show.getId())).get(seasonString));
                                                        HashMap<String, Object> allEpisodes = (HashMap<String, Object>)entry.getValue();
                                                        for (Map.Entry<String, Object> e : allEpisodes.entrySet()) {
                                                            if (!currentStoredSeason.containsKey(e.getKey())) {
                                                                currentStoredSeason.put(e.getKey(), e.getValue());
                                                            }
                                                        }
                                                        if (!currentStoredSeason.isEmpty())
                                                            currentStoredSeasons.put(seasonString, currentStoredSeason);
                                                    } else { // Season doesn't exist
                                                        HashMap<String, Object> allEpisodes = (HashMap<String, Object>)entry.getValue();
                                                        currentStoredSeasons.put(seasonString, allEpisodes);
                                                    }
                                                }
                                            }
                                            ref.update(show.getId(), currentStoredSeasons);
                                            ref.update(show.getId() + ".lastWatched", trackedSeasons.get("lastWatched"));
                                        } else {
                                            ref.update(show.getId(), trackedTV.get(show.getId()));
                                            ref.update(show.getId() + ".lastWatched", trackedSeasons.get("lastWatched"));
                                        }
                                    }

                                    final DocumentReference userRef = FirebaseFirestore.getInstance().collection("UserDetails").document(username);
                                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot doc = task.getResult();
                                                Map<String, Object> userData = new HashMap<>();
                                                Map<String, Long> userGenres = (Map<String, Long>)doc.get("genresWatched");
                                                for (int i = 0; i < totalEpsShow - alreadyTrackedAmountShow; i++) {
                                                    for (FullTvShowDetails.Genre g : genreList) {
                                                        if (userGenres.containsKey(g.name))
                                                            userGenres.put(g.name, userGenres.get(g.name) + 1);
                                                        else
                                                            userGenres.put(g.name, 1L);
                                                    }
                                                }
                                                userData.put("genresWatched", userGenres);
                                                userRef.update(userData);

                                                mActivity.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(mContext.getApplicationContext(), "TV Show Tracked!", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            });
                            flagCheck.start();
                        }
                    });
                }
            }
        });
    }

    private static boolean checkForNotCompletes(Map<FullTvShowDetails.season, Boolean> map) {
        for (Map.Entry<FullTvShowDetails.season, Boolean> e : map.entrySet()) {
            if (!e.getValue())
                return true;
        }
        return false;
    }

    private static synchronized Map.Entry<String, Object> trackSeasonForShowTrack(final String seriesId, final FullTvShowDetails.season s, final FullTvShowDetails show, final ArrayList<FullTvShowDetails.Genre> genreList) {
        final HashMap<String, Object> tempMap = new HashMap<>();
        SyncHttpClient client = new SyncHttpClient();
        String url = "https://api.themoviedb.org/3/" + "tv/"+ seriesId + "/season/" + s.season_number + "?api_key=" + TmdbClient.key;
        client.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                final FullTvSeasonDetails season = new Gson().fromJson(response.toString(), FullTvSeasonDetails.class);
                if (season == null) {
                    flags.put(s, true);
                    return;
                }

                final Map<String, Object> trackedEpisode = new HashMap<>();

                ArrayList<FullTvSeasonDetails.Episode> episodes = season.getEpisodes();

                Date date = new Date();

                Globals.trackedTV tv = new Globals.trackedTV();
                if (Globals.basicTvShowExists(show.getId())) {
                    for (Globals.trackedTV t : Globals.getTrackedTvShows()) {
                        if (t.id.equals(seriesId)) {
                            tv = (Globals.trackedTV) cloneObject(t);
                            break;
                        }
                    }
                } else {
                    tv.date = date;
                    tv.id = show.getId();
                    tv.name = show.getName();
                    tv.poster_path = show.getPoster_path();
                    SerializableSparseArray<String> trackedGenres = new SerializableSparseArray<>();
                    for (FullTvShowDetails.Genre g : genreList) {
                        trackedGenres.put(g.id, g.name);
                    }
                    tv.genres = trackedGenres;
                    tv.totalSeasons = show.getNumber_of_seasons();
                }

                Globals.trackedTV.Season sTemp = new Globals.trackedTV.Season();
                if (Globals.trackedSeasonExists(seriesId, s.season_number) == Globals.responseType.PARTIAL || Globals.trackedSeasonExists(seriesId, s.season_number) == Globals.responseType.FULL) {
                    for (Globals.trackedTV t : Globals.getTrackedTvShows()) {
                        if (t.id.equals(seriesId)) {
                            for (Globals.trackedTV.Season seasonTemp : t.trackedSeasons) {
                                if (seasonTemp.seasonNum == s.season_number) {
                                    sTemp = (Globals.trackedTV.Season) cloneObject(seasonTemp);
                                    tv.removeSeason(sTemp.seasonNum);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                } else {
                    sTemp.seasonNum = s.season_number;
                }

                if (sTemp.trackedEpisodes != null && !sTemp.trackedEpisodes.isEmpty()) {
                    alreadyTrackedAmountShow += sTemp.trackedEpisodes.size();
                    sTemp.trackedEpisodes.clear();
                }

                Globals.removeTrackedShow(tv.id);

                int totalEps = 0;
                for (final FullTvSeasonDetails.Episode e : episodes) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date episodeDate = sdf.parse(e.getAir_date());
                        if (episodeDate.after(date)) {
                            flags.put(s, true);
                            break;
                        } else {
                            totalEps++;
                        }
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }

                    final Map<String, Object> trackData = new HashMap<>();
                    trackData.put("date", date);
                    trackData.put("episodeName", e.getName());
                    trackData.put("episodeNum", e.getEpisode_number());

                    trackData.put("id", e.getId());
                    trackData.put("seriesName", show.getName());

                    String episodeString = "Episode " + e.getEpisode_number();
                    trackedEpisode.put(episodeString, trackData);

                    Globals.trackedTV.Episode ep = new Globals.trackedTV.Episode();
                    ep.date = date;
                    ep.episodeName = e.getName();
                    ep.id = e.getId();
                    ep.seriesName = show.getName();
                    ep.episodeNum = e.getEpisode_number();
                    sTemp.addEpisode(ep);
                }
                sTemp.totalEpisodes = totalEps;
                trackedEpisode.put("totalEpisodes", totalEps);
                totalEpsShow += totalEps;

                tv.trackedSeasons.add(sTemp);
                Globals.addToTrackedTvShows(tv);

                String seasonString = "Season " + s.season_number;
                tempMap.put(seasonString, trackedEpisode);

                flags.put(s, true);
            }
        });
        return new AbstractMap.SimpleEntry<>("Season " + s.season_number, tempMap.get("Season " + s.season_number));
    }

    public static AlertDialog untrackMovie(final Context mContext, final Activity mActivity, final String username, final String id, final MaterialProgressBar progressBar) {
        final AlertDialog dialog = SimpleDialog.create(DialogOption.YesCancel, mContext, "Untrack Movie", "Are you sure you want to untrack this movie?");
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (progressBar != null)
                    progressBar.setVisibility(View.VISIBLE);
                DocumentReference ref = FirebaseFirestore.getInstance().collection("TrackedMovies").document(username);
                ref.update(id, FieldValue.delete());

                Globals.removeFromTrackedMovies(id);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SyncHttpClient client = new SyncHttpClient();
                        String url = "https://api.themoviedb.org/3/" + "movie/" + id + "?api_key=" + TmdbClient.key;
                        client.get(url, null, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                FullMovieDetails movieDetails = new Gson().fromJson(response.toString(), FullMovieDetails.class);
                                if (movieDetails == null)
                                    return;
                                final ArrayList<FullMovieDetails.Genre> genreList = movieDetails.getGenres();

                                final DocumentReference userRef = FirebaseFirestore.getInstance().collection("UserDetails").document(username);
                                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot doc = task.getResult();
                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("timeWatched", Integer.valueOf(doc.get("timeWatched").toString()) - runtime);
                                            Map<String, Long> userGenres = (Map<String, Long>)doc.get("genresWatched");
                                            for (FullMovieDetails.Genre g : genreList) {
                                                userGenres.put(g.name, userGenres.get(g.name) - 1);
                                            }
                                            userData.put("genresWatched", userGenres);
                                            userRef.update(userData);

                                            mActivity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (progressBar != null)
                                                        progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(mContext.getApplicationContext(), "Movie Untracked!", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                    }
                }).start();
            }
        });

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public static void untrackTV(final Context mContext, final Activity mActivity, String type, final String username, final String seriesId, @Nullable final Integer seasonNum, @Nullable Integer episodeNum, final MaterialProgressBar progressBar, boolean asking) {
        switch (type) {
            case "series":
                if (asking) {
                    AlertDialog dialog = SimpleDialog.create(DialogOption.YesCancel, mContext, "Track?", "Do you want to track the rest of this show, or untrack all episodes?");
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Track", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            trackTVShow(mContext, mActivity, username, seriesId, progressBar);
                        }
                    });
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Untrack", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            untrackTvShowDialog(mContext, mActivity, username, seriesId, progressBar, false);
                        }
                    });
                    dialog.show();
                } else {
                    untrackTvShowDialog(mContext, mActivity, username, seriesId, progressBar, true);
                }
                break;
            case "season":
                if (seasonNum != null)
                    if (asking) {
                        AlertDialog dialog = SimpleDialog.create(DialogOption.YesCancel, mContext, "Track?", "Do you want to track the rest of this season, or untrack all episodes?");
                        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Track", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                trackTVSeason(mContext, mActivity, username, seriesId, seasonNum, progressBar);
                            }
                        });
                        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Untrack", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                untrackTvSeasonDialog(mContext, mActivity, username, seriesId, seasonNum, progressBar, false);
                            }
                        });
                        dialog.show();
                    } else {
                        untrackTvSeasonDialog(mContext, mActivity, username, seriesId, seasonNum, progressBar, true);
                    }
                else
                    Log.e("Tracking Season", "SeasonNum was null.");
                break;
            case "episode":
                if (seasonNum != null && episodeNum != null)
                    untrackTvEpisodeDialog(mContext, mActivity, username, seriesId, seasonNum, episodeNum, progressBar, true);
                else
                    Log.e("Tracking Episode", "SeasonNum or EpisodeNum was null.");
                break;
            default:
                Log.e("Tracking", "Type was invalid.");
        }
    }

    private static void untrackTvShowDialog(final Context mContext, final Activity mActivity, final String username, final String seriesId, final MaterialProgressBar progressBar, boolean showDialog) {
        if (showDialog) {
            final AlertDialog dialog = SimpleDialog.create(DialogOption.YesCancel, mContext, "Untrack TV", "Are you sure you want to untrack this entire tv show?");
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    untrackTVShow(mContext, mActivity, username, seriesId, progressBar);
                }
            });
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        } else {
            untrackTVShow(mContext, mActivity, username, seriesId, progressBar);
        }
    }

    private static void untrackTVShow(final Context mContext, final Activity mActivity, final String username, final String seriesId, final MaterialProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);

        SerializableSparseArray<String> genreList = new SerializableSparseArray<>();
        int epAmount = 0;
        for (Globals.trackedTV tv : Globals.getTrackedTvShows()) {
            if (tv.id.equals(seriesId)) {
                genreList = tv.genres;
                for (Globals.trackedTV.Season season : tv.trackedSeasons) {
                    epAmount += season.trackedEpisodes.size();
                }
                System.out.println();
                break;
            }
        }

        DocumentReference ref = FirebaseFirestore.getInstance().collection("TrackedTV").document(username);
        ref.update(seriesId, FieldValue.delete());

        Globals.removeTrackedShow(seriesId);

        final SerializableSparseArray<String> finalGenreList = genreList;
        final int finalEpAmount = epAmount;
        new Thread(new Runnable() {
            @Override
            public void run() {
                SyncHttpClient client = new SyncHttpClient();
                String url = "https://api.themoviedb.org/3/" + "tv/" + seriesId + "?api_key=" + TmdbClient.key;
                client.get(url, null, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        FullTvShowDetails tvDetails = new Gson().fromJson(response.toString(), FullTvShowDetails.class);
                        if (tvDetails == null)
                            return;

                        final DocumentReference userRef = FirebaseFirestore.getInstance().collection("UserDetails").document(username);
                        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot doc = task.getResult();
                                    Map<String, Object> userData = new HashMap<>();
                                    Map<String, Long> userGenres = (Map<String, Long>)doc.get("genresWatched");
                                    for (int i = 0; i < finalGenreList.size(); i++) {
                                        for (int j = 0; j < finalEpAmount; j++) {
                                            int key = finalGenreList.keyAt(i);
                                            String val = finalGenreList.get(key);
                                            if (userGenres.containsKey(val)) {
                                                if (userGenres.get(val) - 1 <= 0) {
                                                    userGenres.remove(val);
                                                } else {
                                                    userGenres.put(val, userGenres.get(val) - 1);
                                                }
                                            }
                                        }
                                    }
                                    userData.put("genresWatched", userGenres);
                                    userRef.update(userData);

                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(mContext.getApplicationContext(), "TV Show Untracked", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }

    private static void untrackTvSeasonDialog(final Context mContext, final Activity mActivity, final String username, final String seriesId, final int seasonNum, final MaterialProgressBar progressBar, boolean showDialog) {
        if (showDialog) {
            final AlertDialog dialog = SimpleDialog.create(DialogOption.YesCancel, mContext, "Untrack TV", "Are you sure you want to untrack this entire tv season?");
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    untrackSeason(mActivity, username, seriesId, seasonNum, progressBar);
                }
            });

            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        } else
            untrackSeason(mActivity, username, seriesId, seasonNum, progressBar);
    }

    private static void untrackSeason(final Activity mActivity, final String username, String seriesId, int seasonNum, final MaterialProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);

        SerializableSparseArray<String> genreList = new SerializableSparseArray<>();
        int epAmount = 0;
        for (Globals.trackedTV tv : Globals.getTrackedTvShows()) {
            if (tv.id.equals(seriesId)) {
                genreList = tv.genres;
                for (Globals.trackedTV.Season season : tv.trackedSeasons) {
                    if (season.seasonNum == seasonNum) {
                        epAmount = season.trackedEpisodes.size();
                        break;
                    }
                }
                System.out.println();
                break;
            }
        }

        Globals.removeTrackedSeason(seriesId, seasonNum);

        DocumentReference ref = FirebaseFirestore.getInstance().collection("TrackedTV").document(username);
        Map<String, Object> seasonToDelete = new HashMap<>();
        seasonToDelete.put(seriesId + ".Season " + seasonNum, FieldValue.delete());
        final SerializableSparseArray<String> finalGenreList = genreList;
        final int finalEpAmount = epAmount;
        ref.update(seasonToDelete).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                final DocumentReference userRef = FirebaseFirestore.getInstance().collection("UserDetails").document(username);
                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            Map<String, Object> userData = new HashMap<>();
                            Map<String, Long> userGenres = (Map<String, Long>)doc.get("genresWatched");
                            for (int i = 0; i < finalGenreList.size(); i++) {
                                for (int j = 0; j < finalEpAmount; j++) {
                                    int key = finalGenreList.keyAt(i);
                                    String val = finalGenreList.get(key);
                                    if (userGenres.containsKey(val)) {
                                        if (userGenres.get(val) - 1 <= 0) {
                                            userGenres.remove(val);
                                        } else {
                                            userGenres.put(val, userGenres.get(val) - 1);
                                        }
                                    }
                                }
                            }
                            userData.put("genresWatched", userGenres);
                            userRef.update(userData);

                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(mActivity.getApplicationContext(), "Season Untracked!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    private static void untrackTvEpisodeDialog(final Context mContext, final Activity mActivity, final String username, final String seriesId, final int seasonNum, final int episodeNum, final MaterialProgressBar progressBar, boolean showDialog) {
        if (showDialog) {
            final AlertDialog dialog = SimpleDialog.create(DialogOption.YesCancel, mContext, "Untrack Episode", "Are you sure you want to untrack this episode?");
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    untrackTVEpisode(mActivity, username, seriesId, seasonNum, episodeNum, progressBar);
                }
            });

            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        } else
            untrackTVEpisode(mActivity, username, seriesId, seasonNum, episodeNum, progressBar);
    }

    private static void untrackTVEpisode(final Activity mActivity, final String username, final String seriesId, final int seasonNum, final int episodeNum, final MaterialProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);

        SerializableSparseArray<String> genreList = new SerializableSparseArray<>();
        for (Globals.trackedTV tv : Globals.getTrackedTvShows()) {
            if (tv.id.equals(seriesId)) {
                genreList = tv.genres;
                System.out.println();
                break;
            }
        }

        Globals.removeTrackedEpisode(seriesId, seasonNum, episodeNum);

        DocumentReference ref = FirebaseFirestore.getInstance().collection("TrackedTV").document(username);
        Map<String, Object> episodeToDelete = new HashMap<>();
        episodeToDelete.put(seriesId + ".Season " + seasonNum + ".Episode " + episodeNum, FieldValue.delete());
        final SerializableSparseArray<String> finalGenreList = genreList;
        ref.update(episodeToDelete).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                final DocumentReference userRef = FirebaseFirestore.getInstance().collection("UserDetails").document(username);
                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            Map<String, Object> userData = new HashMap<>();
                            Map<String, Long> userGenres = (Map<String, Long>)doc.get("genresWatched");
                            for (int i = 0; i < finalGenreList.size(); i++) {
                                int key = finalGenreList.keyAt(i);
                                String val = finalGenreList.get(key);
                                if (userGenres.containsKey(val)) {
                                    if (userGenres.get(val) - 1 <= 0) {
                                        userGenres.remove(val);
                                    } else {
                                        userGenres.put(val, userGenres.get(val) - 1);
                                    }
                                }
                                userData.put("genresWatched", userGenres);
                                userRef.update(userData);

                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(mActivity.getApplicationContext(), "Episode Untracked!", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
        });
    }

    private static Object cloneObject(Object orig) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(orig);
            oos.flush();
            oos.close();
            bos.close();
            byte[] byteData = bos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
            Object object = new ObjectInputStream(bais).readObject();
            return object;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}