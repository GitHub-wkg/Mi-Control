package com.google.android.gms.ads.ez;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.ez.utils.StateOption;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.Arrays;

public class RewardedAdUtils {

    private static RewardedAdUtils INSTANCE;


    public static RewardedAdUtils getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new RewardedAdUtils(context);
        }
        INSTANCE.mContext   =context;
        return INSTANCE;
    }


    public RewardedAdUtils(Context mContext) {
        this.mContext = mContext;
        loadAd();
    }

    private RewardedAd mRewardedAd;
    private Context mContext;
    private AdFactoryListener mListenner;
    private StateOption stateOption = StateOption.getInstance();

    public void loadAd() {

        stateOption.setOnLoading();
        AdRequest adRequest = new AdRequest.Builder().build();
        RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("292FA13BA1218092001E4E2A0EC83C31")).build();
        MobileAds.setRequestConfiguration(configuration);
        RewardedAd.load(mContext, "ca-app-pub-5025628276811480/1564526551",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        stateOption.setOnFailed();
                        LogUtils.logString(RewardedAdUtils.class, "Error " + loadAdError.getMessage());
                        mRewardedAd = null;

                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        stateOption.setOnLoaded();
                        mRewardedAd = rewardedAd;
                        LogUtils.logString(RewardedAdUtils.class, "Ad was loaded.");

                        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdShowedFullScreenContent() {
                                stateOption.setShowAd();
                                // Called when ad is shown.
                                LogUtils.logString(RewardedAdUtils.class, "Ad was shown.");
                                mRewardedAd = null;
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                LogUtils.logString(RewardedAdUtils.class, "Ad failed to show.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                stateOption.setDismisAd();
                                // Called when ad is dismissed.
                                // Don't forget to set the ad reference to null so you
                                // don't show the ad a second time.
                                LogUtils.logString(RewardedAdUtils.class, "Ad was dismissed.");
                            }
                        });
                    }
                });
    }

    public void showAds() {
        if (mRewardedAd != null && mContext instanceof Activity) {
            Activity activityContext = (Activity) mContext;
            mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                    LogUtils.logString(RewardedAdUtils.class, "The user earned the reward.");
                }
            });
        } else {
            LogUtils.logString(RewardedAdUtils.class, "The rewarded ad wasn't ready yet. " + (mContext instanceof Activity) +"   " +mRewardedAd);
        }
    }

}
