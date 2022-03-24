package com.ezstudio.controlcenter.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.TrafficStats
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.activity.Splash
import com.ezstudio.controlcenter.activity.SystemShadeActivity
import com.ezstudio.controlcenter.broadcast.BroadCastAccessibility
import com.ezstudio.controlcenter.homewatcher.HomeWatcher
import com.ezstudio.controlcenter.interfaces.OnHomePressedListener
import com.ezstudio.controlcenter.service.accessiblilityUtils.*
import com.ezstudio.controlcenter.widget.ViewDialogOpenSettings
import com.ezstudio.controlcenter.windown_manager.MyWindowManager
import com.ezstudio.controlcenter.windown_manager.WindowSplash
import com.ezteam.baseproject.utils.PreferencesUtils
import org.koin.core.component.KoinApiExtension
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*

class MyAccessibilityService : AccessibilityService() {
    private lateinit var myWindowManager: MyWindowManager
    private lateinit var windowSplash: WindowSplash
    private lateinit var broadCastAccessibility: BroadCastAccessibility
    private val TOTAL_DATA_ON = "TOTAL_DATA_ON"
    private var count = 0
    private lateinit var calendar: Calendar
    lateinit var notificationManager: NotificationManager

    enum class DoingAction {
        FirstInit, None, BlueFilter, AirPlane, AutoRotate, DarkTheme, Hotspot, ScreenTransmission,
        NFC, Location, BatterySaver, DataSaver, Wifi, MobileData
    }

    var doing = DoingAction.None

    companion object {
        @JvmStatic
        var isRunning = false
        var instance: MyAccessibilityService? = null
    }

