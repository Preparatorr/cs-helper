package com.matej.cshelper.network;


public class GoogleDriveSender {

    private static GoogleDriveSender instance;
    public static GoogleDriveSender getInstance()
    {
        if(instance == null)
            instance = new GoogleDriveSender();
        return instance;
    }

    public void sendPhotoToDrive(String path, String ticketID)
    {

    }

}
