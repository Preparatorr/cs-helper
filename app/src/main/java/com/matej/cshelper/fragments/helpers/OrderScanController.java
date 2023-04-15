package com.matej.cshelper.fragments.helpers;

import android.util.Log;

import com.matej.cshelper.storage.ScanComponent;

import java.util.HashMap;

public class OrderScanController {
    private static OrderScanController instance;

    private HashMap<String, ScanComponent> components;

    public static OrderScanController getInstance()
    {
        if(instance == null)
            instance = new OrderScanController();
        return instance;
    }

    public HashMap<String, ScanComponent> getComponents() {
        return components;
    }

    public void setComponents(HashMap<String, ScanComponent> components) {
        this.components = components;
    }

    public String ticketID = "";
}
