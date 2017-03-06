package com.dynaton.xavierlizarraga.testsignalgenerator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by xavierlizarraga on 04/03/17.
 */
public class RecordingsViewActivity extends Activity {


    /* This class is used to draw wavefile tap on list adapter.
    * DONE: The basic approach is to us a Custom Drawable View defined in a class
    * where we can handle onDraw() function from Paint class.
    * TODO: But it exists another approach
    * as create a bitmap with the read sample and scale it to draw all the waveform on display
    * Later zoom functions can be applied scaling this bitmap.*/

        public static String EXTRA_URI_STRING = "SELFIE_URI";

        public Uri mAudioPath;
        CustomDrawableView mCustomDrawableView;
        DataInputStream dis1 = null;
        int size = 0;
        byte[] datainBytes1;
        int header_idx = 44;
        int finger_position = header_idx;
        private GestureDetector mGestureDetector;
        int srate = 44100;
        int width = getScreenWidth();
        int height = getScreenHeight();
        int heightOver3 = height/3;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mAudioPath = Uri.parse(getIntent().getStringExtra(EXTRA_URI_STRING));

            Log.i("AUDIORECORDER", "Launching editor activity...");
            Log.i("AUDIORECORDER", mAudioPath.toString());

            // Read data from audio file
            try {
                dis1 = new DataInputStream(new FileInputStream(mAudioPath.toString()));

            } catch (FileNotFoundException e){
                e.printStackTrace();
            }
            try{
                datainBytes1 = new byte[dis1.available()];       // generate a byte array
                //datainBytes1 = new byte[getScreenWidth()];
                size = dis1.available();
                //Log.i("AUDIORECORDER", String.valueOf(dis1.available()));
                dis1.readFully(datainBytes1);
                //dis1.
                dis1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mCustomDrawableView = new CustomDrawableView(this);

            setContentView(mCustomDrawableView);
            mGestureDetector = new GestureDetector(this,
                    new GestureDetector.SimpleOnGestureListener() {
                        /* maybe on fling is not the best choise */
                        @Override
                        public boolean onFling(MotionEvent e1, MotionEvent e2,
                                               float velocityX, float velocityY) {
                            //Log.i("AUDIORECORDER", "velocityX: "+String.valueOf(Math.abs(velocityX)));
                            if (Math.abs(velocityX) > 0.5f) {
                                finger_position = finger_position + ((int)e1.getRawX()-(int)e2.getRawX());
                                if (finger_position < header_idx)
                                    finger_position = header_idx;
                                if (finger_position > (size/2) - width - header_idx)
                                    finger_position = (size/2) - width - header_idx;
                                //Log.i("AUDIORECORDER", "fingerPosition: "+String.valueOf(finger_position));
                                // Reinitialize - Canvas to draw the new waveform
                                Canvas canvas = new Canvas();
                                mCustomDrawableView.draw(canvas);
                                setContentView(mCustomDrawableView);
                            }
                            return true;
                        }
                    }
            );
        }

        @Override
        protected void onDestroy() {
            finish();
            mCustomDrawableView.reset();
            datainBytes1 = null;
            dis1 = null;
            super.onDestroy();
            Runtime.getRuntime().gc();
            System.gc();
            Log.i("AUDIORECORDER", "onDestroy()");
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return mGestureDetector.onTouchEvent(event);
        }

        public class CustomDrawableView extends View {
            //private ShapeDrawable mDrawable;
            Canvas ca;
            View v;
            Paint paint = new Paint();

            public CustomDrawableView(Context context) {
                super(context);
            }

            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                paint = new Paint();
                // draw blue background
                paint.setColor(Color.BLUE);
                canvas.drawRect(0, 0, width, height, paint);
                // draw white line to define 0-point
                paint.setColor(Color.WHITE);
                canvas.drawLine(0, (heightOver3)+25, width, (heightOver3)+25, paint);
                // draw audio file with drawLines - yellow lines
                paint.setColor(Color.YELLOW);
                for (int i = finger_position; i< finger_position+width-3;i++){
                    // 2) Read audio file and draw
                    short smp1 = (short)(( datainBytes1[i*2] & 0xff )|( datainBytes1[i*2+1] << 8 ));
                    short smp2 = (short)(( datainBytes1[i*2+2] & 0xff )|( datainBytes1[i*2+3] << 8 ));
                    float s1 = (float) smp1/32767;
                    float s2 = (float) smp2/32767;
                    canvas.drawLine(i-finger_position,(heightOver3)+25+(s1*(heightOver3)), i+1-finger_position,(heightOver3)+25+(s2*(heightOver3)),paint);
                }
                // print interval time on the display
                canvas.drawText(String.format("%.3f",(float)(finger_position-header_idx)/srate) +" - "
                        + String.format("%.3f",(float)(finger_position-header_idx+width)/srate) + "s", 5, 15, paint);
                // print wavefile duration
                canvas.drawText(String.format("%.3f" ,(float)(size/2/srate)) + "s", 5, height-100,paint);

                // TODO: redefine getHeight() and getWidth(), because any device have different display and we want to know the display available to draw
            /*Thread myThread = new Thread(new UpdateThread());
            myThread.start();*/
            }
            public void reset() {
                paint = null;
            }
        }

        public static int getScreenWidth() {
            return Resources.getSystem().getDisplayMetrics().widthPixels;
        }

        public static int getScreenHeight() {
            return Resources.getSystem().getDisplayMetrics().heightPixels;
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_recordings_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_recordings_list:
                // Launch RecordingsManager activity to manage recordings already done.
                intent = new Intent(this, MainActivity.class);
                this.startActivity(intent);
                return true;
            case R.id.action_recorder:
                // Launch RecordingsManager activity to manage recordings already done.
                intent = new Intent(this, RecordingsManager.class);
                this.startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    }

