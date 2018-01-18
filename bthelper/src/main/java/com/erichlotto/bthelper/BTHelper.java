package com.erichlotto.bthelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by erich on 1/12/18.
 */

public class BTHelper {

    private Activity activity;
    private ScanHelper scanHelper;
    private PairHelper pairHelper;
    private List<ConnectionThread> connectionThreads = new ArrayList<>();
    private AlertDialog dialog;


    public BTHelper(Activity activity) {
        this.activity = activity;
        this.scanHelper = new ScanHelper(activity);
        this.pairHelper = new PairHelper(activity);
    }

    // SCAN
    public void scanDevices(final ScanHelper.DeviceScanListener deviceScanListener) {
        scanHelper.scanDevices(deviceScanListener);
    }

    // PAIR
    public void pairDevice(BluetoothDevice btDevice, PairHelper.OnPairListener callback) {
        pairDevice(btDevice, PairHelper.DEFAULT_PIN, callback);
    }

    public void pairDevice(BluetoothDevice btDevice, int pin, PairHelper.OnPairListener callback) {
        if (isPaired(btDevice))
            callback.onSucces(btDevice);
        else {
            pairHelper.pair(btDevice, pin, callback);
        }
    }

    public boolean isPaired(BluetoothDevice btDevice) {
        return PairHelper.isPaired(btDevice);
    }


    // CONNECT
    public void connectDevice(BluetoothDevice device, final ConnectionThread.OnConnectionListener callback) {
        scanHelper.cancelScan();
        if(!PairHelper.isPaired(device)) {
            pairDevice(device, new PairHelper.OnPairListener() {
                @Override
                public void onSucces(BluetoothDevice btDevice) {
                    connectDevice(btDevice, callback);
                }

                @Override
                public void onError(Exception e) {
                    callback.onError(e);
                }
            });
        } else {
            ConnectionThread ct = new ConnectionThread(device, new ConnectionThread.OnConnectionListener() {
                @Override
                public void onConnected(BluetoothDevice device, BluetoothSocket socket) {
                    callback.onConnected(device, socket);
                    LastDeviceHelper.storeLastAddress(activity, device.getAddress());
                }

                @Override
                public void onError(Exception e) {
                    callback.onError(e);
                }
            });
            connectionThreads.add(ct);
            ct.start();
        }
    }


    /*
    * Displays a dialog asking the user which device he wants to create a connection to
    */
    public void showConnectionDialog(final ConnectionThread.OnConnectionListener onConnectionListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final LayoutInflater inflater = activity.getLayoutInflater();

        View v = inflater.inflate(R.layout.bluetooth_selection_dialog, null);

        final LinearLayout ll = v.findViewById(R.id.llDevicesList);

        final View loadingIndicator = v.findViewById(R.id.loading_indicator);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(v)
                // Add action buttons
                .setNegativeButton(android.R.string.cancel, null);

        dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setTitle(R.string.title_select_bluetooth_device);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                scanHelper.onDestroy();
            }
        });
        dialog.show();

        scanDevices(new ScanHelper.DeviceScanListener() {
            @Override
            public void onScanStarted() {
                loadingIndicator.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDeviceFound(final BluetoothDevice device) {
                View v = inflater.inflate(R.layout.bluetooth_entry, null);
                ((TextView)v.findViewById(R.id.text1)).setText(device.getName()!=null
                        && device.getName().length()>0?device.getName() : activity.getString(R.string.label_nameless_device));
                ((TextView)v.findViewById(R.id.text2)).setText(device.getAddress());
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        final ProgressDialog progress = new ProgressDialog(activity);
                        progress.setCancelable(false);
                        progress.setMessage(activity.getString(R.string.wait));
                        progress.show();
                        connectDevice(device, new ConnectionThread.OnConnectionListener() {
                            @Override
                            public void onConnected(final BluetoothDevice device, final BluetoothSocket socket) {
                                progress.dismiss();
                                onConnectionListener.onConnected(device, socket);
                            }

                            @Override
                            public void onError(final Exception e) {
                                progress.dismiss();
                                onConnectionListener.onError(e);
                            }
                        });
                    }
                });
                ll.addView(v, ll.getChildCount() - 1);
            }

            @Override
            public void onScanFinished() {
                loadingIndicator.setVisibility(View.GONE);
            }
        });
    }


    /*
    * Try to create a connection with the last device
    */
    public void connectToLastDevice(ConnectionThread.OnConnectionListener callback){
        for(BluetoothDevice d : PairHelper.getPairedDevices()){
            if(LastDeviceHelper.getLastAddress(activity)!=null && d.getAddress().equals(LastDeviceHelper.getLastAddress(activity))){
                connectDevice(d, callback);
                return;
            }
        }
        callback.onError(new Exception("Last device unavailable"));
    }


    /*
    * ALWAYS call onDestroy() when finished using BTHelper
    */
    public void onDestroy() {
        if(dialog!=null)
            dialog.dismiss();
        for(ConnectionThread ct : connectionThreads)
            ct.cancel();
        scanHelper.onDestroy();

        activity = null;
        scanHelper = null;
        pairHelper = null;
        dialog = null;
    }


    /*
    * Turn adapter ON
    */
    public static void turnOn() {
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().enable();
        }
    }


    /*
    * Turn adapter OFF
    */
    public static void turnOff() {
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().disable();
        }
    }

}
