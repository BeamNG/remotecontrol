package com.beamng.udpsteering;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity implements SensorEventListener, OnUdpConnected {

    private SensorManager mSensorManager;
    public Handler mHandler;
    private Context mContext;
    private NetworkInfo mWifi;
    private ConnectivityManager connManager;
    private InetAddress adress;
    private Activity aContext;
    private OnUdpConnected udpInterf;
    private String Iadress;
    private float angle;
    private float oldangle = 0.0f;
    private int sendingTimeout = 50;
    private ObjectAnimator oban;
    private ProgressDialog ringProgressDialog;
    private boolean connected;
    private UdpExploreSender exploreSender;
    private UdpSessionSender sessionsender;
    private UdpSessionReceiver sessionreceiver;
    private WifiManager wifiManager;


    //Sensordata damping elements
    private List<Float>[] rollingAverage = new List[4];
    private static final int MAX_SAMPLE_SIZE = 5;
    private float gravity;


    //UI Elements
    private Button udptest;
    private Button throttle;
    private Button breaks;
    private float thrpushed;
    private float brpushed;

    //Views depending on communication
    private RelativeLayout mainLayout;
    private ProgressBar pbSpeed;
    private ProgressBar pbRspeed;
    private ProgressBar pbFuel;
    private ProgressBar pbHeat;
    private TextView textSpeed;
    private TextView textGear;
    private TextView textOdo;
    private ImageView[] lightViews;

    //Orientationhandling
    private Display display;
    int orientation;
    private int orientationhandler = 1;

    // magnetic field vector
    private float[] magnet = new float[3];

    // accelerometer vector
    private float[] accel = new float[3];

    // orientation angles from accel and magnet
    private float[] accMagOrientation = new float[3];

    // accelerometer and magnetometer based rotation matrix
    private float[] rotationMatrix = new float[9];

    //UI update interval
    public static final int TIME_CONSTANT = 50;

    private Timer fuseTimer = new Timer();

    //Multithreading
    private ThreadPoolExecutor executor;
    private BlockingQueue<Runnable> mDecodeWorkQueue;
    private int KEEP_ALIVE_TIME = 1;
    private TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private int NUMBER_OF_CORES;


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus){
            hideSystemUI();
        }
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        //HUD-Lights in the order of given Structure in Recievepacket.java
        lightViews = new ImageView[11];
        lightViews[10] = (ImageView) findViewById(R.id.light_abs);
        lightViews[2] = (ImageView) findViewById(R.id.light_break);
        lightViews[0] = (ImageView) findViewById(R.id.light_headlight);
        lightViews[1] = (ImageView) findViewById(R.id.light_fullbeam);
        lightViews[5] = (ImageView) findViewById(R.id.light_leftindicator);
        lightViews[6] = (ImageView) findViewById(R.id.light_rightindicator);

        udptest = (Button) findViewById(R.id.button);
        throttle = (Button) findViewById(R.id.throttlecontrol);
        breaks = (Button) findViewById(R.id.breakcontrol);
        ringProgressDialog = new ProgressDialog(this);


        mContext = getApplicationContext();

        aContext = this;

        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        orientation = display.getRotation();

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




        fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
                1000, TIME_CONSTANT);

        udpInterf = this;

        // Check for WiFi connectivity
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(mWifi == null || !mWifi.isConnected())
        {
            Toast.makeText(this,"You need to be connected to a WiFi network.",Toast.LENGTH_LONG).show();
        }

        //Buttons
        udptest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
                Iadress = String.format("%d.%d.%d.%d",(ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                        (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));

                mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if(mWifi.isConnected()) {
                    adress = getBroadcastAddress(getIpAddress());
                    Log.i("Broadcastadress", adress.getHostAddress());

                    if (!connected) {
                        new UdpExploreSender(adress, aContext, udpInterf, Iadress, MainActivity.this).executeOnExecutor(executor);
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"Not connected to a WIFI network",Toast.LENGTH_SHORT).show();
                }


            }
        });


        throttle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    thrpushed = 1f;

                }else if(event.getAction() == MotionEvent.ACTION_UP) {
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

                }else if(event.getAction() == MotionEvent.ACTION_UP) {
                    brpushed = 0f;
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




    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                // copy new accelerometer data into accel array and calculate orientation
                System.arraycopy(event.values, 0, accel, 0, 3);
                calculateAccMagOrientation();

                rollingAverage[1] = roll(rollingAverage[1], event.values[1]);
                gravity = averageList(rollingAverage[1]);


                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                // copy new magnetometer data into magnet array
                System.arraycopy(event.values, 0, magnet, 0, 3);
                break;

            // Check for orientation-change sensor event
            case Sensor.TYPE_ROTATION_VECTOR:
                orientation = display.getRotation();
                switch (orientation) {
                    case Surface.ROTATION_90:
                        if(orientationhandler != 1){orientationhandler=1;}
                        break;
                    case Surface.ROTATION_270:
                        if(orientationhandler != -1){orientationhandler=-1;}
                        break;
                }
                 break;

        }
    }

    // calculates orientation angles from accelerometer and magnetometer output
    public void calculateAccMagOrientation() {
        if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister sensor listeners to prevent the activity from draining the device's battery.
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister sensor listeners to prevent the activity from draining the device's battery.
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // restore the sensor listeners when user resumes the application.
        // should be altered, when "Connecting..."-screen is implemented
        initListeners();
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    // This function registers sensor listeners for the accelerometer, magnetometer
    public void initListeners(){

        rollingAverage[1] = new ArrayList<Float>();

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void updateOreintationDisplay() {
        //most accurate angle to be send via UDP
        angle = (float) (accMagOrientation[1] * 180 / Math.PI);

        //angle damped via the average of the last 5 sensordata entries
        //with Boundaries -60 to +60°:
        //float uiAngle =Math.min(Math.max((float) (gravity * -7.9 * orientationhandler), -60f),60f);
        float uiAngle =(float) (gravity * -7.9 * orientationhandler);
        //animation of the whole mainLayout when UI is updated every 50ms
        oban = ObjectAnimator.ofFloat(mainLayout,"rotation",oldangle,uiAngle);
            oban.setDuration(50);
            oban.setInterpolator(new LinearInterpolator());
            oban.start();

        oldangle = uiAngle;
    }


    private Runnable updateOrientationDisplayTask = new Runnable() {
        public void run() {
            updateOreintationDisplay();
        }
    };
    class calculateFusedOrientationTask extends TimerTask {
        public void run() {
            // update sensor output in GUI
            mHandler.post(updateOrientationDisplayTask);
        }
    }

    //rolling list of the last 5 Sensorevents
    public List<Float> roll(List<Float> list, float newMember){
        if(list.size() == MAX_SAMPLE_SIZE){
            list.remove(0);
        }
        list.add(newMember);
        return list;
    }
    //average of the rolling list
    public float averageList(List<Float> tallyUp){

        float total=0;
        for(float item : tallyUp ){
            total+=item;
        }
        total = total/tallyUp.size();

        return total;
    }

    //method to get Network Broadcastadress
    public InetAddress getBroadcastAddress(InetAddress inetAddr) {
        NetworkInterface temp;
        InetAddress iAddr = null;
        try {
            temp = NetworkInterface.getByInetAddress(inetAddr);
            List <InterfaceAddress> addresses = temp.getInterfaceAddresses();

            for (InterfaceAddress inetAddress: addresses)

                iAddr = inetAddress.getBroadcast();
            return iAddr;

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    //method to get IpAdress
    public InetAddress getIpAddress() {
        InetAddress inetAddress = null;
        InetAddress myAddr = null;

        try {
            for (Enumeration < NetworkInterface > networkInterface = NetworkInterface
                    .getNetworkInterfaces(); networkInterface.hasMoreElements();) {

                NetworkInterface singleInterface = networkInterface.nextElement();

                for (Enumeration< InetAddress > IpAddresses = singleInterface.getInetAddresses(); IpAddresses
                        .hasMoreElements();) {
                    inetAddress = IpAddresses.nextElement();

                    if (!inetAddress.isLoopbackAddress() && (singleInterface.getDisplayName()
                            .contains("wlan0") ||
                            singleInterface.getDisplayName().contains("eth0") ||
                            singleInterface.getDisplayName().contains("ap0"))) {

                        myAddr = inetAddress;
                    }
                }
            }

        } catch (SocketException ex) {
        }
        return myAddr;
    }

    private void hideSystemUI(){
        // If the Android version is lower than Jellybean, use this call to hide
        // the status bar.
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }else{
            View decorView = getWindow().getDecorView();
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher
            int uiOptions;
            if (Build.VERSION.SDK_INT >= 19){
                uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                decorView.setSystemUiVisibility(uiOptions);
            }else{
                uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                decorView.setSystemUiVisibility(uiOptions);}
        }
    }

    public void connectionTimeout(){
        udptest.setVisibility(View.VISIBLE);
        sessionsender.cancel(true);
        sessionreceiver.cancel(true);
        connected=false;
        Toast.makeText(aContext,"Your connection timed out",Toast.LENGTH_LONG).show();
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

    //Interface for starting threads for sending and recieving data
    @Override
    public void onUdpConnected(InetAddress hostadress) {
        Toast.makeText(getApplicationContext(),"Connected to BeamNG",Toast.LENGTH_LONG).show();
        initListeners();
        ringProgressDialog.dismiss();
        udptest.setVisibility(View.GONE);
        sessionsender = new UdpSessionSender(hostadress,aContext,Iadress);
        sessionsender.executeOnExecutor(executor);
        sessionreceiver = new UdpSessionReceiver(hostadress,aContext,Iadress);
        sessionreceiver.executeOnExecutor(executor);
        connected = true;
    }

    //udp sender thread
    public class UdpSessionSender extends AsyncTask<String,String,String> {

        DatagramPacket packets;
        Float sendFloat;
        int PORT = 4445;
        InetAddress receiveradress;
        DatagramSocket socketS;
        Activity aContext;
        Boolean bKeepRunning = true;
        String myIadr;
        Sendpacket sendpacket;


        public UdpSessionSender (InetAddress iadrSend, Activity activityContext, String myiadrr) {
            this.receiveradress = iadrSend;
            this.aContext = activityContext;
            this.myIadr = myiadrr;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            bKeepRunning=false;
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    socketS.disconnect();
                    socketS.close();
                }
            }, 200);
        }

        @Override
        protected String doInBackground(String... arg0) {

            Log.i("UdpClient","started");
            while (bKeepRunning){
                try {
                    Thread.sleep(sendingTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            sendFloat = Math.min(Math.max((angle*orientationhandler)/75,-1f),1f);

                if (sendpacket == null){
                    sendpacket = new Sendpacket(sendFloat,thrpushed,brpushed);
                }else{
                    sendpacket.setSteeringAngle(sendFloat);
                    sendpacket.setThrottle(thrpushed);
                    sendpacket.setBreaks(brpushed);
                }
                if(isCancelled()){
                    bKeepRunning = false;
                    continue;
                }

            byte[] buffer = sendpacket.getSendingByteArray();
            if(socketS == null) {
                try {
                    socketS = new DatagramSocket();}catch (SocketException e) {e.printStackTrace();}}
            try {
                packets = new DatagramPacket(buffer, buffer.length, receiveradress, PORT);
                socketS.send(packets);
                Log.i("UDP","Package Sent.");
            } catch (Exception e) {
                e.printStackTrace();
            }
                }
            return null;
        }

    }

    //udp reciever thread
    public class UdpSessionReceiver extends AsyncTask<String,String,String> {

        DatagramPacket packetr;
        int PORT = 4445;
        InetAddress receiveradress;
        DatagramSocket socketR;
        Activity aContext;
        Boolean bKeepRunning = true;
        String myIadr;
        InetAddress hostadress;
        String message;
        int oldSpeed = 0;
        int oldRPM = 0;
        int oldEngTemp = 0;
        int oldFuel = 0;
        int newSpeed = 0;
        int newRPM = 0;
        int newEngTemp = 0;
        int newFuel = 0;
        String speedvar ="";
        Recievepacket packet;
        private ObjectAnimator animation1;
        private ObjectAnimator animation2;
        private ObjectAnimator animation3;
        private ObjectAnimator animation4;
        private AnimatorSet animSet;


        public UdpSessionReceiver (InetAddress iadrSend, Activity activityContext, String myiadrr) {
            this.receiveradress = iadrSend;
            this.aContext = activityContext;
            this.myIadr = myiadrr;
        }

        @Override
        protected String doInBackground(String... arg0) {
            Log.i("UdpServer","started");
            Log.i("RecieveSocketBinder",Iadress + ":4445");
            if(socketR == null) {
                try {
                    DatagramChannel channel = DatagramChannel.open();
                    socketR = channel.socket();
                    socketR.bind(new InetSocketAddress(Iadress , PORT));
                }catch (Exception e) {e.printStackTrace();}}

            try {
                //Timeout after 10 seconds of not recieving a packet
                socketR.setSoTimeout(10000);
            }catch (SocketException e){}


            byte[] buf = new byte[67];
            packetr = new DatagramPacket(buf, buf.length);
            while (bKeepRunning){
                try {
                    socketR.receive(packetr);
                }catch(SocketTimeoutException e){
                    publishProgress("TIMEOUT");
                }catch (IOException e) {
                    e.printStackTrace();
                }
                if(isCancelled()){
                    bKeepRunning = false;
                    continue;
                }

                packet = new Recievepacket(buf);
                hostadress = packetr.getAddress();
                Log.i("UDP SERVER","Recieved a packet");
                publishProgress("");

            }

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            bKeepRunning=false;
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    socketR.disconnect();
                    socketR.close();
                }
            }, 200);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if(values!=null && values[0].equals("TIMEOUT")){
                cancel(true);
                connectionTimeout();
                return;
            }
            //Speed
            newSpeed = Math.round(123 * packet.getSpeed());
            //Log.i("Speed ", "set to: " + packet.getRPM());
            animation1 = ObjectAnimator.ofInt(pbSpeed, "progress", oldSpeed, newSpeed);
            oldSpeed = newSpeed;

            //RPM
            newRPM = Math.round(123 * packet.getRPM());
            //Log.i("RPM ", "set to: " + packet.getSpeed());
            animation2 = ObjectAnimator.ofInt(pbRspeed, "progress", oldRPM, newRPM);
            oldRPM = newRPM;

            //EngTemp
            newEngTemp = Math.round(42 * packet.getEngineTemp());
            //Log.i("Engtemp ", "set to: " + packet.getEngineTemp());
            animation3 = ObjectAnimator.ofInt(pbHeat, "progress", oldEngTemp, newEngTemp);
            oldEngTemp = newEngTemp;

            //Fuel
            newFuel= Math.round(42 * packet.getFuel());
            //Log.i("Fuel", "set to: " + packet.getFuel());
            animation4 = ObjectAnimator.ofInt(pbFuel, "progress", oldFuel, newFuel);
            oldFuel = newFuel;

            animSet = new AnimatorSet();
            animSet.playTogether(animation1,animation2,animation3,animation4);
            animSet.setInterpolator(new LinearInterpolator());
            animSet.setDuration(500);
            animSet.start();

            speedvar = String.format("%03d", Math.round(220 * packet.getSpeed()));
            textSpeed.setText(speedvar);

            textGear.setText(packet.getGear());
            textOdo.setText(String.format("%06d",packet.getOdometer()));

            boolean[] lightsarray = packet.getActiveLightsArr();

            for (int i=0;i<11;i++) {
                //Check if we have a View for that Flag
                if(lightViews[i]!= null) {
                    if (lightsarray[i]) {
                        if(lightViews[i].getVisibility() == View.INVISIBLE) {
                            lightViews[i].setVisibility(View.VISIBLE);
                        }

                    } else {
                        if(lightViews[i].getVisibility() == View.VISIBLE){
                            lightViews[i].setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }

            if(packet.getFlagsArray()[3]){
                //KMH
                Log.i("User wants ","KMH");
            }


                bKeepRunning = true;

        }

    }

}