package com.github.lucadruda.iotcentral.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.lucadruda.iotcentral.R;
import com.github.lucadruda.iotcentral.service.Application;


public class AppAdapter extends BaseAdapter {
    private Context context;
    private final Application[] apps;

    public AppAdapter(Context context, Application[] apps) {
        this.context = context;
        this.apps = apps;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null) {

            gridView = new View(context);

            gridView = inflater.inflate(R.layout.item, null);

            TextView textView = (TextView) gridView
                    .findViewById(R.id.grid_item_label);
            textView.setText(apps[position].getName());


        } else {
            gridView = (View) convertView;
        }

        return gridView;
    }

    @Override
    public int getCount() {
        return apps.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }



}