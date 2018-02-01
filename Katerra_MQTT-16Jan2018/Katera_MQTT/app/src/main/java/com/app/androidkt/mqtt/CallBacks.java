package com.app.androidkt.mqtt;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ravikumar on 12/9/17.
 */

public interface CallBacks {

    public void SubscribeTopics(MqttAndroidClient mqttAndroidClient) throws MqttException;
    public  void MessageArrivedFrom_GW_REG_RES_Topic(JSONObject obj) throws JSONException;
    public void MessageArrivedFrom_GW_HB_RES_Topic(JSONObject obj) throws JSONException;
    public void MessageArrivedFrom_DEV_NOTIFY_RES_Topic(JSONObject obj) throws JSONException;
    public void MessageArrivedFrom_GW_AUTO_RES_Topic(JSONObject obj) throws JSONException;
    public  void  MessageArrivedFrom_DEV_REG_RES(JSONObject obj) throws JSONException;

}
