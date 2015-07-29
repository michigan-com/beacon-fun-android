package com.michigan.erock.beaconfun;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Nearable;
import com.estimote.sdk.Region;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {
    private BeaconManager beaconManager;
    private String scanId;

    ArrayList<String> beaconItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        beaconManager = new BeaconManager(this);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, beaconItems);
        setListAdapter(adapter);

        beaconManager.setNearableListener(new BeaconManager.NearableListener() {
            @Override
            public void onNearablesDiscovered(List<Nearable> nearables) {
                Log.d("BEACON", "Discovered nearables: " + nearables);
            }
        });
    }

    @Override
    protected void onDestroy() {
        beaconManager.disconnect();
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

    public void findBeacons(View view) {
        //beaconItems.add("Hey!  That hurt!");
        boolean on = ((ToggleButton) view).isChecked();

        if (on) {
            //start listening
            beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
                @Override public void onServiceReady() {
                    scanId = beaconManager.startNearableDiscovery();
                }
            });
        } else {
            //stop listening
            //beaconManager.stopBeaconDiscovery(scanId);
            beaconManager.stopNearableDiscovery(scanId);
        }

        adapter.notifyDataSetChanged();
    }
}
