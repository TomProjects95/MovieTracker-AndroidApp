package com.agiledev.agiledevapp;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class issueDialogFragment extends DialogFragment
{
    private static final String TAG = "issueDialogFragment";

    //wigets
    private Button mActionSubmit, mActionCancel;
    public EditText mName, mEmail, mMessage;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.dialogfrag_issue, container, false);
        mActionSubmit = view.findViewById(R.id.issueSubmitbutton);
        mActionCancel = view.findViewById(R.id.issuecancelbutton);
        mMessage = view.findViewById(R.id.issuemessagetext);
        sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        final AlertDialog noTextDialog = SimpleDialog.create(DialogOption.OkOnlyDismiss, getActivity(),
                "Error!", "Issue needs to have text!");

        mActionCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                getDialog().dismiss();
                mMessage.setText("");
            }
        });

        mActionSubmit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(mMessage.getText().toString().length() < 5)
                {
                    noTextDialog.show();
                }
                else
                {
                    String username = sharedPref.getString(getString(R.string.prefs_loggedin_username), null);
                    final String messageS = mMessage.getText().toString();
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("UserDetails").document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot doc = task.getResult();
                                if (doc != null && doc.exists()) {
                                    final String email = doc.get("email").toString();
                                    db.collection("Feedback").document(email).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            HashMap<String, Object> feedbackIssuesMap = new HashMap<>();
                                            HashMap<String, ArrayList<String>> issueMap = new HashMap<>();
                                            ArrayList<String> issueMessages = new ArrayList<>();
                                            if (documentSnapshot.exists()) {
                                                feedbackIssuesMap = (HashMap<String, Object>)documentSnapshot.getData();
                                                issueMap = (HashMap<String, ArrayList<String>>)feedbackIssuesMap.get("Feedback");
                                                if (issueMap.get("Issues") != null)
                                                {
                                                    issueMessages = issueMap.get("Issues");
                                                }
                                            }
                                            issueMessages.add(messageS);
                                            issueMap.put("Issues", issueMessages);
                                            feedbackIssuesMap.put("Feedback", issueMap);

                                            db.collection("Feedback").document(email).set(feedbackIssuesMap);
                                        }
                                    });
                                }
                            }
                        }
                    });
                    dismiss();
                    final AlertDialog dialog = SimpleDialog.create(DialogOption.OkOnlyDismiss, getActivity(),
                            "Report Sent.", "Thank you for sending Issue report");
                    dialog.show();
                }
                mMessage.setText("");
            }

        });
        return view;
    }
}