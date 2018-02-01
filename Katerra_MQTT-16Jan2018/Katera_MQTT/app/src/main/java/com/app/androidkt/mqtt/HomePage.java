package com.app.androidkt.mqtt;

import android.app.ProgressDialog;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;

import android.view.Menu;
import android.widget.LinearLayout;



public class HomePage extends AppCompatActivity {
    ProgressDialog process ;
    //CardView cardView;
    LinearLayout cardView;
    Toolbar toolbar_home;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

//        cardView = (CardView) findViewById(R.id.card_view);
        cardView = (LinearLayout) findViewById(R.id.card_view);
        toolbar_home = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar_home);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("List Of Spider");

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                process = new ProgressDialog(HomePage.this);
                process.setMessage("fetching device .....");
                process.show();
                process.setCancelable(false);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        process.dismiss();
                    }
                }, 2000);
                Intent intent = new Intent(view.getContext(), Spider.class);
                startActivity(intent);
            }
        });
       /* Explode explode = new Explode();
        explode.setDuration(500);
        getWindow().setExitTransition(explode);
        getWindow().setEnterTransition(explode);*/

    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       /* getMenuInflater().inflate(R.menu.home_page, menu);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
