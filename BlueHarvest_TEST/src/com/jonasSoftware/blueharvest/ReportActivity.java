package com.jonasSoftware.blueharvest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

/**
 * Created by Brad on 2/14/2015.
 */
public class ReportActivity extends Activity {

    DatabaseHandler db = new DatabaseHandler(this);
    private static ListView _listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        final ListView listView = (ListView) findViewById(R.id.listView);
    }
}