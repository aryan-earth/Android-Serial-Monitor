package com.qass.serialmonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class BluetoothActivity extends AppCompatActivity {

    private int REQUEST_ENABLE_BT = 1;
    public static EditText sendCommandsEditText;
    public static Button sendButton;
    public static BluetoothSocket mmSocket;
    public static List<BtCommands> commandsList = new ArrayList<>();
    public static ArrayList<String> string_commands = new ArrayList<>();
    public static int count = 0;
    public static String instructions = "none";
    private static Context context;
    private ScrollView scrollView;
    private TextView text;
    private Button clear;
    public static boolean deviceDisconnected = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        Toolbar toolbar = findViewById(R.id.bluetooth_activity_toolbar);
        setSupportActionBar(toolbar);
        BluetoothActivity.context = getApplicationContext();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Your device doesn't support bluetooth", Toast.LENGTH_LONG);
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        sendCommandsEditText = (EditText) findViewById(R.id.send_command_bt);
        sendButton = (Button) findViewById(R.id.send_button_bt);
        text = (TextView)findViewById(R.id.text);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        text.setMovementMethod(new ScrollingMovementMethod());
        clear = (Button)findViewById(R.id.clear_button);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(BtDeviceDisconnected, filter);

    }

    BroadcastReceiver BtDeviceDisconnected = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                showToast(0);
                deviceDisconnected = true;
            }

        }
    };

    public boolean checkBluetoothStatus() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter.isEnabled()) {
            // Bluetooth is enabled
            return true;
        } else return false;
    }

    public static Context getAppContext() {
        return BluetoothActivity.context;
    }


    public void scanDevices(View view) {

        Intent intent = new Intent(BluetoothActivity.this, showScannedBtDevices.class);
        startActivity(intent);

    }

    public void clearBt(View view) {
        // Clear Scroll view
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        text.setText("");
    }

    public void showToast(int i) {
        if(i == 0)
            Toast.makeText(this, "Device Disconnected!", Toast.LENGTH_SHORT).show();
        else if(i == 1)
            Toast.makeText(this, "Error Connecting!", Toast.LENGTH_SHORT).show();
        else if(deviceDisconnected) {
            Toast.makeText(this, "Device Disconnected!", Toast.LENGTH_SHORT).show();
        }

    }

    public static void failedConnecting() {
        Toast.makeText(getAppContext(), "Error Connecting!", Toast.LENGTH_SHORT).show();
    }




    public void showPairedDevices(View view) {
        Intent intent = new Intent(BluetoothActivity.this, showPairedBtDevices.class);
        startActivity(intent);
        // Start listening
        Receive receive = new Receive();
        receive.execute();

    }

    public void sendCommandsBt(View view) {
        // Check if bluetooth is off
        if(!checkBluetoothStatus()) {
            Toast.makeText(this, "Please turn on Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            String command = sendCommandsEditText.getText().toString();
            if(!command.equals("")) {
                try {
                    ConnectedThread ob = new ConnectedThread(mmSocket);
                    byte[] bytes = command.getBytes();
                    ob.write(bytes);
                    if(!deviceDisconnected) {
                        commandsList.add(new BtCommands(count, command));
                        count++;
                        DisplaySent(command);
                    } else showToast(1);
                } catch(IllegalStateException exc) {
                    Toast.makeText(this, "No device Connected!", Toast.LENGTH_SHORT).show();
                } catch(NullPointerException exc) {
                    Toast.makeText(this, "No device Connected!", Toast.LENGTH_SHORT).show();
                }

                sendCommandsEditText.setText("");
            }
        }

    }

    public void DisplaySent(final String s){
        this.runOnUiThread(() -> {
                text.append(s + "\n");
            scrollView.fullScroll(View.FOCUS_DOWN);
        });
    }

    public void DisplayReceived(final String s) {
        Log.i("TAG", "In display Received");
        text.append(">> " + s +"\n");
    }



    public static class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        private UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;


            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.

                tmp = device.createRfcommSocketToServiceRecord(DEFAULT_UUID);
            } catch (IOException e) {
                Log.i("TAGG", "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {

            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();
            Log.i("TAGG", "CANCELLED DISCOVERY");

            try {
                Log.i("TAGG", "In try");
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.i("TAGG", "Connected to device");
                deviceDisconnected = false;
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                Log.i("TAGG", "Unable to  connect");
                failedConnecting();
                deviceDisconnected = true;
                try {
                    mmSocket.close();
                    Log.i("TAGG", "Socket Closed");
                } catch (IOException closeException) {
                    Log.i("TAGG", "Could not close the client socket", closeException);
                }
                return;
            }


        }


        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public  class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.i("TAG", "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.i("TAG", "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
                mmBuffer = new byte[1024];
                int numBytes; // bytes returned from read()

                // Keep listening to the InputStream until an exception occurs.
                while (true) {
                    try {

                        // Read from the InputStream.
                        numBytes = mmInStream.read(mmBuffer);
                        if(numBytes > 4) break;
                        instructions = new String(mmBuffer, 0, numBytes);
                        Log.i("TAG", "no of bytes " + numBytes);
                        Log.i("TAGG", "Instructions: " + instructions);
                        if(numBytes > 0) {
                            DisplayReceived(instructions);
                        }
                    } catch (IOException e) {
                        //Log.i("TAG", "Input stream was disconnected");
                        break;
                    }catch(RuntimeException exc) {
                        //Log.i("TAG", "Runtime Exception caught in run()");
                        break;
                    }
                }
                Log.i("TAG",  "Exiting run in ConnectedThread");
        }

        public void write(byte[] bytes) {
            Log.i("TAG",  "In write in ConnectedThread");
            try {
                mmOutStream.write(bytes);


            } catch (IOException e) {

            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.i("TAG", "Could not close the connect socket", e);
            }
        }
    }

    public class Receive extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void...params){
           while(true) {
               try {

                   ConnectedThread ct = new ConnectedThread(mmSocket);
                   ct.run();
               } catch(RuntimeException exc) {

               }
           }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnectedThread ct = new ConnectedThread(mmSocket);
        ct.cancel();
    }
}
