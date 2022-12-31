package com.matej.cshelper.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.matej.cshelper.R;
import com.matej.cshelper.core.InstanceProvider;
import com.matej.cshelper.fragments.helpers.OrderListController;
import com.matej.cshelper.network.redmine.entities.Order;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        ViewGroup mainLayout = root.findViewById(R.id.home_main_layout);

        OrderListController controller = (OrderListController) InstanceProvider.GetInstance(OrderListController.class);
        
        for(Order order : controller.ActiveOrders)
        {

        }

        View preparationTile = inflater.inflate(R.layout.main_screen_tile, mainLayout, false);
        TextView title = preparationTile.findViewById(R.id.main_tile_title);
        title.setText("Preparation phase");
        TextView count = preparationTile.findViewById(R.id.main_tile_count);
        count.setText("5x NEW\n1x Started");
        mainLayout.addView(preparationTile);

        View buildTile = inflater.inflate(R.layout.main_screen_tile, mainLayout, false);
        title = preparationTile.findViewById(R.id.main_tile_title);
        title.setText("Build phase");
        count = preparationTile.findViewById(R.id.main_tile_count);
        count.setText("5x NEW\n1x Started");
        mainLayout.addView(buildTile);

        return root;
    }

    public HomeFragment()
    {

    }
}