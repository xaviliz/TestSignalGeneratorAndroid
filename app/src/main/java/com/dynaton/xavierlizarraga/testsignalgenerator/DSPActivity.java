package com.dynaton.xavierlizarraga.testsignalgenerator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DSPActivity extends Activity {

    private Context context;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dsp);
        context = this;
        // Display current volume level in TextView
        final TextView tv_rec = (TextView) findViewById(R.id.text_record);
        /* AsyncTask that will handle the background work.
        In the pre and post execute methods, we worry about the ProgressDialog
         */
        /*
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                pd = new ProgressDialog(context);
                pd.setTitle("Processing...");
                pd.setMessage("Please wait.");
                pd.setCancelable(false);
                pd.setIndeterminate(true);
                pd.show();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    //Do DSP process here...
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (pd!=null) {
                    pd.dismiss();
                    // Perform action on click - it should call to a display of results
                    //Intent intent= new Intent(DSPActivity.this,UICharacResponse.class);
                    //startActivity(intent);
                }
            }

        };
        task.execute((Void[])null);*/
    }
}
