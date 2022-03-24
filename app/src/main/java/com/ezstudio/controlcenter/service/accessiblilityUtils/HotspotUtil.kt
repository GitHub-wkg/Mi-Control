package com.ezstudio.controlcenter.service.accessiblilityUtils

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.R
import com.ezteam.baseproject.utils.PreferencesUtils

class HotspotUtil :BaseAccessiblilityUtil() {

    private val LABEL_HOTSPOT = "quick_settings_hotspot_label"

    fun setEnable(context: Context, nodeInfo: AccessibilityNodeInfo?): Int {
        if (nodeInfo == null) {
            return NOT_CLICKABLE
        }

        val firstStage = checkEnable(context)

        var isClickSuccess = false
        var getList =
            nodeInfo.findAccessibilityNodeInfosByText(
                getStringByName(
                    context,
                    LABEL_HOTSPOT
                )
            )
        if (getList.isNullOrEmpty()) {
            getList = nodeInfo
                .findAccessibilityNodeInfosByText("Hotspot")
        }
        if (getList.isNullOrEmpty()) {
            val labels = PreferencesUtils.getString(
                "${context.resources.getString(R.string.hotspot)} - Name",
                ""
            )
            getList =
                nodeInfo.findAccessibilityNodeInfosByText(labels)
        }
        if (getList.isNullOrEmpty()) {
            try {
                val location = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.hotspot)} - Location",
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
            if (firstStage == 1) CLICKABLE_DISABLE else CLICKABLE_ENABLE
        } else {
            NOT_CLICKABLE
        }
    }

    fun checkEnable(context: Context): Int {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val method = wifiManager.javaClass.getMethod("getWifiApState")
        val tmp = method.invoke(wifiManager) as Int
        return if (tmp == 13) {
            CLICKABLE_ENABLE
        } else {
            CLICKABLE_DISABLE
        }
    }

}