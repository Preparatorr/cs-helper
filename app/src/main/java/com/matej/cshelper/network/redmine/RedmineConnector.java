package com.matej.cshelper.network.redmine;

import android.content.DialogInterface;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;

import com.matej.cshelper.MainActivity;
import com.matej.cshelper.R;
import com.matej.cshelper.config.SecretKeys;
import com.matej.cshelper.network.WebRequest;
import com.matej.cshelper.network.redmine.entities.*;
import com.matej.cshelper.storage.OrderProcess;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RedmineConnector {

    private static String TAG = "RedmineConnector";
    private static RedmineConnector instance = null;
    public static RedmineConnector getInstance()
    {
        if(instance == null)
            instance = new RedmineConnector();
        return instance;
    }

    public ArrayList<Order> GetLatestOrders()
    {
        //GetOrder("24131");
        //return null;
        ArrayList<Order> result = new ArrayList<>();
        String url = SecretKeys.getInstance().RedmineURL + "issues.json?"+SecretKeys.getInstance().RedmineQuery;
        Log.d("URL ", url);
        WebRequest request = new WebRequest(url, WebRequest.Method.Get,null);
        String response = request.Invoke();
        //TODO: No connectivity to Redmine
        if(response == null)
        {
            return new ArrayList<>();
        }
        try {
            JSONArray issues = new JSONObject(response).getJSONArray("issues");
            Log.d(TAG,"issues: " + issues.length());
            for (int i = 0; i< issues.length();i++)
            {
                Order order = ParseOrder(issues.getJSONObject(i));
                if(order != null)
                    result.add(order);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Order GetOrder (String id)
    {
        String url = SecretKeys.getInstance().RedmineURL + "issues/"+id+".json";
        WebRequest request = new WebRequest(url, WebRequest.Method.Get,null);
        String response = request.Invoke();
        try {
            return ParseOrder(new JSONObject(response).getJSONObject("issue"));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean UpdateIssue(OrderProcess order)
    {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject notesObject = new JSONObject();
            String items = order.RedminePrint().replace("\r ","");
            notesObject.put("notes","Tech department update:"+items);
            jsonObject.put("issue",notesObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = SecretKeys.getInstance().RedmineURL + "issues/"+order.TicketID+".json";
        Log.d(TAG,url);
        String body = jsonObject.toString();
        Log.d(TAG,body);
        WebRequest request = new WebRequest(url, WebRequest.Method.Put,body);
        String response = request.Invoke();
        Log.d(TAG +"Response",response);
        return true;
    }

    private Order ParseOrder (JSONObject issue)
    {
        Order order = new Order();
        try {
            order.TicketID = issue.getString("id");
            JSONArray customFields = issue.getJSONArray("custom_fields");
            for(int i=0;i<customFields.length();i++)
            {
                JSONObject object = customFields.getJSONObject(i);
                switch(object.getInt("id"))
                {
                    case Keys.ORDER_ID:
                        order.OrderID = object.getString("value");
                        break;
                    case Keys.COMPANY:
                        order.Company = object.getString("value");
                        break;
                    case Keys.STRESSTEST:
                        order.Stresstest = object.getString("value");
                        break;
                    default:
                        break;
                }
            }
            Log.d(TAG,issue.toString());
            String descString = issue.getString("description");
            int author = issue.getJSONObject("author").getInt("id");
            if(descString.isEmpty())
            {
                return null;
            }
            if(author != 34 && !order.TicketID.equals("24569"))
            {
                return null;
            }
            boolean newFormat = false;
            for(String line: descString.split("\n"))
            {

                if(line.contains("["))
                {
                    //Log.d("koko",line);
                    String line2 = line.replaceAll("\\s+\\]","\\]");
                    String[] split = line2.split(" ");
                    ServerComponent component = new ServerComponent();
                    if(!line.contains("x -"))
                        component.Quantity = 1;
                    else
                        component.Quantity = Integer.parseInt(split[2].replace("x",""));
                    int index = 6;
                    if(split[5].contains("["))
                    {
                        component.Type = split[5].replaceAll("\\[","").replaceAll("\\]","");
                        newFormat = true;
                    }
                    else
                    {
                        component.Type = "";
                        index = 5;
                    }

                    for(int i = index;i<split.length;i++)
                    {
                        component.Name += split[i] + " ";
                    }
                    component.Name = component.Name.replaceAll("null","");
                    order.Components.add(component);
                }
            }
            //if(!newFormat)
            //    return null;
            //Log.d(TAG + " done",order.toString());
            return order;
        } catch (JSONException e) {
            Log.d(TAG+"CRASH ",order.toString());
            e.printStackTrace();
            return null;
        }
    }
}
