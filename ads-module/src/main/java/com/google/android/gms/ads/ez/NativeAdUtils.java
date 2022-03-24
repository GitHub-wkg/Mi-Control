package com.google.android.gms.ads.ez;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.ez.admob.AdmobNativePartial;

public class NativeAdUtils {
    protected AdmobNativePartial admobNativePartial;
    private Context mContext;

    private static NativeAdUtils INSTANCE;

    public static void init(Context context) {
        INSTANCE = new NativeAdUtils(context);
    }

    public static NativeAdUtils getInstance() {
        return INSTANCE;
    }

    public NativeAdUtils(Context context) {
        mContext = context;
        // khoi tao de load ad lan dau
        admobNativePartial = new AdmobNativePartial(mContext);
    }

    public boolean isLoaded() {
        admobNativePartial.loadAdFromQueue();
        return admobNativePartial.isLoaded();
    }

    public RelativeLayout getNativeAdView() {
        LogUtils.logString(NativeAdUtils.class, "getNativeAdView");
        if (admobNativePartial.getParent() != null) {
            ((ViewGroup) admobNativePartial.getParent()).removeView(admobNativePartial);
        }
        admobNativePartial.loadAdFromGoogle();
        return admobNativePartial;
    }
}
