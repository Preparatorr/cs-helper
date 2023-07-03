package com.matej.cshelper.storage;

import com.matej.cshelper.fragments.helpers.OrderScanController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ScanOrder
{
    public String ticketID = "";
    public boolean uploaded = false;
    private ArrayList<String> componentsNames = new ArrayList<>(Arrays.asList("Server","Case","MB","BP","Riser","Raid","Battery","PSU","RAM","HDD"));
    private HashMap<String, ScanComponent> components = new HashMap<>();

    public ScanOrder(String ticketID) {
        this.ticketID = ticketID;
        for (String component: componentsNames)
        {
            ScanComponent c = new ScanComponent(component);
            c.pns.add(new Component("", null));
            components.put(component, c);
        }
    }
    public ScanOrder()
    {
        for (String component: componentsNames)
        {
            ScanComponent c = new ScanComponent(component);
            c.pns.add(new Component("", null));
            components.put(component, c);
        }
    }

    public ArrayList<String> getComponentsNames()
    {
        return componentsNames;
    }

    public HashMap<String, ScanComponent> getComponents() {
        return components;
    }

    public ScanComponent getComponent(String name)
    {
        if(!components.containsKey(name))
            return new ScanComponent(name);
        return components.get(name);
    }

    public void addComponent(String name)
    {
        componentsNames.add(name);
        components.put(name, new ScanComponent(name));
        components.get(name).pns.add(new Component("", null));
        OrderScanController.getInstance().saveOrders();
    }
    public void addPn(String componentName, Component component)
    {
        components.get(componentName).pns.add(component);
        OrderScanController.getInstance().saveOrders();
    }
    public void addPn(String componentName, String Pn)
    {
        if(components.get(componentName).pns.isEmpty())
            components.get(componentName).pns.add(null);
        components.get(componentName).pns.add(components.get(componentName).pns.size()-1, new Component(Pn, componentName));
        OrderScanController.getInstance().saveOrders();
    }

    public void deletePn(String componentName, String pn)
    {
        for(int i=0; i< components.get(componentName).pns.size(); i++)
        {
            if(components.get(componentName).pns.get(i).pn.equals(pn))
                components.get(componentName).pns.remove(i);
        }
    }

    public void addSn(String componentName, String pn, String sn)
    {
        //if(components.get(componentName).pns == null)
        //    components.get(componentName).pns.add(new Component(pn, componentName));
        for(Component c: components.get(componentName).pns)
        {
            if(c.pn.equals(pn))
            {
                if(c.serials.get(c.serials.size()-1).isEmpty())
                    c.serials.set(c.serials.size()-1, sn);
                else
                    c.serials.add(sn);
            }
        }
        OrderScanController.getInstance().saveOrders();
    }
}
