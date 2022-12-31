package com.matej.cshelper.storage;

import android.util.Log;

import androidx.annotation.NonNull;

import com.matej.cshelper.network.firebase.FirebaseConnector;
import com.matej.cshelper.network.firebase.entities.GlobalComponentsConfig;
import com.matej.cshelper.network.redmine.entities.Order;
import com.matej.cshelper.network.redmine.entities.ServerComponent;

import java.util.ArrayList;
import java.util.HashMap;

public class OrderProcess {

    public enum OrderStatus
    {
        NEW,
        COMPONENT_PREPARATION_START,
        COMPONENT_PREPARATION_DONE,
        BUILD_START,
        BUILD_DONE,
        BUILD_CHECK_START,
        BUILD_CHECK_DONE,
        EXPORT_START,
        EXPORT_DONE,
        ORDER_DONE
    }

    private static String TAG = "OrderProcess";

    public String TicketID;
    public String OrderID;
    public String Company;
    public String Stresstest;
    public String Server;
    public ArrayList<ProcessStep> OrderSteps;
    public ArrayList<ComponentProcess> Components;
    public OrderStatus Status;

    public OrderProcess(Order order)
    {
        TicketID = order.TicketID;
        OrderID = order.OrderID;
        Company = order.Company;
        Stresstest = order.Stresstest;
        Status = OrderStatus.NEW;

        Components = new ArrayList<>();
        for (ServerComponent component: order.Components)
        {
            ComponentProcess c = new ComponentProcess();
            c.Name = component.Name;
            c.Type = component.Type;
            c.Quantity = component.Quantity;
            c.steps = new HashMap<>();
            GlobalComponentsConfig config = FirebaseConnector.getInstance().ComponentsConfig;
            ArrayList<String> list = config.components_config.get(c.Type);
            if(list == null)
            {
                if(c.Type.equals("SBRB"))
                    Server = c.Name;
                continue;
            }
            for (String step:list)
            {
                c.steps.put(step,false);
            }
            Components.add(c);
        }

        ArrayList<ProcessStep> steps = FirebaseConnector.getInstance().OrderProcessSteps;
        OrderSteps = new ArrayList<>();
        for(ProcessStep step : steps)
        {
            OrderSteps.add(new ProcessStep(step));
        }
    }

    public String RedminePrint()
    {
        StringBuilder result = new StringBuilder();

        for(ProcessStep step : OrderSteps)
        {
            result.append("\n ").append(step.name).append(" - ").append(step.status.toString());
            if(step.type == 1)
            {
                for(ComponentProcess component : Components)
                {
                    result.append("\n  ").append(component.Quantity).append(" - ").append(component.Name);
                    for(String key : component.steps.keySet())
                    {
                        result.append("\n    ").append(key).append(" - ");
                        result.append((component.steps.get(key)?"DONE":"NOT DONE"));
                    }
                }
            }

        }

        return result.toString();
    }

    @NonNull
    @Override
    public String toString() {
        String result;

        result = "TicketID: " + TicketID;
        result += "\nOrderID: " + OrderID;
        result += "\nCompany: " + Company;
        result += "\nStresstest: " + Stresstest;

        for (ComponentProcess comp : Components)
        {
            result += "\n  " + comp.Quantity + "x " +comp.Name;
        }

        return result;
    }
}
