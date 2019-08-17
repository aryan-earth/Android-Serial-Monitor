package com.qass.serialmonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(toolbar);
    }

    public void startBluetoothActivity(View view) {
        Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
        startActivity(intent);;

    }

    public void startWifiActivity(View view) {
        Intent intent = new Intent(MainActivity.this, WifiActivity.class);
        startActivity(intent);;
    }

    public void startUsbActivity(View view) {
        Intent intent = new Intent(MainActivity.this, UsbActivity.class);
        startActivity(intent);;
    }

    public void aboutUs(View view) {
        Intent intent = new Intent(MainActivity.this, AboutUsActivity.class);
        startActivity(intent);
    }
}
