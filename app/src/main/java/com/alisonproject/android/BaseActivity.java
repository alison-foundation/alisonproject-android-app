package com.alisonproject.android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

    BluetoothAdapter bluetoothAdapter;
    protected boolean checkedStateChangedByCode = false;
    protected final int REQUEST_ENABLE_BT = 10;
    protected Switch switchItem;
    protected UUID MY_UUID;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        MY_UUID = UUID.fromString(getString(R.string.spp_uuid));
        BluetoothConnManager.setMyUuid(MY_UUID);
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
    }
}
