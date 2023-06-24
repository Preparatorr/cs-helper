package com.matej.cshelper.fragments;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
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

public class OrderProcessingFragment extends Fragment implements OrderProcessingManager.GetOrderCallback {


    private static String TAG = "OrderProcessingFragment";
    public static final String ARG_TICKET_ID = "ticketID";

    private LayoutInflater inflater;
    private LinearLayout stepsLayout;

    private String ticketID;
    private OrderProcess order = null;
    private OrderProcessingFragment instance;
    private ViewGroup mainLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.ticketID = getArguments().getString(ARG_TICKET_ID);
            OrderProcessingManager.getInstance().GetOrder(this.ticketID, this);
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

        ((MainActivity) getActivity()).setActionBarTitle("Ticket: " + ticketID);
        mainLayout = root.findViewById(R.id.order_process_main_layout);
        LinearLayout stepsLayout = mainLayout.findViewById(R.id.order_steps_layout);
        this.inflater = inflater;
        this.stepsLayout = stepsLayout;
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
        switch(step.type)
        {
            case 1:
                Button buildButton = stepView.findViewById(R.id.build_button);
                buildButton.setVisibility(View.VISIBLE);
                buildButton.setText("Start");
                buildButton.setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    args.putString(ComponentTestFragment.AGR_TICKET_ID, ticketID);
                    NavHostFragment.findNavController(this).navigate(R.id.componentTestFragment, args);
                });
                break;
            case 2:
                TextView componentTestText = stepView.findViewById(R.id.items);
                componentTestText.setVisibility(View.VISIBLE);
                componentTestText.setText("Component test");

                LinearLayout componentTestLayout = stepView.findViewById(R.id.layout_item);

                for(ComponentProcess component : order.Components)
                {
                    CheckBox checkBox = new CheckBox(MainActivity.getContext());
                    checkBox.setText(component.Name);
                    checkBox.setChecked(component.CoponentCheck);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            component.CoponentCheck = b;
                            OrderProcessingManager.getInstance().saveFirebaseOrder(order);
                        }
                    });
                    componentTestLayout.addView(checkBox);
                }

                ImageView arrowTestComp = stepView.findViewById(R.id.collapse_button);
                arrowTestComp.setVisibility(View.VISIBLE);
                arrowTestComp.setOnClickListener(new View.OnClickListener() {
                    private boolean expanded = false;
                    @Override
                    public void onClick(View view) {
                        expanded = !expanded;
                        componentTestLayout.setVisibility(expanded? View.VISIBLE : View.GONE);
                        if(expanded)
                            ((ImageView)view).setImageResource(R.drawable.ic_baseline_expand_less_24);
                        else
                            ((ImageView)view).setImageResource(R.drawable.ic_baseline_expand_more_24);
                    }
                });
                break;
            case 3:

                TextView textView = stepView.findViewById(R.id.items);
                textView.setVisibility(View.VISIBLE);
                textView.setText("Mark external components");
                LinearLayout componentLayout = stepView.findViewById(R.id.layout_item);

                for(ComponentProcess component : order.Components)
                {
                    CheckBox checkBox = new CheckBox(MainActivity.getContext());
                    checkBox.setText(component.Name);
                    checkBox.setChecked(component.External);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            component.External = b;
                            OrderProcessingManager.getInstance().saveFirebaseOrder(order);
                        }
                    });
                    componentLayout.addView(checkBox);
                }

                ImageView arrow = stepView.findViewById(R.id.collapse_button);
                arrow.setVisibility(View.VISIBLE);
                arrow.setOnClickListener(new View.OnClickListener() {
                    private boolean expanded = false;
                    @Override
                    public void onClick(View view) {
                        expanded = !expanded;
                        componentLayout.setVisibility(expanded? View.VISIBLE : View.GONE);
                        if(expanded)
                            ((ImageView)view).setImageResource(R.drawable.ic_baseline_expand_less_24);
                        else
                            ((ImageView)view).setImageResource(R.drawable.ic_baseline_expand_more_24);
                    }
                });
                break;
            case 4:
                Button scanButton = stepView.findViewById(R.id.build_button);
                scanButton.setVisibility(View.VISIBLE);
                scanButton.setText("Scan");
                scanButton.setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    args.putString(OrderScanFragment.ARG_TICKET_ID, ticketID);
                    NavHostFragment.findNavController(this).navigate(R.id.orderScanFragment, args);
                });
                break;
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
                    EditText notes = new EditText(MainActivity.getContext());
                    notes.setHint("Add notes");
                    stepsLayout.addView(notes);
                    Button nextStep = new Button(MainActivity.getContext());
                    nextStep.setText("DONE");
                    nextStep.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            for(ProcessStep orderStep: order.OrderSteps)
                            {
                                if(orderStep.mandatory && orderStep.status != ProcessStep.Status.DONE)
                                {
                                    Snackbar.make(view, "Unfinished mandatory step " + orderStep.name, Snackbar.LENGTH_LONG).show();
                                    return;
                                }
                            }
                            Log.d(TAG,"Order done!");
                            order.Status = OrderProcess.OrderStatus.BUILD_DONE;
                            Log.d("ORDER DONE: ", order.RedminePrint());
                            if(notes.getText().length()>0)
                                order.Note += "\nMontáž poznámka: " + notes.getText().toString();
                            RedmineConnector.getInstance().UpdateIssue(order);

                            Snackbar.make(view, "Sending update to Redmine", Snackbar.LENGTH_LONG).show();
                            OrderProcessingManager.getInstance().saveFirebaseOrder(order);
                            Bundle args = new Bundle();
                            args.putSerializable(OrdersFragment.ARG_STATE, OrdersFragment.State.BUILD);
                            NavHostFragment.findNavController(instance).navigate(R.id.ordersFragment,args);
                        }
                    });
                    stepsLayout.addView(nextStep);
                }
            }
            else
            {
                showOrder(step);
                Button nextStep = new Button(MainActivity.getContext());
                nextStep.setText("Next step");
                nextStep.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(step.mandatory && step.status != ProcessStep.Status.DONE)
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
        OrderProcessingManager.getInstance().saveFirebaseOrder(order);
    }

    @Override
    public void onGetOrderSuccess(OrderProcess order) {
        this.order = order;
        this.order.Status = OrderProcess.OrderStatus.BUILD_START;
        //Log.d(TAG, "Order: "+order);
        ((TextView)mainLayout.findViewById(R.id.order_process_ticket)).setText(order.TicketID);
        ((TextView)mainLayout.findViewById(R.id.order_process_order)).setText(order.OrderID);
        ((TextView)mainLayout.findViewById(R.id.order_process_company)).setText(order.Company);
        if(order.Note != null && !order.Note.isEmpty())
            ((TextView)mainLayout.findViewById(R.id.note_from_prep)).setText(order.Note);
        else
            ((TextView)mainLayout.findViewById(R.id.note_from_prep)).setVisibility(View.GONE);
        redrawLayout();
        RedmineConnector.getInstance().addNote("Montáž serveru začala.", order.TicketID);
    }

    @Override
    public void onGetOrderFail(String message) {
        Log.e(TAG, "Order get fail" + message);
    }
}