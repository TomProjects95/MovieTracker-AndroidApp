package com.agiledev.agiledevapp;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static android.view.Gravity.CENTER_VERTICAL;

public class SearchResultsActivity extends AppCompatActivity {

    ProgressBar spinner;
    RecyclerView recyclerView;
    SearchResultsAdapter adapter;
    ItemTouchHelper itemTouchHelper;
    List<BasicMovieDetails> movies = new ArrayList<>();
    List<BasicTvShowDetails> tvshows = new ArrayList<>();
    String searchPhrase = "";
    View v;
    LinearLayout searchResults;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        v = this.getWindow().getDecorView().findViewById(android.R.id.content);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        recyclerView = findViewById(R.id.search_recycler_view);
        spinner = findViewById(R.id.searchLoadingSpinner);
        searchResults = findViewById(R.id.searchResults);

        handleIntent(getIntent());

        if (Globals.getLastSearchType() == Globals.SearchType.Movie) {
            searchMovieByTitle(searchPhrase, this);
        } else if (Globals.getLastSearchType() == Globals.SearchType.TV) {
            searchTvByTitle(searchPhrase);
        }

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        final Spinner spinner = (Spinner)menu.findItem(R.id.type).getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.media_type, R.layout.app_bar_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (Globals.getLastSearchType() == Globals.SearchType.Movie) {
            spinner.setSelection(adapter.getPosition("Movie"));
        } else if (Globals.getLastSearchType() == Globals.SearchType.TV) {
            spinner.setSelection(adapter.getPosition("TV"));
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Globals.setLastSearchType(Globals.SearchType.valueOf(spinner.getSelectedItem().toString()));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        LinearLayout layout = (LinearLayout)searchView.getChildAt(0);
        layout.addView(spinner);
        layout.setGravity(CENTER_VERTICAL);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        return true;
    }

    protected void onNewIntent(Intent intent) {
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchPhrase = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
            suggestions.saveRecentQuery(searchPhrase, null);
        }
    }

    protected synchronized void searchMovieByTitle(String title, final Activity mActivity) {
        TmdbClient.searchMoviesByQuery(title,null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray results = new JSONArray();
                try {
                    results = response.getJSONArray("results");
                } catch (JSONException e) {
                    Log.e("JSON Error", e.getMessage());
                    if(e.getMessage().equals("No value for Search")) {
                        final Snackbar noResults = Snackbar.make(findViewById(R.id.searchResultsLayout), "No results found.", Snackbar.LENGTH_INDEFINITE);
                        noResults.setActionTextColor(ContextCompat.getColor(getBaseContext(),R.color.colorPrimary)).setAction("Dismiss", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                noResults.dismiss();
                            }
                        });
                        noResults.show();
                    }
                }
                for (int i = 0; i < results.length(); i++) {
                    try {
                        Log.e("Results:", results.get(i).toString());
                        BasicMovieDetails movie = new Gson().fromJson(results.get(i).toString(), BasicMovieDetails.class);
                        movies.add(movie);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapter = new SearchResultsAdapter(getBaseContext(), mActivity, v, movies, getSupportFragmentManager(), "Movie");
                spinner.setVisibility(View.GONE);
                recyclerView.setAdapter(adapter);
                itemTouchHelper = new ItemTouchHelper(new SwipeToTrackCallback(adapter, getBaseContext(), movies, MediaTracking.Media.MOVIE, sharedPref.getString(getString(R.string.prefs_loggedin_username), null)));
                itemTouchHelper.attachToRecyclerView(recyclerView);
                searchResults.setVisibility(View.VISIBLE);
            }
        });
    }

    protected synchronized void searchTvByTitle(String title) {
        TmdbClient.searchTvByQuery(title, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray results = new JSONArray();
                try {
                    results = response.getJSONArray("results");
                } catch (JSONException e) {
                    Log.e("JSON Error", e.getMessage());
                    if (e.getMessage().equals("No value for Search")) {
                        final Snackbar noResults = Snackbar.make(findViewById(R.id.searchResultsLayout), "No results found.", Snackbar.LENGTH_INDEFINITE);
                        noResults.setActionTextColor(ContextCompat.getColor(getBaseContext(),R.color.colorPrimary)).setAction("Dismiss", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                noResults.dismiss();
                            }
                        });
                        noResults.show();
                    }
                }
                for (int i = 0; i < results.length(); i++) {
                    try {
                        Log.e("Results:", results.get(i).toString());
                        BasicTvShowDetails tv = new Gson().fromJson(results.get(i).toString(), BasicTvShowDetails.class);
                        tvshows.add(tv);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapter = new SearchResultsAdapter(getBaseContext(), getParent(), v, tvshows, getSupportFragmentManager(), "TV");
                spinner.setVisibility(View.GONE);
                recyclerView.setAdapter(adapter);
                itemTouchHelper = new ItemTouchHelper(new SwipeToTrackCallback(adapter, getBaseContext(), tvshows, MediaTracking.Media.TV, sharedPref.getString(getString(R.string.prefs_loggedin_username), null)));
                itemTouchHelper.attachToRecyclerView(recyclerView);
                searchResults.setVisibility(View.VISIBLE);
            }
        });
    }
}