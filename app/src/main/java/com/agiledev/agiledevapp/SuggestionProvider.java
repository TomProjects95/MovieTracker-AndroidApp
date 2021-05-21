package com.agiledev.agiledevapp;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by glees on 20/02/2019.
 */

public class SuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.agiledev.agiledevapp.SuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
