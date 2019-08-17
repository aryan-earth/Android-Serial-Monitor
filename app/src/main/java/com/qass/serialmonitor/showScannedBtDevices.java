package com.qass.serialmonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import java.util.ArrayList;

public class showScannedBtDevices extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> mDeviceList = new ArrayList<String>();
    private ArrayList<String> mDeviceAddress = new ArrayList<String>();
    String address = "0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(statusOfGPS) {
        } else {
            Toast.makeText(this, "Please turn on gps and set it to high accuracy!", Toast.LENGTH_LONG).show();
            Intent gpsOptionsIntent = new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(gpsOptionsIntent);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_scanned_bt_devices);



        listView = (ListView) findViewById(R.id.scan_bt_listview);
        Toast.makeText(this, "Scanning...", Toast.LENGTH_LONG).show();
        try {
            Thread.sleep(1000);
        } catch(InterruptedException exc) {
            Log.i("TAG", "Thread error");
        }
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Boolean b = bluetoothAdapter.startDiscovery();
        Log.i("TAG", b.toString());
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDeviceList));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                address = mDeviceAddress.get(position);
                BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
                BluetoothActivity.ConnectThread ct = new BluetoothActivity.ConnectThread(bluetoothDevice);
                ct.run();

                finish();
            }
        });
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i("TAG", "Received something");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                mDeviceList.add(device.getName() + "\n" + device.getAddress());
                mDeviceAddress.add(device.getAddress());
                Log.i("TAG", device.getAddress());
                listView.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mDeviceList));

            }

        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);

    }
}
