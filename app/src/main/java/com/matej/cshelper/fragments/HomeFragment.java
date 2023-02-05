package com.matej.cshelper.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.matej.cshelper.R;
import com.matej.cshelper.core.InstanceProvider;
import com.matej.cshelper.fragments.helpers.OrderListController;
import com.matej.cshelper.fragments.helpers.OrderProcessingManager;
import com.matej.cshelper.network.redmine.RedmineConnector;
import com.matej.cshelper.network.redmine.entities.Order;
import com.matej.cshelper.storage.OrderProcess;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment implements OrderListController.RedmineOrdersLoadedCallback {

    private View preparationTile;
    private View buildTile;
    private View expTile;
    private HomeFragment instance;
    private View root;

    private static final String TAG = "HomeFragment";

    int buildNew = 0;
    int buildStarted = 0;
    int prepNew = 0;
    int prepStarted = 0;
    int expNew = 0;
    int expStarted = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        ViewGroup mainLayout = root.findViewById(R.id.home_main_layout);

        instance = this;
        preparationTile = inflater.inflate(R.layout.main_screen_tile, mainLayout, false);
        TextView title = preparationTile.findViewById(R.id.main_tile_title);
        title.setText("Preparation phase");
        TextView count = preparationTile.findViewById(R.id.main_tile_count);
        count.setText("0x NEW\n0x Started");
        mainLayout.addView(preparationTile);
        preparationTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putSerializable(OrdersFragment.ARG_STATE,OrdersFragment.State.PREPARATION);
                NavHostFragment.findNavController(instance).navigate(R.id.ordersFragment,args);
            }
        });

        buildTile = inflater.inflate(R.layout.main_screen_tile, mainLayout, false);
        title = buildTile.findViewById(R.id.main_tile_title);
        title.setText("Build phase");
        count = buildTile.findViewById(R.id.main_tile_count);
        count.setText("0x NEW\n0x Started");
        mainLayout.addView(buildTile);
        buildTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putSerializable(OrdersFragment.ARG_STATE,OrdersFragment.State.BUILD);
                NavHostFragment.findNavController(instance).navigate(R.id.ordersFragment,args);
            }
        });

        expTile = inflater.inflate(R.layout.main_screen_tile, mainLayout, false);
        title = expTile.findViewById(R.id.main_tile_title);
        title.setText("Expedition phase");
        count = expTile.findViewById(R.id.main_tile_count);
        count.setText("0x NEW\n0x Started");
        mainLayout.addView(expTile);
        expTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putSerializable(OrdersFragment.ARG_STATE,OrdersFragment.State.EXPEDITION);
                NavHostFragment.findNavController(instance).navigate(R.id.ordersFragment,args);
            }
        });

        OrderListController.Instance().setCallback(this);
        if(OrderListController.Instance().ActiveOrders != null)
            onLoaded();
        return root;
    }

    @Override
    public void onLoaded() {

        prepNew = prepStarted = buildStarted = buildNew = expNew = expStarted = 0;
        Log.d(TAG, "OnLoaded");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        for(Order order: OrderListController.Instance().ActiveOrders)
        {
            OrderProcessingManager.getInstance().GetOrder(order.TicketID, new OrderProcessingManager.GetOrderCallback() {
                @Override
                public void onGetOrderSuccess(OrderProcess orderProcess) {
                    ((TextView)root.findViewById(R.id.last_sync_time)).setVisibility(View.VISIBLE);
                    ((TextView)root.findViewById(R.id.last_sync_time)).setText("Last sync: " + dateFormatter.format(RedmineConnector.getInstance().LastSync));

                    switch (orderProcess.Status)
                    {
                        case NEW:
                            prepNew++;
                            break;
                        case COMPONENT_PREPARATION_START:
                            prepStarted++;
                            break;
                        case COMPONENT_PREPARATION_DONE:
                            buildNew++;
                            break;
                        case BUILD_START:
                            buildStarted++;
                            break;
                        case BUILD_DONE:
                            expNew++;
                            break;
                        case EXPORT_START:
                            expStarted++;
                            break;
                    }
                    ((TextView)buildTile.findViewById(R.id.main_tile_count)).setText(new StringBuilder().append(buildNew).append("x NEW\n").append(buildStarted).append("x Started").toString());
                    ((TextView)preparationTile.findViewById(R.id.main_tile_count)).setText(new StringBuilder().append(prepNew).append("x NEW\n").append(prepStarted).append("x Started").toString());
                    ((TextView)expTile.findViewById(R.id.main_tile_count)).setText(new StringBuilder().append(expNew).append("x NEW\n").append(expStarted).append("x Started").toString());
                }

                @Override
                public void onGetOrderFail(String message) {

                }
            });
        }
    }

}