package com.matej.cshelper.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.matej.cshelper.MainActivity;
import com.matej.cshelper.R;
import com.matej.cshelper.core.InstanceProvider;
import com.matej.cshelper.fragments.helpers.OrderProcessingManager;
import com.matej.cshelper.network.redmine.RedmineConnector;
import com.matej.cshelper.storage.ComponentProcess;
import com.matej.cshelper.storage.OrderProcess;
import com.matej.cshelper.storage.ProcessStep;

public class OrderProcessingFragment extends Fragment {


    private static String TAG = "OrderProcessingFragment";
    public static final String ARG_TICKET_ID = "ticketID";

    private LayoutInflater inflater;
    private LinearLayout stepsLayout;

    private String ticketID;
    private OrderProcess order;
    private OrderProcessingFragment instance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.ticketID = getArguments().getString(ARG_TICKET_ID);
            this.order = ((OrderProcessingManager)InstanceProvider.GetInstance(OrderProcessingManager.class)).GetOrder(this.ticketID);
            this.order.Status = OrderProcess.OrderStatus.BUILD_START;
        }
        else
            Log.e(TAG, "Fatal: no ticket ID provided");
        Log.i(TAG, "onCreate " + this.ticketID);
        this.instance = this;
    }

    @Override
    public void onResume() {
        super.onResume();
        redrawLayout();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_order_processing, container, false);

        ((MainActivity) getActivity()).setActionBarTitle("Ticket: " + order.TicketID);
        Log.d(TAG, "Order: "+order);
        ViewGroup mainLayout = root.findViewById(R.id.order_process_main_layout);
        ((TextView)mainLayout.findViewById(R.id.order_process_ticket)).setText(order.TicketID);
        ((TextView)mainLayout.findViewById(R.id.order_process_order)).setText(order.OrderID);
        ((TextView)mainLayout.findViewById(R.id.order_process_company)).setText(order.Company);
        LinearLayout stepsLayout = mainLayout.findViewById(R.id.order_steps_layout);

        this.inflater = inflater;
        this.stepsLayout = stepsLayout;

        redrawLayout();
        return root;
    }

    private void showOrder(ProcessStep step)
    {
        View stepView = inflater.inflate(R.layout.order_processing_step_item, stepsLayout, false);
        ((TextView)stepView.findViewById(R.id.component_name)).setText(step.name);
        stepsLayout.addView(stepView);
        CheckBox stepCheckbox = stepView.findViewById(R.id.step_done);
        stepCheckbox.setChecked(step.status == ProcessStep.Status.DONE);
        stepCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CheckBox)view).isChecked())
                    step.status = ProcessStep.Status.DONE;
                else
                    step.status = ProcessStep.Status.IGNORE;
                redrawLayout();
            }
        });
        if(step.type == 2)
        {
            TextView items = stepView.findViewById(R.id.items);
            items.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();

            if(order.Server != null)
                sb.append(order.Server + "\n");
            for(ComponentProcess component : order.Components)
            {
                sb.append(" - " + component.Quantity + "x" + component.Name + "\n");
            }
            items.setText(sb.toString());
            items.setTextSize(getResources().getDimension(R.dimen.text_size_small));
            if(items.getParent() != null)
                ((ViewGroup)items.getParent()).removeView(items);
            ((LinearLayout)stepView.findViewById(R.id.step_items)).addView(items);
        }
        if(step.type == 1)
        {
            Button buildButton = stepView.findViewById(R.id.build_button);
            buildButton.setVisibility(View.VISIBLE);
            buildButton.setText("Start");
            buildButton.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString(ComponentTestFragment.AGR_TICKET_ID, ticketID);
                NavHostFragment.findNavController(this).navigate(R.id.componentTestFragment, args);
            });
            //buildButton.setCol(getResources().getColor(R.color.white));
        }
    }

    private void redrawLayout()
    {
        //Log.d(TAG,"RedraWLayout");
        stepsLayout.removeAllViewsInLayout();
        for(int i = 0; i < order.OrderSteps.size(); i++)
        {
            ProcessStep step = order.OrderSteps.get(i);
            if(step.status != ProcessStep.Status.NOT_STARTED)
            {
                showOrder(step);
                if(i == order.OrderSteps.size()-1)
                {
                    Button nextStep = new Button(MainActivity.getContext());
                    nextStep.setText("DONE");
                    nextStep.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG,"Order done!");
                            order.Status = OrderProcess.OrderStatus.BUILD_DONE;
                            Log.d("ORDER DONE: ", order.RedminePrint());
                            RedmineConnector.getInstance().UpdateIssue(order);
                            Snackbar.make(view, "Sending update to Redmine", Snackbar.LENGTH_LONG).show();
                            NavHostFragment.findNavController(instance).navigate(R.id.ordersFragment);
                        }
                    });
                    stepsLayout.addView(nextStep);
                }
            }
            else
            {
                showOrder(step);
                Button nextStep = new Button(MainActivity.getContext());
                nextStep.setText("Skip step");
                nextStep.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(step.mandatory)
                        {
                            Snackbar.make(view, "This step is mandatory", Snackbar.LENGTH_SHORT).show();
                                    //.setAction("Action", null).show();
                            return;
                        }
                        step.status = ProcessStep.Status.IGNORE;
                        redrawLayout();
                    }
                });
                stepsLayout.addView(nextStep);
                break;
            }
        }
    }
}