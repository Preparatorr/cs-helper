package com.matej.cshelper.network;

import com.matej.cshelper.config.SecretKeys;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class WebRequest {

    public enum Method
    {
        Post,
        Get,
    }

    private String Url;
    private Method RequestMethod;

    public WebRequest(String url, Method requestMethod) {
        Url = url;
        RequestMethod = requestMethod;
    }

    public String Invoke()
    {
        NetworkThread thread = new NetworkThread(this.Url);
        thread.start();
        try {
            thread.join(5000);
            return thread.responseString;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }


    class NetworkThread extends Thread{
        public String responseString = "";
        public String Url;

        public NetworkThread(String url) {
            this.Url = url;
        }

        @Override
        public void run() {
            String url = this.Url;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type","application/json")
                    .addHeader("X-Redmine-API-Key", SecretKeys.getInstance().RedmineAPIKey)
                    .get()
                    .build();
            responseString = null;
            try {
                Response response = client.newCall(request).execute();
                responseString = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
