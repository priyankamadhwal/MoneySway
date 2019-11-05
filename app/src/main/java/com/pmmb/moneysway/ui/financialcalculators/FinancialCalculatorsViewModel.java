package com.pmmb.moneysway.ui.financialcalculators;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FinancialCalculatorsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public FinancialCalculatorsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is financial calculators fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}