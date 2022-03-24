package com.ezstudio.controlcenter.service.accessiblilityUtils

import android.content.Context
import android.os.PowerManager
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.batterysaver.BatterySaverFilterUtil
import com.ezteam.baseproject.utils.PreferencesUtils

class BatterySaveUtil : BaseAccessiblilityUtil() {

    private val BATTERY_SAVER_LABEL = "battery_detail_switch_title"
    private val BATTERY_SAVER_LABEL_HUAWEI = "super_power_widget_name"

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
                    BATTERY_SAVER_LABEL
                )
            )
        if (getList.isNullOrEmpty()) {
            getList =
                nodeInfo.findAccessibilityNodeInfosByText(
                    BatterySaverFilterUtil.getStringByName(
                        context,
                        BATTERY_SAVER_LABEL_HUAWEI
                    )
                )
        }
        if (getList.isNullOrEmpty()) {
            val labels = PreferencesUtils.getString(
                "${context.resources.getString(R.string.battery_saver)} - Name",
                ""
            )
            getList =
                nodeInfo.findAccessibilityNodeInfosByText(labels)
        }
        if (getList.isNullOrEmpty()) {
            try {
                val location = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.battery_saver)} - Location",
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
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isPowerSaveMode
    }
}