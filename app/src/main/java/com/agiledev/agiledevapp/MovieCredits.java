package com.agiledev.agiledevapp;

import java.util.ArrayList;

/**
 * Created by t7037453 on 28/02/19.
 */

public class MovieCredits {

    private String id;
    private ArrayList<Cast> cast;

    public MovieCredits(String id, ArrayList<Cast> cast) {
        this.id = id;
        this.cast = cast;
    }

    public class Cast {
        private int cast_id;
        private String character;
        private String credit_id;
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

        public String getCredit_id() {
            return credit_id;
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

    public String getId() {
        return id;
    }

    public ArrayList<Cast> getCast() {
        return cast;
    }
}
