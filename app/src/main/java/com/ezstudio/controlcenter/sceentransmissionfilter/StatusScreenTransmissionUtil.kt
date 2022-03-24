package com.ezstudio.controlcenter.sceentransmissionfilter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.settingutils.SettingUtils

class StatusScreenTransmissionUtil(private var mContext: Context) {
    var isExpand = false
    var isFirstExpand: Boolean
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
        private var INSTANCE: StatusScreenTransmissionUtil? = null
        fun getInstance(context: Context): StatusScreenTransmissionUtil? {
            if (INSTANCE == null) {
                return StatusScreenTransmissionUtil(context).also { INSTANCE = it }
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
                if (getInstance(context)!!.isFirstExpand) {
                    // load data lan dau
                    val isEnableScreenTransmission =
                        ScreenTransmissionFilterUtil.checkEnable(context, nodeInfo)

                    getInstance(context)!!.setting.isEnableScreenTransmission =
                        isEnableScreenTransmission
                    //
                    //
                    val intent =
                        Intent(context.resources.getString(R.string.action_screen_transmission))
                    context.sendBroadcast(intent)
                    //
                    //
                    getInstance(context)!!.isFirstExpand = false
                    // bo sung nhung setup khac
                } else {
                    // set data nhung lan sau
                    val isEnableScreenTransmissionSystem =
                        ScreenTransmissionFilterUtil.checkEnable(context, nodeInfo) // cua he thong
                    val isEnableScreenTransmission: Boolean =
                        getInstance(context)!!.setting.isEnableScreenTransmission // cua app minh muon set

                    if (isEnableScreenTransmission != isEnableScreenTransmissionSystem) {

                        ScreenTransmissionFilterUtil.setEnable(
                            context,
                            nodeInfo,
                            isEnableScreenTransmission,
                            listenerEndWindow
                        )
                        getInstance(context)!!.setting.isEnableScreenTransmission =
                            (isEnableScreenTransmission)
                    } else {
                        getInstance(context)!!.setting.isEnableScreenTransmission =
                            (isEnableScreenTransmissionSystem)
                        val intent =
                            Intent(context.resources.getString(R.string.action_screen_transmission))
                        context.sendBroadcast(intent)
                    }
                    // lan nao cung vao day
                }
            }
        }
    }

    init {
        isFirstExpand = true
    }
}