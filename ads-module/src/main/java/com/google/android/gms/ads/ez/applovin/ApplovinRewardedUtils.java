package com.google.android.gms.ads.ez.applovin;

import android.app.Activity;
import android.os.Handler;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.google.android.gms.ads.ez.AdFactoryListener;
import com.google.android.gms.ads.ez.EzAdControl;
import com.google.android.gms.ads.ez.LogUtils;
import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.google.android.gms.ads.ez.utils.StateOption;

import java.util.concurrent.TimeUnit;

public class ApplovinRewardedUtils {


    public static ApplovinRewardedUtils INSTANCE;


    public static ApplovinRewardedUtils getInstance(Activity context) {
        if (INSTANCE == null) {
            INSTANCE = new ApplovinRewardedUtils(context);
        }
        INSTANCE.mActivity = context;
        return INSTANCE;
    }

    private StateOption stateOption = StateOption.getInstance();
    private AdFactoryListener adListener;
    public Activity mActivity;
    private MaxRewardedAd rewardedAd;
    private int retryAttempt;


    public ApplovinRewardedUtils(Activity acctivity) {
        this.mActivity = acctivity;
        String id = AdUnit.getApplovinRewardId();
        LogUtils.logString(ApplovinRewardedUtils.class, "Load reward with id " + id);
        if (id.equals("")) {
            return;
        }
        rewardedAd = MaxRewardedAd.getInstance(id, mActivity);
        rewardedAd.setListener(maxListener);
    }


    public void loadAd() {
        if (rewardedAd != null) {
            rewardedAd.loadAd();
            stateOption.setOnLoading();
        } else {
            stateOption.setOnFailed();
            if (adListener != null) {
                adListener.onError();
            }
        }
    }

    public void showAds() {
        if (rewardedAd != null && rewardedAd.isReady() && stateOption.isLoaded()) {
            rewardedAd.showAd();
        } else {
            LogUtils.logString(ApplovinRewardedUtils.class, "k show ddc rqward ");
            EzAdControl.getInstance(mActivity).setAdListener(new AdFactoryListener() {
                @Override
                public void onError() {
                    LogUtils.logString(ApplovinRewardedUtils.class, "inter error");
                }

                @Override
                public void onLoaded() {
                    LogUtils.logString(ApplovinRewardedUtils.class, "inter onLoaded");
                }

                @Override
                public void onDisplay() {
                    super.onDisplay();
                    if (adListener != null) {
                        adListener.onRewardVideoStart();
                    }
                    LogUtils.logString(ApplovinRewardedUtils.class, "onDisplay");
                }

                @Override
                public void onDisplayFaild() {
                    super.onDisplayFaild();
                    if (adListener != null) {
                        adListener.onDisplayFaild();
                    }
                    LogUtils.logString(ApplovinRewardedUtils.class, "onDisplayFaild");
                }

                @Override
                public void onClosed() {
                    super.onClosed();
                    if (adListener != null) {
                        adListener.onEarnedReward();
                    }
                    LogUtils.logString(ApplovinRewardedUtils.class, "onClosed");
                }
            }).showAds();
        }
    }

    public ApplovinRewardedUtils setAdListener(AdFactoryListener adListener) {
        this.adListener = adListener;
        return this;
    }

    private MaxRewardedAdListener maxListener = new MaxRewardedAdListener() {
        // MAX Ad Listener
        @Override
        public void onAdLoaded(final MaxAd maxAd) {
            // Rewarded ad is ready to be shown. rewardedAd.isReady() will now return 'true'
            // Reset retry attempt
            retryAttempt = 0;
            stateOption.setOnLoaded();
            if (adListener != null) {
                adListener.onLoaded();
            }
        }

        @Override
        public void onAdLoadFailed(final String adUnitId, final MaxError error) {
            // Rewarded ad failed to load
            // We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds)

            retryAttempt++;
            long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retryAttempt)));

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadAd();
                }
            }, delayMillis);

            stateOption.setOnFailed();
            if (adListener != null) {
                adListener.onError();
            }
        }

        @Override
        public void onAdDisplayFailed(final MaxAd maxAd, final MaxError error) {
            // Rewarded ad failed to display. We recommend loading the next ad
            rewardedAd.loadAd();
            if (adListener != null) {
                adListener.onDisplayFaild();
            }
        }

        @Override
        public void onAdDisplayed(final MaxAd maxAd) {
            stateOption.setShowAd();
            if (adListener != null) {
                adListener.onDisplay();
            }
        }

        @Override
        public void onAdClicked(final MaxAd maxAd) {
        }

        @Override
        public void onAdHidden(final MaxAd maxAd) {
            // rewarded ad is hidden. Pre-load the next ad
            loadAd();
            stateOption.setDismisAd();
            if (adListener != null) {
                adListener.onClosed();
            }
        }

        @Override
        public void onRewardedVideoStarted(final MaxAd maxAd) {
            if (adListener != null) {
                adListener.onRewardVideoStart();
            }
        }

        @Override
        public void onRewardedVideoCompleted(final MaxAd maxAd) {

        }

        @Override
        public void onUserRewarded(final MaxAd maxAd, final MaxReward maxReward) {
            // Rewarded ad was displayed and user should receive the reward
            if (adListener != null) {
                adListener.onEarnedReward();
            }
        }
    };
}
