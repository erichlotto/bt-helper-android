package com.erichlotto.bthelper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by erich on 13/09/16.
 */
public class ConnectionThread extends Thread {

    private static final UUID ARDUINO_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    public BluetoothDevice btDevice;
    private BluetoothSocket mmSocket;
    private OnConnectionListener callback;

    public ConnectionThread(BluetoothDevice device, OnConnectionListener callback) {
        this.btDevice = device;
        this.callback = callback;

        BluetoothSocket tmp = null;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            tmp = device.createRfcommSocketToServiceRecord(ARDUINO_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmSocket = tmp;
    }

    @Override
    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            System.out.println("CONNECTING...");
            mmSocket.connect();
            System.out.println("SUCCESS!");
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    callback.onConnected(btDevice, mmSocket);
                }
            });
        } catch (final IOException connectException) {
            System.out.println(connectException);
            // Unable to connect; close the socket and get out
            cancel();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    callback.onError(connectException);
                }
            });
            return;
        }
    }

    /*
    * Will cancel an in-progress connection, and close the socket
    */
    void cancel() {
        try {
            System.out.println("CLOSING CONNECTION");
            mmSocket.close();
            System.out.println("CONNECTION CLOSED");
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }


    /*
    * Check if socket is connected (if the connection is active)
    */
    boolean isConnected(){
        return mmSocket != null && mmSocket.isConnected();
    }


    /*
    * Connection callback
    */
    public interface OnConnectionListener {
        void onConnected(BluetoothDevice device, BluetoothSocket socket);
        void onError(Exception e);
    }

}
