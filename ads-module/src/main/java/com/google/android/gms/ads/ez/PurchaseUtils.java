package com.google.android.gms.ads.ez;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PurchaseUtils implements BillingProcessor.IBillingHandler {
    public static final String TAG = "PurchaseUtils";
    private static String API_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuGpXDhe+RQBqPx57bINEJgta92z3QKW/oUmOqjiU8jJp+dxoSFUoL617+g/tkeib/Em55bUY/Kpp1F3IpoEE7AKBgXdUo9JHFT2h7Pf3TCG1JaK/Uyw7ZmhBULUfA287VO1rPiQSfUpQXvd91qlQYM6c9wKj/BJaw98086K6cph5Qj8Zgib8d47FRDFzAxvCSUi3Xmy83SMNQ8k2QchiCXUaGbMsOIGioY2AHxpgE/iZkNhulqxRCaaXd+jqVsKDoEokJ2t5U4rRubGJPo0vhgLFESEfx5HUEA0Du2XlLPPY+T8KGfNb6BuyYaGbGGUfCc/qhUqLZZde5L1+K0EsbwIDAQAB";
    public static final String REMOVE_ADS = "remove_ads";
    public static final String KEY_REMOVE_ADS_ONE_HOUR = "remove_ads_one_hour";
    public static final String TAG_REMOVED_ADS = "tag_removed_ads";
    public static final String ONE_MONTH = "one_month";
    public static final String THREE_MONTH = "three_months";
    public static final String SIX_MONTH = "six_month";
    public static final String LIFE_TIME = "life_time";
    private ArrayList<String> arrSub;
    private BillingProcessor bp;
    private Activity mActivity;


    private static PurchaseUtils INSTANCE;

    private HashMap<String, SkuDetails> hashSubDetail;

    public static PurchaseUtils getInstance() {
        return INSTANCE;
    }

    public static void init(Activity activity) {
        if (INSTANCE == null) {
            new PurchaseUtils(activity);
        }
    }

    public static boolean isPurchase() {


        if (getInstance() != null && getInstance().checkPurchase()) {
            return true;
        }
        return false;
    }


    public static void purchase(String id) {
        if (id.equals(LIFE_TIME)) {
            getInstance().callPurchase(id);
        } else {
            getInstance().callSubscribe(id);
        }
    }


    public PurchaseUtils(Activity context) {
        INSTANCE = this;
        mActivity = context;
        bp = new BillingProcessor(context, API_KEY, this);
        bp.initialize();


    }

    public static SkuDetails getDetailSub(String id) {
        if (INSTANCE != null) {
            return INSTANCE.getDetailSub1(id);
        }
        return null;
    }

    public SkuDetails getDetailSub1(String id) {
        if (bp != null) {
            return bp.getSubscriptionListingDetails(id);
        }
        return null;
    }

    public static SkuDetails getPurchaseDetail(String id) {
        if (INSTANCE != null) {
            return INSTANCE.getPurchaseDetail1(id);
        }
        return null;
    }

    public SkuDetails getPurchaseDetail1(String id) {
        return bp.getPurchaseListingDetails(id);
    }

//    public HashMap<String, SkuDetails> getSubDetail() {
//        try {
//            Log.e(TAG, "getSubDetail: getSubList " + bp.isSubscribed(ONE_MONTH));
//            List<BillingHistoryRecord> xx = bp.getPurchaseHistory(Constants.PRODUCT_TYPE_SUBSCRIPTION, null);
//            Log.e(TAG, "getSubDetail: getSubList " + xx.size() + "  " + xx.get(0).productId);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return hashSubDetail;
//    }

    private boolean checkPurchase() {
        if (bp != null && bp.isInitialized()) {
            Log.e(TAG, "checkPurchase: " + bp.isSubscribed(ONE_MONTH));
            Log.e(TAG, "checkPurchase: " + bp.isSubscribed(ONE_MONTH));
            if (bp.isPurchased(REMOVE_ADS)) {
                return true;
            }
            if (bp.isPurchased(LIFE_TIME)) {
                return true;
            }
            if (bp.isSubscribed(ONE_MONTH)) {
                return true;
            }
            if (bp.isSubscribed(SIX_MONTH)) {
                return true;
            }
            if (bp.isSubscribed(THREE_MONTH)) {
                return true;
            }
        }
        return false;
    }


    private void callPurchase(String id) {

        if (bp != null && bp.isInitialized()) {
            bp.purchase(mActivity, id);
        }
    }

    private void callSubscribe(String id) {

        if (bp != null && bp.isInitialized()) {
            bp.subscribe(mActivity, id);
        }
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
//        MySubject.getInstance().notifyChange(REMOVE_ADS);
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {

    }

    @Override
    public void onBillingInitialized() {
        arrSub = new ArrayList<>();
        arrSub.add(ONE_MONTH);
        arrSub.add(THREE_MONTH);
        arrSub.add(SIX_MONTH);
        arrSub.add(LIFE_TIME);
        List<SkuDetails> arrSubDetail = bp.getSubscriptionListingDetails(arrSub);
        Log.e(TAG, "onBillingInitialized: " + arrSubDetail);
        hashSubDetail = new HashMap<>();
        if (arrSubDetail != null) {
            for (SkuDetails item : arrSubDetail) {
                Log.e(TAG, "onBillingInitialized: " + item.productId);
                hashSubDetail.put(item.productId, item);
            }
        }
    }
}
