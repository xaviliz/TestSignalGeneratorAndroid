package com.dynaton.xavierlizarraga.testsignalgenerator;

import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ddf.minim.analysis.*;
import ddf.minim.*;

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
    private Button mProcessButton;
    private MusicIntentReceiver myReceiver;
    private ProgressBar mProgressBar;
    private ActionBar actionBar;
    private final int duration = 20; // seconds
    private final int sampleRate = 44100;
    private final int numSamples = duration * sampleRate;
    private final double sample[] = new double[numSamples];
    private final double freqOfTone = 1000; // hz
    private final byte generatedSnd[] = new byte[2 * numSamples];
    private AudioTrack mAudioTrack;
    double amplitude = 1.0;
    double twoPi = 2. * Math.PI;
    int f1 = 20;
    int f2 = 20000;
    int BytesPerElement = 2; // 2 bytes in 16bit format
    Minim minim;

    FFT fft;

    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    short[] audioData;

    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    int[] bufferData;
    int bytesRecorded;
    private static String DATE_FORMAT_STRING = "yyyyMMdd_HHmmss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                if (isChecked) {
                    startPlaying();
                    Toast.makeText(MainActivity.this, "Playing sound!!! =)",
                            Toast.LENGTH_SHORT).show();
                } else {
                    stopPlaying();
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
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "Recording sound!!! =)",
                            Toast.LENGTH_SHORT).show();
                    startRecording();
                } else {
                    stopRecording();
                }
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

        // Instantiate Broadcast Receiver to check if headphones output is plugged
        myReceiver = new MusicIntentReceiver();

        // Recorder settings
        bufferSize = AudioRecord.getMinBufferSize
                (sampleRate, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING) * 3;

        audioData = new short[bufferSize]; //short array that pcm data is put into.

        // Create process button to call when testing signals are recorded. It should be icative if nothing is recorded.
        mProcessButton = (Button) findViewById(R.id.process_button);

        mProcessButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent intent= new Intent(MainActivity.this,DSPActivity.class);
                startActivity(intent);
            }
        });
        mProcessButton.setEnabled(false);

        actionBar = getActionBar();
        //actionBar.setIcon(R.drawable.ic_recordings);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.file:
                //newGame();
                setSignals();
                return true;
            case R.id.action_recording:
                // TODO - Create recordingMAnager activity and launch activity
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setSignals() {
        // Initialize Alert Dialog to display a menu
        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create(); //Read Update
        alertDialog.setTitle("Setting Signals");
        alertDialog.setMessage("Controls for testing signal generator.");

        // All this shluod be done in a new menu as is proposed here:
        // http://stackoverflow.com/questions/6424032/android-seekbar-in-dialog

        // Seekbars to control testing signals parameters: gain, duration, f1 and f2
        LinearLayout linear=new LinearLayout(this);

        linear.setOrientation(LinearLayout.VERTICAL);
        TextView text=new TextView(this);
        text.setText("Gain");
        text.setPadding(10, 10, 10, 10);

        final SeekBar gainBar = new SeekBar(this);
        gainBar.setMax(255);
        gainBar.setKeyProgressIncrement(1);

        EditText durEditText = new EditText(this);//(EditText)findViewById(R.id.edit_text);
        durEditText.setText("Duration: ", TextView.BufferType.EDITABLE);

        // Adding View components to Linear Layout
        linear.addView(text);
        linear.addView(gainBar);
        linear.addView(durEditText);

        alertDialog.setView(linear);

        alertDialog.setButton("Continue..", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // here you can add functions
            }
        });
        alertDialog.show();
    }
    /*
    // Basic approach to generate a sinewave
    private void playSound(double frequency, int duration) {
        // AudioTrack definition
        int mBufferSize = AudioTrack.getMinBufferSize(44100,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        AudioTrack mAudioTrack = new AudioTrack(mAudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                mBufferSize, AudioTrack.MODE_STREAM);

        // Sine wave definition
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

    }*/

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
        if (mFileName != null) {
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
        /*
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");

        recordingThread.start();*/
    }

    // Stop playback. Release resources
    private void stopPlaying() {
        if (null != mPlayer) {
            if (mPlayer.isPlaying())
                mPlayer.stop();
            mPlayer.reset();
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

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // Generating Testing signals depending on spinner choice
        genSignals(pos);
        byteConversion();
        writeSynthesizedDataToFile();
        mPlayButton.setEnabled(true);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        mPlayButton.setEnabled(false);
    }

    @Override
    protected void onResume() {
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

    void genTone() {
        Log.i(TAG, "genTone is called");
        // Generate a sine wave on wave file format (byte)
        // Fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
        }
    }

    void genSweepTone(double f1, double f2) {
        if (f1 < 1)
            f1 = 1.;        // Avoid 0Hz
        else if (f2 > f1)
            Log.i(TAG, "Generating Swept tone signal...");
        else {
            Log.e(TAG, "Error defining f1 and f2, f2 must be greater than f1");
        }
        // convert to log2
        double b1 = Math.log10(f1) / Math.log10(2.);
        double b2 = Math.log10(f2) / Math.log10(2.);
        // define log2 range
        double rb = b2 - b1;
        // defining step by time resolution
        double step = rb / numSamples;
        double nf = b1;   // new frequency
        for (int i = 0; i < numSamples; i++) {
            double time = i * 1.0 / sampleRate;
            double f = Math.pow(2., nf);
            sample[i] = (amplitude * Math.sin(twoPi * f * time));
            nf = nf + step;
        }
    }

    void generateWhiteNoise() {
        Log.i(TAG, "Generating white noise...");
        // Generate signal
        double Max = amplitude;
        double Min = -amplitude;
        for (int i = 0; i < numSamples; i++) {
            sample[i] = (Math.random() * (Max - Min)) - 1.;
        }
    }

    void generateMLS(int N) {
        Log.i(TAG, "Generating MLS signal...");
        // Initialize abuff array to ones
        // Generate pseudo random signal
        int nsamp = (int) Math.pow(2, N);
        int taps = 4, tap1 = 1, tap2 = 2, tap3 = 4, tap4 = 15;
        if (N != 16) {
            Log.e(TAG, "At this moment MLS signal is only defined for 16 bits, soon other tap values will be included.");
        }
        int[] abuff = new int[N];
        // fill with ones
        for (int i = 0; i < abuff.length; i++) {
            abuff[i] = 1;
        }
        for (int i = nsamp; i > 1; i--) {
            // feedback bit
            int xorbit = abuff[tap1] ^ abuff[tap2];
            // second logic level
            if (taps == 4) {
                int xorbit2 = abuff[tap3] ^ abuff[tap4]; //4 taps = 3 xor gates & 2 levels of logic
                xorbit = xorbit ^ xorbit2;        //second logic level
            }
            // Circular buffer
            for (int j = N - 1; j > 0; j--) {
                int temp = abuff[j - 1];
                abuff[j] = temp;
            }
            abuff[0] = xorbit;
            // fill sample value
            sample[i] = (-2. * xorbit) + 1.;
        }
    }

    void byteConversion() {
        // Convert to 16 bit pcm sound array
        // Assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // Scaling to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
    }

    void genSignals(int pos) {
        switch (pos) {
            case 0: {
                genTone();
                break;
            }
            case 1: {
                genSweepTone(f1, f2);
                break;
            }
            case 2: {
                generateWhiteNoise();
                break;
            }
            case 3: {
                generateMLS(16);
                break;
            }
        }
    }

    void playSound() {
        mAudioTrack = new AudioTrack(mAudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                AudioTrack.MODE_STATIC);
        mAudioTrack.write(generatedSnd, 0, generatedSnd.length);
        mAudioTrack.play();
    }

    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + //System.currentTimeMillis() +
                "synth_test.wav");
    }

    private String getFilename2() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);
        String timeStamp = new SimpleDateFormat(DATE_FORMAT_STRING).format(new Date());
        if (!file.exists()) {
            file.mkdirs();
        }

        return (file.getAbsolutePath() + File.separator + timeStamp + ".wav");
    }

    private void writeSynthesizedDataToFile() {
        int bufferSize = 4096;
        byte data[] = new byte[bufferSize];
        mFileName = getFilename();        //"synth_test.wav";//getTempFilename();
        int channels = 1;
        long byteRate = RECORDER_BPP * sampleRate * channels / 8;
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
            long byteRate) throws IOException {
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
        @Override
        public void onReceive(Context context, Intent intent) {
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
                        break;
                    default:
                        Log.d(TAG, "I have no idea what the headset state is");
                }
            }
        }
    }

    @Override
    public void onPause() {
        unregisterReceiver(myReceiver);
        super.onPause();
    }

    private String getTempFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);

        if (tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }


    private void writeAudioDataToFile() {
        byte data2[] = new byte[bufferSize*BytesPerElement];
        String filename = getTempFilename();
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int read = 0;
        if (null != os) {
            while (isRecording) {
                read = recorder.read(data2, 0, bufferSize*BytesPerElement);
                if (read > 0) {
                }

                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os.write(data2,0,bufferSize*BytesPerElement);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteTempFile() {
        File file = new File(getTempFilename());
        file.delete();
    }

    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = sampleRate;
        int channels = 1;
        long byteRate = RECORDER_BPP * sampleRate * channels / 8;

        byte[] data = new byte[bufferSize*BytesPerElement];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            Log.i(TAG, "File size: " + totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while (in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Start recording wave file
    private void startRecording() {
        recorder = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER, // CAMCORDER is the best option bec MIC change to a mixing with line output
                sampleRate,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                bufferSize*BytesPerElement);
        int i = recorder.getState();
        if (i == 1)
            recorder.startRecording();

        isRecording = true;

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");

        recordingThread.start();
    }

    // Stop recording wave file
    private void stopRecording() {
        if (null != recorder) {
            isRecording = false;
            int i = recorder.getState();
            if (i == 1)
                recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }

        copyWaveFile(getTempFilename(), getFilename2());
        deleteTempFile();
        // Remove record.raw - temporal file - every time any file has been recorded
        try {
            removeRawFile();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // makes enable to process button when some audio is recorded
        mProcessButton.setEnabled(true);
    }

    /* This function is used to remove raw file before to close the application -
    it works but onDestroy is not always called when app is closed */
    void removeRawFile() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);
        file.delete();
        if (file.exists()) {
            try {
                file.getCanonicalFile().delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (file.exists()) {
                getApplicationContext().deleteFile(file.getName());
            }
        }
    }
    /* onDestroy doesn't guaranteed that removeRawFile is called
    * TODO - Look for other strategy */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            removeRawFile();
            Toast.makeText(this,"onDestroy " ,Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (null != recorder) {
            Log.i("CallRecorder", "RecordService::onDestroy calling recorder.release()");
            isRecording = false;
            recorder.release();
            Toast t = Toast.makeText(getApplicationContext(), "CallRecorder finished recording call", Toast.LENGTH_LONG);
            t.show();
        }
    }

}

// TODO - Add intro Activity to include dynaton icon, use IntroActivity project as example
