package com.matej.cshelper.network.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.matej.cshelper.config.SecretKeys;
import com.matej.cshelper.fragments.SettingsFragment;
import com.matej.cshelper.network.OnFinishedCallback;
import com.matej.cshelper.network.firebase.entities.GlobalComponentsConfig;
import com.matej.cshelper.network.redmine.RedmineConnector;
import com.matej.cshelper.storage.ProcessStep;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class FirebaseConnector {

    public static String TAG = "FirebaseConnector";
    private static FirebaseConnector instance = null;

    public GlobalComponentsConfig ComponentsConfig = null;
    public ArrayList<ProcessStep> OrderProcessSteps = null;

    private FirebaseFirestore db = null;

    public static FirebaseConnector getInstance()
    {
        if(instance == null)
            instance = new FirebaseConnector();
        return instance;
    }

    public boolean Init()
    {
        try
        {
            this.db = FirebaseFirestore.getInstance();
            getConfigs();
            getOrderSteps();
            return true;
        }
        catch (Exception e )
        {
            Log.e(TAG,e.toString());
            return false;
        }

    }

    public void getKeys(OnFinishedCallback callback)
    {
        if(this.db == null)
            Init();
        DocumentReference globalConfigDoc = db.collection("configs").document("redmine_keys");
        globalConfigDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        SecretKeys.getInstance().RedmineAPIKey = document.getString("RedmineAPIKey");
                        SecretKeys.getInstance().RedmineURL = document.getString("RedmineURL");
                        SecretKeys.getInstance().RedmineQuery = document.getString("RedmineQuery");
                        SettingsFragment.SendEmailsEnabled = document.getBoolean("emails_enabled");
                        Gson gson = new Gson();
                        RedmineConnector.getInstance().EmailMessages = gson.fromJson(document.getString("email_messages"), HashMap.class);
                        Log.d(TAG,SecretKeys.getInstance().RedmineURL);
                        callback.OnFinished();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void getConfigs()
    {
        if(this.db == null)
            Init();
        DocumentReference globalConfigDoc = db.collection("configs").document("global_config");
        globalConfigDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String config = document.getString("file");
                        Log.i(TAG, "Downloaded config: " + config);
                        Gson gson = new Gson();
                        GlobalComponentsConfig globalComponentsConfig = gson.fromJson(config, GlobalComponentsConfig.class);
                        ComponentsConfig = globalComponentsConfig;
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void getOrderSteps()
    {
        ArrayList<ProcessStep> result = new ArrayList<>();
        DocumentReference globalConfigDoc = db.collection("configs").document("order_steps");
        globalConfigDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String config = document.getString("file");
                        Log.i(TAG, "Downloaded order steps: " + config);
                        try {
                            JSONArray steps = new JSONObject(config).getJSONArray("orderSteps");
                            for(int i=0; i< steps.length();i++)
                            {
                                Gson gson = new Gson();
                                ProcessStep step = gson.fromJson(steps.getString(i),ProcessStep.class);
                                result.add(step);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        OrderProcessSteps = result;
    }

    public void SetEmailNotifications(boolean status)
    {
        db.collection("configs").document("redmine_keys").update("emails_enabled", status).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Email sending switch to: " + status);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error writing document", e);
                }
            });
    }

}
