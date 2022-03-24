package com.ezstudio.controlcenter.service.accessiblilityUtils

import android.content.Context
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.airplanefilter.AirPlaneFilterUtil
import com.ezstudio.controlcenter.batterysaver.BatterySaverFilterUtil
import com.ezteam.baseproject.utils.PreferencesUtils

class AirPlaneUtil : BaseAccessiblilityUtil() {

    private val LABEL_AIR_PLANE = "quick_settings_flight_mode_detail_title"
    private val LABEL_AIR_PLANE_SS_LOW = "quick_settings_airplane_mode_label"
    private val LABEL_AIR_PLANE_HUAWEI = "airplane_mode"

    fun setEnable(context: Context, nodeInfo: AccessibilityNodeInfo?): Int {
        if (nodeInfo == null) {
            return NOT_CLICKABLE
        }

        val firstStage = checkEnable(context)

        var isClickSuccess = false
        var labels = AirPlaneFilterUtil.getStringByName(context, LABEL_AIR_PLANE)
        var index = labels.indexOf("\n")
        if (index != -1) {
            labels = labels.substring(0, index)
        }
        var getList =
            nodeInfo.findAccessibilityNodeInfosByText(labels)
        //
        if (getList.isNullOrEmpty()) {
            labels = AirPlaneFilterUtil.getStringByName(
                context,
                LABEL_AIR_PLANE_SS_LOW
            )
            index = labels.indexOf("\n")
            if (index != -1) {
                labels = labels.substring(index + 1, labels.length)
            }
            getList =
                nodeInfo.findAccessibilityNodeInfosByText(labels)
        }

        if (getList.isNullOrEmpty()) {
            labels = AirPlaneFilterUtil.getStringByName(
                context,
                LABEL_AIR_PLANE_HUAWEI
            )
            getList =
                nodeInfo.findAccessibilityNodeInfosByText(labels)
        }
        if (getList.isNullOrEmpty()) {
            val labels = PreferencesUtils.getString(
                "${context.resources.getString(R.string.airplane_mode)} - Name",
                ""
            )
            getList =
                nodeInfo.findAccessibilityNodeInfosByText(labels)
        }
        if (getList.isNullOrEmpty()) {
            try {
                val location = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.airplane_mode)} - Location",
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
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
    }
}