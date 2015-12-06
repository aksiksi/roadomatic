package me.assil.roadomatic;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.lang.Runnable;

/**
 * Created by saeed_000 on 10/30/2015.
 */
public class RoadomaticUpdater implements Runnable {
    TextView mSpeed;
    TextView mName;
    TextView mLastUpdated;

    Activity mActivity;

    RoadomaticRequest mRequest;
    RoadomaticActivity.MyLocationListener mListener;

    private int reqCount;

    // Constructor to initialize the variables
    public RoadomaticUpdater(TextView speed, TextView name, TextView lastUpdated, Activity activity,
                             RoadomaticActivity.MyLocationListener l) {
        mSpeed = speed;
        mName = name;
        mLastUpdated = lastUpdated;

        mActivity = activity;
        mRequest = new RoadomaticRequest();
        mListener = l;

        reqCount = 0;
    }

    public void close() {
        mRequest.closeSocket();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
              = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void run() {
        try {
            // Check for connectivity
            boolean x = isNetworkAvailable();

            if (!x)
                return;

            double lat = mListener.mLat;
            double lng = mListener.mLng;

            if (lat == -1)
                return;

            // {"lat": lat, "lng": lng}
            String s = "{\"lat\":" + lat + "," + "\"lng\":" + lng + "}";

            // {"o": 1, "f": 1, "n": "Shei..", "s": 80}
            JSONObject resp = mRequest.sendAndReceive(s);
            reqCount++;

            final String name;
            final int speed;

            if (resp == null || resp.getInt("o") == 0) {
                name = "Server offline!";
                speed = -1;
            } else {
                int found = resp.getInt("f");

                if (found == 0) {
                    name = "Road not found!";
                    speed = -1;
                }
                else {
                    name = resp.getString("n");
                    speed = resp.getInt("s");
                }
            }

            // Update the UI
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mName.setText(name);

                    if (speed == -1)
                        mSpeed.setText("--");
                    else
                        mSpeed.setText(speed + "");

                    mLastUpdated.setText("Made " + reqCount + " requests");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
