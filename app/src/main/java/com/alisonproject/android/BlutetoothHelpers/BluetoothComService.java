package com.alisonproject.android.BlutetoothHelpers;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class BluetoothComService extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private byte[] mmBuffer; // mmBuffer store for the stream
    String TAG = "##commService";
    private StringBuilder received;
    private Handler handler;

    public BluetoothComService(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.handler = handler;
        this.received = new StringBuilder();
        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
            e.printStackTrace();
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
            e.printStackTrace();
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        mmBuffer = new byte[1024];
        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                // Read from the InputStream.
                numBytes = mmInStream.read(mmBuffer);
                byte[] data = Arrays.copyOf(mmBuffer, numBytes);

                received.append(new String(data));
                if('.' == received.charAt(received.length() - 1)){

                    // Send the obtained bytes to the UI activity.
                    Message readMsg = handler.obtainMessage(MessageConstants.MESSAGE_READ, numBytes, -1, received.toString());
                    received.setLength(0);
                    readMsg.sendToTarget();
                }
//                Log.d("##recu", received.toString());
            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected", e);
                handler.obtainMessage(MessageConstants.DEVICE_DISCONNECTED).sendToTarget();
                break;
            }
        }
    }

    // Call this from the main activity to send data to the remote device.
    public void send(String msg) {
        try {
            //1024 max
            byte[] bytes = (msg + ".").getBytes();
            mmOutStream.write(bytes);

            // Share the sent message with the UI activity.
            Message writtenMsg = handler.obtainMessage(
                    MessageConstants.MESSAGE_WRITE, -1, -1, new String(bytes));
            writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);

            // Send a failure message back to the activity.
            Message writeErrorMsg =
                    handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
            writeErrorMsg.obj = "Couldn't send data to the other device";
            writeErrorMsg.sendToTarget();
        }
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }

//
//    .
//            receivedMsg.append(new String(data));
//
//                if(receivedMsg.toString().endsWith(".")) {
//        Message readMsg = handler.obtainMessage(
//                MessageConstants.MESSAGE_READ, numBytes, -1,
//                new String(receivedMsg.toString()));
//
////                    Log.d("##recu", new String(data));
//        readMsg.sendToTarget();
//        receivedMsg = new StringBuilder();
//    }
}
