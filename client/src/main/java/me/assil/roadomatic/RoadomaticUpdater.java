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
    TextView mText;
    Activity mActivity;
    RoadomaticRequest mRequest;
	
	double lat = -1;
	double lng = -1;

    // Constructor to initialize the variables
    RoadomaticUpdater(TextView text, Activity activity){
        mText = text;
        mActivity = activity;
        mRequest = new RoadomaticRequest();
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
	
	/*---------- Listener class to get coordinates ------------- */
	private class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location loc) {

			lat = loc.getLatitude();
			lng = loc.getLongitude();

		}

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
	}



    public void run() {
        try {
            // Check for connectivity
			boolean x = isNetworkAvailable();
			if(!x)
				return;

            // Get the GPS location
			if (lat == -1)
				return;
			
			Log.d("MAIN", lat + "");
			
			// {"lat": lat, "lng": lng}
			String s = "{\"lat\": " + lat + "," + "{\"lng\":" + lng + "}";
			
			Log.d("MAIN", s);
			
			RoadomaticRequest req = new RoadomaticRequest();
			JSONObject resp = req.sendAndReceive(s);
			
			if (resp == null) {
				Log.d("MAIN", "Server is offline");
				return;
			}
			
			// {"o": 1, "f": 1, "n": "Shei..", "s": 80}
			
			Log.d("MAIN", resp.getInt("s") +"");

            // Send the request to the server

            // Get the street name and the speed

            // Update the UI
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mText.setText("THE UPDATED SPEED");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
