package com.pmmb.moneysway.ui.budgets;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BudgetsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public BudgetsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is budgets fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}