package com.example.datatransferusb;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;

import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.Runnable;
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
    FileDescriptor fd;
    boolean threadCreated = false;
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
    public boolean mPermissionRequestPending = true;

    Context con=this;
    String aa="";



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1=(Button)findViewById(R.id.b1);
        t2=(TextView)findViewById(R.id.txt2);
        etxt=(EditText) findViewById(R.id.et1);
        b2=(Button)findViewById(R.id.b2);


        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent( ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(permissionReceiver, filter);

        //Listener for View Files Button
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData();

            }
        });
    }




    private void openAccessory(UsbAccessory usbAccessory) {
        Log.e(TAG, "openAccessory: " + usbAccessory);
        Toast.makeText(this,"Accessory Opened.", Toast.LENGTH_SHORT).show();
        fileDescriptor = usbManager.openAccessory(usbAccessory);
        if (fileDescriptor != null)
        {
            usbStarted = true;
            fd = fileDescriptor.getFileDescriptor();
            inputStream = new FileInputStream(fd);
            outputStream = new FileOutputStream(fd);
            thread = new usbThread(usbHandler,inputStream);
            thread.start();
        }
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
                    Log.e(TAG, "write success");

                    outputStream.write(buffers,0, buffer1 );
                } catch (IOException e) {
                    Toast.makeText(this, "Write Failed --" +e, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "write failed", e);
                }
            }
            else {
                Toast.makeText(this, "null outputstream", Toast.LENGTH_LONG).show();
            }
            t2.setText(new1 + "\n" +t2.getText().toString());
            Log.e(TAG, new1 + "\n" +t2.getText().toString());
        } else {
            Toast.makeText(this, "please enter something.", Toast.LENGTH_LONG).show();
            //1.show();
        }
        t2.setMovementMethod(new ScrollingMovementMethod());
    }


    // Close Accessory
    private void CloseAccessory()
    {

        try {
            inputStream.close();
        } catch(IOException e){}

        try {
            outputStream.close();

        }catch(IOException e){}

        super.onDestroy();
        fd = null;
        inputStream = null;
        outputStream = null;
        System.exit(0);
    }


    //USB HANDLER
    final Handler usbHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            byte res = msg.getData().getByte("val");
            Toast.makeText(con,"Byte: "+Byte.toString(res), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Byte " + Byte.toString(res));
        }};


    // Thread class
    private class usbThread extends Thread {
     boolean tryt = true;
     Handler mHandler;
     FileInputStream instream;
     usbThread(Handler h, FileInputStream finstream)
     {
         mHandler = h;
         instream = finstream;
         if(!threadCreated)
         {
             threadCreated = true;
         }
         else
         {
             tryt = false;
             Toast.makeText(con,"Tried to open second thread", Toast.LENGTH_SHORT).show();
             Log.e(TAG, "Tried to open second thread");
         }
     }

     @Override
     public void run()
     {
         while(usbStarted && tryt)
         {
             byte[] buffer = new byte[10];
             int  ret = 0;
             try
             {
                 int av = instream.available();
                 ret = instream.read(buffer, 0, av);

             } catch (IOException e)
             {
                 e.printStackTrace();
                 Toast.makeText(con,"IOException on buffer read.", Toast.LENGTH_SHORT).show();
                 Log.e("MMI_IO_READ", "IOException on buffer read.");
             }

             if(ret > 0)
             {
                 Toast.makeText(con,"MMI_IO_READ-"+"Ret = " +ret, Toast.LENGTH_SHORT).show();
                 Log.e("MMI_IO_READ", "Ret = " +ret);
                 for(int i = 0; i < ret; i++)
                 {
                     Message m = mHandler.obtainMessage();
                     Bundle b = new Bundle();
                     b.putByte("val", buffer[i]);
                     m.setData(b);
                     mHandler.sendMessage(m);
                 }
             }
             else{
                 Toast.makeText(con,"nothing to read: " +ret, Toast.LENGTH_SHORT).show();
                 Log.e("Read", "nothing to read: " +ret);
             }
         }
         Toast.makeText(con,"Thread Shutdown.", Toast.LENGTH_SHORT).show();
         Log.e("UT", "Thread Shutdown");

     }
 }

