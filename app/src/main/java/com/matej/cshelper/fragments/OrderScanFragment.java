package com.matej.cshelper.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.matej.cshelper.MainActivity;
import com.matej.cshelper.R;
import com.matej.cshelper.fragments.helpers.OrderScanController;
import com.matej.cshelper.network.redmine.RedmineConnector;
import com.matej.cshelper.storage.Component;
import com.matej.cshelper.storage.ScanComponent;
import com.matej.cshelper.storage.ScanOrder;

import java.util.ArrayList;
import java.util.Arrays;

public class OrderScanFragment extends Fragment {
    public static final String TAG = "OrdersScanFragment";
    public static final String ARG_SCAN = "ScannedValue";
    public static final String ARG_SCROLL_TO = "ScrollTo";
    public static final String ARG_TICKET_ID = "TicketID";
    LinearLayout mainLayout;
    LayoutInflater inflater;
    OrderScanFragment instance;
    private String ticketID;

    private int scrollTo = 0;
    private View root;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        if (getArguments() != null)
        {
            String payload = getArguments().getString(ScanFragment.ARG_SOURCE_PAYLOAD, "");
            String value = getArguments().getString(ARG_SCAN, "");
            this.scrollTo = getArguments().getInt(ARG_SCROLL_TO, 0);
            ticketID = getArguments().getString(ARG_TICKET_ID,"");
            if(payload.isEmpty())
                return;

            String[] payloadArr = payload.split("\\|");
            Log.d(TAG, "Get from scan = " + Arrays.toString(payloadArr));
            //PN Scanned
            if(payloadArr.length == 1)
            {
                OrderScanController.getInstance().getOrder(ticketID).addPn(payload, value);
            }
            else
            {
                //SN scanned
                ArrayList<Component> components = OrderScanController.getInstance().getOrder(ticketID).getComponent(payloadArr[0]).pns;
                OrderScanController.getInstance().getOrder(ticketID).addSn(payloadArr[0], payloadArr[1], value);
            }
        }
        else
        {
            ticketID = "";
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ((MainActivity) getActivity()).setActionBarTitle("Scanner");
        this.inflater = inflater;
        this.root = inflater.inflate(R.layout.fragment_order_scan, container, false);
        if(ticketID.isEmpty())
        {
            showInputDialog("Enter ticketID", new OnInputDialogAction() {
                @Override
                public void done(String input) {
                    ticketID = input;
                    ((EditText)root.findViewById(R.id.ticket_number_input)).setText(ticketID);
                    redrawLayout();
                }

                @Override
                public void cancel() {
                    showInputDialog("TicketID is mandatory!", this);
                }
            });
        }
        redrawLayout();
        return root;
    }

    private void redrawLayout()
    {
        ((EditText)root.findViewById(R.id.ticket_number_input)).setText(ticketID);
        ArrayList<String> components = OrderScanController.getInstance().getOrder(ticketID).getComponentsNames();
        mainLayout = this.root.findViewById(R.id.order_scan_main_layout);
        mainLayout.removeAllViews();

        for(String component : components)
        {
            View item = inflater.inflate(R.layout.order_scan_item,(ViewGroup) root,false);
            ((TextView)item.findViewById(R.id.scan_component_name)).setText(component);
            mainLayout.addView(item);
            item.findViewById(R.id.button_add_pn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    OrderScanController.getInstance().getOrder(ticketID).addPn(component, "");
                    redrawLayout();
                }
            });
            for(int i = 0; i < OrderScanController.getInstance().getOrder(ticketID).getComponent(component).pns.size(); i++)
            {
                Component componentPn = OrderScanController.getInstance().getOrder(ticketID).getComponent(component).pns.get(i);
                View pnView = inflater.inflate(R.layout.order_scan_component_inner,(ViewGroup) root,false);
                EditText inputPn =((EditText)pnView.findViewById(R.id.scan_input_pn));
                if(componentPn == null)
                    continue;
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
                        args.putString(ScanFragment.ARG_COMPONENT_NAME, "PN for: " + component);
                        args.putString(ARG_TICKET_ID, ticketID);
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
                pnView.findViewById(R.id.delete_pn_button).setOnClickListener((v)-> {
                    OrderScanController.getInstance().getOrder(ticketID).deletePn(component, componentPn.pn);
                    redrawLayout();
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
        Button addComponent = new Button(MainActivity.getContext());
        addComponent.setText("Add component");
        addComponent.setOnClickListener(view -> showInputDialog("Enter component name", new OnInputDialogAction() {
            @Override
            public void done(String input) {
                OrderScanController.getInstance().getOrder(ticketID).addComponent(input);
                redrawLayout();
            }
            @Override
            public void cancel() {
            }
        }));
        mainLayout.addView(addComponent);
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
                String scan = printScan();
                Log.d(TAG, "Scan: \n" + scan);
                RedmineConnector.getInstance().updateSerialNumbers(issue, scan);
                Snackbar.make(view, "Order Sent", Snackbar.LENGTH_LONG).show();
                NavHostFragment.findNavController(instance).navigate(R.id.homeFragment);

            }
        });
        ((EditText)root.findViewById(R.id.ticket_number_input)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                ticketID = editable.toString();
            }
        });
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
                args.putString(ScanFragment.ARG_COMPONENT_NAME, "Serial number for: " + (component.name.isEmpty()?componentName : component.name));
                args.putString(ARG_TICKET_ID, ticketID);
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
        //OrderScanController.getInstance().saveOrders();
    }

    private String printScan()
    {
        StringBuilder result = new StringBuilder();
        for(String componentName : OrderScanController.getInstance().getOrder(ticketID).getComponentsNames())
        {
            ScanComponent componentPn = OrderScanController.getInstance().getOrder(ticketID).getComponent(componentName);
            if(!componentPn.toString().isEmpty())
                result.append("\n").append(componentName).append(": ").append(componentPn);
        }
        return result.toString();
    }

    private void showInputDialog(String title, OnInputDialogAction action)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getInstance());
        builder.setTitle(title);
        final EditText input = new EditText(MainActivity.getInstance());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", (dialog, which) -> {
            String name = input.getText().toString();
            action.done(name);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            action.cancel();
        });
        builder.show();
    }

    interface OnInputDialogAction
    {
        void done(String input);
        void cancel();
    }
}