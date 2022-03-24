package com.ezstudio.controlcenter.batterysaver

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.PowerManager
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.activity.SystemShadeActivity
import com.ezstudio.controlcenter.widget.ViewDialogOpenSettings
import com.ezteam.baseproject.utils.PreferencesUtils
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

object BatterySaverFilterUtil {
    private const val TAG = "BlueLightFilterUtils"
    private const val BATTERY_SAVER_LABEL = "battery_detail_switch_title"
    private const val BATTERY_SAVER_LABEL_HUAWEI = "super_power_widget_name"
    private var alertDialogBatterySaver: Dialog? = null

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
            var isEnable = detectBatterySaver(context)

            if (isEnable == enable) {
                return false
            }
            //
            var getList =
                nodeInfo.findAccessibilityNodeInfosByText(
                    getStringByName(
                        context,
                        BATTERY_SAVER_LABEL
                    )
                )
            if (getList.size == 0) {
                getList =
                    nodeInfo.findAccessibilityNodeInfosByText(
                        getStringByName(
                            context,
                            BATTERY_SAVER_LABEL_HUAWEI
                        )
                    )
            }
            //
            if (getList.size == 0) {
                val labels = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.battery_saver)} - Name",
                    ""
                )
                getList =
                    nodeInfo.findAccessibilityNodeInfosByText(labels)
            }
            //
            if (getList.size == 0) {
                try {
                    val location = PreferencesUtils.getString(
                        "${context.resources.getString(R.string.battery_saver)} - Location",
                        "0"
                    ).toInt()
                    if (location == 0) {
                        setUpSetting(context, listenerEndWindow)
                    } else {
                        logNodeHeirarchy(nodeInfo, location)
                    }
                } catch (ex: NumberFormatException) {
                    setUpSetting(context, listenerEndWindow)
                }
            } else {
                for (child in getList) {
                    if (child != null) {
                        isEnable = detectBatterySaver(context)
                        if (isEnable == enable) {
                            return true
                        } else {
                            autoClickInfo(child)
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            setUpSetting(context, listenerEndWindow)
        }
        return false
    }

    private  fun autoClickInfo(child : AccessibilityNodeInfo){
        child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        child.recycle()
    }
    private fun logNodeHeirarchy(nodeInfo: AccessibilityNodeInfo?, location: Int) {
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
                    }
                }

            }
        }
    }

    private fun setUpSetting(context: Context, listenerEndWindow: () -> Unit) {
        if (alertDialogBatterySaver != null) {
            if (!alertDialogBatterySaver!!.isShowing) {
                showDialog(context, listenerEndWindow)
            }
        } else {
            showDialog(context, listenerEndWindow)
        }


    }

    private fun showDialog(context: Context, listenerEndWindow: () -> Unit) {
        alertDialogBatterySaver = Dialog(context)
        val viewDialog = ViewDialogOpenSettings(context, null)
        viewDialog.binding.txtSetUpName.text = context.getString(R.string.set_up_battery_saver)
        viewDialog.binding.txtContentDialog.text =
            context.getString(R.string.describe_battery_saver)
        viewDialog.binding.icControl.setImageResource(R.drawable.ic_battery_saver)
        viewDialog.binding.btnOpenSettings.setOnClickListener {
            alertDialogBatterySaver?.dismiss()
            listenerSettingBatterySaver(
                viewDialog.binding.txtSetUpName.context,
                listenerEndWindow
            )
        }
        viewDialog.binding.openHelper.setOnClickListener {
            alertDialogBatterySaver!!.dismiss()
            val intent = Intent(context, SystemShadeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            listenerEndWindow.invoke()
        }
        alertDialogBatterySaver?.setCancelable(true)
        alertDialogBatterySaver?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialogBatterySaver?.setContentView(viewDialog.binding.root)
        if (Build.VERSION.SDK_INT >= 22)
            alertDialogBatterySaver?.window!!.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        else
            alertDialogBatterySaver?.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        alertDialogBatterySaver?.show()
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

    fun detectBatterySaver(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isPowerSaveMode
    }

}