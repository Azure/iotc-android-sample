package com.github.lucadruda.iotcentral.helpers;

import android.graphics.Color;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

public class TraceManager {

    private static final int MAX_LENGTH = 1000;
    private TextView view;

    public TraceManager(TextView view) {
        this.view = view;
        this.view.setMaxLines(MAX_LENGTH);
    }

    public void trace(String data) {
        colored(data, 0);
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
        view.append(data + "\n");
        int end = view.getText().length();
        if (color == 0) {
            return;
        }
        Spannable spannableText = (Spannable) view.getText();
        spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
    }
}
