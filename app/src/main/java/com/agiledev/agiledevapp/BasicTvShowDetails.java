package com.agiledev.agiledevapp;

import java.util.ArrayList;

public class BasicTvShowDetails {

    private String name;
    private String first_air_date;
    private String poster_path;
    private ArrayList<Integer> genre_ids;
    private ArrayList<String> genre_names;
    private String id;
    private Float vote_average;

    public BasicTvShowDetails() {
    }

    public BasicTvShowDetails(String name, String firstairdate, String poster_path, String id, Float vote_average) {
        this.name = name;
        this.first_air_date = firstairdate;
        this.poster_path = poster_path;
        this.id = id;
        this.vote_average = vote_average;
    }

    public String getName() {
        return name;
    }

    public String getFirst_air_date() {
        return first_air_date;
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
            names.add(Globals.getTvGenreTags().get(i));
        }
        String returnString = names.toString();
        returnString = returnString.replace("[","");
        returnString = returnString.replace("]","");

        return returnString;
    }
}
