package com.app.androidkt.mqtt;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by ravikumar on 10/10/17.
 */
class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.MyViewHolder>{


    public List<Device> moviesList;
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_list_row, parent, false);

        return new MyViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        Device movie = moviesList.get(position);
        holder.device_manufacturer.setText(movie.getDevice_manufacturer());
        holder.device_type.setText(movie.getDevice_type());
        holder.deviceName.setText(movie.getDeviceName());
        String device=movie.getTypeOfDevice();
        System.out.println("============= Inside Adapter111"+device);
        if(device.equalsIgnoreCase("Water"))
        {
            System.out.println("=============Water"+device);
            holder.device_icon.setImageResource(android.R.color.transparent);
//            holder.device_icon.setImageResource(R.drawable.water);
            holder.device_icon.setImageResource(R.drawable.ic_watersensor);
            holder.status.setText(movie.getStatus());
            if(movie.getStatus().equalsIgnoreCase("Leak"))
                holder.status.setTextColor(Color.GREEN);
            else
                holder.status.setTextColor(Color.BLACK);
        }
        else if (device.equalsIgnoreCase("Switch"))
        {

            holder.status.setVisibility(View.VISIBLE);
            holder.status.setTextColor(Color.GREEN);
            System.out.println("=============Switch"+device);
            holder.device_icon.setImageResource(android.R.color.transparent);
            holder.device_icon.setImageResource(R.drawable.bell);
            if(movie.getStatus().equalsIgnoreCase("Ring"))
            {
                holder.status.setVisibility(View.VISIBLE);
                holder.status.setText("Ring");
            }
            else
            {
                holder.status.setVisibility(View.GONE);
            }

        }
        else if(device.equalsIgnoreCase("MultiSensor"))
        {
            System.out.println("============= Inside Adapter1 MultiSensor"+device);
            holder.device_icon.setImageResource(android.R.color.transparent);
//            holder.device_icon.setImageResource(R.drawable.multiicon);
            holder.device_icon.setImageResource(R.drawable.ic_sensor);
            holder.status.setVisibility(View.GONE);
            if(movie.getBurglarValue().equalsIgnoreCase("3"))
            {
                System.out.println(movie.getBurglarValue()+" Inside Adapter1 ================33");
                holder.status.setText("Motion");
                holder.status.setTextColor(Color.RED);
                holder.status.setVisibility(View.VISIBLE);
                holder.status.postDelayed(new Runnable() {
                    public void run() {
                        holder.status.setText("");
                        holder.status.setVisibility(View.INVISIBLE);
                    }
                }, 3000);
            }
            else
            {
                holder.status.setVisibility(View.INVISIBLE);
            }

        }
        else if(device.equalsIgnoreCase("DoorBell"))
        {
            System.out.println("Inside Adapter1 =============DoorBell"+device);
            holder.device_icon.setImageResource(android.R.color.transparent);
            holder.device_icon.setImageResource(R.drawable.closeicon);
            holder.status.setText(movie.getStatus());
            if(movie.getStatus().equalsIgnoreCase("Open")) {
                holder.status.setTextColor(Color.GREEN);
                holder.status.setVisibility(View.VISIBLE);
                holder.device_icon.setImageResource(android.R.color.transparent);
                holder.device_icon.setImageResource(R.drawable.openicon);
            }
            else{
                holder.status.setVisibility(View.VISIBLE);
                holder.status.setTextColor(Color.BLACK);
                holder.device_icon.setImageResource(android.R.color.transparent);
                holder.device_icon.setImageResource(R.drawable.closeicon);
            }

        }

    }


    @Override
    public int getItemCount() {
        return moviesList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        public MyViewHolder(final View view) {
            super(view);
            device_type = (TextView) view.findViewById(R.id.device_type);
            device_manufacturer = (TextView) view.findViewById(R.id.device_manufacturer);
            deviceName=(TextView)view.findViewById(R.id.deviceName);
            device_icon=(ImageView)view.findViewById(R.id.device_icon);
            status=(TextView)view.findViewById(R.id.status1);

        }
        public TextView device_type,deviceName,device_manufacturer,status;
        ImageView device_icon;
    }

    public DeviceAdapter(List<Device> moviesList)
    {
        this.moviesList = moviesList;
    }

}