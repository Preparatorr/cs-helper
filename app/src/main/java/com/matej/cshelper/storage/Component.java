package com.matej.cshelper.storage;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Component {
    public String pn;
    public ArrayList<String> serials;

    public Component(String pn) {
        this.pn = pn;
        serials = new ArrayList<>();
    }

    @NonNull
    @Override
    public String toString() {
        return (!serials.contains(""))? String.join(", ", serials) + ";" : "";
    }
}
