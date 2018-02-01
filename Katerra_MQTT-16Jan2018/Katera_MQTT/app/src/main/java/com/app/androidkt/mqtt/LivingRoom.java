package com.app.androidkt.mqtt;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.Image;
import android.os.Handler;
import java.util.HashMap;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ravikumar on 5/10/17.
 */

public class LivingRoom  extends Fragment implements CallBacks,NetworkStateReceiver.NetworkStateReceiverListener {

    private List<Device> movieList = new ArrayList<>();
    private RecyclerView recyclerView;
    private DeviceAdapter mAdapter;
    FloatingActionButton fab;
    String spinnerText;
    String deviceText;
    int index=-1;
    Device d1;
    public SessionManager sessionManager;
    JSONObject request_Payload;
    Gson gson;
    public DeviceService myService;
    public boolean bound = false;
    Device d;
    public MqttAndroidClient client;
    public PahoMqttClient pahoMqttClient;
    Constants constants;
    String adapPosition="";
    ProgressDialog process ;
    View rootView;
    AlertDialog.Builder popup;
    HashMap<String,String>valueBeforeAdding;
    private NetworkStateReceiver networkStateReceiver;
    Handler h = new Handler();
    int delay = 15000; //15 seconds
    Runnable runnable;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,  Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.living_room, container, false);
        fab=(FloatingActionButton)rootView.findViewById(R.id.fab);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.cardList);
        sessionManager=new SessionManager(getActivity());
        constants=new Constants();
        request_Payload=new JSONObject();
        pahoMqttClient = new PahoMqttClient();
        client = pahoMqttClient.getMqttClient(getContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
        mAdapter = new DeviceAdapter(movieList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        valueBeforeAdding=new HashMap<String,String>();
        valueBeforeAdding.put("Temperature","");
        valueBeforeAdding.put("Relative Humidity","");
        valueBeforeAdding.put("Luminance","");
        valueBeforeAdding.put("Burglar","");
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        getActivity().registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        h.postDelayed(new Runnable() {
            public void run() {
                getActivity().sendBroadcast(new Intent("ABCD"));
                runnable=this;
                h.postDelayed(runnable, delay);
            }
        }, delay);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                prepareDeviceList();
            }
        }, 2000);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                LayoutInflater inflater = LayoutInflater.from(view.getContext());
                View dialogView = inflater.inflate(R.layout.add_device, null);
                final TextView deviceName=(TextView)dialogView.findViewById(R.id.deviceName);
                final Spinner spinner=(Spinner)dialogView.findViewById(R.id.spinner);
                Button addDevice=(Button)dialogView.findViewById(R.id.addDevice);
                alert.setView(dialogView);
                final AlertDialog alertDialog = alert.create();
                alertDialog.setCanceledOnTouchOutside(false);
                addDevice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        spinnerText=deviceName.getText().toString();
                        deviceText=spinner.getSelectedItem().toString();
                        if(!(spinnerText.isEmpty()&& deviceText.isEmpty()))
                        {
                            try
                            {
                                request_Payload.put("req_type"," DEV_REG_REQ2SER");
                                request_Payload.put("user_id",sessionManager.getUser_ID());
                                request_Payload.put("gw_id",sessionManager.getGateway_ID());
                                request_Payload.put("proto_id",spinner.getSelectedItem().toString());
                                request_Payload.put("cmd_id","ADD");
                                request_Payload.put("device_type","");
                                request_Payload.put("timestamp","");
                                MqttMessage message = new MqttMessage();
                                message.setPayload(request_Payload.toString().getBytes());
                                Log.d("Add Device",message.toString()+"DEV_REG_REQ2SER/"+sessionManager.getGateway_ID());
                                pahoMqttClient.publishMessage(client, message.toString(), 2, "DEV_REG_REQ2SER/"+sessionManager.getGateway_ID());
                                alertDialog.cancel();
                                process=new ProgressDialog(getContext());
                                process.setMessage("adding device...");
                                process.show();
                            }
                            catch (Exception exp)
                            {
                                exp.printStackTrace();
                            }
                        }
                    }
                });
                alertDialog.show();


            }
        });


        recyclerView.addOnItemTouchListener(new RecycleritemTouchListener(getContext(),recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {


            }

            @Override
            public void onDoubleTap(View child, int childAdapterPosition) {

                Device d=movieList.get(childAdapterPosition);
                Gson gson=new GsonBuilder().create();
                String json=gson.toJson(d);
                Intent intent=new Intent(getActivity(),DeviceDesc.class);
                intent.putExtra("json",json);
                startActivity(intent);

            }

            @Override
            public void onLongClick(View view, int position) {
                adapPosition = ""+position;
                AlertDialog.Builder alert= new AlertDialog.Builder(getActivity());
                alert.setTitle("Delete Device?");
                alert.setMessage("Are you sure you want to delete?");
                alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        try {
                            request_Payload.put("req_type","DEV_REG_REQ");
                            request_Payload.put("user_id",sessionManager.getUser_ID());
                            request_Payload.put("trans_id",constants.GenerateRandomNumber());
                            request_Payload.put("gw_id",sessionManager.getGateway_ID());
                            request_Payload.put("proto_id","Zwave");
                            request_Payload.put("cmd_id","DEL");
                            request_Payload.put("device_type","");
                            request_Payload.put("timestamp","");
                            Log.d("Del Device",request_Payload.toString());
                            MqttMessage message = new MqttMessage();
                            message.setPayload(request_Payload.toString().getBytes());
                            pahoMqttClient.publishMessage(client, message.toString(), 2, "DEV_REG_REQ2SER/"+sessionManager.getGateway_ID());
                            process=new ProgressDialog(getContext());
                            process.setMessage("deleting device...");
                            process.show();


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }


                });
                alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // close dialog
                        dialog.cancel();
                    }

                });
                alert.show();
            }
        }));

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
            h.removeCallbacks(runnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        networkStateReceiver.removeListener(this);
        getActivity().unregisterReceiver(networkStateReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        h.postDelayed(runnable,delay);

    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), DeviceService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (bound) {
        myService.setCallbacks(null); // unregister
        getActivity().unbindService(serviceConnection);
        bound = false;}
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            DeviceService.LocalBinder binder = (DeviceService.LocalBinder) service;
            myService = binder.getService();
            bound = true;
            myService.setCallbacks(LivingRoom.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    private void prepareDeviceList() {
       /* gson=new Gson();
        String json=sessionManager.get_DeviceDetails();
        System.out.println("=======json"+json);

        if(!json.equals(""))
        {
            Type type = new TypeToken<List<Device>>(){}.getType();
            List<Device> u= gson.fromJson(json, type);
            movieList=u;
        }*/

        try {


            movieList.clear();
            request_Payload.put("req_type","DEV_LIST_REQ");
            request_Payload.put("user_id",sessionManager.getUser_ID());
            request_Payload.put("trans_id",constants.GenerateRandomNumber());
            request_Payload.put("gw_id",sessionManager.getGateway_ID());
            request_Payload.put("cmd_id","DEVICES_LIST");
            request_Payload.put("timestamp","");
            Log.d("Fetch Device",request_Payload.toString());
            MqttMessage message = new MqttMessage();
            message.setPayload(request_Payload.toString().getBytes());
            System.out.println("====111"+request_Payload+" "+client+" "+pahoMqttClient);
            pahoMqttClient.publishMessage(client, message.toString(), 2, "DEV_LIST_REQ/"+sessionManager.getGateway_ID());
            process=new ProgressDialog(getActivity());
            process.setCanceledOnTouchOutside(false);
            process.setMessage("fetching devices...");
            process.setCancelable(true);
            process.show();


        } catch (Exception e) {
            e.printStackTrace();
            process.cancel();
        }


    }

    @Override
    public void networkAvailable() {

        System.out.println("================Network Available Living Activty"+sessionManager.getOffline());
        sessionManager.setOffline("true");
    }

    @Override
    public void networkUnavailable() {

        System.out.println("================Network UnAvailable Living Activty"+sessionManager.getOffline());
        if(sessionManager.getOffline().equals("true")){
        LayoutInflater li = LayoutInflater.from(getContext());
        View dialogView = li.inflate(R.layout.connection_lost_dialog, null);
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(
                getContext());
        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
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
                                    client = pahoMqttClient.getMqttClient(getContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
                                    sessionManager.setOffline("false");
                                    System.out.println("================Living room Offline"+sessionManager.getOffline());
                                    Toast.makeText(getContext(),"Connected to Local IP",Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception exp)
                                {
                                    exp.printStackTrace();
                                }

                            }
                        });


        // show it
            if(alertDialog.isShowing())
        alertDialog.show();
        sessionManager.setOffline("false");}
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

    public void ShowDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Alert!!");
        builder.setMessage(message);
        builder.setCancelable(true);

        final AlertDialog dlg = builder.create();
        dlg.setCanceledOnTouchOutside(false);

        dlg.show();

        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                dlg.dismiss(); // when the task active then close the dialog
                t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
            }
        }, 2000);

    }
    @Override
    public void MessageArrivedFrom_DEV_NOTIFY_RES_Topic(JSONObject obj) throws JSONException {

        System.out.println("Inside Living Room Notification Activty ======================="+obj);
        index=-1;
        boolean userReference=false;
        if(obj.has("userReference"))
          userReference=obj.getBoolean("userReference");
        JSONObject object=obj.getJSONObject("dev_param");

        if(object.has("device_id") && object.getString("device_id").toString()!="")
        {
            index=DeviceIndexInList(object.getString("device_id"));
            System.out.println("=================Index"+index);

            if(index<0)
            {
                JSONObject objectNotification=obj.getJSONObject("dev_param");
                if(objectNotification.has("id"))
                    valueBeforeAdding.put("id",objectNotification.getInt("device_id")+"");
                if(object.has("label"))
                {
                    String lable=objectNotification.getString("label");
                    valueBeforeAdding.put(lable,objectNotification.getInt("value")+"");
                }

            }
        }
        if(index>=0)
        {
            Device d=movieList.get(index);
            System.out.println(d.getTypeOfDevice()+"Living Room Notification Activty================");
            if(d.getTypeOfDevice().equalsIgnoreCase("Water"))
            {
                if(!userReference){
                int value=object.getInt("value");
                if(value==255)
                {
                    d.setStatus("Leak");
                    ShowDialog("Leak");
                }
                else
                    d.setStatus("Dry");}
            }
            if(d.getTypeOfDevice().equalsIgnoreCase("DoorBell"))
            {
                if(!userReference){
                String label=object.getString("label");
                System.out.println(d.getTypeOfDevice()+"Inside Doorbeel if================");
                int value=object.getInt("value");
                if(value==22)
                {
                    d.setStatus("Open");
                    ShowDialog("Open");
                }
                if(value==23)
                    d.setStatus("Close");}
            }
            if(d.getTypeOfDevice().equalsIgnoreCase("MultiSensor"))
            {
                String label=object.getString("label");
                System.out.println(d.getTypeOfDevice()+"Inside MultiSendor if================");
                int value=object.getInt("value");
                if(label.equalsIgnoreCase("Luminance"))
                {
                    d.setLuminanceValue(Integer.toString(value));
                    if(!userReference)
                       ShowDialog("Luminance  :"+Integer.toString(value));
                }
                if(label.equalsIgnoreCase("Burglar"))
                {
                    d.setBurglarValue(Integer.toString(value));
                    System.out.println("BUrg========="+d.getBurglarValue());
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                        }
                    }, 2000);
                   if(value==3 && !userReference)
                     ShowDialog("Motion  :"+Integer.toString(value));

                }
                if(label.equalsIgnoreCase("Relative Humidity"))
                {
                    d.setHumidityValue(Integer.toString(value));
                    if(!userReference)
                    ShowDialog("Relative Humidity  :"+Integer.toString(value));
                }
                if(label.equalsIgnoreCase("Temperature"))
                {
                    d.setTempretureValue(Integer.toString(value));
                    if(!userReference)
                    ShowDialog("Temperature  :"+Integer.toString(value)+" F");
                }
            }
            if(d.getTypeOfDevice().equalsIgnoreCase("Switch"))
            {

                if(!userReference)
                {
                    boolean value=object.getBoolean("value");
                if(value==true)
                {
                    d.setStatus("Ring");
                    ShowDialog("Ring");
                }
                else
                    {
                    d.setStatus(" ");
                }
                }
            }
            movieList.set(index,d);
            mAdapter.notifyDataSetChanged();
            gson=new Gson();
            String str=gson.toJson(movieList);
            sessionManager.set_DeviceDetails(str);

        }

    }

    private int DeviceIndexInList(String device_id1) {

        for (int i=0;i<movieList.size();i++)
        {
            Device d=movieList.get(i);
            if(d.getDevice_id().equals(device_id1)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void MessageArrivedFrom_GW_AUTO_RES_Topic(JSONObject obj) throws JSONException {

    }

    @Override
    public void MessageArrivedFrom_DEV_REG_RES(final JSONObject obj) throws JSONException {
        System.out.println("Living Room Activty ======================="+obj);


        Log.d("DEV_REG_RES DM===",obj.toString());

        if(obj.has("devices_list"))
        {
            JSONArray array=obj.getJSONArray("devices_list");
                    if(array.length()==0)
                    {
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("Alert!!");
                        alert.setMessage("No devices added by you");
                        alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                        alert.setCancelable(false);
                        alert.show();
                    }
            else
            {
                for(int i=0;i<array.length();i++)
                {
                    JSONObject object=array.getJSONObject(i);
                    Device d2=new Device();
                    d2.setDevice_id(object.getString("device_id"));
                    d2.setDevice_type(object.getString("device_type"));
                    d2.setValue("");
                    d2.setLabel("");
                    d2.setStatus("");
                    if(object.has(""))
                    d2.setTempretureValue("");
                    d2.setHumidityValue("");
                    d2.setLuminanceValue("");
                    d2.setBurglarValue("");
                    d2.setDeviceName(object.getString("device_name"));
                    d2.setTime(constants.getTime());
                    d2.setDevice_manufacturer(object.getString("device_manufacturer"));
                    String device=object.getString("device_name");
                    if(device.equalsIgnoreCase("Routing Binary Sensor"))
                    {
                        d2.setTypeOfDevice("Water");
                        d2.setDeviceName("Water Sensor");
                    }
                    if(device.equalsIgnoreCase("Home Security Sensor"))
                    {
                        d2.setTypeOfDevice("MultiSensor");
                    }
                    if(device.equalsIgnoreCase("Siren"))
                    {
                        d2.setTypeOfDevice("Switch");
                    }
                    if(device.equalsIgnoreCase("Sensor Notification"))
                    {
                        d2.setTypeOfDevice("DoorBell");
                    }

                    //  Device d2 = new Device(object.getString("device_id"),object.getString("device_type"),object.getString("device_manufacturer"),object.getString("label"),object.getString("value"),constants.getTime(),deviceText);
                    movieList.add(d2);

                }
                mAdapter.notifyDataSetChanged();
                mAdapter = new DeviceAdapter(movieList);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(mAdapter);
                gson=new Gson();
                sessionManager.set_DeviceDetails(gson.toJson(movieList));
            }
        }

        if(process!=null)
        {
            process.dismiss();
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        String str=obj.getString("message").trim();
        System.out.print(str.contains("Gateway is ready for exclusion")+"   ===");
        if(str.contains("Gateway is not ready for exclusion"))
        {

            alert.setTitle("Alert!!");
            alert.setMessage(str+" , please try later");
            alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            alert.show();
        }
        if(str.contains("Gateway is ready for exclusion"))
        {

            alert.setTitle("Alert!!");
            alert.setMessage(str+" , press the device button thrice");
            alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                    process=new ProgressDialog(getActivity());
                    process.setMessage("please wait");
                    process.show();
                }
            });
            alert.show();
        }
        if(str.contains("Deletion fail...Please try again"))
        {

            if(process!=null)
                process.dismiss();
            alert.setTitle("Failed");
            alert.setMessage(str);
            alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();

                }
            });
            alert.show();
        }
        if(str.contains("Device Removed Successfully"))
        {
            if(process!=null)
                process.dismiss();
            alert.setTitle("Success");
            alert.setMessage(str);
            alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(!adapPosition.equalsIgnoreCase("")){
                        System.out.println("Came here"+adapPosition+" "+movieList);
                        movieList.remove(Integer.parseInt(adapPosition));
                        mAdapter.notifyDataSetChanged();
                        gson=new Gson();
                        String json = gson.toJson(movieList);
                        sessionManager.set_DeviceDetails(json);
                    }
                    dialogInterface.cancel();
                }
            });
            alert.show();
        }
        if(str.contains("Gateway is not ready for inclusion"))
        {

            alert.setTitle("Alert!!");
            alert.setMessage(str+" , please try later");
            alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            alert.show();
        }
        if(str.contains("Gateway is ready for inclusion"))
        {
            alert.setTitle("Alert!!");
            alert.setMessage(str+" , press the device button thrice");
            alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                    process=new ProgressDialog(getActivity());
                    process.setMessage("please wait..");
                    process.show();
                }
            });
            alert.show();
        }
        if(str.contains("Inclusion fail...Please try again"))
        {
            if(process!=null)
                process.dismiss();
            alert.setTitle("Failed");
            alert.setMessage(str);
            alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();

                }
            });
            alert.show();
        }
        if(str.contains("Device Added Successfully"))
        {

            if(process!=null)
                process.dismiss();
            alert.setTitle("Success");
            alert.setMessage(str);
            alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                    //Device d2=new Device(deviceText,"Sdfdfdamsung","123456","Water HEater",spinnerText,"BLE");
                    try {
                        JSONObject object=obj.getJSONObject("dev_param");
                        System.out.println(object+"aaaaa"+object.getString("device_id"));
                        // JSONObject object=array.getJSONObject(0);
                        Device d2 = new Device();
                        d2.setDevice_id(object.getString("device_id"));
                        String device=object.getString("device_name");
                        if(device.equalsIgnoreCase("Routing Binary Sensor"))
                        {
                            d2.setTypeOfDevice("Water");
                            d2.setDeviceName("Water Sensor");
                        }
                        if(device.equalsIgnoreCase("Home Security Sensor"))
                        {
                            d2.setTypeOfDevice("MultiSensor");

                        }
                        if(device.equalsIgnoreCase("Siren"))
                        {
                            d2.setTypeOfDevice("Switch");
                        }
                        if(device.equalsIgnoreCase("Sensor Notification"))
                        {
                            d2.setTypeOfDevice("DoorBell");
                        }
                        d2.setDevice_type(object.getString("device_type"));
                        d2.setValue("");
                        d2.setLabel("");
                        d2.setStatus("");
                        d2.setTempretureValue(valueBeforeAdding.get("Temperature"));
                        d2.setHumidityValue(valueBeforeAdding.get("Relative Humidity"));
                        d2.setLuminanceValue(valueBeforeAdding.get("Luminance"));
                        d2.setBurglarValue(valueBeforeAdding.get("Burglar"));
                        System.out.println("===========Adding Hash"+valueBeforeAdding);
                        d2.setDeviceName(object.getString("device_name"));
                        d2.setTime(constants.getTime());
                        d2.setDevice_manufacturer(object.getString("device_manufacturer"));

                        //  Device d2 = new Device(object.getString("device_id"),object.getString("device_type"),object.getString("device_manufacturer"),object.getString("label"),object.getString("value"),constants.getTime(),deviceText);
                        movieList.add(d2);
                        mAdapter.notifyDataSetChanged();
                        mAdapter = new DeviceAdapter(movieList);
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
                        recyclerView.setLayoutManager(mLayoutManager);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        recyclerView.setAdapter(mAdapter);
                        gson=new Gson();
                        sessionManager.set_DeviceDetails(gson.toJson(movieList));
                        System.out.print(movieList.size()+"size");
                        valueBeforeAdding.clear();

                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }                                ;


                }
            });
            alert.show();
        }

    }

}

