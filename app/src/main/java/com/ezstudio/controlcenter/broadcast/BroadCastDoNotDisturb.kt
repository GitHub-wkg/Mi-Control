package com.ezstudio.controlcenter.broadcast

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutIconControlBinding
import com.ezteam.baseproject.utils.PreferencesUtils

class BroadCastDoNotDisturb : BroadcastReceiver() {
    lateinit var binding: LayoutIconControlBinding
    private val BACKGROUND_COLOR = "BACKGROUND_COLOR"
    private val SELECTED_COLOR = "SELECTED_COLOR"
    override fun onReceive(context: Context, intent: Intent) {
        if (NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED == intent.action) {
            val mNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                when (mNotificationManager.currentInterruptionFilter) {
                    NotificationManager.INTERRUPTION_FILTER_ALARMS -> {
                        binding.backgroundDoNotDisturb.setColorFilter(
                            Color.parseColor(
                                PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")
                            ),
                            PorterDuff.Mode.SRC_IN
                        )
                    }
                    NotificationManager.INTERRUPTION_FILTER_NONE -> {
                        binding.backgroundDoNotDisturb.setColorFilter(
                            Color.parseColor(
                                PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")
                            ), PorterDuff.Mode.SRC_IN
                        )
                    }
                    NotificationManager.INTERRUPTION_FILTER_ALL -> {
                        binding.backgroundDoNotDisturb.setColorFilter(
                            Color.parseColor(
                                PreferencesUtils.getString(
                                    BACKGROUND_COLOR,
                                    context.resources.getString(R.string.color_4DFFFFFF)
                                )
                            ), PorterDuff.Mode.SRC_IN
                        )
                    }
                    NotificationManager.INTERRUPTION_FILTER_PRIORITY -> {
                        binding.backgroundDoNotDisturb.setColorFilter(
                            Color.parseColor(
                                PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")
                            ), PorterDuff.Mode.SRC_IN
                        )
                    }
                    NotificationManager.INTERRUPTION_FILTER_UNKNOWN -> {
                        binding.backgroundDoNotDisturb.setColorFilter(
                            Color.parseColor(
                                PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")
                            ), PorterDuff.Mode.SRC_IN
                        )
                    }
                }
            }
        }
    }


}