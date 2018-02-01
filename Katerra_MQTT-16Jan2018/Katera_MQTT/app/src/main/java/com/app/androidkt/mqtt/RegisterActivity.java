package com.app.androidkt.mqtt;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.util.Properties;




import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class RegisterActivity extends AppCompatActivity implements CallBacks ,NetworkStateReceiver.NetworkStateReceiverListener{

    private RegisterService myService;
    private boolean bound = false;
    private MqttAndroidClient client;
    private PahoMqttClient pahoMqttClient;

    private List<Users> usersList = new ArrayList<>();
    JSONObject request_Payload;
    Constants constants;
    EditText et_email,et_password,et_username,mobile;
    Button bt_go;
    /*@InjectView(R.id.et_email)
    EditText et_email;
    @InjectView(R.id.fab)
    FloatingActionButton fab;
    @InjectView(R.id.cv_add)
    CardView cvAdd;
    @InjectView(R.id.bt_go)
    Button bt_go;
    @InjectView(R.id.et_password)
    EditText et_password;
    @InjectView(R.id.et_username)
    EditText et_username;
    @InjectView(R.id.mobile)
    EditText mobile;*/
    ProgressDialog dialog1;
    String user_ID, gateWay_ID, email_ID, mobileNumber;
    SessionManager sessionManager;
    android.support.v7.app.AlertDialog.Builder alert;
    String randomPassword;
    private NetworkStateReceiver networkStateReceiver;
    Handler h = new Handler();
    int delay = 15000; //15 seconds
    Runnable runnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_new);
        et_email=(EditText)findViewById(R.id.et_email);
        et_username=(EditText)findViewById(R.id.et_username);
        et_password=(EditText)findViewById(R.id.et_password);
        mobile=(EditText)findViewById(R.id.mobile);
        bt_go=(Button)findViewById(R.id.bt_go);

        sessionManager = new SessionManager(this);
        constants = new Constants();
        alert = new android.support.v7.app.AlertDialog.Builder(RegisterActivity.this);
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        pahoMqttClient = new PahoMqttClient();
        client = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
        h.postDelayed(new Runnable() {
            public void run() {
                sendBroadcast(new Intent("ABCD"));
                runnable=this;
                h.postDelayed(runnable, delay);
            }
        }, delay);

/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ShowEnterAnimation();
        }
*/

/*
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateRevealClose();
            }
        });
*/


        bt_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mobile.getText().toString().isEmpty() & !et_password.getText().toString().isEmpty() && !et_username.getText().toString().isEmpty() && !et_email.getText().toString().isEmpty()) {
                    user_ID = et_username.getText().toString();
                    randomPassword = constants.GenerateUniquePassword();
                    gateWay_ID = et_password.getText().toString();
                    mobileNumber = mobile.getText().toString();
                    email_ID = et_email.getText().toString();
                    email_ID = email_ID.trim().toString();
                    String pattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                    if (email_ID.matches(pattern)) {
                        try {
                            request_Payload = new JSONObject();
                            request_Payload.put("req_type", "USR_REG_REQ");
                            request_Payload.put("trans_id", new Constants().GenerateRandomNumber());
                            request_Payload.put("gw_id", et_password.getText());
                            request_Payload.put("Psswrd", randomPassword);
                            request_Payload.put("Email", email_ID);
                            request_Payload.put("Usr_name", et_username.getText());
                            request_Payload.put("err_id", "0");
                            request_Payload.put("cmd_id", "ADD");
                            request_Payload.put("Mob_numb", mobileNumber);
                            request_Payload.put("message", "Register new User");

                            MqttMessage message = new MqttMessage();
                            message.setPayload(request_Payload.toString().getBytes());
                            pahoMqttClient.publishMessage(client, message.toString(), 2, "USR_REG_REQ/" + et_password.getText());
                            //  Toast.makeText(RegisterActivity.this,"Pubslishing to Spider/",Toast.LENGTH_SHORT).show();
                            dialog1 = new ProgressDialog(RegisterActivity.this);
                            dialog1.setMessage("please wait");
                            dialog1.show();
                        } catch (Exception exp) {
                            exp.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Enter valid email", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Fields can't be Blank", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
    public void networkAvailable() {
        sessionManager.setOffline("true");
        System.out.println("================Network Available HB Activty"+sessionManager.getOffline());
    }

    @Override
    public void networkUnavailable() {

        System.out.println("================Network UnAvailable HB Activty"+sessionManager.getOffline());
        if(sessionManager.getOffline().equals("true")){
            LayoutInflater li = LayoutInflater.from(RegisterActivity.this);
            View dialogView = li.inflate(R.layout.connection_lost_dialog, null);
            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(
                    RegisterActivity.this);
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
                                        client = pahoMqttClient.getMqttClient(RegisterActivity.this, Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
                                        sessionManager.setOffline("false");
                                        Toast.makeText(RegisterActivity.this,"Connected to Local IP",Toast.LENGTH_SHORT).show();
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


/*
    private void ShowEnterAnimation() {

        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.fabtransition);
        getWindow().setSharedElementEnterTransition(transition);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                cvAdd.setVisibility(View.GONE);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                transition.removeListener(this);
                animateRevealShow();
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }

        });
    }
*/

    /*public void animateRevealShow() {
        Animator mAnimator = ViewAnimationUtils.createCircularReveal(cvAdd, cvAdd.getWidth() / 2, 0, fab.getWidth() / 2, cvAdd.getHeight());
        mAnimator.setDuration(500);
        mAnimator.setInterpolator(new AccelerateInterpolator());
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                cvAdd.setVisibility(View.VISIBLE);
                super.onAnimationStart(animation);
            }
        });
        mAnimator.start();
    }

    public void animateRevealClose() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Animator mAnimator = ViewAnimationUtils.createCircularReveal(cvAdd, cvAdd.getWidth() / 2, 0, cvAdd.getHeight(), fab.getWidth() / 2);
            mAnimator.setDuration(500);
            mAnimator.setInterpolator(new AccelerateInterpolator());
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    cvAdd.setVisibility(View.INVISIBLE);
                    super.onAnimationEnd(animation);
                    fab.setImageResource(R.drawable.if_plus);
                    RegisterActivity.super.onBackPressed();
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                }
            });
            mAnimator.start();
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
    }*/


    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }


    @Override
    public void SubscribeTopics(MqttAndroidClient mqttAndroidClient) throws MqttException {

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, RegisterService.class);
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

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            RegisterService.LocalBinder binder = (RegisterService.LocalBinder) service;
            myService = binder.getService();
            bound = true;
            myService.setCallbacks(RegisterActivity.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    public void MessageArrivedFrom_GW_REG_RES_Topic(JSONObject obj) throws JSONException {

        dialog1.dismiss();
        System.out.println("Register Activity ================" + obj.toString());
        dialog1 = null;
        Log.d("Reg. Act. Res Payload", obj.toString());


        if (obj.has("message") && obj.get("message").equals("USER ADDED")) {

            final String user_id = obj.getString("user_id");
            alert.setTitle("Success");
            alert.setMessage("Please check your email");
            alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                    Users users = new Users(gateWay_ID, user_id, constants.getTime());
                    usersList.add(users);
                    String json = new Gson().toJson(usersList);
                    sessionManager.set_Users_Details(json);
                    dialogInterface.cancel();
                    startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                    //animateRevealClose();

                }
            });
            alert.show();

        } else {

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
    protected void onDestroy() {
        super.onDestroy();

    }
}

