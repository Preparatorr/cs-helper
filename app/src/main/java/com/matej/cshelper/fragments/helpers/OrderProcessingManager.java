package com.matej.cshelper.fragments.helpers;

import static com.matej.cshelper.MainActivity.getContext;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.matej.cshelper.MainActivity;
import com.matej.cshelper.core.InstanceProvider;
import com.matej.cshelper.core.interfaces.InstanceBase;
import com.matej.cshelper.network.redmine.entities.Order;
import com.matej.cshelper.storage.OrderProcess;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class OrderProcessingManager extends InstanceBase {

    private static String TAG = "OrderProcessingManager";
    private HashMap<String, OrderProcess> orders;

    public OrderProcessingManager()
    {
        this.orders = new HashMap<>();
        loadOrders();
    }

    public OrderProcess GetOrder(String ticketID)
    {
        if(this.orders == null)
            orders = new HashMap<>();
        if(this.orders.containsKey(ticketID))
            return this.orders.get(ticketID);
        else
        {
            ArrayList<Order> orderList = ((OrderListController)InstanceProvider.GetInstance(OrderListController.class)).ActiveOrders;
            for(Order o : orderList)
            {
                if(o.TicketID.equals(ticketID))
                {
                    OrderProcess process = new OrderProcess(o);
                    orders.put(ticketID,process);
                    return process;
                }
            }
        }
        return null;
    }

    public void saveOrders()
    {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("orders", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String json = gson.toJson(this.orders);
        //Log.d(TAG,json);
        editor.putString("SavedOrders", json);
        editor.commit();

    }

    private void loadOrders()
    {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("orders", 0);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("SavedOrders", "");
        Type type = new TypeToken<HashMap<String, OrderProcess>>(){}.getType();
        this.orders = gson.fromJson(json,type);
    }
}
