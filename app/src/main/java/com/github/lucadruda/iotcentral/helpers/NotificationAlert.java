package com.github.lucadruda.iotcentral.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.github.lucadruda.iotcentral.R;

public class NotificationAlert {

    private AlertDialog.Builder builder;
    private AlertDialog notificationAlert;

    public NotificationAlert(Context context, String text) {
        builder = new AlertDialog.Builder(context);
        builder.setMessage(text);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //do things
            }
        });
    }


    public void show() {
        notificationAlert = builder.show();
    }

}
