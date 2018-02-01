package com.app.androidkt.mqtt;


import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.telephony.TelephonyManager;
import android.transition.Explode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.skyfishjy.library.RippleBackground;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.ExceptionHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;



public class MainActivity extends AppCompatActivity implements  CallBacks ,NetworkStateReceiver.NetworkStateReceiverListener,View.OnClickListener {

    private MqttMessageService myService;
    private boolean bound = false;
    ProgressBar pb;

    private MqttAndroidClient client;
    private String TAG = "MainActivity";
    private PahoMqttClient pahoMqttClient;
    ProgressDialog dialog ,dialog1;

    EditText etUsername,etPassword;
    Button btGo,fab;
    /*EditText etUsername;
    @InjectView(R.id.et_password)
    EditText etPassword;
    @InjectView(R.id.bt_go)
    Button btGo;
    @InjectView(R.id.cv)
    CardView cv;
    @InjectView(R.id.fab)
    FloatingActionButton fab;*/
    JSONObject request_Payload;
    SessionManager sessionManager;
    ActivityOptionsCompat oc2;
    MqttMessage message;
    Intent i2;
    RelativeLayout relativeLayout1;
    String IMEI;
    Constants constants;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 345;
    private NetworkStateReceiver networkStateReceiver;
    static int flag=1;
    Handler h = new Handler();
    int delay = 15000; //15 seconds
    Runnable runnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        relativeLayout1=(RelativeLayout)findViewById(R.id.layout1);
        sessionManager=new SessionManager(this);
        sessionManager.setOffline("false");
        System.out.println("=================Broker"+Constants.MQTT_BROKER_URL);
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver,new IntentFilter("ABCD"));

        etUsername=(EditText)findViewById(R.id.et_username);
        etPassword=(EditText)findViewById(R.id.et_password);
        btGo=(Button)findViewById(R.id.bt_go);
        fab=(Button)findViewById(R.id.fab);

        btGo.setOnClickListener(this);
        fab.setOnClickListener(this);


        h.postDelayed(new Runnable() {
            public void run() {
                sendBroadcast(new Intent("ABCD"));
                runnable=this;
                h.postDelayed(runnable, delay);
            }
        }, delay);

       // rippleBackground=(RippleBackground)findViewById(R.id.content);

       /* new FetchIPDetails().execute("192.168.100");*/
        if (Build.VERSION.SDK_INT >= 23){
            boolean hasPermission = (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {
                ActivityCompat.requestPermissions((Activity) MainActivity.this,
                        new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }else {
                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                IMEI=telephonyManager.getDeviceId();
                sessionManager.setIMEI(IMEI);
            }
        }else {
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            IMEI=telephonyManager.getDeviceId();
            sessionManager.setIMEI(IMEI);
        }


        Intent intent = new Intent(MainActivity.this, MqttMessageService.class);
        startService(intent);

//        Intent intent1=new Intent(this,HomePage.class);
//        startActivity(intent1);


        pahoMqttClient = new PahoMqttClient();
        etUsername=(EditText)findViewById(R.id.et_username);

        sessionManager.setIMEI(IMEI);
        client = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());


/*
        if(Constants.MQTT_BROKER_URL.contains("142"))
        {new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkServerStatus();
            }
        }, 5000);}
        else
        {
            relativeLayout1.setVisibility(View.VISIBLE);
        }
*/


    }

    /*private void checkServerStatus() {
        try {
            request_Payload=new JSONObject();
            request_Payload.put("client_id",sessionManager.getIMEI());
            request_Payload.put("message","get_status");
            message= new MqttMessage();
            message.setPayload(request_Payload.toString().getBytes());
            pahoMqttClient.publishMessage(client, message.toString(), 2, "Server_Status");
            System.out.println("Checking Server status=="+message.toString());
            dialog1=new ProgressDialog(MainActivity.this);
            dialog1.show();


            final Timer t = new Timer();
            t.schedule(new TimerTask() {
                public void run() {
                    if(dialog1.isShowing())
                    {
                        dialog1.dismiss();
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder alert1= new AlertDialog.Builder(MainActivity.this);
                                alert1.setMessage("No Internet Connectivty ");
                                alert1.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                        relativeLayout1.setVisibility(View.VISIBLE);
                                    }
                                });
                                alert1.show();
                            }
                        });
                    }

            t.cancel();
                    }



            }, 10000);

        }
        catch (Exception exp)
        {
            exp.printStackTrace();
        }
    }*/
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
              /*  getWindow().setExitTransition(null);
                getWindow().setEnterTransition(null);
*/
                startActivity(new Intent(this, RegisterActivity.class));
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options =
                            ActivityOptions.makeSceneTransitionAnimation(this, fab, fab.getTransitionName());
                    startActivity(new Intent(this, RegisterActivity.class), options.toBundle());
                } else {
                    startActivity(new Intent(this, RegisterActivity.class));
                }*/
                break;
            case R.id.bt_go:
                /*Explode explode = new Explode();
                explode.setDuration(500);

                getWindow().setExitTransition(explode);
                getWindow().setEnterTransition(explode);*/



                if(!etPassword.getText().toString().isEmpty() && !etUsername.getText().toString().isEmpty())
                {
                    try{

                       request_Payload=new JSONObject();
                       request_Payload.put("req_type","GW_LOGIN_REQ");
                       request_Payload.put("trans_id",new Constants().GenerateRandomNumber());
                       request_Payload.put("gw_id",etPassword.getText());
                       request_Payload.put("user_id",etUsername.getText());
                       request_Payload.put("err_id","0");
                       request_Payload.put("cmd_id","ADD");
                       request_Payload.put("message","Valid");

                   }

                    catch(JSONException exp)
                    {
                        exp.printStackTrace();
                    }

                    String msg = etPassword.getText().toString().trim();
                    if (!msg.isEmpty()) {
                        try {
                            message= new MqttMessage();
                            message.setPayload(request_Payload.toString().getBytes());
                            Log.d("Main Acti.Login Req",request_Payload.toString());
                            String str="Spider/";
                          //  Toast.makeText(this,"Publishing to"+str,Toast.LENGTH_SHORT).show();
                            pahoMqttClient.publishMessage(client, message.toString(), 2, "Spider/12345/GW_LOGIN_REQ");
                            dialog=new ProgressDialog(MainActivity.this);
                           // rippleBackground.startRippleAnimation();
                            dialog.setMessage("please wait");
                            dialog.show();
                        } catch (MqttException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }

                }

                else
                    Toast.makeText(getApplicationContext(),"Feilds Can't be Blank",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void SubscribeTopics(MqttAndroidClient mqttAndroidClient) throws MqttException {


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        h.removeCallbacks(runnable);
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
    protected void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, MqttMessageService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        System.out.println("OnResume========");
       h.postDelayed(runnable,delay);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            MqttMessageService.LocalBinder binder = (MqttMessageService.LocalBinder) service;
            myService = binder.getService();
            bound = true;
            myService.setCallbacks(MainActivity.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    public void MessageArrivedFrom_GW_REG_RES_Topic(JSONObject obj) throws JSONException {

        if(dialog1.isShowing())
        {
            dialog1.dismiss();
            relativeLayout1.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void MessageArrivedFrom_GW_HB_RES_Topic(JSONObject obj) throws JSONException {

    }

    @Override
    public void MessageArrivedFrom_DEV_NOTIFY_RES_Topic(JSONObject obj) throws JSONException {
        /*System.out.println("Main Activity111======"+obj.getString("message"));
        Constants.MQTT_BROKER_URL=obj.getString("message");
*/
    }

    @Override
    public void MessageArrivedFrom_GW_AUTO_RES_Topic(JSONObject obj) throws JSONException {


        System.out.println("Main Activity ================"+obj+"========="+obj.getString("message"));
        String message=obj.getString("message");
        AlertDialog.Builder alert= new AlertDialog.Builder(this);
        Log.d("Main Act.Resp Login",obj.toString());
        if(obj.has("req_type") && obj.get("req_type").toString().contains("GW_AUTO_RES"))
        {
            if(message.contains("Existing USER"))
            {
                /*oc2= ActivityOptionsCompat.makeSceneTransitionAnimation(this);
                i2 = new Intent(this,HomePage.class);*/
                dialog.dismiss();
                //rippleBackground.startRippleAnimation();
                sessionManager.setGateway_ID(etPassword.getText().toString());
                sessionManager.setUser_ID(etUsername.getText().toString());
                i2 = new Intent(this,HomePage.class);
                startActivity(i2);


            }
            else
            {
                String ip=obj.getString("local_ip");
                if(message.contains("Gateway is Offline")) {
                    Constants.MQTT_BROKER_URL = "tcp://" + ip + ":1883";
                    System.out.println("=========Broker URL" + Constants.MQTT_BROKER_URL);
                    try {
                        dialog.dismiss();
                        final AlertDialog.Builder alert1 = new AlertDialog.Builder(MainActivity.this);
                        alert1.setMessage("Please change your wifi to local ,then press ok");
                        alert1.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();

                                try {
                                    pahoMqttClient.disconnect(client);
                                    client = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
                                } catch (Exception exp) {
                                    exp.printStackTrace();
                                }

                            }
                        });
                        alert1.show();

                    } catch (Exception exp) {
                        exp.printStackTrace();
                    }
                }

            else
            {
                dialog.dismiss();
                alert.setTitle("Failed");
                alert.setMessage(obj.getString("message"));
                alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                alert.show();
            }
            }
        }
        else
        {
            alert.setTitle("Failed");
            alert.setMessage(obj.getString("Something went wrong,please try again"));
            alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            alert.show();
        }
    }


    @Override
    public void MessageArrivedFrom_DEV_REG_RES(JSONObject obj) throws JSONException {

    }

    @Override
    public void networkAvailable() {

        System.out.println("================Network Available MainActivty"+sessionManager.getOffline());
        sessionManager.setOffline("true");
    }

    @Override
    public void networkUnavailable() {

        System.out.println("==========Network UnAvailable  Main Activity"+sessionManager.getOffline() + flag);
        if(flag==1 ) {
            LayoutInflater li = LayoutInflater.from(MainActivity.this);
            View dialogView = li.inflate(R.layout.connection_lost_dialog, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    MainActivity.this);
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
                                    // get user input and set it to etOutput
                                    // edit text

                                    dialog.cancel();
                                    try {
                                        Constants.MQTT_BROKER_URL = "tcp://" + userInput.getText() + ":1883";
                                        pahoMqttClient.disconnect(client);
                                        client = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
                                        sessionManager.setOffline("false");
                                        MainActivity.this.onResume();
                                        Toast.makeText(MainActivity.this,"Connected to Local IP",Toast.LENGTH_SHORT).show();
                                        flag++;
                                        System.out.println("====================MainActivity Offline" + sessionManager.getOffline());
                                    } catch (Exception exp) {
                                        exp.printStackTrace();
                                    }

                                }
                            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            flag++;
            // show it}
        }

    }
}
