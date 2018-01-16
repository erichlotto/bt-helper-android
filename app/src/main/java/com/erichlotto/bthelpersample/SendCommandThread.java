package com.erichlotto.bthelpersample;

import android.bluetooth.BluetoothSocket;

import com.erichlotto.bthelper.AbstractCommandThread;

import java.io.IOException;

/**
 * Created by erich on 1/12/18.
 */

public class SendCommandThread extends AbstractCommandThread {

    byte count = 0;

    public SendCommandThread(BluetoothSocket socket) {
        super(socket);
    }


    public void run() {
        while(!isInterrupted()){
            sendCommand(0, 5);
            try {
                sleep(20);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            count ++;
        }
    }

    public void sendCommand(double x, double y){
        int new_x = (int) Math.floor(x*4095);
        int new_y = (int) Math.floor(y*4095);
        new_x = Math.min(4095, Math.max(new_x, 0));
        new_y = Math.min(4095, Math.max(new_y, 0));
        byte[] buffer = new byte[9];  // buffer store for the stream

        buffer[0] = (byte)'*';
        buffer[1] = (byte)((0xff00 & new_x)>>8);
        buffer[2] = (byte)(0x00ff & new_x);
        buffer[3] = (byte)((0xff00 & new_y)>>8);
        buffer[4] = (byte)(0x00ff & new_y);
        buffer[5] = (byte)0;
        buffer[6] = (byte)0;
        buffer[7] = count;
        byte b = 0;
        for(int i=1; i<buffer.length-1; i++)
            b+=buffer[i];
        buffer[8] = b;

        try {
            mmOutStream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
