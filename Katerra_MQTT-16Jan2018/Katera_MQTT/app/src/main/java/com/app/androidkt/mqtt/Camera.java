package com.app.androidkt.mqtt;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.app.ProgressDialog;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.markushi.ui.CircleButton;

import static android.R.attr.bitmap;
import static android.R.attr.focusableInTouchMode;


public class Camera extends AppCompatActivity implements CallBacks, TextureView.SurfaceTextureListener,NetworkStateReceiver.NetworkStateReceiverListener  {

    Toolbar toolbar;
    ProgressDialog pDialog;
    SessionManager sessionManager;
    private CameraService myService;
    private boolean bound = false;
    Constants constants;
    JSONObject request_Payload;
    public MqttAndroidClient client;
    public PahoMqttClient pahoMqttClient;
    TextureView videoview;
    String VideoURL = "";
    Intent intent;
    int port;
   // LinearLayout left,right;
    RelativeLayout left,right;
    //Button snapshots;
    CircleButton snapshots,cameraUp,cameraDown,cameraLeft,cameraRight;
    RelativeLayout mainLayout;
    String[]cameraList;
    List<String> ips;
    ProgressDialog progress ;
    RelativeLayout rLayout2;
    RelativeLayout layout1,layout3;
    String CameraIP=null;
    MediaPlayer mMediaPlayer;
    private NetworkStateReceiver networkStateReceiver;
    Handler h = new Handler();
    int delay = 15000; //15 seconds
    Runnable runnable;
    SurfaceTexture surface;
    ImageView topImg,bottomImg,leftImg,rightImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gateway_details);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mainLayout=(RelativeLayout)findViewById(R.id.mainLayout);
        left=(RelativeLayout)findViewById(R.id.left);
        right=(RelativeLayout)findViewById(R.id.right);
       // layout1=(RelativeLayout) findViewById(R.id.relativeLayout1);
        //layout3=(RelativeLayout)findViewById(R.id.relativeLayout3);
        snapshots=(CircleButton) findViewById(R.id.snapshots);
        cameraUp=(CircleButton) findViewById(R.id.cameraUp);
        cameraDown=(CircleButton) findViewById(R.id.cameraDown);
        cameraLeft=(CircleButton) findViewById(R.id.cameraLeft);
        cameraRight=(CircleButton) findViewById(R.id.cameraRight);
        rLayout2 = (RelativeLayout) findViewById(R.id.buttonsLayout);
        setSupportActionBar(toolbar);
        progress = new ProgressDialog(Camera.this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Camera");
        sessionManager = new SessionManager(Camera.this);
        constants = new Constants();
        pahoMqttClient = new PahoMqttClient();
        client = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
        videoview = (TextureView) findViewById(R.id.videoView);
        intent = new Intent(this, Spider.class);
/*
        topImg = (ImageView) findViewById(R.id.upImg);
        bottomImg = (ImageView) findViewById(R.id.bottomImg);
        leftImg = (ImageView) findViewById(R.id.leftImg);
        rightImg = (ImageView) findViewById(R.id.rightImg);*/

       /* h.postDelayed(new Runnable() {
            public void run() {
                sendBroadcast(new Intent("ABCD"));
                runnable=this;
                h.postDelayed(runnable, delay);
            }
        }, delay);*/
        /*VideoURL="udp://@192.168.100.24:1112";*/
        /*VideoURL="udp://@192.168.100.162:1112";*/

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));


        mMediaPlayer = new MediaPlayer();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FetchCameraDetails();
            }
        },1000);
        videoview.setSurfaceTextureListener(Camera.this);
        cameraUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlCamera("up");
            }
        });
        cameraDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlCamera("down");
            }
        });
        cameraLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlCamera("left");
            }
        });
        cameraRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlCamera("right");
            }
        });
        snapshots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeScreenshot(getWindow().getDecorView().getRootView());
            }
        });



