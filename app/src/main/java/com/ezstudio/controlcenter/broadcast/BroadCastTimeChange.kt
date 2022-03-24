package com.ezstudio.controlcenter.broadcast

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ezstudio.controlcenter.databinding.LayoutViewTopNotificationBinding
import java.text.SimpleDateFormat
import java.util.*

class BroadCastTimeChange : BroadcastReceiver() {
    lateinit var binding: LayoutViewTopNotificationBinding

    @SuppressLint("SimpleDateFormat")
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let { intents ->
            context?.let { context ->
                if (intents.action == "android.intent.action.TIME_TICK") {
                    val calendar = Calendar.getInstance()
                    binding.txtHour.text = SimpleDateFormat("hh:mm").format(calendar.time)
                    binding.txtCalender.text = SimpleDateFormat("EEE dd MMM").format(calendar.time)
                }
            }
        }
    }
}