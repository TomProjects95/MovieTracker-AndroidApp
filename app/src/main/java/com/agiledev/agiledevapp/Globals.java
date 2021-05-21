package com.agiledev.agiledevapp;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Globals
{

    //Lists
    private static SparseArray<String> movieGenreTags = new SparseArray<>();
    private static SparseArray<String> tvGenreTags = new SparseArray<>();
    private static List<trackedMovie> trackedMovies = new ArrayList<>();
    private static List<trackedTV> trackedTV = new ArrayList<>();
    private static List<trendingMovie> trendingMovies = new ArrayList<>();
    private static List<trendingTvShow> trendingTvShows = new ArrayList<>();
    private static SearchType lastSearchType = SearchType.Movie;

    //Genres getter and setter
    static SparseArray<String> getMovieGenreTags() {
        return movieGenreTags;
    }
    static synchronized void setMovieGenreTags(SparseArray<String> genres) {
        movieGenreTags = genres;
    }
    static SparseArray<String> getTvGenreTags() {
        return tvGenreTags;
    }
    static synchronized void setTvGenreTags(SparseArray<String> genres) {
        tvGenreTags = genres;
    }


    //Tracked media getters
    static List<trackedMovie> getTrackedMovies() {
        return trackedMovies;
    }

    static List<trackedTV> getTrackedTvShows() {
        return trackedTV;
    }


    //----------- Setters ------------
    static synchronized void setTrackedMovies(List<trackedMovie> trackedMovies) {
        Globals.trackedMovies = trackedMovies;
    }
    static synchronized void setTrackedTvShows(List<trackedTV> trackedTvShows) {
        Globals.trackedTV = trackedTvShows;
        for (Globals.trackedTV tv : trackedTV) {
            Collections.sort(tv.trackedSeasons);
            for (Globals.trackedTV.Season s : tv.trackedSeasons) {
                Collections.sort(s.trackedEpisodes);
            }
        }
    }
    static synchronized void setLastSearchType(SearchType searchType) {
        Globals.lastSearchType = searchType;
    }
    static void setTrendingMovies(List<trendingMovie> trendingMovies)
    {
        Globals.trendingMovies = trendingMovies;
    }
    static void setTrendingTvShows(List<trendingTvShow> trendingTvShows)
    {
        Globals.trendingTvShows = trendingTvShows;
    }

    //----------- Getters ------------
    static SearchType getLastSearchType() {
        return lastSearchType;
    }
    static List<trendingMovie> getTrendingMovies()
    {
        return trendingMovies;
    }
    static List<trendingTvShow> getTrendingTvShows()
    {
        return trendingTvShows;
    }


    //----------- Adding ------------
    static synchronized void addToTrackedMovies(trackedMovie movie) {
        if (!trackedMoviesContains(movie.id))
            Globals.trackedMovies.add(movie);
    }
    static synchronized void addToTrackedTvShows(trackedTV TV) {
        if (!basicTvShowExists(TV.id))
            Globals.trackedTV.add(TV);
    }
    static void addToTrendingMovies(trendingMovie movie) {
        Globals.trendingMovies.add(movie);
    }
    static void addToTrendingTvShows(trendingTvShow tvshow) {
        Globals.trendingTvShows.add(tvshow);
    }


    //----------- Removing ------------
    static synchronized void removeFromTrackedMovies(String id) {
        for (int i = 0; i < Globals.trackedMovies.size(); i++) {
            if (Globals.trackedMovies.get(i).id.equals(id)) {
                Globals.trackedMovies.remove(i);
                break;
            }
        }
    }
    static synchronized void removeTrackedEpisode(String seriesId, int seasonNum, int episodeNum) {
        for (Globals.trackedTV tv : trackedTV) {
            if (tv.id.equals(seriesId)) {
                for (Globals.trackedTV.Season s : tv.trackedSeasons) {
                    if (s.seasonNum == seasonNum) {
                        for (Globals.trackedTV.Episode e : s.trackedEpisodes) {
                            if (e.episodeNum == episodeNum) {
                                s.trackedEpisodes.remove(e);
                                break;
                            }
                        }
                        break;
                    }
                }
                break;
            }
        }
    }
    static synchronized void removeTrackedSeason(String seriesId, int seasonNum) {
        for (Globals.trackedTV tv : trackedTV) {
            if (tv.id.equals(seriesId)) {
                for (Globals.trackedTV.Season s : tv.trackedSeasons) {
                    if (s.seasonNum == seasonNum) {
                        tv.trackedSeasons.remove(s);
                        break;
                    }
                }
                break;
            }
        }
    }
    static synchronized void removeTrackedShow(String seriesId) {
        for (Globals.trackedTV tv : trackedTV) {
            if (tv.id.equals(seriesId)) {
                trackedTV.remove(tv);
                break;
            }
        }
    }


    //----------- Movie Contains ------------
    static boolean trackedMoviesContains(String id) {
        for (trackedMovie m : Globals.trackedMovies) {
            if(m.id.equals(id))
                return true;
        }
        return false;
    }
    //----------- TV Contains ------------
    enum responseType {
        FULL,
        PARTIAL,
        NONE
    }
    static boolean basicTvShowExists(String seriesId) {
        for (trackedTV tv : Globals.trackedTV) {
            if (tv.id.equals(seriesId)) {
                return true;
            }
        }
        return false;
    }
    static responseType trackedEpisodeExists(String seriesId, int seasonNum, int episodeNum) {
        for (trackedTV tv : Globals.trackedTV) {
            if (tv.id.equals(seriesId)) {
                for (Globals.trackedTV.Season s : tv.trackedSeasons) {
                    if (s.seasonNum == seasonNum) {
                        ArrayList<Globals.trackedTV.Episode> trackedEpisodes = s.trackedEpisodes;
                        for (Globals.trackedTV.Episode e : trackedEpisodes) {
                            if (e.episodeNum == episodeNum)
                                return responseType.FULL;
                        }
                        return responseType.NONE;
                    }
                }
            }
        }
        return responseType.NONE;
    }
    static responseType trackedSeasonExists(String seriesId, int seasonNum) {
        for (trackedTV tv : Globals.trackedTV) {
            if (tv.id.equals(seriesId)) {
                for (Globals.trackedTV.Season s : tv.trackedSeasons) {
                    if (s.seasonNum == seasonNum) { //Season exists
                        if (s.trackedEpisodes.size() == s.totalEpisodes) { //All episodes exist
                            return responseType.FULL;
                        } else if (s.trackedEpisodes.size() == 0) { //No episodes exist
                            return responseType.NONE;
                        } else { //Some episodes exist
                            return responseType.PARTIAL;
                        }
                    }
                }
            }
        }
        return responseType.NONE;
    }
    static responseType trackedShowExists(String seriesId) {
        HashMap<Integer, responseType> responses = new HashMap<>();
        for (trackedTV tv : Globals.trackedTV) {
            if (tv.id.equals(seriesId)) {
                if (tv.trackedSeasons.size() < tv.totalSeasons && tv.trackedSeasons.size() > 0) {
                    responses.put(-1, responseType.PARTIAL);
                } else {
                    for (Globals.trackedTV.Season s : tv.trackedSeasons) {
                        if (s.trackedEpisodes.size() == s.totalEpisodes) {
                            responses.put(s.seasonNum, responseType.FULL);
                        } else if (s.trackedEpisodes.size() > 0) {
                            responses.put(s.seasonNum, responseType.PARTIAL);
                        }
                    }
                }
            }
        }
        if (responses.containsValue(responseType.PARTIAL))
            return responseType.PARTIAL;
        else if (!responses.containsValue(responseType.FULL))
            return responseType.NONE;
        else if (!responses.containsValue(responseType.NONE))
            return responseType.FULL;
        return responseType.NONE;
    }


    //----------- Sorting ------------
    static synchronized void sortTrackedMovies() {
        if (Globals.trackedMovies.size() > 0) {
            Collections.sort(Globals.trackedMovies, new Comparator<trackedMovie>() {
                @Override
                public int compare(Globals.trackedMovie o1, Globals.trackedMovie o2) {
                    return o2.date.compareTo(o1.date);
                }
            });
        }
    }
    static synchronized void sortTrackedTvShows() {
        if (Globals.trackedTV.size() > 0) {
            Collections.sort(Globals.trackedTV, new Comparator<trackedTV>() {
                @Override
                public int compare(Globals.trackedTV o1, Globals.trackedTV o2) {
                    return o2.date.compareTo(o1.date);
                }
            });
        }
    }


    //-------Tracked Movies--------
    static class trackedMovie implements Comparable<trackedMovie> {
        String id, name;
        Date date;
        String poster_path;
        SparseArray<String> genres = new SparseArray<>();

        @Override
        public int compareTo(@NonNull trackedMovie o) {
            return o.date.compareTo(date);
        }
    }

    //-------Tracked TV Shows--------
    static class trackedTV implements Serializable {
        String id, name;
        String poster_path;
        Date date;
        int totalSeasons;
        SerializableSparseArray<String> genres = new SerializableSparseArray<>();
        ArrayList<Season> trackedSeasons = new ArrayList<>();

        trackedTV() {}

        static class Episode implements Comparable<Episode>, Serializable{
            Date date;
            String episodeName, id, seriesName;
            int episodeNum, seasonNum;

            public Episode() {}

            @Override
            public int compareTo(@NonNull Episode episode) {
                return Integer.compare(episodeNum, episode.episodeNum);
            }
        }

        static class Season implements Comparable<Season>, Serializable {
            int seasonNum, totalEpisodes;
            ArrayList<Episode> trackedEpisodes;

            Season() {}

            @Override
            public int compareTo(@NonNull Season season) {
                return Integer.compare(seasonNum, season.seasonNum);
            }

            void addEpisode(Episode episode) {
                if (trackedEpisodes == null)
                    trackedEpisodes = new ArrayList<>();
                if (!trackedEpisodes.contains(episode))
                    trackedEpisodes.add(episode);
            }
        }

        void removeSeason(int seasonNum) {
            ArrayList<Season> tempSeasons = new ArrayList<>(trackedSeasons);
            for (Season s : tempSeasons) {
                if (s.seasonNum == seasonNum) {
                    trackedSeasons.remove(s);
                }
            }
        }
    }


    //-------Trending Movie--------
    static class trendingMovie {
        String id;
        String poster_path;
        Float vote_average;
    }

    //-------Trending TvShow--------
    static class trendingTvShow
    {
        String id;
        String poster_path;
        Float vote_average;
    }

    //-------Last Search Type--------
    static enum SearchType {
        Movie,
        TV
    }
}
