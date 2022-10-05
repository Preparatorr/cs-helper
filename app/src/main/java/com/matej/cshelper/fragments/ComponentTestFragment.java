package com.matej.cshelper.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.matej.cshelper.MainActivity;
import com.matej.cshelper.R;
import com.matej.cshelper.core.InstanceProvider;
import com.matej.cshelper.fragments.helpers.OrderProcessingManager;
import com.matej.cshelper.storage.ComponentProcess;
import com.matej.cshelper.storage.OrderProcess;
import com.matej.cshelper.storage.ProcessStep;

public class ComponentTestFragment extends Fragment {

    public static String AGR_TICKET_ID;

    private static String TAG = "ComponentTestFragment";

    private OrderProcess order;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String ticketID = getArguments().getString(AGR_TICKET_ID);
            this.order = ((OrderProcessingManager) InstanceProvider.GetInstance(OrderProcessingManager.class)).GetOrder(ticketID);
            Log.i(TAG, "onCreate " + ticketID);
            ((MainActivity) getActivity()).setActionBarTitle("Ticket: " + ticketID);
        }
        else
            Log.e(TAG, "Fatal: no ticket ID provided");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_component_test, container, false);
        LinearLayout mainLayout = root.findViewById(R.id.component_test_main_list);
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
                    if (isChecked) {
                        component.steps.put(step,true);
                    } else {
                        component.steps.put(step,true);
                    }
                });
                ColorStateList colorStateList = new ColorStateList(
                        new int[][]{
                                new int[]{android.R.attr.state_enabled} //enabled
                        },
                        new int[] {checkBox.getResources().getColor(R.color.white) }
                );
                checkBox.setTextColor(colorStateList);
                checkBox.setButtonTintList(colorStateList);
                layout.addView(checkBox);
            }

            mainLayout.addView(checklist);
        }
        return root;
    }
}