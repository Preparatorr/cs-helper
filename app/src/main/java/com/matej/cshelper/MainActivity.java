package com.matej.cshelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.Navigation;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;
import com.matej.cshelper.core.InstanceProvider;
import com.matej.cshelper.fragments.OrdersFragment;
import com.matej.cshelper.fragments.helpers.OrderListController;
import com.matej.cshelper.fragments.helpers.OrderProcessingManager;
import com.matej.cshelper.network.OnFinishedCallback;
import com.matej.cshelper.network.firebase.FirebaseConnector;
import com.matej.cshelper.network.redmine.RedmineConnector;

public class MainActivity extends AppCompatActivity implements OnFinishedCallback {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    ActionBarDrawerToggle actionBarDrawerToggle;

    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.menu_open, R.string.menu_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                drawerLayout.closeDrawer(GravityCompat.START);
                Bundle args = new Bundle();
                switch (id) {
                    case R.id.nav_home:
                        Navigation.findNavController(findViewById(R.id.fragmentContainerView)).navigate(R.id.homeFragment,new Bundle());
                        break;
                    case R.id.nav_orders:
                        args.putSerializable(OrdersFragment.ARG_STATE,OrdersFragment.State.BUILD);
                        Navigation.findNavController(findViewById(R.id.fragmentContainerView)).navigate(R.id.ordersFragment,args);
                        break;
                    case R.id.nav_settings:
                        Navigation.findNavController(findViewById(R.id.fragmentContainerView)).navigate(R.id.settingsFragment);
                        break;
                    case R.id.nav_prepearation:
                        args.putSerializable(OrdersFragment.ARG_STATE,OrdersFragment.State.PREPARATION);
                        Navigation.findNavController(findViewById(R.id.fragmentContainerView)).navigate(R.id.ordersFragment,args);
                        break;
                }
                return true;
            }
        });

        FirebaseConnector.getInstance().getKeys(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        ((OrderProcessingManager)InstanceProvider.GetInstance(OrderProcessingManager.class)).saveOrders();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void setActionBarTitle(String title){
        setTitle(title);
    }

    public static Context getContext() {
        return context;
    }

    @Override
    public void OnFinished() {
        InstanceProvider.RegisterInstance(new OrderListController(RedmineConnector.getInstance().GetLatestOrders()));
        InstanceProvider.RegisterInstance(new OrderProcessingManager());
        //((OrderListController)InstanceProvider.GetInstance(OrderListController.class));
    }
}