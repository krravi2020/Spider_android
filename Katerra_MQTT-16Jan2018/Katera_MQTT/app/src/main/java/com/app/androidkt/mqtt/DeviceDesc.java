package com.app.androidkt.mqtt;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ravikumar on 10/10/17.
 */

public class DeviceDesc extends AppCompatActivity implements CallBacks ,NetworkStateReceiver.NetworkStateReceiverListener
{

    Bundle b;
    SessionManager sessionManager;
    Device d;
    JSONObject object;
    public MqttAndroidClient client;
    public PahoMqttClient pahoMqttClient;
    Constants constants;
    public TextView deviceName1,deviceType1,tempretureValue,humidityValue,burglarValue,LuminanceValue,currentStatus;
    ImageView device_icon1;
    LinearLayout layout1,layout2;
    Toolbar toolbar;
    Dialog dialog;
    ProgressDialog dialog1 ;
    JSONObject request_Payload;
    ImageButton settings;
    private DeviceManagementService myService;
    private boolean bound = false;
    TextInputEditText t1,t2;
    private NetworkStateReceiver networkStateReceiver;
    Handler h = new Handler();
    int delay = 15000; //15 seconds
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_details);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Device Details");
        sessionManager=new SessionManager(DeviceDesc.this);
        constants=new Constants();
        b=getIntent().getExtras();
        dialog1=new ProgressDialog(DeviceDesc.this);
        deviceType1 = (TextView)findViewById(R.id.deviceType1);
        settings=(ImageButton)findViewById(R.id.settings);
        deviceName1=(TextView)findViewById(R.id.deviceName1);
        device_icon1=(ImageView)findViewById(R.id.device_icon1);
        tempretureValue=(TextView)findViewById(R.id.tempretureValue);
        humidityValue=(TextView)findViewById(R.id.humidityValue);
        burglarValue=(TextView)findViewById(R.id.burglarValue);
        LuminanceValue=(TextView)findViewById(R.id.LuminanceValue);
        currentStatus=(TextView)findViewById(R.id.currentStatus);
        layout1=(LinearLayout)findViewById(R.id.layout1);
        layout2=(LinearLayout)findViewById(R.id.layout2);
        pahoMqttClient = new PahoMqttClient();
        client = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());

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


        try {
            object=new JSONObject(b.getString("json"));
            String type=object.getString("typeOfDevice");
            if(type.equalsIgnoreCase("Water"))
            {
                device_icon1.setImageResource(android.R.color.transparent);
                device_icon1.setImageResource(R.drawable.waterbanner);
                deviceType1.setText(object.getString("deviceName"));
                deviceName1.setText(object.getString("device_type"));
                currentStatus.setVisibility(View.VISIBLE);
                currentStatus.setText(object.getString("status"));
                if(currentStatus.getText().toString().equalsIgnoreCase("Leak"))
                        currentStatus.setTextColor(Color.GREEN);
                else
                        currentStatus.setTextColor(Color.BLACK);

            }
           if(type.equalsIgnoreCase("MultiSensor"))
            {
                device_icon1.setImageResource(android.R.color.transparent);
                device_icon1.setImageResource(R.drawable.multi);
                tempretureValue.setText(object.getString("tempretureValue")+"F");
                humidityValue.setText(object.getString("humidityValue"));
                burglarValue.setText(object.getString("burglarValue"));
                LuminanceValue.setText(object.getString("LuminanceValue"));
                layout1.setVisibility(View.VISIBLE);
                layout2.setVisibility(View.VISIBLE);



            }
           if(type.equalsIgnoreCase("Switch"))
            {
                device_icon1.setImageResource(android.R.color.transparent);
                device_icon1.setImageResource(R.drawable.bell);
                currentStatus.setVisibility(View.VISIBLE);
                currentStatus.setText(object.getString("status"));


            }
           if(type.equalsIgnoreCase("DoorBell"))
            {
                device_icon1.setImageResource(android.R.color.transparent);
                device_icon1.setImageResource(R.drawable.close);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog = new Dialog(view.getContext());
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.miultisensor);
                Button go=(Button)dialog.findViewById(R.id.bt_go1);
                t1=(TextInputEditText)dialog.findViewById(R.id.number);
                t2=(TextInputEditText)dialog.findViewById(R.id.value);



                go.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try{

                            request_Payload=new JSONObject();
                            request_Payload.put("req_type","DEV_PARAM_REQ");
                            request_Payload.put("trans_id",constants.GenerateRandomNumber());
                            request_Payload.put("gw_id",sessionManager.getGateway_ID());
                            request_Payload.put("proto_id","Zwave");
                            request_Payload.put("cmd_id","SET_PARAM");
                            request_Payload.put("timestamp",constants.getTime());
                            request_Payload.put("user_id",sessionManager.getUser_ID());
                            request_Payload.put("device_id",object.getString("device_id"));
                            request_Payload.put("param",t1.getText()+"");
                            request_Payload.put("value",t2.getText()+"");
                            MqttMessage message = new MqttMessage();
                            message.setPayload(request_Payload.toString().getBytes());
                            System.out.println("Called APi====");
                            pahoMqttClient.publishMessage(client, message.toString(), 2, "DEV_PARAM_REQ/"+sessionManager.getGateway_ID());
                            System.out.println("Published to====="+message.toString());
                            dialog.dismiss();
                            dialog1.setMessage("please wait.....");
                            dialog1.show();


                        }
                        catch (Exception exp)
                        {

                        }
                    }
                });
                dialog.show();

            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        System.out.println("OnResume========");

        h.postDelayed(runnable,delay);
    }
    @Override
    protected void onPause() {
        super.onPause();
        h.removeCallbacks(runnable);
    }
    @Override
    public void networkAvailable() {
        sessionManager.setOffline("true");
        System.out.println("================Network Available DeviceDesc Activty"+sessionManager.getOffline());
    }

    @Override
    public void networkUnavailable() {
        System.out.println("================Network Available DeviceDesc Activty"+sessionManager.getOffline());
        if(sessionManager.getOffline().contains("true")){
            LayoutInflater li = LayoutInflater.from(DeviceDesc.this);
            View dialogView = li.inflate(R.layout.connection_lost_dialog, null);
            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(
                    DeviceDesc.this);
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
                                        client = pahoMqttClient.getMqttClient(DeviceDesc.this, Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
                                        sessionManager.setOffline("false");
                                        Toast.makeText(DeviceDesc.this,"Connected to Local IP",Toast.LENGTH_SHORT).show();
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

        String dev_id=object.getString("device_id");
        String req=obj.getString("req_type");
        if(dialog1.isShowing())
        {

            dialog1.cancel();
        }
        if(req.contains("DEV_PARAM_RES"))
        {
            android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(DeviceDesc.this);
            alert.setMessage(obj.getString("message"));
            alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            alert.show();

        }
        JSONObject object2=obj.getJSONObject("dev_param");
        if(object2.has("device_id") && object2.getString("device_id").equalsIgnoreCase(dev_id))
        {
            String type=object.getString("typeOfDevice");
            System.out.println(dev_id+"==============="+type);
            if(type.equalsIgnoreCase("Water"))
            {
                System.out.println(dev_id+"==============="+type);
                int value=object2.getInt("value");
                        if(value==255)
                        {
                            currentStatus.setText("Leak");
                            currentStatus.setTextColor(Color.GREEN);
                        }
                        else
                        {
                            currentStatus.setText("Dry");
                            currentStatus.setTextColor(Color.BLACK);
                        }

            }
            if(type.equalsIgnoreCase("MultiSensor"))
            {
                int value1;
                String label=object2.getString("label");
                System.out.println("Incoming ============"+obj);

               // int value=object2.getInt("value");
                if(label.equalsIgnoreCase("Luminance"))
                {
                    value1=object2.getInt("value");
                    LuminanceValue.setText(value1+"");
                    /*humidityValue.setText(object.getString("humidityValue"));
                    burglarValue.setText(object.getString("burglarValue"));
                    tempretureValue.setText(object.getString("tempretureValue")+"F");*/

                }
                if(label.equalsIgnoreCase("Burglar"))
                {
                    value1=object2.getInt("value");
                    burglarValue.setText(value1+"");
                    /*humidityValue.setText(object.getString("humidityValue"));
                    LuminanceValue.setText(object.getString("LuminanceValue"));
                    tempretureValue.setText(object.getString("tempretureValue")+"F");*/
                }
                if(label.equalsIgnoreCase("Relative Humidity"))
                {
                    value1=object2.getInt("value");
                    System.out.println("===Lable"+label+"==value1==="+value1+"==object2=="+object2+"==========="+object);
                    humidityValue.setText(value1+"");
                    /*burglarValue.setText(object.getString("burglarValue"));
                    LuminanceValue.setText(object.getString("LuminanceValue"));
                    tempretureValue.setText(object.getString("tempretureValue")+"F");*/
                }
                if(label.equalsIgnoreCase("Temperature"))
                {
                    value1=object2.getInt("value");
                    System.out.println("===Lable"+label+"==value1==="+value1+"==object2=="+object2+"==========="+object);
                    tempretureValue.setText(value1+"");
                   /* burglarValue.setText(object.getString("burglarValue"));
                    LuminanceValue.setText(object.getString("LuminanceValue"));
                    humidityValue.setText(object.getString("tempretureValue")+"F");*/
                }
            }
            if(type.equalsIgnoreCase("Switch"))
            {

                boolean value=object2.getBoolean("value");
                if(value==true) {

                    currentStatus.setText("Ring");
                    currentStatus.setVisibility(View.VISIBLE);
                    currentStatus.setTextColor(Color.GREEN);
                }
                else
                {

                    currentStatus.setText(" ");
                    currentStatus.setVisibility(View.VISIBLE);
                }
            }
            if(type.equalsIgnoreCase("DoorBell"))
            {

                String label1=object2.getString("label");

                if(label1.contains("Access"))
                {

                    int value=object2.getInt("value");
                    if(value==22) {
                        currentStatus.setText("Open");
                        currentStatus.setTextColor(Color.GREEN);
                        device_icon1.setImageResource(android.R.color.transparent);
                        device_icon1.setImageResource(R.drawable.open);
                    }
                    if(value==23) {
                        currentStatus.setText("Close");
                        currentStatus.setTextColor(Color.BLACK);
                        device_icon1.setImageResource(android.R.color.transparent);
                        device_icon1.setImageResource(R.drawable.close);
                    }
                    //device_icon1.setImageResource(R.drawable.bell);
                    currentStatus.setVisibility(View.VISIBLE);
                }
            }
        }



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
        Intent intent = new Intent(this, DeviceManagementService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        super.onStop();
        if (bound) {
            myService.setCallbacks(null); // unregister
            unbindService(serviceConnection);
            bound = false;
        }
    }
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            DeviceManagementService.LocalBinder binder = (DeviceManagementService.LocalBinder) service;
            myService = binder.getService();
            bound = true;
            myService.setCallbacks(DeviceDesc.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

}
