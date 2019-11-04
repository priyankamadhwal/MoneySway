package com.pmmb.moneysway.ui.exportreports;

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

public class ExportReportsFragment extends Fragment {

    private ExportReportsViewModel exportReportsViewViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        exportReportsViewViewModel =
                ViewModelProviders.of(this).get(ExportReportsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_export_reports, container, false);
        final TextView textView = root.findViewById(R.id.text_export_reports);
        exportReportsViewViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}