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
import com.github.lucadruda.iotcentral.service.types.DeviceTemplate;

import java.util.ArrayList;

public class IoTCAdapter extends RecyclerView.Adapter<IoTCAdapter.DeviceHolder> {

    private Object[] mDataset;
    private Context context;
    private final View.OnClickListener itemClickListener;

    // Provide a suitable constructor (depends on the kind of dataset)
    public IoTCAdapter(Context context, Object[] objects, View.OnClickListener itemClickListener) {
        this.context = context;
        this.itemClickListener = itemClickListener;
        mDataset = objects;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class DeviceHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View view;
        public TextView textView;

        public DeviceHolder(View v) {
            super(v);
            view = v;
            textView = (TextView) v.findViewById(R.id.text1);
            v.setOnClickListener(itemClickListener);
        }
    }


    // Create new views (invoked by the layout manager)
    @Override
    public IoTCAdapter.DeviceHolder onCreateViewHolder(final ViewGroup parent,
                                                       int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.iotc_item, parent, false);
        DeviceHolder vh = new DeviceHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(DeviceHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Object obj = mDataset[position];
        holder.textView.setText(obj.toString());
        holder.view.setTag(obj);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }


}