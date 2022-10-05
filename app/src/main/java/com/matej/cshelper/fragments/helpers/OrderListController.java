package com.matej.cshelper.fragments.helpers;

import android.util.Log;

import com.matej.cshelper.core.interfaces.InstanceBase;
import com.matej.cshelper.network.redmine.entities.Order;

import java.util.ArrayList;

public class OrderListController extends InstanceBase {
    private static String TAG = "OrderListController";

    public ArrayList<Order> ActiveOrders;

    public OrderListController()
    {
    }

    public OrderListController(ArrayList<Order> orders)
    {
        this.ActiveOrders = orders;
        Log.d(TAG, "Orders set! size: " + ActiveOrders.size());
    }

}
