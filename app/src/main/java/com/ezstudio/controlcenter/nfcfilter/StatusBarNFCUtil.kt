package com.ezstudio.controlcenter.nfcfilter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.nfcfilter.NFCFilterUtil.detectNFC
import com.ezstudio.controlcenter.settingutils.SettingUtils

class StatusBarNFCUtil(private var mContext: Context) {
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
        private var INSTANCE: StatusBarNFCUtil? = null
        fun getInstance(context: Context): StatusBarNFCUtil? {
            if (INSTANCE == null) {
                return StatusBarNFCUtil(context).also { INSTANCE = it }
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
                val isEnableNFCSystem = detectNFC(context)
                val isEnableNFC: Boolean =
                    getInstance(context)!!.setting.isEnableNFC // cua app minh muon set

                if (isEnableNFC != isEnableNFCSystem) {
                    NFCFilterUtil.setEnable(context, nodeInfo, isEnableNFC,listenerEndWindow)
                    getInstance(context)!!.setting.isEnableNFC = (isEnableNFC)
                }
                // lan nao cung vao day
            }
        }
    }

}