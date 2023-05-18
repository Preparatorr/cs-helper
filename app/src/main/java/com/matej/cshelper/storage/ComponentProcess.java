package com.matej.cshelper.storage;

import java.util.HashMap;

public class ComponentProcess
{
    public int Quantity = 0;
    public String Type = "";
    public String Name = "";
    public boolean PrepDone = false;
    public boolean External = false;
    public boolean ExpDone = false;

    public boolean CoponentCheck = false;

    public HashMap<String,Boolean> steps;
}
