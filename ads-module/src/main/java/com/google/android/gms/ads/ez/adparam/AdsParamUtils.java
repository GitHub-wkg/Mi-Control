package com.google.android.gms.ads.ez.adparam;


import com.google.android.gms.ads.ez.remote.AppConfigs;
import com.google.android.gms.ads.ez.remote.RemoteKey;

public class AdsParamUtils extends AdUnitFactory {

    @Override
    public String getAdmobInterId() {
        return AppConfigs.getString(RemoteKey.ADMOB_INTER_ID);
    }

    @Override
    public String getAdmobNativeId() {
        return AppConfigs.getString(RemoteKey.ADMOB_NATIVE_ID);
    }

    @Override
    public String getAdmobOpenId() {
        return AppConfigs.getString(RemoteKey.ADMOB_OPEN_ID);
    }

    @Override
    public String getAdmobBannerd() {
        return AppConfigs.getString(RemoteKey.ADMOB_BANNER_ID);
    }

    @Override
    public String getFacebookInterId() {
        return AppConfigs.getString(RemoteKey.FB_INTER_ID);
    }

    @Override
    public String getFacebookBannerId() {
        return AppConfigs.getString(RemoteKey.FB_BANNER_ID);
    }

    @Override
    public String getFacebookNativeId() {
        return AppConfigs.getString(RemoteKey.FB_NATIVE_ID);
    }

    @Override
    public String getUnityInterId() {
        return "admobInterMediation";
    }

    @Override
    public String getUnityAppId() {
        return "2911003";
    }

    @Override
    public String getMopubInterId() {
        return "1beeab2329484e739513d1630168bbe6";
    }

    @Override
    public String getMopubBannerId() {
        return "56a521be7dd44f408703220c968794d7";
    }

    @Override
    public String getMopubNativeId() {
        return "962d67169f59484cb4c2b0f5105caa7e";
    }

    @Override
    public String getMintegralAppId() {
        return "126043";
    }

    @Override
    public String getMintegralAppKey() {
        return "4846bbbfacb9ba40f38ff5a425e37ed4";
    }

    @Override
    public String getMintegralInterId() {
        return "229746";
    }

    @Override
    public String getDisplayAppId() {
        return "8565";
    }

    @Override
    public String getDisplayInterId() {
        return "6795";
    }

    @Override
    public String getDisplayBannerId() {
        return "6737";
    }

    @Override
    public String getDisplayNativeId() {
        return "6796";
    }

    @Override
    public String getTappXId() {
        return "pub-53735-android-2058";
    }

    @Override
    public String getMasterAdsNetwork() {
        return AppConfigs.getString(RemoteKey.MASTER_ADS_NETWORK);
    }


    @Override
    public String getAdxInterId() {
        return AppConfigs.getString(RemoteKey.ADX_INTER_ID);
    }

    @Override
    public String getAdxBannerId() {
        return AppConfigs.getString(RemoteKey.ADX_BANNER_ID);
    }

    @Override
    public String getAdxNativeId() {
        return AppConfigs.getString(RemoteKey.ADX_NATIVE_ID);
    }

    @Override
    public String getApplovinBannerId() {
        return AppConfigs.getString(RemoteKey.APPLOVIN_BANNER_ID);
    }

    @Override
    public String getApplovinInterId() {
        return AppConfigs.getString(RemoteKey.APPLOVIN_INTER_ID);
    }

    @Override
    public String getApplovinRewardId() {
        return AppConfigs.getString(RemoteKey.APPLOVIN_REWARDED_ID);
    }

    @Override
    public int getCountShowAds() {
        return 1;
    }

    @Override
    public int getLimitShowAds() {
        return AppConfigs.getInt(RemoteKey.MAX_SHOW_DAY);
    }

    @Override
    public long getTimeLastShowAds() {
        return AppConfigs.getInt(RemoteKey.TIME_SHOW_ADS);
    }

    @Override
    public String getIronSouceAppKey() {
        return "8d642545";
    }


    @Override
    public String getInmobiAccountId() {
        return "a26168429abf4cb6a997bfd91d38ac7c";
    }

    @Override
    public long getInmobiInterId() {
        return 1552792860743L;
    }

    @Override
    public long getInmobiNativeId() {
        return 1584796855126L;
    }

    @Override
    public long getInmobiBannerId() {
        return 1543313250759L;
    }
}
