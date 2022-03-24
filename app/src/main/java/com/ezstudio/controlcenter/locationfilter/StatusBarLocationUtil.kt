package com.ezstudio.controlcenter.locationfilter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.locationfilter.LocationFilterUtil.detectLocation
import com.ezstudio.controlcenter.settingutils.SettingUtils

class StatusBarLocationUtil(private var mContext: Context) {
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
        private var INSTANCE: StatusBarLocationUtil? = null
        fun getInstance(context: Context): StatusBarLocationUtil? {
            if (INSTANCE == null) {
                return StatusBarLocationUtil(context).also { INSTANCE = it }
            }
            INSTANCE!!.mContext = context
            return INSTANCE
        }

        fun setupStatusBar(context: Context, nodeInfo: AccessibilityNodeInfo ,  listenerEndWindow : ()-> Unit) {
            // kiem tra xem la isEnable la dang bat hay tat
            //
            if (getInstance(context)!!.isExpand) {
                // bo sung nhung setup khac
                // set data nhung lan sau
                val isEnableLocationSystem = detectLocation(context)
                val isEnableLocation: Boolean =
                    getInstance(context)!!.setting.isEnableLocation // cua app minh muon set
                if (isEnableLocation != isEnableLocationSystem) {
                    LocationFilterUtil.setEnable(context, nodeInfo, isEnableLocation,listenerEndWindow)
                    getInstance(context)!!.setting.isEnableLocation = (isEnableLocation)
                }
                // lan nao cung vao day
            }
        }
    }

}