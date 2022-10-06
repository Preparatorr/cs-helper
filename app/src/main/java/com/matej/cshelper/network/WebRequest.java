package com.matej.cshelper.network;

import com.matej.cshelper.config.SecretKeys;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import okio.BufferedSink;

public class WebRequest {

    public enum Method
    {
        Post,
        Get,
        Put
    }

    private String Url;
    private Method RequestMethod;
    private String body;

    public WebRequest(String url, Method requestMethod, String body) {
        this.Url = url;
        this.RequestMethod = requestMethod;
        this.body = body;
    }

    public String Invoke()
    {
        NetworkThread thread = new NetworkThread(this.Url, this.RequestMethod, this.body);
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
        public String url;
        public Method method;
        public String body;

        public NetworkThread(String url, Method method, String body)
        {
            this.url = url;
            this.method = method;
            this.body = body;
        }

        @Override
        public void run() {
            String url = this.url;
            OkHttpClient client = new OkHttpClient();
            Request request;
            if(method == Method.Put)
            {
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON,this.body);
                request = new Request.Builder()
                        .url(url)
                        .addHeader("Content-Type","application/json")
                        .addHeader("X-Redmine-API-Key", SecretKeys.getInstance().RedmineAPIKey)
                        .put(body)
                        .build();
            }
            else
            {
                request = new Request.Builder()
                        .url(url)
                        .addHeader("Content-Type","application/json")
                        .addHeader("X-Redmine-API-Key", SecretKeys.getInstance().RedmineAPIKey)
                        .get()
                        .build();
            }
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
