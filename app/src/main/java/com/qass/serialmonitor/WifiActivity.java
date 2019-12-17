package com.qass.serialmonitor;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class WifiActivity extends AppCompatActivity {

    public static Socket s;
    private ScrollView scrollView;
    private TextView text;
    Button clear;
    public static String ip_host = "none";
    public static int port_host = 8000;
    private static Context context;
    private  EditText name;
    private  EditText ip;
    private  EditText port;
    String deviceName = "none";
    String deviceIp = "none";
    int devicePort = 0;
    public static String command;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        Toolbar toolbar = findViewById(R.id.wifi_activity_toolbar);
        setSupportActionBar(toolbar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ip = (EditText) findViewById(R.id.ip_editText);
        port = (EditText) findViewById(R.id.port_editText);

        WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = new WifiConfiguration();
        if(!wifi.isWifiEnabled()) {
            Toast.makeText(this, "Please open Wifi and connect to same network as device", Toast.LENGTH_LONG).show();
            Intent enableWifiIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(enableWifiIntent);
        }

        scrollView = (ScrollView) findViewById(R.id.scrollView2);
        text = (TextView)findViewById(R.id.text);
        text.setMovementMethod(new ScrollingMovementMethod());
        clear = (Button)findViewById(R.id.clear_button);


    }
    
    private Context getAppContext() {
        context = this;
        return context;
    }

    public void clear(View view) {
        scrollView = (ScrollView) findViewById(R.id.scrollView2);
        text.setText("");
    }


    private boolean checkWifiStatus() {
        WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifi.isWifiEnabled();
    }


    public void Connect(View view) {
        Log.i("TAGG", "In connect");
        try {
            // Save the details and connect

            if(!ip.getText().toString().equals(""))
                deviceIp = ip.getText().toString();
            if(!port.getText().toString().equals(""))
                devicePort = Integer.parseInt(port.getText().toString());

            // Connect to device
            if(ip.getText().toString().equals("") | port.getText().toString().equals("")){
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (checkWifiStatus()){
                ConnectToServer connectToServer = new ConnectToServer();
                connectToServer.execute();
                ip_host = ip.getText().toString();
                port_host = Integer.parseInt(port.getText().toString());
            } else {
                Toast.makeText(this, "Wifi Disabled!", Toast.LENGTH_SHORT).show();
            }


        } catch(IllegalStateException exc) {

        }catch(RuntimeException exc) {

        }
    }
    

    public void send(View view) {
       try {
           EditText sendEditText = (EditText)findViewById(R.id.command_edittext);
           if(sendEditText.getText().toString().equals("")) {
               Toast.makeText(this, "Empty command! ", Toast.LENGTH_SHORT).show();

           } else {
               Log.i("TAGG", "In send");
               // Send commands to device
               command = sendEditText.getText().toString();
               Log.i("TAGG", "Calling SendAsync");
               SendMsg sendmsg = new SendMsg();
               sendmsg.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

               Log.i("TAGG", "Called SendAsync");
               sendEditText.setText("");
           }
       } catch(RuntimeException exc) {
           Toast.makeText(this, "No device connected!", Toast.LENGTH_SHORT).show();
       }
    }

    public void Display(final String s, int i){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(i == 0) {
                    text.append(s + "\n");
                } else {
                    text.append(">> " + s +"\n");
                }
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public void disconnectServer(View view) {
        // Disconnect from server when user taps disconnect button
        try {
            s.shutdownInput();
            s.close();
        } catch(IOException exc) {
            Log.i("TAG", "Error closing socket");
        } catch(Exception exc) {
            Toast.makeText(this, "No Device Connected", Toast.LENGTH_LONG).show();
        }

    }

    public void scan(View view) {
        Intent intent = new Intent(this, ShowScannedWifi.class);
        startActivity(intent);
    }

    public void showWifiToast(int i) {
        if(i == 0) {
            Toast.makeText(this, "Host Not Found!", Toast.LENGTH_SHORT).show();
        } else if(i == 1) {
            Toast.makeText(this, "Failed to connect!", Toast.LENGTH_SHORT).show();
        } else if(i == 2) {
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        }
    }

    class ConnectToServer extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void...params){
            try {
                s = new Socket(ip_host,port_host);
                runOnUiThread(new Runnable(){

                    @Override
                    public void run(){
                        //update ui here
                        // display toast here
                        showWifiToast(2);
                    }
                });
                Receive receive = new Receive();
                receive.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch(UnknownHostException exc) {
                Log.i("TAGG", "UNKNOWNHOSTEXCEPTION");
                runOnUiThread(new Runnable(){

                    @Override
                    public void run(){
                        //update ui here
                        // display toast here
                        Toast.makeText(WifiActivity.this, "No host found!", Toast.LENGTH_SHORT).show();
                    }
                });

                //showWifiToast(0);

            } catch(IOException exc) {
                Log.i("TAGG", "IOException");
                runOnUiThread(new Runnable(){

                    @Override
                    public void run(){
                        //update ui here
                        // display toast here
                        showWifiToast(1);
                    }
                });
            } catch(IllegalStateException exc) {

                runOnUiThread(new Runnable(){

                    @Override
                    public void run(){
                        //update ui here
                        // display toast here
                        //showWifiToast(1);
                    }
                });

            } catch(RuntimeException exc) {
                runOnUiThread(new Runnable(){

                    @Override
                    public void run(){
                        //update ui here
                        // display toast here
                    }
                });
            }
            return null;
        }
    }

    class Receive extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void...params){
            Log.i("TAGG", "Started Listening!");
            String msg;
            try {
               while(true) {
                   BufferedReader inFromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
                   msg = inFromServer.readLine().toString();
                   Display(msg, 1);
               }

            } catch (UnknownHostException e) {

                Log.i("TAGG", "UNKNOWNHOSTEXCEPTION in closing socket");
                e.printStackTrace();
            } catch (IOException e) {
                Log.i("TAGG", "IOEXCEPTION in closing socket");
                e.printStackTrace();
            } catch(IllegalStateException exc) {
                Log.i("TAGG", "Illegal  state exception");
            } catch(NullPointerException exc)  {
                Log.i("TAGG", "Caught Exception");
                runOnUiThread(new Runnable(){

                    @Override
                    public void run(){
                        //update ui here
                        // display toast here
                        Toast.makeText(WifiActivity.this, "Device Disconnected!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }
    }

    class SendMsg extends AsyncTask<Void, Void, Void> {
        @Override
        protected  Void doInBackground(Void...params){

            Log.i("TAGG", "In SendAsync");
            try {
                Log.i("TAGG", "Sending : " + command );
                DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());
                outToServer.writeBytes(command);
                Log.i("TAGG", "Sent : " + command );
                Display(command, 0);
            } catch(IOException exc) {
                Log.i("TAGG", "Error sending data");
            } catch(RuntimeException exc) {
                runOnUiThread(new Runnable(){

                    @Override
                    public void run(){
                        //update ui here
                        // display toast here
                        Toast.makeText(WifiActivity.this, "No device Connected!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }
    }
}
