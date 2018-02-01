package com.app.androidkt.mqtt;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import at.markushi.ui.CircleButton;

/**
 * Created by ravikumar on 21/11/17.
 */

public class BLEDetails extends AppCompatActivity implements CallBacks,NetworkStateReceiver.NetworkStateReceiverListener  {

    //ImageView imageView1,imageView2;
    public BLEDetailsService myService;
    public boolean bound = false;
    JSONObject request_Payload;
    Constants constants;
    private RecyclerView recyclerView;
    SessionManager sessionManager;
    ProgressDialog dialog1 ;
    private MqttAndroidClient client;
    private String TAG = "MainActivity";
    private PahoMqttClient pahoMqttClient;
    private NetworkStateReceiver networkStateReceiver;
    Toolbar toolbar;
    Handler h = new Handler();
    int delay = 15000; //15 seconds
    Runnable runnable;
    //CircleImageView switchonoff;
    CircleButton switchonoff,imageView1,imageView2;
    TextView textView_off;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_details);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("BLE Device");
        //imageView1=(ImageView)findViewById(R.id.arrowUp);
       // imageView2=(ImageView)findViewById(R.id.arrowDown);
        imageView1=(CircleButton) findViewById(R.id.imageView1);
        imageView2=(CircleButton) findViewById(R.id.imageView2);
        switchonoff = (CircleButton) findViewById(R.id.switchonoff);
        textView_off = (TextView) findViewById(R.id.textview_off);
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


        switchonoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textView_off.getText().equals("ON")){
                    switchonoff.setImageDrawable(getResources().getDrawable(R.drawable.ic_switchoff));
                    textView_off.setText("OFF");
                    textView_off.setTextColor(getResources().getColor(R.color.colorBlack));
                }else {
                    switchonoff.setImageDrawable(getResources().getDrawable(R.drawable.ic_switchon));
                    textView_off.setText("ON");
                    textView_off.setTextColor(getResources().getColor(R.color.colorGreen));
                }
            }
        });
        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    request_Payload=new JSONObject();
                    request_Payload.put("req_type","DEV_CONFIG_REQ");
                    request_Payload.put("trans_id",constants.GenerateRandomNumber());
                    request_Payload.put("gw_id",sessionManager.getGateway_ID());
                    request_Payload.put("proto_id","BLE");
                    request_Payload.put("cmd_id","UP");
                    request_Payload.put("timestamp",constants.getTime());
                    request_Payload.put("user_id",sessionManager.getUser_ID());
                    request_Payload.put("dev_type","CONFIG");
                    MqttMessage message = new MqttMessage();
                    message.setPayload(request_Payload.toString().getBytes());
                    pahoMqttClient.publishMessage(client, message.toString(), 2, "Spider/"+sessionManager.getGateway_ID()+"/"+"DEV_CONFIG_REQ/");

                    System.out.println("==============BLE"+message.toString());
                    dialog1.setMessage("please wait........");
                    dialog1.show();
                }
                catch (Exception exp)
                {

                }
            }
        });


        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    request_Payload=new JSONObject();
                    request_Payload.put("req_type","DEV_CONFIG_REQ");
                    request_Payload.put("trans_id",constants.GenerateRandomNumber());
                    request_Payload.put("gw_id",sessionManager.getGateway_ID());
                    request_Payload.put("proto_id","BLE");
                    request_Payload.put("cmd_id","DOWN");
                    request_Payload.put("timestamp",constants.getTime());
                    request_Payload.put("user_id",sessionManager.getUser_ID());
                    request_Payload.put("dev_type","CONFIG");
                    MqttMessage message = new MqttMessage();
                    message.setPayload(request_Payload.toString().getBytes());
                    pahoMqttClient.publishMessage(client, message.toString(), 2, "Spider/"+sessionManager.getGateway_ID()+"/"+"DEV_CONFIG_REQ/");
                    System.out.println("==============BLE111 Details"+message.toString());
                    dialog1.setMessage("please wait........");
                    dialog1.show();
                }
                catch (Exception exp)
                {
                    exp.printStackTrace();
                }
            }
        });

        sessionManager=new SessionManager(getApplicationContext());
        constants=new Constants();
        dialog1 = new ProgressDialog(BLEDetails.this);
        pahoMqttClient = new PahoMqttClient();
        client = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());



    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BLEDetailsService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
            BLEDetailsService.LocalBinder binder = (BLEDetailsService.LocalBinder) service;
            myService = binder.getService();
            bound = true;
            myService.setCallbacks(BLEDetails.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    public void SubscribeTopics(MqttAndroidClient mqttAndroidClient) throws MqttException {

    }

    @Override
    public void MessageArrivedFrom_GW_REG_RES_Topic(JSONObject obj) throws JSONException {

        System.out.println("==========BLE Details Service"+obj);
        String str=obj.getString("message");
        if(str.equalsIgnoreCase("UP") || str.equalsIgnoreCase("DOWN"))
        {
            dialog1.cancel();
            MDToast.makeText(BLEDetails.this, str, MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
//            final PrettyDialog pdialog = new PrettyDialog(this);
//            pdialog.show();
//            pdialog
//                    .setTitle(str)
//                    .setMessage("")
//                    .setIcon(R.drawable.spiderluncher,R.color.pdlg_color_blue,null)
//                    .addButton("OK", R.color.pdlg_color_white, R.color.pdlg_color_green, new PrettyDialogCallback() {
//                        @Override
//                        public void onClick() {
//                            pdialog.dismiss();
//                           // Toast.makeText(BLEDetails.this,"OK selected",Toast.LENGTH_SHORT).show();
//                        }
//                    });
//            android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(BLEDetails.this);
//            alert.setMessage(str);
//            alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    dialogInterface.cancel();
//                }
//            });
//            alert.show();
        }
        else
        {
            dialog1.cancel();
            android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(BLEDetails.this);
            alert.setMessage(obj.getString("Something wrong,please try later"));
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
    public void MessageArrivedFrom_GW_HB_RES_Topic(JSONObject obj) throws JSONException {

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
    public void networkAvailable() {
        sessionManager.setOffline("true");
        System.out.println("================Network Available BLEDetails Activty"+sessionManager.getOffline());
    }

    @Override
    public void networkUnavailable() {

        System.out.println("================Network Available BLEDetails Activty"+sessionManager.getOffline());
        if(sessionManager.getOffline().contains("true")){
            LayoutInflater li = LayoutInflater.from(BLEDetails.this);
            View dialogView = li.inflate(R.layout.connection_lost_dialog, null);
            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(
                    BLEDetails.this);
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
                                        client = pahoMqttClient.getMqttClient(BLEDetails.this, Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
                                        sessionManager.setOffline("false");
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
            sessionManager.setOffline("false");
        }
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
