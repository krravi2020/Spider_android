package com.app.androidkt.mqtt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ravikumar on 4/1/18.
 */

public class NetworkStateReceiver extends BroadcastReceiver {

    protected Set<NetworkStateReceiverListener> listeners;
    protected Boolean connected;

    public NetworkStateReceiver() {
        listeners = new HashSet<NetworkStateReceiverListener>();
        connected = null;
    }

    public void onReceive(final Context context, Intent intent) {

        final String action = intent.getAction();
        System.out.println("===================Action   "+action);
        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = manager.getActiveNetworkInfo();
        if(ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {

            String command = "ping -c 1 google.com";
            try {
                connected=(Runtime.getRuntime().exec (command).waitFor()==0);
                System.out.println("Called Here==========1111"+connected);
            } catch (InterruptedException e) {
                e.printStackTrace();
                connected = false;
                notifyStateToAll();
            } catch (IOException e) {
                e.printStackTrace();
                connected = false;
                notifyStateToAll();
            }
        } else if(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
            connected = false;
        }
        notifyStateToAll();
    }

    private void notifyStateToAll() {
        System.out.println("NOtofify All========="+connected);
        for(NetworkStateReceiverListener listener : listeners)
            notifyState(listener);
    }

    private void notifyState(NetworkStateReceiverListener listener) {
        System.out.println("NOtofify State========="+connected);
        if(connected == null || listener == null)
            return;
        if(connected == true)
            listener.networkAvailable();
        else
            listener.networkUnavailable();
    }

    public void addListener(NetworkStateReceiverListener l) {
        listeners.add(l);
        notifyState(l);
    }

    public void removeListener(NetworkStateReceiverListener l) {
        listeners.remove(l);
    }

    public interface NetworkStateReceiverListener {
        public void networkAvailable();
        public void networkUnavailable();
    }
}
