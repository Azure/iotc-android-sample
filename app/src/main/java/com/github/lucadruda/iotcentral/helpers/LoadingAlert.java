package com.github.lucadruda.iotcentral.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

import com.github.lucadruda.iotcentral.R;

public class LoadingAlert {

    private AlertDialog.Builder builder;
    private AlertDialog loadingAlert;
    private Context context;

    public LoadingAlert(Context context, String text) {
        this(context);
        builder.setMessage(text);
        //builder.setCancelable(false);
    }

    public LoadingAlert(Context context, String text, boolean cancelable) {
        this(context, text);
        builder.setCancelable(cancelable);
    }

    public LoadingAlert(Context context) {
        this.context = context;
        builder = new AlertDialog.Builder(context);
        builder.setView(R.layout.loading);
    }

    public void start() {
        if (loadingAlert != null && loadingAlert.isShowing()) {
            return;
        }
        loadingAlert = builder.show();
    }

    public void start(String text) {
        builder.setMessage(text);
        start();
    }

    public void start(String text, boolean cancelable) {
        builder.setCancelable(cancelable);
        start(text);
    }

    public void stop() {
        if (loadingAlert != null && loadingAlert.isShowing()) {
            loadingAlert.dismiss();
        }
    }

    public void stop(String message) {
        stop();
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT);
    }

    public boolean isStarted() {
        return loadingAlert != null;
    }
}
