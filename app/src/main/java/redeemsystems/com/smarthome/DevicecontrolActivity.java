package redeemsystems.com.smarthome;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DevicecontrolActivity extends AppCompatActivity
{
    Switch switch1;
    byte[] res;
    private String mDeviceName;
    private String mDeviceAddress;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public Bluetoothservice mBluetoothLeService;
    public boolean mConnected = false;
    public BluetoothGattCharacteristic characteristicTX;
    public BluetoothGattCharacteristic characteristicRX;
    public final static UUID HM_RX_TX =
            UUID.fromString(SampleGattAttributes.HM_RX_TX);
    String str;
    String uuid;
    String ph_uuid;
    static TextView msg;

    public final String LIST_NAME = "NAME";
    public final String LIST_UUID = "UUID";

    public final ServiceConnection mServiceConnection = new ServiceConnection() {

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            try {
                mBluetoothLeService = ((Bluetoothservice.LocalBinder) service).getService();
                if (!mBluetoothLeService.initialize()) {
                    Log.v("check", "Unable to initialize Bluetooth");
                    return;
                }
                // Automatically connects to the device upon successful start-up initialization.
                try {
                    mBluetoothLeService.connect(mDeviceAddress);
                } catch (Throwable e) {
                    e.printStackTrace();
                    Log.d("check", e.toString());
                }
            }catch (Throwable e){
                e.printStackTrace();
                Log.d("sus",e.toString());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                final String action = intent.getAction();
                if (Bluetoothservice.ACTION_GATT_CONNECTED.equals(action)) {
                    mConnected = true;
                    Toast.makeText(DevicecontrolActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                } else if (Bluetoothservice.ACTION_GATT_DISCONNECTED.equals(action)) {
                    mConnected = false;
                    Toast.makeText(DevicecontrolActivity.this, "DisConnected", Toast.LENGTH_SHORT).show();
                } else if (Bluetoothservice.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    Log.i("Swapnil", "reached");
                    // Show all the supported services and characteristics on the user interface.
                    displayGattServices(mBluetoothLeService.getSupportedGattServices());
                    if (characteristicTX == null) {
                        Log.i("Sw", "Its not nullllllll");
                    }
                } else if (Bluetoothservice.EXTRA_DATA.equals(action))
                {
                    Log.d("joan", "extra data");

                    displayGattServices(mBluetoothLeService.getSupportedGattServices());
                    Log.d("joan", "extra data display gatt");

                    if(characteristicRX == null)
                    {
                        makeChange();
                        Log.d("joan", "extra data make change");

                    }
                }
            }catch (Throwable e){
                e.printStackTrace();
                Log.d("sus",e.toString());
            }
        }
    };
    BluetoothDevice mDevice;
    String writeMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devicecontrol);
        switch1 = findViewById(R.id.switch1);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        msg = findViewById(R.id.msg);
        Intent gattServiceIntent = new Intent(DevicecontrolActivity.this, Bluetoothservice.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                writeMessage = "QUEsVjAwMDEsTG9jayxBQgA=";   //Lock
                writeMessage = "QUEsVjAwMDEsVW5sb2NrLEFCAA==";  //Unlock
//                writeMessage = "Room1";
//                if (isChecked) {
//                    writeMessage = "QUEsVjAwMDEsTG9jayxBQgA=";
//                }
//                else {
//                    writeMessage = "QUEsVjAwMDEsVW5sb2NrLEFCAA==";
//                }
                makeChange();
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onResume() {
        super.onResume();
        try {
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            if (mBluetoothLeService != null) {
                final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                Log.v("check", "Connect request result=" + result);
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            Log.d("sus", e.toString());
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mGattUpdateReceiver);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            Log.d("sus", e.toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            Log.d("sus", e.toString());
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            Log.i("Sw","Services not availabele");
            return;
        }
        uuid = null;
        String unknownServiceString = "Unknown Service";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();


        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            try {
                HashMap<String, String> currentServiceData = new HashMap<String, String>();
                uuid = gattService.getUuid().toString();
                Log.i("Sw ", "UUID: " + uuid);
                currentServiceData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));

                // If the service exists for HM 10 Serial, say so.
                if (SampleGattAttributes.lookup(uuid, unknownServiceString) == "HM 10 Serial") {
                } else {
                }
                currentServiceData.put(LIST_UUID, uuid);
                gattServiceData.add(currentServiceData);

                // get characteristic when UUID matches RX/TX UUID
                characteristicTX = gattService.getCharacteristic(Bluetoothservice.UUID_HM_RX_TX);
                characteristicRX = gattService.getCharacteristic(Bluetoothservice.UUID_HM_RX_TX);

                Log.i("Sw", characteristicTX == null ? "Itsnull" : "wsbhd");
                Log.i("Sw", "About to call make change");
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.d("check", e.toString());
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                Log.d("check", e.toString());
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void makeChange() {


        final byte[] tx = writeMessage.getBytes();
        if(mConnected) {
            try {
                Log.i("Sw","mConnected");
                byte[] tx1 = Arrays.copyOfRange(tx, 0, tx.length/2);
                characteristicTX.setValue(tx1);
                mBluetoothLeService.writeCharacteristic(characteristicTX);


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        byte[] tx2 = Arrays.copyOfRange(tx, (tx.length/2), tx.length);

                        characteristicTX.setValue(tx2);
                        Log.d("joan", Arrays.toString(tx2));
                        mBluetoothLeService.writeCharacteristic(characteristicTX);
                    }
                }, 150);


                mBluetoothLeService.setCharacteristicNotification(characteristicRX,true);
                characteristicRX.getValue();
                mBluetoothLeService.readCharacteristic(characteristicRX);
                Toast.makeText(this, writeMessage, Toast.LENGTH_LONG).show();
                Log.d("sus", Arrays.toString(tx));
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                Log.d("check", e.toString());
            }
        }
    }
    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Bluetoothservice.ACTION_GATT_CONNECTED);
        intentFilter.addAction(Bluetoothservice.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(Bluetoothservice.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(Bluetoothservice.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


}
