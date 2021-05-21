package com.agiledev.agiledevapp;

import java.util.ArrayList;

/**
 * Created by glees on 13/02/2019.
 */

public class FullMovieDetails {

    private boolean adult;
    private String backdrop_path;
    private ArrayList<Genre> genres;
    private String imdb_id;
    private String original_language;
    private String overview;
    private String poster_path;
    private ArrayList<ProductionCompanies> production_companies;
    private String release_date;
    private int runtime;
    private String status;
    private String tagline;
    private String title;
    private String id;

    private Videos videos;

    private Credits credits;

    public FullMovieDetails() {}

    public FullMovieDetails(boolean adult, String backdrop_path, ArrayList<Genre> genres, String imdb_id, String original_language, String overview, String poster_path, ArrayList<ProductionCompanies> production_companies, String release_date, int runtime, String status, String tagline, String title, Videos videos, String id) {
        this.adult = adult;
        this.backdrop_path = backdrop_path;
        this.genres = genres;
        this.imdb_id = imdb_id;
        this.original_language = original_language;
        this.overview = overview;
        this.poster_path = poster_path;
        this.production_companies = production_companies;
        this.release_date = release_date;
        this.runtime = runtime;
        this.status = status;
        this.tagline = tagline;
        this.title = title;
        this.videos = videos;
        this.id = id;
    }

    public boolean isAdult() {
        return adult;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public ArrayList<Genre> getGenres() {
        return genres;
    }

    public String getImdb_id() {
        return imdb_id;
    }

    public String getOriginal_language() {
        return original_language;
    }

    public String getOverview() {
        return overview;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public ArrayList<ProductionCompanies> getProduction_companies() {
        return production_companies;
    }

    public String getRelease_date() {
        return release_date;
    }

    public int getRuntime() {
        return runtime;
    }

    public String getStatus() {
        return status;
    }

    public String getTagline() {
        return tagline;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<Video> getVideos() {
        return videos.getResults();
    }

    public String getId() { return id; }

    public ArrayList<Cast> getCast() {
        return credits.getCast();
    }

    public class Genre {
        int id;
        String name;
    }

    public class ProductionCompanies {
        int id;
        String name;
        String logo_path;
        String origin_country;
    }

    public class Videos {
        ArrayList<Video> results;

        public ArrayList<Video> getResults() {
            return results;
        }
    }
    public class Video {
        private String id;
        private String key;
        private String name;
        private String site;
        private int size;
        private String type;

        public String getId() {
            return id;
        }
        public String getKey() {
            return key;
        }
        public String getName() {
            return name;
        }
        public String getSite() {
            return site;
        }
        public int getSize() {
            return size;
        }
        public String getType() {
            return type;
        }
    }

    public class Credits {
        ArrayList<Cast> cast;

        public ArrayList<Cast> getCast() {
            return cast;
        }
    }
    public class Cast {
        private int cast_id;
        private String character;
        private int gender;
        private int id;
        private String name;
        private String profile_path;

        public int getCast_id() {
            return cast_id;
        }
        public String getCharacter() {
            return character;
        }
        public int getGender() {
            return gender;
        }
        public int getId() {
            return id;
        }
        public String getName() {
            return name;
        }
        public String getProfile_path() {
            return profile_path;
        }
    }

    public String getGenresString() {
        ArrayList<Genre> genres = getGenres();
        StringBuilder returnString = new StringBuilder();
        for (int i = 0; i < genres.size(); i++) {
            returnString.append(genres.get(i).name);
            if (i != genres.size() - 1)
                returnString.append(" / ");
        }
        return returnString.toString();
    }
}
