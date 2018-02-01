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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.beppi.tristatetogglebutton_library.TriStateToggleButton;

/**
 * Created by ravikumar on 17/11/17.
 */

public class Zigbee extends AppCompatActivity implements CallBacks,NetworkStateReceiver.NetworkStateReceiverListener  {

    Toolbar toolbar;
    public ZigbeeService myService;
    public boolean bound = false;
    JSONObject request_Payload;
    Constants constants;
    private RecyclerView recyclerView;
    SessionManager sessionManager;
    ProgressDialog dialog1 ;
    private MqttAndroidClient client;
    private String TAG = "MainActivity";
    private PahoMqttClient pahoMqttClient;
    //SwitchCompat switch_compat;
    TriStateToggleButton switch_compat;
    ZigbeeAdapter mAdapter;
    ImageView arrow;
    Button offAll,onAll;
    RelativeLayout relativeLayout;
    private List<ZigbeeDevices> bleDeviceList = new ArrayList<>();
    private NetworkStateReceiver networkStateReceiver;
    Handler h = new Handler();
    int delay = 15000; //15 seconds
    Runnable runnable;
    TextView buttonstatus;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zigbee);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("ZIGB");

        Intent intent = new Intent(Zigbee.this, ZigbeeService.class);
        startService(intent);
        sessionManager=new SessionManager(getApplicationContext());
        constants=new Constants();
        dialog1 = new ProgressDialog(Zigbee.this);
        pahoMqttClient = new PahoMqttClient();
        client = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());


        switch_compat = (TriStateToggleButton) findViewById(R.id.Switch);
        buttonstatus = (TextView) findViewById(R.id.buttonstatus);
        //switch_compat=(SwitchCompat)findViewById(R.id.Switch);
        recyclerView = (RecyclerView) findViewById(R.id.cardList1);
        relativeLayout=(RelativeLayout)findViewById(R.id.rLayout1);
        offAll=(Button)findViewById(R.id.offAll) ;
        onAll=(Button)findViewById(R.id.onAll) ;
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new ZigbeeAdapter(bleDeviceList);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

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

        switch_compat.setToggleStatus(false);
        switch_compat.setOnToggleChanged(new TriStateToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(TriStateToggleButton.ToggleStatus toggleStatus, boolean booleanToggleStatus, int toggleIntValue) {
                switch (toggleStatus) {
                    case off: buttonstatus.setText("OFF"); break;
                    case on: buttonstatus.setText("ON"); break;
                }
                if(booleanToggleStatus){
                    showViews();
                }else {
                    hideViews();
                }
            }
        });

