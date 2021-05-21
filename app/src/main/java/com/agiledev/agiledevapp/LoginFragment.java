package com.agiledev.agiledevapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.agiledev.agiledevapp.LoginRegisterActivity.logIn;
import static com.agiledev.agiledevapp.LoginRegisterActivity.usernameFound;

public class LoginFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {

    private EditText txtUsername, txtPassword;
    private View v;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_login, container, false);

        sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        Button btnLogin = v.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        txtUsername = v.findViewById(R.id.txtUsername);
        txtPassword = v.findViewById(R.id.txtPassword);

        LinearLayout layout = v.findViewById(R.id.layoutLogin);
        layout.setOnTouchListener(this);

        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                if (txtUsername.getText() == null || txtUsername.getText().toString().trim().equals("")) {
                    Log.e("Invalid Field", "The username field is empty!");
                    SimpleDialog.create(DialogOption.OkOnlyDismiss, view.getContext(), "Invalid Username", "The username text field was left empty!").show();
                } else if (txtPassword.getText() == null || txtPassword.getText().toString().trim().equals("")) {
                    Log.e("Invalid Field", "The password field is empty!");
                    SimpleDialog.create(DialogOption.OkOnlyDismiss, view.getContext(), "Invalid Password", "The password text field was left empty!").show();
                } else {
                    if (!usernameFound(txtUsername.getText().toString().trim())) {
                        SimpleDialog.create(DialogOption.OkOnlyDismiss, view.getContext(), "Invalid Username", "The entered username was not found!").show();
                    } else {
                        if (passwordMatchesUsername()) {
                            logIn(txtUsername.getText().toString().trim(), getContext()); // Log In Success //
                            getRecentMovies();
                        } else {
                            SimpleDialog.create(DialogOption.OkOnlyDismiss, view.getContext(), "Invalid Password", "The password you entered was incorrect!").show();
                        }
                    }
                }
                break;
        }
    }

    private boolean passwordMatchesUsername() {
        boolean valid = false;
        for (LoginRegisterActivity.User u : LoginRegisterActivity.userList) {
            String hashedPass = RegisterFragment.hash(txtPassword.getText().toString());
            if (u.getUsername().equals(txtUsername.getText().toString().trim()) && u.getPassword().equals(hashedPass)) {
                valid = true;
            }
        }
        return valid;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        CloseKeyboard.hideKeyboard(getActivity());
        return true;
    }

    public synchronized void getRecentMovies() {
        final ArrayList<Globals.trackedMovie> movieList = new ArrayList<>();
        db.collection("TrackedMovies").document(sharedPref.getString(getString(R.string.prefs_loggedin_username), null)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Map<String, Object> movies = doc.getData();
                        for (Map.Entry<String, Object> entry : movies.entrySet()) {
                            Globals.trackedMovie movie = new Globals.trackedMovie();
                            movie.id = entry.getKey();
                            Map<String, Object> field = (Map)entry.getValue();
                            Timestamp timestamp = (Timestamp)field.get("date");
                            movie.date = timestamp.toDate();
                            movie.poster_path = (String)field.get("poster_path");
                            movie.name = (String)field.get("name");
                            HashMap<String, String> genreMap = (HashMap)field.get("genres");
                            for (HashMap.Entry<String, String> e : genreMap.entrySet()) {
                                movie.genres.put(Integer.parseInt(e.getKey()), e.getValue());
                            }
                            movieList.add(movie);
                        }
                        Globals.setTrackedMovies(movieList);
                        Collections.sort(Globals.getTrackedMovies());
                    }
                }
            }
        });
    }
}