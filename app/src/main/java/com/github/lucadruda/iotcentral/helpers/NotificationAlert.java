package com.github.lucadruda.iotcentral.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.github.lucadruda.iotcentral.R;

public class NotificationAlert {

    private AlertDialog.Builder builder;
    private AlertDialog notificationAlert;

    public NotificationAlert(Context context, String text) {
        this(context);
        builder.setMessage(text);
    }

    public NotificationAlert(Context context) {
        builder = new AlertDialog.Builder(context);
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

    public void show(String message) {
        builder.setMessage(message);
        this.show();
    }

    public void show(String message, DialogInterface.OnClickListener onClick) {
        builder.setPositiveButton("OK", onClick);
        this.show(message);
    }

}
