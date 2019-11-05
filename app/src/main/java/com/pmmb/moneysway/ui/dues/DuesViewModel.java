package com.pmmb.moneysway.ui.dues;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DuesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public DuesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dues fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}