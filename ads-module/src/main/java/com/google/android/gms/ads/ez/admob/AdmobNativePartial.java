package com.google.android.gms.ads.ez.admob;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.ez.AdFactoryListener;
import com.google.android.gms.ads.ez.R;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

public class AdmobNativePartial extends RelativeLayout {

    private NativeAd nativeAd;
    private NativeAdView nativeAdView;
    private Context mContext;
    private AdFactoryListener mListener;
    private boolean isLoaded = false;
    private boolean isLoading = false;


    public AdmobNativePartial(Context context) {
        super(context);
        mContext = context;
        initViews();
        // goi de load ad lan dau
        loadAdFromGoogle();
    }

    public AdmobNativePartial(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
        // goi de load ad lan dau
        loadAdFromGoogle();
    }

    public void loadAdFromQueue() {
        // lay ad da co san trong AdmobNativeAdUtils
        nativeAd = AdmobNativeAdUtils.getInstance(mContext).getNativeAd();
        if (nativeAd == null) {
            if (mListener != null) {
                mListener.onError();
            }
            isLoaded = false;
            return;
        }
        isLoaded = true;
        populateNativeAdView(nativeAd);


    }

    public void loadAdFromGoogle() {
        AdmobNativeAdUtils.getInstance(mContext).loadAd();
    }

    private void initViews() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.native_admob_item, this);
        nativeAdView = (NativeAdView) findViewById(R.id.ad_view);
        MediaView mediaView = (MediaView) nativeAdView.findViewById(R.id.ad_media);
        nativeAdView.setMediaView(mediaView);

        // Set other ad assets.
        nativeAdView.setHeadlineView(nativeAdView.findViewById(R.id.ad_headline));
        nativeAdView.setBodyView(nativeAdView.findViewById(R.id.ad_body));
        nativeAdView.setCallToActionView(nativeAdView.findViewById(R.id.ad_call_to_action));
        nativeAdView.setIconView(nativeAdView.findViewById(R.id.ad_app_icon));
        nativeAdView.setPriceView(nativeAdView.findViewById(R.id.ad_price));
        nativeAdView.setStoreView(nativeAdView.findViewById(R.id.ad_store));
        nativeAdView.setAdvertiserView(nativeAdView.findViewById(R.id.ad_advertiser));
    }

    public void setListener(AdFactoryListener mListener) {
        this.mListener = mListener;
    }

    public boolean isLoaded() {
        return isLoaded;
    }


    public boolean isLoading() {
        return isLoading;
    }


    private void populateNativeAdView(NativeAd nativeAd) {


        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) nativeAdView.getHeadlineView()).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            nativeAdView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            nativeAdView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) nativeAdView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            nativeAdView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            nativeAdView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) nativeAdView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            nativeAdView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) nativeAdView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            nativeAdView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            nativeAdView.getPriceView().setVisibility(View.GONE);
        } else {
            nativeAdView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) nativeAdView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            nativeAdView.getStoreView().setVisibility(View.GONE);
        } else {
            nativeAdView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) nativeAdView.getStoreView()).setText(nativeAd.getStore());
        }


        if (nativeAd.getAdvertiser() == null) {
            nativeAdView.getAdvertiserView().setVisibility(View.GONE);
        } else {
            ((TextView) nativeAdView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            nativeAdView.getAdvertiserView().setVisibility(View.VISIBLE);
        }




        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the nativeAdView's MediaView
        // with the media content from this native ad.
        nativeAdView.setNativeAd(nativeAd);
    }
}
