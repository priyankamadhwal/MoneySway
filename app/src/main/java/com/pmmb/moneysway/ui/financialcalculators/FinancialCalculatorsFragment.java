package com.pmmb.moneysway.ui.financialcalculators;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.pmmb.moneysway.R;

public class FinancialCalculatorsFragment extends Fragment {

    private FinancialCalculatorsViewModel financialCalculatorsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        financialCalculatorsViewModel =
                ViewModelProviders.of(this).get(FinancialCalculatorsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_financial_calculators, container, false);
        final TextView textView = root.findViewById(R.id.text_financial_calculators);
        financialCalculatorsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}