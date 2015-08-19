package com.megster.cordova;

import java.io.IOException;

/**
 * Created by joser on 19.08.2015.
 */
public class ConnectThread extends Thread {
    private /*final*/ BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final String mSocketType;

    public ConnectThread(BluetoothDevice device, boolean secure) {
        mmDevice = device;
        BluetoothSocket tmp = null;
        mSocketType = secure ? "Secure" : "Insecure";

        // Get a BluetoothSocket for a connection with the given BluetoothDevice
        try {
            if (secure) {
                // tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                tmp = device.createRfcommSocketToServiceRecord(UUID_SPP);
            } else {
                //tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                tmp = device.createInsecureRfcommSocketToServiceRecord(UUID_SPP);
            }
        } catch (IOException e) {
            Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {
        Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
        setName("ConnectThread" + mSocketType);

        // Always cancel discovery because it will slow down a connection
        mAdapter.cancelDiscovery();

        // Make a connection to the BluetoothSocket
        try {
            // This is a blocking call and will only return on a successful connection or an exception
            Log.i(TAG, "Connecting to socket...");
            mmSocket.connect();
            Log.i(TAG, "Connected");
        } catch (IOException e) {
            Log.e(TAG, e.toString());

            // Some 4.1 devices have problems, try an alternative way to connect
            // See https://github.com/don/BluetoothSerial/issues/89
            try {
                Log.i(TAG, "Trying fallback...");
                mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(mmDevice, 1);
                mmSocket.connect();
                Log.i(TAG, "Connected");
            } catch (Exception e2) {
                Log.e(TAG, "Couldn't establish a Bluetooth connection.");
                try {
                    mmSocket.close();
                } catch (IOException e3) {
                    Log.e(TAG, "unable to close() " + mSocketType + " socket during connection failure", e3);
                }
                connectionFailed();
                return;
            }
        }

        // Reset the ConnectThread because we're done
        synchronized (BluetoothSerialService.this) {
            mConnectThread = null;
        }

        // Start the connected thread
        connected(mmSocket, mmDevice, mSocketType);
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
        }
    }
}