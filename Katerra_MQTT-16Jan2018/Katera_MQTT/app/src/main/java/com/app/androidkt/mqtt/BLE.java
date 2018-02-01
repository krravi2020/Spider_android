package com.app.androidkt.mqtt;

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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class BLE extends AppCompatActivity implements CallBacks,NetworkStateReceiver.NetworkStateReceiverListener  {
    Toolbar toolbar;
    public BLEService myService;
    public boolean bound = false;
    JSONObject request_Payload;
    Constants constants;
    private RecyclerView recyclerView;
    SessionManager sessionManager;
    ProgressDialog dialog1 ;
    private MqttAndroidClient client;
    private String TAG = "MainActivity";
    private PahoMqttClient pahoMqttClient;
    SwitchCompat switch_compat;
    BLEAdapter mAdapter;
    ImageView arrow;
    private NetworkStateReceiver networkStateReceiver;
    private List<BLEDevices> bleDeviceList = new ArrayList<>();
    Handler h = new Handler();
    int delay = 15000; //15 seconds
    Runnable runnable;
    TriStateToggleButton tstb_4 ;
    TextView buttonstatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("BLE");
        Intent intent = new Intent(BLE.this, BLEService.class);
        startService(intent);

        sessionManager=new SessionManager(getApplicationContext());
        constants=new Constants();
        dialog1 = new ProgressDialog(BLE.this);
        pahoMqttClient = new PahoMqttClient();
        client = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());

        tstb_4 = (TriStateToggleButton) findViewById(R.id.Switch);
        buttonstatus = (TextView) findViewById(R.id.buttonstatus);
        //switch_compat=(SwitchCompat)findViewById(R.id.Switch);
        recyclerView = (RecyclerView) findViewById(R.id.cardList1);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new BLEAdapter(bleDeviceList);
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



        if(sessionManager.getBLEEnabled().equalsIgnoreCase("true")) {
           // switch_compat.setChecked(true);
            recyclerView.setVisibility(View.VISIBLE);
        }
        tstb_4.setToggleStatus(false);
        tstb_4.setOnToggleChanged(new TriStateToggleButton.OnToggleChanged() {
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

        recyclerView.addOnItemTouchListener(new RecycleritemTouchListener(this,recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {

               Intent intent1 =new Intent(view.getContext(),BLEDetails.class);
                startActivity(intent1);
            }

            @Override
            public void onDoubleTap(View child, int childAdapterPosition) {
                //Toast.makeText(HomePage.this,"Double Clicked",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));


    }
    @Override
    protected void onPause() {
        super.onPause();
        h.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("OnResume========");
        h.postDelayed(runnable,delay);
    }

    private void showViews() {

        dialog1.setMessage("enabling BLE , please wait........");
        dialog1.show();
        try{
            request_Payload=new JSONObject();
            request_Payload.put("req_type","DEV_CONFIG_REQ");
            request_Payload.put("trans_id",constants.GenerateRandomNumber());
            request_Payload.put("gw_id",sessionManager.getGateway_ID());
            request_Payload.put("proto_id","BLE");
            request_Payload.put("cmd_id","ENABLE");
            request_Payload.put("timestamp",constants.getTime());
            request_Payload.put("user_id",sessionManager.getUser_ID());
            request_Payload.put("dev_type","PERIPHERALS");
            MqttMessage message = new MqttMessage();
            message.setPayload(request_Payload.toString().getBytes());
            //pahoMqttClient.publishMessage(client, message.toString(), 2, "DEV_CONFIG_REQ/"+sessionManager.getGateway_ID());
            pahoMqttClient.publishMessage(client, message.toString(), 2, "Spider/"+sessionManager.getGateway_ID()+"/"+"DEV_CONFIG_REQ/");
            System.out.println("==============BLE"+message.toString());
            dialog1.setMessage("please wait........");
            dialog1.show();
        }
        catch (Exception exp)
        {

        }

    }
    private void hideViews(){
    recyclerView.setVisibility(View.GONE);
        sessionManager.setBLEEnabled("false");
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
            BLEService.LocalBinder binder = (BLEService.LocalBinder) service;
            myService = binder.getService();
            bound = true;
            myService.setCallbacks(BLE.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BLEService.class);
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
    public void SubscribeTopics(MqttAndroidClient mqttAndroidClient) throws MqttException {

    }

    @Override
    public void MessageArrivedFrom_GW_REG_RES_Topic(JSONObject obj) throws JSONException {
        String message=obj.getString("message");
        if(message.equalsIgnoreCase("BLE ENABLE"))
        {
            dialog1.cancel();
            sessionManager.setBLEEnabled("true");
            prepareBLEDeviceList();
            recyclerView.setVisibility(View.VISIBLE);
        }

        else
        {
            dialog1.cancel();
            sessionManager.setBLEEnabled("false");
            android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(BLE.this);
            alert.setTitle("Failed !!!");
            alert.setMessage(obj.getString("Something wrong,please try later"));
            alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                   // switch_compat.setChecked(false);
                }
            });
            alert.show();
            recyclerView.setVisibility(View.GONE);
        }


    }

    private void prepareBLEDeviceList() {

        try{
            BLEDevices device=new BLEDevices();
            device.setId(1101);
            device.setName("BLE Device");
            device.setTimestamp(constants.getTime());
            bleDeviceList.clear();
            bleDeviceList.add(device);
            mAdapter.notifyDataSetChanged();

        }catch (Exception exp)
        {
            exp.printStackTrace();
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
        System.out.println("================Network UnAvailable BLE Activty"+sessionManager.getOffline());

    }

    @Override
    public void networkUnavailable() {

        System.out.println("================Network Available BLE Activty"+sessionManager.getOffline());
        if(sessionManager.getOffline().contains("true")){
            LayoutInflater li = LayoutInflater.from(BLE.this);
            View dialogView = li.inflate(R.layout.connection_lost_dialog, null);
            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(
                    BLE.this);
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
                                        client = pahoMqttClient.getMqttClient(BLE.this, Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
                                        sessionManager.setOffline("false");
                                        Toast.makeText(BLE.this,"Connected to Local IP",Toast.LENGTH_SHORT).show();
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


}

class BLEAdapter extends RecyclerView.Adapter<BLEAdapter.MyViewHolder1>
{

     List<BLEDevices> bleDevicesList;
    @Override
    public BLEAdapter.MyViewHolder1 onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ble_devices_row, parent, false);

        return new BLEAdapter.MyViewHolder1(itemView);
    }
    @Override
    public int getItemCount() {
        return bleDevicesList.size();
    }

    @Override
    public void onBindViewHolder(BLEAdapter.MyViewHolder1 holder, int position) {

        BLEDevices user = bleDevicesList.get(position);
        holder.deviceID.setText(user.getId()+"");
        holder.deviceName.setText(user.getName());
        holder.time.setText(user.getTimestamp());
        //holder.deviceIcon.setImageResource(R.drawable.user);
       // holder.arrowIcon.setImageResource(R.drawable.arrowup1);
    }

    class MyViewHolder1 extends RecyclerView.ViewHolder {
        public TextView deviceName,deviceID,time;
        public ImageView deviceIcon,arrowIcon;

        public MyViewHolder1(View view) {
            super(view);
            deviceName= (TextView) view.findViewById(R.id.deviceName);
            time = (TextView) view.findViewById(R.id.time);
            deviceID=(TextView)view.findViewById(R.id.deviceID);
          //  deviceID = (TextView) view.findViewById(R.id.deviceID);
           // deviceIcon=(ImageView)view.findViewById(R.id.device_icon);
            arrowIcon=(ImageView)view.findViewById(R.id.arrow);

        }

    }
    public BLEAdapter(List<BLEDevices> bleDevicesList1) {
        this.bleDevicesList = bleDevicesList1;
    }
}
