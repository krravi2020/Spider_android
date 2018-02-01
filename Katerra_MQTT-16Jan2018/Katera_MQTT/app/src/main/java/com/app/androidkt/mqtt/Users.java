package com.app.androidkt.mqtt;

/**
 * Created by ravikumar on 21/9/17.
 */

public class Users {

        private String user_ID,time,gateway_ID;

        public Users(String gateway_ID, String user_ID,String time) {
            this.user_ID = user_ID;
            this.time = time;
            this.gateway_ID = gateway_ID;
        }

        public String getUser_ID() {
            return user_ID;
        }

        public void setUser_ID(String user_ID) {
            this.user_ID = user_ID;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getGateway_ID() {
            return gateway_ID;
        }

        public void setGateway_ID(String gateway_ID) {
            this.gateway_ID = gateway_ID;
        }

    }
