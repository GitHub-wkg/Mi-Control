package com.ezstudio.controlcenter.service.accessiblilityUtils

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.airplanefilter.AirPlaneFilterUtil
import com.ezstudio.controlcenter.batterysaver.BatterySaverFilterUtil
import com.ezteam.baseproject.utils.PreferencesUtils
import java.lang.Exception
import java.lang.reflect.Method

class MobileDataUtil : BaseAccessiblilityUtil() {

    private val LABEL_MOBLIE_DATA = "quick_settings_mobile_data_detail_title"

    fun setEnable(context: Context, nodeInfo: AccessibilityNodeInfo?): Int {
        if (nodeInfo == null) {
            return NOT_CLICKABLE
        }

        val firstStage = checkEnable(context)

        var isClickSuccess = false
        var getList =
            nodeInfo.findAccessibilityNodeInfosByText(
                BatterySaverFilterUtil.getStringByName(
                    context,
                    LABEL_MOBLIE_DATA
                )
            )

        if (getList.isNullOrEmpty()) {
            val labels = PreferencesUtils.getString(
                "${context.resources.getString(R.string.mobile_data)} - Name",
                ""
            )
            getList =
                nodeInfo.findAccessibilityNodeInfosByText(labels)
        }
        if (getList.isNullOrEmpty()) {
            try {
                val location = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.mobile_data)} - Location",
                    "0"
                ).toInt()
                if (location == 0) {
                    return NOT_CLICKABLE
                } else {
                    logNodeHeirarchy(nodeInfo, location)
                    isClickSuccess = true
                }
            } catch (ex: NumberFormatException) {
                return NOT_CLICKABLE
            }
        } else {
            for (child in getList) {
                if (child != null) {
                    child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    child.recycle()
                    isClickSuccess = true
                }
            }
        }

        return if (isClickSuccess) {
            if (firstStage) CLICKABLE_DISABLE else CLICKABLE_ENABLE
        } else {
            NOT_CLICKABLE
        }
    }

    fun checkEnable(context: Context): Boolean {
        var mobileDataEnabled = false
        val cm: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            val cmClass = Class.forName(cm.javaClass.name)
            val method: Method = cmClass.getDeclaredMethod("getMobileDataEnabled")
            method.isAccessible = true
            mobileDataEnabled = method.invoke(cm) as Boolean
        } catch (e: Exception) {

        }
        return mobileDataEnabled
    }
}