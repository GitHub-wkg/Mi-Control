package com.ezstudio.controlcenter.broadcast

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.nfc.NfcAdapter
import android.os.Build
import android.os.PowerManager
import android.telephony.TelephonyManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.WindownManagerBinding
import com.ezstudio.controlcenter.windown_manager.MyWindowManager
import com.ezteam.baseproject.utils.PreferencesUtils

class BroadCastBackgroundColor : BroadcastReceiver() {
    lateinit var binding: WindownManagerBinding
    lateinit var windowManager: MyWindowManager
    private val BACKGROUND_COLOR = "BACKGROUND_COLOR"
    private val SELECTED_COLOR = "SELECTED_COLOR"

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            if (context != null) {
                val connManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                //
                if (it.action == context.resources.getString(R.string.action_background_color)) {
                    //
                    binding.layoutBtnFist.setStageDataMobile()
                    //
                    if (mWifi != null) {
                        binding.layoutTaskBar.detectWifi(mWifi)
                    }
                    binding.layoutBtnSecondsLine.setStageBluetooth()
                    //
                    // loi color flashlight
                    if (binding.layoutBtnSecondsLine.binding.txtStatusFlash.text.toString()
                            .equals(context.getString(R.string.off), true)
                    ) {
                        setColorDrawable(
                            binding.layoutBtnSecondsLine.binding.btnFlashlight,
                            PreferencesUtils.getString(
                                BACKGROUND_COLOR,
                                context.resources.getString(R.string.color_4DFFFFFF)
                            )
                        )
                    }
                    //
                    binding.layoutIconControls.setStageSound()
                    //
                    binding.layoutIconControls.setStageBatterySaver()
                    //
                    binding.layoutIconControls.setStageAirPlane()
                    //
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        binding.layoutIconControls.setStageDoNotDisturb()
                    }
                    //
                    binding.layoutControlsSecondsLine.setStageAutoRotation()
                    //
                    binding.layoutControlsSecondsLine.setStageViewNightLight()
                    //
                    binding.layoutControlsSecondsLine.setStageDarkTheme()
                    //
                    binding.layoutControlsSecondsLine.setStageHotspost()
                    //
                    binding.layoutIconCenterHide.setStageDataSaver()
                    //
                    binding.layoutIconCenterHide.setStageNFC()
                    //
                    binding.layoutIconCenterHide.setStageLocation()
                    //
                    windowManager.detectBrightnessMode()
                    // line
                    windowManager.changeColorLine()
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