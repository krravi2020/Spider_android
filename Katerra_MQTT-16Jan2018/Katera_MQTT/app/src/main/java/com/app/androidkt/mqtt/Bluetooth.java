package com.app.androidkt.mqtt;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import co.mobiwise.library.InteractivePlayerView;
import co.mobiwise.library.OnActionClickedListener;

/**
 * Created by ravikumar on 23/10/17.
 */

public class Bluetooth  extends AppCompatActivity implements OnActionClickedListener,CallBacks,NetworkStateReceiver.NetworkStateReceiverListener  {

    Toolbar toolbar;
    TextView musicTitle,musicArtistName;
    RelativeLayout songPlayerTopLayout,latestAddedSongs,relativeLayout3,startPlay,stopPlay;
    SwitchCompat switch_compat;
    InteractivePlayerView mInteractivePlayerView;
    ImageView imageView;
    private BluetoothService myService;
    private boolean bound = false;
    JSONObject request_Payload;
    Constants constants;
    SessionManager sessionManager;
    ProgressDialog dialog1 ;
    private MqttAndroidClient client;
    private String TAG = "MainActivity";
    private PahoMqttClient pahoMqttClient;
    private NetworkStateReceiver networkStateReceiver;
    Handler h = new Handler();
    int delay = 15000; //15 seconds
    Runnable runnable;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Bluetooth");


        FindViews();
        sessionManager=new SessionManager(Bluetooth.this);
        constants=new Constants();
        dialog1 = new ProgressDialog(Bluetooth.this);
        pahoMqttClient = new PahoMqttClient();
        client = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
        Intent intent = new Intent(Bluetooth.this, BluetoothService.class);
        startService(intent);
        if(sessionManager.getIsEnabled().equalsIgnoreCase("true")) {

            switch_compat.setChecked(true);
            System.out.println("came inside");
        }
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


        switch_compat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position

