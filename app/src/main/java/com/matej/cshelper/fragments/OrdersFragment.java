package com.matej.cshelper.fragments;

import static com.matej.cshelper.fragments.OrdersFragment.State.BUILD;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.matej.cshelper.MainActivity;
import com.matej.cshelper.R;
import com.matej.cshelper.core.InstanceProvider;
import com.matej.cshelper.fragments.helpers.OrderListController;
import com.matej.cshelper.fragments.helpers.OrderProcessingManager;
import com.matej.cshelper.network.redmine.entities.Order;
import com.matej.cshelper.storage.OrderProcess;

import kotlin.NotImplementedError;

public class OrdersFragment extends Fragment {

    public enum State
    {
        PREPARATION,
        BUILD,
        CHECK,
        EXPEDITION
    }
    public static final String TAG = "OrdersFragment";
    public static final String ARG_STATE = "state";
    private State state;
    private boolean searchActive = false;
    private EditText searchInput;
    LinearLayout ordersLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            this.state = (State)getArguments().getSerializable(ARG_STATE);
        }
        else
        {
            throw new NotImplementedError();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        switch(this.state)
        {
            case PREPARATION:
                ((MainActivity) getActivity()).setActionBarTitle("Orders for preparation");
                break;
            case BUILD:
                ((MainActivity) getActivity()).setActionBarTitle("Orders for build");
                break;
            default:
                ((MainActivity) getActivity()).setActionBarTitle("Redmine orders");
        }

        View root = inflater.inflate(R.layout.fragment_orders, container, false);
        LinearLayout mainLayout = root.findViewById(R.id.orders_main_layout);
        searchInput = mainLayout.findViewById(R.id.text_input_edit_text);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchActive = searchInput.getText().length() > 0;
                refreshOrdersLayout();
            }
        });
        ordersLayout = mainLayout.findViewById(R.id.orders_list_layout);
        OrderListController controller = OrderListController.Instance();
        for(Order order : controller.ActiveOrders)
        {
            OrderProcessingManager.getInstance().GetOrder(order.TicketID, new OrderProcessingManager.GetOrderCallback() {
                @Override
                public void onGetOrderSuccess(OrderProcess processedOrder) {
                    boolean ignore = false;
                    switch (state){
                        case BUILD:
                            if(processedOrder.Status != OrderProcess.OrderStatus.COMPONENT_PREPARATION_DONE && processedOrder.Status != OrderProcess.OrderStatus.BUILD_START)
                                ignore = true;
                            break;
                        case PREPARATION:
                            if(processedOrder.Status != OrderProcess.OrderStatus.NEW && processedOrder.Status != OrderProcess.OrderStatus.COMPONENT_PREPARATION_START)
                                ignore = true;
                        default:
                            break;
                    }

                    if(ignore)
                        return;
                    View item = inflater.inflate(R.layout.order_list_item,(ViewGroup) root,false);
                    TextView ticketID = item.findViewById(R.id.order_ticket_id);
                    ticketID.setText(order.TicketID);
                    TextView orderID = item.findViewById(R.id.order_order_id);
                    orderID.setText(order.OrderID);
                    TextView company = item.findViewById(R.id.order_company);
                    company.setText(order.Company);

                    item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Bundle data = new Bundle();
                            data.putString(OrderProcessingFragment.ARG_TICKET_ID,order.TicketID);
                            openOrder(data);
                        }
                    });

                    TextView orderStatus = item.findViewById(R.id.order_status);
                    switch(processedOrder.Status)
                    {
                        case NEW:
                            orderStatus.setText("New");
                            //orderStatus.setTextColor(getResources().getColor(R.color.green));
                            break;
                        case BUILD_START:
                        case COMPONENT_PREPARATION_START:
                            orderStatus.setText("Started");
                            orderStatus.setTextColor(getResources().getColor(R.color.orange));
                            break;
                        case COMPONENT_PREPARATION_DONE:
                            if(state == State.PREPARATION)
                            {
                                orderStatus.setText("Done");
                                orderStatus.setTextColor(getResources().getColor(R.color.green));
                            }
                            else
                                orderStatus.setText("New");
                            break;
                    }
                    ordersLayout.addView(item);
                }

                @Override
                public void onGetOrderFail(String message) {
                    Log.e(OrdersFragment.TAG, "Order get fail" + message);
                }
            });

        }
        if(controller.ActiveOrders.size() == 0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.myDialog));
            // Add the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                }
            });
            builder.setTitle("No connection to Redmine! Please restart app");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return root;
    }

    private void openOrder(Bundle args)
    {
        switch(this.state)
        {
            case PREPARATION:
                NavHostFragment.findNavController(this).navigate(R.id.componentPreparationFragment, args);
                break;
            case BUILD:
                NavHostFragment.findNavController(this).navigate(R.id.orderProcessingFragment, args);
                break;
        }

    }

    private void refreshOrdersLayout()
    {
        for(int i=0; i < ordersLayout.getChildCount(); i++)
        {
            View view = ordersLayout.getChildAt(i);
            TextView ticketID = view.findViewById(R.id.order_ticket_id);
            TextView orderID = view.findViewById(R.id.order_order_id);
            TextView company = view.findViewById(R.id.order_company);
            if(searchActive)
            {
                String searchString = searchInput.getText().toString().toLowerCase();
                if(ticketID.getText().toString().toLowerCase().contains(searchString) ||
                        orderID.getText().toString().toLowerCase().contains(searchString) ||
                        company.getText().toString().toLowerCase().contains(searchString))
                {
                    view.setVisibility(View.VISIBLE);
                }
                else
                    view.setVisibility(View.GONE);
            }
            else
                view.setVisibility(View.VISIBLE);
        }
    }
}