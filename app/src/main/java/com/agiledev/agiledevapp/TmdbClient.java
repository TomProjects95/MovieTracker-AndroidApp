package com.agiledev.agiledevapp;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.*;

class TmdbClient {
    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    static String key;

    private static AsyncHttpClient client = new AsyncHttpClient();

//  API Calls  //

    /**
     * This method is used to return a JSONArray of duration from the API.
     */
    static void getMovieGenres(RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String url = getAbsoluteUrl("genre/movie/list?api_key=" + key);
        client.get(url, params, responseHandler);
    }

    static void getTvGenres(RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String url = getAbsoluteUrl("genre/tv/list?api_key=" + key);
        client.get(url, params, responseHandler);
    }

    /**
     * This method is used to return information about a specific movie, using the movie's ID as the specifier.
     *
     * @param movieID The ID of the movie whose details will be pulled from the API.
     */
    static void getMovieInfo(String movieID, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String url = getAbsoluteUrl("movie/" + movieID + "?api_key=" + key + "&append_to_response=videos,credits");
        client.get(url, params, responseHandler);
    }

    static void getTvShowInfo(String tvshowID, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String url = getAbsoluteUrl("tv/" + tvshowID + "?api_key=" + key + "&append_to_response=videos,credits");
        client.get(url, params, responseHandler);
    }
    /**
     * This method is used to return information about a specific tv show, using the tvshow's ID as the specifier.
     *
     * @param tvshowID The ID of the movie whose details will be pulled from the API.
     */
    static void getFullTvShowDetails(String tvshowID, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String url = getAbsoluteUrl("tv/" + tvshowID + "?api_key=" + key + "&append_to_response=videos,credits");
        client.get(url, params, responseHandler);
    }

    static void getTvSeasonDetails(String tvId, int seasonNum, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String url = getAbsoluteUrl("tv/"+ tvId + "/season/" + seasonNum + "?api_key=" + key);
        client.get(url, params, responseHandler);
    }

    static void getTvEpisodeDetails(String tvId, int seasonNum, int episodeNum, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String url = getAbsoluteUrl("tv/" + tvId + "/season/" + seasonNum + "/episode/" + episodeNum + "?api_key=" + key);
        client.get(url, params, responseHandler);
    }

    /**
     * This method is used to search the API for movies that have the specified query in their name.
     *
     * @param query The query to search for, usually the movie title.
     */
    static void searchMoviesByQuery(String query, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String url = getAbsoluteUrl("search/movie?api_key=" + key + "&query=" + query);
        client.get(url, params, responseHandler);
    }

    /**
     * This method is used to search the API for tv shows that have the specified query in their name.
     *
     * @param query The query to search for, usually the tv show title.
     */
    static void searchTvByQuery(String query, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String url = getAbsoluteUrl("search/tv?api_key=" + key + "&query=" + query);
        client.get(url, params, responseHandler);
    }

    /**
     * This method is used to pull information regarding a specific person, using their ID as the specifier.
     *
     * @param personID The ID of the person who's details should be queried.
     */
    static void getPersonDetails(int personID, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String url = getAbsoluteUrl("person/" + personID + "?api_key=" + key);
        client.get(url, params, responseHandler);
    }

    static void getweektrendingmovies(RequestParams params, AsyncHttpResponseHandler responseHandler)
    {
        String url = getAbsoluteUrl("trending/movie/week?api_key=" + key);
        client.get(url, params, responseHandler);
    }

    static void getweektrendingtvshows(RequestParams params, AsyncHttpResponseHandler responseHandler)
    {
        String url = getAbsoluteUrl("trending/tv/week?api_key=" + key);
        client.get(url, params, responseHandler);
    }

    static void getMovieImages(String movieId, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String url = getAbsoluteUrl("movie/" + movieId + "/images?api_key=" + key);
        client.get(url, params, responseHandler);
    }


    static void getMovieCast(String movieID, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String url = getAbsoluteUrl("movie/" + movieID + "/credits?api_key=" + key);
        client.get(url, params, responseHandler);
    }

    static void getTvCast(String tvshowID, RequestParams params, AsyncHttpResponseHandler responseHandler)
    {
        String url = getAbsoluteUrl("tv/" + tvshowID + "/credits?api_key=" + key);
        client.get(url, params, responseHandler);
    }


    static void getRelatedMovies(String movieId, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String url = getAbsoluteUrl("movie/" + movieId + "/similar?api_key=" + key);
        client.get(url, params, responseHandler);
    }

    static void getRelatedTV(String tvId, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String url = getAbsoluteUrl("tv/" + tvId + "/similar?api_key=" + key);
        client.get(url, params, responseHandler);
    }

    static void getPopularMoviesInRegion(String region, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String url = getAbsoluteUrl("movie/popular?api_key=" + key + "&region=" + region);
        client.get(url, params, responseHandler);
    }

    static void getPopularTvInRegion(String region, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String url = getAbsoluteUrl("tv/popular?api_key=" + key + "&region=" + region);
        client.get(url, params, responseHandler);
    }

    static void getRelatedTvshows(String genreString, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String url = getAbsoluteUrl("discover/tv?api_key=" + key + "&sort_by=popularity.desc&with_genres=" + genreString);
        client.get(url, params, responseHandler);
    }
    /**
     * @param relativeUrl The specific part of the url after the BASE_URL that you want to request from.
     * @return The BASE_URL concatenated with the relative url that was passed as a parameter.
     */
    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

//  Image Loading  //

    /**
     * This method loads the specified image into the specified ImageView.
     *
     * @param path The relative path of the image to load.
     * @param holder The ImageView to load the image into.
     */
    static void loadImage(Context mContext, String path, ImageView holder, imageType type, String usage) {
        switch(type) {
            case SMALLICON:
                if (usage.equals("cast")) {
                    Glide.with(mContext).load(mContext.getResources().getString(R.string.poster_icon_base_url_small) + path).placeholder(R.drawable.placeholder_med_cast).into(holder);
                } else if (usage.equals("movie")) {
                    Glide.with(mContext).load(mContext.getResources().getString(R.string.poster_icon_base_url_small) + path).placeholder(R.drawable.placeholder_med_movie).into(holder);
                }
                break;
            case ICON:
                if (usage.equals("cast")) {
                    Glide.with(mContext).load(mContext.getResources().getString(R.string.poster_icon_base_url) + path).placeholder(R.drawable.placeholder_med_cast).into(holder);
                } else if (usage.equals("movie")) {
                    Glide.with(mContext).load(mContext.getResources().getString(R.string.poster_icon_base_url) + path).placeholder(R.drawable.placeholder_med_movie).into(holder);
                }
                break;
            case LARGEICON:
                if (usage.equals("cast")) {
                    Glide.with(mContext).load(mContext.getResources().getString(R.string.poster_icon_base_url_large) + path).placeholder(R.drawable.placeholder_large_cast).into(holder);
                } else if (usage.equals("movie")) {
                    Glide.with(mContext).load(mContext.getResources().getString(R.string.poster_icon_base_url_large) + path).placeholder(R.drawable.placeholder_large_movie).into(holder);
                }
                break;
        }
    }

    public enum imageType {
        SMALLICON,
        ICON,
        LARGEICON
    }
}