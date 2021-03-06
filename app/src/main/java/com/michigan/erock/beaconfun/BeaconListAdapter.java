package com.michigan.erock.beaconfun;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.Utils;
import com.estimote.sdk.cloud.CloudCallback;
import com.estimote.sdk.cloud.EstimoteCloud;
import com.estimote.sdk.cloud.model.BeaconColor;
import com.estimote.sdk.cloud.model.BeaconInfo;
import com.estimote.sdk.cloud.model.Color;
import com.estimote.sdk.exception.EstimoteServerException;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ebower on 7/31/15.
 */
public class BeaconListAdapter extends BaseAdapter {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ArrayList<Beacon> beacons;
    private LayoutInflater inflater;

    public BeaconListAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        this.beacons = new ArrayList<>();
    }

    public void replaceWith(Collection<Beacon> newBeacons) {
        this.beacons.clear();
        this.beacons.addAll(newBeacons);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return beacons.size();
    }

    @Override
    public Beacon getItem(int position) {
        return beacons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflateIfRequired(view, position, parent);
        bind(getItem(position), view);
        return view;
    }

    private void bind(final Beacon beacon, final View view) {
        EstimoteCloud.getInstance().fetchBeaconDetails(beacon.getMacAddress(), new CloudCallback<BeaconInfo>() {
            @Override
            public void success(BeaconInfo beaconInfo) {
                ViewHolder holder = (ViewHolder) view.getTag();
                holder.nameTextView.setText(beaconInfo.name + " (" + beaconInfo.color + ")");
                holder.macTextView.setText(String.format("MAC: %s (%.2fm) (%s)", beacon.getMacAddress(), Utils.computeAccuracy(beacon), Utils.computeProximity(beacon)));
                holder.majorTextView.setText("Major: " + beacon.getMajor());
                holder.minorTextView.setText("Minor: " + beacon.getMinor());
                holder.measuredPowerTextView.setText("MPower: " + beacon.getMeasuredPower());
                holder.rssiTextView.setText("RSSI: " + beacon.getRssi());
            }

            @Override
            public void failure(EstimoteServerException e) {
                Log.e(TAG, "BEACON INFO ERROR: " + e);
            }
        });
    }

    private View inflateIfRequired(View view, int position, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.beacon_item, null);
            view.setTag(new ViewHolder(view));
        }
        return view;
    }

    static class ViewHolder {
        final TextView nameTextView;
        final TextView macTextView;
        final TextView majorTextView;
        final TextView minorTextView;
        final TextView measuredPowerTextView;
        final TextView rssiTextView;

        ViewHolder(View view) {
            nameTextView = (TextView) view.findViewWithTag("name");
            macTextView = (TextView) view.findViewWithTag("mac");
            majorTextView = (TextView) view.findViewWithTag("major");
            minorTextView = (TextView) view.findViewWithTag("minor");
            measuredPowerTextView = (TextView) view.findViewWithTag("mpower");
            rssiTextView = (TextView) view.findViewWithTag("rssi");
        }
    }
}
