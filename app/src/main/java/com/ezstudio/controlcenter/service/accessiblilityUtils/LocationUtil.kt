package com.ezstudio.controlcenter.service.accessiblilityUtils

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.locationfilter.LocationFilterUtil
import com.ezteam.baseproject.utils.PreferencesUtils

class LocationUtil : BaseAccessiblilityUtil() {

    private val LABEL_LOCATION = "quick_settings_location_label"
    private val LABEL_LOCATION_HUAWEI = "location_access"

    fun setEnable(context: Context, nodeInfo: AccessibilityNodeInfo?): Int {
        if (nodeInfo == null) {
            return NOT_CLICKABLE
        }

        val firstStage = checkEnable(context)

        var isClickSuccess = false
        var label = LocationFilterUtil.getStringByName(context, LABEL_LOCATION)
        //
        var getList = nodeInfo
            .findAccessibilityNodeInfosByText(label)
        if (getList.isNullOrEmpty()) {
            getList = nodeInfo
                .findAccessibilityNodeInfosByText(
                    LocationFilterUtil.getStringByName(
                        context,
                        LABEL_LOCATION_HUAWEI
                    )
                )
        }
        if (getList.isNullOrEmpty()) {
            getList = nodeInfo
                .findAccessibilityNodeInfosByText("Location")
        }
        if (getList.isNullOrEmpty()) {
            getList = nodeInfo
                .findAccessibilityNodeInfosByText("GPS")
        }
        if (getList.isNullOrEmpty()) {
            getList = nodeInfo
                .findAccessibilityNodeInfosByText("Vị trí")
        }
        if (getList.isNullOrEmpty()) {
            val labels = PreferencesUtils.getString(
                "${context.resources.getString(R.string.location)} - Name",
                ""
            )
            getList =
                nodeInfo.findAccessibilityNodeInfosByText(labels)
        }
        if (getList.isNullOrEmpty()) {
            try {
                val location = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.location)} - Location",
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
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            val mode = Settings.Secure.getInt(
                context.contentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }

}