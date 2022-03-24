package com.ezstudio.controlcenter.bluefilter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.sceentransmissionfilter.StatusScreenTransmissionUtil
import com.ezstudio.controlcenter.settingutils.SettingUtils

class StatusBarBlueUtil(private var mContext: Context) {
    var isExpand = false
        private set
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
            StatusScreenTransmissionUtil.getInstance(mContext)?.isExpand = true
            //
            object : CountDownTimer(400, 400) {
                override fun onTick(l: Long) {}
                override fun onFinish() {
                    collapseStatusBar()
                    listenerClickable(true)
                }
            }.start()

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
            StatusScreenTransmissionUtil.getInstance(mContext)?.isExpand = false
        } catch (ex: Exception) {
        }
    }

    companion object {
        private const val TAG = "StatusBarUtils"
        private var INSTANCE: StatusBarBlueUtil? = null
        fun getInstance(context: Context): StatusBarBlueUtil? {
            if (INSTANCE == null) {
                return StatusBarBlueUtil(context).also { INSTANCE = it }
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
                    val isEnableBlueLight = BlueLightFilterUtil.checkEnable(context, nodeInfo)
                    getInstance(context)!!.setting.isEnableBlueFilter = isEnableBlueLight
                    val intent = Intent(context.resources.getString(R.string.action_night_light))
                    context.sendBroadcast(intent)
                    getInstance(context)!!.isFirstExpand = false
                    // bo sung nhung setup khac
                } else {
                    // set data nhung lan sau
                    val isEnableBlueLightSystem =
                        BlueLightFilterUtil.checkEnable(context, nodeInfo) // cua he thong
                        Log.e("XXX",isEnableBlueLightSystem.toString())
                    val isEnableBlueLight: Boolean =
                        getInstance(context)!!.setting.isEnableBlueFilter // cua app minh muon set
//                    if (isEnableBlueLight != isEnableBlueLightSystem) {
                    BlueLightFilterUtil.setEnable(
                        context,
                        nodeInfo,
                        !isEnableBlueLightSystem,
                        listenerEndWindow
                    )
//                    }
                    getInstance(context)!!.setting.isEnableBlueFilter = !isEnableBlueLightSystem
                    // lan nao cung vao day
                }
            }
        }
    }

    init {
        isFirstExpand = true
    }
}