package com.github.lucadruda.iotcentral.helpers;

import android.app.AlertDialog;
import android.content.Context;

import com.github.lucadruda.iotcentral.R;

public class LoadingAlert {

    private AlertDialog.Builder builder;
    private AlertDialog loadingAlert;

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

    public boolean isStarted() {
        return loadingAlert != null;
    }
}
