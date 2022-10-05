package com.matej.cshelper.core;

import com.matej.cshelper.core.interfaces.InstanceBase;

import java.util.HashMap;

public class InstanceProvider {

    private static HashMap<String, InstanceBase> instances;

    public static void RegisterInstance(InstanceBase instance)
    {
        if(instances == null)
            instances = new HashMap<>();
        instances.put(instance.getClass().toString(),instance);
    }

    public static InstanceBase GetInstance(Class name)
    {
        return instances.get(name.toString());
    }
}
