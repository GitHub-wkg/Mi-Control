package com.ezstudio.controlcenter.bluefilter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.activity.SystemShadeActivity
import com.ezstudio.controlcenter.widget.ViewDialogOpenSettings
import com.ezteam.baseproject.utils.PreferencesUtils
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

object BlueLightFilterUtil {
    private const val TAG = "BlueLightFilterUtils"
    private const val ALL_DEVICE_LABEL = "quick_settings_night_display_label"
    private const val ALL_DEVICE_LABEL_HUAWEI = "eye_comfort_widget_name"
    private const val ALL_DEVICE_LABEL_ON = "quick_settings_secondary_label_until"
    private const val SAMSUNG_LOW_DEVICE_LABEL = "quick_settings_bluelightfilter_label"
    private const val SAMSUNG_HIGH_DEVICE_LABEL = "quick_settings_eyecomfortshield_label"
    private const val SAMSUNG_DEVICE_LABEL_ON = "accessibility_desc_on"
    private var alertDialogBlueNight: Dialog? = null
    fun getStringByName(context: Context, name: String?): String {
        try {
            val resourcesPackageName = "com.android.systemui"
            val resources = context.packageManager.getResourcesForApplication(resourcesPackageName)
            val resourceId = resources.getIdentifier(name, "string", resourcesPackageName)
            return if (resourceId > 0) {
                resources.getString(resourceId)
            } else ""
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    fun checkEnable(context: Context, nodeInfo: AccessibilityNodeInfo?): Boolean {
        if (nodeInfo == null) {
            return false
        }
        if (checkDeviceSamsung()) {
            return checkEnableSamsung(context, nodeInfo)
        }
        var label = ""
        label = getStringByName(context, ALL_DEVICE_LABEL)
        var index = label.indexOf("\n")
        if (index != -1) {
            label = label.substring(0, index)
        }
        var labelOn = getStringByName(context, ALL_DEVICE_LABEL_ON)
        index = labelOn.indexOf(" %s")
        if (index != -1) {
            labelOn = labelOn.substring(0, index)
        }
        //
        var getList =
            nodeInfo.findAccessibilityNodeInfosByText(label)
        if (getList.size == 0) {
            getList = nodeInfo.findAccessibilityNodeInfosByText(
                getStringByName(
                    context,
                    ALL_DEVICE_LABEL_HUAWEI
                )
            )
        }
        if (getList.size == 0) {
            label = PreferencesUtils.getString(
                "${context.resources.getString(R.string.night_light)} - Name",
                ""
            )
            getList =
                nodeInfo.findAccessibilityNodeInfosByText(label)
        }
        if (getList.size == 0) {
            try {
                val location = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.night_light)} - Location",
                    "0"
                ).toInt()
                if (location == 0) {
                    return false
                } else {
                    val accessibilityNodeInfo = getChild(nodeInfo, location)
                    return if (accessibilityNodeInfo != null) {
                        if (accessibilityNodeInfo.text != null) {
                            !accessibilityNodeInfo.text.toString()
                                .equals(context.resources.getString(R.string.tat), true)
                        } else {
                            accessibilityNodeInfo.isEnabled
                        }
                    } else {
                        false
                    }
                }
            } catch (ex: NumberFormatException) {
                return false
            }
        } else {
            for (child in getList) {
                if (child != null) {
                    child.recycle()
                    return if (child.text != null) {
                        !child.text.toString()
                            .equals(context.resources.getString(R.string.tat), true)
                    } else {
                        child.isEnabled
                    }
                }
            }
        }
        //
        //
        return false
    }


    fun checkEnableSamsung(context: Context, nodeInfo: AccessibilityNodeInfo?): Boolean {
        if (nodeInfo == null) {
            return false
        }
        var label = ""
        label = getSamsungLabel(context)
        val index = label.indexOf("\n")
        if (index != -1) {
            label = label.substring(0, index)
        }
        val labelOn = getStringByName(context, SAMSUNG_DEVICE_LABEL_ON)
        val list = nodeInfo
            .findAccessibilityNodeInfosByText(label)
        for (child in list) {
            if (child != null) {
                if (child.contentDescription != null) {
                    if (child.contentDescription.toString().indexOf(labelOn) != -1) {
                        return true
                    }
                    child.recycle()
                } else if (child.text != null) {
                    if (child.text.toString().indexOf(labelOn) != -1) {
                        return true
                    }
                    child.recycle()
                }
            }
        }
        return false
    }

    fun checkDeviceSamsung(): Boolean {
        return try {
            Build.VERSION::class.java.getDeclaredField("SEM_PLATFORM_INT")
                .getInt(null) - 90000 >= 0
        } catch (unused: Throwable) {
            false
        }
    }

    fun setEnable(
        context: Context,
        nodeInfo: AccessibilityNodeInfo?,
        enable: Boolean,
        listenerEndWindow: () -> Unit
    ): Boolean {
        try {
            if (nodeInfo == null) {
                return false
            }
            var isEnable = checkEnable(context, nodeInfo)
            if (isEnable == enable) {
                return false
            }
            var label = ""
            label = if (checkDeviceSamsung()) {
                getSamsungLabel(context)
            } else {
                getStringByName(context, ALL_DEVICE_LABEL)
            }
            val index = label.indexOf("\n")
            if (index != -1) {
                label = label.substring(0, index)
            }
            var getList = nodeInfo.findAccessibilityNodeInfosByText(label)
            //
            if (getList.size == 0) {
                getList = nodeInfo.findAccessibilityNodeInfosByText(
                    getStringByName(
                        context,
                        ALL_DEVICE_LABEL_HUAWEI
                    )
                )
            }
            if (getList.size == 0) {
                label = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.night_light)} - Name",
                    ""
                )
                getList =
                    nodeInfo.findAccessibilityNodeInfosByText(label)
            }
            if (getList.size == 0) {
                try {
                    val location = PreferencesUtils.getString(
                        "${context.resources.getString(R.string.night_light)} - Location",
                        "0"
                    ).toInt()
                    if (location == 0) {
                        setUpSetting(context, listenerEndWindow, nodeInfo)
                    } else {
                        logNodeHeirarchy(nodeInfo, location)
                        val intent =
                            Intent(context.resources.getString(R.string.action_night_light))
                        context.sendBroadcast(intent)
                    }
                } catch (ex: NumberFormatException) {
                    setUpSetting(context, listenerEndWindow, nodeInfo)
                }
            } else {
                for (child in getList) {
                    if (child != null) {
                        isEnable = checkEnable(context, nodeInfo)
                        if (isEnable == enable) {
                            return true
                        } else {
                            child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            child.recycle()
                            val intent =
                                Intent(context.resources.getString(R.string.action_night_light))
                            context.sendBroadcast(intent)
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            setUpSetting(context, listenerEndWindow, nodeInfo!!)
        }
        return false
    }

    fun logNodeHeirarchy(nodeInfo: AccessibilityNodeInfo?, location: Int) {
        if (nodeInfo == null) return
        when {
            "HUAWEI".equals(Build.MANUFACTURER, true) -> {
                nodeInfo.getChild(0).getChild(12).getChild(0).getChild(location)
                    .performAction(AccessibilityNodeInfo.ACTION_CLICK)
//                nodeInfo.getChild(0).getChild(12).getChild(0).getChild(location).recycle()
            }
            "SAMSUNG".equals(Build.MANUFACTURER, true) -> {
                when ((Build.MANUFACTURER
                        + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                        + " " + Build.VERSION_CODES::class.java.fields[Build.VERSION.SDK_INT].name)) {
                    "samsung SM-G965F 9 O_MR1" -> {
                        if (location > 12) {
                            //
                            nodeInfo.getChild(0).getChild(1).getChild(location - 13).getChild(0)
                                .performAction(AccessibilityNodeInfo.ACTION_CLICK)
//                            nodeInfo.getChild(0).getChild(1).getChild(location - 13).getChild(0)
//                                .recycle()
                        } else {
                            nodeInfo.getChild(0).getChild(0).getChild(location - 1).getChild(0)
                                .performAction(AccessibilityNodeInfo.ACTION_CLICK)
//                            nodeInfo.getChild(0).getChild(0).getChild(location - 1).getChild(0)
//                                .recycle()
                        }
                    }}

            }
        }
    }

    private fun getChild(nodeInfo: AccessibilityNodeInfo?, location: Int): AccessibilityNodeInfo? {
        if (nodeInfo == null)
            return null
        when {
            "HUAWEI".equals(Build.MANUFACTURER, true) -> {
                return nodeInfo.getChild(0).getChild(12).getChild(0).getChild(location)
            }
            "SAMSUNG".equals(Build.MANUFACTURER, true) -> {
                if (location > 12) {
                    //
                    return nodeInfo.getChild(0).getChild(1).getChild(location - 13).getChild(0)
                } else {
                    return nodeInfo.getChild(0).getChild(0).getChild(location - 1).getChild(0)
                }
            }
        }
        return null
    }

    private fun setUpSetting(
        context: Context,
        listenerEndWindow: () -> Unit,
        nodeInfo: AccessibilityNodeInfo
    ) {
        if (alertDialogBlueNight != null) {
            if (!alertDialogBlueNight!!.isShowing) {
                showDialog(context, listenerEndWindow, nodeInfo)
            }
        } else {
            showDialog(context, listenerEndWindow, nodeInfo)
        }
    }

    private fun showDialog(
        context: Context,
        listenerEndWindow: () -> Unit,
        nodeInfo: AccessibilityNodeInfo
    ) {
        StatusBarBlueUtil.getInstance(context)?.setting?.isEnableBlueFilter =
            checkEnable(context, nodeInfo)
        alertDialogBlueNight = Dialog(context)
        val viewDialog = ViewDialogOpenSettings(context, null)
        viewDialog.binding.txtSetUpName.text = context.getString(R.string.set_up_night_light)
        viewDialog.binding.txtContentDialog.text =
            context.getString(R.string.describe_night_light)
        viewDialog.binding.icControl.setImageResource(R.drawable.ic_night_light)
        viewDialog.binding.btnOpenSettings.setOnClickListener {
            alertDialogBlueNight?.dismiss()
            listenerSettingBatterySaver(
                viewDialog.binding.txtSetUpName.context,
                listenerEndWindow
            )
        }
        viewDialog.binding.openHelper.setOnClickListener {
            alertDialogBlueNight!!.dismiss()
            val intent = Intent(context, SystemShadeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            listenerEndWindow.invoke()
        }
        alertDialogBlueNight?.setCancelable(true)
        alertDialogBlueNight?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialogBlueNight?.setContentView(viewDialog.binding.root)
        if (Build.VERSION.SDK_INT >= 22)
            alertDialogBlueNight?.window!!.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        else
            alertDialogBlueNight?.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        alertDialogBlueNight?.show()
        alertDialogBlueNight?.setOnDismissListener {
            val intent =
                Intent(context.resources.getString(R.string.action_night_light))
            context.sendBroadcast(intent)
        }
    }

    private fun listenerSettingBatterySaver(context: Context, listenerEndWindow: () -> Unit) {
        expandSettingsPanel(context)
        listenerEndWindow.invoke()
    }

    @SuppressLint("WrongConstant")
    private fun expandSettingsPanel(context: Context) {
        try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManager: Class<*> = Class.forName("android.app.StatusBarManager")
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

    fun getSamsungLabel(context: Context): String {
        val f = File("/system/system_ext/priv-app/SystemUI/SystemUI.apk")
        if (f.exists()) {
            // android one ui 3.1 // samsung android 11
            return getStringByName(context, SAMSUNG_HIGH_DEVICE_LABEL)
        } else if (File("/system/priv-app/SystemUI/SystemUI.apk").exists()) {
            return getStringByName(context, SAMSUNG_LOW_DEVICE_LABEL)
        }
        return ""
    }
}