package com.matej.cshelper.fragments.helpers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.matej.cshelper.config.SecretKeys;
import com.matej.cshelper.fragments.OrderScanFragment;
import com.matej.cshelper.fragments.ScanListFragment;
import com.matej.cshelper.fragments.SettingsFragment;
import com.matej.cshelper.network.redmine.RedmineConnector;
import com.matej.cshelper.storage.ScanComponent;
import com.matej.cshelper.storage.ScanOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OrderScanController {
    private static final String TAG = "OrderScanController";
    private static OrderScanController instance;
    private HashMap<String, ScanOrder> orders = new HashMap<>();
    private FirebaseFirestore db;

    private OrderScanController()
    {
        db = FirebaseFirestore.getInstance();
        getFirebaseScans(()->{
            Log.d(TAG, "Scans loaded");
        });
    }

    public static OrderScanController getInstance()
    {
        if(instance == null)
            instance = new OrderScanController();
        return instance;
    }

    public ScanOrder getOrder(String ticketID)
    {
        if(!orders.containsKey(ticketID))
        {
            ScanOrder order = new ScanOrder();
            orders.put(ticketID, order);
            return order;
        }

        return orders.get(ticketID);
    }

    public HashMap<String, ScanOrder> getOrders()
    {
        return orders;
    }

    public void saveOrders()
    {
        Gson gson = new Gson();
        Map<String,Object> orderEntry = new HashMap<>();
        orders.forEach((k,v) ->
        {
            if(k.isEmpty())
                return;
            String json = gson.toJson(v);
            orderEntry.put(k,json);
        });

        db.collection("scan").document("scan-db").update(orderEntry).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Scans successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void getFirebaseScans(OnFinished callback)
    {
        DocumentReference scans = db.collection("scan").document("scan-db");
        scans.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> map = document.getData();
                    Gson gson = new Gson();
                    for (String s : map.keySet())
                    {
                        ScanOrder order = gson.fromJson((String)map.get(s), ScanOrder.class);
                        orders.put(s,order);
                    }
                    callback.done();
                } else {
                    Log.d(TAG, "No such document");
                    callback.done();
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    public void deleteScan(String ticketId)
    {
        DocumentReference scans = db.collection("scan").document("scan-db");
        Map<String,Object> updates = new HashMap<>();
        updates.put(ticketId, FieldValue.delete());
        scans.update(updates).addOnCompleteListener(runnable -> {
           Log.d(TAG, "Delete done");
        });
        orders.remove(ticketId);

    }

    public interface OnFinished
    {
        void done();
    }

}
