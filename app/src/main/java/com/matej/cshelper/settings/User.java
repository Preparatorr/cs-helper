package com.matej.cshelper.settings;

public class User
{
    public int UserID;
    public String Name;

    public User()
    {
        UserID = 0;
        Name = "Janko Hráško";
    }
    public User(int userID, String name) {
        UserID = userID;
        Name = name;
    }
}
