package com.google.android.gms.ads.ez;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;

public class BannerAd extends RelativeLayout {
    private final String TAG = "BannerAd";
    private AdView fbBanner;
    private com.google.android.gms.ads.AdView admobBanner;
    private Context mContext;
    private boolean isReloadFB = false;
    private AdManagerAdView adxBanner;
    private MaxAdView applovinBanner;

    private void removeAds() {
        removeAllViews();
        setVisibility(GONE);
    }

    public BannerAd(Context context) {
        super(context);

        mContext = context;


        loadAdmob();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        setLayoutParams(layoutParams);
    }

    public BannerAd(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mContext = context;


        loadAdmob();


        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        setLayoutParams(layoutParams);
    }


    private void loadFacebookBanner() {


        fbBanner = new AdView(mContext, AdUnit.getFacebookBannerId(), com.facebook.ads.AdSize.BANNER_HEIGHT_50);


        AdListener adListener = new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                loadAdmob();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                removeAllViews();
                addView(fbBanner);
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        };

        fbBanner.loadAd(fbBanner.buildLoadAdConfig().withAdListener(adListener).build());
    }

    private void loadAdmob() {

        admobBanner = new com.google.android.gms.ads.AdView(mContext);
        admobBanner.setAdSize(getAdSize());
        admobBanner.setAdUnitId(AdUnit.getAdmobBannerId());
        AdRequest adRequest = new AdRequest.Builder().build();
        admobBanner.loadAd(adRequest);
        admobBanner.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.e(TAG, "onAdFailedToLoad: ");
                loadApplovin();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.e(TAG, "onAdLoaded: ");
                removeAllViews();
                addView(admobBanner);
            }
        });


    }


    private void loadAdx() {

        adxBanner = new AdManagerAdView(mContext);

        adxBanner.setAdSizes(getAdSize());

        adxBanner.setAdUnitId(AdUnit.getAdxBannerId());

        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        adxBanner.loadAd(adRequest);

        adxBanner.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdLoaded() {
                Log.e(TAG, "Adx onAdLoaded: ");
                removeAllViews();
                addView(adxBanner);
                setVisibility(View.VISIBLE);
            }


            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                Log.e(TAG, "Adx onAdFailedToLoad: ");
                setVisibility(GONE);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }


            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
            }
        });

    }

    private void loadApplovin() {
        if(AdUnit.getApplovinBannerId().equals("")){
            loadAdx();
        }
        applovinBanner = new MaxAdView(AdUnit.getApplovinBannerId(), (Activity) mContext);
        applovinBanner.setListener(new MaxAdViewAdListener() {
            @Override
            public void onAdExpanded(MaxAd ad) {

            }

            @Override
            public void onAdCollapsed(MaxAd ad) {

            }

            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.e(TAG, "onAdLoaded: applovin" );
                removeAllViews();
                addView(applovinBanner);
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {

            }

            @Override
            public void onAdHidden(MaxAd ad) {

            }

            @Override
            public void onAdClicked(MaxAd ad) {

            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.e(TAG, "onAdLoadFailed: applovin " + error );
                loadAdx();
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {

            }
        });

        // Stretch to the width of the screen for banners to be fully functional
        int width = ViewGroup.LayoutParams.MATCH_PARENT;

        // Banner height on phones and tablets is 50 and 90, respectively
        int heightPx = getResources().getDimensionPixelSize(R.dimen._45sdp);

        applovinBanner.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));

        // Set background or background color for banners to be fully functional
        applovinBanner.setBackgroundColor(Color.RED);


        // Load the ad
        applovinBanner.loadAd();

    }

    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        try {
//            Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = mContext.getResources().getDisplayMetrics();
//            display.getMetrics(outMetrics);

            float widthPixels = outMetrics.widthPixels;
            float density = outMetrics.density;

            int adWidth = (int) (widthPixels / density);
            Log.e(TAG, "getAdSize: " + adWidth + "  " + outMetrics.widthPixels);
            // Step 3 - Get adaptive ad size and return for setting on the ad view.
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mContext, adWidth);
        } catch (Exception e) {
            Log.e(TAG, "getAdSize: ", e);
            e.printStackTrace();
        }
        return com.google.android.gms.ads.AdSize.BANNER;
    }

}