package com.matej.cshelper.storage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class Component {

    public String name;
    public String pn;
    public ArrayList<String> serials;

    public Component(String pn,@Nullable String name) {
        this.pn = pn;
        this.name = name == null? "" : name;
        serials = new ArrayList<>();
    }

    @NonNull
    @Override
    public String toString() {
        return (serials.toString().length() > 4)? String.join(", ", serials) + ";" : "";
    }
}
