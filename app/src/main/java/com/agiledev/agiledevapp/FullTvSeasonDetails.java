package com.agiledev.agiledevapp;

import java.util.ArrayList;

public class FullTvSeasonDetails {
    private String air_date;
    private ArrayList<Episode> episodes;
    private String name;
    private String overview;
    private String seriesId;
    private String poster_path;
    private String season_number;

    public FullTvSeasonDetails(String air_date, ArrayList<Episode> episodes, String name, String overview, String seriesId, String poster_path, String season_number) {
        this.air_date = air_date;
        this.episodes = episodes;
        this.name = name;
        this.overview = overview;
        this.seriesId = seriesId;
        this.poster_path = poster_path;
        this.season_number = season_number;
    }

    public String getAir_date() {
        return air_date;
    }

    public ArrayList<Episode> getEpisodes() {
        return episodes;
    }

    public String getName() {
        return name;
    }

    public String getOverview() {
        return overview;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public String getSeason_number() {
        return season_number;
    }

    public class Episode {
        private String air_date;
        private int episode_number;
        private String name;
        private String overview;
        private String id;
        private int season_number;
        private String still_path;
        private float vote_average;
        private int vote_count;
        private String show_id;

        public String getAir_date() {
            return air_date;
        }
        public int getEpisode_number() {
            return episode_number;
        }
        public String getName() {
            return name;
        }
        public String getOverview() {
            return overview;
        }
        public String getId() {
            return id;
        }
        public int getSeason_number() {
            return season_number;
        }
        public String getStill_path() {
            return still_path;
        }
        public float getVote_average() {
            return vote_average;
        }
        public int getVote_count() {
            return vote_count;
        }
        public String getShow_id() {
            return show_id;
        }
    }
}
