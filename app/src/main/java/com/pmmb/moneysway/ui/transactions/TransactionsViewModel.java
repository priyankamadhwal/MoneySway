package com.pmmb.moneysway.ui.transactions;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TransactionsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public TransactionsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is transactions fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}