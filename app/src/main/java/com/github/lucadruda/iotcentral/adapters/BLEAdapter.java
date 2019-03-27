package com.github.lucadruda.iotcentral.adapters;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.lucadruda.iotcentral.R;

import java.util.ArrayList;

public class BLEAdapter extends RecyclerView.Adapter<BLEAdapter.DeviceHolder> {

    private ArrayList<BluetoothDevice> mDataset;
    private Context context;
    private final View.OnClickListener itemClickListener;

    // Provide a suitable constructor (depends on the kind of dataset)
    public BLEAdapter(Context context, View.OnClickListener itemClickListener) {
        this.context = context;
        this.itemClickListener = itemClickListener;
        mDataset = new ArrayList<>();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class DeviceHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View view;
        public TextView macView;
        public TextView nameView;
        public ImageView imageView;

        public DeviceHolder(View v) {
            super(v);
            view = v;
            macView = (TextView) v.findViewById(R.id.macLine);
            nameView = (TextView) v.findViewById(R.id.firstLine);
            imageView = (ImageView) v.findViewById(R.id.icon);
            v.setOnClickListener(itemClickListener);
        }
    }


    // Create new views (invoked by the layout manager)
    @Override
    public BLEAdapter.DeviceHolder onCreateViewHolder(final ViewGroup parent,
                                                      int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ble_item, parent, false);
        DeviceHolder vh = new DeviceHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(DeviceHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final BluetoothDevice device = mDataset.get(position);
        holder.nameView.setText(device.getName());
        holder.macView.setText(device.getAddress());
        holder.view.setTag(device);
        int bClass = device.getBluetoothClass().getDeviceClass();
        int icon = -1;
        if (bClass == BluetoothClass.Device.COMPUTER_LAPTOP) {
            icon = R.drawable.laptop;
        } else if (bClass == BluetoothClass.Device.PHONE_SMART) {
            icon = R.drawable.phone;
        } else if (bClass == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES) {
            icon = R.drawable.headset;
        } else {
            icon = R.drawable.generic;
        }
        holder.imageView.setImageResource(icon);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void addItem(BluetoothDevice item) {
        this.mDataset.add(item);
    }

    public void initData(ArrayList<BluetoothDevice> list) {
        mDataset = list;
    }

    public BluetoothDevice getitem(int position) {
        return mDataset.get(position);
    }

    public void clear() {
        mDataset.clear();
    }


}