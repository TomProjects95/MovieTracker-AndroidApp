package com.agiledev.agiledevapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;

import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceFragmentCompat;

import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


//public class SettingsFragment extends android.support.v4.app.Fragment
//{
//
//    View myView;
//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//        myView = inflater.inflate(R.layout.fragment_help, container, false);
//      //  PreferenceFragment.addPreferencesFromResource(R.xml.preferences);
//        return myView;
//    }

// extends PreferenceFragment
public class SettingsFragment extends PreferenceFragmentCompat
{
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    Map<String, String[]> currentUserDetails = new HashMap<String, String[]>();
    //   @Override
//    public void onCreate(Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//        addPreferencesFromResource(R.xml.preferences);
//}

    @Override
    public void onCreatePreferences(Bundle bundle, String s)
    {
        //      setPreferencesFromResource(R.xml.preferences, s);
        addPreferencesFromResource(R.xml.preferences);


        sharedPref = this.getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();


        final android.support.v7.preference.EditTextPreference userNamePreference = (android.support.v7.preference.EditTextPreference) findPreference("key_changeUsername");
        final android.support.v7.preference.EditTextPreference userEmailPreference = (android.support.v7.preference.EditTextPreference) findPreference("key_changeEmail");
        final android.support.v7.preference.EditTextPreference userPasswordPreference = (android.support.v7.preference.EditTextPreference) findPreference("key_changePassword");

        final CheckBoxPreference checkboxPref = (CheckBoxPreference) getPreferenceManager().findPreference("key_checkbox1");

        String currentUserName = getString(R.string.empty_string, sharedPref.getString(getString(R.string.prefs_loggedin_username),"Default value"));
        getCurrentDetails(db, currentUserName, userNamePreference, userEmailPreference); // firstly, fill the dialog pop outs with current username and email

        checkboxPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                if(!checkboxPref.isChecked())
                {
                    checkboxPref.setSummary("checked");
                } else {
                    checkboxPref.setSummary("unchecked");
                }
              //  Log.d("MyApp", "Pref " + preference.getKey() + " changed to " + newValue.toString());
                return true;
            }
        });

        userNamePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
             //   Log.d("MyApp", "Pref " + preference.getKey() + " changed to " + newValue.toString());

                 if(newValue.toString().isEmpty() || LoginRegisterActivity.usernameFound(newValue.toString()))
                 {
                     SimpleDialog.create(DialogOption.OkOnlyDismiss, getContext(), "Invalid Username", "The username you entered is empty!").show();
                     userNamePreference.setSummary("Please enter your new username");


                 } else {
                     SimpleDialog.create(DialogOption.OkOnlyDismiss, getContext(), "Valid Username", "The username changed successfully!").show();
                     userNamePreference.setSummary("New username: " + newValue.toString());
                     editor.remove("loggedInUsername");
                     editor.putString("loggedInUsername", newValue.toString());
                     editor.apply();
                 }
                return true;
            }
        });
//email
        userEmailPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                if(!TextUtils.isEmpty(newValue.toString()) && Patterns.EMAIL_ADDRESS.matcher(newValue.toString()).matches())
                {
                    SimpleDialog.create(DialogOption.OkOnlyDismiss, getContext(), "Valid email adress", "The email was changed successfully!").show();
                } else {
                    SimpleDialog.create(DialogOption.OkOnlyDismiss, getContext(), "Invalid email adress", "Please use valid email format!").show();
                }
                return true;
            }
        });




        final CheckBoxPreference checkboxPref2 = (CheckBoxPreference) getPreferenceManager().findPreference("key_checkbox_newEpisode");
        final CheckBoxPreference checkboxPref3 = (CheckBoxPreference) getPreferenceManager().findPreference("key_checkbox_trailerN");

        //username string, details string array. array contains current password, email and dob.



        //      String[] userdetails = {"pass", "email1", "Dob"};
        //     currentUserDetails.put(currentUserName, userdetails);

        //      userNamePreference.setText(currentUserDetails.get(currentUserName)[1]);


        //     Log.e("Saved User22", "Username: " +currentUserName + " | email: " + currentUserDetails.get(currentUserName)[1]);
//        userEmailPreference.setText(currentUserDetails.get(currentUserName)[1]);
//        userPasswordPreference.setText(currentUserDetails.get(currentUserName)[0]);

        //     Map<String, String[]> currentUserDetails = new Map<String, String[]>();

        //default text of change username pop up settings is the current username
        //  userNamePreference.setText(getString(R.string.empty_string, sharedPref.getString(getString(R.string.prefs_loggedin_username),"Default value")));
