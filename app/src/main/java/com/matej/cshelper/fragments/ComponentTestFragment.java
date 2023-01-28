package com.matej.cshelper.fragments;

import android.content.res.ColorStateList;
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
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.matej.cshelper.MainActivity;
import com.matej.cshelper.R;
import com.matej.cshelper.core.InstanceProvider;
import com.matej.cshelper.fragments.helpers.OrderProcessingManager;
import com.matej.cshelper.storage.ComponentProcess;
import com.matej.cshelper.storage.OrderProcess;
import com.matej.cshelper.storage.ProcessStep;

public class ComponentTestFragment extends Fragment implements OrderProcessingManager.GetOrderCallback {

    public static String AGR_TICKET_ID;

    private static String TAG = "ComponentTestFragment";

    private OrderProcess order;
    private ViewGroup mainLayout = null;
    private LayoutInflater inflater;
    private ComponentTestFragment instance;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String ticketID = getArguments().getString(AGR_TICKET_ID);
            OrderProcessingManager.getInstance().GetOrder(ticketID,this);
            Log.i(TAG, "onCreate " + ticketID);
            ((MainActivity) getActivity()).setActionBarTitle("Ticket: " + ticketID);
        }
        else
            Log.e(TAG, "Fatal: no ticket ID provided");
        instance = this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_component_test, container, false);
        this.mainLayout = root.findViewById(R.id.component_test_main_list);
        this.inflater = inflater;

        return root;
    }

    @Override
    public void onGetOrderSuccess(OrderProcess order) {
        this.order = order;
        ((TextView)mainLayout.findViewById(R.id.server_name)).setText(order.Server);
        for(ComponentProcess component : order.Components)
        {
            View checklist = inflater.inflate(R.layout.component_test_step_item, mainLayout, false);
            LinearLayout layout = checklist.findViewById(R.id.step_items);
            TextView componentName = checklist.findViewById(R.id.component_name);
            componentName.setText(new StringBuilder().append(component.Quantity).append("x ").append(component.Name).toString());
            for(String step : component.steps.keySet())
            {
                CheckBox checkBox = new CheckBox(MainActivity.getContext());
                checkBox.setText(step);
                checkBox.setChecked(component.steps.get(step));
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    Log.d(TAG,"steps.put " + step + isChecked);
                        component.steps.put(step,isChecked);
                    OrderProcessingManager.getInstance().saveFirebaseOrder(order);
                });
                ColorStateList colorStateList = new ColorStateList(
                        new int[][]{
                                new int[]{android.R.attr.state_enabled} //enabled
                        },
                        new int[] {checkBox.getResources().getColor(R.color.grey) }
                );
                checkBox.setTextColor(colorStateList);
                checkBox.setButtonTintList(colorStateList);
                layout.addView(checkBox);
            }

            mainLayout.addView(checklist);
        }
        Button done = new Button(MainActivity.getContext());
        done.setText("DONE");
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OrderProcessingManager.getInstance().saveFirebaseOrder(order);
                Bundle args = new Bundle();
                args.putString(OrderProcessingFragment.ARG_TICKET_ID, order.TicketID);
                NavHostFragment.findNavController(instance).navigate(R.id.orderProcessingFragment,args);

            }
        });
        mainLayout.addView(done);
    }

    @Override
    public void onGetOrderFail(String message) {

    }
}