                if(isChecked==true)
                {
                    showViews();
                    sessionManager.setIsEnabled("true");
                }
                else
                {
                    hideViews();
                    sessionManager.setIsEnabled("false");
                }
            }
        });


        mInteractivePlayerView.setMax(114);
        mInteractivePlayerView.setProgress(0);
        mInteractivePlayerView.setOnActionClickedListener(this);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mInteractivePlayerView.isPlaying()) {
                    pausePlay();
                   /* mInteractivePlayerView.start();
                    imageView.setBackgroundResource(R.drawable.ic_action_pause);*/
                } else {
                    pausePlay();
                    /*mInteractivePlayerView.stop();
                    imageView.setBackgroundResource(R.drawable.ic_action_play);*/
                }
            }
        });

        stopPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlay();
            }
        });

        startPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPlay();
            }
        });
    }

    private void startPlay() {
        try{
            request_Payload=new JSONObject();
            request_Payload.put("req_type","DEV_CONFIG_REQ");
            request_Payload.put("trans_id",constants.GenerateRandomNumber());
            request_Payload.put("gw_id",sessionManager.getGateway_ID());
            request_Payload.put("proto_id","BLUETOOH");
            request_Payload.put("cmd_id","PLAY");
            request_Payload.put("timestamp",constants.getTime());
            request_Payload.put("user_id",sessionManager.getUser_ID());
            request_Payload.put("dev_type","CONFIG");
            MqttMessage message = new MqttMessage();
            message.setPayload(request_Payload.toString().getBytes());
            pahoMqttClient.publishMessage(client, message.toString(), 2, "DEV_CONFIG_REQ/"+sessionManager.getGateway_ID());
            dialog1.setMessage("starting , please wait........");
            dialog1.show();
        }
        catch (Exception exp)
        {

        }
    }

    private void pausePlay() {

        try{
            request_Payload=new JSONObject();
            request_Payload.put("req_type","DEV_CONFIG_REQ");
            request_Payload.put("trans_id",constants.GenerateRandomNumber());
            request_Payload.put("gw_id",sessionManager.getGateway_ID());
            request_Payload.put("proto_id","BLUETOOH");
            request_Payload.put("cmd_id","PAUSE");
            request_Payload.put("timestamp",constants.getTime());
            request_Payload.put("user_id",sessionManager.getUser_ID());
            request_Payload.put("dev_type","CONFIG");
            MqttMessage message = new MqttMessage();
            message.setPayload(request_Payload.toString().getBytes());
            pahoMqttClient.publishMessage(client, message.toString(), 2, "DEV_CONFIG_REQ/"+sessionManager.getGateway_ID());
            dialog1.setMessage("pausing , please wait........");
            dialog1.show();
        }
        catch (Exception exp)
        {

        }
    }

    private void stopPlay() {

        try{
            request_Payload=new JSONObject();
            request_Payload.put("req_type","DEV_CONFIG_REQ");
            request_Payload.put("trans_id",constants.GenerateRandomNumber());
            request_Payload.put("gw_id",sessionManager.getGateway_ID());
            request_Payload.put("proto_id","BLUETOOH");
            request_Payload.put("cmd_id","STOP");
            request_Payload.put("timestamp",constants.getTime());
            request_Payload.put("user_id",sessionManager.getUser_ID());
            request_Payload.put("dev_type","CONFIG");
            MqttMessage message = new MqttMessage();
            message.setPayload(request_Payload.toString().getBytes());
            pahoMqttClient.publishMessage(client, message.toString(), 2, "DEV_CONFIG_REQ/"+sessionManager.getGateway_ID());
            dialog1.setMessage("stoping , please wait........");
            dialog1.show();
        }
        catch (Exception exp)
        {

        }

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            myService = binder.getService();
            bound = true;
            myService.setCallbacks(Bluetooth.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };


    private void showLayout()
    {
        musicTitle.setVisibility(View.VISIBLE);
        musicArtistName.setVisibility(View.VISIBLE);
        songPlayerTopLayout.setVisibility(View.VISIBLE);
        latestAddedSongs.setVisibility(View.VISIBLE);
        relativeLayout3.setVisibility(View.VISIBLE);
        startPlay.setVisibility(View.VISIBLE);
        stopPlay.setVisibility(View.VISIBLE);
    }

    private void showViews()
    {

        try{
            request_Payload=new JSONObject();
            request_Payload.put("req_type","DEV_CONFIG_REQ");
            request_Payload.put("trans_id",constants.GenerateRandomNumber());
            request_Payload.put("gw_id",sessionManager.getGateway_ID());
            request_Payload.put("proto_id","BLUETOOH");
            request_Payload.put("cmd_id","ENABLE");
            request_Payload.put("timestamp",constants.getTime());
            request_Payload.put("user_id",sessionManager.getUser_ID());
            request_Payload.put("dev_type","AUDIO");
            MqttMessage message = new MqttMessage();
            message.setPayload(request_Payload.toString().getBytes());
            pahoMqttClient.publishMessage(client, message.toString(), 2, "DEV_CONFIG_REQ/"+sessionManager.getGateway_ID());
            dialog1.setMessage("please wait........");
            dialog1.show();
        }
        catch (Exception exp)
        {

        }

    }
    private void hideViews()
    {
        musicTitle.setVisibility(View.GONE);
        musicArtistName.setVisibility(View.GONE);
        songPlayerTopLayout.setVisibility(View.GONE);
        latestAddedSongs.setVisibility(View.GONE);
        relativeLayout3.setVisibility(View.GONE);
        if (mInteractivePlayerView.isPlaying()) {
            mInteractivePlayerView.stop();
            imageView.setBackgroundResource(R.drawable.ic_action_play);
        }
    }

    private void FindViews() {
        musicTitle=(TextView)findViewById(R.id.musicTitle);
        musicArtistName=(TextView)findViewById(R.id.musicArtistName);
        songPlayerTopLayout=(RelativeLayout) findViewById(R.id.songPlayerTopLayout);
        latestAddedSongs=(RelativeLayout) findViewById(R.id.latestAddedSongs);
        relativeLayout3=(RelativeLayout) findViewById(R.id.relativeLayout3);
        switch_compat=(SwitchCompat)findViewById(R.id.Switch);
        mInteractivePlayerView= (InteractivePlayerView) findViewById(R.id.interactivePlayerView);
        imageView = (ImageView) findViewById(R.id.control);
        startPlay=(RelativeLayout)findViewById(R.id.stratPlay);
        stopPlay=(RelativeLayout)findViewById(R.id.stopPlay);
    }

    @Override
    public void onActionClicked(int id) {
        switch (id) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            default:
                break;
        }
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

    @Override
    public void SubscribeTopics(MqttAndroidClient mqttAndroidClient) throws MqttException {

    }

    @Override
    public void MessageArrivedFrom_GW_REG_RES_Topic(JSONObject obj) throws JSONException {

    }

    @Override
    public void MessageArrivedFrom_GW_HB_RES_Topic(JSONObject obj) throws JSONException {

        System.out.print(obj + "Blutooth Activity============================");
        dialog1.cancel();
        if (obj.has("req_type") && obj.get("req_type").toString().contains("DEV_CONFIG_RES"))
        {
            if(obj.has("message") && obj.getString("message").equalsIgnoreCase("AUDIO ENABLE"))
            {
                android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(Bluetooth.this);
                alert.setTitle("Enabled");
                alert.setMessage(obj.getString("message"));
                alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        showLayout();
                    }
                });
                alert.show();
            }
            if(obj.has("message") && obj.getString("message").equalsIgnoreCase("CONFIG PAUSE"))
            {
                if(mInteractivePlayerView.isPlaying())
                {
                    mInteractivePlayerView.stop();
                    imageView.setImageResource(android.R.color.transparent);
                    imageView.setBackgroundResource(R.drawable.ic_action_play);
                }
                else
                {
                    mInteractivePlayerView.start();
                    imageView.setImageResource(android.R.color.transparent);
                    imageView.setBackgroundResource(R.drawable.ic_action_pause);
                }

            }
            if(obj.has("message") && obj.getString("message").equalsIgnoreCase("CONFIG PLAY"))
            {
                Toast.makeText(getApplicationContext(),"Played",Toast.LENGTH_SHORT).show();
                if(!mInteractivePlayerView.isPlaying()) {
                    mInteractivePlayerView.setProgress(0);
                    mInteractivePlayerView.start();
                }

            }
            if(obj.has("message") && obj.getString("message").equalsIgnoreCase("CONFIG STOP"))
            {
                Toast.makeText(getApplicationContext(),"Stopped",Toast.LENGTH_SHORT).show();
                if(mInteractivePlayerView.isPlaying()) {
                    mInteractivePlayerView.setProgress(0);
                    mInteractivePlayerView.stop();
                }
            }
        }
      /*  else
        {
            hideViews();
        }*/

    }

    @Override
    public void MessageArrivedFrom_DEV_NOTIFY_RES_Topic(JSONObject obj) throws JSONException {

    }

    @Override
    public void MessageArrivedFrom_GW_AUTO_RES_Topic(JSONObject obj) throws JSONException {

    }

    @Override
    public void MessageArrivedFrom_DEV_REG_RES(JSONObject obj) throws JSONException {

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BluetoothService.class);
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
    public void networkAvailable() {

        sessionManager.setOffline("true");
        System.out.println("================Network Available Bluetooth Activty"+sessionManager.getOffline());
    }

    @Override
    public void networkUnavailable() {

        System.out.println("================Network UnAvailable Bluetooth Activty"+sessionManager.getOffline());
        if(sessionManager.getOffline().contains("true")){
            LayoutInflater li = LayoutInflater.from(Bluetooth.this);
            View dialogView = li.inflate(R.layout.connection_lost_dialog, null);
            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(
                    Bluetooth.this);
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
                                        client = pahoMqttClient.getMqttClient(Bluetooth.this, Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
                                        sessionManager.setOffline("false");
                                        Toast.makeText(Bluetooth.this,"Connected to Local IP",Toast.LENGTH_SHORT).show();
                                    }
                                    catch (Exception exp)
                                    {
                                        exp.printStackTrace();
                                    }

                                }
                            });

            android.app.AlertDialog alertDialog = alertDialogBuilder.create();
            // show it
            alertDialog.show();
            sessionManager.setOffline("false");}
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);

    }

    @Override
    protected void onPause() {
        super.onPause();
        h.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        h.postDelayed(runnable,delay);



    }

}
