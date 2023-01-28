package com.matej.cshelper.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.matej.cshelper.MainActivity;
import com.matej.cshelper.R;
import com.matej.cshelper.core.InstanceProvider;
import com.matej.cshelper.fragments.helpers.OrderProcessingManager;
import com.matej.cshelper.network.redmine.RedmineConnector;
import com.matej.cshelper.storage.ComponentProcess;
import com.matej.cshelper.storage.OrderProcess;
import com.matej.cshelper.storage.ProcessStep;


public class ComponentPreparationFragment extends Fragment implements OrderProcessingManager.GetOrderCallback {

    private static String TAG = "ComponentPreparationFragment";

    private LayoutInflater inflater;
    private LinearLayout stepsLayout;

    private String ticketID;
    private OrderProcess order = null;
    private ComponentPreparationFragment instance;

    private ViewGroup mainLayout;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.ticketID = getArguments().getString(OrderProcessingFragment.ARG_TICKET_ID);
            OrderProcessingManager.getInstance().GetOrder(this.ticketID,this);
        }
        else
            Log.e(TAG, "Fatal: no ticket ID provided");
        Log.i(TAG, "onCreate " + this.ticketID);
        this.instance = this;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(this.order != null)
            redrawLayout();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_order_processing, container, false);
        this.mainLayout = root.findViewById(R.id.order_process_main_layout);
        ((TextView)this.mainLayout.findViewById(R.id.order_process_ticket)).setText(this.ticketID);
        LinearLayout stepsLayout = this.mainLayout.findViewById(R.id.order_steps_layout);

        this.inflater = inflater;
        this.stepsLayout = stepsLayout;
        return root;
    }

    private void showOrder(ComponentProcess component)
    {
        View stepView = inflater.inflate(R.layout.order_processing_step_item, stepsLayout, false);
        ((TextView)stepView.findViewById(R.id.component_name)).setText(component.Name);
        stepsLayout.addView(stepView);

        CheckBox stepCheckbox = stepView.findViewById(R.id.step_done);
        //stepCheckbox.setChecked(step.status == ProcessStep.Status.DONE);
        TextView quantity = stepView.findViewById(R.id.items);
        quantity.setVisibility(View.VISIBLE);
        quantity.setText(component.Quantity+"x");
        quantity.setTextSize(getResources().getDimension(R.dimen.text_size_small));
        if(quantity.getParent() != null)
            ((ViewGroup)quantity.getParent()).removeView(quantity);
        ((LinearLayout)stepView.findViewById(R.id.step_items)).addView(quantity);
    }

    private void redrawLayout()
    {
        //Log.d(TAG,"RedraWLayout");
        stepsLayout.removeAllViewsInLayout();
        ComponentProcess server = new ComponentProcess();
        server.Name = order.Server;
        server.Quantity = 1;
        showOrder(server);

        for(ComponentProcess component: order.Components)
        {
            showOrder(component);
        }
        Button nextStep = new Button(MainActivity.getContext());
        nextStep.setText("DONE");
        nextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"Order done!");
                order.Status = OrderProcess.OrderStatus.COMPONENT_PREPARATION_DONE;
            }
        });
        OrderProcessingManager.getInstance().saveFirebaseOrder(order);
        stepsLayout.addView(nextStep);
    }

    @Override
    public void onGetOrderSuccess(OrderProcess order) {
        this.order = order;
        this.order.Status = OrderProcess.OrderStatus.COMPONENT_PREPARATION_START;
        ((TextView)mainLayout.findViewById(R.id.order_process_order)).setText(order.OrderID);
        ((TextView)mainLayout.findViewById(R.id.order_process_company)).setText(order.Company);
        ((MainActivity) getActivity()).setActionBarTitle("Ticket: " + order.TicketID);
        redrawLayout();
    }

    @Override
    public void onGetOrderFail(String message) {
        Log.e(TAG, "Order get fail" + message);
    }
}