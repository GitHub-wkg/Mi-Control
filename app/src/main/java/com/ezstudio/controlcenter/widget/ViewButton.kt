package com.ezstudio.controlcenter.widget

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.broadcast.BroadCastChangeWifi
import com.ezstudio.controlcenter.broadcast.BroadCastMobileData
import com.ezstudio.controlcenter.databinding.LayoutBtnBinding
import com.ezstudio.controlcenter.databinding.WindownManagerBinding
import com.ezstudio.controlcenter.service.MyAccessibilityService
import com.ezteam.baseproject.utils.PreferencesUtils

@SuppressLint("ClickableViewAccessibility")
class ViewButton(context: Context, attrs: AttributeSet?) : BaseViewChild(context, attrs) {
    lateinit var binding: LayoutBtnBinding
    lateinit var broadCastMobileData: BroadCastMobileData
    val wifiManager: WifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
    var listener: (() -> Unit)? = null
    var endWindowManager: (() -> Unit)? = null
    lateinit var broadCastWifi: BroadCastChangeWifi
    private lateinit var alertDialogDataMobile: Dialog
    lateinit var layoutWindowManager: WindownManagerBinding
    private val TOTAL_DATA_ON = "TOTAL_DATA_ON"

    init {
        initView()
        wifi()
        mobileData()
        setUpUsageDataLayout(context)
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_btn, this, true)
        binding = LayoutBtnBinding.bind(view)
        alertDialog = Dialog(context)
        alertDialogDataMobile = Dialog(context)
    }

    @SuppressLint("UseCompatLoadingForDrawables", "ClickableViewAccessibility")
    private fun wifi() {
        binding.btnWifi.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    longClickCountTimer(event) {
                        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                        intent.flags = FLAG_ACTIVITY_NEW_TASK
                        startActivity(context, intent, null)
                        endWindowManager?.invoke()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    actionUpItemView(event) {
                        startAnimVector(binding.icWifi)
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            setStageWifi()
                        } else {
                            autoShowMaskBackgroundBlack()
                            MyAccessibilityService.instance?.actionAutoClick(
                                MyAccessibilityService.DoingAction.Wifi
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
        //

    }

    fun setStageWifi(action: Boolean? = null) {
        action?.let {
            if (it) {
                setColorDrawable(
                    binding.btnWifi,
                    PreferencesUtils.getString(
                        SELECTED_COLOR, "#2C61CC"
                    )
                )
                binding.txtStatusWifi.text = context.getString(R.string.connecting)
            } else {
                binding.name.text =
                    context.getString(R.string.wi_fi)
                setColorDrawable(
                    binding.btnWifi,
                    PreferencesUtils.getString(
                        BACKGROUND_COLOR,
                        context.resources.getString(R.string.color_4DFFFFFF)
                    )
                )
                //
                binding.txtStatusWifi.text =
                    context.getString(R.string.off)
            }
        } ?: run {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val isEnabled = Settings.Global.getInt(
                    context.contentResolver,
                    Settings.Global.AIRPLANE_MODE_ON, 0
                ) != 0
                if (!isEnabled) {
                    wifiManager.isWifiEnabled =
                        !(binding.txtStatusWifi.text.toString()
                            .equals(
                                context.getString(
                                    R.string.on
                                ), true
                            ))
                    if (!wifiManager.isWifiEnabled) {
                        setColorDrawable(
                            binding.btnWifi,
                            PreferencesUtils.getString(
                                SELECTED_COLOR, "#2C61CC"
                            )
                        )
                        binding.txtStatusWifi.text =
                            context.getString(
                                R.string.connecting
                            )
                    }
                }
            }
        }
    }

    private fun mobileData() {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        //
        setStageDataMobile()
        //
        broadCastMobileData = BroadCastMobileData()
        broadCastMobileData.binding = binding
        val intent = IntentFilter()
        intent.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(broadCastMobileData, intent)

        binding.btnMobileData.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    longClickCountTimer(event) {
                        listener?.invoke()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    actionUpItemView(event) {
                        if (!PreferencesUtils.getBoolean(
                                context.resources.getString(
                                    R.string.state_usage_data
                                ),
                                false
                            ) && !PreferencesUtils.getBoolean(
                                context.resources.getString(
                                    R.string.stata_usage_day
                                ),
                                false
                            )
                        ) {
                            if (binding.txtStatusMobileData.text == context.getString(
                                    R.string.off
                                )
                            ) {
                                when {
                                    telephonyManager?.simState != TelephonyManager.SIM_STATE_READY -> {
                                        listener?.invoke()
                                        showExplanationError(
                                            context.getString(
                                                R.string.no_sim_card
                                            ),
                                            context.getString(
                                                R.string.describe_no_sim_card
                                            )
                                        )
                                    }
                                    isAirplaneModeOn(context) -> {
                                        listener?.invoke()
                                        showExplanationError(
                                            context.getString(
                                                R.string.airplane_mode
                                            ),
                                            context.getString(
                                                R.string.describe_airplane_mode
                                            )
                                        )
                                    }
                                    else -> {
                                        try {
                                            setColorDrawable(
                                                binding.btnMobileData,
                                                "#BF2CAF67"
                                            )
                                            if (!PreferencesUtils.getBoolean(
                                                    context.resources.getString(
                                                        R.string.state_usage_data
                                                    ),
                                                    false
                                                ) && !PreferencesUtils.getBoolean(
                                                    context.resources.getString(
                                                        R.string.stata_usage_day
                                                    ),
                                                    false
                                                )
                                            ) {
                                                startAnimVector(binding.icDataMobile)
                                            }
                                            binding.txtStatusMobileData.text =
                                                context.getString(
                                                    R.string.connecting
                                                )
                                            setMobileDataEnabled(
                                                context, true
                                            )
                                        } catch (e: Exception) {
                                            autoShowMaskBackgroundBlack()
                                            MyAccessibilityService.instance?.actionAutoClick(
                                                MyAccessibilityService.DoingAction.MobileData
                                            )
                                        }
                                    }
                                }
                            } else {
                                try {
                                    setMobileDataEnabled(context, false)
                                } catch (ex: java.lang.Exception) {
                                    autoShowMaskBackgroundBlack()
                                    MyAccessibilityService.instance?.actionAutoClick(
                                        MyAccessibilityService.DoingAction.MobileData
                                    )
                                }
                            }
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

    fun setStageDataMobile() {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (!PreferencesUtils.getBoolean(
                context.resources.getString(R.string.state_usage_data),
                false
            ) && !PreferencesUtils.getBoolean(
                context.resources.getString(R.string.stata_usage_day),
                false
            )
        ) {
            if (telephonyManager!!.simState == TelephonyManager.SIM_STATE_READY) {
                if (Settings.Global.getInt(context.contentResolver, "mobile_data", 1) == 1) {
                    setColorDrawable(binding.btnMobileData, "#BF2CAF67")
                    binding.txtStatusMobileData.text = context.getString(R.string.on)
                } else {
                    setColorDrawable(
                        binding.btnMobileData,
                        PreferencesUtils.getString(
                            BACKGROUND_COLOR,
                            context.resources.getString(R.string.color_4DFFFFFF)
                        )
                    )
                    binding.txtStatusMobileData.text = context.getString(R.string.off)
                }
            } else {
                setColorDrawable(
                    binding.btnMobileData,
                    PreferencesUtils.getString(
                        BACKGROUND_COLOR,
                        context.resources.getString(R.string.color_4DFFFFFF)
                    )
                )
                binding.txtStatusMobileData.text = context.getString(R.string.off)
            }
        }
    }

    private fun setMobileDataEnabled(context: Context, enabled: Boolean) {
        val conman = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val conmanClass = Class.forName(conman.javaClass.name)
        val iConnectivityManagerField = conmanClass.getDeclaredField("mService")
        iConnectivityManagerField.isAccessible = true
        val iConnectivityManager = iConnectivityManagerField.get(conman)
        val iConnectivityManagerClass = Class.forName(iConnectivityManager.javaClass.name)
        val setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod(
            "setMobileDataEnabled",
            java.lang.Boolean.TYPE
        )
        setMobileDataEnabledMethod.isAccessible = true
        setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled)
    }

    private fun isAirplaneModeOn(context: Context): Boolean {
        return Settings.System.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
    }

    fun settingMobileData() {
        val intent = Intent()
        intent.action = Settings.ACTION_WIRELESS_SETTINGS
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun hideScanView(viewDialog: ViewDialogSetting) {
        viewDialog.binding.layoutLocation.visibility = View.VISIBLE
        viewDialog.binding.rclScanWifi.visibility = View.GONE
    }

    private fun setColorDrawable(layout: ConstraintLayout, color: String) {
        val bgShape = layout.background as GradientDrawable
        bgShape.mutate()
        bgShape.setColor(Color.parseColor(color))
    }

    @SuppressLint("SetTextI18n")
    fun setUpUsageDataLayout(context: Context) {
        if (PreferencesUtils.getBoolean(
                context.resources.getString(R.string.state_usage_data),
                false
            ) && !PreferencesUtils.getBoolean(
                context.resources.getString(R.string.stata_usage_day),
                false
            )
        ) {
            setUpDetailUsageDataLayout(
                context.resources.getString(R.string.this_month),
                TOTAL_DATA_ON
            )
        } else if (!PreferencesUtils.getBoolean(
                context.resources.getString(R.string.state_usage_data),
                false
            ) && PreferencesUtils.getBoolean(
                context.resources.getString(R.string.stata_usage_day),
                false
            )
        ) {
            setUpDetailUsageDataLayout(
                context.resources.getString(R.string.this_day),
                context.resources.getString(R.string.total_usage_data_of_day)
            )
        } else if (PreferencesUtils.getBoolean(
                context.resources.getString(R.string.state_usage_data),
                false
            ) && PreferencesUtils.getBoolean(
                context.resources.getString(R.string.stata_usage_day),
                false
            )
        ) {
            binding.layoutContentDataMobile.visibility = View.GONE
            binding.layoutContentUsageData.visibility = View.VISIBLE
            binding.icDataMobile.setImageResource(R.drawable.ic_usage_data)
            binding.icDataMobile.setColorFilter(Color.parseColor("#FF2F80ED"))
            //
            if (totalData() - PreferencesUtils.getLong(
                    TOTAL_DATA_ON,
                    0
                ) >= PreferencesUtils.getInteger(
                    binding.layoutBtnFist.context.resources.getString(R.string.warning_gb), 0
                )
                && PreferencesUtils.getInteger(
                    binding.layoutBtnFist.context.resources.getString(R.string.warning_gb), 0
                ) != 0
                //
                || totalData() - PreferencesUtils.getLong(
                    context.resources.getString(R.string.total_usage_data_of_day),
                    0
                ) >= PreferencesUtils.getInteger(
                    binding.layoutBtnFist.context.resources.getString(R.string.warning_gb), 0
                )
                && PreferencesUtils.getInteger(
                    binding.layoutBtnFist.context.resources.getString(R.string.warning_gb), 0
                ) != 0
            //
            ) {
                setColorDrawable(binding.btnMobileData, "#EB5757")
            }
            //
            when (totalData() - PreferencesUtils.getLong(
                TOTAL_DATA_ON,
                0
            )) {
                in 0..999 -> {
                    binding.txtMonth.text =
                        "${context.resources.getString(R.string.this_month)} ${
                            totalData() - PreferencesUtils.getLong(
                                TOTAL_DATA_ON,
                                0
                            )
                        }  kb"
                }
                in 1000..999999 -> {
                    binding.txtMonth.text =
                        "${context.resources.getString(R.string.this_month)} ${
                            totalData() - PreferencesUtils.getLong(
                                TOTAL_DATA_ON,
                                0
                            ) / 1000F
                        }  Mb"
                }
                in 1000000..9000000000 -> {
                    binding.txtMonth.text =
                        "${context.resources.getString(R.string.this_month)} ${
                            totalData() - PreferencesUtils.getLong(
                                TOTAL_DATA_ON,
                                0
                            ) / 1000000F
                        }  Gb"
                }
            }
            //
            when (totalData() - PreferencesUtils.getLong(
                context.resources.getString(R.string.total_usage_data_of_day),
                0
            )) {
                in 0..999 -> {
                    binding.txtDay.text =
                        "${context.resources.getString(R.string.this_day)} ${
                            totalData() - PreferencesUtils.getLong(
                                TOTAL_DATA_ON,
                                0
                            )
                        }  kb"
                }
                in 1000..999999 -> {
                    binding.txtDay.text =
                        "${context.resources.getString(R.string.this_day)} ${
                            totalData() - PreferencesUtils.getLong(
                                TOTAL_DATA_ON,
                                0
                            ) / 1000F
                        }  Mb"
                }
                in 1000000..9000000000 -> {
                    binding.txtDay.text =
                        "${context.resources.getString(R.string.this_day)} ${
                            totalData() - PreferencesUtils.getLong(
                                TOTAL_DATA_ON,
                                0
                            ) / 1000000F
                        }  Gb"
                }
            }
            binding.btnMobileData.isClickable = false
            setColorDrawable(binding.btnMobileData, "#FFFFFF")
        } else {
            binding.icDataMobile.setImageResource(R.drawable.ic_cc_qs_data_mobile_on)
            binding.icDataMobile.setColorFilter(
                Color.parseColor(
                    PreferencesUtils.getString(
                        context.resources.getString(R.string.ICON_COLOR),
                        "#FFFFFF"
                    )
                )
            )
            binding.txtMobileData.text =
                context.resources.getString(R.string.mobile_data)
            binding.btnMobileData.isClickable = true
            setStageDataMobile()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpDetailUsageDataLayout(name: String, totalName: String) {
        binding.layoutContentDataMobile.visibility = View.VISIBLE
        binding.layoutContentUsageData.visibility = View.GONE
        binding.icDataMobile.setImageResource(R.drawable.ic_usage_data)
        binding.icDataMobile.setColorFilter(Color.parseColor("#FF2F80ED"))
        binding.txtMobileData.text = name
        setUpTotalData(totalName)
        binding.btnMobileData.isClickable = false
        setColorDrawable(binding.btnMobileData, "#FFFFFF")
    }

    private fun totalData(): Long {
        val received = TrafficStats.getTotalRxBytes() / (1024 * 1024)
        val send = TrafficStats.getTotalTxBytes() / (1024 * 1024)
        return received + send
    }

    @SuppressLint("SetTextI18n")
    private fun setUpTotalData(nameTotalData: String) {
        if (totalData() - PreferencesUtils.getLong(
                nameTotalData,
                0
            ) >= PreferencesUtils.getInteger(
                binding.layoutBtnFist.context.resources.getString(R.string.warning_gb), 0
            )
            //
            && PreferencesUtils.getInteger(
                binding.layoutBtnFist.context.resources.getString(R.string.warning_gb), 0
            ) != 0

        //
        ) {
            setColorDrawable(binding.btnMobileData, "#EB5757")
        }
        when (totalData() - PreferencesUtils.getLong(
            nameTotalData,
            0
        )) {
            in 0..999 -> {
                binding.txtStatusMobileData.text =
                    "${
                        totalData() - PreferencesUtils.getLong(
                            nameTotalData,
                            0
                        )
                    }  kb"
            }
            in 1000..999999 -> {
                binding.txtStatusMobileData.text =
                    "${
                        totalData() - PreferencesUtils.getLong(
                            nameTotalData,
                            0
                        ) / 1000F
                    }  Mb"
            }
            in 1000000..9000000000 -> {
                binding.txtStatusMobileData.text =
                    "${
                        totalData() - PreferencesUtils.getLong(
                            nameTotalData,
                            0
                        ) / 1000000F
                    }  Gb"
            }
        }
    }
}