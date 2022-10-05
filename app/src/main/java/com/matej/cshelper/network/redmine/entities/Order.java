package com.matej.cshelper.network.redmine.entities;

import java.util.ArrayList;

public class Order {

    public String TicketID;
    public String OrderID;
    public String Company;
    public String Stresstest;

    public ArrayList<ServerComponent> Components;

    public Order() {
        this.Components = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Order{" +
                "TicketID='" + TicketID + '\'' +
                ", OrderID='" + OrderID + '\'' +
                ", Company='" + Company + '\'' +
                ", Stresstest='" + Stresstest + '\'' +
                ", Components=" + Components +
                '}';
    }
}
