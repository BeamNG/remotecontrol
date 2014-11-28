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
import android.net.DhcpInfo;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
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
    private InetAddress adress;
    private Activity aContext;
    private OnUdpConnected udpInterf;
    private String Iadress;
    private float angle;
    private float oldangle = 0.0f;
    private int sendingTimeout = 50;
    private ThreadPoolExecutor executor;
    private ObjectAnimator oban;
    private ProgressDialog ringProgressDialog;
    private boolean connected;
    private UdpExploreSender exploreSender;


    //Sensordata damping elements
    private List<Float>[] rollingAverage = new List[4];
    private static final int MAX_SAMPLE_SIZE = 5;
    private float gravity;

    //test
    //private int x = 10;

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

    // Need handler for callbacks of the Testrunnable to the UI thread
    final Handler mmHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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



        setContentView(R.layout.activity_main);

        mainLayout = (RelativeLayout) findViewById(R.id.main);
        pbSpeed = (ProgressBar) findViewById(R.id.progressBar);
        pbRspeed = (ProgressBar) findViewById(R.id.progressBar2);
        pbFuel = (ProgressBar) findViewById(R.id.progressBar3);
        pbHeat = (ProgressBar) findViewById(R.id.progressBar4);
        textSpeed = (TextView) findViewById(R.id.Textspeed);
        textGear = (TextView) findViewById(R.id.Textgear);
        textOdo = (TextView) findViewById(R.id.Textodo);

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
        BlockingQueue<Runnable> mDecodeWorkQueue;
        // instantiate the queue of Runnables as a LinkedBlockingQueue
        mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();
        // Sets the amount of time an idle thread waits before terminating
        int KEEP_ALIVE_TIME = 1;
        // Sets the Time Unit to seconds
        TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

        int NUMBER_OF_CORES =
                Runtime.getRuntime().availableProcessors();

        // Creates a thread pool manager
        executor = new ThreadPoolExecutor(
                NUMBER_OF_CORES,       // Initial pool size
                NUMBER_OF_CORES,       // Max pool size
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mDecodeWorkQueue);




        fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
                1000, TIME_CONSTANT);


        //get broadcastadress and start initial message on buttonclick
        udpInterf = this;

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        Iadress = String.format("%d.%d.%d.%d",(ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));

        //Buttons

        udptest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    adress = getBroadcastAddress();
                    Log.i("Broadcastadress", adress.getHostAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(!connected){
                new UdpExploreSender(adress, aContext, udpInterf, Iadress,MainActivity.this).executeOnExecutor(executor);}


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

        // Check for WiFi connectivity
        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(mWifi == null || !mWifi.isConnected())
        {
            Toast.makeText(this,"Sorry! You need to be connected to a WiFi network. Aborting.",Toast.LENGTH_LONG).show();
        }

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
    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    //Interface for starting threads for sending and recieving data
    @Override
    public void onUdpConnected(InetAddress hostadress) {
        Toast.makeText(getApplicationContext(),"Connected to BeamNG",Toast.LENGTH_LONG).show();
        initListeners();
        ringProgressDialog.dismiss();
        udptest.setVisibility(View.GONE);
        UdpSessionSender sessionsender = new UdpSessionSender(hostadress,aContext,Iadress);
        sessionsender.executeOnExecutor(executor);
        UdpSessionReceiver sessionreceiver = new UdpSessionReceiver(hostadress,aContext,Iadress);
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

            byte[] buffer = sendpacket.getSendingByteArray();
            if(socketS == null) {
                try {
                    socketS = new DatagramSocket();}catch (SocketException e) {e.printStackTrace();}}
            try {
                packets = new DatagramPacket(buffer, buffer.length, receiveradress, PORT);
                Log.i("Socket","created + sending");

                socketS.send(packets);
                Log.i("UDP","C: Sent.");
            } catch (Exception e) {
                e.printStackTrace();
            }}
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
        public ObjectAnimator animation1;
        public ObjectAnimator animation2;
        public ObjectAnimator animation3;
        public ObjectAnimator animation4;


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

            byte[] buf = new byte[67];
            packetr = new DatagramPacket(buf, buf.length);
            while (bKeepRunning){
                try {socketR.receive(packetr);}catch (IOException e) {e.printStackTrace();}

                packet = new Recievepacket(buf);
                hostadress = packetr.getAddress();
                Log.i("UDP SERVER","Recieved a packet: IP " + packetr.getAddress().toString() + ":" + packetr.getPort());
                publishProgress();
            }

            return null;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            //Speed
            newSpeed = Math.round(123 * packet.getSpeed());
            Log.i("Speed ", "set to: " + packet.getRPM());
            animation1 = ObjectAnimator.ofInt(pbSpeed, "progress", oldSpeed, newSpeed);
            oldSpeed = newSpeed;

            //RPM
            newRPM = Math.round(123 * packet.getRPM());
            Log.i("RPM ", "set to: " + packet.getSpeed());
            animation2 = ObjectAnimator.ofInt(pbRspeed, "progress", oldRPM, newRPM);
            oldRPM = newRPM;

            //EngTemp
            newEngTemp = Math.round(42 * packet.getEngineTemp());
            Log.i("Engtemp ", "set to: " + packet.getEngineTemp());
            animation3 = ObjectAnimator.ofInt(pbHeat, "progress", oldEngTemp, newEngTemp);
            oldEngTemp = newEngTemp;

            //Fuel
            newFuel= Math.round(42 * packet.getFuel());
            Log.i("Speed", "set to: " + packet.getEngineTemp());
            animation4 = ObjectAnimator.ofInt(pbFuel, "progress", oldFuel, newFuel);
            oldFuel = newFuel;

            AnimatorSet animSet = new AnimatorSet();
            animSet.play(animation1).with(animation2).with(animation3).with(animation4);
            animSet.setInterpolator(new LinearInterpolator());
            animSet.setDuration(500);
            animSet.start();

            speedvar = String.format("%03d", Math.round(220 * packet.getSpeed()));
            textSpeed.setText(speedvar);

            textGear.setText(packet.getGear());
            textOdo.setText(String.format("%06d",packet.getOdometer()));

            boolean[] lightsarray = packet.getActiveLightsArr();

            for (int i=0;i<11;i++) {
                if(lightsarray[i]==true){
                    //turn lights on
                    Log.i("LIGHTSARRAY","Number "+i+" turned on.");
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