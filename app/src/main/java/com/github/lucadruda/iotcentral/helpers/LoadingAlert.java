package com.github.lucadruda.iotcentral.helpers;

import android.app.AlertDialog;
import android.content.Context;

import com.github.lucadruda.iotcentral.R;

public class LoadingAlert {

    private AlertDialog.Builder builder;
    private AlertDialog loadingAlert;

    public LoadingAlert(Context context, String text) {
        builder = new AlertDialog.Builder(context);
        builder.setView(R.layout.loading);
        builder.setMessage(text);
        //builder.setCancelable(false);
    }

    public LoadingAlert(Context context, String text, boolean cancelable) {
        this(context, text);
        builder.setCancelable(cancelable);
    }

    public void start() {
        loadingAlert = builder.show();
    }

    public void start(String text) {
        builder.setMessage(text);
        loadingAlert = builder.show();
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
