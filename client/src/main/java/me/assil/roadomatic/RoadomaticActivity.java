package me.assil.roadomatic;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RoadomaticActivity extends ActionBarActivity {
    // Referencing
    RoadomaticUpdater mUpdater;
    ScheduledExecutorService mPool;
    ScheduledFuture mTask;

    public TextView mSpeed;

    public class MyLocationListener implements LocationListener {

        public double mLat = -1;
        public double mLng = -1;

        @Override
        public void onLocationChanged(Location loc) {

            mLat = loc.getLatitude();
            mLng = loc.getLongitude();

        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
    
//    public TextView mSpeed = ; // to change the text view
//    public TextView mName = (TextView) findViewById(R.id.StreetName); // to change the text view
//    public TextView mTime = (TextView) findViewById(R.id.UpdateTime); // to change the text view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roadomatic);

        mSpeed = (TextView) findViewById(R.id.Speed);

        LocationManager m = (LocationManager) getSystemService(RoadomaticActivity.LOCATION_SERVICE);
        MyLocationListener l = new MyLocationListener();
        m.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, l);

        // Referencing the RoadomaticUpdater class to a new Object with an input
        // where "this" refers to the activity
        mUpdater = new RoadomaticUpdater(mSpeed, this, l);

        // Initializing the thread pool with 1 thread
        mPool = Executors.newScheduledThreadPool(1);

        // Execute the runnable at a fixed rate (once every three seconds)
        mTask = mPool.scheduleAtFixedRate(mUpdater, 0, 3, TimeUnit.SECONDS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close socket
        mUpdater.close();

        // Canceling the thread
        mTask.cancel(true);

        // Shutting down the thread pool
        mPool.shutdownNow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_roadomatic, menu);
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
