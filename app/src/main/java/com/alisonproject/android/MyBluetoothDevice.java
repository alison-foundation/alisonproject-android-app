package com.alisonproject.android;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.annotation.Nullable;


public class MyBluetoothDevice {
    private String name;
    private String address;
//    private Handler handler = null;
    private BluetoothDevice device = null;

    public MyBluetoothDevice(BluetoothDevice device)//, Handler handler)
    {
        if(device != null)
        {
            this.device = device;
            this.name = device.getName();
            this.address = device.getAddress();
//            this.handler = handler;
        }
        else
        {
            this.device = device;
            this.name = "None";
            this.address = "";
//            this.handler = handler;
        }

        // TODO
    }

    public String getName()
    {
        return name;
    }

    public String getAddress()
    {
        return address;
    }

    public boolean isConnected()
    {
        // TODO

        return false;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String toString()
    {
        return "\nNom : " + name + "\nAdresse : " + address;
    }

    public void send(String data)
    {
        // TODO
    }

    public void connect()
    {
        // TODO
        Log.d("##", "connect to [" + name + "]   " + address);
    }

    public boolean disconnect()
    {
        // TODO
        return true;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == this)
            return true;
        if(obj instanceof MyBluetoothDevice)
            return ((MyBluetoothDevice) obj).getAddress().equals(address);
        else
            return super.equals(obj);
    }
}
