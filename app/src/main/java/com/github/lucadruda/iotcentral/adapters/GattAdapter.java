package com.github.lucadruda.iotcentral.adapters;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.lucadruda.iotcentral.IoTCentral;
import com.github.lucadruda.iotcentral.R;
import com.github.lucadruda.iotcentral.bluetooth.SampleGattAttributes;
import com.github.lucadruda.iotcentral.service.types.Measure;

import java.util.ArrayList;
import java.util.List;

public class GattAdapter extends BaseExpandableListAdapter {

    private List<BluetoothGattService> services;
    private List<Measure> measures;
    private Activity context;
    private final String unknownServiceString;
    private final String unknownCharaString;


    public GattAdapter(Activity context, List<BluetoothGattService> services, List<Measure> measures) {
        this.context = context;
        this.services = services;
        this.measures = measures;
        this.measures.add(0, new Measure(MeasureAdapter.DEFAULT_TEXT_KEY, context.getResources().getString(R.string.select_telemetry), Measure.MeasureType.EVENT));
        this.unknownServiceString = context.getResources().getString(R.string.unknown_service);
        this.unknownCharaString = context.getResources().getString(R.string.unknown_characteristic);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = mInflater.inflate(R.layout.gatt_service_item, null);
        TextView name = (TextView) convertView.findViewById(R.id.gatt_service_name);
        name.setText(SampleGattAttributes.lookup(services.get(groupPosition).getUuid().toString(), unknownServiceString));
        TextView uuid = (TextView) convertView.findViewById(R.id.gatt_service_UUID);
        uuid.setText(services.get(groupPosition).getUuid().toString());
      /*  if(isExpanded){
            for (BluetoothGattCharacteristic characteristic : services.get(groupPosition).getCharacteristics()){

            }
        }*/
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.gatt_char_item, null);
        }
        BluetoothGattCharacteristic characteristic = services.get(groupPosition).getCharacteristics().get(childPosition);
        // Save final reference to be able to access it in inner classes
        final View finalConvertView = convertView;

        // Set the item name
        TextView featureName = (TextView) convertView.findViewById(R.id.featureName);
        String name = SampleGattAttributes.lookup(characteristic.getUuid().toString(), unknownServiceString);
        featureName.setText(name);

        TextView uuid = (TextView) convertView.findViewById(R.id.featureUUID);
        uuid.setText(characteristic.getUuid().toString());

        Spinner measureSpinner = (Spinner) convertView.findViewById(R.id.telemetrySpinner);
        MeasureAdapter adapter = new MeasureAdapter(context, android.R.id.text1, new ArrayList<Measure>(measures), name);
        measureSpinner.setAdapter(adapter);
        measureSpinner.setOnItemSelectedListener(adapter.getOnItemSelectedListener());
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public int getGroupCount() {
        return services.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return services.get(groupPosition).getCharacteristics().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return services.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return services.get(groupPosition).getCharacteristics().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
