package com.github.lucadruda.iotcentral.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

import com.github.lucadruda.iotcentral.Constants;
import com.github.lucadruda.iotcentral.R;

public class InputAlert {


    private AlertDialog.Builder builder;
    private AlertDialog inputAlert;
    private EditText input;

    public InputAlert(Context context, DialogInterface.OnClickListener onOK, DialogInterface.OnClickListener onCancel, String title) {
        this(context, onOK, onCancel);
        builder.setTitle(title);
        //builder.setCancelable(false);
    }

    public InputAlert(Context context, DialogInterface.OnClickListener onOK, DialogInterface.OnClickListener onCancel, String title, boolean cancelable) {
        this(context, onOK, onCancel, title);
        builder.setCancelable(cancelable);
    }

    public InputAlert(Context context, DialogInterface.OnClickListener onOK, DialogInterface.OnClickListener onCancel) {
        input = new EditText(context);
        input.setId(Constants.INPUTDIALOG_ID);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder = new AlertDialog.Builder(context);
        builder.setView(input);
        builder.setPositiveButton("OK", onOK);
        builder.setNegativeButton("Cancel", onCancel);
    }

    public void start() {
        if (inputAlert != null && inputAlert.isShowing()) {
            return;
        }
        inputAlert = builder.show();
    }

    void start(String title) {
        builder.setTitle(title);
        start();
    }

    public void start(String title, boolean cancelable) {
        builder.setCancelable(cancelable);
        start(title);
    }

    public void stop() {
        if (inputAlert != null && inputAlert.isShowing()) {
            inputAlert.dismiss();
        }
    }

    public boolean isStarted() {
        return inputAlert != null;
    }

    public String getText() {
        return input.getText().toString();
    }
}
