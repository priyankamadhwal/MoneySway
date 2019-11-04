package com.pmmb.moneysway.ui.exportreports;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ExportReportsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ExportReportsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is export reports fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}