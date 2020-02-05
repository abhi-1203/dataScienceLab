package com.example.datatransferusb;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.util.logging.Logger;


public class MainActivity extends Activity
{

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final String TAG = "_UT";

    UsbManager usbManager;
    usbThread thread;
    boolean usbStarted = false;
    boolean resumeRan = false;
    UsbAccessory accessory;
    ParcelFileDescriptor fileDescriptor;
    FileInputStream inputStream;
    FileOutputStream outputStream;
    TextView t1,t2;
    Button b1,b2;
    EditText etxt;
    PendingIntent mPermissionIntent;
    Context con=this;
    String aa="";


    private void openAccessory() {
        Log.d(TAG, "openAccessory: " + accessory);
        Toast.makeText(this,"Accessory Opened.", Toast.LENGTH_SHORT).show();
        fileDescriptor = usbManager.openAccessory(accessory);
        if (fileDescriptor != null)
        {
            Toast.makeText(this, "file D not null", Toast.LENGTH_SHORT).show();
            usbStarted = true;
            FileDescriptor fd = fileDescriptor.getFileDescriptor();
            inputStream = new FileInputStream(fd);
            outputStream = new FileOutputStream(fd);
            if(inputStream == null || outputStream == null){
                Toast.makeText(this, "input || output stream is null", Toast.LENGTH_SHORT);
                Log.e(TAG, "input || output stream is null");
            }

            thread = new usbThread(this);
            thread.start();
        }
        else{
            Toast.makeText(this, "file D is null", Toast.LENGTH_SHORT);
            Log.e(TAG, "file D is null");

        }
    }

