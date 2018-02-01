package com.app.androidkt.mqtt;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ravikumar on 13/9/17.
 */

public class SessionManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;
    Context con;


    private static final String Offline="Offline";
    private static final String User_ID="User_ID";
    private static final  String Device_Name="Device_Name";
    private static final String DeviceDetails="DeviceDetails";
    private static final String PREF_NAME = "kattera_device";
    private static final String Gateway_ID="Gateway_ID";
    private static final String Users_Details="Users_Details";
    private static final String IMEI="IMEI";
    private static final String isEnabled="true";
    private static final String BLEEnabled="true";
    private static final String ZigbeeEnabled="true";
    public void setOffline(String string)
    {
        editor.putString(Offline,string);
        editor.commit();
    }
    public String getOffline()
    {
        return  pref.getString(Offline,"false");
    }
    public String getBLEEnabled()
    {
        return pref.getString(BLEEnabled,"false");
    }
    public void setBLEEnabled(String a)
    {
        editor.putString(BLEEnabled,a);
        editor.commit();
    }
    public String getZigbeeEnabled()
    {
        return pref.getString(ZigbeeEnabled,"false");
    }
    public  void setZigbeeEnabled(String a)
    {
        editor.putString(ZigbeeEnabled,a);
        editor.commit();
    }

    public  String getIsEnabled() {
        return pref.getString(isEnabled,"false");

    }
    public void setIsEnabled(String a)
    {
        editor.putString(isEnabled,a);
        editor.commit();
    }

    public String getIMEI()
    {
        return pref.getString(IMEI,"");
    }
    public void setIMEI(String str)
    {
        editor.putString(IMEI,str);
        editor.commit();
    }

    public  String getUsers_Details() {
        return pref.getString(Users_Details,"");
    }

    public  void set_Users_Details(String str)
    {
        editor.putString(Users_Details,str);
        editor.commit();
    }
    public String getUser_ID(){return pref.getString("User_ID","");}
    public  void setUser_ID(String str)
    {
        editor.putString(User_ID,str);
        editor.commit();
    }
    public String getGateway_ID(){ return  pref.getString("Gateway_ID","");}
    public void setGateway_ID(String str)
    {
        editor.putString(Gateway_ID,str);
        editor.commit();
    }

    public  String getDevice_Name() {
        return pref.getString(Device_Name,"");
    }
    public void setDevice_Name(String name)
    {
        editor.putString(Device_Name,name);
        editor.commit();
    }
    public  String get_DeviceDetails() {
        return pref.getString(DeviceDetails,"");
    }
    public void set_DeviceDetails(String name)
    {
        editor.putString(DeviceDetails,name);
        editor.commit();
    }

    public SessionManager(Context context){

        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.commit();
    }

}
