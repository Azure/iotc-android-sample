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
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.github.lucadruda.iotcentral.helpers.MappingStorage;
import com.github.lucadruda.iotcentral.services.BLEService;
import com.github.lucadruda.iotcentral.service.types.Measure;

import java.util.HashMap;
import java.util.List;

public class MeasureAdapter extends ArrayAdapter<Measure> {

    Context context;
    final HashMap<String, Measure> availableMeasures;
    List<Measure> currentMeasures;
    //Measure firstElement;
    private boolean firstTime;
    private BroadcastReceiver listener;
    private LocalBroadcastManager localBroadcastManager;
    private String currentKey;
    private int currentPosition;
    private final String gattPair;

    private final String MEASURE_DATASTORE_CHANGE = "MEASURE_DATASTORE_CHANGE";
    private final String MEASURE_DATASTORE_KEYTOREMOVE = "MEASURE_DATASTORE_KEYTOREMOVE";
    private final String MEASURE_DATASTORE_KEYTOADD = "MEASURE_DATASTORE_KEYTOADD";

    public static final String DEFAULT_TEXT_KEY = "$default";

    public MeasureAdapter(Context context, int textViewResourceId, List<Measure> measures, String gattPair) {
        super(context, textViewResourceId, measures);
        this.context = context;
        this.firstTime = true;
        this.gattPair = gattPair;
        this.availableMeasures = new HashMap<>();
        for (Measure measure : measures) {
            this.availableMeasures.put(measure.getFieldName(), measure);
        }
        currentMeasures = measures;
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

        if (convertView == null) {
            convertView = View.inflate(context, android.R.layout.simple_spinner_dropdown_item, null);
        }
        ((CheckedTextView) convertView).setText(this.getItem(position).toString());

        ((CheckedTextView) convertView).setTextColor(Color.BLACK);
        ((CheckedTextView) convertView).setBackgroundColor(Color.WHITE);

        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, android.R.layout.simple_spinner_item, null);
        }
        if (position == 0 || currentPosition != position) {
            // view come back from scroll or dataset has been changed so position has different item
            ((TextView) convertView).setText(availableMeasures.get(currentKey).toString());
        } else {
            ((TextView) convertView).setText(getItem(position).toString());
            // set right position now. if dataset changes we restore this value
            currentPosition = position;
        }
        return convertView;
    }


    public AdapterView.OnItemSelectedListener getOnItemSelectedListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    return;
                }
                Measure item = (Measure) parent.getItemAtPosition(position);
                String key = item.getFieldName();
                Intent dataChangeIntent = new Intent(MEASURE_DATASTORE_CHANGE);
                Intent assignmentIntent = new Intent(BLEService.TELEMETRY_ASSIGNED);
                assignmentIntent.putExtra(BLEService.MEASURE_MAPPING_GATT_PAIR, gattPair);
                assignmentIntent.putExtra(BLEService.MEASURE_MAPPING_IOTC, key);
                if (currentKey.equals(key)) {
                    return;
                } else if (currentKey.equals(DEFAULT_TEXT_KEY) && !key.equals(DEFAULT_TEXT_KEY)) {
                    dataChangeIntent.putExtra(MEASURE_DATASTORE_KEYTOREMOVE, key);
                } else if (!currentKey.equals(DEFAULT_TEXT_KEY) && key.equals(DEFAULT_TEXT_KEY)) {
                    dataChangeIntent.putExtra(MEASURE_DATASTORE_KEYTOADD, currentKey);
                } else {
                    dataChangeIntent.putExtra(MEASURE_DATASTORE_KEYTOREMOVE, key);
                    dataChangeIntent.putExtra(MEASURE_DATASTORE_KEYTOADD, currentKey);
                }
                currentKey = key;
                // force view to refresh since this is called after getview and position might be different
                ((TextView) view).setText(availableMeasures.get(currentKey).toString());
                localBroadcastManager.sendBroadcast(dataChangeIntent);
                localBroadcastManager.sendBroadcast(assignmentIntent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }


    public int getPosition(String fieldName) {
        Measure measure = availableMeasures.get(fieldName);
        return this.getPosition(measure);
    }

    private IntentFilter getReceiverFilter() {
        final IntentFilter intentFilter = new IntentFilter(MEASURE_DATASTORE_CHANGE);
        return intentFilter;
    }


}