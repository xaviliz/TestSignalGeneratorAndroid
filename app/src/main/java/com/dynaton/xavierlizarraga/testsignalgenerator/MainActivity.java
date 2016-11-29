package com.dynaton.xavierlizarraga.testsignalgenerator;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    protected static final String TAG = "TestSignalGenerator";
    private String mFileName;
    private int mVolume = 6;
    private final int mVolumeMax = 10;
    private final int mVolumeMin = 0;
    private int mSoundId;
    private MediaPlayer mPlayer;
    private AudioManager mAudioManager;
    private ToggleButton mPlayButton, mRecordButton;
    private MusicIntentReceiver myReceiver;
    private ProgressBar mProgressBar;
    private final int duration = 40; // seconds
    private final int sampleRate = 44100;
    private final int numSamples = duration * sampleRate;
    private final double sample[] = new double[numSamples];
    private final double freqOfTone = 1000; // hz
    private final byte generatedSnd[] = new byte[2 * numSamples];
    //private final byte data[] = new byte[2 * numSamples];
    private AudioTrack mAudioTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO - Add Dialog while signals are generated and added to mAudioManager

        // Create play button
        mPlayButton = (ToggleButton) findViewById(R.id.play_button);

        // Set up play Button
        mPlayButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                // Set enabled state
                //mRecordButton.setEnabled(!isChecked);
                Log.i(TAG, "Play button pressed");
                // Start/stop playback
                //onPlayPressed(isChecked);
                // TODO - Edit onPlayPressed to play the testing signal selected
                if(isChecked) {
                    //playSound();
                    startPlaying();
                    Toast.makeText(MainActivity.this, "Playing sound!!! =)",
                            Toast.LENGTH_SHORT).show();
                }
                else{
                    stopPlaying();
                    /*mAudioTrack.stop();
                    mAudioTrack.release();*/
                }
            }
        });
        mPlayButton.setEnabled(false);

        // Create record button
        mRecordButton = (ToggleButton) findViewById(R.id.rec_button);

        // Set up play Button
        mRecordButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                // Set enabled state
                //mPlayButton.setEnabled(!isChecked);
                Log.i(TAG, "Play button pressed");
                // Start/stop recording
                //onRecordPressed(isChecked);
                // TODO - Create onRecordPressed to play the testing signal selected
                if(isChecked)
                    Toast.makeText(MainActivity.this, "Recording sound!!! =)",
                        Toast.LENGTH_SHORT).show();
            }
        });
        mRecordButton.setEnabled(false);

        // Get reference to the AudioManager
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // Request audio focus
        mAudioManager.requestAudioFocus(afChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        // Volume controls modify the volume of the active audio stream
        setVolumeControlStream(mAudioManager.STREAM_MUSIC);

        Spinner spinner = (Spinner) findViewById(R.id.signals_spinner);
        // specify the interface implementation
        spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.testing_signals, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        // Display current volume level in TextView
        final TextView tv = (TextView) findViewById(R.id.text_volume);
        //tv.setText(String.valueOf(mVolume));

        // Display current volume level in TextView
        final TextView tv_rec = (TextView) findViewById(R.id.text_record);

        // Generate testing signals, they can be generated on the intro activity
        genTone();
        writeSynthesizedDataToFile();

        // Instantiate Broadcast Receiver to check if headphones output is plugged
        myReceiver = new MusicIntentReceiver();


        // TODO - Add circular progress bar - check Threading AsyncTask in examples
        /* Progress Bar while signals are sinthesized
        ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        ObjectAnimator animation = ObjectAnimator.ofInt (mProgressBar, "progress", 0, 500); // see this max value coming back here, we animale towards that value
        animation.setDuration (5000); //in milliseconds
        animation.setInterpolator (new DecelerateInterpolator());
        animation.start ();*/

    }


    // Basic idea to generate a sinewave
    private void playSound(double frequency, int duration) {
        // AudioTrack definition
        int mBufferSize = AudioTrack.getMinBufferSize(44100,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        AudioTrack mAudioTrack = new AudioTrack(mAudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                mBufferSize, AudioTrack.MODE_STREAM);

        // Sine wave
        double[] mSound = new double[4410];
        short[] mBuffer = new short[duration];
        for (int i = 0; i < mSound.length; i++) {
            mSound[i] = Math.sin((2.0*Math.PI * i/(44100/frequency)));
            mBuffer[i] = (short) (mSound[i]*Short.MAX_VALUE);
        }

        mAudioTrack.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
        mAudioTrack.play();

        mAudioTrack.write(mBuffer, 0, mSound.length);
        mAudioTrack.stop();
        mAudioTrack.release();

    }

    // Toggle playback
    private void onPlayPressed(boolean shouldStartPlaying) {

        if (shouldStartPlaying) {
            startPlaying();
        } else {
            stopPlaying();
        }

    }

    // Playback audio using MediaPlayer
    private void startPlaying() {

        if(mFileName != null) {
            mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(mFileName);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                Log.e(TAG, "Couldn't prepare and start MediaPlayer");
            }
        } else {
            Log.e(TAG, "First generate a testing signal");
        }
    }

    // Stop playback. Release resources
    private void stopPlaying() {
        if (null != mPlayer) {
            if (mPlayer.isPlaying())
                mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }
    // Listen for Audio Focus changes
    // To avoid every music app playing at the same time, Android uses audio focus to moderate audio playback.
    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {

            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                mAudioManager.abandonAudioFocus(afChangeListener);

                // Stop playback, if necessary
                if (null != mPlayer && mPlayer.isPlaying())
                    stopPlaying();
            }

        }

    };

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        mPlayButton.setEnabled(true);

        // TODO - Change to testing signal depending on parent getItemAtPosition(pos) with mFileName, you can define names for each testing signal
        // and too reproduce them you only have to change mFileName

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        mPlayButton.setEnabled(false);
    }

    @Override protected void onResume() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(myReceiver, filter);
        super.onResume();
    }
     /*   // Use a new tread as this can take a while
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                genTone();
                /*handler.post(new Runnable() {

                    public void run() {
                        playSound();
                    }
                });*/
           /* }
        });
        thread.start();
    }*/

    void genTone(){
        Log.i(TAG, "genTone is called");
        // Use a new tread as this can take a while
        //Thread thread = new Thread(new Runnable() {
          //  public void run() {
                // fill out the array
                for (int i = 0; i < numSamples; ++i) {
                    sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
                }

                // convert to 16 bit pcm sound array
                // assumes the sample buffer is normalised.
                int idx = 0;
                for (final double dVal : sample) {
                    // scale to maximum amplitude
                    final short val = (short) ((dVal * 32767));
                    // in 16 bit wav PCM, first byte is the low order byte
                    generatedSnd[idx++] = (byte) (val & 0x00ff);
                    generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
                }
            //}
        //});
    }

    void playSound(){
        mAudioTrack = new AudioTrack(mAudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                AudioTrack.MODE_STATIC);
        mAudioTrack.write(generatedSnd, 0, generatedSnd.length);
        mAudioTrack.play();
    }

    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,"AudioRecorder");

        if (!file.exists()) {
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + //System.currentTimeMillis() +
                "synth_test.wav");
    }

    private void writeSynthesizedDataToFile() {
        int bufferSize = 4096;
        byte data[] = new byte[bufferSize];
        mFileName = getFilename();        //"synth_test.wav";//getTempFilename();
        int channels = 1;
        long byteRate = 16 * sampleRate * channels/8;
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(mFileName);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int read = 0;
        if (null != os) {

            try {
                WriteWaveFileHeader(os, numSamples, numSamples + 36,
                        sampleRate, channels, byteRate);

                while (read < numSamples - bufferSize) {
                    // copy generated sound array on data buffer
                    for (int i = 0; i < bufferSize; i++) {
                        data[i] = generatedSnd[read + i];
                    }
                    // write buffer on the fileoutputstream
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // updating counter
                    read = read + bufferSize;
                }

                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException
    {
        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = 16;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    // Class necessary to know when line out is unplugged
    private class MusicIntentReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        Log.d(TAG, "Headset is unplugged");
                        // unable Play button if headset is unplugged
                        mRecordButton.setEnabled(false);        // avoiding feedback
                        Toast.makeText(MainActivity.this, "Headset is unplugged",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Log.d(TAG, "Headset is plugged");
                        mPlayButton.setEnabled(true);
                        mRecordButton.setEnabled(true);
                        //Toast.makeText(MainActivity.this, "Headset is plugged",
                        //        Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Log.d(TAG, "I have no idea what the headset state is");
                }
            }
        }
    }

    @Override public void onPause() {
        unregisterReceiver(myReceiver);
        super.onPause();
    }
}


// TODO - Add intro Activity to include dynaton icon, use IntroActivity project as example