//        String newUsername = getString(R.string.empty_string, sharedPref.getString(getString(R.string.prefs_loggedin_username),"Default value"));
//        userNamePreference.setDefaultValue(userNamePreference);
//        editor.remove("loggedInUsername");
//        editor.putString("loggedInUsername", newUsername);
//        editor.apply();

        //    String newUsername = getString(R.string.empty_string, sharedPref.getString(getString(R.string.prefs_loggedin_username),"Default value"));
        //default value = current logged in as: "this"
/*
        userNamePreference.setDefaultValue(getString(R.string.empty_string, sharedPref.getString(getString(R.string.prefs_loggedin_username),"Default value")));
        editor.remove("loggedInUsername");
        editor.putString("loggedInUsername", userNamePreference.getText()); // logged in as = text of changeusername
        editor.apply();
*/
        //  checkboxPref.setSummary(newUsername);


        //     Map<String, Object> user = new HashMap<>();
        //      user.put("dob", txtDoB.getText().toString());
//        user.put("email", txtEmail.getText().toString());
//        user.put("password", hashedPass);
//
        //      registerUser(newUsername, user);

        //     checkboxPref.setSummary(userNamePreference.getText());


      //  checkboxPref.setOnPreferenceChangeListener(myCheckboxListener)


/*
        if(checkboxPref.isChecked())
        { // stay logged in settings checkbox
            // stay logged in
            checkboxPref.setSummary("checked");
        } else {
            checkboxPref.setSummary("unchecked");
         //   editor.remove("loggedIn");
         //   editor.putBoolean("loggedIn", false);
            // log the user off after exiting the app
        }
*/
        if(checkboxPref2.isChecked())
        { // new episode notifications
            // send the user notifications if new episode of currently watched show comes out
        } else {
            // do nothing, don't send any notification
        }

        if(checkboxPref3.isChecked())
        { // new trailer notifications
            // send the user notifications if new trailer of currently watched show comes out
        } else {
            // do nothing, don't send any notification
        }


        //  userEmailPreference.setText(getString(R.string.empty_string, sharedPref.getString(getString(R.string.prefs_loggedin_email),"Default value")));
        //  userEmailPreference.setText(getString(R.string.empty_string, sharedPref.getString(getString(R.string.prefs_loggedin_email),"Default value")));


        //   userNamePreference.setText(sharedPref.getString(String.valueOf(R.string.prefs_loggedin_username), "defautl value")); // default value is the current username
        //  userNamePreference.setDefaultValue(sharedPref.getString(String.valueOf(R.string.prefs_loggedin_username).toString(), "d"));



        //    textView.setText(getString(R.string.nav_loggedin_as, sharedPref.getString(getString(R.string.prefs_loggedin_username),"Error, user not found!")));

        //  userNamePreference.setDefaultValue(sharedPref.getString(R.string.prefs_loggedin_username));


        //sharedPref.getString(getString(R.string.prefs_loggedin_username)
        //  sharedPref.getString(getString(R.string.prefs_loggedin_username))

        //   checkboxPref.setSummary(String.valueOf(changUss.getText().toString()));

        //   System.out.println(String.valueOf(changUss.getText().toString()));

        // checkboxPref.setSummary("suumma");
        // changUss.setSummary("suumma");

        //password goes into fb
        //to do username create onclick listener here, when accept is clicked new create new shared preference and replace/add new username from textfield string
        // loginergisteractivity.java = see
    }

    private synchronized void getCurrentDetails(FirebaseFirestore db, final String currentUserName, final android.support.v7.preference.EditTextPreference userNamePref, final android.support.v7.preference.EditTextPreference emailPref)
    {
        //     String currentUserName = getString(R.string.empty_string, sharedPref.getString(getString(R.string.prefs_loggedin_username),"Default value"));
        db.collection("UserDetails").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots)
                {
                    if(document.getId().equals(currentUserName))
                    {
                        String userName = document.getId();
                        String[] userdetails = {document.getData().get("password").toString()
                                ,document.getData().get("email").toString()
                                ,document.getData().get("dob").toString()};
                        currentUserDetails.put(currentUserName, userdetails);

                        userNamePref.setText(userName);
                        emailPref.setText(currentUserDetails.get(currentUserName)[1]);



                    }
//
//                    Log.e("Found Username", document.getId());
//                    Log.e("Found Password", document.getData().get("password").toString());
//                    LoginRegisterActivity.User user = new LoginRegisterActivity.User();
//                    user.setUsername(document.getId());
//                    user.setPassword(document.getData().get("password").toString());
//                    userList.add(user);
//                    Log.e("Saved User", "Username: " + user.getUsername() + " | Password: " + user.getPassword());
//                    Log.e("-",":-");
                }
            }
        });

        //     return userDetails;
    }





}