    fun actionAutoClick(doingAction: DoingAction) {
        StatusBarAutomatic.expandStatusBar(this)
        Handler().postDelayed({
            doing = doingAction
        }, 300) /*time delay show status bar*/
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        initView()
        PreferencesUtils.putBoolean(
            resources.getString(R.string.MEDIA_PROJECTION_ENABLE),
            false
        )
        // noti de quay man hinh cho service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForegroundService()
        }
        count = PreferencesUtils.getInteger(resources.getString(R.string.count), 0)
        calendar = Calendar.getInstance()
        instance ?: let {
            instance = this
        }
        //
        startOldActivity(this)
        // start home watcher
    }

    fun initView() {
        doing = DoingAction.FirstInit
        myWindowManager = MyWindowManager(this)
        windowSplash = WindowSplash(this, myWindowManager.windowManager)
        //
        broadCastAccessibility = BroadCastAccessibility()
        broadCastAccessibility.myServiceAccessibility = this
        val intentFilter = IntentFilter("ACTION_DISABLE_ACCESSIBILITY_SERVICE")
        this.registerReceiver(broadCastAccessibility, intentFilter)
        //
        val intent = Intent(this, Splash::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("PERMISSION", true)
        }
        startActivity(intent)
    }

    @KoinApiExtension
    private fun endWindowManager(action: Boolean? = null) {
        myWindowManager.updateViewUseAccessibility(doing, action)
        doing = DoingAction.None
    }



    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event != null) {
            if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event.eventType
            ) {
                var nodeInfo: AccessibilityNodeInfo? = null
                try {
                    nodeInfo = event.source ?: return
                } catch (e: NullPointerException) {
                    return
                }
                Handler().postDelayed({
                    doing = DoingAction.None
                }, 1000)
                when (doing) {
                    DoingAction.FirstInit -> {
                        BlueLightFilterUtils.checkEnable(this, nodeInfo).let {
                            if (it == 1) {
                                SingleSettingStage.getInstance().isEnableBlueFilter = true
                            }
                        }
                        AutoRotateUtil().checkEnable(this).let {
                            if (it == 1) {
                                SingleSettingStage.getInstance().isEnableAutoRotate = true
                            }
                        }
                        DarkThemeUtil().checkEnable(this).let {
                            if (it == 1) {
                                SingleSettingStage.getInstance().isEnableDarkTheme = true
                            }
                        }
                        HotspotUtil().checkEnable(this).let {
                            if (it == 1) {
                                SingleSettingStage.getInstance().isEnableHotspot = true
                            }
                        }
                        DataSaveUtil().checkEnable(this).let {
                            if (it == 1) {
                                SingleSettingStage.getInstance().isEnableDataSaver = true
                            }
                        }
                        ScreenTransmissionUtil().checkEnable(this, nodeInfo).let {
                            SingleSettingStage.getInstance().isEnableHotspot = it
                        }
                        NfcUtil().checkEnable(this).let {
                            SingleSettingStage.getInstance().isEnableNFC = it
                        }
                        BatterySaveUtil().checkEnable(this).let {
                            SingleSettingStage.getInstance().isEnableBatterySaver = it
                        }
                        AirPlaneUtil().checkEnable(this).let {
                            SingleSettingStage.getInstance().isEnableAirPlane = it
                        }
                        myWindowManager.updateViewUseAccessibility(doing)
                    }
                    DoingAction.BlueFilter -> {
                        val isEnable: Int = BlueLightFilterUtils.setEnable(this, nodeInfo)
                        if (isEnable != 0) {
                            SingleSettingStage.getInstance().isEnableBlueFilter = isEnable == 1
                            endWindowManager()
                        } else {
                            showDialogSupport(
                                doing,
                                resources.getString(R.string.action_night_light)
                            )
                        }
                    }
                    DoingAction.AirPlane -> {
                        val isEnable: Int = AirPlaneUtil().setEnable(this, nodeInfo)
                        if (isEnable != 0) {
                            SingleSettingStage.getInstance().isEnableAirPlane = isEnable == 1
                            endWindowManager()
                        } else {
                            showDialogSupport(doing)
                        }
                    }
                    DoingAction.AutoRotate -> {
                        val isEnable: Int = AutoRotateUtil().setEnable(this, nodeInfo)
                        if (isEnable != 0) {
                            SingleSettingStage.getInstance().isEnableAutoRotate = isEnable == 1
                            endWindowManager()
                        } else {
                            showDialogSupport(doing)
                        }
                    }
                    DoingAction.DarkTheme -> {
                        val isEnable: Int = DarkThemeUtil().setEnable(this, nodeInfo)
                        if (isEnable != 0) {
                            SingleSettingStage.getInstance().isEnableDarkTheme = isEnable == 1
                            endWindowManager()
                        } else {
                            showDialogSupport(
                                doing,
                                resources.getString(R.string.action_dark_theme)
                            )
                        }
                    }
                    DoingAction.Hotspot -> {
                        val isEnable: Int = HotspotUtil().setEnable(this, nodeInfo)
                        if (isEnable != 0) {
                            SingleSettingStage.getInstance().isEnableHotspot = isEnable == 1
                            endWindowManager()
                        } else {
                            showDialogSupport(doing)
                        }
                    }
                    DoingAction.ScreenTransmission -> {
                        val isEnable: Int = ScreenTransmissionUtil().setEnable(this, nodeInfo)
                        if (isEnable != 0) {
                            SingleSettingStage.getInstance().isEnableScreenTransmission =
                                isEnable == 1
                            endWindowManager(isEnable == 1)
                        } else {
                            showDialogSupport(doing)
                        }
                    }
                    DoingAction.NFC -> {
                        val isEnable: Int = NfcUtil().setEnable(this, nodeInfo)
                        if (isEnable != 0) {
                            SingleSettingStage.getInstance().isEnableNFC = isEnable == 1
                            endWindowManager(isEnable == 1)
                        } else {
                            showDialogSupport(doing)
                        }
                    }
                    DoingAction.Location -> {
                        val isEnable: Int = LocationUtil().setEnable(this, nodeInfo)
                        if (isEnable != 0) {
                            SingleSettingStage.getInstance().isEnableLocation = isEnable == 1
                            endWindowManager()
                        } else {
                            showDialogSupport(doing)
                        }
                    }
                    DoingAction.BatterySaver -> {
                        val isEnable: Int = BatterySaveUtil().setEnable(this, nodeInfo)
                        if (isEnable != 0) {
                            SingleSettingStage.getInstance().isEnableBatterySaver = isEnable == 1
                            endWindowManager(isEnable == 1)
                        } else {
                            showDialogSupport(doing)
                        }
                    }
                    DoingAction.DataSaver -> {
                        val isEnable: Int = DataSaveUtil().setEnable(this, nodeInfo)
                        if (isEnable != 0) {
                            SingleSettingStage.getInstance().isEnableDataSaver = isEnable == 1
                            endWindowManager()
                        } else {
                            showDialogSupport(doing)
                        }
                    }
                    DoingAction.Wifi -> {
                        val isEnable: Int = WifiUtil().setEnable(this, nodeInfo)
                        if (isEnable != 0) {
                            SingleSettingStage.getInstance().isWifi = isEnable == 1
                            endWindowManager(isEnable == 1)
                        } else {
                            showDialogSupport(doing)
                        }
                    }
                    DoingAction.MobileData -> {
                        val isEnable: Int = MobileDataUtil().setEnable(this, nodeInfo)
                        if (isEnable != 0) {
                            SingleSettingStage.getInstance().isMobileData = isEnable == 1
                            endWindowManager(isEnable == 1)
                        } else {
                            showDialogSupport(doing)
                        }
                    }
                }

            }
        }
        //
        checkFistDayOFMonth()
        checkFistHourOfDay()
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        SingleSettingStage.getInstance().cleanAll()
        PreferencesUtils.putInteger(resources.getString(R.string.count), count)
        isRunning = false
        val intent = Intent("ACTION_ACCESSIBILITY_SERVICE")
        intent.putExtra("isRunning", isRunning)
        sendBroadcast(intent)
        try {
            unRegister()
        } catch (ex: Exception) {

        }
        instance = null
        PreferencesUtils.putBoolean(
            resources.getString(R.string.MEDIA_PROJECTION_ENABLE),
            false
        )
    }

    private fun unRegister() {
        unregisterReceiver(myWindowManager.broadCastWifi)
        unregisterReceiver(myWindowManager.binding.layoutTaskBar.broadCastSimChange)
        unregisterReceiver(myWindowManager.binding.layoutTaskBar.broadCastChangeBattery)
        unregisterReceiver(myWindowManager.binding.layoutBtnSecondsLine.broadCastBluetooth)
        unregisterReceiver(myWindowManager.binding.layoutBtnFist.broadCastMobileData)
        unregisterReceiver(myWindowManager.binding.layoutIconControls.broadCastBatterySaver)
        unregisterReceiver(myWindowManager.binding.layoutIconControls.broadCastAirplane)
        unregisterReceiver(myWindowManager.binding.layoutIconControls.broadCastDoNotDisturb)
        unregisterReceiver(myWindowManager.binding.layoutTaskBar.broadCastChangeRingerMode)
        unregisterReceiver(myWindowManager.binding.layoutControlsSecondsLine.broadCastHotspot)
        unregisterReceiver(broadCastAccessibility)
        unregisterReceiver(myWindowManager.broadCastBlurImage)
        unregisterReceiver(myWindowManager.broadCastBackgroundColor)
        unregisterReceiver(myWindowManager.broadCastTextColor)
        unregisterReceiver(myWindowManager.broadCastUsageData)
        unregisterReceiver(myWindowManager.broadCastScreenShot)
        unregisterReceiver(myWindowManager.broadCastListenerNotification)
        unregisterReceiver(myWindowManager.broadCastTimeChange)
    }

    private fun checkFistDayOFMonth() {
        val month: Int = calendar.get(Calendar.MONTH)
        val year: Int = calendar.get(Calendar.YEAR)
        if (checkUsageAccess() && PreferencesUtils.getBoolean(
                this.resources.getString(R.string.state_usage_data),
                false
            )
        ) {
            if (PreferencesUtils.getInteger(
                    resources.getString(R.string.use_date_set_up),
                    1
                ) != 1
            ) {
                if (calendar.get(Calendar.DAY_OF_MONTH) == PreferencesUtils.getInteger(
                        resources.getString(
                            R.string.use_date_set_up
                        )
                    ) && count == 0
                ) {
                    count++
                    PreferencesUtils.putLong(TOTAL_DATA_ON, totalData())
                    PreferencesUtils.putLong(
                        resources.getString(R.string.total_usage_data_of_day),
                        totalData()
                    )
                } else if (calendar.get(Calendar.DAY_OF_MONTH) != PreferencesUtils.getInteger(
                        resources.getString(
                            R.string.use_date_set_up
                        )
                    )
                ) {
                    count = 0
                }
            } else {
                if (month != PreferencesUtils.getInteger(
                        resources.getString(R.string.month),
                        13
                    )
                ) {
                    PreferencesUtils.putLong(TOTAL_DATA_ON, totalData())
                    PreferencesUtils.putLong(
                        resources.getString(R.string.total_usage_data_of_day),
                        totalData()
                    )
                } else if (year != PreferencesUtils.getInteger(
                        resources.getString(R.string.year),
                        0
                    )
                ) {
                    PreferencesUtils.putLong(TOTAL_DATA_ON, totalData())
                    PreferencesUtils.putLong(
                        resources.getString(R.string.total_usage_data_of_day),
                        totalData()
                    )
                }
            }
        }
        PreferencesUtils.putInteger(resources.getString(R.string.month), month)
        PreferencesUtils.putInteger(resources.getString(R.string.year), year)
    }

    private fun checkFistHourOfDay() {
        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
        if (checkUsageAccess() && PreferencesUtils.getBoolean(
                this.resources.getString(R.string.stata_usage_day),
                false
            )
        ) {
            if (day != PreferencesUtils.getInteger(resources.getString(R.string.day), 0)) {
                PreferencesUtils.putLong(
                    resources.getString(R.string.total_usage_data_of_day),
                    totalData()
                )
            }
        }
        PreferencesUtils.putInteger(resources.getString(R.string.day), day)
    }

    private fun checkUsageAccess(): Boolean {
        return try {
            val packageManager = applicationContext.packageManager
            val applicationInfo =
                packageManager.getApplicationInfo(applicationContext.packageName, 0)
            val appOpsManager =
                applicationContext.getSystemService(APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo.uid,
                applicationInfo.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun totalData(): Long {
        val received = TrafficStats.getTotalRxBytes() / (1024 * 1024)
        val send = TrafficStats.getTotalTxBytes() / (1024 * 1024)
        return received + send
    }

    private fun startOldActivity(context: Context) {
        try {
            val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val tasks = activityManager.appTasks
            for (appTask: ActivityManager.AppTask? in tasks) {
                if (appTask != null && (Build.VERSION.SDK_INT < 23 || appTask.taskInfo.topActivity!!.packageName != context.packageName)) {
                    val intent = appTask.taskInfo.baseIntent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                                or Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                    context.startActivity(intent)
                    return
                }
            }
        } catch (unused: Throwable) {
        }
    }

    private fun buildNotification() {
        val notification = NotificationCompat.Builder(this, "No")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        startForeground(1, notification)
    }

    @SuppressLint("WrongConstant")
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("No", "Music", importance)
            mChannel.setSound(null, null)
            mChannel.description = "No"
            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    private fun startForegroundService() {
        notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createChannel()
        buildNotification()
    }

    private fun showDialogSupport(doingAction: DoingAction, action: String? = null) {
        val alertDialog = Dialog(this)
        val viewDialog = ViewDialogOpenSettings(this, null)
        var title = ""
        var description = ""
        var icon = 0
        when (doingAction) {
            DoingAction.DarkTheme -> {
                title = getString(R.string.set_up_dark_theme)
                description = getString(R.string.describe_dark_theme)
                icon = R.drawable.ic_dark_theme
            }
            DoingAction.AutoRotate -> {
                title = getString(R.string.set_up_auto_rotate)
                description = getString(R.string.describe_auto_rotate)
                icon = R.drawable.ic_rotation_lock
            }
            DoingAction.BlueFilter -> {
                title = getString(R.string.set_up_night_light)
                description = getString(R.string.describe_night_light)
                icon = R.drawable.ic_night_light
            }
            DoingAction.Hotspot -> {
                title = getString(R.string.set_up_hotspot)
                description = getString(R.string.describe_hotspot)
                icon = R.drawable.ic_hotspot
            }
            DoingAction.DataSaver -> {
                title = getString(R.string.set_up_data_saver)
                description = getString(R.string.describe_data_saver)
                icon = R.drawable.ic_data_saver_on
            }
            DoingAction.ScreenTransmission -> {
                title = getString(R.string.set_up_screen_transmission)
                description = getString(R.string.describe_screen_transmission)
                icon = R.drawable.ic_screen_transmission
            }
            DoingAction.NFC -> {
                title = getString(R.string.set_up_nfc)
                description = getString(R.string.describe_nfc)
                icon = R.drawable.ic_nfc
            }
            DoingAction.Location -> {
                title = getString(R.string.set_up_location)
                description = getString(R.string.describe_location)
                icon = R.drawable.ic_location
            }
            DoingAction.BatterySaver -> {
                title = getString(R.string.set_up_battery_saver)
                description = getString(R.string.describe_battery_saver)
                icon = R.drawable.ic_battery_saver
            }
            DoingAction.AirPlane -> {
                title = getString(R.string.set_up_airplane)
                description = getString(R.string.describe_airplane)
                icon = R.drawable.ic_airplane
            }
            DoingAction.Wifi -> {
                title = getString(R.string.set_up_wifi)
                description = getString(R.string.describe_wifi)
                icon = R.drawable.ic_wifi_full
            }
            DoingAction.MobileData -> {
                title = getString(R.string.set_up_data_mobile)
                description = getString(R.string.describe_data_mobile)
                icon = R.drawable.ic_mobile_data
            }
        }
        viewDialog.binding.txtSetUpName.text = title
        viewDialog.binding.txtContentDialog.text = description
        viewDialog.binding.icControl.setImageResource(icon)
        viewDialog.binding.btnOpenSettings.setOnClickListener {
            alertDialog.dismiss()
            when (doing) {
                DoingAction.NFC -> {
                    val intent = if (Build.VERSION.SDK_INT >= 29) {
                        Intent("android.settings.panel.action.NFC")
                    } else {
                        Intent("android.settings.NFC_SETTINGS")
                    }
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                DoingAction.ScreenTransmission -> {
                    val intent = Intent("android.settings.CAST_SETTINGS")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                DoingAction.AirPlane -> {
                    val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                else -> {
                    try {
                        val statusBarService = getSystemService("statusbar")
                        val statusBarManager: Class<*> =
                            Class.forName("android.app.StatusBarManager")
                        val show: Method = statusBarManager.getMethod("expandSettingsPanel")
                        show.invoke(statusBarService)
                    } catch (_e: ClassNotFoundException) {
                        _e.printStackTrace()
                    } catch (_e: NoSuchMethodException) {
                        _e.printStackTrace()
                    } catch (_e: IllegalArgumentException) {
                        _e.printStackTrace()
                    } catch (_e: IllegalAccessException) {
                        _e.printStackTrace()
                    } catch (_e: InvocationTargetException) {
                        _e.printStackTrace()
                    }
                }
            }
            myWindowManager.endWindowManager()
        }
        viewDialog.binding.openHelper.setOnClickListener {
            alertDialog.dismiss()
            val intent = Intent(this, SystemShadeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            myWindowManager.endWindowManager()
        }
        alertDialog.setCancelable(true)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setContentView(viewDialog.binding.root)
        if (Build.VERSION.SDK_INT >= 22)
            alertDialog.window!!.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        else
            alertDialog.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        alertDialog.show()
        alertDialog.setOnDismissListener {
            action?.let {
                val intent = Intent(it)
                Handler().postDelayed({
                    sendBroadcast(intent)
                }, 200)
            }
        }
        doing = DoingAction.None
    }
}