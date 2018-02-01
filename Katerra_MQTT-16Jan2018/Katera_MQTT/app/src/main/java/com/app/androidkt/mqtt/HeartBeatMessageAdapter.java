package com.app.androidkt.mqtt;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sushree on 16/1/18.
 */

public class HeartBeatMessageAdapter extends RecyclerView.Adapter<HeartBeatMessageAdapter.MyViewHolder>{

    public List<Gatewaypojo> objectgateway;

    public HeartBeatMessageAdapter(List<Gatewaypojo> objectgateway){
        this.objectgateway = objectgateway;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View heartbeatview = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.heartbeatlayout,parent,false);
        return new MyViewHolder(heartbeatview);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Gatewaypojo gatewaypojo = objectgateway.get(position);
        holder.gateway_id.setText(gatewaypojo.getGatewayid());
        holder.time_stamp.setText(gatewaypojo.getTimestamp());
        holder.response_message.setText(gatewaypojo.getMessage());
        holder.user_id.setText(gatewaypojo.getTransid());
    }

    @Override
    public int getItemCount() {
        return objectgateway.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView gateway_id,time_stamp,response_message,user_id;
        public MyViewHolder(View itemView) {
            super(itemView);
            gateway_id = (TextView) itemView.findViewById(R.id.txt_gatewayid);
            time_stamp = (TextView) itemView.findViewById(R.id.txt_timestamp);
            response_message = (TextView) itemView.findViewById(R.id.txt_response);
            user_id = (TextView) itemView.findViewById(R.id.txt_userid);
        }
    }
}
