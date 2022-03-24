package com.ezstudio.controlcenter.service.accessiblilityUtils

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.darktheme.DarkThemeFilterUtil
import com.ezteam.baseproject.utils.PreferencesUtils

class DarkThemeUtil : BaseAccessiblilityUtil() {

    private val LABEL_DARK_THEME = "quick_settings_ui_mode_night_label"
    private val LABEL_DARK_THEME_HUAWEI = "dark_ui_mode"

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
                    LABEL_DARK_THEME
                )
            )

        if (getList.isNullOrEmpty()) {
            getList =
                nodeInfo.findAccessibilityNodeInfosByText(
                    getStringByName(
                        context,
                        LABEL_DARK_THEME_HUAWEI
                    )
                )
        }
        if (getList.isNullOrEmpty()) {
            val labels = PreferencesUtils.getString(
                "${context.resources.getString(R.string.dark_theme)} - Name",
                ""
            )
            getList =
                nodeInfo.findAccessibilityNodeInfosByText(labels)
        }
        if (getList.isNullOrEmpty()) {
            try {
                val location = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.dark_theme)} - Location",
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
        val uiManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        return when (uiManager.nightMode) {
            UiModeManager.MODE_NIGHT_NO -> {
                CLICKABLE_DISABLE
            }
            UiModeManager.MODE_NIGHT_YES, UiModeManager.MODE_NIGHT_AUTO -> {
                CLICKABLE_ENABLE
            }
            else -> {
                CLICKABLE_DISABLE
            }
        }
    }

}