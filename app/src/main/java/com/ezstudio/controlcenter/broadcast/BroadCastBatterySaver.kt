package com.ezstudio.controlcenter.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutIconControlBinding
import com.ezstudio.controlcenter.databinding.WindownManagerBinding
import com.ezteam.baseproject.utils.PreferencesUtils

class BroadCastBatterySaver : BroadcastReceiver() {
    var binding: LayoutIconControlBinding? = null
    var layoutManager: WindownManagerBinding? = null
    var powerManager: PowerManager? = null
    private val BACKGROUND_COLOR = "BACKGROUND_COLOR"
    private val SELECTED_COLOR = "SELECTED_COLOR"
    override fun onReceive(context: Context, intent: Intent) {
        if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED == intent.action && powerManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding?.let {
                    if (powerManager!!.isPowerSaveMode) {
                        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                        val batteryPct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                        setPowerBattery(batteryPct)
                        it.backgroundBatterySaver.setColorFilter(
                            Color.parseColor(PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")),
                            PorterDuff.Mode.SRC_IN
                        )
                    } else {
                        it.backgroundBatterySaver.setColorFilter(
                            Color.parseColor(
                                PreferencesUtils.getString(
                                    BACKGROUND_COLOR,
                                    context.resources.getString(R.string.color_4DFFFFFF))),
                            PorterDuff.Mode.SRC_IN
                        )
                    }
                }
            }
        }
    }

    private fun setPowerBattery(batteryPct: Int) {
        when (batteryPct) {
            in 0..20 -> {
                layoutManager?.layoutTaskBar?.binding?.icBattery?.setImageResource(
                    R.drawable.ic_battery_saver_20
                )

            }
            in 20..40 -> {
                layoutManager?.layoutTaskBar?.binding?.icBattery?.setImageResource(
                    R.drawable.ic_battery_saver_40
                )
            }
            in 40..60 -> {
                layoutManager?.layoutTaskBar?.binding?.icBattery?.setImageResource(
                    R.drawable.ic_battery_saver_60
                )
            }
            in 60..80 -> {
                layoutManager?.layoutTaskBar?.binding?.icBattery?.setImageResource(
                    R.drawable.ic_battery_saver_80
                )
            }
            else -> {
                layoutManager?.layoutTaskBar?.binding?.icBattery?.setImageResource(
                    R.drawable.ic_battery_saver_full
                )
            }
        }
    }
}