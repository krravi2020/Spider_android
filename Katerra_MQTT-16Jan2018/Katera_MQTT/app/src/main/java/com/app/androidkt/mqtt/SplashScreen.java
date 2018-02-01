package com.app.androidkt.mqtt;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.skyfishjy.library.RippleBackground;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ravikumar on 10/1/18.
 */

public class SplashScreen extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 10000;
    JSONObject request_Payload;
    SessionManager sessionManager;
    MqttMessage message;
    ProgressDialog dialog ,dialog1;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient client;
    RippleBackground rippleBackground;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        /*com.wang.avi.AVLoadingIndicatorView avi=(com.wang.avi.AVLoadingIndicatorView)findViewById(R.id.avi);
        avi.show();*/
        rippleBackground=(RippleBackground)findViewById(R.id.content);
        rippleBackground.startRippleAnimation();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                rippleBackground.stopRippleAnimation();
                Intent i=new Intent(SplashScreen.this,MainActivity.class);
                startActivity(i);
                finish();
            }
        },SPLASH_TIME_OUT);


        /*if(Constants.MQTT_BROKER_URL.contains("142"))
        {new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkServerStatus();
            }
        }, 5000);}
*/
    }
    private void checkServerStatus() {
        try {
            request_Payload=new JSONObject();
            request_Payload.put("client_id",sessionManager.getIMEI());
            request_Payload.put("message","get_status");
            message= new MqttMessage();
            message.setPayload(request_Payload.toString().getBytes());
            pahoMqttClient.publishMessage(client, message.toString(), 2, "Server_Status");
            System.out.println("Checking Server status=="+message.toString());
            dialog1=new ProgressDialog(SplashScreen.this);
            dialog1.show();


            final Timer t = new Timer();
            t.schedule(new TimerTask() {
                public void run() {
                    if(dialog1.isShowing())
                    {
                        dialog1.dismiss();
                        SplashScreen.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder alert1= new AlertDialog.Builder(SplashScreen.this);
                                alert1.setMessage("Could not contact Spider");
                                alert1.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
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
    }
}
