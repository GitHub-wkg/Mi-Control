package com.ezstudio.controlcenter.widget

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_AIRPLANE_MODE_CHANGED
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.ACTION_SOUND_SETTINGS
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.broadcast.BroadCastAirplane
import com.ezstudio.controlcenter.broadcast.BroadCastBatterySaver
import com.ezstudio.controlcenter.broadcast.BroadCastDoNotDisturb
import com.ezstudio.controlcenter.databinding.LayoutIconControlBinding
import com.ezstudio.controlcenter.databinding.WindownManagerBinding
import com.ezstudio.controlcenter.service.MyAccessibilityService
import com.ezteam.baseproject.utils.PreferencesUtils

@SuppressLint("ClickableViewAccessibility")
class ViewIconControl(context: Context, attrs: AttributeSet?) : BaseViewChild(context, attrs) {
    lateinit var binding: LayoutIconControlBinding
    var layoutManager: WindownManagerBinding? = null
    var listenerEndLayout: (() -> Unit)? = null
    var listenerChangeAirplane: ((Boolean) -> Unit)? = null
    lateinit var broadCastBatterySaver: BroadCastBatterySaver
    lateinit var broadCastAirplane: BroadCastAirplane
    private lateinit var notificationManager: NotificationManager
    lateinit var broadCastDoNotDisturb: BroadCastDoNotDisturb

    init {
        initView()
        stateSound()
        initBatterySaver()
        initAirplaneMode()
        initDoNotDisturb()
    }

    private fun initView() {
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val view = LayoutInflater.from(context).inflate(R.layout.layout_icon_control, this, true)
        binding = LayoutIconControlBinding.bind(view)
        binding.txtAirplaneMode.isSelected = true
        binding.txtMute.isSelected = true
        binding.txtBatterySaver.isSelected = true
        binding.txtDoNotDisturb.isSelected = true
    }

