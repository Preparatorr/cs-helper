package com.matej.cshelper.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.matej.cshelper.MainActivity;
import com.matej.cshelper.R;
import com.matej.cshelper.core.InstanceProvider;
import com.matej.cshelper.fragments.helpers.OrderListController;
import com.matej.cshelper.fragments.helpers.OrderProcessingManager;
import com.matej.cshelper.network.redmine.entities.Order;
import com.matej.cshelper.storage.OrderProcess;

public class OrdersFragment extends Fragment {

    public enum State
    {
        PREPARATION,
        BUILD,
        CHECK,
        EXPEDITION
    }
    public static final String ARG_STATE = "state";
    private State state;
    private OrderListController controller;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            this.state = (State)getArguments().getSerializable(ARG_STATE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        switch(this.state)
        {
            case PREPARATION:
                ((MainActivity) getActivity()).setActionBarTitle("Orders for preparation");
                break;
            case BUILD:
                ((MainActivity) getActivity()).setActionBarTitle("Orders for build");
                break;
            default:
                ((MainActivity) getActivity()).setActionBarTitle("Redmine orders");
        }

        View root = inflater.inflate(R.layout.fragment_orders, container, false);
        LinearLayout mainLayout = root.findViewById(R.id.orders_list_layout);
        this.controller = (OrderListController) InstanceProvider.GetInstance(OrderListController.class);
        for(Order order : this.controller.ActiveOrders)
        {
            OrderProcess preocessedOrder = ((OrderProcessingManager)InstanceProvider.GetInstance(OrderProcessingManager.class)).GetOrder(order.TicketID);
            boolean ignore = false;
            switch (this.state){
                case BUILD:
                    if(preocessedOrder.Status != OrderProcess.OrderStatus.COMPONENT_PREPARATION_DONE && preocessedOrder.Status != OrderProcess.OrderStatus.BUILD_START)
                        ignore = true;
                    break;
                case PREPARATION:
                    if(preocessedOrder.Status != OrderProcess.OrderStatus.NEW && preocessedOrder.Status != OrderProcess.OrderStatus.COMPONENT_PREPARATION_START)
                        ignore = true;
                default:
                    break;
            }

            if(ignore)
                continue;
            View item = inflater.inflate(R.layout.order_list_item,(ViewGroup) root,false);
            TextView ticketID = item.findViewById(R.id.order_ticket_id);
            ticketID.setText(order.TicketID);
            TextView orderID = item.findViewById(R.id.order_order_id);
            orderID.setText(order.OrderID);
            TextView company = item.findViewById(R.id.order_company);
            company.setText(order.Company);

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle data = new Bundle();
                    data.putString(OrderProcessingFragment.ARG_TICKET_ID,order.TicketID);
                    openOrder(data);
                }
            });

            TextView orderStatus = item.findViewById(R.id.order_status);
            orderStatus.setText(preocessedOrder.Status.toString());
            switch(preocessedOrder.Status){
                case BUILD_DONE:
                    orderStatus.setTextColor(getResources().getColor(R.color.green));
                    break;
                case BUILD_START:
                    orderStatus.setTextColor(getResources().getColor(R.color.orange));
                default:
                    break;
            }
            mainLayout.addView(item);
        }
        if(this.controller.ActiveOrders.size() == 0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.myDialog));
            // Add the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                }
            });
            builder.setTitle("No connection to Redmine! Please restart app");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return root;
    }

    private void openOrder(Bundle args)
    {
        switch(this.state)
        {
            case PREPARATION:
                NavHostFragment.findNavController(this).navigate(R.id.componentPreparationFragment, args);
                break;
            case BUILD:
                NavHostFragment.findNavController(this).navigate(R.id.orderProcessingFragment, args);
                break;
        }

    }
}