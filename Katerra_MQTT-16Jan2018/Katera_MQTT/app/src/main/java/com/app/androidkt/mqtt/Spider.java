package com.app.androidkt.mqtt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;

/**
 * Created by ravikumar on 5/10/17.
 */

public class Spider extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    FloatingActionButton fab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.abc);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Gateway");

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        fab=(FloatingActionButton)findViewById(R.id.fab);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.addTab(tabLayout.newTab().setText("Living Room"));
        tabLayout.addTab(tabLayout.newTab().setText("Kitchen"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this,HomePage.class));
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id==R.id.homePage)
        {
            startActivity(new Intent(this,HomePage.class));
        }
        if (id == R.id.manage_User) {
            // Handle the camera action
            startActivity(new Intent(this,ManageUser.class));
        }  else if (id == R.id.heartBeat) {

            startActivity(new Intent(this,HeartBeat.class));
        }
        else if(id==R.id.bluetooth){
            startActivity(new Intent(this,Bluetooth.class));

        }else if (id == R.id.gateWay_Details) {
            //startActivity(new Intent(this,GatewayDeatils.class));

        }
        else if(id==R.id.camera)
        {
            startActivity(new Intent(this,Camera.class));
        }
        else if(id==R.id.ble)
        {
            startActivity(new Intent(this,BLE.class));
        }
        else if(id==R.id.Zigbee)
        {
            startActivity(new Intent(this,Zigbee.class));
        }
        else if(id==R.id.singOut)
        {
            Log.d("AC","Came Here ==========");
            Intent intent=new Intent(Spider.this,MainActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
 class ViewPagerAdapter extends FragmentPagerAdapter {
    Context context;
     String tabTitles[] = new String[] { "Living Room", "Kitchen" };
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        //return new LivingRoom();
        switch (position) {
            case 0:
                return new LivingRoom();
            case 1:
                return new Kitchen();

        }

        return null;
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }
     @Override
     public CharSequence getPageTitle(int position) {
         // Generate title based on item position
         return tabTitles[position];
     }
}
