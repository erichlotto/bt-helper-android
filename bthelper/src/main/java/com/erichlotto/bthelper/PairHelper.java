package com.erichlotto.bthelper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by erich on 13/09/16.
 */
public class PairHelper {

    static final int DEFAULT_PIN = 1234;

    private final Context context;
    private static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    PairHelper(Context context) {
        this.context = context;
    }


    /*
    * Pair the specified device
    */
    void pair(BluetoothDevice device, int pin, OnPairListener devicePairCallback) {

        PairReceiver receiver = new PairReceiver(pin, devicePairCallback);

        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(receiver, intent);
        try {
            device.createBond();
        } catch (Exception e) {
            devicePairCallback.onError(e);
            context.unregisterReceiver(receiver);
        }
    }


    /*
    * Get a list of paired devices
    */
    public static List<BluetoothDevice> getPairedDevices() {
        //Turn BT ON if needed
        BTHelper.turnOn();
        List<BluetoothDevice> devices = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        devices.addAll(pairedDevices);
        return devices;
    }


    /*
    * Check if a Bluetooth device is paired
    */
    static boolean isPaired(BluetoothDevice btDevice) {
        return btDevice.getBondState() == BluetoothDevice.BOND_BONDED;
    }


    /*
    * Pair callback
    */
    public interface OnPairListener {
        void onSucces(BluetoothDevice btDevice);
        void onError(Exception e);
    }


    /*
    * Wee need a custom receiver to store the PIN
    */
    private class PairReceiver extends BroadcastReceiver {

        private final int mPin;
        private final OnPairListener devicePairCallback;

        PairReceiver(int pin, OnPairListener devicePairCallback) {
            this.mPin = pin;
            this.devicePairCallback = devicePairCallback;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    try {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        int pin = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_KEY, mPin);
                        byte[] pinBytes;
                        pinBytes = ("" + pin).getBytes("UTF-8");
                        device.setPin(pinBytes);
//                        device.setPairingConfirmation(true);
                        devicePairCallback.onSucces(device);
                    } catch (UnsupportedEncodingException e) {
                        devicePairCallback.onError(e);
                    } finally {
                        context.unregisterReceiver(this);
                    }
                }
            }
        }
    }

}
