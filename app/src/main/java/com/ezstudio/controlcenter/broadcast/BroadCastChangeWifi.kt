package com.ezstudio.controlcenter.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutStatusBinding
import com.ezstudio.controlcenter.databinding.WindownManagerBinding
import com.ezteam.baseproject.utils.PreferencesUtils

class BroadCastChangeWifi : BroadcastReceiver() {
    var bindingViewStatus: LayoutStatusBinding? = null
    var layoutWindowManager: WindownManagerBinding? = null
    var wifiManager: WifiManager? = null
    private val BACKGROUND_COLOR = "BACKGROUND_COLOR"
    private val SELECTED_COLOR = "SELECTED_COLOR"
    override fun onReceive(context: Context, intent: Intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
            if (isConnection(context) && wifiManager != null) {
                layoutWindowManager?.layoutBtnFist?.binding?.name?.text =
                    if (wifiManager!!.connectionInfo.ssid.startsWith("<unknown"))
                        context.getString(R.string.wi_fi) else wifiManager!!.connectionInfo.ssid.replace(
                        '"',
                        ' '
                    ).trim()
                bindingViewStatus?.icWifi?.visibility = View.VISIBLE
                layoutWindowManager?.let {
                    setColorDrawable(
                        layoutWindowManager!!.layoutBtnFist.binding.btnWifi,
                        PreferencesUtils.getString(SELECTED_COLOR,"#2C61CC")
                    )
                }
                //
                layoutWindowManager?.layoutBtnFist?.binding?.txtStatusWifi?.text =
                    context.getString(R.string.on)
                //
//                if (this.wifiManager != null) {
//                    wifiManager!!.startScan()
//                    val wifiList: List<ScanResult> = wifiManager!!.scanResults
//                    listenerScanningWifi?.invoke(wifiList)
//                }
            } else {
                layoutWindowManager?.layoutBtnFist?.binding?.name?.text =
                    context.getString(R.string.wi_fi)
                bindingViewStatus?.icWifi?.visibility = View.GONE
                layoutWindowManager?.let {
                    setColorDrawable(
                        layoutWindowManager!!.layoutBtnFist.binding.btnWifi,
                        PreferencesUtils.getString(BACKGROUND_COLOR, context.resources.getString(R.string.color_4DFFFFFF))
                    )
                }
                //
                layoutWindowManager?.layoutBtnFist?.binding?.txtStatusWifi?.text =
                    context.getString(R.string.off)
            }
        }
//        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION == intent.action) {
//            if (wifiManager != null ) {
//                val wifiList: List<ScanResult> = wifiManager!!.scanResults
//                listenerScanningWifi?.invoke(wifiList)
//            }
//        }
        if (WifiManager.WIFI_STATE_CHANGED_ACTION == intent.action) {
            if (wifiManager != null) {
                if (!wifiManager!!.isWifiEnabled) {
                    layoutWindowManager?.layoutBtnFist?.binding?.name?.text =
                        context.getString(R.string.wi_fi)
                    bindingViewStatus?.icWifi?.visibility = View.GONE
                    layoutWindowManager?.let {
                        setColorDrawable(
                            layoutWindowManager!!.layoutBtnFist.binding.btnWifi,
                            PreferencesUtils.getString(BACKGROUND_COLOR, context.resources.getString(R.string.color_4DFFFFFF))
                        )
                    }
                    //
                    layoutWindowManager?.layoutBtnFist?.binding?.txtStatusWifi?.text =
                        context.getString(R.string.off)
                } else if (!isConnection(context) && wifiManager!!.isWifiEnabled) {
                    layoutWindowManager?.let {
                        setColorDrawable(
                            layoutWindowManager!!.layoutBtnFist.binding.btnWifi,
                          PreferencesUtils.getString(SELECTED_COLOR,"#2C61CC")
                        )
                    }
                    //
                    layoutWindowManager?.layoutBtnFist?.binding?.txtStatusWifi?.text =
                        context.getString(R.string.connecting)
                }
            }
        }
        if (WifiManager.RSSI_CHANGED_ACTION == intent.action) {
            when (getWifiLevel(context)) {
                in 0 until 34 -> {
                    bindingViewStatus?.icWifi?.setImageResource(R.drawable.ic_wifi_min)
                }
                in 34 until 66 -> {
                    bindingViewStatus?.icWifi?.setImageResource(R.drawable.ic_wifi_normal)
                }
                in 66..100 -> {
                    bindingViewStatus?.icWifi?.setImageResource(R.drawable.ic_wifi_full)
                }
            }
        }
    }

    private fun isConnection(context: Context?): Boolean {
        val connectionManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkInfo = connectionManager.activeNetwork ?: return false
            val capabilities = connectionManager.getNetworkCapabilities(networkInfo)
            return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            val networkInfo = connectionManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private fun getWifiLevel(context: Context): Int {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val linkSpeed = wifiManager.connectionInfo.rssi
        val MIN_RSSI = -100
        val MAX_RSSI = -55
        val numLevels = 101
        return when {
            linkSpeed <= MIN_RSSI -> {
                0
            }
            linkSpeed >= MAX_RSSI -> {
                numLevels - 1
            }
            else -> {
                val inputRange = (MAX_RSSI - MIN_RSSI).toFloat()
                val outputRange: Float = numLevels - 1F
                return ((linkSpeed - MIN_RSSI).toFloat() * outputRange / inputRange).toInt()
            }
        }
    }

    private fun setColorDrawable(layout: ConstraintLayout, color: String) {
        val bgShape = layout.background as GradientDrawable
        bgShape.mutate()
        bgShape.setColor(Color.parseColor(color))
    }
}