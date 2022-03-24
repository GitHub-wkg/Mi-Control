package com.google.android.gms.ads.ez.applovin;

import android.app.Activity;
import android.content.Context;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.ez.AdsFactory;
import com.google.android.gms.ads.ez.EzAdControl;
import com.google.android.gms.ads.ez.LogUtils;
import com.google.android.gms.ads.ez.adparam.AdUnit;

public class ApplovinUtils extends AdsFactory {

    public static ApplovinUtils INSTANCE;
    private final String TAG = "ApplovinUtils";

    public static ApplovinUtils getInstance(Activity context) {
        if (INSTANCE == null) {
            INSTANCE = new ApplovinUtils(context);
        }
        INSTANCE.mContext = context;
        return INSTANCE;
    }


    public void init() {
        LogUtils.logString(ApplovinUtils.class, "init");
        // Please make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance(mContext).setMediationProvider("max");
        AppLovinSdk.initializeSdk(mContext, new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration) {
                // AppLovin SDK is initialized, start loading ads
                LogUtils.logString(ApplovinUtils.class, "init Success");
                isInit = true;
                loadAds();
            }
        });
    }


    private MaxInterstitialAd interstitialAd;
    private boolean isInit;


    public ApplovinUtils(Activity mContext) {
        super(mContext);
    }


    @Override
    public void loadAds() {
        if (!isInit) {
            return;
        }
        String id = AdUnit.getApplovinInterId();
        LogUtils.logString(this, "Load Applovin With Id " + id);
        if (id.equals("")) {
            stateOption.setOnFailed();
            if (mListener != null) {
                mListener.onError();
            }
            return;
        }
        interstitialAd = new MaxInterstitialAd(id, mContext);
        interstitialAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                LogUtils.logString(ApplovinUtils.class, "ApplovinUtils Loaded");

                stateOption.setOnLoaded();
                // Show the ad
                if (mListener != null) {
                    mListener.onLoaded();
                }
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                LogUtils.logString(ApplovinUtils.class, "ApplovinUtils onAdDisplayed");
                stateOption.setShowAd();
                if (mListener != null) {
                    mListener.onDisplay();
                }

            }

            @Override
            public void onAdHidden(MaxAd ad) {
                LogUtils.logString(ApplovinUtils.class, "ApplovinUtils onAdHidden");

                stateOption.setDismisAd();
                if (mListener != null) {
                    mListener.onClosed();
                }
                EzAdControl.getInstance(mContext).loadAd();
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                LogUtils.logString(ApplovinUtils.class, "ApplovinUtils onAdClicked");
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                LogUtils.logString(ApplovinUtils.class, "ApplovinUtils onAdLoadFailed " + error);

                stateOption.setOnFailed();
                if (mListener != null) {
                    mListener.onError();
                }
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                LogUtils.logString(ApplovinUtils.class, "ApplovinUtils onAdDisplayFailed");
                if (mListener != null) {
                    mListener.onDisplayFaild();
                }
            }
        });

        // Load the first ad


        if (stateOption.isLoading()) {
            // neu dang loading  thi k load nua
        } else if (stateOption.isLoaded()) {
            // neu da loaded thi goi callback luon
            if (mListener != null) {
                mListener.onLoaded();
            }
        } else {
            // neu k loading cung k loaded thi goi ham load ads va dat loading = true

            LogUtils.logString(ApplovinUtils.class, "Load ApplovinUtils: Start Loading ");
            interstitialAd.loadAd();
            stateOption.setOnLoading();
        }
    }

    @Override
    public boolean showAds() {
        if (stateOption.isLoaded() && mContext != null && interstitialAd.isReady()) {
            interstitialAd.showAd();
            stateOption.setShowAd();
            return true;
        }
        return false;
    }
}