//Ends here




    @Override
    public void onResume()
    {
        super.onResume();
        Intent intent = getIntent();
        if (inputStream != null && outputStream != null) {
            return;
        }
        UsbAccessory[] accessoryList = usbManager.getAccessoryList();
        UsbAccessory accessory = (accessoryList == null ? null : accessoryList[0]);

        if (accessoryList != null)
        {
            if (usbManager.hasPermission(accessory)) {
                openAccessory(accessory);
            }
            else
            {
                synchronized (permissionReceiver) {
                    if (!mPermissionRequestPending) {
                        usbManager.requestPermission(accessory,mPermissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        }
        else{
            Toast.makeText(this, "No Accessory available.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy()
    {
       // unregisterReceiver(permissionReceiver);
        CloseAccessory();
        super.onDestroy();
    }


    //BroadCast Receiver
    private final BroadcastReceiver permissionReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action))
            {
                Toast.makeText(context,"Asking for USB permission.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Asking for usb permission");
                synchronized (this)
                {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
                    {
                        if((!usbStarted) && resumeRan)
                        {
                            resumeRan = false;
                            Toast.makeText(context, "Model: "+ accessory.getModel(), Toast.LENGTH_SHORT).show();
                            openAccessory(accessory);
                        }
                    }
                    else
                    {
                        Toast.makeText(context, "permission denied for device" , Toast.LENGTH_SHORT).show();
                    }
                    mPermissionRequestPending = false;
                }
            }
            else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action))
            {
                Toast.makeText(context, "Device/Accessory has been removed.",  Toast.LENGTH_SHORT ).show();
                if (accessory != null )//&& accessory.equals(usbaccessory))
                {
                    CloseAccessory();
                }
            }
        }
    };



};



<?xml version="1.0" encoding="utf-8"?>
<!-- Mention target's model and manufacturer details-->
<resources>
    <usb-accessory model="Google" manufacturer="Pixel 3"  version="1.0" uri ="http://www.example.com" description="DataTransferUsb"/>
</resources>


#include "stdafx.h"
#include <stdio.h>
#include <libusb.h>
#include <string.h>
//#include <unistd.h>
#include <inttypes.h>
#include <iostream>


#define ACCESSORY_VID 0x18d1
#define ACCESSORY_PID 0x2D00
#define ACCESSORY_ADB_PID 0x2D01
#define INTERFACE 0


const char* manufacturer = "Google";
const char* modelName = "Google ex";
const char* description = "DataTransferUSB";
const char* version = "1.0";
const char* uri = "http://example.com";
const char* serialNumber = "33fb3b53";

//static

static void error(int code)
{
	fprintf(stdout, "%s\n", libusb_strerror((libusb_error)code));
}

static int shutdown(libusb_device_handle *handle)
{
	if (handle)
	{
		libusb_release_interface(handle, INTERFACE);
		libusb_close(handle);
	}
	libusb_exit(NULL);
	return 0;
}


static int init()
{
	libusb_init(NULL);
	libusb_set_debug(NULL, 3);  //Verbose debugging level

	return 0;
}

