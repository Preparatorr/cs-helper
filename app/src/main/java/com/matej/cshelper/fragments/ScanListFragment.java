package com.matej.cshelper.fragments;

import static com.matej.cshelper.fragments.OrderScanFragment.ARG_TICKET_ID;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.matej.cshelper.R;
import com.matej.cshelper.fragments.helpers.OrderScanController;


public class ScanListFragment extends Fragment {

    private View root;
    private ViewGroup list;
    private ScanListFragment instance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_scan_list, container, false);
        list = root.findViewById(R.id.scan_list);
        for(String ticketId : OrderScanController.getInstance().getOrders().keySet())
        {
            View item = inflater.inflate(R.layout.scan_list_item, list, false);
            ((TextView)item.findViewById(R.id.ticket_number_text)).setText(ticketId);
            item.setOnClickListener(view -> {
                Bundle args = new Bundle();
                args.putString(ARG_TICKET_ID, ticketId);
                NavHostFragment.findNavController(instance).navigate(R.id.orderScanFragment,args);
            });

            list.addView(item);
        }

        root.findViewById(R.id.button_new_scan).setOnClickListener(view -> {
            Bundle args = new Bundle();
            NavHostFragment.findNavController(instance).navigate(R.id.orderScanFragment,args);
        });
        return root;
    }
}