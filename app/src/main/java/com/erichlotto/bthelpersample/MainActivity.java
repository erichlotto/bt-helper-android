package com.erichlotto.bthelpersample;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.erichlotto.bthelper.BTHelper;
import com.erichlotto.bthelper.ConnectionThread;

public class MainActivity extends AppCompatActivity {

    BTHelper btHelper;
    SendCommandThread command;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btHelper = new BTHelper(this);

        findViewById(R.id.bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btHelper.showConnectionDialog(new ConnectionThread.OnConnectionListener(){
                    @Override
                    public void onConnected(BluetoothDevice device, final BluetoothSocket socket) {
                        Toast.makeText(MainActivity.this, "Conectou em " + device.getName(), Toast.LENGTH_SHORT).show();
                        command = new SendCommandThread(socket);
                        command.start();

                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(MainActivity.this, "Erro: " + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btHelper.connectToLastDevice(new ConnectionThread.OnConnectionListener() {
            @Override
            public void onConnected(BluetoothDevice device, BluetoothSocket socket) {
                Toast.makeText(MainActivity.this, "Conectou em " + device.getName(), Toast.LENGTH_SHORT).show();
                command = new SendCommandThread(socket);
                command.start();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this, "Erro: " + e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        btHelper.onDestroy();
        if(command!=null)
            command.interrupt();
    }
}
