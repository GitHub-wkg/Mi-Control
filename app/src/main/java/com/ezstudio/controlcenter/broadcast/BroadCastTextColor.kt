package com.ezstudio.controlcenter.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.WindownManagerBinding
import com.ezteam.baseproject.utils.PreferencesUtils

class BroadCastTextColor : BroadcastReceiver() {
    lateinit var binding: WindownManagerBinding
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            if (context != null) {
                if (it.action == context.resources.getString(R.string.action_text_color)) {
                    setColorText(
                        PreferencesUtils.getString(
                            context.resources.getString(R.string.TEXT_COLOR),
                            "#FFFFFF"
                        )
                    )
                } else if (it.action == context.resources.getString(R.string.action_icon_color)) {
                    setColorIcon(
                        PreferencesUtils.getString(
                            context.resources.getString(R.string.ICON_COLOR),
                            "#FFFFFF"
                        ), context
                    )
                } else {
                    setDimmerColor(
                        PreferencesUtils.getString(
                            context.resources.getString(R.string.DIMMER_COLOR),
                            "#FFFFFFFF"
                        )
                    )
                }
            }
        }
    }

    private fun setColorText(color: String) {
        binding.layoutBtnFist.binding.txtMobileData.setTextColor(Color.parseColor(color))
        binding.layoutBtnFist.binding.txtStatusMobileData.setTextColor(Color.parseColor(color))
        binding.layoutBtnFist.binding.name.setTextColor(Color.parseColor(color))
        binding.layoutBtnFist.binding.txtStatusWifi.setTextColor(Color.parseColor(color))
        //
        binding.layoutBtnSecondsLine.binding.txtBluetooth.setTextColor(Color.parseColor(color))
        binding.layoutBtnSecondsLine.binding.txtStatusBluetooth.setTextColor(Color.parseColor(color))
        binding.layoutBtnSecondsLine.binding.txtFlashlight.setTextColor(Color.parseColor(color))
        binding.layoutBtnSecondsLine.binding.txtStatusFlash.setTextColor(Color.parseColor(color))
        //
        binding.layoutIconControls.binding.txtMute.setTextColor(Color.parseColor(color))
        binding.layoutIconControls.binding.txtBatterySaver.setTextColor(Color.parseColor(color))
        binding.layoutIconControls.binding.txtAirplaneMode.setTextColor(Color.parseColor(color))
        binding.layoutIconControls.binding.txtDoNotDisturb.setTextColor(Color.parseColor(color))
        //
        binding.layoutControlsSecondsLine.binding.txtAutoRotate.setTextColor(Color.parseColor(color))
        binding.layoutControlsSecondsLine.binding.txtNightLight.setTextColor(Color.parseColor(color))
        binding.layoutControlsSecondsLine.binding.txtDarkTheme.setTextColor(Color.parseColor(color))
        binding.layoutControlsSecondsLine.binding.txtHotspot.setTextColor(Color.parseColor(color))
        //
        binding.layoutIconCenterHide.binding.txtDataSaver.setTextColor(Color.parseColor(color))
        binding.layoutIconCenterHide.binding.txtScreenTransmission.setTextColor(
            Color.parseColor(
                color
            )
        )
        binding.layoutIconCenterHide.binding.txtNfc.setTextColor(Color.parseColor(color))
        binding.layoutIconCenterHide.binding.txtLocation.setTextColor(Color.parseColor(color))
    }

    private fun setColorIcon(color: String, context: Context) {
        if (!PreferencesUtils.getBoolean(
                context.resources.getString(R.string.state_usage_data),
                false
            )&& !PreferencesUtils.getBoolean(
                context.resources.getString(R.string.stata_usage_day),
                false
            )
        ) {
            binding.layoutBtnFist.binding.icDataMobile.setColorFilter(Color.parseColor(color))
        }
        binding.layoutBtnFist.binding.icWifi.setColorFilter(Color.parseColor(color))
        //
        binding.layoutBtnSecondsLine.binding.icBluetooth.setColorFilter(Color.parseColor(color))
        binding.layoutBtnSecondsLine.binding.icFlashLight.setColorFilter(Color.parseColor(color))
        //
        binding.layoutIconControls.binding.icMute.setColorFilter(Color.parseColor(color))
        binding.layoutIconControls.binding.icBatterySaver.setColorFilter(Color.parseColor(color))
        binding.layoutIconControls.binding.icAirplaneMode.setColorFilter(Color.parseColor(color))
        binding.layoutIconControls.binding.icDoNotDisturb.setColorFilter(Color.parseColor(color))
        //
        binding.layoutControlsSecondsLine.binding.icAutoRotate.setColorFilter(
            Color.parseColor(
                color
            )
        )
        binding.layoutControlsSecondsLine.binding.icNightLight.setColorFilter(
            Color.parseColor(
                color
            )
        )
        binding.layoutControlsSecondsLine.binding.icDarkTheme.setColorFilter(Color.parseColor(color))
        binding.layoutControlsSecondsLine.binding.icHotspot.setColorFilter(Color.parseColor(color))
        //
        binding.layoutIconCenterHide.binding.icDataSaver.setColorFilter(Color.parseColor(color))
        binding.layoutIconCenterHide.binding.icScreenTransmission.setColorFilter(
            Color.parseColor(
                color
            )
        )
        binding.layoutIconCenterHide.binding.icNfc.setColorFilter(Color.parseColor(color))
        binding.layoutIconCenterHide.binding.icLocation.setColorFilter(Color.parseColor(color))
        //
        binding.icA.setColorFilter(Color.parseColor(color))
    }

    private fun setDimmerColor(color: String) {
        val colorOpacity = color.substring(3)
        binding.boxedVertical.setBackgroundColorDimmer = (Color.parseColor("#CC${colorOpacity}"))
        binding.boxedVertical.setProgressColor = (Color.parseColor(color))
    }
}