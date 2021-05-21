package com.agiledev.agiledevapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.res.ResourcesCompat;

import static com.agiledev.agiledevapp.DialogOption.*;

public class SimpleDialog {

    public static AlertDialog create(DialogOption option, Context context, String title, String message) {
        final Context finalContext = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(finalContext);
        builder.setTitle(title);
        builder.setMessage(message);
        if(option == OkOnlyDismiss)
            builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

        if(option == YesCancel) {
            builder.setPositiveButton("Yes", null);
        }

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ResourcesCompat.getColor(finalContext.getResources(), R.color.colorPrimaryDark, null));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ResourcesCompat.getColor(finalContext.getResources(), R.color.colorPrimaryDark, null));
            }
        });

        if (option == YesCancel) {
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.dismiss();
                }
            });
        }

        return dialog;
    }
}
