package com.github.lucadruda.iotcentral.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.lucadruda.iotcentral.IServiceCallback;
import com.github.lucadruda.iotcentral.service.types.Measure;

import java.util.HashMap;
import java.util.List;

public class MeasureAdapter extends ArrayAdapter<Measure> {

    Context context;
    final HashMap<String, Measure> availableMeasures;
    List<Measure> currentMeasures;
    //Measure firstElement;
    boolean isFirstTime;
    private BroadcastReceiver listener;
    private LocalBroadcastManager localBroadcastManager;
    private String currentKey;
    private String featureName;

    private final String MEASURE_DATASTORE_CHANGE = "MEASURE_DATASTORE_CHANGE";
    private final String MEASURE_DATASTORE_KEYTOREMOVE = "MEASURE_DATASTORE_KEYTOREMOVE";
    private final String MEASURE_DATASTORE_KEYTOADD = "MEASURE_DATASTORE_KEYTOADD";
    private final String MEASURE_TELEMETRY_ASSIGNED = "TELEMETRY_ASSIGNED";
    public static final String DEFAULT_TEXT_KEY = "$default";

    public MeasureAdapter(Context context, int textViewResourceId, List<Measure> measures, String name) {
        super(context, textViewResourceId, measures);
        this.context = context;
        this.featureName = name;
        this.availableMeasures = new HashMap<>();
        for (Measure measure : measures) {
            this.availableMeasures.put(measure.getFieldName(), measure);
        }
        currentMeasures = measures;
        this.isFirstTime = true;
        currentKey = DEFAULT_TEXT_KEY;
        listener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(MEASURE_DATASTORE_CHANGE)) {
                    String keyToremove = intent.getStringExtra(MEASURE_DATASTORE_KEYTOREMOVE);
                    String keyToadd = intent.getStringExtra(MEASURE_DATASTORE_KEYTOADD);
                    if (keyToremove != null) {
                        if (keyToremove.equals(currentKey)) {
                            // not removing current spinner
                            return;
                        }
                        Measure measureToRemove = availableMeasures.get(keyToremove);
                        remove(measureToRemove);
                    }
                    if (keyToadd != null) {
                        add(availableMeasures.get(keyToadd));
                    }
                    notifyDataSetChanged();
                }
            }
        };

        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.registerReceiver(listener, getReceiverFilter());
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        View view;
        view = View.inflate(context, android.R.layout.simple_spinner_dropdown_item, null);
        final TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(this.getItem(position).toString());

        textView.setTextColor(Color.BLACK);
        textView.setBackgroundColor(Color.WHITE);


        return view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(context, android.R.layout.simple_spinner_item, null);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(this.getItem(position).toString());
        return textView;
    }


    public AdapterView.OnItemSelectedListener getOnItemSelectedListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Measure item = (Measure) parent.getItemAtPosition(position);
                String key = item.getFieldName();
                Intent intent = new Intent(MEASURE_DATASTORE_CHANGE);
                if (currentKey.equals(key) && currentKey.equals(DEFAULT_TEXT_KEY)) {
                    return;
                } else if (currentKey.equals(DEFAULT_TEXT_KEY) && !key.equals(DEFAULT_TEXT_KEY)) {
                    intent.putExtra(MEASURE_DATASTORE_KEYTOREMOVE, key);
                } else if (!currentKey.equals(DEFAULT_TEXT_KEY) && key.equals(DEFAULT_TEXT_KEY)) {
                    intent.putExtra(MEASURE_DATASTORE_KEYTOADD, currentKey);
                } else {
                    intent.putExtra(MEASURE_DATASTORE_KEYTOREMOVE, key);
                    intent.putExtra(MEASURE_DATASTORE_KEYTOADD, currentKey);
                }
                currentKey = key;
                localBroadcastManager.sendBroadcast(intent);
                localBroadcastManager.sendBroadcast(new Intent(MEASURE_TELEMETRY_ASSIGNED));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }

    private IntentFilter getReceiverFilter() {
        final IntentFilter intentFilter = new IntentFilter(MEASURE_DATASTORE_CHANGE);
        return intentFilter;
    }


}