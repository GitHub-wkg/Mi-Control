package com.ezstudio.controlcenter.service.accessiblilityUtils

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.datasaver.DataSaverFilterUtil
import com.ezteam.baseproject.utils.PreferencesUtils

class DataSaveUtil :BaseAccessiblilityUtil() {

    private val LABEL_DATA_SAVER = "data_saver"
    private val LABEL_DATA_SAVER_SS_LOW = ""
    private val LABEL_DATA_SAVER_HUAWEI = ""

    fun setEnable(context: Context, nodeInfo: AccessibilityNodeInfo?): Int {
        if (nodeInfo == null) {
            return NOT_CLICKABLE
        }

        val firstStage = checkEnable(context)

        var isClickSuccess = false
        var labels =
            DataSaverFilterUtil.getStringByName(context, LABEL_DATA_SAVER)
        var index = labels.indexOf("\n")
        if (index != -1) {
            labels = labels.substring(0, index)
        }
        var getList =
            nodeInfo.findAccessibilityNodeInfosByText(labels)
        if (getList.isNullOrEmpty()) {
            labels = DataSaverFilterUtil.getStringByName(
                context,
                LABEL_DATA_SAVER_SS_LOW
            )
            index = labels.indexOf("\n")
            if (index != -1) {
                labels = labels.substring(index + 1, labels.length)
            }
            getList =
                nodeInfo.findAccessibilityNodeInfosByText(labels)
        }
        if (getList.isNullOrEmpty()) {
            labels = DataSaverFilterUtil.getStringByName(
                context,
                LABEL_DATA_SAVER_HUAWEI
            )
            getList =
                nodeInfo.findAccessibilityNodeInfosByText(labels)
        }
        if (getList.isNullOrEmpty()) {
            labels = PreferencesUtils.getString(
                "${context.resources.getString(R.string.data_saver)} - Name",
                ""
            )
            getList =
                nodeInfo.findAccessibilityNodeInfosByText(labels)
        }
        if (getList.isNullOrEmpty()) {
            try {
                val location = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.data_saver)} - Location",
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
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when (connectivityManager.restrictBackgroundStatus) {
                ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED -> {
                    return CLICKABLE_ENABLE
                }
                ConnectivityManager.RESTRICT_BACKGROUND_STATUS_WHITELISTED, ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED -> {
                    return CLICKABLE_DISABLE
                }
            }
        }
        return CLICKABLE_DISABLE
    }

}