package com.google.android.gms.ads.ez.admob;

import android.content.Context;

import com.google.android.gms.ads.ez.LogUtils;
import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;

public class AdmobNativeAdUtils {
    private NativeAd nativeAd;
    private Context mContext;
    private AdListener mListener;
    private static AdmobNativeAdUtils INSTANCE;

    public static AdmobNativeAdUtils getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AdmobNativeAdUtils(context);
        }
        return INSTANCE;
    }

    public AdmobNativeAdUtils(Context context) {
        mContext = context;
        INSTANCE = this;
        loadAd();
    }

    public NativeAd getNativeAd() {
        return nativeAd;
    }

    public AdmobNativeAdUtils setListener(AdListener mListener) {
        this.mListener = mListener;
        return this;
    }

    public void loadAd() {

        nativeAd = null;
        LogUtils.logString(AdmobNativeAdUtils.class, AdUnit.getAdmobNativeId());


        AdLoader.Builder builder = new AdLoader.Builder(mContext, AdUnit.getAdmobNativeId())
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(NativeAd nati) {
                        nativeAd = nati;
                    }
                });


        builder.withNativeAdOptions(new NativeAdOptions.Builder().setVideoOptions(new VideoOptions.Builder().setStartMuted(true).build()).build());
        builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                LogUtils.logString(AdmobNativeAdUtils.class, "onAdFailedToLoad " + adError);
                if (mListener != null) {
                    mListener.onAdFailedToLoad(adError);
                }
            }
            @Override
            public void onAdClicked() {
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (nativeAd != null) {
                    LogUtils.logString(AdmobNativeAdUtils.class, "onAdLoaded " + nativeAd.getHeadline());
                }
                if (mListener != null) {
                    mListener.onAdLoaded();
                }
            }
        }).build().loadAd(new AdRequest.Builder().build());



    }
}
