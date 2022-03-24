package com.google.android.gms.ads.ez.adparam;

public abstract class AdUnitFactory {
    private static AdUnitFactory INSTANCE;

    public static AdUnitFactory getInstance(boolean isTest) {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        if (isTest) {
            return new AdsParamUtilsTest();
        } else {
            return new AdsParamUtils();
        }
    }

    public AdUnitFactory() {
        INSTANCE = this;
    }

    public abstract String getAdmobInterId();

    public abstract String getAdmobNativeId();

    public abstract String getAdmobOpenId();

    public abstract String getAdmobBannerd();

    public abstract String getFacebookInterId();

    public abstract String getFacebookBannerId();

    public abstract String getFacebookNativeId();

    public abstract int getCountShowAds();

    public abstract int getLimitShowAds();

    public abstract long getTimeLastShowAds();

    public abstract String getIronSouceAppKey();


    public abstract String getInmobiAccountId();

    public abstract long getInmobiInterId();

    public abstract long getInmobiNativeId();

    public abstract long getInmobiBannerId();


    public abstract String getUnityAppId();

    public abstract String getUnityInterId();

    public abstract String getMopubInterId();

    public abstract String getMopubBannerId();

    public abstract String getMopubNativeId();

    public abstract String getMintegralAppId();

    public abstract String getMintegralAppKey();

    public abstract String getMintegralInterId();


    public abstract String getDisplayAppId();

    public abstract String getDisplayInterId();

    public abstract String getDisplayBannerId();

    public abstract String getDisplayNativeId();


    public abstract String getTappXId();


    public abstract String getMasterAdsNetwork();

    public abstract String getAdxInterId();

    public abstract String getAdxBannerId();

    public abstract String getAdxNativeId();


    public abstract String getApplovinBannerId();

    public abstract String getApplovinInterId();

    public abstract String getApplovinRewardId();
}
