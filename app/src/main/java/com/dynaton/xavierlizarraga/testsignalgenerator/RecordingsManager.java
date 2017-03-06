package com.dynaton.xavierlizarraga.testsignalgenerator;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by xavierlizarraga on 04/03/17.
 */
public class RecordingsManager extends ListActivity {

    private int CAMERA_REQUEST_CODE = 1;
    private RecordingInfo mCurrRecording;
    //private Intent camIntent;


    RecordingsViewAdapter mViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewAdapter = new RecordingsViewAdapter(this);

        setListAdapter(mViewAdapter);
        //Log.i("RECORDINGS MANAGER", "OnCreate");
    }

    @Override
    public void onListItemClick(ListView listView, View v, int position, long id) {
        String item = (String) listView.getItemAtPosition(position);

        // Show a toast if the user clicks on an item
        Toast.makeText(RecordingsManager.this, "Item Clicked: " + item, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    ;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        // Call for test signal generator to create new audio signals.
        if (id == R.id.action_recording) {
            // Launch MainActivity (recorder) activity to create new recordings.
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }
}