// Send AOA specified introdction control information.
static int androidIntroduction(libusb_device_handle *handle)
{
	unsigned char ioBuffer[2];
	int devVersion;
	int response;
	response = libusb_control_transfer(
		handle, //handle
		0xC0, //bmRequestType
		51, //bRequest
		0, //wValue
		0, //wIndex
		ioBuffer, //data
		2, //wLength
		100 //timeout
		);
	fprintf(stdout, "Sent getProtocol\n");

	if (response < 0)
	{

		error(response); return -1;
	}


	fprintf(stdout, "Response \n");

	devVersion = ioBuffer[1] << 8 | ioBuffer[0];
	if (!(devVersion == 1 || devVersion == 2))
		return -1;
	fprintf(stdout, "Version Code Device: %d\n", devVersion);

	Sleep(1000);//sometimes hangs on the next transfer :(

	response = libusb_control_transfer(handle, 0x40, 52, 0, 0, (unsigned char*)manufacturer, strlen(manufacturer) + 1, 0);
	if (response < 0)
	{
		fprintf(stdout, "sent 1\n");
		error(response); return -1;
	}
	response = libusb_control_transfer(handle, 0x40, 52, 0, 1, (unsigned char*)modelName, strlen(modelName) + 1, 0);
	if (response < 0)
	{
		fprintf(stdout, "sent 2\n");
		error(response); return -1;
	}
	response = libusb_control_transfer(handle, 0x40, 52, 0, 2, (unsigned char*)description, strlen(description) + 1, 0);
	if (response < 0)
	{
		fprintf(stdout, "sent 3\n");
		error(response); return -1;
	}
	response = libusb_control_transfer(handle, 0x40, 52, 0, 3, (unsigned char*)version, strlen(version) + 1, 0);
	if (response < 0)
	{
		fprintf(stdout, "sent 4\n");
		error(response); return -1;
	}
	response = libusb_control_transfer(handle, 0x40, 52, 0, 4, (unsigned char*)uri, strlen(uri) + 1, 0);
	if (response < 0)
	{
		fprintf(stdout, "sent 5\n");
		error(response); return -1;
	}
	response = libusb_control_transfer(handle, 0x40, 52, 0, 5, (unsigned char*)serialNumber, strlen(serialNumber) + 1, 0);
	if (response < 0)
	{
		fprintf(stdout, "sent 6\n");

		error(response); return -1;
	}

	fprintf(stdout, "Accessory Identification sent\n");

	return 1;
}

// Send introduction information to given handle, then try to put it into
// accessory mode and catch it once it reconnects.
static libusb_device_handle* setupAccessory(libusb_device_handle *handle1)
{
	int response;
	response = androidIntroduction(handle1);
	if (response < 0)
		return NULL;
	response = libusb_control_transfer(handle1, 0x40, 53, 0, 0, NULL, 0, 0);
	if (response < 0){ error(response); return NULL; }

	fprintf(stdout, "Attempted to put device into accessory mode...\n");


	libusb_device_handle *androidHandle;

	int tries = 4;
	while (true)
	{
		tries--;
		if ((androidHandle = libusb_open_device_with_vid_pid(NULL, ACCESSORY_VID, ACCESSORY_PID)) == NULL)
		{
			//if ((androidHandle = libusb_open_device_with_vid_pid(NULL, ACCESSORY_VID, ACCESSORY_PID)) == NULL)
			//{
				if (tries < 0)
				{
					std::cout << "Could not..." << '\n';
					return NULL;
				}
			//}
			//else
			//{


			//	break;
			//}
		}
		else
		{
			fprintf(stdout, "Device is in accessory mode...\n");
			break;
		}
		Sleep(1000000);
	}

	return androidHandle;
}

//Find the first Bulk OUT Enpoint of the given device. 
uint8_t findBulkOut(libusb_device *device)
{
	libusb_config_descriptor *con_desc;
	libusb_get_active_config_descriptor(device, &con_desc);
	const libusb_interface *interfaceList = con_desc->interface;
	uint16_t numInterface = con_desc->bNumInterfaces;
	for (int j = 0; j<numInterface; j++)
	{
		libusb_interface interface = interfaceList[j];
		const libusb_interface_descriptor *intDescList = interface.altsetting;
		int numAlt = interface.num_altsetting;
		for (int p = 0; p < numAlt; p++)
		{
			libusb_interface_descriptor intDesc = intDescList[p];
			uint8_t numEnd = intDesc.bNumEndpoints;
			const libusb_endpoint_descriptor *ends = intDesc.endpoint;
			for (int k = 0; k < numEnd; k++)
			{
				libusb_endpoint_descriptor endpoint = ends[k];
				uint8_t type = 0x03 & endpoint.bmAttributes;
				uint8_t address = endpoint.bEndpointAddress;
				switch (type) {
				case LIBUSB_TRANSFER_TYPE_CONTROL:
					break;
				case LIBUSB_TRANSFER_TYPE_ISOCHRONOUS:
					break;
				case LIBUSB_TRANSFER_TYPE_BULK:
					if ((address & LIBUSB_ENDPOINT_IN)) //LIBUSB_ENPOINT_OUT is simply 0000, can't AND that...
					{
						return address;
					}
					else if (!address & LIBUSB_ENDPOINT_IN){
						return address;
					}
					break;
				case LIBUSB_TRANSFER_TYPE_INTERRUPT:
					break;
				case LIBUSB_TRANSFER_TYPE_BULK_STREAM:
					break;
				}
			}
		}
	}
}

