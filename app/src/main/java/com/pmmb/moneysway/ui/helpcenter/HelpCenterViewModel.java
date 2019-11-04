package com.pmmb.moneysway.ui.helpcenter;

import android.provider.Settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HelpCenterViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HelpCenterViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is help center fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}