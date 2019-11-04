package com.pmmb.moneysway.ui.calendarview;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CalendarViewViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CalendarViewViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is calendar view fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}