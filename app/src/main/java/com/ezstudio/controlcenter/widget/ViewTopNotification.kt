package com.ezstudio.controlcenter.widget

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutViewTopNotificationBinding
import java.text.SimpleDateFormat
import java.util.*

class ViewTopNotification(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    lateinit var binding: LayoutViewTopNotificationBinding
    private val calendar = Calendar.getInstance()
    var listenerEndWindow : (()->Unit)? =  null
    init {
        initData()
        initView()
        initListener()
    }

    private fun initListener() {
        binding.icNotification.setOnClickListener {
            val intent = Intent()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            } else {
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra("app_package", context.packageName)
                intent.putExtra("app_uid", context.applicationInfo.uid)
            }
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                context.startActivity(Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
                    .setData(Uri.fromParts("package", context.packageName, null as String?))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            listenerEndWindow?.invoke()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_view_top_notification, this, true)
        binding = LayoutViewTopNotificationBinding.bind(view)
        binding.txtHour.text = SimpleDateFormat("hh:mm").format(calendar.time)
        binding.txtCalender.text = SimpleDateFormat("EEE dd MMM").format(calendar.time)
    }

    private fun initData() {
    }
}