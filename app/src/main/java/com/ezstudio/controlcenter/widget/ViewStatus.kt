package com.ezstudio.controlcenter.widget

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.broadcast.BroadCastChangeBattery
import com.ezstudio.controlcenter.broadcast.BroadCastChangeRingerMode
import com.ezstudio.controlcenter.broadcast.BroadCastChangeWifi
import com.ezstudio.controlcenter.broadcast.BroadcastSimChange
import com.ezstudio.controlcenter.databinding.LayoutStatusBinding
import com.ezstudio.controlcenter.databinding.WindownManagerBinding
import com.ezstudio.controlcenter.listener.Sim1SignalStrengthsListener
import com.ezstudio.controlcenter.listener.Sim2SignalStrengthsListener
import com.ezstudio.controlcenter.telephony.TelephonyInfo
import com.ezteam.baseproject.utils.PreferencesUtils
import java.lang.reflect.Method


class ViewStatus(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    var layoutWindowManager: WindownManagerBinding? = null
    var broadCastWifi: BroadCastChangeWifi? = null
    lateinit var broadCastChangeRingerMode: BroadCastChangeRingerMode
    lateinit var broadCastChangeBattery: BroadCastChangeBattery
    lateinit var broadCastSimChange: BroadcastSimChange
    lateinit var binding: LayoutStatusBinding
    var listenerConnectWifi: (() -> Unit)? = null
    var endLayoutWindowManager: (() -> Unit)? = null
    val wifiManager: WifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
    private val BACKGROUND_COLOR = "BACKGROUND_COLOR"
    private val SELECTED_COLOR = "SELECTED_COLOR"
    lateinit var telephonyManager: TelephonyManager
    private var telephonyInfo: TelephonyInfo? = null
    var mSim2SignalStrengthsListener: Sim2SignalStrengthsListener? = null
    var mSim1SignalStrengthsListener: Sim1SignalStrengthsListener? = null

    init {
        initView()
        stateWifi()
        stateBattery()
        registerReceiverSim()
        statePhone()
    }

    fun statePhone() {
        if (checkPhonePermission()) {
            binding.layoutStateSim.visibility = View.VISIBLE
            stateSignalSim(telephonyManager)
        } else {
            val isEnabled = Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON, 0
            ) != 0
            if (isEnabled) {
                binding.layoutStateSim.visibility = View.VISIBLE
                binding.icSilentSim1.visibility = View.VISIBLE
                binding.icSilentSim1.setImageResource(R.drawable.ic_airplane)
                binding.nameSim1.visibility = View.GONE
                binding.icSim1.visibility = View.GONE
                binding.layoutSim2.visibility = View.GONE
            } else {
                binding.layoutStateSim.visibility = View.INVISIBLE
            }
        }
    }

    private fun stateBattery() {
        broadCastChangeBattery = BroadCastChangeBattery()
        broadCastChangeBattery.binding = binding
        val intent = IntentFilter()
        intent.addAction(Intent.ACTION_BATTERY_CHANGED)
        intent.addAction(Intent.ACTION_POWER_CONNECTED)
        intent.addAction(Intent.ACTION_POWER_DISCONNECTED)
        context.registerReceiver(broadCastChangeBattery, intent)
        //
        val statusBattery: Int = context.registerReceiver(broadCastChangeBattery, intent)!!
            .getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        if (statusBattery == BatteryManager.BATTERY_STATUS_CHARGING || statusBattery == BatteryManager.BATTERY_STATUS_FULL) {
            broadCastChangeBattery.isPowerConnected = true
        } else if (statusBattery == BatteryManager.BATTERY_STATUS_DISCHARGING) {
            broadCastChangeBattery.isPowerConnected = false
        }
        var batteryPct = 0
        val bm = context.getSystemService(BATTERY_SERVICE) as BatteryManager
        batteryPct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        broadCastChangeBattery.setPowerBattery(batteryPct)
    }

    fun stateWifi() {
        if (broadCastWifi != null) {
            wifiManager?.let { broadCastWifi!!.wifiManager = wifiManager!! }
            broadCastWifi!!.layoutWindowManager = layoutWindowManager
            broadCastWifi!!.bindingViewStatus = binding
        }
        val intent = IntentFilter()
        intent.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        intent.addAction(WifiManager.RSSI_CHANGED_ACTION)
        context.registerReceiver(broadCastWifi, intent)
        val connManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        mWifi?.let {
            detectWifi(it)
        }

        when (getWifiLevel()) {
            in 0 until 34 -> {
                binding.icWifi.setImageResource(R.drawable.ic_wifi_min)
            }
            in 34 until 66 -> {
                binding.icWifi.setImageResource(R.drawable.ic_wifi_normal)
            }
            in 66..100 -> {
                binding.icWifi.setImageResource(R.drawable.ic_wifi_full)
            }
        }
    }

    fun detectWifi(it: NetworkInfo) {
        if (it.isConnected) {
            listenerConnectWifi?.invoke()
            binding.icWifi.visibility = View.VISIBLE
            layoutWindowManager?.let {
                setColorDrawable(
                    layoutWindowManager!!.layoutBtnFist.binding.btnWifi,
                    PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")
                )
            }
            //
            layoutWindowManager?.layoutBtnFist?.binding?.txtStatusWifi?.text =
                context.getString(R.string.on)
        } else {
            layoutWindowManager?.let {
                setColorDrawable(
                    layoutWindowManager!!.layoutBtnFist.binding.btnWifi,
                    PreferencesUtils.getString(
                        BACKGROUND_COLOR,
                        context.resources.getString(R.string.color_4DFFFFFF)
                    )
                )
            }
            layoutWindowManager?.layoutBtnFist?.binding?.txtStatusWifi?.text =
                context.getString(R.string.off)
            binding.icWifi.visibility = View.GONE
        }
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_status, this, true)
        binding = LayoutStatusBinding.bind(view)
        telephonyInfo = TelephonyInfo.getInstance(binding.icSim1.context)
    }

    private fun registerReceiverSim() {
        telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        broadCastSimChange = BroadcastSimChange()
        broadCastSimChange.binding = binding
        broadCastSimChange.changeListener = {
            if (checkPhonePermission()) {
                phoneCount(telephonyManager)
            }
        }
        val intent = IntentFilter("android.intent.action.SIM_STATE_CHANGED")
        context.registerReceiver(broadCastSimChange, intent)
    }

    private fun stateSignalSim(telephonyManager: TelephonyManager) {
        //
        phoneCount(telephonyManager)
        //
        val isEnabled = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
        if (isEnabled) {
            binding.icSilentSim1.visibility = View.VISIBLE
            binding.icSilentSim1.setImageResource(R.drawable.ic_airplane)
            binding.nameSim1.visibility = View.GONE
            binding.icSim1.visibility = View.GONE
            binding.layoutSim2.visibility = View.GONE
        }
    }

    private fun sim1(mTelephonyManager: TelephonyManager) {
        if (Build.VERSION.SDK_INT >= 22) {
            val mSubscriptionManager = SubscriptionManager.from(context)
            //
            // check permission
            val sub0 = if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0)
            } else {
                return
            }

            //
            if (sub0 != null) {
                // unRegister
                unRegisterPhoneState1()
                mSim1SignalStrengthsListener = Sim1SignalStrengthsListener(sub0.subscriptionId)
                mSim1SignalStrengthsListener?.binding = binding
                mSim1SignalStrengthsListener?.let {
                    mTelephonyManager.listen(
                        it, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                    )
                }

                //
                try {
                    telephonyInfo?.let {
                        if (!it.isSIM1Ready) {
                            binding.icSilentSim1.visibility = View.GONE
                            binding.icSim1.visibility = View.VISIBLE
                            binding.nameSim1.visibility = View.VISIBLE
                            binding.nameSim1.text = context.getString(R.string.no_sim_card)
                            binding.nameSim1.isSelected = true
                        } else {
                            // get name sim
                            val subsInfoList: List<SubscriptionInfo> =
                                mSubscriptionManager.activeSubscriptionInfoList
                            for (i in 0..(subsInfoList.size)) {
                                if (i == 0) {
                                    binding.icSilentSim1.visibility = View.VISIBLE
                                    binding.icSim1.visibility = View.VISIBLE
                                    binding.nameSim1.visibility = View.VISIBLE
                                    binding.nameSim1.text = subsInfoList[i].carrierName
                                    break
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                binding.icSilentSim1.visibility = View.GONE
                binding.nameSim1.text = context.getString(R.string.no_sim_card)
                binding.nameSim1.visibility = View.VISIBLE
                binding.icSim1.visibility = View.VISIBLE
                binding.nameSim1.isSelected = true
            }
        } else {
            // api < 22
            binding.layoutSim2.visibility = View.GONE
            when (mTelephonyManager.simState) {
                TelephonyManager.SIM_STATE_READY -> {
                    binding.icSilentSim1.visibility = View.VISIBLE
                    binding.icSim1.visibility = View.VISIBLE
                    binding.nameSim1.visibility = View.VISIBLE
                    try {
                        if (getOutput(context, "getCarrierName", 0) == null) {
                            binding.nameSim1.text = mTelephonyManager.simOperatorName
                        } else {
                            binding.nameSim1.text = getOutput(context, "getCarrierName", 0)
                        }
                    } catch (e: Exception) {
                        binding.nameSim1.text = mTelephonyManager.simOperatorName

                    }
                }
                else -> {
                    binding.icSilentSim1.visibility = View.VISIBLE
                    binding.icSim1.visibility = View.VISIBLE
                    binding.nameSim1.visibility = View.VISIBLE
                    binding.nameSim1.text = context.getString(R.string.no_sim_card)
                }
            }
        }
    }

    private fun sim2(mTelephonyManager: TelephonyManager) {
        if (Build.VERSION.SDK_INT >= 22) {
            val mSubscriptionManager = SubscriptionManager.from(context)
            val sub0 = if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(1)
            } else {
                return
            }

            //
            if (sub0 != null) {
                // unRegister
                unRegisterPhoneState2()
                mSim2SignalStrengthsListener =
                    Sim2SignalStrengthsListener(sub0.subscriptionId)
                mSim2SignalStrengthsListener?.binding = binding
                mSim2SignalStrengthsListener?.let {
                    mTelephonyManager.listen(
                        it, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                    )
                }
                if (Build.VERSION.SDK_INT >= 26) {
                    if (telephonyManager.getSimState(1) != TelephonyManager.SIM_STATE_READY) {
                        binding.layoutSim2.visibility = View.GONE
                    } else {
                        try {
                            telephonyInfo?.let {
                                if (!it.isSIM1Ready) {
                                    binding.layoutSim1.visibility = View.GONE
                                }
                            }
                            binding.layoutSim2.visibility = View.VISIBLE
                            binding.icSilentSim2.visibility = View.VISIBLE
                            binding.icSim2.visibility = View.VISIBLE
                            val subsInfoList: List<SubscriptionInfo> =
                                mSubscriptionManager.activeSubscriptionInfoList
                            for (i in 0..(subsInfoList.size)) {
                                if (i == 1) {
                                    binding.icSilentSim2.visibility = View.VISIBLE
                                    binding.icSim2.visibility = View.VISIBLE
                                    binding.nameSim2.visibility = View.VISIBLE
                                    binding.nameSim2.text = subsInfoList[i].carrierName
                                    break
                                }
                            }
                        } catch (e: Exception) {
                            binding.layoutSim2.visibility = View.GONE
                            e.printStackTrace()
                        }
                    }
                } else {
                    try {
                        telephonyInfo?.let {
                            if (!it.isSIM1Ready) {
                                binding.layoutSim1.visibility = View.GONE
                            }
                        }
                        binding.layoutSim2.visibility = View.VISIBLE
                        binding.icSilentSim2.visibility = View.VISIBLE
                        binding.icSim2.visibility = View.VISIBLE
                        val subsInfoList: List<SubscriptionInfo> =
                            mSubscriptionManager.activeSubscriptionInfoList
                        for (i in 0..(subsInfoList.size)) {
                            if (i == 1) {
                                binding.icSilentSim2.visibility = View.VISIBLE
                                binding.icSim2.visibility = View.VISIBLE
                                binding.nameSim2.visibility = View.VISIBLE
                                binding.nameSim2.text = subsInfoList[i].carrierName
                                break
                            }
                        }
                    } catch (e: Exception) {
                        binding.layoutSim1.visibility = View.VISIBLE
                        binding.layoutSim2.visibility = View.GONE
                        e.printStackTrace()
                    }
                }
            } else {
                binding.layoutSim2.visibility = View.GONE
            }

        } else {
            binding.layoutSim2.visibility = View.VISIBLE
            when (mTelephonyManager.simState) {
                TelephonyManager.SIM_STATE_READY -> {
                    binding.icSilentSim2.visibility = View.VISIBLE
                    binding.icSim2.visibility = View.VISIBLE
                    binding.nameSim2.visibility = View.VISIBLE
                    try {
                        if (getOutput(context, "getCarrierName", 1) == null) {
                            binding.layoutSim2.visibility = View.GONE
                        } else {
                            binding.nameSim2.text = getOutput(context, "getCarrierName", 1)
                        }
                    } catch (e: Exception) {
                        binding.layoutSim2.visibility = View.GONE
                    }
                }
                else -> {
                    binding.layoutSim2.visibility = View.GONE
                }
            }
        }

    }

    @SuppressLint("MissingPermission")
    fun phoneCount(telephonyManager: TelephonyManager) {
        if (telephonyManager.simState == TelephonyManager.SIM_STATE_READY) {
            if (Build.VERSION.SDK_INT >= 22) {
                val mSubscriptionManager = SubscriptionManager.from(context)
                if (mSubscriptionManager.activeSubscriptionInfoList != null) {
                    val subsInfoList: List<SubscriptionInfo> =
                        mSubscriptionManager.activeSubscriptionInfoList
                    when {
                        subsInfoList.size > 1 -> {
                            sim1(telephonyManager)
                            sim2(telephonyManager)
                        }
                        subsInfoList.size == 1 -> {
                            sim1(telephonyManager)
                            binding.layoutSim2.visibility = View.GONE
                        }
                        else -> {
                            binding.layoutSim2.visibility = View.GONE
                            binding.icSilentSim1.visibility = View.INVISIBLE
                            binding.nameSim1.visibility = View.VISIBLE
                            binding.icSim1.visibility = View.VISIBLE
                            binding.nameSim1.text = context.getString(R.string.no_sim_card)
                        }
                    }
                } else {
                    binding.layoutSim2.visibility = View.GONE
                    binding.icSilentSim1.visibility = View.INVISIBLE
                    binding.nameSim1.visibility = View.VISIBLE
                    binding.icSim1.visibility = View.VISIBLE
                    binding.nameSim1.text = context.getString(R.string.no_sim_card)
                }

            } else {
                sim1(telephonyManager)
                sim2(telephonyManager)
            }
        } else {
            sim1(telephonyManager)
            binding.layoutSim2.visibility = View.GONE
        }

    }

    fun statusAudio() {
        broadCastChangeRingerMode = BroadCastChangeRingerMode()
        broadCastChangeRingerMode.binding = binding
        broadCastChangeRingerMode.layoutWindowManager = layoutWindowManager
        val intent = IntentFilter()
        intent.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
        context.registerReceiver(broadCastChangeRingerMode, intent)
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (am.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> binding.icSound.setImageResource(
                R.drawable.ic_silent
            )
            AudioManager.RINGER_MODE_VIBRATE -> binding.icSound.setImageResource(
                R.drawable.ic_vibrate
            )
            AudioManager.RINGER_MODE_NORMAL -> binding.icSound.setImageResource(R.drawable.ic_ring)
        }

        //
    }

    private fun getWifiLevel(): Int {
        val MIN_RSSI = -100
        val MAX_RSSI = -55
        val numLevels = 101
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val linkSpeed = wifiManager.connectionInfo.rssi
        return when {
            linkSpeed <= MIN_RSSI -> {
                0
            }
            linkSpeed >= MAX_RSSI -> {
                numLevels - 1
            }
            else -> {
                val inputRange = (MAX_RSSI - MIN_RSSI).toFloat()
                val outputRange: Float = numLevels - 1F
                return ((linkSpeed - MIN_RSSI).toFloat() * outputRange / inputRange).toInt()
            }
        }
    }

    private fun setColorDrawable(layout: ConstraintLayout, color: String) {
        val bgShape = layout.background as GradientDrawable
        bgShape.mutate()
        bgShape.setColor(Color.parseColor(color))
    }

    private fun checkPhonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutput(context: Context, methodName: String, slotId: Int): String? {
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val telephonyClass: Class<*>
        var reflectionMethod: String? = null
        var output: String? = null
        try {
            telephonyClass = Class.forName(telephony.javaClass.name)
            for (method in telephonyClass.methods) {
                val name = method.name
                if (name.contains(methodName)) {
                    val params = method.parameterTypes
                    if (params.size == 1 && params[0].name == "int") {
                        reflectionMethod = name
                    }
                }
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        if (reflectionMethod != null) {
            try {
                output = getOpByReflection(telephony, reflectionMethod, slotId, false)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return output
    }

    private fun getOpByReflection(
        telephony: TelephonyManager,
        predictedMethodName: String,
        slotID: Int,
        isPrivate: Boolean
    ): String? {

        //Log.i("Reflection", "Method: " + predictedMethodName+" "+slotID);
        var result: String? = null
        try {
            val telephonyClass = Class.forName(telephony.javaClass.name)
            val parameter = arrayOfNulls<Class<*>?>(1)
            parameter[0] = Int::class.javaPrimitiveType
            val getSimID: Method? = if (slotID != -1) {
                if (isPrivate) {
                    telephonyClass.getDeclaredMethod(predictedMethodName, *parameter)
                } else {
                    telephonyClass.getMethod(predictedMethodName, *parameter)
                }
            } else {
                if (isPrivate) {
                    telephonyClass.getDeclaredMethod(predictedMethodName)
                } else {
                    telephonyClass.getMethod(predictedMethodName)
                }
            }
            val ob_phone: Any?
            val obParameter = arrayOfNulls<Any>(1)
            obParameter[0] = slotID
            if (getSimID != null) {
                ob_phone = if (slotID != -1) {
                    getSimID.invoke(telephony, obParameter)
                } else {
                    getSimID.invoke(telephony)
                }
                if (ob_phone != null) {
                    result = ob_phone.toString()
                }
            }
        } catch (e: java.lang.Exception) {
            //e.printStackTrace();
            return null
        }
        //Log.i("Reflection", "Result: " + result);
        return result
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unRegisterPhoneState1()
        unRegisterPhoneState2()
    }

    private fun unRegisterPhoneState1() {
        mSim1SignalStrengthsListener?.let {
            telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE)
            mSim1SignalStrengthsListener = null
        }
    }

    private fun unRegisterPhoneState2() {
        mSim2SignalStrengthsListener?.let {
            telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE)
            mSim2SignalStrengthsListener = null
        }
    }
}