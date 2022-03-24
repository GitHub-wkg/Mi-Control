package com.ezstudio.controlcenter.widget

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutStatusBinding


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ViewStatus(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    lateinit var binding: LayoutStatusBinding
    init {
        initView()
        getNetworkSpeed()
    }

    private fun initView() {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val view = LayoutInflater.from(context).inflate(R.layout.layout_status, this, true)
        binding = LayoutStatusBinding.bind(view)
        binding.txtInternet.isSelected = true
        when (telephonyManager.simState) {
            TelephonyManager.SIM_STATE_READY -> {
                binding.nameSim.text = telephonyManager.simOperatorName
            }
            else -> {
                binding.nameSim.text = context.getString(R.string.no_sim_card)
                binding.nameSim.isSelected = true
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getNetworkSpeed() {
        val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nc = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) as NetworkCapabilities
            binding.txtInternet.text = nc.linkDownstreamBandwidthKbps.toString()
        }
    }
}