package com.app.androidkt.mqtt;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;

/**
 * Created by ravikumar on 10/10/17.
 */

public class DeviceService extends Service {
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    private CallBacks callBacks;
    private final IBinder binder = new com.app.androidkt.mqtt.DeviceService.LocalBinder();
    SessionManager sessionManager;
    private String TAG = "Device Service";
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreateCommand ");
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
                System.out.println("Living Room Service======================="+mqttMessage.toString());
                Log.d("Device Service","From +  "+s+"   "+mqttMessage.toString());
                setMessageNotification(s, new String(mqttMessage.getPayload()));
                if(s.contains("DEV_REG_RES2USR") || s.contains("DEV_DEL_RES") || s.contains("DEV_LIST_RES") ||s.contains("SER_NOTIF_RES"))
                {

                    callBacks.MessageArrivedFrom_DEV_REG_RES(new JSONObject(mqttMessage.toString()));
                }
                if(s.contains("DEV_NOTIFY_RES"))
                {
                    callBacks.MessageArrivedFrom_DEV_NOTIFY_RES_Topic(new JSONObject(mqttMessage.toString()));
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {


            }
        });
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
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
                System.out.println("Living Room Service======================="+mqttMessage.toString());
                Log.d("Device Service","From +  "+s+"   "+mqttMessage.toString());
                setMessageNotification(s, new String(mqttMessage.getPayload()));
                if(s.contains("DEV_REG_RES2USR") || s.contains("DEV_DEL_RES") || s.contains("DEV_LIST_RES") ||s.contains("SER_NOTIF_RES"))
                {

                    callBacks.MessageArrivedFrom_DEV_REG_RES(new JSONObject(mqttMessage.toString()));
                }
                if(s.contains("DEV_NOTIFY_RES"))
                {
                    callBacks.MessageArrivedFrom_DEV_NOTIFY_RES_Topic(new JSONObject(mqttMessage.toString()));
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
        DeviceService getService() {
            // Return this instance of MyService so clients can call public methods
            return DeviceService.this;
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
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
