package com.app.androidkt.mqtt;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.textservice.TextServicesManager;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class MqttMessageService extends Service {

    private static final String TAG = "MqttMessageService";
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    private CallBacks callBacks;
    private final IBinder binder = new LocalBinder();
    SessionManager sessionManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sessionManager=new SessionManager(getApplicationContext());
        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());

       /* Log.d(TAG, "===========onCreate");
        sessionManager=new SessionManager(getApplicationContext());
        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

                //Toast.makeText(getApplicationContext(),s+"  "+mqttMessage.toString(),Toast.LENGTH_SHORT).show();
                Log.d("MQQT Service","From +  "+s+"   "+mqttMessage.toString());
                System.out.println("Main Activity Service================"+mqttMessage.toString());
                setMessageNotification(s, new String(mqttMessage.getPayload()));
                if(s.contains("Server_Status"))
                {
                    System.out.println("Spider hub will topic =========="+Constants.MQTT_BROKER_URL);
                    pahoMqttClient.disconnect(mqttAndroidClient);
                    System.out.println("Spider hub will topic 111=========="+Constants.MQTT_BROKER_URL);
                    System.out.println("Spider hub will topic 222=========="+Constants.MQTT_BROKER_URL);

                    callBacks.MessageArrivedFrom_GW_REG_RES_Topic(new JSONObject(mqttMessage.toString()));
                }
                else
                {
                    callBacks.MessageArrivedFrom_GW_AUTO_RES_Topic(new JSONObject(mqttMessage.toString()));
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "==========onStartCommand");

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

                //Toast.makeText(getApplicationContext(),s+"  "+mqttMessage.toString(),Toast.LENGTH_SHORT).show();
                Log.d("MQQT Service","From +  "+s+"   "+mqttMessage.toString());
                System.out.println("Main Activity Service================"+mqttMessage.toString());
                setMessageNotification(s, new String(mqttMessage.getPayload()));
                if(s.contains("Server_Status"))
                {
                    callBacks.MessageArrivedFrom_GW_REG_RES_Topic(new JSONObject(mqttMessage.toString()));
                }
                else {
                    if (mqttMessage.toString().contains("GATEWAY ONLINE"))
                    {
                            Constants.MQTT_BROKER_URL ="tcp://111.93.133.142:1883";
                            System.out.println("=========GW ONline Broker URL" + Constants.MQTT_BROKER_URL);
                            pahoMqttClient.disconnect(mqttAndroidClient);
                            mqttAndroidClient = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());
                            Toast.makeText(getApplicationContext(),"GW Online,conn. to Public IP",Toast.LENGTH_SHORT).show();
                                sessionManager.setOffline("false");

                    }
                    else if(mqttMessage.toString().contains("Gateway is Offline"))
                    {
                        try{
                            JSONObject obj=new JSONObject(mqttMessage.toString());
                            String ip=obj.getString("local_ip");
                            Constants.MQTT_BROKER_URL = "tcp://" + ip + ":1883";
                            Toast.makeText(getApplicationContext(),"GW Offline,change network",Toast.LENGTH_SHORT).show();
                            pahoMqttClient.disconnect(mqttAndroidClient);
                            mqttAndroidClient = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, sessionManager.getIMEI());


                        }
                        catch (Exception exp)
                        {
                            exp.printStackTrace();
                        }
                    }
                    else

                    callBacks.MessageArrivedFrom_GW_AUTO_RES_Topic(new JSONObject(mqttMessage.toString()));
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;

    }




    class LocalBinder extends Binder {
        MqttMessageService getService() {
            // Return this instance of MyService so clients can call public methods
            return MqttMessageService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "=============onDestroy");


    }

    private void setMessageNotification(@NonNull String topic, @NonNull String msg) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_message_black_24dp)
                        .setContentTitle(topic)
                        .setContentText(msg);
        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(100, mBuilder.build());
    }


    public void setCallbacks(CallBacks callbacks) {
        callBacks=callbacks;
    }

}
