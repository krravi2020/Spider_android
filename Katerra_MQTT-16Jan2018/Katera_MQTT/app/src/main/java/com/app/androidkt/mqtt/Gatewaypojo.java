package com.app.androidkt.mqtt;

/**
 * Created by sushree on 16/1/18.
 */

public class Gatewaypojo {
    private String gatewayid,transid,message,timestamp,user_id;
    public Gatewaypojo(){

    }
    public Gatewaypojo(String gatewayid, String transid, String message, String timestamp,String user_id){
        this.gatewayid = gatewayid;
        this.transid = transid;
        this.message = message;
        this.timestamp = timestamp;
        this.user_id = user_id;
    }

    public String getGatewayid() {
        return gatewayid;
    }

    public void setGatewayid(String gatewayid) {
        this.gatewayid = gatewayid;
    }

    public String getTransid() {
        return transid;
    }

    public void setTransid(String transid) {
        this.transid = transid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
