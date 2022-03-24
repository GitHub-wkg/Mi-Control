package com.ezstudio.controlcenter.service

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.util.Log

object StatusBarAutomatic {

    fun expandStatusBar(context: Context) {
        try {
            @SuppressLint("WrongConstant") val service: Any = context.getSystemService("statusbar")
            val statusbarManager = Class.forName("android.app.StatusBarManager")
            val expand = statusbarManager.getMethod("expandSettingsPanel")
            expand.invoke(service)
            object : CountDownTimer(400, 400) {
                override fun onTick(l: Long) {}
                override fun onFinish() {
                    collapseStatusBar(context)
                }
            }.start()
        } catch (ex: Exception) {
        }
    }

    private fun collapseStatusBar(context: Context) {
        try {
            @SuppressLint("WrongConstant") val service: Any = context.getSystemService("statusbar")
            val statusbarManager = Class.forName("android.app.StatusBarManager")
            val expand = statusbarManager.getMethod("collapsePanels")
            expand.invoke(service)
        } catch (ex: java.lang.Exception) {
            Log.e("XXX", "onNewIntent: ", ex)
        }
    }
}