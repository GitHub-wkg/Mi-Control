package com.ezstudio.controlcenter.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ezstudio.controlcenter.common.KeyBroadCast
import com.ezstudio.controlcenter.common.KeyIntent
import com.ezstudio.controlcenter.service.NotificationListener

class BroadCastNotificationManager : BroadcastReceiver() {
    lateinit var notificationListenerService: NotificationListener
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let { it ->
            context?.let { context ->
                when (it.action) {
                    KeyBroadCast.ACTION_NOTIFICATION_CLEAR_FOR_KEY -> {
                        notificationListenerService.clearNotificationForKey(
                            it.getStringExtra(KeyIntent.key)
                                ?: "", it.getIntExtra(KeyIntent.ID, -100)
                        )
                    }
                    KeyBroadCast.ACTION_NOTIFICATION_CLEAR_ALL -> {
                        notificationListenerService.clearAll()
                        notificationListenerService.getNotification()
                    }
                    KeyBroadCast.ACTION_RELOAD -> {
//                        notificationListenerService.getNotification()
                    }
                }
            }
        }
    }
}