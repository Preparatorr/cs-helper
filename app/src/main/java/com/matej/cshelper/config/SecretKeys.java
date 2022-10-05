package com.matej.cshelper.config;

public class SecretKeys {

    private static SecretKeys instance = null;

    public String RedmineAPIKey = "";
    public String RedmineURL = "";
    public String RedmineQuery = "";

    private SecretKeys() {
    }

    public static SecretKeys getInstance()
    {
        if(instance == null)
            instance = new SecretKeys();
        return instance;
    }
}
