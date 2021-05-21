package com.agiledev.agiledevapp;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import static android.view.Gravity.CENTER_VERTICAL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FragmentManager fragmentManager = getSupportFragmentManager();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    public static final int PERMS_LOCATION = 1;
    public static Boolean locationBool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            return;
        }

        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        TmdbClient.key = getResources().getString(R.string.tmdb_api_key);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        System.out.println();
        final TextView logout = findViewById(R.id.nav_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        TextView textView = navigationView.getHeaderView(0).findViewById(R.id.loggedInUser);
        textView.setText(getString(R.string.nav_loggedin_as, sharedPref.getString(getString(R.string.prefs_loggedin_username),"Error, user not found!")));

        fragmentManager.beginTransaction().replace(R.id.content_frame,new HomeFragment()).commit();

    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public PermissionCallback callback;

    public void setCallback(PermissionCallback callback) {
        this.callback = callback;
    }

    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMS_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Granted
                    Toast.makeText(this,"Location Permission Granted", Toast.LENGTH_LONG).show();
                    locationBool = true;

                    if (callback != null) {
                        callback.onPermissionGranted();
                        callback = null;
                    }
                } else {
                    //Denied
                    Toast.makeText(this,"Location Permission Denied", Toast.LENGTH_LONG).show();
                    locationBool = false;

                    if (callback != null) {
                        callback.onPermissionDenied();
                        callback = null;
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        final Spinner spinner = (Spinner)menu.findItem(R.id.type).getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.media_type, R.layout.app_bar_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinner.getSelectedItem().equals("TV Show")) {
                    Globals.setLastSearchType(Globals.SearchType.TV);
                } else if (spinner.getSelectedItem().equals("Movie")) {
                    Globals.setLastSearchType(Globals.SearchType.Movie);
                }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void removeAllFragments(FragmentManager fragmentManager) {
        while (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            //finish();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame
                            ,new HomeFragment())
                    .commit();

        } else if (id == R.id.nav_profile) {
            Bundle bundle = new Bundle();
            bundle.putString("username", sharedPref.getString(getString(R.string.prefs_loggedin_username), null));
            ProfileFragment fragment = new ProfileFragment();
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame
                            ,fragment)
                    .commit();

        } else if (id == R.id.nav_movies) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame
                            ,new MovieFragment())
                    .commit();

        } else if (id == R.id.nav_tv) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame
                            ,new TvShowFragment())
                    .commit();

        } else if (id == R.id.nav_help) {
            removeAllFragments(fragmentManager);
            Intent intent = new Intent(this, ActivityHelp.class);
            startActivity(intent);

        } else if (id == R.id.nav_settings)
        {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame
                            ,new SettingsFragment())
                    .commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logout() {
        DialogInterface.OnClickListener dialogClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == DialogInterface.BUTTON_POSITIVE) {
                    editor.remove(getString(R.string.prefs_loggedin_username));
                    editor.remove(getString(R.string.prefs_loggedin_boolean));
                    editor.apply();

                    finishAffinity();
                    Intent intent = new Intent(getBaseContext(), LoginRegisterActivity.class);
                    getBaseContext().startActivity(intent);
                } else if (i == DialogInterface.BUTTON_NEGATIVE)
                    dialogInterface.dismiss();
            }
        };
        AlertDialog dialog = SimpleDialog.create(DialogOption.YesCancel, this,"Logout?", "Are you sure you want to logout?");
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", dialogClick);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", dialogClick);
        dialog.show();
    }
}
