package com.ezstudio.controlcenter.darktheme

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.os.Handler
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.darktheme.DarkThemeFilterUtil.detectDarkTheme
import com.ezstudio.controlcenter.settingutils.SettingUtils

class StatusBarDarkTheme(private var mContext: Context) {
    var isExpand = false
        private set
    private val settingUtils: SettingUtils = SettingUtils()
    val setting: SettingUtils
        get() = settingUtils


    fun expandStatusBar(listenerClickable: (Boolean) -> Unit) {
        try {
            @SuppressLint("WrongConstant") val service = mContext.getSystemService("statusbar")
            val statusbarManager = Class.forName("android.app.StatusBarManager")
            val expand = statusbarManager.getMethod("expandSettingsPanel")
            expand.invoke(service)
            isExpand = true
            Handler().postDelayed({
                collapseStatusBar()
                listenerClickable(true)
            }, 400)

        } catch (ex: Exception) {
            listenerClickable(true)
        }
    }

    private fun collapseStatusBar() {
        try {
            @SuppressLint("WrongConstant") val service = mContext.getSystemService("statusbar")
            val statusbarManager = Class.forName("android.app.StatusBarManager")
            val expand = statusbarManager.getMethod("collapsePanels")
            expand.invoke(service)
            isExpand = false
        } catch (ex: Exception) {
        }
    }

    companion object {
        private const val TAG = "StatusBarUtils"
        private var INSTANCE: StatusBarDarkTheme? = null
        fun getInstance(context: Context): StatusBarDarkTheme? {
            if (INSTANCE == null) {
                return StatusBarDarkTheme(context).also { INSTANCE = it }
            }
            INSTANCE!!.mContext = context
            return INSTANCE
        }

        fun setupStatusBar(
            context: Context,
            nodeInfo: AccessibilityNodeInfo,
            listenerEndWindow: () -> Unit
        ) {
            // kiem tra xem la isEnable la dang bat hay tat
            //
            if (getInstance(context)!!.isExpand) {
                // bo sung nhung setup khac
                // set data nhung lan sau
                val isEnableDarkThemeSystem = detectDarkTheme(context)
                val isEnableDarkTheme: Boolean =
                    getInstance(context)!!.setting.isEnableDarkTheme // cua app minh muon set

                if (isEnableDarkTheme != isEnableDarkThemeSystem) {
                    DarkThemeFilterUtil.setEnable(
                        context,
                        nodeInfo,
                        isEnableDarkTheme,
                        listenerEndWindow
                    )
                    getInstance(context)!!.setting.isEnableDarkTheme = (isEnableDarkTheme)
                }
                // lan nao cung vao day
            }
        }
    }

    fun detectDarkTheme(context: Context): Boolean {
        val uiManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        when (uiManager.nightMode) {
            UiModeManager.MODE_NIGHT_NO -> {
                return false
            }
            UiModeManager.MODE_NIGHT_YES, UiModeManager.MODE_NIGHT_AUTO -> {
                return true
            }
        }
        return false
    }

}