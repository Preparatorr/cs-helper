package com.matej.cshelper.network.redmine.entities;

public class ServerComponent {

    public int Quantity = 0;
    public String Type = "";
    public String Name = "";


    @Override
    public String toString()
    {
        return Quantity + "x " + Type + " " + Name;
    }
}
