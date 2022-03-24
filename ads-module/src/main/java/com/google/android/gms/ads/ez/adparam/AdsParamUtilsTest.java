package com.google.android.gms.ads.ez.adparam;

import android.content.Context;

import com.google.android.gms.ads.ez.EzAdControl;
import com.google.android.gms.ads.ez.remote.AppConfigs;
import com.google.android.gms.ads.ez.remote.RemoteKey;


public class AdsParamUtilsTest extends AdUnitFactory {


    @Override
    public String getAdmobInterId() {
        return "ca-app-pub-3940256099942544/1033173712";
    }

    @Override
    public String getAdmobNativeId() {
        return "ca-app-pub-3940256099942544/2247696110";
    }

    @Override
    public String getAdmobOpenId() {
        return "ca-app-pub-3940256099942544/3419835294";
    }

    @Override
    public String getAdmobBannerd() {
        return "ca-app-pub-3940256099942544/6300978111";
    }

    @Override
    public String getFacebookInterId() {
        return "256135302826650_256136129493234_";
    }

    @Override
    public String getFacebookBannerId() {
        return "270481753652191_602364100463953";
    }

    @Override
    public String getFacebookNativeId() {
        return "270481753652191_612014289498934";
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
        return "24534e1901884e398f1253216226017e";
    }

    @Override
    public String getMopubBannerId() {
        return "b195f8dd8ded45fe847ad89ed1d016da";
    }

    @Override
    public String getMopubNativeId() {
        return "11a17b188668469fb0412708c3d16813";
    }

    @Override
    public String getMintegralAppId() {
        return "118690";
    }

    @Override
    public String getMintegralAppKey() {
        return "7c22942b749fe6a6e361b675e96b3ee9";
    }

    @Override
    public String getMintegralInterId() {
        return "146871";
    }

    @Override
    public String getDisplayAppId() {
        return "6494";
    }

    @Override
    public String getDisplayInterId() {
        return "4654";
    }

    @Override
    public String getDisplayBannerId() {
        return "6428";
    }

    @Override
    public String getDisplayNativeId() {
        return "6429";
    }

    @Override
    public String getTappXId() {
        Context context = EzAdControl.getContext();
        if (context == null) {
            return "";
        }
        String locale = context.getResources().getConfiguration().locale.getCountry();
        switch (locale) {
            case "US":
                return "pub-53735-android-2058";
            case "UK":
                return "pub-53800-android-9392";
            case "IT":
                return "pub-53800-android-9392";
            case "SP":
                return "pub-53800-android-9392";
            case "KR":
                return "pub-53800-android-9392";
            case "JP":
                return "pub-53800-android-9392";
            case "CA":
                return "pub-53800-android-9392";
            case "AU":
                return "pub-53800-android-9392";
            case "VN":
                return "pub-53990-android-0871";
            default:
                return "pub-53801-android-0358";
        }
    }

    @Override
    public String getMasterAdsNetwork() {
        return AppConfigs.getString(RemoteKey.MASTER_ADS_NETWORK);
    }

    @Override
    public String getAdxInterId() {
        return "/6499/example/interstitial";
    }

    @Override
    public String getAdxBannerId() {
        return null;
    }

    @Override
    public String getAdxNativeId() {
        return "/6499/example/native";
    }

    @Override
    public String getApplovinBannerId() {
        return "4400a73227b4e77f";
    }

    @Override
    public String getApplovinInterId() {
        return "c3a0cadabefea4e7";
    }

    @Override
    public String getApplovinRewardId() {
        return "8a24e5b04e6fdf66";
    }

    @Override
    public int getCountShowAds() {
        return 1;
    }

    @Override
    public int getLimitShowAds() {
        return 50;
    }

    @Override
    public long getTimeLastShowAds() {
        return 10;
    }

    @Override
    public String getIronSouceAppKey() {
        return "8f2a8d75";
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
