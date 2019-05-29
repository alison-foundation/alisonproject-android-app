package com.alisonproject.android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentTransaction;

import com.alisonproject.android.BlutetoothHelpers.BluetoothConnManager;
import com.alisonproject.android.BlutetoothHelpers.MessageConstants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends BaseActivity implements ConnectFragment.OnFragmentInteractionListener, ActionsFragment.OnFragmentInteractionListener, SaveSoundFragment.SaveSoundDialogListener {


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener  = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
//                        mTextMessage.setText(R.string.title_home);
                        BluetoothConnManager.getInstance().send("Home");
                        return true;
                    case R.id.navigation_dashboard:
//                        mTextMessage.setText(R.string.title_dashboard);
//                        BluetoothConnManager.getInstance().send("Dashboard");
                        return true;
                    case R.id.navigation_notifications:
//                        mTextMessage.setText(R.string.title_notifications);
//                        BluetoothConnManager.getInstance().send("Notifs");
                        return true;
                }
                return false;
            };

    private CardView cardView;
    ProgressBarFragment progressBarFragment;

    private Handler conHandler = new Handler( msg -> {

        switch (msg.what){
            case MessageConstants.DEVICE_CONNECTING:
                //show spinner
                Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
                {
                    progressBarFragment = new ProgressBarFragment();
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container_view, progressBarFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
                break;
            case MessageConstants.DEVICE_CONNECTED:
                //Chaange fragment to menu fragment
                Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
                {
                    ActionsFragment newFragment = new ActionsFragment();
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container_view, newFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
                break;
            case MessageConstants.DEVICE_DISCONNECTED:
                Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
            {
                ConnectFragment newFragment = new ConnectFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container_view, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
                break;
            default:
                Log.d("##MainActivity", "Undefined handler message constant [" + msg.what + "]");
                return false;
        }
        return true;
    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Alison App");

        cardView = (CardView) findViewById(R.id.container_view);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BluetoothConnManager.setUIhandler(conHandler);
//        connManager = BluetoothConnManager.getInstance();
        //create fragment

        if(!isBluetoothSupported()){shutdown();}
        ConnectFragment newFragment = new ConnectFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//
//// Replace whatever is in the fragment_container view with this fragment,
//// and add the transaction to the back stack
        transaction.replace(R.id.container_view, newFragment);
        transaction.addToBackStack(null);
//// Commit the transaction
        transaction.commit();
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
            Toast.makeText(this, getString(R.string.bluetooth_activated), Toast.LENGTH_SHORT).show();
            switchItem.setChecked(true);
        }
        else if(requestCode == REQUEST_ENABLE_BT){
            Toast.makeText(this, getString(R.string.bluetooth_disabled), Toast.LENGTH_SHORT).show();
            switchItem.setChecked(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onFragmentInteraction(int what, String text) {
        if(what == MessageConstants.MESSAGE_TOAST)
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        if(what == MessageConstants.DEVICE_DISCONNECTED){
            Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
            BluetoothConnManager.setUIhandler(conHandler);
            {
                ConnectFragment newFragment = new ConnectFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container_view, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }
    }



    @Override
    public void onDialogPositiveClick(SaveSoundFragment dialog, String tag) {
        String colorHex = Integer.toHexString(dialog.selectedColor).substring(2);
        BluetoothConnManager.getInstance().send("save | " + tag + " | " + colorHex);
        Toast.makeText(getApplicationContext(), "Sound learned", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDialogNegativeClick(SaveSoundFragment dialog) {
        BluetoothConnManager.getInstance().send("drop");
    }
}
