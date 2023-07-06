package com.samtechs.thehinjuapp.ui.main.tokens;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TokensViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public TokensViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is tokens fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}