    private final BroadcastReceiver permissionReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action))
            {
                Toast.makeText(context,"Asking for USB permission.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Asking for usb permission");
                synchronized (this)
                {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
                    {
                        if((!usbStarted) && resumeRan)
                        {
                            resumeRan = true;
                            Toast.makeText(context, "Model: "+ accessory.getModel(), Toast.LENGTH_SHORT).show();
                            openAccessory();
                        }
                    }
                    else
                    {
                        Toast.makeText(context, "permission denied for device" , Toast.LENGTH_SHORT).show();
                    }
                }
            }
            else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action))
            {
                unregisterReceiver(permissionReceiver);

                Toast.makeText(context, "Device/Accessory has been removed.",  Toast.LENGTH_SHORT ).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1=(Button)findViewById(R.id.b1);
        t2=(TextView)findViewById(R.id.txt1);
        etxt=(EditText) findViewById(R.id.et1);
        b2=(Button)findViewById(R.id.b2);

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(permissionReceiver, filter);

        //Listener for View Files Button
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReadData();
            }
        });

        //Listener for View Files Button
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData();
            }
        });
    }

    public void sendData(){
        String new1=etxt.getText().toString();
        //String a = t2.getText().toString();
        //t2.setText(etxt.getText().toString());
        byte[] buffer = new byte[2];

        byte[] buffers=etxt.getText().toString().getBytes();
        byte buffer1 = (byte)buffers.length;
        byte buffer2=0;


        if (!new1.equals("")) {
            if (outputStream != null) {
                try {
                    Toast.makeText(this,"Write Success.",Toast.LENGTH_SHORT).show();

                    outputStream.write(buffers);
                } catch (IOException e) {
                    Toast.makeText(this, "Write Failed --" +e, Toast.LENGTH_SHORT).show();
                    //Log.e(TAG, "write failed", e);
                }
            }
            else {
                Toast.makeText(this, "null outputstream", Toast.LENGTH_LONG).show();
            }
            t2.setText(new1 + "\n" +t2.getText().toString());
        } else {
            Toast.makeText(this, "please enter something.", Toast.LENGTH_LONG).show();
            //1.show();
        }
        t2.setMovementMethod(new ScrollingMovementMethod());
    }

    public void ReadData(){
        String readdata = t2.getText().toString();
        byte[] buffer = new byte[1000];
        int  ret = 0;
        try
        {
            if(inputStream != null) {
                int av = inputStream.available();
                if (av > 0)
                    ret = inputStream.read(buffer, 0, av);
                else {
                    Toast.makeText(con,"av is null.", Toast.LENGTH_SHORT).show();
                    Log.d("_UT", "av is null");
                }
            }
            else{
                Toast.makeText(con,"istream is null.", Toast.LENGTH_SHORT).show();
                Log.d("_UT", "instream is null");
            }

         
        if(ret > 0){
            Toast.makeText(con,"MMI_IO_READ-"+"Ret = " +ret, Toast.LENGTH_SHORT).show();
            Log.d("MMI_IO_READ", "Ret = " +ret);
            t2.setText(readdata +"\nRead data from PC: " +ret);
        }else{
            Toast.makeText(con, "NO data received to read", Toast.LENGTH_SHORT).show();
            Log.d("MMI_IO_READ", " NO data to read Ret = " +ret);

        }
		}
		catch (IOException e)
        {
            e.printStackTrace();
            Toast.makeText(con,"IOException on buffer read.", Toast.LENGTH_SHORT).show();
            Log.d("MMI_IO_READ", "IOException on buffer read.");
        }

    }


    Handler usbHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            byte res = msg.getData().getByte("val");
            Toast.makeText(con,"Byte: "+Byte.toString(res), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Byte " + Byte.toString(res));
        }};




    @Override
    protected void onResume()
    {
        super.onResume();

        UsbAccessory[] accessoryList = usbManager.getAccessoryList();
        if (accessoryList != null)
        {
            if(!usbStarted)
            {
                Toast.makeText(this, "Resume, usb not started" , Toast.LENGTH_SHORT).show();
                //Log.d(TAG, "Resume, usb not started");

                accessory = accessoryList[0];
                Toast.makeText(this, "Manufacturer" + accessory.getManufacturer(), Toast.LENGTH_SHORT).show();

                Log.d(TAG, "Manufacturer " + accessory.getManufacturer());
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);
                resumeRan = true;
                usbManager.requestPermission(accessory, mPermissionIntent);
            }
        }
        else{
            Toast.makeText(this, "No Accessory available.", Toast.LENGTH_SHORT).show();

        }
    }

    public class usbThread extends Thread {
        boolean threadCreated = false;

        boolean usbStart;


        boolean tryt = true;
        usbThread(Context cont)
        {
            usbStart = usbStarted;

            if(!threadCreated)
            {
                threadCreated = true;
            }
            else
            {
                tryt = false;
                Toast.makeText(con,"Tried to open second thread", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Tried to open second thread");
            }
        }

        @Override
        public void run()
        {
            while(usbStart && tryt)
            {
                byte[] buffer = new byte[10];
                int  ret = 0;
                try
                {
                    if(inputStream != null) {
                        int av = inputStream.available();
                        if (av > 0)
                            ret = inputStream.read(buffer, 0, av);
                        else {
                            Toast.makeText(con,"av is null.", Toast.LENGTH_SHORT).show();
                            Log.d("_UT", "av is null");
                        }
                    }
                    else{
                        Toast.makeText(con,"istream is null.", Toast.LENGTH_SHORT).show();
                        Log.d("_UT", "instream is null");
                    }

                } catch (IOException e)
                {
                    e.printStackTrace();

                    Log.d("MMI_IO_READ", "IOException on buffer read.");
                }

                if(ret > 0)
                {
                    Toast.makeText(con,"MMI_IO_READ-"+"Ret = " +ret, Toast.LENGTH_SHORT).show();
                    //Log.d("MMI_IO_READ", "Ret = " +ret);
                    for(int i = 0; i < ret; i++)
                    {
                        Message m = usbHandler.obtainMessage();
                        Bundle b = new Bundle();
                        b.putByte("val", buffer[i]);
                        m.setData(b);
                        usbHandler.sendMessage(m);
                    }
                }
            }
            Toast.makeText(con,"Thread Shutdown.", Toast.LENGTH_SHORT).show();
            //Log.d(TAG, "Thread Shutdown");
        }


    }


}
