package com.ezstudio.controlcenter.service.accessiblilityUtils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.File;
import java.util.List;

public class BlueLightFilterUtils {


    private static final String TAG = "BlueLightFilterUtils";
    private static final String ALL_DEVICE_LABEL = "quick_settings_night_display_label";
    private static final String ALL_DEVICE_LABEL_ON = "quick_settings_secondary_label_until";
    private static final String SAMSUNG_LOW_DEVICE_LABEL = "quick_settings_bluelightfilter_label";
    private static final String SAMSUNG_HIGH_DEVICE_LABEL = "quick_settings_eyecomfortshield_label";
    private static final String SAMSUNG_DEVICE_LABEL_ON = "accessibility_desc_on";

    public static final int NOT_CLICKABLE = 0; // k click đc
    public static final int CLICKABLE_ENABLE = 1; // click duoc - trang thai sau khi click la BAT >><< trong ham check thi no co nghia la dang bat
    public static final int CLICKABLE_DISABLE = 2; // click duoc - trang thai sau khi click la TAT >><< trong ham check thi no co nghia la dang tat


    public static String getStringByName(Context context, String name) {
        try {
            String resourcesPackageName = "com.android.systemui";
            Resources resources = context.getPackageManager().getResourcesForApplication(resourcesPackageName);
            int resourceId = resources.getIdentifier(name, "string", resourcesPackageName);
            if (resourceId > 0) {
                return resources.getString(resourceId);
            }
            return "";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static int checkEnableSamsung(Context context, AccessibilityNodeInfo nodeInfo) {

        if (nodeInfo == null) {
            return NOT_CLICKABLE;
        }


        String label = "";
        label = getSamsungLabel(context);

        int index = label.indexOf("\n");
        if (index != -1) {
            label = label.substring(0, index);
        }


        String labelOn = getStringByName(context, SAMSUNG_DEVICE_LABEL_ON);


        List<AccessibilityNodeInfo> list = nodeInfo
                .findAccessibilityNodeInfosByText(label);

        if (list == null || list.size() == 0) {
            return NOT_CLICKABLE;
        }
        for (AccessibilityNodeInfo child : list) {
            if (child != null) {
                Log.e(TAG, "checkEnableSamsung: " + child.getContentDescription());
                Log.e(TAG, "checkEnableSamsung: " + child.getText());
                if (child.getContentDescription() != null) {
                    if (child.getContentDescription().toString().indexOf(labelOn) != -1) {
                        return CLICKABLE_ENABLE;
                    }
                } else if (child.getText() != null) {
                    if (child.getText().toString().indexOf(labelOn) != -1) {
                        return CLICKABLE_ENABLE;
                    }
                }
            }

        }
        return CLICKABLE_DISABLE;
    }

    public static boolean checkDeviceSamsung() {
        try {
            if (Build.VERSION.class.getDeclaredField("SEM_PLATFORM_INT").getInt(null) - 90000 >= 0) {
                return true;
            }
            return false;
        } catch (Throwable unused) {
            return false;
        }
    }

    public static int checkEnable(Context context, AccessibilityNodeInfo nodeInfo) {

        if (nodeInfo == null) {
            return NOT_CLICKABLE;
        }

        if (checkDeviceSamsung()) {
            return checkEnableSamsung(context, nodeInfo);
        }

        boolean isOn = false;
        String label = "";
        label = getStringByName(context, ALL_DEVICE_LABEL);

        int index = label.indexOf("\n");
        if (index != -1) {
            label = label.substring(0, index);
        }


        String labelOn = getStringByName(context, ALL_DEVICE_LABEL_ON);
        index = labelOn.indexOf(" %s");
        if (index != -1) {
            labelOn = labelOn.substring(0, index);
        }
        List<AccessibilityNodeInfo> list = nodeInfo
                .findAccessibilityNodeInfosByText(label);
        if (list == null || list.size() == 0) {
            return NOT_CLICKABLE;
        }

        for (AccessibilityNodeInfo child : list) {

            if (child != null) {
                if (child.getContentDescription() != null) {
                    if (child.getContentDescription().toString().indexOf(labelOn) != -1) {
                        return CLICKABLE_ENABLE;
                    }
                } else if (child.getText() != null) {
                    if (child.getText().toString().indexOf(labelOn) != -1) {
                        return CLICKABLE_ENABLE;
                    }
                }
            }

        }
        return CLICKABLE_DISABLE;
    }

    public static int setEnable(Context context, AccessibilityNodeInfo nodeInfo) {

        if (nodeInfo == null) {
            return NOT_CLICKABLE;
        }


        int isEnable = checkEnable(context, nodeInfo);
        boolean isClickSuccess = false;
        Log.e(TAG, "setEnable: trang thai ban dau " + isEnable);

        String label = "";
        if (checkDeviceSamsung()) {
            label = getSamsungLabel(context);
        } else {
            label = getStringByName(context, ALL_DEVICE_LABEL);
        }

        int index = label.indexOf("\n");
        if (index != -1) {
            label = label.substring(0, index);
        }


        List<AccessibilityNodeInfo> list = nodeInfo
                .findAccessibilityNodeInfosByText(label);

        if (list == null || list.size() == 0) {
            Log.e(TAG, "setEnable: khong click dc vi khong tim thay blue light");
            return NOT_CLICKABLE;
        }

        for (AccessibilityNodeInfo child : list) {

            if (child != null) {
                child.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                isClickSuccess = true;
                Log.e(TAG, "setEnable: click thanh cong");

            }

        }
//

        if (isEnable != checkEnable(context, nodeInfo)) {
            int statusFinal = isEnable == CLICKABLE_ENABLE ? CLICKABLE_DISABLE : CLICKABLE_ENABLE;
            Log.e(TAG, "setEnable: trang thai luc sau la " + statusFinal);
            // trang thai hien tai khac voi trang thai ban dau
            return statusFinal;
        } else {
            // trang thai hien tai giong voi trang thai ban dau
            int statusFinal = isClickSuccess ? (isEnable == CLICKABLE_ENABLE ? CLICKABLE_DISABLE : CLICKABLE_ENABLE) : isEnable;
            Log.e(TAG, "setEnable: trang thai tra ve la " + statusFinal);
            return statusFinal;
        }

    }


    public static String getSamsungLabel(Context context) {
        File f = new File("/system/system_ext/priv-app/SystemUI/SystemUI.apk");
        if (f.exists()) {
            // android one ui 3.1 // samsung android 11
            return getStringByName(context, SAMSUNG_HIGH_DEVICE_LABEL);
        } else if ((new File("/system/priv-app/SystemUI/SystemUI.apk")).exists()) {
            return getStringByName(context, SAMSUNG_LOW_DEVICE_LABEL);
        }
        return "";
    }
}
