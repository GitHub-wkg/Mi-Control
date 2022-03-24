package com.ezstudio.controlcenter.autorotatefilter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.settingutils.SettingUtils

class StatusBarAutoRotateUtil(private var mContext: Context) {
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
        private var INSTANCE: StatusBarAutoRotateUtil? = null
        fun getInstance(context: Context): StatusBarAutoRotateUtil? {
            if (INSTANCE == null) {
                return StatusBarAutoRotateUtil(context).also { INSTANCE = it }
            }
            INSTANCE!!.mContext = context
            return INSTANCE
        }

        fun setupStatusBar(
            context: Context,
            nodeInfo: AccessibilityNodeInfo,
            listenerEndWindow: () -> Unit
        ) {
            if (getInstance(context)!!.isExpand) {
                // set data nhung lan sau
                val isEnableAutoRotateSystem = Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.ACCELEROMETER_ROTATION,
                    0
                ) == 1
                val isEnableAutoRotate: Boolean =
                    getInstance(context)!!.setting.isEnableAutoRotate // cua app minh muon set

                if (isEnableAutoRotate != isEnableAutoRotateSystem) {
                    AutoRotateFilterUtil.setEnable(
                        context,
                        nodeInfo,
                        isEnableAutoRotate,
                        listenerEndWindow
                    )
                    getInstance(context)!!.setting.isEnableAutoRotate = (isEnableAutoRotate)
                    // lan nao cung vao day
                }
            }
        }
    }

}