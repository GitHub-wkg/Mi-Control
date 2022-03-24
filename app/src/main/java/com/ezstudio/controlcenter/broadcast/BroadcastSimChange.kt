package com.ezstudio.controlcenter.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutStatusBinding

class BroadcastSimChange : BroadcastReceiver() {
    lateinit var binding: LayoutStatusBinding
    var changeListener: (() -> Unit)? = null
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null) {
            if ("android.intent.action.SIM_STATE_CHANGED" == intent.action) {
                changeListener?.invoke()
            }
        }
    }
}
