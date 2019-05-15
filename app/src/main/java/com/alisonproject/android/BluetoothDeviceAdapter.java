package com.alisonproject.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.MyViewHolder> {
    private ArrayList<MyBluetoothDevice> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public final View mView;
        public final TextView deviceName;
        public final TextView deviceMac;
        public Button connectBtn;
        public MyViewHolder(View v) {
            super(v);
            mView = v;
            deviceName = mView.findViewById(R.id.deviceNameTV);
            deviceMac = mView.findViewById(R.id.deviceMacTW);
            connectBtn = mView.findViewById(R.id.connectBtn);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public BluetoothDeviceAdapter(ArrayList<MyBluetoothDevice> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public BluetoothDeviceAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                  int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bluetooth_device_view, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.deviceName.setText(mDataset.get(position).getName());
        holder.deviceMac.setText(mDataset.get(position).getAddress());
        holder.connectBtn.setId(position);
        holder.connectBtn.setOnClickListener( listener -> mDataset.get(position).connect());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
