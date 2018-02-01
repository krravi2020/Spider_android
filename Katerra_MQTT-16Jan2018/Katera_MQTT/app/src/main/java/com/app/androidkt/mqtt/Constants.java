package com.app.androidkt.mqtt;

import android.app.Service;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static android.content.Context.WIFI_SERVICE;


public class Constants {
    //tcp://192.168.43.236:1883
    public static  String MQTT_BROKER_URL = "tcp://111.93.133.142:1883";
    /*public static  String MQTT_BROKER_URL = "tcp://192.168.100.201:1883";*/
    /*public static final String PUBLISH_TOPIC = "Spider/";
    public static final String CLIENT_ID = "test1";*/
    public static final String[] RESPONSE_TOPIC = {"867512024697142/Server_Status","GW_REG_RES/#", "DEV_CONFIG_RES/#",
            "GW_USRID_RES/12345", "DEV_NOTIFY_RES/#",
            "GW_HB_RES/#", "GW_AUTO_RES/#", "DEV_DEL_RES/#",
            "GW_DETLS_RES/#", "DEV_REG_RES2USR/#", "DEV_LIST_RES/#", "DEV_PARAM_RES/12345",
            "SER_NOTIF_RES/#", "Spider_Hub_will_topic"};

    public String GenerateRandomNumber() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(10000);
        String formatted = String.format("%05d", num);
        return formatted;
    }

    public String GenerateUniquePassword() {
        char[] CHARSET_AZ_09 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        Random random = new Random();
        char[] result = new char[6];
        for (int i = 0; i < result.length; i++) {
            // picks a random index out of character set > random character
            int randomCharIndex = random.nextInt(CHARSET_AZ_09.length);
            result[i] = CHARSET_AZ_09[randomCharIndex];
        }
        return new String(result);
    }

    public String getTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy hh:mm:ss");
        String strDate = sdf.format(date);
        return strDate;
    }



}

