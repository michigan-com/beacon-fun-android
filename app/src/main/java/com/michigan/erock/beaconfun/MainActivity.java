package com.michigan.erock.beaconfun;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.Nearable;
import com.estimote.sdk.Region;
import com.estimote.sdk.cloud.EstimoteCloud;
import com.estimote.sdk.cloud.model.BeaconInfo;
import com.estimote.sdk.connection.BeaconConnection;
import com.estimote.sdk.exception.EstimoteDeviceException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String EXTRAS_TARGET_ACTIVITY = "extrasTargetActivity";
    public static final String EXTRAS_BEACON = "extrasBeacon";

    private BeaconManager beaconManager;
    private BeaconListAdapter adapter;
    private String scanId;

    private static final int REQUEST_ENABLE_BT = 1234;
    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EstimoteSDK.initialize(this, "i-pd-beacon-test", "7d06e8ad396ff7a5c429e3cb07ed16bc");
        EstimoteSDK.enableDebugLogging(true);

        adapter = new BeaconListAdapter(this);

        ListView list = (ListView) findViewById(R.id.beacon_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(createOnItemClickListener());

        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //toolbar.setSubtitle("Found beacons: " + beacons.size());
                        adapter.replaceWith(beacons);
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        beaconManager.disconnect();
        super.onDestroy();
    }

    protected void startListening() {
        if (!beaconManager.hasBluetooth()) {
            Toast.makeText(this, "Device do not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
            return;
        }

        if (!beaconManager.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            connectToService();
        }
    }

    protected void stopListening() {
        try {
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
        } catch (RemoteException e) {
            Log.d(TAG, "Error while stopping ranging", e);
        }
    }

    /*@Override
    protected void onStart() {
        super.onStart();
        startListening();
    }*/

    @Override
    protected void onStop() {
        stopListening();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                connectToService();
            } else {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
                //toolbar.setSubtitle("Bluetooth not enabled");
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void connectToService() {
        //toolbar.setSubtitle("Scanning ...");
        adapter.replaceWith(Collections.<Beacon>emptyList());
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                } catch (RemoteException e) {
                    Toast.makeText(MainActivity.this, "Cannot start ranging, something terrible happened", Toast.LENGTH_LONG);
                    Log.e(TAG, "Cannot start ranging", e);
                }
            }
        });
    }

    private AdapterView.OnItemClickListener createOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, position + " was just clicked!");
                Log.d(TAG, "CLICK EXTRA: " + getIntent().getStringExtra(EXTRAS_TARGET_ACTIVITY));

                if (getIntent().getStringExtra(EXTRAS_TARGET_ACTIVITY) != null) {
                    try {
                        Class<?> clazz = Class.forName(getIntent().getStringExtra(EXTRAS_TARGET_ACTIVITY));
                        Intent intent = new Intent(MainActivity.this, clazz);
                        intent.putExtra(EXTRAS_BEACON, adapter.getItem(position));
                        startActivity(intent);
                    } catch (ClassNotFoundException e) {
                        Log.e(TAG, "Finding class by name failed", e);
                    }
                }
            }
        };
    }

    public void findBeacons(View view) {
        //beaconItems.add("Hey!  That hurt!");
        boolean on = ((ToggleButton) view).isChecked();

        if (on) {
            //start listening
            startListening();
        } else {
            //stop listening
            stopListening();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
