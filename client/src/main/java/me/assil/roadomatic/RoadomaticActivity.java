package me.assil.roadomatic;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
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

    TextView mSpeed;
    TextView mName;
    TextView mLastUpdated;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roadomatic_v2);

        mSpeed = (TextView) findViewById(R.id.Speed);
        mName = (TextView) findViewById(R.id.StreetName);
        mLastUpdated = (TextView) findViewById(R.id.UpdateTime);

        mName.setText("Waiting for GPS...");
        greyOutImages();

        // GPS setup
        LocationManager m = (LocationManager) getSystemService(RoadomaticActivity.LOCATION_SERVICE);
        MyLocationListener l = new MyLocationListener();
        m.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, l);

        // Referencing the RoadomaticUpdater class to a new Object with an input
        // where "this" refers to the activity
        mUpdater = new RoadomaticUpdater(mSpeed, mName, mLastUpdated, this, l);

        // Initializing the thread pool with 1 thread
        mPool = Executors.newScheduledThreadPool(1);

        // Execute the runnable at a fixed rate (once every three seconds)
        mTask = mPool.scheduleAtFixedRate(mUpdater, 0, 10, TimeUnit.SECONDS);

        //Keep screen on while working
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void greyOutImages() {
        int[] ids = new int[]{R.id.Silent, R.id.School, R.id.mosque, R.id.Airport};

        for (int id: ids) {
            ImageView iv = (ImageView) findViewById(id);
            iv.setAlpha(0.5f);
        }
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
