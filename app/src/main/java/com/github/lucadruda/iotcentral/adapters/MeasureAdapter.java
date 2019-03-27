package com.github.lucadruda.iotcentral.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.lucadruda.iotcentral.bluetooth.BLEService;
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

    private final String MEASURE_DATASTORE_CHANGE = "MEASURE_DATASTORE_CHANGE";
    private final String MEASURE_DATASTORE_KEYTOREMOVE = "MEASURE_DATASTORE_KEYTOREMOVE";
    private final String MEASURE_DATASTORE_KEYTOADD = "MEASURE_DATASTORE_KEYTOADD";
    public static final String DEFAULT_TEXT_KEY = "$default";

    public MeasureAdapter(Context context, int textViewResourceId, List<Measure> measures) {
        super(context, textViewResourceId, measures);
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        this.context = context;
        this.availableMeasures = new HashMap<>();
        for (Measure measure : measures) {
            this.availableMeasures.put(measure.getFieldName(), measure);
        }
        currentMeasures = measures;
        this.isFirstTime = true;
        //setDefaultText(defaultText);
        currentKey = DEFAULT_TEXT_KEY;
        listener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(MEASURE_DATASTORE_CHANGE)) {
                    String keyToremove = intent.getStringExtra(MEASURE_DATASTORE_KEYTOREMOVE);
                    String keyToadd = intent.getStringExtra(MEASURE_DATASTORE_KEYTOADD);
                    if (keyToremove != null) {
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
        context.registerReceiver(listener, getReceiverFilter());
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
       /* if (isFirstTime) {
            getItem()
            currentMeasures.set(0, firstElement);
            isFirstTime = false;
        }*/
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        notifyDataSetChanged();
        return getCustomView(position, convertView, parent);
    }


    public View getCustomView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        TextView label = (TextView) row.findViewById(android.R.id.text1);
        label.setText(this.getItem(position).toString());

        return row;
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }

    private IntentFilter getReceiverFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MEASURE_DATASTORE_CHANGE);
        return intentFilter;
    }


}