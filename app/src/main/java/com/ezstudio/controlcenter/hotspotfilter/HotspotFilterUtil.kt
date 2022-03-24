package com.ezstudio.controlcenter.hotspotfilter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.R.string.set_up_hotspot
import com.ezstudio.controlcenter.activity.SystemShadeActivity
import com.ezstudio.controlcenter.widget.ViewDialogOpenSettings
import com.ezteam.baseproject.utils.PreferencesUtils
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

object HotspotFilterUtil {
    private const val TAG = "BlueLightFilterUtils"
    private const val LABEL_HOTSPOT = "quick_settings_hotspot_label"

    var alertDialogHotspot: Dialog? = null
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
            var isEnable = detectHotSpot(context)
            //
            if (isEnable == enable) {
                return false
            }
            var label = getStringByName(context, LABEL_HOTSPOT)
            //
            var list = nodeInfo
                .findAccessibilityNodeInfosByText(label)
            if (list.size == 0) {
                list = nodeInfo
                    .findAccessibilityNodeInfosByText("Hotspot")
            }
            if (list.size == 0) {
                label = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.hotspot)} - Name",
                    ""
                )
                list =
                    nodeInfo.findAccessibilityNodeInfosByText(label)
            }
            if (list.size > 0) {
                for (child in list) {
                    if (child != null) {
                        isEnable = detectHotSpot(context)
                        if (isEnable == enable) {
                            return true
                        } else {
                            child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            child.recycle()
                        }
                    }
                }

            } else {
                try {
                    val location = PreferencesUtils.getString(
                        "${context.resources.getString(R.string.hotspot)} - Location",
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
            }
        } catch (ex: Exception) {
            setUpSetting(context, listenerEndWindow)
        }
        //        nodeInfo.recycle();
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
//                            nodeInfo.getChild(0).getChild(1).getChild(location - 1).getChild(0)
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
        if (alertDialogHotspot != null) {
            if (!alertDialogHotspot!!.isShowing) {
                showDialog(context, listenerEndWindow)
            }
        } else {
            showDialog(context, listenerEndWindow)
        }
        //
    }

    private fun showDialog(context: Context, listenerEndWindow: () -> Unit) {
        alertDialogHotspot = Dialog(context)
        val viewDialog = ViewDialogOpenSettings(context, null)
        viewDialog.binding.txtSetUpName.text = context.getString(set_up_hotspot)
        viewDialog.binding.txtContentDialog.text =
            context.getString(R.string.describe_hotspot)
        viewDialog.binding.icControl.setImageResource(R.drawable.ic_hotspot)
        viewDialog.binding.btnOpenSettings.setOnClickListener {
            alertDialogHotspot?.dismiss()
            listenerSettingBatterySaver(
                viewDialog.binding.txtSetUpName.context,
                listenerEndWindow
            )
        }
        viewDialog.binding.openHelper.setOnClickListener {
            alertDialogHotspot!!.dismiss()
            val intent = Intent(context, SystemShadeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            listenerEndWindow.invoke()
        }
        alertDialogHotspot?.setCancelable(true)
        alertDialogHotspot?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialogHotspot?.setContentView(viewDialog.binding.root)
        if (Build.VERSION.SDK_INT >= 22)
            alertDialogHotspot?.window!!.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        else
            alertDialogHotspot?.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        alertDialogHotspot?.show()
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

    fun detectHotSpot(context: Context): Boolean {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val method = wifiManager.javaClass.getMethod("getWifiApState")
        val tmp = method.invoke(wifiManager) as Int
        return tmp == 13
    }
}