//        left.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ControlCamera("left");
//            }
//        });
//        right.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ControlCamera("right");
//            }
//        });
//        layout1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ControlCamera("up");
//            }
//        });
//        layout3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ControlCamera("down");
//            }
//        });
//        snapshots.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                takeScreenshot(getWindow().getDecorView().getRootView());
//            }
//        });


    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {


    }
    @Override
    public void networkAvailable() {
        sessionManager.setOffline("true");
        System.out.println("================Network Available Camera Activty"+sessionManager.getOffline());
    }

    @Override
    public void networkUnavailable() {
        System.out.println("================Network UnAvailable Camera Activty"+sessionManager.getOffline());
        if(sessionManager.getOffline().contains("true")){
            LayoutInflater li = LayoutInflater.from(Camera.this);
            View dialogView = li.inflate(R.layout.connection_lost_dialog, null);
            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(
                    Camera.this);
            // set title
            alertDialogBuilder.setTitle("Connection Lost");
            alertDialogBuilder.setView(dialogView);
            final EditText userInput = (EditText) dialogView
                    .findViewById(R.id.et_input);
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {

                                    dialog.cancel();
                                    try{
                                        Constants.MQTT_BROKER_URL = "tcp://" + userInput.getText() + ":1883";
                                        pahoMqttClient.disconnect(client);
                                        client = pahoMqttClient.getMqttClient(Camera.this, Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
                                        sessionManager.setOffline("false");
                                        Toast.makeText(Camera.this,"Connected to Local IP",Toast.LENGTH_SHORT).show();
                                    }
                                    catch (Exception exp)
                                    {
                                        exp.printStackTrace();
                                    }

                                }
                            });

            android.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            // show it
            alertDialog.show();
            sessionManager.setOffline("false");
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);

    }

    private void ControlCamera(String command) {

        try{
            request_Payload=new JSONObject();
            request_Payload.put("req_type","DEV_CONFIG_REQ");
            request_Payload.put("trans_id",constants.GenerateRandomNumber());
            request_Payload.put("gw_id",sessionManager.getGateway_ID());
            request_Payload.put("proto_id","CAMERA");
            request_Payload.put("cmd_id","");
            request_Payload.put("timestamp",constants.getTime());
            request_Payload.put("user_id",sessionManager.getUser_ID());
            request_Payload.put("dev_type",command);
            request_Payload.put("UserIP",getIP());
            MqttMessage message = new MqttMessage();
            message.setPayload(request_Payload.toString().getBytes());
            System.out.println("Camera Searching to=========="+message.toString());
            //pahoMqttClient.publishMessage(client, message.toString(), 2, "DEV_CONFIG_REQ/"+sessionManager.getGateway_ID());
            pahoMqttClient.publishMessage(client, message.toString(), 2, "Spider/"+sessionManager.getGateway_ID()+"/DEV_CONFIG_REQ/");
            Toast.makeText(Camera.this,command+"command",Toast.LENGTH_SHORT).show();
        }
        catch (Exception exp)
        {

        }
    }
    private Bitmap takeScreenshot(View mAnyView) {

        String imagePath = null;
        Bitmap imageBitmap = videoview.getBitmap();
        if (imageBitmap != null) {
            imagePath = MediaStore.Images.Media.insertImage(getContentResolver(), imageBitmap, "Screenshot", null);
            Toast.makeText(Camera.this,"Screenshot Taken",Toast.LENGTH_SHORT).show();
        }

        return null;
        //return videoview.getBitmap();


    }
    private void FetchCameraDetails() {

        try{
            request_Payload=new JSONObject();
            request_Payload.put("req_type","DEV_CONFIG_REQ");
            request_Payload.put("trans_id",constants.GenerateRandomNumber());
            request_Payload.put("gw_id",sessionManager.getGateway_ID());
            request_Payload.put("proto_id","CAMERA");
            request_Payload.put("cmd_id","");
            request_Payload.put("timestamp",constants.getTime());
            request_Payload.put("user_id",sessionManager.getUser_ID());
            request_Payload.put("dev_type","SEARCH");
            request_Payload.put("UserIP",getIP());
            MqttMessage message = new MqttMessage();
            message.setPayload(request_Payload.toString().getBytes());
            System.out.println("Camera Searching to=========="+message.toString());
            pahoMqttClient.publishMessage(client, message.toString(), 2, "Spider/"+sessionManager.getGateway_ID()+"/DEV_CONFIG_REQ/"+sessionManager.getGateway_ID());
            progress.setMessage("fetching camera ..please wait");
            progress.show();
        }
        catch (Exception exp)
        {
            exp.printStackTrace();
        }
    }
    public String getIP() throws Exception
    {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            // Do whatever
            WifiManager wm = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
            String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
            return ip;
        }
        else
        {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return  addr.getHostAddress();
                    }
                }
            }


        }
        return "";
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //startActivity(new Intent(this,Spider.class));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            CameraService.LocalBinder binder = (CameraService.LocalBinder) service;
            myService = binder.getService();
            bound = true;
            myService.setCallbacks(Camera.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, CameraService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            myService.setCallbacks(null); // unregister
            unbindService(serviceConnection);
            bound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("OnResume========");
        h.postDelayed(runnable,delay);
    }
    @Override
    protected void onPause() {
        super.onPause();
        h.removeCallbacks(runnable);
    }

    @Override
    public void SubscribeTopics(MqttAndroidClient mqttAndroidClient) throws MqttException {

    }

    @Override
    public void MessageArrivedFrom_GW_REG_RES_Topic(JSONObject obj) throws JSONException {

    }

    @Override
    public void MessageArrivedFrom_GW_HB_RES_Topic(JSONObject obj) throws JSONException {

    }

    @Override
    public void MessageArrivedFrom_DEV_NOTIFY_RES_Topic(JSONObject obj) throws JSONException {

    }

    @Override
    public void MessageArrivedFrom_GW_AUTO_RES_Topic(JSONObject obj) throws JSONException {

        System.out.println("Camera Service Message "+obj.toString());
        String dev_type = obj.getString("proto_id");
        String message = obj.getString("message");

        if (progress != null) {
            progress.cancel();
        }
        if (dev_type.equalsIgnoreCase("CAMERA")) {

            ips=ExtractIP(message);

            cameraList=ips.toArray(new String[ips.size()] );
            System.out.println("c"+Arrays.toString(cameraList));
            AlertDialog.Builder builder = new AlertDialog.Builder(Camera.this);
            builder.setTitle("select a camera to stream");
            builder.setItems(cameraList, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        default:
                        {
                            CameraIP=cameraList[which];
                            StreamCamera();
                            dialog.cancel();
                            break;
                        }
                    }
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();


          /*  else
            {

                AlertDialog.Builder alert= new AlertDialog.Builder(Camera.this);
                alert.setMessage("No Camera Found");
                alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                alert.show();

            }*/

        }
        else
        {
            android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(Camera.this);
            alert.setTitle("!!!!");
            alert.setMessage(obj.getString("please try again"));
            alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            alert.show();
        }
    }


    private List<String> ExtractIP(String message) {
        Pattern ptn =
                Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");
        Matcher mtch = ptn.matcher(message);
        ips= new ArrayList<String>();
        while(mtch.find()){
            ips.add(mtch.group());
        }
        return ips;
    }

    private  void StreamCamera() {
        try{
            final AlertDialog.Builder alert = new AlertDialog.Builder(Camera.this);
            LayoutInflater inflater = LayoutInflater.from(Camera.this);
            View dialogView = inflater.inflate(R.layout.camera_select, null);
            alert.setCancelable(false);
            Button addDevice=(Button)dialogView.findViewById(R.id.addDevice);
            final Spinner spinner=(Spinner)dialogView.findViewById(R.id.spinner);
            alert.setView(dialogView);
            final AlertDialog alertDialog = alert.create();
            addDevice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.cancel();
                    startStream(CameraIP,spinner.getSelectedItem().toString());

                }
            });
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
        catch (Exception exp)
        {
            Toast.makeText(getApplicationContext(),"Something went wrong",Toast.LENGTH_SHORT).show();
        }
    }

    private void startStream(String ip, String s) {

        if(s.equalsIgnoreCase("HsmartLink"))
        {
            port=10554;
            VideoURL="rtsp://"+ip+":"+port+"/tcp/av0_0";
        }
        else
        {
            port=554;
            // rtsp://admin:admin@192.168.100.28:554/cam/realmonitor?channel=1&subtype=1
            VideoURL="rtsp://admin:admin@"+ip+":"+port+"/cam/realmonitor?channel=1&subtype=1";
        }

        //  VideoURL="rtsp://"+ip+":"+port+"/tcp/av0_0";
        System.out.println("Video URL===="+VideoURL);
        if(videoview.isAvailable()){
            System.out.println("video view is available");
        }
        try{
            if(VideoURL=="")
            {
                Toast.makeText(Camera.this, "Please edit VideoViewDemo Activity, and set path" + " variable to your media file URL/path", Toast.LENGTH_LONG).show();
                return;
            }
            else
            {
                pDialog = new ProgressDialog(Camera.this);
                pDialog.setTitle("Streaming ");
                pDialog.setMessage("Buffering...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.show();
                onSurfaceTextureAvailable(surface,1080,1680);

            /*try {
                mMediaPlayer = new MediaPlayer();
//progressDialog.dismiss();
                System.out.println("after media player1111");
                String vURL = "rtsp://192.168.43.218:10554/tcp/av0_0";
                System.out.println("On Surface Called========1111"+VideoURL+"===111"+vURL);
                System.out.println("================1111 On Surface"+vURL.equals(VideoURL));
                mMediaPlayer
                        .setDataSource(this, Uri.parse(VideoURL));
                mMediaPlayer.setLooping(true);
                System.out.println("the video url in surface available is.111"+VideoURL);
                mMediaPlayer.prepareAsync();
                // mMediaPlayer.start();
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        System.out.println("MediaPlyer onPrepared ==============111");
                        mediaPlayer.start();
                        pDialog.dismiss();
                        if(port!=554)
                            EnableControl();

                    }
                });
                mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {


                        return false;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
*/
            }}
        catch (Exception exp)
        {
            exp.printStackTrace();
        }
    }

    private void EnableControl() {

        try{
            request_Payload=new JSONObject();
            request_Payload.put("req_type","DEV_CONFIG_REQ");
            request_Payload.put("trans_id",constants.GenerateRandomNumber());
            request_Payload.put("gw_id",sessionManager.getGateway_ID());
            request_Payload.put("proto_id","CAMERA");
            request_Payload.put("cmd_id","");
            request_Payload.put("timestamp",constants.getTime());
            request_Payload.put("user_id",sessionManager.getUser_ID());
            request_Payload.put("dev_type","int_motion");
            request_Payload.put("UserIP",getIP());
            request_Payload.put("CameraIP",CameraIP);
            MqttMessage message = new MqttMessage();
            message.setPayload(request_Payload.toString().getBytes());
            System.out.println("Camera Stream Publishing to=========="+message.toString());
            pahoMqttClient.publishMessage(client, message.toString(), 2, "Spider/"+sessionManager.getGateway_ID()+"/DEV_CONFIG_REQ/"+sessionManager.getGateway_ID());
            Toast.makeText(Camera.this,"Camera Controls Enabled",Toast.LENGTH_SHORT).show();
        }
        catch (Exception exp)
        {
            exp.printStackTrace();
        }
    }


    @Override
    public void MessageArrivedFrom_DEV_REG_RES(JSONObject obj) throws JSONException {

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture1, int i, int i1) {
        System.out.println("On Surface Called========"+surfaceTexture1+"==="+i+"===="+i1);
        surface=surfaceTexture1;
        Surface surface2= new Surface(surfaceTexture1);
        System.out.println("after surface");

        try {
            mMediaPlayer = new MediaPlayer();
//progressDialog.dismiss();
           /* System.out.println("after media player");
            String vURL = "rtsp://192.168.43.218:10554/tcp/av0_0";
            System.out.println("On Surface Called========"+VideoURL+"==="+vURL);
            System.out.println("================On Surface"+vURL.equals(VideoURL));*/

            // String vURL = "rtsp://192.168.43.218:10554/tcp/av0_0";
            mMediaPlayer
                    .setDataSource(this, Uri.parse(VideoURL));
            mMediaPlayer.setSurface(surface2);
            mMediaPlayer.setLooping(true);
            System.out.println("the video url in surface available is."+VideoURL);

// don't forget to call MediaPlayer.prepareAsync() method when you use constructor for
// creating MediaPlayer
            mMediaPlayer.prepareAsync();
            // mMediaPlayer.start();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    System.out.println("MediaPlyer onPrepared ==============");
                    mediaPlayer.start();
                    if(pDialog.isShowing())pDialog.dismiss();
                    if(port!=554)
                        EnableControl();

                }
            });
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    return false;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
