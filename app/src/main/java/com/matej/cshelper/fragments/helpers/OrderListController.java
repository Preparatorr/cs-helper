package com.matej.cshelper.fragments.helpers;

import android.content.DialogInterface;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;

import com.matej.cshelper.MainActivity;
import com.matej.cshelper.R;
import com.matej.cshelper.core.interfaces.InstanceBase;
import com.matej.cshelper.network.redmine.entities.Order;

import java.util.ArrayList;

public class OrderListController{

    public interface RedmineOrdersLoadedCallback
    {
        void onLoaded();
    }

    private RedmineOrdersLoadedCallback callback;
    private static String TAG = "OrderListController";
    private static OrderListController instance;

    public ArrayList<Order> ActiveOrders;

    private OrderListController()
    {
        ActiveOrders = new ArrayList<>();
    }

    public static OrderListController Instance()
    {
        if (instance == null)
            instance = new OrderListController();
        return instance;
    }

    public void setActiveOrders(ArrayList<Order> orders)
    {
        this.ActiveOrders = orders;
        Log.d(TAG, "Orders set! size: " + ActiveOrders.size());
        callback.onLoaded();
    }


    public void setCallback(RedmineOrdersLoadedCallback callback) {
        this.callback = callback;
    }
}
