package com.alisonproject.android;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends BaseActivity {

    private RecyclerView.LayoutManager layoutManager;
    private Button searchButton ;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener  = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        mTextMessage.setText(R.string.title_home);
                        connManager.send("Home");
                        return true;
                    case R.id.navigation_dashboard:
                        mTextMessage.setText(R.string.title_dashboard);
                        connManager.send("Dashboard");
                        return true;
                    case R.id.navigation_notifications:
                        mTextMessage.setText(R.string.title_notifications);
                        connManager.send("Notifs");
                        return true;
                }
                return false;
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Alison App");

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        searchButton = findViewById(R.id.scan_button);
        searchButton.setOnClickListener(listener -> {
            fetchPairedDevices();
            if(!bluetoothAdapter.startDiscovery()) Log.d("##", "Nope");
            else Log.d("##", "Yep");
        });
        if(!isBluetoothSupported()){shutdown();}

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        Log.d("##", UUID.fromString(getString(R.string.app_uuid)).toString());
//        Log.d("##", UUID.fromString("OnePlus 6T").toString());
        Method getUuidsMethod = null;
        try {
            getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
            ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(bluetoothAdapter, null);

            for (ParcelUuid uuid: uuids) {
                Log.d("##uuid", "UUID: " + uuid.getUuid().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        switchItem = (Switch)menu.findItem(R.id.app_bar_switch).getActionView().findViewById(R.id.switch_item);;
        if(isBluetoothSupported())
            switchItem.setChecked(bluetoothAdapter.isEnabled());
        switchItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(!checkedStateChangedByCode)
                toggleBluetooth(isChecked);
            else
                checkedStateChangedByCode = false;
        });
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            mTextMessage.setText(getString(R.string.bluetooth_activated));
            switchItem.setChecked(true);
        }
        else if(requestCode == REQUEST_ENABLE_BT){
            mTextMessage.setText(getString(R.string.bluetooth_disabled));
            switchItem.setChecked(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
