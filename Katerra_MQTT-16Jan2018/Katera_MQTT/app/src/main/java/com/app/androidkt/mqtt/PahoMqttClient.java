package com.app.androidkt.mqtt;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;


public class PahoMqttClient  implements CallBacks{


    CallBacks callbacks;
    MainActivity newMainActivity=new MainActivity();
    private static final String TAG = "PahoMqttClient";
    private  MqttAndroidClient mqttAndroidClient;

    Context context;



    public MqttAndroidClient getMqttClient(final Context context, String brokerUrl, String clientId) {
        this.context=context;
        mqttAndroidClient = new MqttAndroidClient(context, brokerUrl, clientId);
        try {
            IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());
                    Log.d(TAG, "Success");
                    System.out.println("Connection Success============"+Constants.MQTT_BROKER_URL);
                   //Toast.makeText(context,"Paho Connection Established",Toast.LENGTH_SHORT).show();
                    try {

                        SubscribeTopics(mqttAndroidClient);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                   // Toast.makeText(context,"Paho Connection Failed",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Failure " + exception.toString());
                    System.out.println("Connection Failed============"+Constants.MQTT_BROKER_URL);
                    exception.printStackTrace();
                    //Toast.makeText(context,"Paho Connection Failed",Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        return mqttAndroidClient;
    }

    public void disconnect(@NonNull MqttAndroidClient client) throws MqttException {
        IMqttToken mqttToken = client.disconnect();
        mqttToken.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "Successfully disconnected");
                System.out.println("Discconnected ============"+Constants.MQTT_BROKER_URL);
               // getMqttClient(context,Constants.MQTT_BROKER_URL,new SessionManager(context).getIMEI());
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.d(TAG, "Failed to disconnected " + throwable.toString());
            }
        });
    }

    @NonNull
    private DisconnectedBufferOptions getDisconnectedBufferOptions() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(false);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        return disconnectedBufferOptions;
    }

    @NonNull
    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true);
       // mqttConnectOptions.setAutomaticReconnect(true);
        //mqttConnectOptions.setWill(Constants.PUBLISH_TOPIC, "I am going offline".getBytes(), 1, true);
        //mqttConnectOptions.setUserName("ngbllzzy");
        //mqttConnectOptions.setPassword("WtjhZKl3OPoK".toCharArray());
        return mqttConnectOptions;
    }


    public void publishMessage(@NonNull MqttAndroidClient client, @NonNull String msg, int qos, @NonNull String topic)
            throws MqttException, UnsupportedEncodingException {
        byte[] encodedPayload = new byte[0];
        encodedPayload = msg.getBytes("UTF-8");
        MqttMessage message = new MqttMessage(encodedPayload);
        message.setId(320);
        message.setRetained(false);
        message.setQos(qos);
        client.publish(topic, message);
    }

    public void subscribe(@NonNull final MqttAndroidClient client, @NonNull final String topic, final int qos) throws MqttException {

        IMqttToken token = client.subscribe(topic, qos);
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "MQTT Authenticated " + topic);
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.e(TAG, "Subscribe Failed " + topic);
            }
        });
    }

    public void unSubscribe(@NonNull MqttAndroidClient client, @NonNull final String topic) throws MqttException {

        IMqttToken token = client.unsubscribe(topic);

        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "UnSubscribe Successfully " + topic);
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.e(TAG, "UnSubscribe Failed " + topic);
            }
        });
    }

    @Override
    public void SubscribeTopics(final MqttAndroidClient mqttAndroidClient) throws MqttException {

        for(int i=0;i<Constants.RESPONSE_TOPIC.length;i++){
            final String topic1=Constants.RESPONSE_TOPIC[i];
        final IMqttToken token = mqttAndroidClient.subscribe(topic1, 2);
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "MQTT Authenticated " + topic1);
                System.out.println("Topic Subscribed ============"+topic1);
                //Toast.makeText(context,"Subscribed"+topic1,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.e(TAG, "Subscribe Failed " + topic1);
                System.out.println("Topic Failed ============"+topic1);
                /*try {
                    mqttAndroidClient.subscribe(topic1,1);
                } catch (MqttException e) {
                    e.printStackTrace();
                }*/
            }
        });
        }

    }

    @Override
    public void MessageArrivedFrom_GW_REG_RES_Topic(JSONObject obj) {

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
}