    private fun stateSound() {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (am.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> binding.icMute.setImageResource(R.drawable.ic_mute)
            AudioManager.RINGER_MODE_VIBRATE -> binding.icMute.setImageResource(
                R.drawable.ic_vibrate
            )
            AudioManager.RINGER_MODE_NORMAL -> binding.icMute.setImageResource(R.drawable.ic_ring)
        }

        //
        binding.layoutSound.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    longClickCountTimer(event) {
                        val intent = Intent(ACTION_SOUND_SETTINGS)
                        intent.flags = FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        listenerEndLayout?.invoke()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    actionUpItemView(event) {
                        v.startAnimation(animClickIcon)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                            && !notificationManager.isNotificationPolicyAccessGranted
                        ) {
                            showExplanationWriteSetting(
                                context.getString(R.string.ACCESS_NOTIFICATION),
                                context.getString(R.string.DESCRIBE_ACCESS_NOTIFICATION_POLICY)
                            )
                            listenerEndLayout?.invoke()
                        } else {
                            when (am.ringerMode) {
                                AudioManager.RINGER_MODE_SILENT -> {
                                    am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                                }
                                AudioManager.RINGER_MODE_VIBRATE -> {
                                    am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                                }
                                AudioManager.RINGER_MODE_NORMAL -> {
                                    am.ringerMode = AudioManager.RINGER_MODE_SILENT
                                }
                            }
                            setStageSound()
                        }
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    actionMoveView(event)
                }
            }
            true
        }

    }

    fun setStageSound() {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (am.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> binding.backgroundIcSound.setColorFilter(
                Color.parseColor(
                    PreferencesUtils.getString(
                        BACKGROUND_COLOR,
                        context.resources.getString(R.string.color_4DFFFFFF)
                    )
                ),
                PorterDuff.Mode.SRC_IN
            )
            AudioManager.RINGER_MODE_VIBRATE
            -> binding.backgroundIcSound.setColorFilter(
                Color.parseColor(PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")),
                PorterDuff.Mode.SRC_IN
            )
            AudioManager.RINGER_MODE_NORMAL -> binding.backgroundIcSound.setColorFilter(
                Color.parseColor(PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun initBatterySaver() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        setStageBatterySaver()
        //
        broadCastBatterySaver = BroadCastBatterySaver()
        broadCastBatterySaver.binding = binding
        layoutManager?.let { broadCastBatterySaver.layoutManager = it }
        broadCastBatterySaver.powerManager = powerManager
        val intent = IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        context.registerReceiver(broadCastBatterySaver, intent)

        binding.layoutBatterySaver.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    longClickCountTimer(event) {
                        when {
                            "samsung".equals(Build.MANUFACTURER, true) -> {
                                val intent = Intent()
                                intent.flags = FLAG_ACTIVITY_NEW_TASK
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                    intent.action = Settings.ACTION_BATTERY_SAVER_SETTINGS
                                } else {
                                    intent.action = Settings.ACTION_SETTINGS
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (ex: ActivityNotFoundException) {
                                    val intents = Intent(Settings.ACTION_SETTINGS)
                                    intents.flags = FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(intent, null)
                                }
                            }
                            else -> {
                                expandSettingsPanel()
                            }
                        }
                        listenerEndLayout?.invoke()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    actionUpItemView(event) {
                        v.startAnimation(animClickIcon)
                        autoShowMaskBackgroundBlack()
                        MyAccessibilityService.instance?.actionAutoClick(MyAccessibilityService.DoingAction.BatterySaver)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    actionMoveView(event)
                }
            }
            true
        }


    }

    fun setStageBatterySaver() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (powerManager.isPowerSaveMode) {
            binding.backgroundBatterySaver.setColorFilter(
                Color.parseColor(PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")),
                PorterDuff.Mode.SRC_IN
            )
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val batteryPct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            setPowerBattery(batteryPct)
        } else {
            binding.backgroundBatterySaver.setColorFilter(
                Color.parseColor(
                    PreferencesUtils.getString(
                        BACKGROUND_COLOR,
                        context.resources.getString(R.string.color_4DFFFFFF)
                    )
                ),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun initAirplaneMode() {
        setStageAirPlane()
        ////
        broadCastAirplane = BroadCastAirplane()
        broadCastAirplane.binding = binding
        broadCastAirplane.listenerChangeStatusAirplane = {
            listenerChangeAirplane?.invoke(it)
        }
        val intent = IntentFilter(ACTION_AIRPLANE_MODE_CHANGED)
        context.registerReceiver(broadCastAirplane, intent)
        //

        binding.layoutAirPlane.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    longClickCountTimer(event) {
                        val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
                        intent.flags = FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        listenerEndLayout?.invoke()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    actionUpItemView(event) {
                        v.startAnimation(animClickIcon)
                        autoShowMaskBackgroundBlack()
                        MyAccessibilityService.instance?.actionAutoClick(MyAccessibilityService.DoingAction.AirPlane)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    actionMoveView(event)
                }
            }
            true
        }
    }

    fun setStageAirPlane() {
        val isEnabled = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
        if (isEnabled) {
            binding.backgroundAirPlane.setColorFilter(
                Color.parseColor(PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            binding.backgroundAirPlane.setColorFilter(
                Color.parseColor(
                    PreferencesUtils.getString(
                        BACKGROUND_COLOR,
                        context.resources.getString(R.string.color_4DFFFFFF)
                    )
                ),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun initDoNotDisturb() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setStageDoNotDisturb()
            //
            broadCastDoNotDisturb = BroadCastDoNotDisturb()
            broadCastDoNotDisturb.binding = binding
            val intent = IntentFilter(ACTION_INTERRUPTION_FILTER_CHANGED)
            context.registerReceiver(broadCastDoNotDisturb, intent)
        } else {
            binding.backgroundDoNotDisturb.setColorFilter(
                Color.parseColor(
                    PreferencesUtils.getString(
                        BACKGROUND_COLOR,
                        context.resources.getString(R.string.color_4DFFFFFF)
                    )
                ),
                PorterDuff.Mode.SRC_IN
            )
        }

        binding.layoutDoNotDisturb.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    longClickCountTimer(event) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val intent = Intent("android.settings.ZEN_MODE_SETTINGS")
                            intent.flags = FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                            listenerEndLayout?.invoke()
                        } else {
                            showExplanationError(
                                context.resources.getString(R.string.control_center_notification),
                                context.resources.getString(R.string.device_does_not_support)
                            )
                            listenerEndLayout?.invoke()
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    actionUpItemView(event) {
                        v.startAnimation(animClickIcon)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!notificationManager.isNotificationPolicyAccessGranted) {
                                showExplanationWriteSetting(
                                    context.getString(R.string.ACCESS_NOTIFICATION_POLICY),
                                    context.getString(R.string.DESCRIBE_ACCESS_NOTIFICATION_POLICY)
                                )
                                listenerEndLayout?.invoke()
                            } else {
                                if (notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
                                    notificationManager.setInterruptionFilter(
                                        NotificationManager.INTERRUPTION_FILTER_NONE
                                    )
                                } else {
                                    notificationManager.setInterruptionFilter(
                                        NotificationManager.INTERRUPTION_FILTER_ALL
                                    )
                                }
                                startAnimVector(binding.icDoNotDisturb)
                                setStageDoNotDisturb()
                            }
                        } else {
                            showExplanationError(
                                context.resources.getString(R.string.control_center_notification),
                                context.resources.getString(R.string.device_does_not_support)
                            )
                            listenerEndLayout?.invoke()
                        }
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    actionMoveView(event)
                }
            }
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun setStageDoNotDisturb() {
        if (notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
            binding.backgroundDoNotDisturb.setColorFilter(
                Color.parseColor(
                    PreferencesUtils.getString(
                        BACKGROUND_COLOR,
                        context.resources.getString(R.string.color_4DFFFFFF)
                    )
                ),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            binding.backgroundDoNotDisturb.setColorFilter(
                Color.parseColor(PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun setPowerBattery(batteryPct: Int) {
        if (layoutManager != null) {
            when (batteryPct) {
                in 0..20 -> {
                    layoutManager!!.layoutTaskBar.binding.icBattery.setImageResource(
                        R.drawable.ic_battery_saver_20
                    )
                }
                in 20..40 -> {
                    layoutManager!!.layoutTaskBar.binding.icBattery.setImageResource(
                        R.drawable.ic_battery_saver_40
                    )
                }
                in 40..60 -> {
                    layoutManager!!.layoutTaskBar.binding.icBattery.setImageResource(
                        R.drawable.ic_battery_saver_60
                    )
                }
                in 60..80 -> {
                    layoutManager!!.layoutTaskBar.binding.icBattery.setImageResource(
                        R.drawable.ic_battery_saver_80
                    )
                }
                else -> {
                    layoutManager!!.layoutTaskBar.binding.icBattery.setImageResource(
                        R.drawable.ic_battery_saver_full
                    )
                }
            }
        }
    }
}