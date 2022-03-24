package com.ezstudio.controlcenter.activity

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.broadcast.BroadCastAccessibility
import com.ezstudio.controlcenter.common.EventTracking
import com.ezstudio.controlcenter.databinding.ActivityRequestPermissionBinding
import com.ezstudio.controlcenter.service.MyAccessibilityService
import com.ezstudio.controlcenter.service.NotificationListener
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.baseproject.utils.PreferencesUtils
import com.google.android.gms.ads.ez.analytics.FlurryAnalytics

class ActivityRequestPermission : BaseActivity<ActivityRequestPermissionBinding>() {
    private val REQUEST_CODE_WRITE_SETTINGS = 2
    private val BACKGROUND_MODEL = "BACKGROUND_MODEL"
    private lateinit var broadCastAccessibility: BroadCastAccessibility

    companion object {
        var isNotifyPermission = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isNotifyPermission = false
    }

    override fun initView() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            binding.imgBackgroundNotification.visibility = View.GONE
            binding.layoutStateNotification.visibility = View.GONE
        }
        FlurryAnalytics.logEvent(EventTracking.GRANT_PERMISSION, "open")
        // check permission state phone
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            binding.layout2Sim.visibility = View.VISIBLE
            binding.imgBackground2Sim.visibility = View.VISIBLE
            binding.switchStatus2Sim.isChecked = false
        } else {
            binding.layout2Sim.visibility = View.GONE
            binding.imgBackground2Sim.visibility = View.GONE
        }
        // check notify
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (isNotificationServiceRunning(this)) {
                binding.layoutStateNotification.visibility = View.GONE
                binding.imgBackgroundNotification.visibility = View.GONE
            } else {
                binding.layoutStateNotification.visibility = View.VISIBLE
                binding.imgBackgroundNotification.visibility = View.VISIBLE
                binding.switchOverlay.isChecked = false
            }
        }
        // check Accessibility
        if (MyAccessibilityService.isRunning) {
            binding.layoutAccessibility.visibility = View.GONE
            binding.imgBackgroundAccessibility.visibility = View.GONE
        } else {
            binding.layoutAccessibility.visibility = View.VISIBLE
            binding.imgBackgroundAccessibility.visibility = View.VISIBLE
            binding.switchStatusAccessibility.isChecked = false
        }

        broadCastAccessibility = BroadCastAccessibility()
        broadCastAccessibility.binding = binding
        val intent = IntentFilter("ACTION_ACCESSIBILITY_SERVICE")
        this.registerReceiver(broadCastAccessibility, intent)
    }

    override fun initData() {}

    override fun initListener() {
        binding.switchStatus2Sim.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                requestPermission(complete = {
                    if (it) {
                        binding.layout2Sim.visibility = View.GONE
                        binding.imgBackground2Sim.visibility = View.GONE
                    } else {
                        binding.layout2Sim.visibility = View.VISIBLE
                        binding.imgBackground2Sim.visibility = View.VISIBLE
                        binding.switchStatus2Sim.isChecked = false
                    }
                    FlurryAnalytics.logEvent(EventTracking.GRANT_PERMISSION, "2Sim")
                }, Manifest.permission.READ_PHONE_STATE)
            }
        }
        // notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            binding.switchOverlay.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    isNotifyPermission = true
                    startNotifyPermission()
                    binding.switchOverlay.isChecked = false
                }
            }
        }
        // Accessibility
        //
        //
        binding.switchStatusAccessibility.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                startAccessibilitySetting()
                binding.switchStatusAccessibility.isChecked = false
                FlurryAnalytics.logEvent(EventTracking.GRANT_PERMISSION, "Accessibility")
            }
        }
        //
        binding.icBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun viewBinding(): ActivityRequestPermissionBinding {
        return ActivityRequestPermissionBinding.inflate(LayoutInflater.from(this))
    }

    private fun startNotifyPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        val bundle = Bundle()
        val str = packageName + "/" + NotificationListener::class.java.getName()
        bundle.putString(":settings:fragment_args_key", str)
        intent.putExtra(":settings:fragment_args_key", str)
        intent.putExtra(":settings:show_fragment_args", bundle)
        try {
            startActivity(intent)
            Toast.makeText(
                applicationContext,
                resources.getString(R.string.find_app_here, resources.getString(R.string.app_name)),
                Toast.LENGTH_SHORT
            ).show()
        } catch (unused: java.lang.Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(broadCastAccessibility)
    }

    private fun isNotificationServiceRunning(context: Context): Boolean {
        return try {
            val contentResolver: ContentResolver = context.contentResolver
            val enabledNotificationListeners: String =
                Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            val packageName: String = context.packageName
            enabledNotificationListeners.contains(
                packageName
            )
        } catch (e: Exception) {
            true
        }
    }

    private fun startAccessibilitySetting() {
        var intent = Intent("com.samsung.accessibility.installed_service")
        if (intent.resolveActivity(packageManager) == null) {
            intent = Intent("android.settings.ACCESSIBILITY_SETTINGS")
        }
        val bundle = Bundle()
        val str = packageName + "/" + MyAccessibilityService::class.java.getName()
        bundle.putString(":settings:fragment_args_key", str)
        intent.putExtra(":settings:fragment_args_key", str)
        intent.putExtra(":settings:show_fragment_args", bundle)
        try {
            startActivity(intent)
            Toast.makeText(
                applicationContext,
                resources.getString(R.string.find_app_here, resources.getString(R.string.app_name)),
                Toast.LENGTH_SHORT
            ).show()
        } catch (unused: java.lang.Exception) {
        }
    }

    override fun onRestart() {
        super.onRestart()
        isNotifyPermission = false
        if (isNotificationServiceRunning(this)) {
            binding.layoutStateNotification.visibility = View.GONE
            binding.imgBackgroundNotification.visibility = View.GONE
            PreferencesUtils.putString(
                BACKGROUND_MODEL, "null"
            )
            PreferencesUtils.putString(
                resources.getString(R.string.TEXT_COLOR), "#FFFFFF"
            )
//            startActivity(Intent(this, Splash::class.java).putExtra("PERMISSION", true))
        } else {
            binding.layoutStateNotification.visibility = View.VISIBLE
            binding.imgBackgroundNotification.visibility = View.VISIBLE
            binding.switchOverlay.isChecked = false
        }
    }
}