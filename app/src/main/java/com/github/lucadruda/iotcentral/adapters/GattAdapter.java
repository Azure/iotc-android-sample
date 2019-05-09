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
import com.github.lucadruda.iotcentral.helpers.GattPair;
import com.github.lucadruda.iotcentral.helpers.MappingStorage;
import com.github.lucadruda.iotcentral.service.types.Measure;
import com.github.lucadruda.iotcentral.targets.Targets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class GattAdapter extends BaseExpandableListAdapter {

    private LinkedHashMap<String, List<String>> services;
    private List<Measure> measures;
    private Activity context;
    private final String unknownServiceString;
    private final String unknownCharaString;
    private final MappingStorage storage;
    private HashMap<String, MeasureAdapter> mappings;


    public GattAdapter(Activity context, MappingStorage storage, HashMap<String, List<String>> services, List<Measure> measures) {
        this.context = context;
        this.services = new LinkedHashMap<>(services);
        this.mappings = new HashMap<>();
        this.measures = measures;
        this.storage = storage;
        this.measures.add(0, new Measure(MeasureAdapter.DEFAULT_TEXT_KEY, context.getResources().getString(R.string.select_telemetry), Measure.MeasureType.EVENT));
        for (String serviceUUID : this.services.keySet()) {
            for (String charUUID : this.services.get(serviceUUID)) {
                this.mappings.put(charUUID, new MeasureAdapter(context, android.R.id.text1, new ArrayList<Measure>(measures), new GattPair(serviceUUID, charUUID).getKey()));
            }
        }
        this.unknownServiceString = context.getResources().getString(R.string.unknown_service);
        this.unknownCharaString = context.getResources().getString(R.string.unknown_characteristic);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = mInflater.inflate(R.layout.gatt_service_item, null);
        String serviceUUID = (String) services.keySet().toArray()[groupPosition];
        TextView name = (TextView) convertView.findViewById(R.id.gatt_service_name);
        name.setText(Targets.servicelookup(serviceUUID).getName());
        TextView uuid = (TextView) convertView.findViewById(R.id.gatt_service_UUID);
        uuid.setText(serviceUUID);
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
        String serviceUUID = (String) services.keySet().toArray()[groupPosition];
        String characteristicUUID = services.get(serviceUUID).get(childPosition);
        // Save final reference to be able to access it in inner classes
        final View finalConvertView = convertView;

        // Set the item name
        TextView featureName = (TextView) convertView.findViewById(R.id.featureName);
        String name = Targets.featureslookup(characteristicUUID).getName();
        featureName.setText(name);

        TextView uuid = (TextView) convertView.findViewById(R.id.featureUUID);
        uuid.setText(characteristicUUID);

        Spinner measureSpinner = (Spinner) convertView.findViewById(R.id.telemetrySpinner);
        MeasureAdapter adapter = mappings.get(characteristicUUID);
        measureSpinner.setAdapter(adapter);
        measureSpinner.setOnItemSelectedListener(adapter.getOnItemSelectedListener());
        // already has a mapping for this char
        String iotcTelemetry = storage.getIoTCTelemetry(new GattPair(serviceUUID, characteristicUUID).getKey());
        if (iotcTelemetry != null) {
            measureSpinner.setSelection(adapter.getPosition(iotcTelemetry), true);
        }

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
        return services.get((String) getGroup(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return services.keySet().toArray()[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return services.get(getGroup(groupPosition)).get(childPosition);
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
