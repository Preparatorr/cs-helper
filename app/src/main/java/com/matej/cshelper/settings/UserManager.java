package com.matej.cshelper.settings;

import static com.matej.cshelper.MainActivity.getContext;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.matej.cshelper.config.SecretKeys;
import com.matej.cshelper.fragments.helpers.OrderListController;
import com.matej.cshelper.network.redmine.entities.Order;
import com.matej.cshelper.storage.OrderProcess;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class UserManager
{
    private static final String TAG = "UserManager";

    private static UserManager instance;

    private User actualUser;
    private HashMap<Integer,User> userDB;
    private FirebaseFirestore db = null;

    public static UserManager getInstance()
    {
        if(instance == null)
            instance = new UserManager();
        return instance;
    }

    private UserManager()
    {
        try
        {
            this.db = FirebaseFirestore.getInstance();
        }
        catch (Exception e )
        {
            Log.e(TAG,e.toString());
        }

        DocumentReference globalConfigDoc = db.collection("users").document("usersDB");
        globalConfigDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Gson gson = new Gson();
                        Type type = new TypeToken<HashMap<Integer, User>>(){}.getType();
                        userDB = gson.fromJson(document.getString("file"),type);
                        Log.d(TAG, "Active users: " + userDB.toString());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public User getCurrentUser()
    {
        if(actualUser == null)
            return new User();
        return actualUser;
    }

    public void setCurrentUser(int userID)
    {
        if(!userDB.containsKey(userID))
        {
            Log.e(TAG, "Invalid user: " + userID);
            return;
        }
        actualUser = userDB.get(userID);
    }

}