//        switch_compat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked==true)
//                {
//                    showViews();
//                }
//                else
//                {
//
//                    hideViews();
//                }
//            }
//        });

        offAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enable("",0,"OFFALL");
            }
        });
        onAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enable("",0,"ONALL");
            }
        });
    }

    private void showViews() {

        dialog1.setMessage("enabling Zigbee , please wait........");
        dialog1.show();
        try{
            request_Payload=new JSONObject();
            request_Payload.put("req_type","DEV_CONFIG_REQ");
            request_Payload.put("trans_id",constants.GenerateRandomNumber());
            request_Payload.put("gw_id",sessionManager.getGateway_ID());
            request_Payload.put("proto_id","ZIGB");
            request_Payload.put("cmd_id","ENABLE");
            request_Payload.put("timestamp",constants.getTime());
            request_Payload.put("user_id",sessionManager.getUser_ID());
            request_Payload.put("dev_type","PERIPHERALS");
            MqttMessage message = new MqttMessage();
            message.setPayload(request_Payload.toString().getBytes());
            /*pahoMqttClient.publishMessage(client, message.toString(), 2, "DEV_CONFIG_REQ/"+sessionManager.getGateway_ID());*/
            pahoMqttClient.publishMessage(client, message.toString(), 2, "Spider/"+sessionManager.getGateway_ID()+"/"+"DEV_CONFIG_REQ/");

            System.out.println("==============Zigbee"+message.toString());
            dialog1.setMessage("please wait........");
            dialog1.show();
        }
        catch (Exception exp)
        {

        }

    }
    private void hideViews(){
        recyclerView.setVisibility(View.GONE);
        sessionManager.setZigbeeEnabled("false");
        relativeLayout.setVisibility(View.GONE);
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            ZigbeeService.LocalBinder binder = (ZigbeeService.LocalBinder) service;
            myService = binder.getService();
            bound = true;
            myService.setCallbacks(Zigbee.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, ZigbeeService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
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
    protected void onResume() {
        super.onResume();
        System.out.println("OnResume========");
        h.postDelayed(runnable,delay);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
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
    public void networkAvailable() {

        sessionManager.setOffline("true");
    }

    @Override
    public void networkUnavailable() {


        if(sessionManager.getOffline().contains("true")){
            LayoutInflater li = LayoutInflater.from(Zigbee.this);
            View dialogView = li.inflate(R.layout.connection_lost_dialog, null);
            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(
                    Zigbee.this);
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
                                        client = pahoMqttClient.getMqttClient(Zigbee.this, Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
                                        sessionManager.setOffline("false");
                                    }
                                    catch (Exception exp)
                                    {
                                        exp.printStackTrace();
                                    }

                                }
                            });

            android.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            sessionManager.setOffline("false");
        }
    }

    @Override
    public void SubscribeTopics(MqttAndroidClient mqttAndroidClient) throws MqttException {

    }

    @Override
    public void MessageArrivedFrom_GW_REG_RES_Topic(JSONObject obj) throws JSONException {


        dialog1.cancel();

        String message=obj.getString("message");
        System.out.println("Response =========== "+message);
        if(message.equalsIgnoreCase("ZIGB ENABLE"))
        {

            switch_compat.setToggleStatus(true);
            sessionManager.setZigbeeEnabled("true");
            relativeLayout.setVisibility(View.VISIBLE);
            prepareBLEDeviceList();
            recyclerView.setVisibility(View.VISIBLE);
        }
        else if(message.equalsIgnoreCase("ONALL"))
        {
            System.out.println("ON ALL====== "+message);
            updateView("ONALL");
        }
        else if(message.equalsIgnoreCase("OFFALL"))
        {
            System.out.println("OFF ALL====== "+message);
            updateView("OFFALL");
        }
        else if(message.equalsIgnoreCase("ON12"))
        {
            updateAt(0,"ON");
        }
        else if(message.equalsIgnoreCase("ON13"))
        {
            updateAt(1,"ON");
        }
        else if(message.equalsIgnoreCase("OFF12"))
        {
            updateAt(0,"OFF");
        }
        else if(message.equalsIgnoreCase("OFF13"))
        {
            updateAt(1,"OFF");
        }
        else
        {

            sessionManager.setZigbeeEnabled("false");
            android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(Zigbee.this);
            alert.setTitle("Failed !!!");
            alert.setMessage(obj.getString("Something wrong,please try later"));
            alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                    switch_compat.setToggleStatus(false);
                }
            });
            alert.show();
            recyclerView.setVisibility(View.GONE);
        }




    }
    private void updateAt(int i,String action)
    {
        RecyclerView.ViewHolder v;
        SwitchCompat ch;
        v=recyclerView.findViewHolderForAdapterPosition(i);
        View view=v.itemView;
        //v=recyclerView.findViewHolderForItemId(mAdapter.getItemId(i)).itemView;
        ch=(SwitchCompat) view.findViewById(R.id.arrow);
        if(action.equalsIgnoreCase("ON"))
            ch.setChecked(true);
        else
            ch.setChecked(false);
    }

    private void updateView(String onall) {
        RecyclerView.ViewHolder v;
        SwitchCompat ch;
        System.out.println(bleDeviceList.size()+"=========="+bleDeviceList.toString());
        for(int i=0;i<bleDeviceList.size();i++)
        {
            System.out.println("UPdate View====== "+i);
            v=recyclerView.findViewHolderForAdapterPosition(i);
            View view=v.itemView;
            //v=recyclerView.findViewHolderForItemId(mAdapter.getItemId(i)).itemView;
            ch=(SwitchCompat) view.findViewById(R.id.arrow);
             if(onall.contains("ONALL"))
             {
                 ch.setChecked(true);
             }
             else{
                 ch.setChecked(false);
             }



        }
    }

    public void enable(String id,int postion,String cmd)
    {
        dialog1.setMessage("enabling Zigbee , please wait........");
        dialog1.show();
        try{
            request_Payload=new JSONObject();
            request_Payload.put("req_type","DEV_CONFIG_REQ");
            request_Payload.put("trans_id",constants.GenerateRandomNumber());
            request_Payload.put("gw_id",sessionManager.getGateway_ID());
            request_Payload.put("proto_id","ZIGB");
            request_Payload.put("cmd_id",cmd+id);
            request_Payload.put("timestamp",constants.getTime());
            request_Payload.put("user_id",sessionManager.getUser_ID());
            request_Payload.put("dev_type","CONFIG");
            MqttMessage message = new MqttMessage();
            message.setPayload(request_Payload.toString().getBytes());
            /*pahoMqttClient.publishMessage(client, message.toString(), 2, "DEV_CONFIG_REQ/"+sessionManager.getGateway_ID());*/
            pahoMqttClient.publishMessage(client, message.toString(), 2, "Spider/"+sessionManager.getGateway_ID()+"/"+"DEV_CONFIG_REQ/");

            System.out.println("==============Zigbee On"+message.toString());
            dialog1.setMessage("please wait........");
            dialog1.show();
        }
        catch (Exception exp)
        {

        }
    }
    private void disable(int id, int position) {

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

    private void prepareBLEDeviceList() {

        try{
            ZigbeeDevices device=new ZigbeeDevices();
            device.setId(12);
            device.setName("ZIGB Device1");
            device.setTimestamp(constants.getTime());
            ZigbeeDevices device1=new ZigbeeDevices();
            device1.setId(13);
            device1.setName("ZIGB Device2");
            device1.setTimestamp(constants.getTime());
            bleDeviceList.clear();
            bleDeviceList.add(device);
            bleDeviceList.add(device1);
            mAdapter.notifyDataSetChanged();

        }catch (Exception exp)
        {
            exp.printStackTrace();
        }
    }

    class ZigbeeAdapter extends RecyclerView.Adapter<ZigbeeAdapter.MyViewHolder1>
    {

        List<ZigbeeDevices> bleDevicesList;
        @Override
        public ZigbeeAdapter.MyViewHolder1 onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.zegbee_devices_row, parent, false);

            return new ZigbeeAdapter.MyViewHolder1(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder1 holder, final int position) {

            final ZigbeeDevices user = bleDevicesList.get(position);
            holder.deviceID.setText(user.getId()+"");
            holder.deviceName.setText(user.getName());
            holder.time.setText(user.getTimestamp());

            holder.deviceIcon1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if(isChecked==true)
                    {
                        enable(user.getId()+"",position,"ON");
                    }
                    else
                    {
                        enable(user.getId()+"",position,"OFF");

                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return bleDevicesList.size();
        }


        class MyViewHolder1 extends RecyclerView.ViewHolder {
            public TextView deviceName,deviceID,time;
            public ImageView arrowIcon;
            public SwitchCompat deviceIcon1;

            public MyViewHolder1(View view) {
                super(view);
                deviceName= (TextView) view.findViewById(R.id.deviceName);
                time = (TextView) view.findViewById(R.id.time);
                deviceID=(TextView)view.findViewById(R.id.deviceID);
                //  deviceID = (TextView) view.findViewById(R.id.deviceID);
                //deviceIcon=(ImageView)view.findViewById(R.id.device_icon);
                deviceIcon1=(SwitchCompat) view.findViewById(R.id.arrow);

            }

        }
        public ZigbeeAdapter(List<ZigbeeDevices> bleDevicesList1) {
            this.bleDevicesList = bleDevicesList1;
        }
    }


    class ZigbeeDevices
    {
        private int id;
        private String name;
        private String timestamp;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }


}
