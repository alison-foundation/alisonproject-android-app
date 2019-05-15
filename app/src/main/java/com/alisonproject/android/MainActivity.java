package com.alisonproject.android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    BluetoothAdapter bluetoothAdapter;
    private final int REQUEST_ENABLE_BT = 10;
    private boolean checkedStateChangedByCode = false;
    private Switch switchItem;
    private Button searchButton ;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<MyBluetoothDevice> devices = new ArrayList<>();

    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
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
    private final BroadcastReceiver scanReceiver =  new BroadcastReceiver() {
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



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        setTitle("Alison App");
        Log.d("##","\n\n\n\n\ndfghfd\n\n\n\n");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        searchButton = findViewById(R.id.scan_button);

        // Register for broadcasts when a device is discovered.
        IntentFilter filterBluetoothScan = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(scanReceiver, filterBluetoothScan);

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        searchButton.setOnClickListener(listener -> {
            fetchPairedDevices();
            if(!bluetoothAdapter.startDiscovery()) Log.d("##", "Nope");
            else Log.d("##", "Yep");
        });

        if(!isBluetoothSupported()){
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory( Intent.CATEGORY_HOME );
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            System.exit(0);
        }


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
    }

    private boolean isBluetoothSupported(){
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(getApplicationContext(), "Bluetooth non supporté", Toast.LENGTH_SHORT).show();
            mTextMessage.setText("non supporté");
        }
        return bluetoothAdapter != null;
    }

    private void toggleBluetooth(boolean activate){
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

    private void fetchPairedDevices(){
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                MyBluetoothDevice newDevice = new MyBluetoothDevice(device);
                if(!devices.contains(newDevice))
                    devices.add(newDevice);
            }
        mAdapter = new BluetoothDeviceAdapter(devices);
        recyclerView.setAdapter(mAdapter);

        }

    }

    private void appendNewDevice(BluetoothDevice device){
            devices.add(new MyBluetoothDevice(device));
//
        mAdapter = new BluetoothDeviceAdapter(devices);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        switchItem = (Switch)menu.findItem(R.id.app_bar_switch).getActionView().findViewById(R.id.switch_item);;

        if(isBluetoothSupported()){
            switchItem.setChecked(bluetoothAdapter.isEnabled());
        }
        switchItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(getApplication(), "ON", Toast.LENGTH_SHORT)
                        .show();

                if(!checkedStateChangedByCode)
                    toggleBluetooth(true);
            } else {
                Toast.makeText(getApplication(), "OFF", Toast.LENGTH_SHORT)
                        .show();
                if(!checkedStateChangedByCode)
                    toggleBluetooth(false);
            }

            checkedStateChangedByCode = false;
        });
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            Toast.makeText(getApplicationContext(), "Bluetooth Activé", Toast.LENGTH_SHORT).show();
            mTextMessage.setText("Activé");
            switchItem.setChecked(true);
        }
        else if(requestCode == REQUEST_ENABLE_BT){
            Toast.makeText(getApplicationContext(), "Bluetooth non activé", Toast.LENGTH_SHORT).show();
            mTextMessage.setText("Désactivé");
            switchItem.setChecked(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.

        unregisterReceiver(mReceiver);
        unregisterReceiver(scanReceiver);
    }

}
