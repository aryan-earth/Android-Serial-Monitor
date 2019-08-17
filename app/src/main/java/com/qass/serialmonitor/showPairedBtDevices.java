package com.qass.serialmonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Set;

public class showPairedBtDevices extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> mDeviceList = new ArrayList<String>();
    private ArrayList<String> mDeviceAddress = new ArrayList<String>();
    private BluetoothAdapter mBluetoothAdapter;
    String address = "0";
    public static boolean unableToConnect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_paired_bt_devices);

        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                mDeviceList.add(device.getName() + "\n" + device.getAddress());
                mDeviceAddress.add(device.getAddress());
            }
        } else {
            Toast.makeText(this, "No paired devices", Toast.LENGTH_LONG ).show();
        }

        listView = (ListView) findViewById(R.id.paired_listview);
        listView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mDeviceList));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("TAGG","Clicked" );
                try {
                    address = mDeviceAddress.get(position);
                    BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
                    BluetoothActivity.ConnectThread ct = new BluetoothActivity.ConnectThread(bluetoothDevice);
                    ct.run();
                    Log.i("TAGG","Started" );
                } catch(NullPointerException exc) {
                    Log.i("TAGG", "NullPointer Exception");
                    showPairedToast();

                }
                finish();
            }
        });

    }

    public void showPairedToast() {
        Toast.makeText(this, "Failed Connecting!", Toast.LENGTH_SHORT).show();
    }

}
