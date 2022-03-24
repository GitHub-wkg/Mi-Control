package com.ezstudio.controlcenter.batterysaver

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.batterysaver.BatterySaverFilterUtil.detectBatterySaver
import com.ezstudio.controlcenter.settingutils.SettingUtils

class StatusBarBatterySaverUtil(private var mContext: Context) {
    var isExpand = false
        private set
    private val settingUtils: SettingUtils = SettingUtils()
    val setting: SettingUtils
        get() = settingUtils
    fun expandStatusBar(listenerClickable: (Boolean) -> Unit) {
        try {
            // expand Status Bar
            expandStatusBar()
            Handler().postDelayed({
                collapseStatusBar()
                //  call back when click
                listenerClickable(true)
            }, 400)

        } catch (ex: Exception) {
            listenerClickable(true)
        }
    }
    private  fun expandStatusBar(){
        @SuppressLint("WrongConstant") val service = mContext.getSystemService("statusbar")
        val statusBarManager = Class.forName("android.app.StatusBarManager")
        val expand = statusBarManager.getMethod("expandSettingsPanel")
        expand.invoke(service)
        isExpand = true
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
        private var INSTANCE: StatusBarBatterySaverUtil? = null
        fun getInstance(context: Context): StatusBarBatterySaverUtil? {
            if (INSTANCE == null) {
                return StatusBarBatterySaverUtil(context).also { INSTANCE = it }
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
                val isEnableBatterySaverSystem = detectBatterySaver(context)
                val isEnableBatterySaver: Boolean =
                    getInstance(context)!!.setting.isEnableBatterySaver // cua app minh muon set

                if (isEnableBatterySaver != isEnableBatterySaverSystem) {
                    // enable filter
                    BatterySaverFilterUtil.setEnable(
                        context,
                        nodeInfo,
                        isEnableBatterySaver,
                        listenerEndWindow
                    )
                    getInstance(context)!!.setting.isEnableBatterySaver = (isEnableBatterySaver)
                    // lan nao cung vao day
                }
            }
        }
    }

}