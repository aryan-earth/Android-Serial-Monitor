package com.qass.serialmonitor;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;


public class MainActivity extends AppCompatActivity {
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_splash);
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(progressStatus < 100 ) {
                    progressStatus += 1;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });
                    try{
                        Thread.sleep(49);
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        //Display logo for 5 seconds
        new CountDownTimer(5000,1000){
            @Override
            public void onTick(long millisUntilFinished){}

            @Override
            public void onFinish(){
                //set the new Content of your activity
                MainActivity.this.setContentView(R.layout.activity_main);
                Toolbar toolbar = findViewById(R.id.main_activity_toolbar);
                setSupportActionBar(toolbar);
            }
        }.start();
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
