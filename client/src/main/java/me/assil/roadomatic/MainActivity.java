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

public class MainActivity extends ActionBarActivity {
    //Referencing
    RoadomaticUpdater rn;
    ScheduledExecutorService pool;
    ScheduledFuture task;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Getting the layout
        setContentView(R.layout.activity_main);
        //Referencing the text view and declaring it by an id
        TextView text = (TextView) findViewById(R.id.text1);
        //Referencing the RoadomaticUpdater class to a new Object with an input, where "this" refers to the activity
        rn = new RoadomaticUpdater(text, this);
        //Initializing the thread pool with 1 thread
        pool = Executors.newScheduledThreadPool(1);
        // Execute the runnable at a fixed rate (once every three seconds)
        task = pool.scheduleAtFixedRate(rn, 0, 3, TimeUnit.SECONDS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Canceling the thread
        task.cancel(true);
        // shutting down the thread pool
        pool.shutdownNow();
    }
}
