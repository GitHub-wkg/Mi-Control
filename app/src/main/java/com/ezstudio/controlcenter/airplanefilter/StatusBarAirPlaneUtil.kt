package com.ezstudio.controlcenter.airplanefilter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.settingutils.SettingUtils

class StatusBarAirPlaneUtil(private var mContext: Context) {
    var isExpand = false
        private set
    private val settingUtils: SettingUtils = SettingUtils()
    val setting: SettingUtils
        get() = settingUtils

    fun expandStatusBar(listenerClickable: (Boolean) -> Unit) {
        try {
            expandStatusBar()
            Handler().postDelayed({
                collapseStatusBar()
                listenerClickable(true)
            }, 400)
        } catch (ex: Exception) {
            listenerClickable(true)
        }
    }
    private  fun expandStatusBar(){
        @SuppressLint("WrongConstant") val service = mContext.getSystemService("statusbar")
        val statusbarManager = Class.forName("android.app.StatusBarManager")
        val expand = statusbarManager.getMethod("expandSettingsPanel")
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
        private const val TAG = "StatusBarUtils"
        private var INSTANCE: StatusBarAirPlaneUtil? = null
        fun getInstance(context: Context): StatusBarAirPlaneUtil? {
            if (INSTANCE == null) {
                return StatusBarAirPlaneUtil(context).also { INSTANCE = it }
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
                val isEnableAirPlaneSystem = Settings.Global.getInt(
                    context.contentResolver,
                    Settings.Global.AIRPLANE_MODE_ON, 0
                ) != 0
                val isEnableAirPlane: Boolean =
                    getInstance(context)!!.setting.isEnableFightMode // cua app minh muon set

                if (isEnableAirPlane != isEnableAirPlaneSystem) {
                    AirPlaneFilterUtil.setEnable(
                        context,
                        nodeInfo,
                        isEnableAirPlane,
                        listenerEndWindow
                    )
                    getInstance(context)!!.setting.isEnableFightMode = (isEnableAirPlane)
                }
                // lan nao cung vao day
            }
        }

    }

}