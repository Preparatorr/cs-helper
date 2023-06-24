package com.matej.cshelper.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.gson.Gson;
import com.matej.cshelper.MainActivity;
import com.matej.cshelper.R;
import com.matej.cshelper.network.firebase.FirebaseConnector;
import com.matej.cshelper.network.redmine.RedmineConnector;
import com.matej.cshelper.settings.User;
import com.matej.cshelper.settings.UserManager;

import java.util.HashMap;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SETTINGS";
    public static final String ARG_USERID = "UserID";

    public static boolean SendEmailsEnabled = false;
    private static final int pic_id = 666;

    private int userID;

    private View layout;
    private SettingsFragment instance;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        this.layout = inflater.inflate(R.layout.fragment_settings, container, false);
        layout.findViewById(R.id.switch_user_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchUser();
            }
        });
        ((Switch)this.layout.findViewById(R.id.emails_sending)).setOnCheckedChangeListener((compoundButton, b) -> {
            SendEmailsEnabled = b;
            FirebaseConnector.getInstance().SetEmailNotifications(b);
        });
        return this.layout;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            this.userID = getArguments().getInt(ARG_USERID);
        }
        else
            this.userID = 0;
        instance = this;
        if(userID != 0)
        {
            UserManager.getInstance().setCurrentUser(this.userID);
            MainActivity.getInstance().refreshUser();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    void switchUser()
    {
        Bundle args = new Bundle();
        args.putInt(ScanFragment.ARG_SOURCE, 1);
        NavHostFragment.findNavController(instance).navigate(R.id.scanFragment,args);
    }
}