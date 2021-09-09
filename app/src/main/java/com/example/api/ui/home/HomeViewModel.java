package com.example.api.ui.home;

import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    public Bundle webViewState;
    public String token2;

    public void setToken(String token) {
        this.token2 = token;
    }

    public String getToken() {
        return token2;
    }

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public Bundle getWebViewState() {
        return webViewState;
    }

    public void initWebViewState() {
        this.webViewState=new Bundle();
      //使用前要先初始化，大坑
    }

    public LiveData<String> getText() {
        return mText;
    }
}