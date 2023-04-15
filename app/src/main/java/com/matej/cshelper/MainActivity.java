package com.matej.cshelper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.Navigation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.matej.cshelper.core.InstanceProvider;
import com.matej.cshelper.fragments.OrderScanFragment;
import com.matej.cshelper.fragments.OrdersFragment;
import com.matej.cshelper.fragments.helpers.OrderListController;
import com.matej.cshelper.fragments.helpers.OrderProcessingManager;
import com.matej.cshelper.network.OnFinishedCallback;
import com.matej.cshelper.network.firebase.FirebaseConnector;
import com.matej.cshelper.network.redmine.RedmineConnector;
import com.matej.cshelper.settings.UserManager;

public class MainActivity extends AppCompatActivity implements OnFinishedCallback {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;

    private static Context context;
    private static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        instance = this;
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
                    case R.id.nav_expedition:
                        args.putSerializable(OrdersFragment.ARG_STATE,OrdersFragment.State.EXPEDITION);
                        Navigation.findNavController(findViewById(R.id.fragmentContainerView)).navigate(R.id.ordersFragment,args);
                        break;
                    case R.id.nav_scanner:
                        Navigation.findNavController(findViewById(R.id.fragmentContainerView)).navigate(R.id.scanFragment);
                        break;
                    case R.id.nav_order_scan:
                        Navigation.findNavController(findViewById(R.id.fragmentContainerView)).navigate(R.id.orderScanFragment);
                        break;
                }
                return true;
            }
        });

        FirebaseConnector.getInstance().getKeys(this);
        if(!(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED))
        {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
        ((TextView)findViewById(R.id.user_text_view)).setText("User: " + UserManager.getInstance().getCurrentUser().Name);
    }

    @Override
    protected void onPause() {
        super.onPause();
        OrderProcessingManager.getInstance().saveOrders();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId())
        {
            case R.id.redmine_refresh:
                OrderListController.Instance().setActiveOrders(RedmineConnector.getInstance().GetLatestOrders());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer_menu,menu);

        return true;
    }

    public void setActionBarTitle(String title){
        setTitle(title);
    }

    public static Context getContext() {
        return context;
    }
    public static MainActivity getInstance() {
        return instance;
    }

    public void refreshUser()
    {
        ((TextView)findViewById(R.id.user_text_view)).setText("User: " + UserManager.getInstance().getCurrentUser().Name);
    }

    @Override
    public void OnFinished() {

        OrderListController.Instance().setActiveOrders(RedmineConnector.getInstance().GetLatestOrders());
        //((OrderListController)InstanceProvider.GetInstance(OrderListController.class));
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });

}