package com.ezstudio.controlcenter.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.wifi.WifiManager
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutIconSecondsLineBinding
import com.ezteam.baseproject.utils.PreferencesUtils


class BroadCastHotspot : BroadcastReceiver() {
    lateinit var binding: LayoutIconSecondsLineBinding
    private val BACKGROUND_COLOR = "BACKGROUND_COLOR"
    private val SELECTED_COLOR = "SELECTED_COLOR"
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action === "android.net.wifi.WIFI_AP_STATE_CHANGED") {
            val apState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)
            if (apState == 13) {
                binding.backgroundHotspot.setColorFilter(
                    Color.parseColor(PreferencesUtils.getString(SELECTED_COLOR,"#2C61CC")),
                    PorterDuff.Mode.SRC_IN
                )
            } else {
                binding.backgroundHotspot.setColorFilter(
                    Color.parseColor(PreferencesUtils.getString(BACKGROUND_COLOR,context.resources.getString(R.string.color_4DFFFFFF))),
                    PorterDuff.Mode.SRC_IN
                )
            }
        }
    }
}