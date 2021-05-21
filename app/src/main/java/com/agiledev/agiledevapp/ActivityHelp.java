package com.agiledev.agiledevapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class ActivityHelp extends AppCompatActivity
{
    private static final String TAG = "ActivityHelp";
    private Button mOpenDialog, ReportButton;
    private ImageButton imdbButton, firebaseButton;
    public FeedbackDialogFragment dialog = new FeedbackDialogFragment();
    public issueDialogFragment issueDialog = new issueDialogFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ReportButton = findViewById(R.id.ReportIssue);
        ReportButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                issueDialog.show(getFragmentManager(), "issueDialogFragment");
            }
        });

        mOpenDialog = findViewById(R.id.FeedbackButton);
        mOpenDialog.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                dialog.show(getSupportFragmentManager(), "FeedbackDialogFragment");
            }
        });

        imdbButton = findViewById(R.id.imdbButton);
        imdbButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://www.themoviedb.org/"));
                startActivity(intent);
            }
        });

        firebaseButton = findViewById(R.id.fireBaseButton);
        firebaseButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://firebase.google.com/"));
                startActivity(intent);
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        return true;
    }

}
