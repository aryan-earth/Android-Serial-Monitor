package com.qass.serialmonitor;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.stealthcopter.networktools.SubnetDevices;
import com.stealthcopter.networktools.subnet.Device;

import java.util.ArrayList;

public class ShowScannedWifi extends AppCompatActivity {
    private ListView listView;
    private ArrayList<String> mDeviceList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_scanned_wifi);
        WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifi.getConnectionInfo();
        if(wifiInfo.getNetworkId() == -1) {
            // Not connected to a network
            Toast.makeText(this, "Not connected to a network!", Toast.LENGTH_SHORT).show();
        } else {
            listView = (ListView) findViewById(R.id.wifi_lisiview);
            Toast.makeText(this, "Scanning...", Toast.LENGTH_SHORT).show();
            // Asynchronously
            SubnetDevices.fromLocalAddress().findDevices(new SubnetDevices.OnSubnetDeviceFound() {
                @Override
                public void onDeviceFound(Device device) {
                    // Stub: Found subnet device
                    mDeviceList.add("IP Address: " + device.ip + " | " + "MAC: " + device.mac);

                }

                @Override
                public void onFinished(ArrayList<Device> devicesFound) {
                    // Stub: Finished scanning

                    runOnUiThread(new Runnable(){

                        @Override
                        public void run(){
                            //update ui here
                            // display toast here
                            listView.setAdapter(new ArrayAdapter<String>(ShowScannedWifi.this,
                                    android.R.layout.simple_list_item_1, mDeviceList));
                            Toast.makeText(ShowScannedWifi.this, "Finished Scanning", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }







    }

    public void goBack(View view) {
        try {
            finish();
        } catch(Exception exc) {

        }
    }
}
