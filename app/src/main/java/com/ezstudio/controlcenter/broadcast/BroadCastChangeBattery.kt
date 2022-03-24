package com.ezstudio.controlcenter.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.PowerManager
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutStatusBinding

class BroadCastChangeBattery : BroadcastReceiver() {
    lateinit var binding: LayoutStatusBinding
    var isPowerConnected = false
    var batteryPct = 0
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BATTERY_CHANGED -> {
                // khi sac luon luon vao
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                batteryPct = (level * 100 / scale)
                binding.txtPercent.text = "$batteryPct%"
                setPowerBattery(batteryPct)
            }
            Intent.ACTION_POWER_CONNECTED -> {
                // co vao ACTION_POWER_CONNECTED
                isPowerConnected = true
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                isPowerConnected = false
                setPowerBattery(batteryPct)
            }
        }
    }

    fun setPowerBattery(batteryPct: Int) {
        val powerManager: PowerManager =
            binding.icBattery.context.getSystemService(Context.POWER_SERVICE) as PowerManager



        when (batteryPct) {
            in 0..20 -> {
                binding.icBattery.setImageResource(
                    if (isPowerConnected) R.drawable.ic_charging_battery_20
                    else if (!powerManager.isPowerSaveMode) R.drawable.ic_battery_status_20_percent
                    else R.drawable.ic_battery_saver_20
                )
            }
            in 20..40 -> {
                binding.icBattery.setImageResource(
                    if (isPowerConnected) R.drawable.ic_charging_battery_40
                    else if (!powerManager.isPowerSaveMode) R.drawable.ic_battery_status_40percent
                    else R.drawable.ic_battery_saver_40
                )
            }
            in 40..60 -> {
                binding.icBattery.setImageResource(
                    if (isPowerConnected) R.drawable.ic_charging_battery_60
                    else if (!powerManager.isPowerSaveMode) R.drawable.ic_battery_status_60percent
                    else R.drawable.ic_battery_saver_60
                )
            }
            in 60..80 -> {
                binding.icBattery.setImageResource(
                    if (isPowerConnected) R.drawable.ic_charging_battery_80
                    else if (!powerManager.isPowerSaveMode) R.drawable.ic_battery_status_80_percent
                    else R.drawable.ic_battery_saver_80
                )
            }
            else -> {
                binding.icBattery.setImageResource(
                    if (isPowerConnected) R.drawable.ic_charging_battery_full
                    else if (!powerManager.isPowerSaveMode) R.drawable.ic_battery_status_full
                    else R.drawable.ic_battery_saver_full
                )
            }
        }
    }
}