package com.agiledev.agiledevapp;

import java.util.ArrayList;

public class BasicMovieDetails {

    private String title;
    private String release_date;
    private String poster_path;
    private ArrayList<Integer> genre_ids;
    private ArrayList<String> genre_names;
    private String id;
    private Float vote_average;

    public BasicMovieDetails() {
    }

    public BasicMovieDetails(String title, String releasedate, String poster_path, String id, Float vote_average) {
        this.title = title;
        this.release_date = releasedate;
        this.poster_path = poster_path;
        this.id = id;
        this.vote_average = vote_average;
    }

    public String getTitle() {
        return title;
    }

    public String getRelease_date() {
        return release_date;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public String getId() {
        return id;
    }

    public Float getVote_average() { return vote_average/2; }

    public ArrayList<Integer> getGenre_ids() {
        return genre_ids;
    }

    public String getGenreNames() {
        ArrayList<Integer> ids = getGenre_ids();
        ArrayList<String> names = new ArrayList<>();
        for (int i : ids) {
            names.add(Globals.getMovieGenreTags().get(i));
        }
        String returnString = names.toString();
        returnString = returnString.replace("[","");
        returnString = returnString.replace("]","");

        return returnString;
    }
}
