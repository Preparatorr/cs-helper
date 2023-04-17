package com.matej.cshelper.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.matej.cshelper.MainActivity;
import com.matej.cshelper.R;
import com.matej.cshelper.fragments.helpers.OrderScanController;
import com.matej.cshelper.network.redmine.RedmineConnector;
import com.matej.cshelper.storage.Component;
import com.matej.cshelper.storage.ScanComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OrderScanFragment extends Fragment {
    public static final String TAG = "OrdersScanFragment";
    public static final String ARG_SCAN = "ScannedValue";
    public static final String ARG_SCROLL_TO = "ScrollTo";
    public static final String ARG_TICKET_ID = "TicketID";
    LinearLayout mainLayout;
    LayoutInflater inflater;
    OrderScanFragment instance;

    private HashMap<String, ScanComponent> scannedItems;

    private int scrollTo = 0;
    private View root;

    private List<String> components = Arrays.asList("Server","Case","MB","BP","Riser","Raid","Battery","PSU","RAM","HDD");
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        scannedItems = new HashMap<>();
        if (getArguments() != null)
        {
            String payload = getArguments().getString(ScanFragment.ARG_SOURCE_PAYLOAD, "");
            String value = getArguments().getString(ARG_SCAN, "");
            this.scrollTo = getArguments().getInt(ARG_SCROLL_TO, 0);
            String ticketId = getArguments().getString(ARG_TICKET_ID,"");
            if(!ticketId.isEmpty())
                OrderScanController.getInstance().ticketID = ticketId;
            if(payload.isEmpty())
                return;
            scannedItems = OrderScanController.getInstance().getComponents();
            String[] payloadArr = payload.split("\\|");
            Log.d(TAG, "Get from scan = " + Arrays.toString(payloadArr));
            if(payloadArr.length == 1)
            {
                ArrayList<Component> arrayList = scannedItems.get(payload).pns;
                if(arrayList.size() > 0)
                {
                    if(!value.isEmpty())
                    {
                        arrayList.get(arrayList.size()-1).pn = value;
                        arrayList.add(new Component(""));
                    }
                }
            }
            else
            {
                ArrayList<Component> components = scannedItems.get(payloadArr[0]).pns;
                for(Component component : components)
                {
                    if(component.pn.equals(payloadArr[1]))
                    {
                        component.serials.set((Integer.parseInt(payloadArr[2]) - 1), value);
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        root = inflater.inflate(R.layout.fragment_order_scan, container, false);
        mainLayout = root.findViewById(R.id.order_scan_main_layout);
        this.inflater = inflater;
        ((MainActivity) getActivity()).setActionBarTitle("Scanner");
        for(String component : components)
        {
            View item = inflater.inflate(R.layout.order_scan_item,(ViewGroup) root,false);
            ((TextView)item.findViewById(R.id.scan_component_name)).setText(component);
            mainLayout.addView(item);
            if(!scannedItems.containsKey(component))
                scannedItems.put(component, new ScanComponent(component));
            for(int i=0; i < scannedItems.get(component).pns.size(); i++)
            {
                Component componentPn = scannedItems.get(component).pns.get(i);
                View pnView = inflater.inflate(R.layout.order_scan_component_inner,(ViewGroup) root,false);
                EditText inputPn =((EditText)pnView.findViewById(R.id.scan_input_pn));
                inputPn.setText(componentPn.pn);
                inputPn.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        componentPn.pn = editable.toString();
                        Log.d(TAG, "New value = " + componentPn.pn);
                    }
                });

                pnView.findViewById(R.id.scan_pn_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "Scroller : " + root.findViewById(R.id.order_scan_scroll_view).getScrollX() + "  " + root.findViewById(R.id.order_scan_scroll_view).getScrollY());


                        Bundle args = new Bundle();
                        args.putInt(ScanFragment.ARG_SOURCE, 2);
                        args.putString(ScanFragment.ARG_SOURCE_PAYLOAD, component);
                        args.putInt(ARG_SCROLL_TO, root.findViewById(R.id.order_scan_scroll_view).getScrollY());
                        NavHostFragment.findNavController(instance).navigate(R.id.scanFragment,args);
                    }
                });
                pnView.findViewById(R.id.add_sn_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addSn(pnView.findViewById(R.id.sns_layout),componentPn, component, "");
                        componentPn.serials.add("");
                    }
                });
                for(String sn : componentPn.serials)
                {
                    addSn(pnView.findViewById(R.id.sns_layout), componentPn, component, sn);
                }
                if(componentPn.serials.size() == 0)
                {
                    componentPn.serials.add("");
                    addSn(pnView.findViewById(R.id.sns_layout), componentPn, component,"");
                }
                ((LinearLayout)item.findViewById(R.id.order_scan_pns_layout)).addView(pnView);
            }
        }
        OrderScanController.getInstance().setComponents(scannedItems);
        Log.d(TAG, "Scrollto: " + scrollTo + "    ");
        if(scrollTo != 0)
        {
           ScrollView scroll = root.findViewById(R.id.order_scan_scroll_view);
           scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.scrollTo(0, scrollTo);
                }
            });
        }
        root.findViewById(R.id.finish_scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String issue = ((EditText)root.findViewById(R.id.ticket_number_input)).getText().toString();
                if (issue.isEmpty())
                {
                    Snackbar.make(view, "Ticket number is empty!!!", Snackbar.LENGTH_LONG).show();
                    return;
                }
                Log.d(TAG, "Scan: \n" + printScan());
                RedmineConnector.getInstance().updateSerialNumbers(issue, printScan());
                Snackbar.make(view, "Order Sent", Snackbar.LENGTH_LONG).show();
                NavHostFragment.findNavController(instance).navigate(R.id.homeFragment);

            }
        });
        ((EditText)root.findViewById(R.id.ticket_number_input)).setText(OrderScanController.getInstance().ticketID);

        return root;
    }

    private void addSn(ViewGroup pnLayout, Component component, String componentName, String value)
    {
        int index = pnLayout.getChildCount();
        View snView = inflater.inflate(R.layout.order_scan_sn_line,pnLayout,false);
        snView.findViewById(R.id.scan_sn_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                Log.d(TAG, "Sending to scan: " + componentName + "|" + component.pn + "|" + pnLayout.getChildCount());
                args.putString(ScanFragment.ARG_SOURCE_PAYLOAD, componentName + "|" + component.pn + "|" + pnLayout.getChildCount());
                args.putInt(ScanFragment.ARG_SOURCE, 2);
                args.putInt(ARG_SCROLL_TO, root.findViewById(R.id.order_scan_scroll_view).getScrollY());
                NavHostFragment.findNavController(instance).navigate(R.id.scanFragment,args);
            }
        });
        ((TextView)snView.findViewById(R.id.sn_text_view)).setText("SN " + (pnLayout.getChildCount()+1));
        EditText input = ((EditText)snView.findViewById(R.id.sn_input_text));
        input.setText(value);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                Log.d(TAG, "Serial change:" + editable.toString());
                component.serials.set(index,editable.toString());
            }
        });
        pnLayout.addView(snView);
    }

    @Override
    public void onPause() {
        super.onPause();
        /*Gson gson = new Gson();
        String json = gson.toJson(scannedItems);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("scanCache", json);
        editor.commit();*/
    }

    private String printScan()
    {
        StringBuilder result = new StringBuilder();
        for(String componentName : components)
        {
            ScanComponent componentPn = scannedItems.get(componentName);
            if(!componentPn.toString().isEmpty())
                result.append("\n").append(componentName).append(": ").append(componentPn);
        }
        return result.toString();
    }
}