package com.ezstudio.controlcenter.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.ezstudio.controlcenter.windown_manager.MyWindowManager

class MyAccessibilityService : AccessibilityService() {
    private lateinit var myWindowManager: MyWindowManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        myWindowManager = MyWindowManager(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }
}