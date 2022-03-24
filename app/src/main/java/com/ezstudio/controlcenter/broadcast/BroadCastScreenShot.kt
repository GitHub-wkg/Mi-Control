package com.ezstudio.controlcenter.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.ezstudio.controlcenter.databinding.WindownManagerBinding
import com.ezstudio.controlcenter.screenshot.ScreenShotHelper
import com.ezstudio.controlcenter.windown_manager.MyWindowManager

class BroadCastScreenShot : BroadcastReceiver() {
    lateinit var myWindowManager: MyWindowManager

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            intent?.let {
                MyWindowManager.intentData = it
            }
        }
    }
}