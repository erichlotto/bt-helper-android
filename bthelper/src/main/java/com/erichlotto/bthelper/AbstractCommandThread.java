package com.erichlotto.bthelper;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by erich on 1/15/18.
 */

public abstract class AbstractCommandThread extends Thread {

    final public BluetoothSocket mmSocket;
    final public InputStream mmInStream;
    final public OutputStream mmOutStream;

    public AbstractCommandThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }


    /*
     * Remember to call interrupt() when finished using the thread
     * (probably on Activity's onDestroy())
     */
    @Override
    public void interrupt() {
        try {
            mmInStream.close();
            mmOutStream.close();
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.interrupt();
    }
}
