package com.ezstudio.controlcenter.service

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.ezstudio.controlcenter.activity.ActivityRequestPermission
import com.ezstudio.controlcenter.broadcast.BroadCastNotificationManager
import com.ezstudio.controlcenter.common.KeyBroadCast
import com.ezstudio.controlcenter.model.ItemNotification
import com.ezstudio.controlcenter.viewmodel.NotificationViewModel
import org.koin.android.ext.android.inject

class NotificationListener : NotificationListenerService() {
    private var connected = false
    private var broadCastNotificationListenerService: BroadCastNotificationManager? = null
    private val viewModel by inject<NotificationViewModel>()

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        //backActivityPermission
        backActivityPermission()
        //
        connected = true
        broadCastNotificationListenerService = BroadCastNotificationManager()
        broadCastNotificationListenerService?.notificationListenerService = this
        val intent = IntentFilter(KeyBroadCast.ACTION_RELOAD)
        intent.addAction(KeyBroadCast.ACTION_NOTIFICATION_CLEAR_ALL)
        intent.addAction(KeyBroadCast.ACTION_NOTIFICATION_CLEAR_FOR_KEY)
        registerReceiver(broadCastNotificationListenerService, intent)
        getNotification()
    }

    override fun onListenerDisconnected() {
        connected = false
        super.onListenerDisconnected()
        broadCastNotificationListenerService?.let {
            unregisterReceiver(it)
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn != null && connected) {
            sbn.postTime
            val itemDataNotification = ItemNotification(
                sbn.notification.extras.getInt(Notification.EXTRA_SMALL_ICON),
                sbn.notification.extras.getString(Notification.EXTRA_TITLE),
                sbn.notification.extras.getString(Notification.EXTRA_TEXT),
                sbn.postTime,
                sbn.packageName,
                sbn.key,
                sbn.id,
                sbn.notification.contentIntent,
                sbn.isClearable
            )
            Log.e("HuyN", " Post Noti:")

            try {
                packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(
                        sbn.packageName!!,
                        PackageManager.GET_META_DATA
                    )
                )
                //
                val intent = Intent(KeyBroadCast.KEY_PULL_NOTIFICATION)
                intent.putExtra("ITEM", itemDataNotification)
                sendBroadcast(intent)
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e("HuyN", " Lỗi Get Tên App : ")
            }
        }
    }

    private fun backActivityPermission() {
        Log.d("Huy", "backActivityPermission: ${ActivityRequestPermission.isNotifyPermission}")
        if (ActivityRequestPermission.isNotifyPermission) {
            startActivity(
                Intent(this, ActivityRequestPermission::class.java).apply {
                    putExtra(
                        "PERMISSION",
                        true
                    )
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn != null && connected) {
            val itemDataNotification = ItemNotification(
                sbn.notification.extras.getInt(Notification.EXTRA_SMALL_ICON),
                sbn.notification.extras.getString(Notification.EXTRA_TITLE),
                sbn.notification.extras.getString(Notification.EXTRA_TEXT),
                -1,
                sbn.packageName,
                sbn.key,
                sbn.id,
                sbn.notification.contentIntent,
                sbn.isClearable
            )
//            sbn.notification.channelId
            val intent = Intent(KeyBroadCast.KEY_PULL_NOTIFICATION)
            intent.putExtra("ITEM", itemDataNotification)
            sendBroadcast(intent)
        }
    }

    fun getNotification() {
        if (connected) {
            try {
                viewModel.getNotification(activeNotifications, this)
            } catch (e: SecurityException) {
            }
        }
    }

    fun clearNotificationForKey(key: String, id: Int) {
        if (connected) {
            cancelNotification(key)
        }
    }

    fun clearAll() {
        this.cancelAllNotifications()
        sendBroadcast(Intent(KeyBroadCast.ACTION_RELOAD))
    }
}