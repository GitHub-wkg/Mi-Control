package com.ezstudio.controlcenter.listener

import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.util.Log
import android.view.View
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutStatusBinding

class Sim2SignalStrengthsListener(subId: Int) : PhoneStateListener() {
    lateinit var binding: LayoutStatusBinding
    override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
        super.onSignalStrengthsChanged(signalStrength)
        binding.icSilentSim2.visibility = View.VISIBLE
        when (getSignalStrengthsLevel(signalStrength)) {
            0 -> binding.icSilentSim2.setImageResource(R.drawable.ic_not_signal_sim)
            1 -> binding.icSilentSim2.setImageResource(R.drawable.ic_signal_sim_min)
            2 -> binding.icSilentSim2.setImageResource(R.drawable.ic_signal_sim_normal_min)
            3 -> binding.icSilentSim2.setImageResource(R.drawable.ic_signal_sim_normal_max)
            4 -> binding.icSilentSim2.setImageResource(R.drawable.ic_signal_sim_full)
        }
    }

    private fun getSignalStrengthsLevel(signalStrength: SignalStrength): Int {
        var level = -1
        try {
            val levelMethod = SignalStrength::class.java.getDeclaredMethod("getLevel")
            level = levelMethod.invoke(signalStrength) as Int
        } catch (e: Exception) {
        }
        return level
    }

    init {
        ReflectUtil.setFieldValue(this, "mSubId", subId)
    }
}