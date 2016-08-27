package com.beamng.udpsteering;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    public Handler mHandler;
    private Activity aContext;
    private String Iadress;
    private float angle;
    private float oldangle = 0.0f;
    private int sendingTimeout = 50;
    private ObjectAnimator oban;
    private UdpSessionSender sessionsender;
    private UdpSessionReceiver sessionreceiver;

    //Sensordata damping elements
    private List<Float> rollingAverage = new ArrayList<Float>();
    private static final int MAX_SAMPLE_SIZE = 5;
    private float gravity;

    //UI Elements
    private Button throttle;
    private Button breaks;
    private float thrpushed;
    private float brpushed;

    //menu items
    private Switch unitToggle;
    private int useKMH = 0;
    private SeekBar sensitivity;
    private float sensitivitySetting = 0.5f;
    private ImageButton menu;
    private LinearLayout menuItems;

    //Views depending on communication
    private RelativeLayout mainLayout;
    private ProgressBar pbSpeed;
    private ProgressBar pbRspeed;
    private ProgressBar pbFuel;
    private ProgressBar pbHeat;
    private TextView textSpeed;
    private TextView textGear;
    private TextView textOdo;
    private TextView textDelay;
    private TextView textUnit;
    private ImageView[] lightViews;

    //Orientationhandling
    private Display display;
    int orientation;
    private int orientationhandler = 1;

    //UI update interval
    public static final int TIME_CONSTANT = 50;

    private Timer fuseTimer = new Timer();

    public static String id = "";

    private Long lpTime, timeDiff, oldDiff;
    private int pID = 1, lastID = 0;

    //Multithreading
    private ThreadPoolExecutor executor;
    private BlockingQueue<Runnable> mDecodeWorkQueue;
    private int KEEP_ALIVE_TIME = 1;
    private TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private int NUMBER_OF_CORES;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private InetAddress hostAddress;

    public static final String prefsName = "UserSettings";


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            hideSystemUI();
        }
        super.onWindowFocusChanged(hasFocus);
    }

    protected void SaveSettings () {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(prefsName, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("unit", useKMH);
        editor.putFloat("sens", sensitivitySetting);
        // Commit the edits!
        editor.commit();

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v("BeamNG", id);

        hideSystemUI();
        setContentView(R.layout.activity_main);

        mainLayout = (RelativeLayout) findViewById(R.id.main);
        pbSpeed = (ProgressBar) findViewById(R.id.progressBar);
        pbRspeed = (ProgressBar) findViewById(R.id.progressBar2);
        pbFuel = (ProgressBar) findViewById(R.id.progressBar3);
        pbHeat = (ProgressBar) findViewById(R.id.progressBar4);
        textSpeed = (TextView) findViewById(R.id.Textspeed);
        textGear = (TextView) findViewById(R.id.Textgear);
        textOdo = (TextView) findViewById(R.id.Textodo);
        textDelay = (TextView) findViewById(R.id.Textdelay);
        textUnit = (TextView) findViewById(R.id.Textunit);

        //HUD-Lights in the order of given structure in Receivepacket.java
        lightViews = new ImageView[11];
        lightViews[10] = (ImageView) findViewById(R.id.light_abs);
        lightViews[2] = (ImageView) findViewById(R.id.light_break);
        lightViews[0] = (ImageView) findViewById(R.id.light_headlight);
        lightViews[1] = (ImageView) findViewById(R.id.light_fullbeam);
        lightViews[5] = (ImageView) findViewById(R.id.light_leftindicator);
        lightViews[6] = (ImageView) findViewById(R.id.light_rightindicator);

        throttle = (Button) findViewById(R.id.throttlecontrol);
        breaks = (Button) findViewById(R.id.breakcontrol);

        aContext = this;
        hostAddress = ((RemoteControlApplication)getApplication()).getHostAddress();
        Iadress = ((RemoteControlApplication)getApplication()).getIp();

        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        orientation = display.getRotation();
        lpTime = System.currentTimeMillis();
        timeDiff = 0l;
        //Multi-Thread-executor:
        // A queue of Runnables
        // instantiate the queue of Runnables as a LinkedBlockingQueue
        mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();
        // Sets the amount of time an idle thread waits before terminating
        KEEP_ALIVE_TIME = 1;
        // Sets the Time Unit to seconds
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

        NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

        // Creates a thread pool manager
        executor = new ThreadPoolExecutor(
                NUMBER_OF_CORES,       // Initial pool size
                NUMBER_OF_CORES,       // Max pool size
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mDecodeWorkQueue);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mainLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
                1000, TIME_CONSTANT);

        throttle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    thrpushed = 1f;

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    thrpushed = 0f;
                }
                return false;
            }
        });

        breaks.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    brpushed = 1f;

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    brpushed = 0f;
                }
                return false;
            }
        });

        unitToggle = (Switch) findViewById(R.id.unitSwitch);
        sensitivity = (SeekBar) findViewById(R.id.sensitivity);
        // Restore options preferences
        SharedPreferences settings = getSharedPreferences(prefsName, 0);
        useKMH = settings.getInt("unit", 0);
        sensitivitySetting = settings.getFloat("sens", 0.5f);
        sensitivity.setProgress(Math.round(sensitivitySetting*100));
        if (useKMH == 1) {
            unitToggle.setChecked(true);
            textUnit.setText("Km/h");
        }

        unitToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    useKMH = 1;
                    textUnit.setText("Km/h");
                } else {
                   useKMH = 0;
                    textUnit.setText("MPH");
                }
                SaveSettings();
            }
        });

        sensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                sensitivitySetting = progressValue/100f;
                SaveSettings();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        menuItems = (LinearLayout)  findViewById(R.id.menuItems);
        menu = (ImageButton) findViewById(R.id.menuButton);
        menu.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (menuItems.getVisibility() == View.INVISIBLE)
                        menuItems.setVisibility(View.VISIBLE);
                    else
                        menuItems.setVisibility(View.INVISIBLE);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                }
                return false;
            }
        });

        //faster handling of the rotating views
        mainLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        //Sensor
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        //initListeners();

        mHandler = new Handler();


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:

                angle = (float) (Math.asin(
                    -event.values[1] / Math.sqrt(
                        event.values[0] * event.values[0] +
                        event.values[1] * event.values[1] +
                        event.values[2] * event.values[2]
                    )
                ) * 180 / Math.PI);

                rollingAverage = roll(rollingAverage, event.values[1]);
                gravity = averageList(rollingAverage);

                break;

            // Check for orientation-change sensor event
            case Sensor.TYPE_ROTATION_VECTOR:
                orientation = display.getRotation();
                switch (orientation) {
                    case Surface.ROTATION_90:
                        if (orientationhandler != 1) {
                            orientationhandler = 1;
                        }
                        break;
                    case Surface.ROTATION_270:
                        if (orientationhandler != -1) {
                            orientationhandler = -1;
                        }
                        break;
                }
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.beamng.udpsteering/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        // unregister sensor listeners to prevent the activity from draining the device's battery.
        mSensorManager.unregisterListener(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister sensor listeners to prevent the activity from draining the device's battery.
        mSensorManager.unregisterListener(this);
        stopUdpTasks();
    }

    @Override
    public void onResume() {
        super.onResume();
        initListeners();
        startUdpTasks();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // This function registers sensor listeners for the accelerometer
    public void initListeners() {
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void updateOrientationDisplay() {
        //angle damped via the average of the last 5 sensordata entries
        //with Boundaries -60 to +60°:
        //float uiAngle =Math.min(Math.max((float) (gravity * -7.9 * orientationhandler), -60f),60f);
        float uiAngle = (float) (gravity * -7.9 * orientationhandler);
        //animation of the whole mainLayout when UI is updated every 50ms
        oban = ObjectAnimator.ofFloat(mainLayout, "rotation", oldangle, uiAngle);
        oban.setDuration(50);
        oban.setInterpolator(new LinearInterpolator());
        oban.start();

        oldangle = uiAngle;
    }

    private Runnable updateOrientationDisplayTask = new Runnable() {
        public void run() {
            updateOrientationDisplay();
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.beamng.udpsteering/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    class calculateFusedOrientationTask extends TimerTask {
        public void run() {
            // update sensor output in GUI
            mHandler.post(updateOrientationDisplayTask);
        }
    }

    //rolling list of the last 5 Sensorevents
    public List<Float> roll(List<Float> list, float newMember) {
        if (list.size() == MAX_SAMPLE_SIZE) {
            list.remove(0);
        }
        list.add(newMember);
        return list;
    }

    //average of the rolling list
    public float averageList(List<Float> tallyUp) {

        float total = 0;
        for (float item : tallyUp) {
            total += item;
        }
        total = total / tallyUp.size();

        return total;
    }

    private void hideSystemUI() {
        // If the Android version is lower than Jellybean, use this call to hide
        // the status bar.
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher
            int uiOptions;
            if (Build.VERSION.SDK_INT >= 19) {
                uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                decorView.setSystemUiVisibility(uiOptions);
            } else {
                uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                decorView.setSystemUiVisibility(uiOptions);
            }
        }
    }

    public void connectionTimeout() {
        //udptest.setVisibility(View.VISIBLE);
        sessionsender.cancel(true);
        sessionreceiver.cancel(true);
        Toast.makeText(aContext, "Your connection timed out", Toast.LENGTH_LONG).show();
        executor.shutdownNow();
        mDecodeWorkQueue = new LinkedBlockingQueue<>();
        // Creates a thread pool manager
        executor = new ThreadPoolExecutor(
                NUMBER_OF_CORES,       // Initial pool size
                NUMBER_OF_CORES,       // Max pool size
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mDecodeWorkQueue);
    }

    private void startUdpTasks() {
        stopUdpTasks();
        sessionsender = new UdpSessionSender(hostAddress, aContext, Iadress);
        sessionsender.executeOnExecutor(executor);
        sessionreceiver = new UdpSessionReceiver(hostAddress, aContext, Iadress);
        sessionreceiver.executeOnExecutor(executor);
    }

    private void stopUdpTasks() {
        if (sessionsender != null) {
            sessionsender.cancel(false);
        }
        if (sessionreceiver != null) {
            // Notice that we are calling our own cancel method for the receiver:
            sessionreceiver.cancel();
        }
    }

    public class UdpSessionSender extends AsyncTask<String, String, String> {
        final int PORT = 4444;
        InetAddress receiverAddress;
        Activity aContext;
        String myIadr;

        public UdpSessionSender(InetAddress iadrSend, Activity activityContext, String myiadrr) {
            this.receiverAddress = iadrSend;
            this.aContext = activityContext;
            this.myIadr = myiadrr;
        }

        @Override
        protected String doInBackground(String... arg0) {
            Log.i("UdpClient", "started");
            try {
                DatagramSocket socket = new DatagramSocket();
                try {
                    Sendpacket sendpacket = new Sendpacket();
                    while (!isCancelled()) {
                        try {
                            Thread.sleep(sendingTimeout);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //Log.i("Sensitivity", ": " + sensitivitySetting);
                        sendpacket.setSteeringAngle(Math.min(Math.max((angle *sensitivitySetting * orientationhandler) / 75, -0.5f), 0.5f) + 0.5f);
                        sendpacket.setThrottle(thrpushed);
                        sendpacket.setBreaks(brpushed);
                        sendpacket.setID(pID);

                        //set packet sent time
                        if (lastID != pID) {
                            lastID = pID;
                            lpTime = System.currentTimeMillis();
                        }

                        byte[] buffer = sendpacket.getSendingByteArray();
                        socket.send(
                                new DatagramPacket(buffer, buffer.length, receiverAddress, PORT)
                        );
                        // Log.i("UDP", "Package sent to " + receiverAddress + ":" + PORT);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    socket.disconnect();
                    socket.close();
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class UdpSessionReceiver extends AsyncTask<String, String, String> {
        final int PORT = 4445;
        InetAddress receiveradress;
        Activity aContext;
        String myIadr;
        InetAddress hostAddress;
        String message;
        int oldSpeed = 0;
        int oldRPM = 0;
        int oldEngTemp = 0;
        int oldFuel = 0;
        Receivepacket packet;
        private ObjectAnimator animation1;
        private ObjectAnimator animation2;
        private ObjectAnimator animation3;
        private ObjectAnimator animation4;
        private AnimatorSet animSet;
        DatagramSocket socket;

        public UdpSessionReceiver(InetAddress iadrSend, Activity activityContext, String myiadrr) {
            this.receiveradress = iadrSend;
            this.aContext = activityContext;
            this.myIadr = myiadrr;
        }

        // We need or own cancel method because socket.receive is blocking and we therefore need
        // to close the socket to quit doInBackground
        public void cancel() {
            super.cancel(false);
            socket.close();
        }

        @Override
        protected String doInBackground(String... arg0) {
            Log.i("UdpServer", "started");
            Log.i("ReceiveSocketBinder", Iadress + ":" + PORT);
            try {
                DatagramChannel channel = DatagramChannel.open();
                socket = channel.socket();
                socket.bind(new InetSocketAddress(Iadress, PORT));
                socket.setSoTimeout(0); // infinite timeout
                try {
                    byte[] buf = new byte[100];
                    while (!isCancelled()) {
                        try {
                            socket.receive(new DatagramPacket(buf, buf.length));
                        } catch (SocketTimeoutException e) {
                            publishProgress("TIMEOUT");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        packet = new Receivepacket(buf);
                        //Log.i("UDP SERVER","Received a packet");
                        publishProgress("");
                    }
                } finally {
                    socket.disconnect();
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onProgressUpdate(String... values) {

            if (values != null && values[0].equals("TIMEOUT")) {
                cancel(true);
                connectionTimeout();
                return;
            }
            //Log.i("packet ID ", "returned: " + packet.getID());

            if (packet.getID() == pID) {
                oldDiff = timeDiff;
                timeDiff = System.currentTimeMillis()-lpTime;
                //smooth the display a bit so the numbers dont jump so erratically
                float disDiff = (oldDiff+timeDiff)/2;
                disDiff /= 2;
                if (timeDiff != 0)
                    textDelay.setText("Delay: "+disDiff+"ms");
                pID++;
                if(pID == 128)
                    pID = 0;
            }
            //convert m/s to mp/h
            int newSpeed = Math.round(2.23694f * packet.getSpeed());
            if (useKMH == 1)
                newSpeed =  Math.round(1.60934f*newSpeed);
            //Log.i("Speed ", "set to: " + packet.getSpeed());
            int barSpeed = Math.round(newSpeed*0.56f);
            animation1 = ObjectAnimator.ofInt(pbSpeed, "progress", oldSpeed, barSpeed);
            oldSpeed = barSpeed;

            int newRPM = Math.round(0.0155f * packet.getRPM());
            //Log.i("RPM ", "set to: " + packet.getRPM());
            animation2 = ObjectAnimator.ofInt(pbRspeed, "progress", oldRPM, newRPM);
            oldRPM = newRPM;

            int newEngTemp = Math.round(42 * packet.getEngineTemp());
            //Log.i("Engtemp ", "set to: " + packet.getEngineTemp());
            animation3 = ObjectAnimator.ofInt(pbHeat, "progress", oldEngTemp, newEngTemp);
            oldEngTemp = newEngTemp;

            int newFuel = Math.round(42 * packet.getFuel());
            //Log.i("Fuel", "set to: " + packet.getFuel());
            animation4 = ObjectAnimator.ofInt(pbFuel, "progress", oldFuel, newFuel);
            oldFuel = newFuel;

            animSet = new AnimatorSet();
            animSet.playTogether(animation1, animation2, animation3, animation4);
            animSet.setInterpolator(new LinearInterpolator());
            animSet.setDuration(500);
            animSet.start();

            textSpeed.setText(String.format("%03d", newSpeed));

            textGear.setText(packet.getGear());
            textOdo.setText(String.format("%06d", packet.getOdometer()));

            boolean[] lightsarray = packet.getActiveLightsArr();

            for (int i = 0; i < 11; i++) {
                //Check if we have a View for that Flag

                if (lightViews[i] != null) {
                    if (lightsarray[i]) {
                        if (lightViews[i].getVisibility() == View.INVISIBLE) {
                            lightViews[i].setVisibility(View.VISIBLE);
                        }

                    } else {
                        if (lightViews[i].getVisibility() == View.VISIBLE) {
                            lightViews[i].setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }

            if (packet.getFlagsArray()[3]) {
                //KMH
                //Log.i("User wants ","KMH");
            }
        }
    }
}
