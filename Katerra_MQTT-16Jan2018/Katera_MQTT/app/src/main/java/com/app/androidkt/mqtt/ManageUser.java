package com.app.androidkt.mqtt;

import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ravikumar on 5/10/17.
 */



import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ManageUser extends AppCompatActivity implements
        CallBacks ,NetworkStateReceiver.NetworkStateReceiverListener {

    private MqttAndroidClient client;
    private PahoMqttClient pahoMqttClient;
    private List<Users> usersList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ManageUser.UsersAdapter mAdapter;
    FloatingActionButton fab;
    String adapPosition="";
    Constants constants;
    public HomePageService myService;
    public boolean bound = false;
    JSONObject request_Payload;
    Users users;
    String a,b;
    ProgressDialog process ;
    Gson gson;
     Toolbar toolbar;
    public SessionManager sessionManager;
    private NetworkStateReceiver networkStateReceiver;
    Handler h = new Handler();
    int delay = 15000; //15 seconds
    Runnable runnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_user);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Manage User");
        sessionManager=new SessionManager(this);
        request_Payload=new JSONObject();
        Intent intent = new Intent(ManageUser.this, HomePageService.class);
        startService(intent);
        pahoMqttClient = new PahoMqttClient();
        client = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
        constants=new Constants();
        recyclerView = (RecyclerView) findViewById(R.id.cardList1);
        sessionManager=new SessionManager(ManageUser.this);
        fab=(FloatingActionButton)findViewById(R.id.fab1);
        prepareUsersData();
        mAdapter = new ManageUser.UsersAdapter(usersList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
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


        recyclerView.addOnItemTouchListener(new RecycleritemTouchListener(this,recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onDoubleTap(View child, int childAdapterPosition) {
                //Toast.makeText(HomePage.this,"Double Clicked",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {
                adapPosition = ""+position;
                Users u=usersList.get(position);
                final  String a=u.getUser_ID();
                AlertDialog.Builder alert = new AlertDialog.Builder(ManageUser.this);
                alert.setTitle("Delete User?");
                alert.setMessage("Are you sure ,you want to delete User?");
                alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        try {
                            request_Payload.put("req_type","GW_REG_REQ");
                            request_Payload.put("user_id",a);
                            request_Payload.put("trans_id",constants.GenerateRandomNumber());
                            request_Payload.put("gw_id",sessionManager.getGateway_ID());
                            request_Payload.put("cmd_id","DEL");
                            request_Payload.put("err_id","0");
                            request_Payload.put("message","Valid");
                            Log.d("Home Page",request_Payload.toString()+"=============From Delete User Device");
                            MqttMessage message = new MqttMessage();
                            message.setPayload(request_Payload.toString().getBytes());
                            pahoMqttClient.publishMessage(client, message.toString(), 2, "USR_REG_REQ/"+sessionManager.getGateway_ID());

                            process=new ProgressDialog(ManageUser.this);
                            process.setMessage("deleting user...");
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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                LayoutInflater inflater = LayoutInflater.from(view.getContext());
                View dialogView = inflater.inflate(R.layout.add_user, null);
                final EditText userID=(EditText)dialogView.findViewById(R.id.userID);
                final EditText gatewayID=(EditText)dialogView.findViewById(R.id.gatewayID);
                Button addUser=(Button)dialogView.findViewById(R.id.addUser);
                alert.setView(dialogView);
                final AlertDialog alertDialog = alert.create();

                addUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        a=userID.getText().toString();
                        b=constants.GenerateUniquePassword();
                        if(!(a.isEmpty()&& b.isEmpty()))
                        {
                            try
                            {
                                request_Payload.put("req_type","GW_REG_REQ");
                                request_Payload.put("trans_id",new Constants().GenerateRandomNumber());
                                request_Payload.put("gw_id",a);
                                request_Payload.put("user_id",b);
                                request_Payload.put("err_id","0");
                                request_Payload.put("cmd_id","ADD");
                                request_Payload.put("message","Add User ID");
                                MqttMessage message = new MqttMessage();
                                message.setPayload(request_Payload.toString().getBytes());
                                Log.d("Add User from HP",message.toString());
                                pahoMqttClient.publishMessage(client, message.toString(), 2, "USR_REG_REQ/"+sessionManager.getGateway_ID());
                                alertDialog.cancel();
                                process=new ProgressDialog(ManageUser.this);
                                process.setMessage("adding user");
                                process.show();
                            }
                            catch (Exception exp)
                            {
                                exp.printStackTrace();
                            }
                        }
                        else
                        {
                            Toast.makeText(ManageUser.this,"Field's cant be blank",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                alertDialog.show();



            }
        });
    }

    private void prepareUsersData() {


        gson=new Gson();
        String json=sessionManager.getUsers_Details();
        System.out.println("======="+json);
        if(!json.equals(""))
        {
            Type type = new TypeToken<List<Users>>(){}.getType();
            List<Users> users1= gson.fromJson(json, type);
            usersList=users1;
        }

    }
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            HomePageService.LocalBinder binder = (HomePageService.LocalBinder) service;
            myService = binder.getService();
            bound = true;
            myService.setCallbacks(ManageUser.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };


    @Override
    public void onBackPressed() {
        super.onBackPressed();
       // startActivity(new Intent(this,HomePage.class));
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_page, menu);
        return true;
    }*/

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

        System.out.println("Manage User Activity ================"+obj);
        Log.d("Home JSOn",obj.toString());

        if(process!=null)
            process.dismiss();
        AlertDialog.Builder alert = new AlertDialog.Builder(ManageUser.this);
        String s=obj.getString("req_type");
        if(obj.has("req_type") && s.contains("GW_USRID_RES"))
        {
            try{
            JSONArray itemArray=new JSONArray(obj);
            for (int i = 0; i < itemArray.length(); i++) {
                String value=itemArray.getString(i);
                users=new Users(value,sessionManager.getGateway_ID(),constants.getTime());
                usersList.add(users);
            }
            mAdapter.notifyDataSetChanged();
            }
            catch (JSONException exp)
            {
                exp.printStackTrace();
            }
        }
        if(obj.has("req_type") && s.contains("GW_REG_RES")) {
            if (obj.has("message") && obj.get("message").toString().equalsIgnoreCase("User Deleted")) {
                alert.setTitle("Success");
                alert.setMessage(obj.getString("message"));
                alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        if (!adapPosition.equalsIgnoreCase("")) {
                            usersList.remove(Integer.parseInt(adapPosition));
                            mAdapter.notifyDataSetChanged();
                            Gson gson = new Gson();
                            String json = new Gson().toJson(usersList);
                            sessionManager.set_Users_Details(json);
                        }
                    }
                });
                alert.show();


            }

            if (obj.has("message") && obj.get("message").toString().equalsIgnoreCase("USER ADDED")) {
                System.out.print("Came hrer");
                alert.setTitle("Success");
                alert.setMessage(obj.getString("message"));
                alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        users=new Users(a,b,constants.getTime());
                        usersList.add(users);
                        mAdapter.notifyDataSetChanged();
                        gson=new Gson();
                        sessionManager.set_Users_Details(gson.toJson(usersList));

                    }
                });
                alert.show();

            }
            else
            {
                alert.setTitle("!!!!!");
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

        else
        {
            alert.setTitle("!!!!!");
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

    @Override
    public void MessageArrivedFrom_GW_HB_RES_Topic(JSONObject obj) throws JSONException {

    }

    @Override
    public void MessageArrivedFrom_DEV_NOTIFY_RES_Topic(JSONObject obj) throws JSONException {

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, HomePageService.class);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
    }

    @Override
    public void networkAvailable() {
        sessionManager.setOffline("true");
    }

    @Override
    public void networkUnavailable() {


        if(sessionManager.getOffline().contains("true")){
            LayoutInflater li = LayoutInflater.from(ManageUser.this);
            View dialogView = li.inflate(R.layout.connection_lost_dialog, null);
            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(
                    ManageUser.this);
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
                                        client = pahoMqttClient.getMqttClient(ManageUser.this, Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
                                        sessionManager.setOffline("false");
                                        Toast.makeText(ManageUser.this,"Connected to Local IP",Toast.LENGTH_SHORT).show();
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
    public void MessageArrivedFrom_GW_AUTO_RES_Topic(JSONObject obj) throws JSONException {

    }

    @Override
    public void MessageArrivedFrom_DEV_REG_RES(JSONObject obj) throws JSONException {
        System.out.println("came here in Home");
    }

    class UsersAdapter extends RecyclerView.Adapter<ManageUser.UsersAdapter.MyViewHolder1>{

        private List<Users> usersList;

        @Override
        public ManageUser.UsersAdapter.MyViewHolder1 onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_details_row, parent, false);

            return new ManageUser.UsersAdapter.MyViewHolder1(itemView);
        }

        @Override
        public int getItemCount() {
            return usersList.size();
        }

        @Override
        public void onBindViewHolder(ManageUser.UsersAdapter.MyViewHolder1 holder, int position) {

            Users user = usersList.get(position);
            holder.gateway_ID.setText(user.getGateway_ID());
            holder.user_ID.setText(user.getUser_ID());
            holder.time.setText(user.getTime());
            holder.imageView.setImageResource(R.drawable.user);


        }
        class MyViewHolder1 extends RecyclerView.ViewHolder {
            public TextView gateway_ID,time,user_ID;
            public ImageView imageView;

            public MyViewHolder1(View view) {
                super(view);
                user_ID= (TextView) view.findViewById(R.id.user_ID);
                time = (TextView) view.findViewById(R.id.time);
                gateway_ID = (TextView) view.findViewById(R.id.gateway_ID);
                imageView=(ImageView)view.findViewById(R.id.user_icon);

            }


        }
        public UsersAdapter(List<Users> usersLIst) {
            this.usersList = usersLIst;
        }
    }

}
