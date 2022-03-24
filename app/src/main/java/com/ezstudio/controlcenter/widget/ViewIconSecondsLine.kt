package com.ezstudio.controlcenter.widget

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.UiModeManager
import android.app.UiModeManager.*
import android.content.*
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.wifi.WifiManager
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.provider.Settings
import android.provider.Settings.ACTION_DISPLAY_SETTINGS
import android.telephony.TelephonyManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.activity.BackGroundActivity
import com.ezstudio.controlcenter.broadcast.BroadCastHotspot
import com.ezstudio.controlcenter.databinding.LayoutIconSecondsLineBinding
import com.ezstudio.controlcenter.service.MyAccessibilityService
import com.ezstudio.controlcenter.service.SingleSettingStage
import com.ezstudio.controlcenter.windown_manager.MyWindowManager
import com.ezteam.baseproject.utils.PreferencesUtils
import kotlin.math.abs
import kotlin.math.max

@SuppressLint("ClickableViewAccessibility")
class ViewIconSecondsLine(context: Context, attrs: AttributeSet?) :
    BaseViewChild(context, attrs) {
    lateinit var binding: LayoutIconSecondsLineBinding
    lateinit var broadCastHotspot: BroadCastHotspot

    init {
        initView()
        initAutoRotation()
        initNightLight()
        initDarkTheme()
        initHotspot()
    }

    private fun initView() {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_icon_seconds_line, this, true)
        binding = LayoutIconSecondsLineBinding.bind(view)
        binding.txtAutoRotate.isSelected = true
        binding.txtNightLight.isSelected = true
        binding.txtDarkTheme.isSelected = true
        binding.txtHotspot.isSelected = true
        alertDialog = Dialog(context)
    }

    private fun initAutoRotation() {
        setStageAutoRotation()
        binding.layoutAutoRotate.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    longClickCountTimer(event) {
                        val intent = Intent(ACTION_DISPLAY_SETTINGS)
                        intent.flags = FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        endLayoutManager?.invoke()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    actionUpItemView(event) {
                        v.startAnimation(animClickIcon)
                        if (Build.VERSION.SDK_INT >= 23) {
                            if (Settings.System.canWrite(context)) {
                                onClickAutoRotate()
                            } else {
                                showExplanation(
                                    "",
                                    context.getString(R.string.DESCRIBE_REQUEST_WRITE_SETTING)
                                )
                            }
                        } else {
                            autoShowMaskBackgroundBlack()
                            MyAccessibilityService.instance?.actionAutoClick(
                                MyAccessibilityService.DoingAction.AutoRotate
                            )
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
    private fun onClickAutoRotate() {
        if (Settings.System.canWrite(context)) {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                max(
                    0, abs(
                        Settings.System.getInt(
                            context.contentResolver,
                            Settings.System.ACCELEROMETER_ROTATION,
                            0
                        ) - 1
                    )
                )
            )
            setStageAutoRotation()
        } else {
            showExplanation(
                "",
                context.getString(R.string.DESCRIBE_REQUEST_WRITE_SETTING)
            )
        }
    }

    fun setStageAutoRotation() {
        if (Settings.System.getInt(
                context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION, 0
            ) == 1
        ) {
            binding.icAutoRotate.setImageResource(R.drawable.ic_cc_qs_rotation_lock_on)
            startAnimVector(binding.icAutoRotate)
            binding.backgroundAutoRotate.setColorFilter(
                Color.parseColor(PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            binding.icAutoRotate.setImageResource(R.drawable.ic_cc_qs_rotation_lock_off)
            startAnimVector(binding.icAutoRotate)
            binding.backgroundAutoRotate.setColorFilter(
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

    private fun initNightLight() {
        setStageViewNightLight()
        //
        binding.icNightLight.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    longClickCountTimer(event) {
                        val intent = Intent(Intent.ACTION_MAIN, null)
                        intent.flags = FLAG_ACTIVITY_NEW_TASK
                        intent.component = ComponentName(
                            "com.android.settings",
                            "com.android.settings.Settings\$BlueLightFilterSettingsActivity"
                        )
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.device_does_not_support),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                        endLayoutManager?.invoke()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    actionUpItemView(event) {
                        v.startAnimation(animClickIcon)
                        autoShowMaskBackgroundBlack()
                        MyAccessibilityService.instance?.actionAutoClick(MyAccessibilityService.DoingAction.BlueFilter)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    actionMoveView(event)
                }
            }
            true
        }
    }


    fun setStageViewNightLight() {
        if (SingleSettingStage.getInstance().isEnableBlueFilter) {
            startAnimVector(binding.icNightLight)
            binding.backgroundNightLight.setColorFilter(
                Color.parseColor(PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            binding.backgroundNightLight.setColorFilter(
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

    private fun initDarkTheme() {
        binding.layoutIconDarkTheme.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    longClickCountTimer(event) {
                        val intent = Intent(ACTION_DISPLAY_SETTINGS)
                        intent.flags = FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        endLayoutManager?.invoke()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    actionUpItemView(event) {
                        v.startAnimation(animClickIcon)
                        autoShowMaskBackgroundBlack()
                        MyAccessibilityService.instance?.actionAutoClick(MyAccessibilityService.DoingAction.DarkTheme)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    actionMoveView(event)
                }
            }
            true
        }
    }

    fun setStageDarkTheme() {
        val uiManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        when (uiManager.nightMode) {
            MODE_NIGHT_NO -> {
                binding.backgroundDarkTheme.setColorFilter(
                    Color.parseColor(
                        PreferencesUtils.getString(
                            BACKGROUND_COLOR,
                            context.resources.getString(R.string.color_4DFFFFFF)
                        )
                    ),
                    PorterDuff.Mode.SRC_IN
                )
            }
            MODE_NIGHT_YES, MODE_NIGHT_AUTO -> {
                startAnimVector(binding.icDarkTheme)
                binding.backgroundDarkTheme.setColorFilter(
                    Color.parseColor(PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")),
                    PorterDuff.Mode.SRC_IN
                )
            }
            else -> {

            }
        }
    }

    private fun initHotspot() {
        broadCastHotspot = BroadCastHotspot()
        broadCastHotspot.binding = binding
        val intentFilter = IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED")
        context.registerReceiver(broadCastHotspot, intentFilter)
        setStageHotspost()
        //
        binding.layoutIconHotspot.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> { longClickCountTimer(event) {
                        val intent = Intent(Intent.ACTION_MAIN, null)
                        intent.addCategory(Intent.CATEGORY_LAUNCHER)
                        val cn = ComponentName(
                            "com.android.settings",
                            "com.android.settings.TetherSettings"
                        )
                        intent.component = cn
                        intent.flags = FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        endLayoutManager?.invoke()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    actionUpItemView(event) {
                        v.startAnimation(animClickIcon)
                        try {
                            val telephonyManager =
                                context.getSystemService(
                                    Context.TELEPHONY_SERVICE
                                ) as TelephonyManager
                            if (telephonyManager.simState != TelephonyManager.SIM_STATE_READY) {
                                endLayoutManager?.invoke()
                            }
                            setStageHotspost()
                        } catch (ex: Exception) {
                            autoShowMaskBackgroundBlack()
                            MyAccessibilityService.instance?.actionAutoClick(MyAccessibilityService.DoingAction.Hotspot)
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

    fun setStageHotspost() {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val method = wifiManager.javaClass.getMethod("getWifiApState")
        val tmp = method.invoke(wifiManager) as Int
        if (tmp == 13) {
            binding.backgroundHotspot.setColorFilter(
                Color.parseColor(
                    PreferencesUtils.getString(
                        SELECTED_COLOR,
                        "#2C61CC"
                    )
                ),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            binding.backgroundHotspot.setColorFilter(
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

}