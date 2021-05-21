package com.agiledev.agiledevapp;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by glees on 20/02/2019.
 */

public class CastDialog extends DialogFragment {
    public static String TAG = "CastDialog";
    public static int personID;
    public static MovieCastAdapter.Person person;
    public Toolbar toolbar;
    RelativeLayout pageContent;

    public static CastDialog newInstance(int id) {
        CastDialog fragment = new CastDialog();
        Bundle args = new Bundle();
        personID = id;
        fragment.setArguments(args);
        return fragment;
    }

    public synchronized void getPersonDetails(final View view){
        TmdbClient.getPersonDetails(personID, null, new JsonHttpResponseHandler() {
          @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
              person = new Gson().fromJson(response.toString(), MovieCastAdapter.Person.class);
              displayCastDetails(view);
          }

        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Dialog);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.cast_dialog_layout, container, false);
        getPersonDetails(view);



        pageContent = view.findViewById(R.id.castContent);

        toolbar = view.findViewById(R.id.castDialogTool_Bar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    protected synchronized void displayCastDetails(View view) {
        TextView toolbarTitle = view.findViewById(R.id.castName);
        ImageView personImage = view.findViewById(R.id.castPersonImage);
        TextView personName = view.findViewById(R.id.castPersonName);
        TextView personGender = view.findViewById(R.id.castPersonGender);
        TextView personKnownFor = view.findViewById(R.id.castPersonKnownFor);
        TextView personDOB = view.findViewById(R.id.castPersonDOB);
        TextView personDied = view.findViewById(R.id.castPersonDied);
        TextView personBio = view.findViewById(R.id.castPersonBio);

        toolbarTitle.setText(person.getName());

        TmdbClient.loadImage(getContext(), person.getProfile_path(), personImage, TmdbClient.imageType.ICON, "cast");

        SpannableString name = new SpannableString(person.getName());
        name.setSpan(new UnderlineSpan(), 0, name.length(), 0);
        personName.setText(name);

        personGender.setText(person.getGender() == 1 ? "Female" : "Male");

        String knownForText = "Known For - " + person.getKnown_for_department();
        personKnownFor.setText(knownForText);

        String bornText = "Born - " + (person.getBirthday() != null && !person.getBirthday().isEmpty() ? person.getBirthday() : "Unknown");
        personDOB.setText(bornText);

        if (person.getDeathday() != null) {
            String diedText = "Died - " + person.getDeathday();
            personDied.setText(diedText);
        }

        personBio.setText(person.getBiography());
    }
}
