package com.google.android.gms.ads.ez;


import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDexApplication;

import com.google.android.gms.ads.ez.admob.AdmobOpenAdUtils;
import com.google.android.gms.ads.ez.applovin.ApplovinRewardedUtils;
import com.google.android.gms.ads.ez.remote.AppConfigs;

public class EzApplication extends MultiDexApplication implements LifecycleObserver, Application.ActivityLifecycleCallbacks {
    private static EzApplication INSTANCE;

    public static EzApplication getInstance() {
        return INSTANCE;
    }

    private static Activity currentActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;


        registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        AppConfigs.getInstance(this);


    }


    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {


        if (currentActivity == null) {
            currentActivity = activity;
            EzAdControl.initAd(currentActivity);
            EzAdControl.getInstance(currentActivity).loadAd();
            ApplovinRewardedUtils.getInstance(currentActivity).loadAd();
            PurchaseUtils.init(currentActivity);
        }
        currentActivity = activity;
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
