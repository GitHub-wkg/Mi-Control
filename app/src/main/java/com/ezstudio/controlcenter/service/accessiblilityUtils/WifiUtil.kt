package com.ezstudio.controlcenter.service.accessiblilityUtils

import android.content.Context
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.airplanefilter.AirPlaneFilterUtil
import com.ezstudio.controlcenter.batterysaver.BatterySaverFilterUtil
import com.ezteam.baseproject.utils.PreferencesUtils

class WifiUtil : BaseAccessiblilityUtil() {

    private val LABEL_WIFI= "quick_settings_wifi_label"

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
                    LABEL_WIFI
                )
            )

        if (getList.isNullOrEmpty()) {
            val labels = PreferencesUtils.getString(
                "${context.resources.getString(R.string.wi_fi)} - Name",
                ""
            )
            getList =
                nodeInfo.findAccessibilityNodeInfosByText(labels)
        }
        if (getList.isNullOrEmpty()) {
            try {
                val location = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.wi_fi)} - Location",
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
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }
}