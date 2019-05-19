package com.alisonproject.android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alisonproject.android.BlutetoothHelpers.BluetoothConnManager;
import com.alisonproject.android.BlutetoothHelpers.MessageConstants;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConnectFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConnectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConnectFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String uuid;
    private String mParam2;
    private UUID MY_UUID;
    protected Handler handler = new Handler(msg -> {
        switch (msg.what){
            case MessageConstants.CONNECT_DEVICE:
                BluetoothConnManager.getInstance().connectTo((BluetoothDevice) msg.obj);
                break;
            default:
                Log.d("##ConnectFragment", "Undefined handler message constant [" + msg.what + "]");
                return false;
        }
        return true;
    });

    protected TextView mTextMessage;
    private BluetoothAdapter bluetoothAdapter;
    protected ArrayList<MyBluetoothDevice> devices = new ArrayList<>();
    protected RecyclerView recyclerView;
    protected RecyclerView.Adapter mAdapter;
    private Button searchButton ;

    protected final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Toast.makeText(context, action, Toast.LENGTH_LONG).show();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Toast.makeText(context, "Bluetooth désactivé", Toast.LENGTH_SHORT).show();;
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        // Bluetooth is turning off
                        Toast.makeText(context, "Désactivation en cours", Toast.LENGTH_SHORT).show();;
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Toast.makeText(context, "Bluetooth actif", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Toast.makeText(context, "Bluetooth en cours d'activation", Toast.LENGTH_SHORT).show();;
                        break;
                }
            }

        }
    };

    protected final BroadcastReceiver scanReceiver =  new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                Toast.makeText(context, "new device", Toast.LENGTH_SHORT).show();
                Log.d("##", "new device");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                appendNewDevice(device);
            }
        }
    };

    private OnFragmentInteractionListener mListener;

    public ConnectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param uuid Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConnectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConnectFragment newInstance(String uuid, String param2) {
        ConnectFragment fragment = new ConnectFragment();
        Bundle args = new Bundle();

        args.putString(ARG_PARAM1, uuid);
        args.putString(ARG_PARAM2, param2);
        fragment.MY_UUID = UUID.fromString(uuid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            MY_UUID = UUID.fromString(getArguments().getString(ARG_PARAM1));
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        IntentFilter filterBluetoothScan = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        // Register for broadcasts when a device is discovered.
        Objects.requireNonNull(getActivity()).registerReceiver(scanReceiver, filterBluetoothScan);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        Objects.requireNonNull(getActivity()).registerReceiver(mReceiver, filter);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        MY_UUID = UUID.fromString(getString(R.string.spp_uuid));
        BluetoothConnManager.setMyUuid(MY_UUID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connect, container, false);
        if(view instanceof ConstraintLayout){
            searchButton = view.findViewById(R.id.scan_button);
            searchButton.setOnClickListener(listener -> {
                onButtonPressed("Search button pressed");
                fetchPairedDevices();
                if(!bluetoothAdapter.startDiscovery()) Log.d("##", "Nope");
                else Log.d("##", "Yep");
            });
            mTextMessage = view.findViewById(R.id.message);
            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
            recyclerView.setLayoutManager(layoutManager);
        }


        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String text) {
        if (mListener != null) {
            mListener.onFragmentInteraction(MessageConstants.MESSAGE_TOAST, text);
        }
    }


    protected void appendNewDevice(BluetoothDevice device){
        MyBluetoothDevice newDevice = new MyBluetoothDevice(device, handler);
        if(!devices.contains(newDevice)) {
            devices.add(newDevice);
            mAdapter = new BluetoothDeviceAdapter(devices);
            Objects.requireNonNull(recyclerView).setAdapter(mAdapter);
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



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        Objects.requireNonNull(getActivity()).unregisterReceiver(mReceiver);
        Objects.requireNonNull(getActivity()).unregisterReceiver(scanReceiver);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(int what, String text);
    }
}
