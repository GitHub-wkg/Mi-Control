package com.ezstudio.controlcenter.service.accessiblilityUtils

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.sceentransmissionfilter.ScreenTransmissionFilterUtil
import com.ezteam.baseproject.utils.PreferencesUtils

class ScreenTransmissionUtil : BaseAccessiblilityUtil() {

    private val ALL_DEVICE_LABEL = "quick_settings_cast_title"
    private val ALL_DEVICE_LABEL_HUAWEI = "wireless_projection_name"

    fun setEnable(context: Context, nodeInfo: AccessibilityNodeInfo?): Int {
        if (nodeInfo == null) {
            return NOT_CLICKABLE
        }

        val firstStage = checkEnable(context, nodeInfo)

        var isClickSuccess = false
        var label = ""
        label = ScreenTransmissionFilterUtil.getStringByName(
            context,
            ALL_DEVICE_LABEL
        )

        val index = label.indexOf("\n")
        if (index != -1) {
            label = label.substring(0, index)
        }
        //
        var getList = nodeInfo
            .findAccessibilityNodeInfosByText(label)
        if (getList.isNullOrEmpty()) {
            label = PreferencesUtils.getString(
                "${context.resources.getString(R.string.screen_transmission)} - Name",
                ""
            )
            getList =
                nodeInfo.findAccessibilityNodeInfosByText(label)
        }
        if (getList.isNullOrEmpty() && "HUAWEI".equals(Build.MANUFACTURER, true)) {
            getList = nodeInfo
                .findAccessibilityNodeInfosByText(
                    ScreenTransmissionFilterUtil.getStringByName(
                        context,
                        ALL_DEVICE_LABEL_HUAWEI
                    )
                )
        }
        if (getList.isNullOrEmpty()) {
            try {
                val location = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.screen_transmission)} - Location",
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

    fun checkEnable(context: Context, nodeInfo: AccessibilityNodeInfo?): Boolean {
        if (nodeInfo == null) {
            return false
        }
        var label = ""
        label = ScreenTransmissionFilterUtil.getStringByName(
            context,
            ALL_DEVICE_LABEL
        )
        var index = label.indexOf("\n")
        if (index != -1) {
            label = label.substring(0, index)
        }
        var list = nodeInfo
            .findAccessibilityNodeInfosByText(label)
        if (list.size == 0 && "HUAWEI".equals(Build.MANUFACTURER, true)) {
            list = nodeInfo
                .findAccessibilityNodeInfosByText(
                    ScreenTransmissionFilterUtil.getStringByName(
                        context,
                        ALL_DEVICE_LABEL_HUAWEI
                    )
                )
        }
        if (list.size == 0) {
            label = PreferencesUtils.getString(
                "${context.resources.getString(R.string.screen_transmission)} - Name",
                ""
            )
            list =
                nodeInfo.findAccessibilityNodeInfosByText(label)
        }
        if (list.size == 0) {
            try {
                val location = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.screen_transmission)} - Location",
                    "0"
                ).toInt()
                if (location == 0) {
                    return false
                } else {
                    val accessibilityNodeInfo =
                        getChild(nodeInfo, location)
                    return if (accessibilityNodeInfo != null) {
                        if (accessibilityNodeInfo.text != null) {
                            !accessibilityNodeInfo.text.toString()
                                .equals(context.resources.getString(R.string.tat), true)
                        } else {
                            accessibilityNodeInfo.isEnabled
                        }
                    } else {
                        false
                    }
                }
            } catch (ex: NumberFormatException) {
                return false
            }
        } else {
            for (child in list) {
                if (child != null) {
                    if (child.contentDescription != null) {
                        return child.contentDescription.toString().indexOf(label) != -1
                    } else if (child.text != null) {
                        return child.text.toString().indexOf(label) != -1
                    }
                }
            }
        }

        return false
    }

    private fun getChild(nodeInfo: AccessibilityNodeInfo?, location: Int): AccessibilityNodeInfo? {
        try {
            if (nodeInfo == null)
                return null
            when {
                "HUAWEI".equals(Build.MANUFACTURER, true) -> {
                    return nodeInfo.getChild(0).getChild(12).getChild(0).getChild(location)
                }
                "SAMSUNG".equals(Build.MANUFACTURER, true) -> {
                    if (location > 12) {
                        //
                        return nodeInfo.getChild(0).getChild(1).getChild(location - 13).getChild(0)
                    } else {
                        return nodeInfo.getChild(0).getChild(0).getChild(location - 1).getChild(0)
                    }
                }
            }
        } catch (ex: Exception) {
        }
        return null
    }
}