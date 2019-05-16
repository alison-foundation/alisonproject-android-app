package com.alisonproject.android.BlutetoothHelpers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alisonproject.android.R;

import java.io.IOException;
import java.util.Observable;
import java.util.UUID;

public class BluetoothConnManager {
    protected BluetoothAdapter bluetoothAdapter;
    protected BluetoothSocket deviceSocket;
    protected final UUID MY_UUID;
    protected AcceptThread acceptThread;
    private  BluetoothComService comService;
    protected Handler handler = new Handler(this::handleServiceMsg);


    public BluetoothConnManager(BluetoothAdapter bluetoothAdapter, UUID uuid){
        this.bluetoothAdapter = bluetoothAdapter;
        MY_UUID = uuid;
        acceptThread = new AcceptThread();
        deviceSocket = null;
        comService = null;
    }


    private boolean handleServiceMsg(Message msg) {
        switch (msg.what){
            case MessageConstants.MESSAGE_WRITE:
                Log.i("##send_ok", (String)msg.obj);
                return true;
            case MessageConstants.MESSAGE_TOAST:
                Log.i("##send_ailed", (String)msg.obj);
                return true;
            case MessageConstants.DEVICE_SOCKET:
                manageConnectedSocket((BluetoothSocket)msg.obj);
                return true;
            default:
                Log.d("##", "");
                return false;
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket){
        if(deviceSocket != null){
            try {
                deviceSocket.close();
            } catch (IOException e) {
                Log.d("##", "Error when trying to close hold Bluetooth socket from connManager");
                e.printStackTrace();
            }
        }

        deviceSocket = socket;
        comService = new BluetoothComService(deviceSocket, handler);
        comService.start();
    }

    public void connectTo(BluetoothDevice device) {
        ConnectThread connectThread = new ConnectThread(device, MY_UUID, bluetoothAdapter, handler);
        connectThread.start();
    }

    public void send(String msg){
        comService.send(msg);
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("Alison app", MY_UUID);
            } catch (IOException e) {
                Log.e("##", "Socket's listen() method failed", e);
                e.printStackTrace();
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e("##", "Socket's accept() method failed", e);
                    e.printStackTrace();
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e("##", "Could not close the connect socket", e);
            }
        }
    }
}
