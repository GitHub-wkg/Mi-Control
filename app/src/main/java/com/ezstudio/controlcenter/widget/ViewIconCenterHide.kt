package com.ezstudio.controlcenter.widget

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.nfc.NfcAdapter
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutIconCenterHideBinding
import com.ezstudio.controlcenter.service.MyAccessibilityService
import com.ezstudio.controlcenter.service.SingleSettingStage
import com.ezteam.baseproject.utils.PreferencesUtils


@SuppressLint("ClickableViewAccessibility")
class ViewIconCenterHide(context: Context, attrs: AttributeSet?) : BaseViewChild(context, attrs) {
    lateinit var binding: LayoutIconCenterHideBinding

    init {
        initView()
        initDataSaver()
        initScreenTransmission()
        initNFC()
        initLocation()
    }

    private fun initView() {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_icon_center_hide, this, true)
        binding = LayoutIconCenterHideBinding.bind(view)
        binding.txtDataSaver.isSelected = true
        binding.txtScreenTransmission.isSelected = true
        binding.txtNfc.isSelected = true
        binding.txtLocation.isSelected = true
        alertDialog = Dialog(context)
    }

    private fun initDataSaver() {
        setStageDataSaver()
        binding.layoutDataSaver.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    longClickCountTimer(event) {
                        if (Build.VERSION.SDK_INT >= 23) {
                            val intent = Intent("android.settings.DATA_USAGE_SETTINGS")
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        } else {
                            showExplanationError(
                                context.resources.getString(R.string.control_center_notification),
                                context.getString(R.string.device_does_not_support_data_saver)
                            )
                        }
                        endLayoutManager?.invoke()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    actionUpItemView(event) {
                        v.startAnimation(animClickIcon)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            autoShowMaskBackgroundBlack()
                            MyAccessibilityService.instance?.actionAutoClick(MyAccessibilityService.DoingAction.DataSaver)
                        } else {
                            setStageDataSaver(true)
                        }
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    actionMoveView(event)
                }
            }
            true
        }
        //
        alertDialog.setOnCancelListener {
            setStageDataSaver()
        }
    }

    fun setStageDataSaver(showMessageNotSupport: Boolean = false) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when (connectivityManager.restrictBackgroundStatus) {
                RESTRICT_BACKGROUND_STATUS_ENABLED -> {
                    binding.backgroundIcDataSaver.setColorFilter(
                        Color.parseColor(PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")),
                        PorterDuff.Mode.SRC_IN
                    )
                    binding.icDataSaver.setImageResource(R.drawable.ic_data_saver_on)
                }
                RESTRICT_BACKGROUND_STATUS_WHITELISTED, RESTRICT_BACKGROUND_STATUS_DISABLED -> {
                    binding.backgroundIcDataSaver.setColorFilter(
                        Color.parseColor(
                            PreferencesUtils.getString(
                                BACKGROUND_COLOR,
                                context.resources.getString(R.string.color_4DFFFFFF)
                            )
                        ),
                        PorterDuff.Mode.SRC_IN
                    )
                    binding.icDataSaver.setImageResource(R.drawable.ic_data_saver)
                }
            }
        } else {
            binding.backgroundIcDataSaver.setColorFilter(
                Color.parseColor(
                    PreferencesUtils.getString(
                        BACKGROUND_COLOR,
                        context.resources.getString(R.string.color_4DFFFFFF)
                    )
                ),
                PorterDuff.Mode.SRC_IN
            )
            if (showMessageNotSupport) {
                showExplanationError(
                    context.resources.getString(R.string.control_center_notification),
                    context.resources.getString(R.string.device_does_not_support)
                )
                endLayoutManager?.invoke()
            }
        }
    }

    private fun initScreenTransmission() {
        setStageScreenTransmission()
        binding.icScreenTransmission.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    longClickCountTimer(event) {
                        val intent = Intent("android.settings.CAST_SETTINGS")
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        endLayoutManager?.invoke()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    actionUpItemView(event) {
                        v.startAnimation(animClickIcon)
                        autoShowMaskBackgroundBlack()
                        MyAccessibilityService.instance?.actionAutoClick(MyAccessibilityService.DoingAction.DataSaver)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    actionMoveView(event)
                }
            }
            true
        }
    }

    fun setStageScreenTransmission() {
        if (SingleSettingStage.getInstance().isEnableScreenTransmission) {
            binding.backgroundIcScreenTransmission.setColorFilter(
                Color.parseColor(PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            binding.backgroundIcScreenTransmission.setColorFilter(
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

    private fun initNFC() {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        //
        binding.layoutIconNfc.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    longClickCountTimer(event) {
                        val intent = Intent()
                        if (Build.VERSION.SDK_INT >= 29) {
                            intent.action = "android.settings.panel.action.NFC"
                        } else {
                            intent.action = "android.settings.NFC_SETTINGS"
                        }
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        endLayoutManager?.invoke()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    actionUpItemView(event) {
                        v.startAnimation(animClickIcon)
                        if (nfcAdapter != null) {
                            autoShowMaskBackgroundBlack()
                            MyAccessibilityService.instance?.actionAutoClick(MyAccessibilityService.DoingAction.NFC)
                        } else {
                            showExplanationError(
                                context.resources.getString(R.string.control_center_notification),
                                context.resources.getString(R.string.device_does_not_support)
                            )
                            endLayoutManager?.invoke()
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

    fun setStageNFC(action: Boolean? = null) {
        action?.let {
            if (it) {
                binding.backgroundIcNfc.setColorFilter(
                    Color.parseColor(PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")),
                    PorterDuff.Mode.SRC_IN
                )
            } else {
                binding.backgroundIcNfc.setColorFilter(
                    Color.parseColor(
                        PreferencesUtils.getString(
                            BACKGROUND_COLOR,
                            context.resources.getString(R.string.color_4DFFFFFF)
                        )
                    ),
                    PorterDuff.Mode.SRC_IN
                )

            }
        } ?: run {
            val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
            if (nfcAdapter != null) {
                if (nfcAdapter.isEnabled) {
                    binding.backgroundIcNfc.setColorFilter(
                        Color.parseColor(PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")),
                        PorterDuff.Mode.SRC_IN
                    )
                } else {
                    binding.backgroundIcNfc.setColorFilter(
                        Color.parseColor(
                            PreferencesUtils.getString(
                                BACKGROUND_COLOR,
                                context.resources.getString(R.string.color_4DFFFFFF)
                            )
                        ),
                        PorterDuff.Mode.SRC_IN
                    )

                }
            } else {
                binding.backgroundIcNfc.setColorFilter(
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

    private fun initLocation() {
        setStageLocation()
        binding.layoutIconLocation.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    longClickCountTimer(event) {
                        val intent = Intent("android.settings.LOCATION_SOURCE_SETTINGS")
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        endLayoutManager?.invoke()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    actionUpItemView(event) {
                        v.startAnimation(animClickIcon)
                        autoShowMaskBackgroundBlack()
                        MyAccessibilityService.instance?.actionAutoClick(MyAccessibilityService.DoingAction.Location)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    actionMoveView(event)
                }
            }
            true
        }
    }

    fun setStageLocation() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (locationManager.isLocationEnabled) {
                binding.backgroundIcLocation.setColorFilter(
                    Color.parseColor(PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")),
                    PorterDuff.Mode.SRC_IN
                )
            } else {
                binding.backgroundIcLocation.setColorFilter(
                    Color.parseColor(
                        PreferencesUtils.getString(
                            BACKGROUND_COLOR,
                            context.resources.getString(R.string.color_4DFFFFFF)
                        )
                    ),
                    PorterDuff.Mode.SRC_IN
                )
            }
        } else {
            val mode = Settings.Secure.getInt(
                context.contentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            if (mode != Settings.Secure.LOCATION_MODE_OFF) {
                binding.backgroundIcLocation.setColorFilter(
                    Color.parseColor(PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")),
                    PorterDuff.Mode.SRC_IN
                )
            } else {
                binding.backgroundIcLocation.setColorFilter(
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

}