//Basically findBulkOut, but with output for EndPoints.
void printEnds(libusb_device *device)
{
	libusb_config_descriptor *con_desc;
	libusb_get_active_config_descriptor(device, &con_desc);
	const libusb_interface *interfaceList = con_desc->interface;
	uint16_t numInterface = con_desc->bNumInterfaces;
	for (int j = 0; j<numInterface; j++)
	{
		libusb_interface interface = interfaceList[j];
		const libusb_interface_descriptor *intDescList = interface.altsetting;
		int numAlt = interface.num_altsetting;
		for (int p = 0; p < numAlt; p++)
		{
			libusb_interface_descriptor intDesc = intDescList[p];
			uint8_t numEnd = intDesc.bNumEndpoints;
			const libusb_endpoint_descriptor *ends = intDesc.endpoint;
			fprintf(stdout, "Interface %d. altSetting %d. Num of endpoints: %d\n", p, intDesc.bInterfaceNumber, numEnd);
			for (int k = 0; k < numEnd; k++)
			{
				libusb_endpoint_descriptor endpoint = ends[k];
				uint8_t type = 0x03 & endpoint.bmAttributes;
				uint8_t address = endpoint.bEndpointAddress;
				fprintf(stdout, "Endpoint type ");
				switch (type) {
				case LIBUSB_TRANSFER_TYPE_CONTROL:
					std::cout << "Control";
					break;
				case LIBUSB_TRANSFER_TYPE_ISOCHRONOUS:
					std::cout << "Isochronus";
					break;
				case LIBUSB_TRANSFER_TYPE_BULK:
					std::cout << "Bulk";
					break;
				case LIBUSB_TRANSFER_TYPE_INTERRUPT:
					std::cout << "Interupt";
					break;
				case LIBUSB_TRANSFER_TYPE_BULK_STREAM:
					std::cout << "Bulk Stream";
					break;
				}
				std::cout << " ";
				std::cout << (address & LIBUSB_ENDPOINT_IN ? "IN" : "OUT");
				std::cout << '\n';
				fprintf(stdout, "Address %04X\n", address);
			}
		}
	}
}

// Go through all connected devices. If they are do not have Google PID
// and VID, try to find out if they are Android devices. If they (most likely)
// are, try to put them in accessory mode. If successful, return that handle
libusb_device_handle* getAndroidHandle()
{
	libusb_device **list;
	ssize_t cnt = libusb_get_device_list(NULL, &list);
	ssize_t i = 0;
	int err = 0;
	if (cnt < 0)
		error(0);
	for (i = 0; i < cnt; i++)
	{
		fprintf(stdout, "\nAttempted index %d\n", (int)i);
		libusb_device *device = list[i];
		libusb_device_descriptor dev_desc;
		libusb_get_device_descriptor(device, &dev_desc);
		uint16_t VID = dev_desc.idVendor;
		uint16_t PID = dev_desc.idProduct;
		fprintf(stdout, "VID: %04X. PID: %04X\n", VID, PID);

		libusb_device_handle *handle;
		int response = libusb_open(device, &handle);
		if (response < 0)
		{
			error(response); continue;
		}
		libusb_set_auto_detach_kernel_driver(handle, 1);

		libusb_device_handle *androidHandle;
		if (VID == ACCESSORY_VID && (PID == ACCESSORY_PID || PID == ACCESSORY_ADB_PID))
		{
			

			int r = androidIntroduction(handle);
			if (r != 1)
				continue;
			androidHandle = handle;
		}
		else
		{
			androidHandle = setupAccessory(handle);
			libusb_close(handle);

		}
		if (androidHandle)
		{
			libusb_free_device_list(list, 1);
			std::cout << "\n\nAndroid Found:" << '\n';
			printEnds(libusb_get_device(androidHandle));
			return androidHandle;
		}
	}
	libusb_free_device_list(list, 1);
	return NULL;
}

