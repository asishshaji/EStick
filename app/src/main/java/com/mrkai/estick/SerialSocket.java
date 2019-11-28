package com.mrkai.estick;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.telecom.TelecomManager;
import android.telephony.SmsManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Executors;

class SerialSocket implements Runnable {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final UUID BLUETOOTH_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BroadcastReceiver disconnectBroadcastReceiver;
    private Context context;
    private SerialListener listener;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private boolean connected;
    private String mCurrentPhotoPath;

    SerialSocket() {
        disconnectBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (listener != null)
                    listener.onSerialIoError(new IOException("background disconnect"));
                disconnect(); // disconnect now, else would be queued until UI re-attached
            }
        };
    }

    /**
     * connect-success and most connect-errors are returned asynchronously to listener
     */
    void connect(Context context, SerialListener listener, BluetoothDevice device) throws IOException {
        if (connected || socket != null)
            throw new IOException("already connected");
        this.context = context;
        this.listener = listener;
        this.device = device;
        context.registerReceiver(disconnectBroadcastReceiver, new IntentFilter(Constants.INTENT_ACTION_DISCONNECT));
        Executors.newSingleThreadExecutor().submit(this);
    }

    void disconnect() {
        listener = null; // ignore remaining data and errors
        // connected = false; // run loop will reset connected
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
        }
        try {
            context.unregisterReceiver(disconnectBroadcastReceiver);
        } catch (Exception ignored) {
        }
    }

    void write(byte[] data) throws IOException {
        if (!connected)
            throw new IOException("not connected");
        socket.getOutputStream().write(data);
    }

    String read() {
        String dataS = null;
        try {
            byte[] buffer = new byte[1024];
            int len;
            //noinspection InfiniteLoopStatement
            while (true) {
                len = socket.getInputStream().read(buffer);
                byte[] data = Arrays.copyOf(buffer, len);
                dataS = new String(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("Via", "read: insideRead" + dataS);
        return dataS;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void run() { // connect & read
        try {
            socket = device.createRfcommSocketToServiceRecord(BLUETOOTH_SPP);
            socket.connect();
            if (listener != null)
                listener.onSerialConnect();
        } catch (Exception e) {
            if (listener != null)
                listener.onSerialConnectError(e);
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
            return;
        }
        connected = true;
        SharedPreferences prefs = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);

        try {
            byte[] buffer = new byte[1024];
            int len;
            //noinspection InfiniteLoopStatement
            while (true) {
                len = socket.getInputStream().read(buffer);
                byte[] data = Arrays.copyOf(buffer, len);

                Log.d("Via", "run: " + new String(data));


//A SAS *
//G GoogleAssistant *
//M Maps
//C Camera
//S S0S *


                if (new String(data).contains("G")) {
                    context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.google.android.apps.googleassistant"));
                } else if (new String(data).equals("S")) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(prefs.getString("guardianphonenumber", "9400376256"), null, prefs.getString("user", "Asish") + "in trouble at location Please help", null, null);
                } else if (new String(data).equals("A")) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(prefs.getString("guardianphonenumber", "9400376256"), null, prefs.getString("user", "Asish") + " is in not feeling good, at location Please help", null, null);

                } else if (new String(data).equals("M")) {
                    context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.google.android.apps.maps"));
                } else if (new String(data).equals("C")) {
                    context.startActivity(new Intent(".CAM"));
                } else if (new String(data).equals("X")) {
                    TelecomManager tm = (TelecomManager) context
                            .getSystemService(Context.TELECOM_SERVICE);

                    if (tm == null) {
                        // whether you want to handle this is up to you really
                        throw new NullPointerException("tm == null");
                    }

                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        tm.acceptRingingCall();

                    }
                }
                if (listener != null)
                    listener.onSerialRead(data);
            }
        } catch (Exception e) {
            connected = false;
            if (listener != null)
                listener.onSerialIoError(e);
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

}
