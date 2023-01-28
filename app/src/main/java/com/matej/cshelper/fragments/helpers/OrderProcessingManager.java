package com.matej.cshelper.fragments.helpers;

import static com.matej.cshelper.MainActivity.getContext;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.matej.cshelper.MainActivity;
import com.matej.cshelper.config.SecretKeys;
import com.matej.cshelper.core.InstanceProvider;
import com.matej.cshelper.core.interfaces.InstanceBase;
import com.matej.cshelper.network.firebase.entities.GlobalComponentsConfig;
import com.matej.cshelper.network.redmine.entities.Order;
import com.matej.cshelper.storage.OrderProcess;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderProcessingManager{

    public interface GetOrderCallback
    {
        void onGetOrderSuccess(OrderProcess order);
        void onGetOrderFail(String message);
    }

    private static OrderProcessingManager instance;
    private static String TAG = "OrderProcessingManager";
    private HashMap<String, OrderProcess> orders;

    private FirebaseFirestore db = null;

    private OrderProcessingManager()
    {
        this.orders = new HashMap<>();
        //loadOrders();
        try
        {
            this.db = FirebaseFirestore.getInstance();
        }
        catch (Exception e )
        {
            Log.e(TAG,e.toString());
        }
    }

    public static OrderProcessingManager getInstance()
    {
        if(instance == null)
            instance = new OrderProcessingManager();
        return instance;
    }

    public void saveFirebaseOrder(OrderProcess order)
    {
        if(this.db == null)
        {
            try
            {
                this.db = FirebaseFirestore.getInstance();
            }
            catch (Exception e )
            {
                Log.e(TAG,e.toString());
            }
        }

        Gson gson = new Gson();
        String json = gson.toJson(order);
        Map<String,Object> orderEntry = new HashMap<>();
        orderEntry.put(order.TicketID,json);

        db.collection("orders").document("orders-db").update(orderEntry).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Order "+ order.TicketID+" successfully written!");
            }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error writing document", e);
                }
            });
    }

    public void GetOrder(String ticketID, GetOrderCallback callback)
    {
        if(this.orders == null)
            orders = new HashMap<>();
        if(this.db == null)
        {
            try
            {
                this.db = FirebaseFirestore.getInstance();
            }
            catch (Exception e )
            {
                Log.e(TAG,e.toString());
            }
        }
        DocumentReference globalConfigDoc = db.collection("orders").document("orders-db");
        globalConfigDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.contains(ticketID)) {
                            String config = document.getString(ticketID);
                            Gson gson = new Gson();
                            OrderProcess order = gson.fromJson(config, OrderProcess.class);
                            orders.put(ticketID, order);
                            callback.onGetOrderSuccess(order);
                        } else {
                            Log.d(TAG, "No such document");
                            OrderProcess order = null;
                            ArrayList<Order> orderList = OrderListController.Instance().ActiveOrders;
                            for (Order o : orderList) {
                                if (o.TicketID.equals(ticketID)) {
                                    order = new OrderProcess(o);
                                    orders.put(ticketID, order);
                                    saveFirebaseOrder(order);
                                }
                            }
                            if (order != null)
                                callback.onGetOrderSuccess(order);
                            else
                                callback.onGetOrderFail("No data");
                        }
                    } else {
                        Log.d(TAG, "Get failed with ", task.getException());
                        callback.onGetOrderFail("Get failed with" + task.getException());
                    }
                }
            }
        });
    }

    public void saveOrders()
    {
        for(OrderProcess order : this.orders.values())
        {
            saveFirebaseOrder(order);
        }

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
