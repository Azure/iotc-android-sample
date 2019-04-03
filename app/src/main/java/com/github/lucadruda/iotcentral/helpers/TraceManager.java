package com.github.lucadruda.iotcentral.helpers;

import android.graphics.Color;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

public class TraceManager {

    private TextView view;

    public TraceManager(TextView view) {
        this.view = view;
    }

    public void trace(String data) {
        view.append(data + "\n");
    }

    public void err(String data) {
        colored(data, Color.RED);
    }

    public void warn(String data) {
        colored(data, Color.YELLOW);
    }

    public void info(String data) {
        colored(data, Color.GREEN);
    }

    private void colored(String data, int color) {
        int start = view.getText().length();
        view.append(data);
        int end = view.getText().length();

        Spannable spannableText = (Spannable) view.getText();
        spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
    }
}
