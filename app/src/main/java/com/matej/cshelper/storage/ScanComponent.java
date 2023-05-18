package com.matej.cshelper.storage;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

public class ScanComponent {


    public String componentName;
    public ArrayList<Component> pns;

    public ScanComponent(String componentName) {
        this.componentName = componentName;
        this.pns = new ArrayList<>();
        this.pns.add(new Component("", componentName));
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for(Component component : pns)
        {
            if(component.toString().isEmpty())
                continue;
            if(component.pn.isEmpty() && pns.size() == 1)
                result.append(component);
            else if (component.pn.isEmpty())
                result.append("\n").append(component);
            else
                result.append("\n PN: ").append(component.pn).append(": ").append(component);
        }
        return result.toString();
    }
}
