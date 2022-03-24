package com.ezstudio.controlcenter.windown_manager

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import com.ezstudio.controlcenter.MyGroupView
import com.ezstudio.controlcenter.bluefilter.StatusBarBlueUtil
import com.ezstudio.controlcenter.databinding.ActivitySplashBinding
import com.ezstudio.controlcenter.service.StatusBarAutomatic

class WindowSplash(var context: Context, windowManager: WindowManager) {
    var binding: ActivitySplashBinding

    init {
        val myGroup = MyGroupView(context)
        binding = ActivitySplashBinding.inflate(LayoutInflater.from(context), myGroup, false)
        windowManager.addView(binding.root, setupLayout())
        Handler().postDelayed({
            StatusBarAutomatic.expandStatusBar(context)
            Handler().postDelayed({
                windowManager.removeViewImmediate(binding.root)
            }, 1200)
        }, 300)

    }

    private fun setupLayout(): WindowManager.LayoutParams {
        val mLayoutParams: WindowManager.LayoutParams
        val flag = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

        val type =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            else WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        val height = WindowManager.LayoutParams.MATCH_PARENT

        mLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            height,
            type,
            flag,
            PixelFormat.TRANSLUCENT
        )
        mLayoutParams.gravity = Gravity.TOP
        mLayoutParams.alpha = 1F
//
        return mLayoutParams
    }
}