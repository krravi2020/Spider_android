package com.app.androidkt.mqtt;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.skyfishjy.library.RippleBackground;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HeartBeat extends AppCompatActivity implements CallBacks,NetworkStateReceiver.NetworkStateReceiverListener {

    private HeartBeatService myService;
    private boolean bound = false;
    Toolbar toolbar;
    Button ping;
    JSONObject request_Payload;
    Constants constants;
    SessionManager sessionManager;
    private NetworkStateReceiver networkStateReceiver;
    private MqttAndroidClient client;
    private String TAG = "MainActivity";
    private PahoMqttClient pahoMqttClient;
    ProgressDialog dialog1 ;
    Handler h = new Handler();
    int delay = 15000; //15 seconds
    Runnable runnable;
    static int flag;
    private RecyclerView heartbeatmessagelist;
    private HeartBeatMessageAdapter heartbeat_adapter;
    private List<Gatewaypojo> gatewaypojos = new ArrayList<>();
    String mqttdate1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_beat);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Heart Beat");
        initvalue();
        sessionManager=new SessionManager(HeartBeat.this);
        constants=new Constants();
        dialog1 = new ProgressDialog(HeartBeat.this);
        pahoMqttClient = new PahoMqttClient();
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        h.postDelayed(new Runnable() {
            public void run() {
                sendBroadcast(new Intent("ABCD"));
                runnable=this;
                h.postDelayed(runnable, delay);
            }
        }, delay);

        System.out.println("==============HB OnCreate"+Constants.MQTT_BROKER_URL);
        client = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
        Intent intent = new Intent(HeartBeat.this, HeartBeatService.class);
        startService(intent);

    }

    private void initvalue() {
        heartbeatmessagelist = (RecyclerView) findViewById(R.id.gatewaydetailsrecyclerview);
        heartbeat_adapter = new HeartBeatMessageAdapter(gatewaypojos);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        heartbeatmessagelist.setLayoutManager(mLayoutManager);
        heartbeatmessagelist.setItemAnimator(new DefaultItemAnimator());
        heartbeatmessagelist.setAdapter(heartbeat_adapter);
        heartbeat_adapter.notifyDataSetChanged();
        //prepareMovieData();

        ping = (Button) findViewById(R.id.ping);
        ping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pingClickHandler();
            }
        });
    }
    public void callHeartBeatMessage(){
        request_Payload = new JSONObject();
        try {
            request_Payload=new JSONObject();
            request_Payload.put("req_type","GW_HB_REQ");
            request_Payload.put("trans_id",constants.GenerateRandomNumber());
            request_Payload.put("gw_id","12345");
            request_Payload.put("message","ping");
            request_Payload.put("timestamp",new Date());

            MqttMessage message = new MqttMessage();
            message.setPayload(request_Payload.toString().getBytes());
            try {
                pahoMqttClient.publishMessage(client,message.toString(),2,"Spider/"+sessionManager.getGateway_ID());
            } catch (MqttException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void prepareMovieData() {
        Gatewaypojo movie = new Gatewaypojo("Mad Max: Fury Road", "Action & Adventure", "abc","abc","abc");
        gatewaypojos.add(movie);

        movie = new Gatewaypojo("Mad Max: Fury Road", "Action & Adventure", "abc","abc","abc");
        gatewaypojos.add(movie);
    }
//    public  void pingClickHandler(View target)
      public  void pingClickHandler()
      {
        try{
            request_Payload=new JSONObject();
            request_Payload.put("req_type","GW_HB_REQ");
            request_Payload.put("trans_id",constants.GenerateRandomNumber());
            request_Payload.put("gw_id","12345");
            request_Payload.put("message","ping");
            request_Payload.put("timestamp",new Date());
            System.out.println("heartbeat---"+request_Payload);
            MqttMessage message = new MqttMessage();
            message.setPayload(request_Payload.toString().getBytes());
            pahoMqttClient.publishMessage(client, message.toString(), 2, "Spider/"+sessionManager.getGateway_ID());
            dialog1.setMessage("pinging........");
            dialog1.show();
        }
        catch (Exception exp)
        {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        h.postDelayed(runnable,delay);
        System.out.println("==============HB OnResume"+Constants.MQTT_BROKER_URL);
        flag=0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        h.removeCallbacks(runnable);
        System.out.println("==============HB OnPause"+Constants.MQTT_BROKER_URL);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //startActivity(new Intent(this,HomePage.class));
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
            HeartBeatService.LocalBinder binder = (HeartBeatService.LocalBinder) service;
            myService = binder.getService();
            bound = true;
            myService.setCallbacks(HeartBeat.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, HeartBeatService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        System.out.println("==============HB OnStart"+Constants.MQTT_BROKER_URL);
        flag=0;
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("==============HB OnStop"+Constants.MQTT_BROKER_URL);
        if (bound) {
            myService.setCallbacks(null); // unregister
            unbindService(serviceConnection);
            bound = false;
        }
    }

    @Override
    public void SubscribeTopics(MqttAndroidClient mqttAndroidClient) throws MqttException {

    }

    @Override
    public void MessageArrivedFrom_GW_REG_RES_Topic(JSONObject obj) throws JSONException {

    }

    @Override
    public void MessageArrivedFrom_GW_HB_RES_Topic(JSONObject obj) throws JSONException {

        dialog1.dismiss();
        System.out.println("Heart Beat Activity ================"+obj);
        if(obj.has("req_type") && obj.get("req_type").toString().contains("GW_HB_RES"))
        {
            if(obj.has("message") )
            {
                if(flag==0){
                AlertDialog.Builder alert = new AlertDialog.Builder(HeartBeat.this);
                alert.setTitle("Success");
                alert.setMessage(obj.getString("message"));
                String mqttdate = obj.getString("timeStamp");
                Date date = new Date(mqttdate);
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                String strDate= formatter.format(date);
                System.out.println(strDate);
                Gatewaypojo movie = new Gatewaypojo();
                movie.setGatewayid(obj.getString("gw_id"));
                movie.setMessage(obj.getString("message"));
                movie.setTransid(obj.getString("transId"));
                movie.setTimestamp(strDate);
                System.out.println("movie----"+movie);
                gatewaypojos.add(movie);
                heartbeat_adapter.notifyDataSetChanged();
                alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                     dialogInterface.dismiss();
                        flag=0;
                    }
                });
                alert.setCancelable(false);
                alert.show();
                flag=1;}
            }
            else
            {
                AlertDialog.Builder alert = new AlertDialog.Builder(HeartBeat.this);
                alert.setTitle("Failed1");
                alert.setMessage("something went wrong ,try again later");
                alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alert.setCancelable(false);
                alert.show();
            }
        }
        else
        {
            AlertDialog.Builder alert = new AlertDialog.Builder(HeartBeat.this);
            alert.setTitle("Failed2");
            alert.setMessage("something went wrong ,try again later");
            alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alert.setCancelable(false);
            alert.show();
        }

    }

    @Override
    public void MessageArrivedFrom_DEV_NOTIFY_RES_Topic(JSONObject obj) throws JSONException {

    }

    @Override
    public void MessageArrivedFrom_GW_AUTO_RES_Topic(JSONObject obj) throws JSONException {

    }

    @Override
    public void MessageArrivedFrom_DEV_REG_RES(JSONObject obj) throws JSONException {
        System.out.println("came here in HB");
    }

    @Override
    public void networkAvailable() {
        sessionManager.setOffline("true");
        System.out.println("================Network Available HB Activty"+sessionManager.getOffline());
    }

    @Override
    public void networkUnavailable() {

        System.out.println("================Network UnAvailable HB Activty"+sessionManager.getOffline());
        if(sessionManager.getOffline().equals("true")){
            LayoutInflater li = LayoutInflater.from(HeartBeat.this);
            View dialogView = li.inflate(R.layout.connection_lost_dialog, null);
            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(
                    HeartBeat.this);
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
                                        client = pahoMqttClient.getMqttClient(HeartBeat.this, Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
                                        sessionManager.setOffline("false");
                                        Toast.makeText(HeartBeat.this,"Connected to Local IP",Toast.LENGTH_SHORT).show();
                                    }
                                    catch (Exception exp)
                                    {
                                        exp.printStackTrace();
                                    }

                                }
                            });

            android.app.AlertDialog alertDialog = alertDialogBuilder.create();
            // show it
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
            sessionManager.setOffline("false");
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
        System.out.println("==============HB OnDestroy"+Constants.MQTT_BROKER_URL);
    }


}
