package com.alisonproject.android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.alisonproject.android.BlutetoothHelpers.BluetoothConnManager;
import com.alisonproject.android.BlutetoothHelpers.MessageConstants;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BaseActivity  extends AppCompatActivity{

    protected TextView mTextMessage;
    BluetoothAdapter bluetoothAdapter;
    protected boolean checkedStateChangedByCode = false;
    protected final int REQUEST_ENABLE_BT = 10;
    protected ArrayList<MyBluetoothDevice> devices = new ArrayList<>();
    protected RecyclerView recyclerView;
    protected RecyclerView.Adapter mAdapter;
    protected Switch switchItem;
    protected BluetoothConnManager connManager;
    protected UUID MY_UUID;
    protected Handler handler = new Handler(msg -> {
        switch (msg.what){
            case MessageConstants.CONNECT_DEVICE:
                connManager.connectTo((BluetoothDevice) msg.obj);
                break;
            default:
                Log.d("##BaseActivity", "Undefined handler message constant [" + msg.what + "]");
                return false;
        }
        return true;
    });

    protected final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Toast.makeText(getApplicationContext(), action, Toast.LENGTH_LONG).show();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        mTextMessage.setText("Bluetooth désactivé");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        // Bluetooth is turning off
                        mTextMessage.setText("Désactivation en cours");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        mTextMessage.setText("Bluetooth actif");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        mTextMessage.setText("Bluetooth en cours d'activation");
                        break;
                }
            }

        }
    };

    //Create a BroadcastReceiver for ACTION_FOUND.
    protected final BroadcastReceiver scanReceiver =  new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                Toast.makeText(getApplicationContext(), "new device", Toast.LENGTH_SHORT).show();
                Log.d("##", "new device");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                appendNewDevice(device);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filterBluetoothScan = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        // Register for broadcasts when a device is discovered.
        registerReceiver(scanReceiver, filterBluetoothScan);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        MY_UUID = UUID.fromString(getString(R.string.app_uuid));
        connManager = new BluetoothConnManager(bluetoothAdapter, MY_UUID);
//        ParcelUuid [] res = bluetoothAdapter.getRemoteDevice(bluetoothAdapter.getAddress()).getUuids();
//        Log.d("##",(res == null)?" null" : res.length + "");
//        if (res!=null)
//        for (ParcelUuid a : res) {
//            Log.d("##_uuids", a.getUuid().toString());
//        }
    }

    protected void appendNewDevice(BluetoothDevice device){
        MyBluetoothDevice newDevice = new MyBluetoothDevice(device, handler);
        if(!devices.contains(newDevice)) {
            devices.add(newDevice);
            mAdapter = new BluetoothDeviceAdapter(devices);
            recyclerView.setAdapter(mAdapter);
        }
    }

    protected void fetchPairedDevices(){
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                MyBluetoothDevice newDevice = new MyBluetoothDevice(device, handler);
                if(!devices.contains(newDevice))
                    devices.add(newDevice);
            }
            mAdapter = new BluetoothDeviceAdapter(devices);
            recyclerView.setAdapter(mAdapter);
        }
    }


    protected void toggleBluetooth(boolean activate){
        if (activate && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else if(!activate && bluetoothAdapter.isEnabled()){
            if(!bluetoothAdapter.disable()){
                switchItem.setEnabled(true);
            }
        }
    }

    protected boolean isBluetoothSupported(){
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(getApplicationContext(), "Bluetooth non supporté", Toast.LENGTH_SHORT).show();
            mTextMessage.setText(getString(R.string.bluetooth_not_supported));
        }
        return bluetoothAdapter != null;
    }


    protected void shutdown(){
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
        System.exit(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
        unregisterReceiver(scanReceiver);
    }
}
