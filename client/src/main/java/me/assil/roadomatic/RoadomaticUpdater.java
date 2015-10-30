package me.assil.roadomatic;

import android.app.Activity;
import android.widget.TextView;

import java.lang.Runnable;

/**
 * Created by saeed_000 on 10/30/2015.
 */
public class RoadomaticUpdater implements Runnable {
    TextView mText;
    Activity mActivity;
    RoadomaticRequest mRequest;

    // Constructor to initialize the variables
    RoadomaticUpdater(TextView text, Activity activity){
        mText = text;
        mActivity = activity;
        mRequest = new RoadomaticRequest();
    }

    public void close() {
        mRequest.closeSocket();
    }

    public void run() {
        try {
            // Check for connectivity

            // Get the GPS location

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
