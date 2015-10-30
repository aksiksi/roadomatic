package me.assil.roadomatic;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roadomatic);

        // Referencing the text view and declaring it by an id
        TextView text = (TextView) findViewById(R.id.text); // Test id

        // Referencing the RoadomaticUpdater class to a new Object with an input
        // where "this" refers to the activity
        mUpdater = new RoadomaticUpdater(text, this);

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
