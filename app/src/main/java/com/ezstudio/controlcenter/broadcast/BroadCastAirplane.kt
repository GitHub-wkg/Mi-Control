package com.ezstudio.controlcenter.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.provider.Settings
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutIconControlBinding
import com.ezteam.baseproject.utils.PreferencesUtils

class BroadCastAirplane : BroadcastReceiver() {
    lateinit var binding: LayoutIconControlBinding
    private val BACKGROUND_COLOR = "BACKGROUND_COLOR"
    private val SELECTED_COLOR = "SELECTED_COLOR"
    var listenerChangeStatusAirplane: ((Boolean) -> Unit)? = null
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_AIRPLANE_MODE_CHANGED == intent.action) {
            val isEnabled = Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON, 0
            ) != 0
            if (isEnabled) {
                binding.backgroundAirPlane.setColorFilter(
                    Color.parseColor(PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")),
                    PorterDuff.Mode.SRC_IN
                )
                listenerChangeStatusAirplane?.invoke(true)
            } else {
                binding.backgroundAirPlane.setColorFilter(
                    Color.parseColor(
                        PreferencesUtils.getString(
                            BACKGROUND_COLOR,
                            context.resources.getString(R.string.color_4DFFFFFF)
                        )
                    ),
                    PorterDuff.Mode.SRC_IN
                )
                listenerChangeStatusAirplane?.invoke(false)
            }
        }
    }
}