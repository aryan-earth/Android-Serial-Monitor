package com.qass.serialmonitor;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
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
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsbActivity extends AppCompatActivity {

    private static boolean deviceConnected = false;
    private static final String ACTION_USB_PERMISSION = "com.example.serialmonitor.USB_PERMISSION";
    private static String deviceName = "";
    private byte[] bytes;
    private static int TIMEOUT = 0;
    private boolean forceClaim = true;
    private ScrollView scrollView3;
    private TextView text3;
    private Button clear;
    private EditText sendEditText;
    private static UsbSerialPort sPort = null;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    public static UsbInterface usbInterface;
    public static UsbEndpoint usbEndpoint;
    public static UsbRequest usbRequest;
    public static UsbDeviceConnection connection;
    public static UsbDevice device;
    public static UsbManager manager;
    private SerialInputOutputManager ioManager;
    Context context;
    public static UsbSerialPort serialPort;
    public static UsbSerialDriver driver;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d("TAG", "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    UsbActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UsbActivity.this.updateReceivedData(data);
                        }
                    });
                }
            };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb);
        Toolbar toolbar = findViewById(R.id.usb_activity_toolbar);
        setSupportActionBar(toolbar);
        scrollView3 = (ScrollView)findViewById(R.id.scrollView3);
        text3 = (TextView)findViewById(R.id.text3);
        text3.setMovementMethod(new ScrollingMovementMethod());
        clear = (Button)findViewById(R.id.clear_button);
        sendEditText = (EditText)findViewById(R.id.send_command_usb);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        manager = (UsbManager)getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();


        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter1 = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter1);
        while(deviceIterator.hasNext()){
            device = deviceIterator.next();



        }


        if(manager.getDeviceList().isEmpty()) {
            ImageView img = (ImageView)findViewById(R.id.device_status_imageview);
            img.setImageResource(R.drawable.ic_action_device_detached);
            deviceConnected = false;
            Toast.makeText(this, "No device attached", Toast.LENGTH_SHORT).show();
        } else {
            deviceConnected = true;
            deviceAttached();

        }
        //TODO; Read from USB

        /*if(deviceConnected){
            manager.requestPermission(device, permissionIntent);
            driver = UsbSerialProber.getDefaultProber().probeDevice(device);
            serialPort = driver.getPorts().get(0);
            connection = manager.openDevice(device);
            try {
                serialPort.open(connection);
                serialPort.setParameters(9600, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                serialPort.setDTR(true); // for arduino, ...
                serialPort.setRTS(true);
                ReceiveIn receiveIn = new ReceiveIn();
                receiveIn.execute();
            } catch (IOException exc) {
                Log.i("TAG", "IOEXCEPTION");
            }

        }*/



        IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(mUsbAttachReceiver , filter);
        filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbDetachReceiver , filter);

    }




    private void updateReceivedData(byte[] data) {
        final String message = "Read " + data.length + " bytes: \n"
                + HexDump.dumpHexString(data) + "\n\n";
        //text3.append(message);
        //scrollView3.smoothScrollTo(0, text3.getBottom());
        DisplayMsg(message, 1);
    }




    public void sendCommandsUsb(View view) {
        //Toast.makeText(this, "In send  Usb", Toast.LENGTH_SHORT).show();
        if(deviceConnected) {
            Log.i("TAG", "In sendUsb");
            if(sendEditText.getText().toString().equals("")) {
                Toast.makeText(this, "Empty command", Toast.LENGTH_SHORT).show();
            } else {
                SendOut sendOut = new SendOut();
                sendOut.execute();
                DisplayMsg(sendEditText.getText().toString(), 0);

            }
        } else {
            Toast.makeText(this, "No device Attached!", Toast.LENGTH_SHORT).show();
        }

    }


    public void clearUsb(View view) {
        scrollView3 = (ScrollView) findViewById(R.id.scrollView3);
        text3.setText("");
    }

    public void DisplayMsg(final String s, int i){
        Log.i("TAG", "In display");
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(i == 0) {
                    text3.append(s + "\n");
                } else {
                    text3.append(">> " + s +"\n");
                }
                scrollView3.fullScroll(View.FOCUS_DOWN);
            }
        });
        sendEditText.setText("");
    }
    BroadcastReceiver mUsbAttachReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if(device.getVendorId() == 9025) {
                deviceName = "Arduino";
            }



            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                deviceAttached(device);
            }
        }
    };



    BroadcastReceiver mUsbDetachReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
               deviceDetached();
            }
        }
    };

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            //call method to set up device communication
                        }
                    }
                    else {
                        Log.d("TAG", "permission denied for device " + device);
                    }
                }
            }
        }
    };

    private void deviceAttached(UsbDevice device) {
        Toast.makeText(this, "Device attached", Toast.LENGTH_SHORT).show();
        ImageView img = (ImageView)findViewById(R.id.device_status_imageview);
        img.setImageResource(R.drawable.ic_action_device_attached);
        deviceConnected = true;
        if(device.getVendorId() == 9025)
            Toast.makeText(this, "Device name: Arduino Uno", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "Device name: Unknown", Toast.LENGTH_LONG).show();

    }

    private void deviceAttached() {
        Toast.makeText(this, "Device attached", Toast.LENGTH_SHORT).show();
        ImageView img = (ImageView)findViewById(R.id.device_status_imageview);
        img.setImageResource(R.drawable.ic_action_device_attached);
        deviceConnected = true;
    }

    private void deviceDetached(UsbDevice device) {
        Toast.makeText(this, "Device detached", Toast.LENGTH_SHORT).show();
        ImageView img = (ImageView)findViewById(R.id.device_status_imageview);
        img.setImageResource(R.drawable.ic_action_device_detached);
        deviceConnected = false;
    }

    private void deviceDetached() {
        Toast.makeText(this, "Device detached", Toast.LENGTH_SHORT).show();
        ImageView img = (ImageView)findViewById(R.id.device_status_imageview);
        img.setImageResource(R.drawable.ic_action_device_detached);
        deviceConnected = false;
    }

    public void showInfo(View view) {
        if(deviceConnected) {
            // Show device info toast
            if(!deviceName.equals("")) {
                if(deviceName.equals("Arduino")) {
                    // Arduino
                    Toast.makeText(this, "Device name: " + deviceName, Toast.LENGTH_LONG).show();

                } else {
                    // unknown device
                    Toast.makeText(this, "Device name: " + "Unknown", Toast.LENGTH_LONG).show();
                }
            }


        } else {
            Toast.makeText(this, "No device attached", Toast.LENGTH_SHORT).show();

        }

    }

    public void showUsbToast() {
        Toast.makeText(this, "No Device Attached!", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbDetachReceiver);
        unregisterReceiver(mUsbAttachReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private Context getAppContext() {
        context = getApplicationContext();
        return context;
    }



    public class SendOut extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void...params){
            byte[] data = sendEditText.getText().toString().getBytes();
            Log.i("TAG", "In Async task");
            // Send messages to arduino
            driver = UsbSerialProber.getDefaultProber().probeDevice(device);
            serialPort = driver.getPorts().get(0);
            connection = manager.openDevice(device);

            try {
                serialPort.open(connection);
                serialPort.setParameters(9600, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                serialPort.setDTR(true); // for arduino, ...
                serialPort.setRTS(true);
                serialPort.write(data, 0);
            } catch(Exception exc) {
                showUsbToast();
            }

            return null;
        }
    }


    public class ReceiveIn extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void...params){
            byte buffer[] = new byte[16];
            String s = "none";
            try {
                int numBytesRead = serialPort.read(buffer, 1000);
                if(numBytesRead > 0) {
                    s = new String(buffer, 0, numBytesRead);
                    DisplayMsg(s, 1);
                }
            } catch(IOException exc) {

            }
            return null;
        }
    }

}
