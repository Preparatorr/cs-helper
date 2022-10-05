package com.matej.cshelper.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    private OrderListController controller;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ((MainActivity) getActivity()).setActionBarTitle("Redmine orders");
        View root = inflater.inflate(R.layout.fragment_orders, container, false);
        LinearLayout mainLayout = root.findViewById(R.id.orders_list_layout);
        this.controller = (OrderListController) InstanceProvider.GetInstance(OrderListController.class);
        for(Order order : this.controller.ActiveOrders)
        {
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
            OrderProcess preocessedOrder = ((OrderProcessingManager)InstanceProvider.GetInstance(OrderProcessingManager.class)).GetOrder(order.TicketID);
            orderStatus.setText(preocessedOrder.Status.toString());
            switch(preocessedOrder.Status){
                case DONE:
                    orderStatus.setTextColor(getResources().getColor(R.color.green));
                    break;
                case IN_PROGRESS:
                    orderStatus.setTextColor(getResources().getColor(R.color.orange));
                default:
                    break;
            }
            mainLayout.addView(item);
        }

        return root;
    }

    private void openOrder(Bundle args)
    {
        NavHostFragment.findNavController(this).navigate(R.id.orderProcessingFragment, args);
    }
}