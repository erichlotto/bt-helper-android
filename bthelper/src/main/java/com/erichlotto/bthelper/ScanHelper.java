package com.erichlotto.bthelper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

/**
 * Created by erich on 1/9/18.
 */

public class ScanHelper {

    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final Context context;

    private BroadcastReceiver deviceScannedReceiver;

    ScanHelper(Context context) {
        this.context = context;
    }


    public void scanDevices(final DeviceScanListener deviceScanListener) {
        //Check if it is already scanning for devices
        if (mBluetoothAdapter.isDiscovering())
            return;

        //Turn BT ON if needed
        BTHelper.turnOn();

        unregisterReceiver(deviceScannedReceiver);
        deviceScannedReceiver = new BroadcastReceiver() {
            boolean scanning;
            public void onReceive(Context context, final Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            deviceScanListener.onDeviceFound(device);
                        }
                    }, 1000);
                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action) && !scanning) {
                    deviceScanListener.onScanStarted();
                    scanning = true;
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    deviceScanListener.onScanFinished();
                    unregisterReceiver(deviceScannedReceiver);
                    scanning = false;
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(deviceScannedReceiver, filter); // Don't forget to unregister during onDestroy
        cancelScan();
        mBluetoothAdapter.startDiscovery();
    }


    void cancelScan() {
        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();
    }

    void onDestroy() {
        cancelScan();
        unregisterReceiver(deviceScannedReceiver);
    }

    private void unregisterReceiver(BroadcastReceiver receiverToUnregister) {
        if (receiverToUnregister != null)
            try {
                context.unregisterReceiver(receiverToUnregister);
            } catch (IllegalArgumentException e) { /* Receiver wasnt even registered */ }
    }

    /* INTERFACES */
    public interface DeviceScanListener {
        void onScanStarted();
        void onDeviceFound(final BluetoothDevice device);
        void onScanFinished();
    }


}
