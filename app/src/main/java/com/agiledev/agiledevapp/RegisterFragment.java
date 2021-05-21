package com.agiledev.agiledevapp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.agiledev.agiledevapp.LoginRegisterActivity.logIn;

public class RegisterFragment extends Fragment implements View.OnClickListener, View.OnTouchListener, View.OnFocusChangeListener {

    private EditText txtUsername, txtPassword, txtName, txtEmail, txtDoB;
    private View v;
    final Calendar calendar = Calendar.getInstance();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v =  inflater.inflate(R.layout.fragment_register, container, false);

        sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        Button button = v.findViewById(R.id.btnRegister);
        button.setOnClickListener(this);

        txtUsername = v.findViewById(R.id.txtNewUsername);
        txtPassword = v.findViewById(R.id.txtNewPassword);
        txtEmail = v.findViewById(R.id.txtNewEmail);
        txtDoB = v.findViewById(R.id.txtNewDOB);

        txtUsername.setOnFocusChangeListener(this);
        txtEmail.setOnFocusChangeListener(this);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                updateDateLabel();
            }
        };

        txtDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getContext(), R.style.Theme_AppCompat_DayNight_Dialog, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        LinearLayout layout = v.findViewById(R.id.registerlayout);
        layout.setOnTouchListener(this);

        return v;
    }

    private void updateDateLabel() {
        String dateFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        txtDoB.setText(sdf.format(calendar.getTime()));
    }

    private boolean isValidEmail(CharSequence text) {
        return (!TextUtils.isEmpty(text) && Patterns.EMAIL_ADDRESS.matcher(text).matches());
    }

    @Override
    public void onClick(View view) {
        if(!TextUtils.isEmpty(txtUsername.getText()) || !TextUtils.isEmpty(txtPassword.getText()) || !TextUtils.isEmpty(txtEmail.getText()) || !TextUtils.isEmpty(txtDoB.getText())) {
            if (LoginRegisterActivity.usernameFound(txtUsername.getText().toString())) {
                SimpleDialog.create(DialogOption.OkOnlyDismiss, view.getContext(), "Invalid Username", "The username you entered is already in use!").show();
            } else {
                String hashedPass = hash(txtPassword.getText().toString());

                Map<String, Object> user = new HashMap<>();
                user.put("dob", txtDoB.getText().toString());
                user.put("email", txtEmail.getText().toString());
                user.put("password", hashedPass);
                user.put("join_date", new Timestamp(new Date()));
                user.put("timeWatched", 0);
                user.put("genresWatched", new HashMap<String, Long>());

                registerUser(txtUsername.getText().toString(), user);

                logIn(txtUsername.getText().toString().trim(), getContext());

                getRecentMovies();
            }
        }
    }

    private void registerUser(String username, Map<String, Object> user)  {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("UserDetails").document(username).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Success", "DocumentSnapshot successfully written!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("Error", "Error writing document", e);
            }
        });
    }

    public static String hash(String md5) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes(StandardCharsets.UTF_8));
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; i++) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        CloseKeyboard.hideKeyboard(getActivity());
        return true;
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (!b) {
            switch (view.getId()) {
                case R.id.txtNewUsername:
                    if (TextUtils.isEmpty(txtUsername.getText()) || LoginRegisterActivity.usernameFound(txtUsername.getText().toString().trim())) {
                        txtUsername.setBackground(getResources().getDrawable(R.drawable.roundedtextbox_invalid));
                    } else {
                        txtUsername.setBackground(getResources().getDrawable(R.drawable.roundedtextbox));
                    }
                    break;
                case R.id.txtNewEmail:
                    if (TextUtils.isEmpty(txtEmail.getText()) || !isValidEmail(txtEmail.getText())) {
                        txtEmail.setBackground(getResources().getDrawable(R.drawable.roundedtextbox_invalid));
                    } else {
                        txtEmail.setBackground(getResources().getDrawable(R.drawable.roundedtextbox));
                    }
                    break;
            }
        }
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
                            movie.name = (String)field.get("name");
                            movie.poster_path = (String)field.get("poster_path");
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