//Try to send data.
static int transferTest(libusb_device_handle *handle)
{
	const static int PACKET_BULK_LEN = 128;
	const static int TIMEOUT = 5000;
	int r, i;
	int transferred;
	Sleep(1000000); //1s

	//libusb_set_configuration(handle, 1);
	r = libusb_claim_interface(handle, INTERFACE);
	if (r < 0)
	{
		error(r); return -1;
	}
	fprintf(stdout, "Interface claimed, ready to transfer data\n");
	// TEST BULK IN/OUT
	Sleep(1000000);// 0.1s
	uint8_t outAddress = findBulkOut(libusb_get_device(handle));
	uint8_t inAddress = findBulkOut(libusb_get_device(handle));
	fprintf(stdout, "Trying to write to %04X\n", outAddress);
	char answer[PACKET_BULK_LEN];
	char question[PACKET_BULK_LEN];
	for (i = 0; i<PACKET_BULK_LEN; i++) question[i] = i;

	// ***TIMES OUT HERE***
	r = libusb_bulk_transfer(handle, outAddress, (unsigned char*)question, PACKET_BULK_LEN,
		&transferred, TIMEOUT);
	if (r < 0)
	{
		std::cout << "Tried to write" << question << "\n";
		fprintf(stderr, "Bulk write error %d\n", r);
		error(r);
		fprintf(stderr, "Number of bytes written %d\n", transferred);
		return r;
	}
	fprintf(stdout, "Wrote %d bytes", transferred);

	r = libusb_bulk_transfer(handle, inAddress, (unsigned char*)answer, PACKET_BULK_LEN,
		&transferred, TIMEOUT);
	if (r < 0)
	{
		fprintf(stderr, "Bulk read error %d\n", r);
		error(r);
		return r;
	}
	fprintf(stdout, "Read %d bytes", r);
	if (transferred < PACKET_BULK_LEN)
	{
		fprintf(stderr, "Bulk transfer short read (%d)\n", r);
		error(r);
		return -1;
	}
	printf("Bulk Transfer Loop Test Result:\n");
	//     for (i=0;i< PACKET_BULK_LEN;i++) printf("%i, %i,\n ",question[i],answer[i]);
	for (i = 0; i < PACKET_BULK_LEN; i++)
	{
		if (i % 8 == 0)
			printf("\n");
		printf("%02x, %02x; ", question[i], answer[i]);
	}
	printf("\n\n");
	return 0;
}

int main(int argc, char *argv[])
{
	fprintf(stdout, "OUT flag %04X IN flag %04X\n", LIBUSB_ENDPOINT_OUT, LIBUSB_ENDPOINT_IN);
	fprintf(stdout, "Shifted in %04X\n", LIBUSB_ENDPOINT_IN >> 7);
	if (init() < 0)
		return -1;

	libusb_device_handle *handle = getAndroidHandle();
	if (!handle)
	{
		fprintf(stdout, "\nError setting up accessory\n");
		shutdown(NULL);
		return -1;
	};
	if (transferTest(handle) < 0)
	{
		fprintf(stdout, "\nError in transferTest\n");
		shutdown(handle);
		return -1;
	}
	shutdown(handle);
	fprintf(stdout, "\nFinished\n");
	return 0;
}
