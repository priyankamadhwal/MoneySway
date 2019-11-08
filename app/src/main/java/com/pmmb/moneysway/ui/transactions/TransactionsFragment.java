package com.pmmb.moneysway.ui.transactions;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pmmb.moneysway.R;

public class TransactionsFragment extends Fragment {

    private FloatingActionButton fab;
    private TransactionsViewModel transactionsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        transactionsViewModel =
                ViewModelProviders.of(this).get(TransactionsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_transactions, container, false);
        final TextView textView = root.findViewById(R.id.text_transactions);
        transactionsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        fab = root.findViewById(R.id.fab_transactions);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTransactionDialog();
            }
        });
        return root;
    }

    private void showAddTransactionDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_transaction, null);
        dialogBuilder.setView(dialogView);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        alertDialog.getWindow().setLayout((int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350, getResources().getDisplayMetrics())), WindowManager.LayoutParams.WRAP_CONTENT);
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel_transaction);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        Spinner categories_dropdown = dialogView.findViewById(R.id.spinner_categories);
        String[] items = new String[]{"Select a category", "Salary", "Bonus"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, items);
        categories_dropdown.setAdapter(adapter);

        categories_dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorText)); /* if you want your item to be white */
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner payment_methods_dropdown = dialogView.findViewById(R.id.spinner_payment_methods);
        String[] payment_methods_items = new String[]{"Select a payment method", "Cash", "Bank"};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, payment_methods_items);
        payment_methods_dropdown.setAdapter(adapter2);

        payment_methods_dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorText)); /* if you want your item to be white */
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }
}