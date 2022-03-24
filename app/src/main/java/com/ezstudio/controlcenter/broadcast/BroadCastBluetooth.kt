package com.ezstudio.controlcenter.broadcast

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.WindownManagerBinding
import com.ezteam.baseproject.utils.PreferencesUtils

class BroadCastBluetooth : BroadcastReceiver() {
    var layoutWindowManager: WindownManagerBinding? = null
    private val BACKGROUND_COLOR = "BACKGROUND_COLOR"
    private val SELECTED_COLOR = "SELECTED_COLOR"
    override fun onReceive(context: Context, intent: Intent) {
        if (BluetoothAdapter.ACTION_STATE_CHANGED == intent.action) {
            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                BluetoothAdapter.STATE_OFF -> {
                    layoutWindowManager?.let {
                        setColorDrawable(
                            layoutWindowManager!!.layoutBtnSecondsLine.binding.btnBluetooth,
                           PreferencesUtils.getString(BACKGROUND_COLOR,context.resources.getString(R.string.color_4DFFFFFF))
                        )
                    }
                    layoutWindowManager?.layoutBtnSecondsLine?.binding?.txtStatusBluetooth?.text =
                        context.getString(R.string.off)
                }
                BluetoothAdapter.STATE_ON -> {
                    layoutWindowManager?.let {
                        setColorDrawable(
                            layoutWindowManager!!.layoutBtnSecondsLine.binding.btnBluetooth,
                            PreferencesUtils.getString(SELECTED_COLOR,"#2C61CC")
                        )
                    }
                    layoutWindowManager?.layoutBtnSecondsLine?.binding?.txtStatusBluetooth?.text =
                        context.getString(R.string.on)
                }
            }

        }
    }

    private fun setColorDrawable(layout: ConstraintLayout, color: String) {
        val bgShape = layout.background as GradientDrawable
        bgShape.mutate()
        bgShape.setColor(Color.parseColor(color))
    }
}