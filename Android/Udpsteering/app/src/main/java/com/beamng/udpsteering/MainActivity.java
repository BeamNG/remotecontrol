package com.beamng.udpsteering;

import android.animation.ObjectAnimator;
import android.app.Activity;
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
import java.util.Arrays;
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
    private int sendingTimeout = 500;
    private ThreadPoolExecutor executor;
    public ObjectAnimator animation;
    private ObjectAnimator oban;
    int oldProgress = 0;
    int newProgress = 0;

    //Sensordata damping elements
    private List<Float>[] rollingAverage = new List[3];
    private static final int MAX_SAMPLE_SIZE = 5;
    private float gravity;
    private int orientationhandler = 1;

    //test
    private int x = 10;

    //UI Elements
    private TextView serverMessage;
    private Button udptest;
    private Button throttle;
    private Button breaks;

    //Views depending on communication
    private RelativeLayout mainLayout;
    private ProgressBar pbSpeed;
    private ProgressBar pbRspeed;
    private ProgressBar pbFuel;
    private ProgressBar pbHeat;
    private TextView textSpeed;
    private TextView textGear;
    private TextView testOdo;

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

        udptest = (Button) findViewById(R.id.button);
        serverMessage = (TextView) findViewById(R.id.tv_servmess);
        throttle = (Button) findViewById(R.id.throttlecontrol);
        breaks = (Button) findViewById(R.id.breakcontrol);
        mContext = getApplicationContext();

        aContext = this;


        //Multi-Thread-executor:
        // A queue of Runnables
        BlockingQueue<Runnable> mDecodeWorkQueue;
        // Instantiates the queue of Runnables as a LinkedBlockingQueue
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
                try {adress = getBroadcastAddress();
                    Log.i("Broadcastadress", adress.getHostAddress());}catch (IOException e) {e.printStackTrace();}

                new UdpExploreSender(adress, aContext,udpInterf,Iadress).executeOnExecutor(executor,null);
            }
        });
        throttle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Gas", "geklickt");
            }
        });
        breaks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Bremse", "geklickt");
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
        initListeners();

        mHandler = new Handler();


        //Testing of all progressbars
        new Thread(new Runnable() {

            public void run() {
                while (x > 1) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // Update the progress bars via testmethod
                    mmHandler.post(new Runnable() {
                        public void run() {
                            testmethod();

                        }
                    });
                }
            }

        }).start();

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
    }

    public void updateOreintationDisplay() {
        //most accurate angle to be send via UDP
        angle = (float) (accMagOrientation[1] * 180 / Math.PI);

        //angle damped via the average of the last 5 sensordata entries

        if (accMagOrientation[2] > 1.0 && orientationhandler !=-1){
            orientationhandler = -1;
            Log.e("CHANGE","SENSORS to -1");
        }else if(accMagOrientation[2] < -1.0 && orientationhandler !=1){
            orientationhandler = 1;
            Log.e("CHANGE","SENSORS to 1");
        }

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

    //method to get Inetadress
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

    //testing of the progressbars (maximum progress 1&2 = 62; 3&4 = 21)
    public void testmethod() {

        if (x>1) {
        int from = 62 / x;
        int to = 62 / (x-1);
        int from1 = 21 / x;
        int to1 = 21 / (x-1);

            ObjectAnimator an;
            an = ObjectAnimator.ofInt(pbSpeed, "progress",from,to);
            an.setDuration(200);
            an.setInterpolator(new LinearInterpolator());
            an.start();

            ObjectAnimator an2;
            an2 = ObjectAnimator.ofInt(pbRspeed, "progress",from,to);
            an2.setDuration(200);
            an2.setInterpolator(new LinearInterpolator());
            an2.start();

            ObjectAnimator an3;
            an3 = ObjectAnimator.ofInt(pbFuel, "progress",from1,to1);
            an3.setDuration(200);
            an3.setInterpolator(new LinearInterpolator());
            an3.start();

            ObjectAnimator an4;
            an4 = ObjectAnimator.ofInt(pbHeat, "progress",from1,to1);
            an4.setDuration(200);
            an4.setInterpolator(new LinearInterpolator());
            an4.start();

            x = x -1;
        }

    }

    //Interface for starting threads for sending and recieving data
    @Override
    public void onUdpConnected(InetAddress hostadress) {
        Toast.makeText(getApplicationContext(),"Connected to BeamNG",Toast.LENGTH_LONG).show();
        UdpSessionSender sessionsender = new UdpSessionSender(hostadress,aContext,Iadress);
        sessionsender.executeOnExecutor(executor,null);
        UdpSessionReceiver sessionreceiver = new UdpSessionReceiver(hostadress,aContext,Iadress);
        sessionreceiver.executeOnExecutor(executor,null);
    }

    //udp sender thread
    public class UdpSessionSender extends AsyncTask<String,String,String> {

        DatagramPacket packets;
        DatagramPacket packetr;
        Float sendFloat;
        int PORT = 7001;
        InetAddress receiveradress;
        DatagramSocket socketS;
        DatagramSocket socketR;
        Activity aContext;
        Boolean bKeepRunning = true;
        String myIadr;
        InetAddress hostadress;


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

            sendFloat = angle;
                int accel = 5;
            Log.e("SendAccel: ", accel + "");
            Log.e("SendFloat: ", sendFloat+"");
                String sendString = "St:"+sendFloat+" Ac:" + accel;
            byte[] buffer = sendString.getBytes();
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

            Log.i("RecieveSocketBinder",Iadress + ":7001");
            if(socketR == null) {
                try {
                    DatagramChannel channel = DatagramChannel.open();
                    socketR = channel.socket();
                    socketR.bind(new InetSocketAddress(Iadress , PORT));
                }catch (Exception e) {e.printStackTrace();}}

            byte[] buf = new byte[1024];
            packetr = new DatagramPacket(buf, buf.length);
            while (bKeepRunning){
                try {socketR.receive(packetr);}catch (IOException e) {e.printStackTrace();}
                String message = new String(buf, 0, packetr.getLength());
                hostadress = packetr.getAddress();
                Log.i("UDP SERVER","Recieved: " + message + " IP " + packetr.getAddress().toString() + ":" + packetr.getPort());
                publishProgress(message);
            }

            return null;
        }

    }

    //udp reciever thread
    public class UdpSessionReceiver extends AsyncTask<String,String,String> {

        DatagramPacket packetr;
        int PORT = 7001;
        InetAddress receiveradress;
        DatagramSocket socketR;
        Activity aContext;
        Boolean bKeepRunning = true;
        String myIadr;
        InetAddress hostadress;
        String message;


        public UdpSessionReceiver (InetAddress iadrSend, Activity activityContext, String myiadrr) {
            this.receiveradress = iadrSend;
            this.aContext = activityContext;
            this.myIadr = myiadrr;
        }

        @Override
        protected String doInBackground(String... arg0) {
            Log.i("UdpServer","started");
            Log.i("RecieveSocketBinder",Iadress + ":7001");
            if(socketR == null) {
                try {
                    DatagramChannel channel = DatagramChannel.open();
                    socketR = channel.socket();
                    socketR.bind(new InetSocketAddress(Iadress , PORT));
                }catch (Exception e) {e.printStackTrace();}}

            byte[] buf = new byte[1024];
            packetr = new DatagramPacket(buf, buf.length);
            while (bKeepRunning){
                try {socketR.receive(packetr);}catch (IOException e) {e.printStackTrace();}
                message = new String(buf, 0, packetr.getLength());
                int uint8first = buf[0] & 0xFF;
                String maskMessage = Integer.toBinaryString(uint8first);
                hostadress = packetr.getAddress();
                Log.i("UDP SERVER","Recieved: " + maskMessage + " IP " + packetr.getAddress().toString() + ":" + packetr.getPort());
                publishProgress(maskMessage);
            }

            return null;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            serverMessage.setText("Server-Nachricht: "+ Arrays.toString(values));


            if (Arrays.toString(values).contains("1000001")){
                if (message.length()>1){
                    newProgress = Integer.valueOf(message.substring(1,message.length()));
                Log.i("Speed","set to: " + message.substring(1,message.length()));

                    animation = ObjectAnimator.ofInt(pbSpeed, "progress", oldProgress, newProgress);
                    animation.setDuration(50);
                    animation.setInterpolator(new LinearInterpolator());
                    animation.start();
                    android.os.SystemClock.sleep(50);
                    pbSpeed.setProgress(newProgress);
                    oldProgress = newProgress;
                }

                bKeepRunning = true;

            }else {
                bKeepRunning = true;
            }
        }

    }

}