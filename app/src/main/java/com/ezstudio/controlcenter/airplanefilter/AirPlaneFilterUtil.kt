package com.ezstudio.controlcenter.airplanefilter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.activity.SystemShadeActivity
import com.ezstudio.controlcenter.widget.ViewDialogOpenSettings
import com.ezteam.baseproject.utils.PreferencesUtils

object AirPlaneFilterUtil {
    private const val LABEL_AIR_PLANE = "quick_settings_flight_mode_detail_title"
    private const val LABEL_AIR_PLANE_SS_LOW = "quick_settings_airplane_mode_label"
    private const val LABEL_AIR_PLANE_HUAWEI = "airplane_mode"
    private var alertDialogAirPlane: Dialog? = null

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
            var isEnable = isEnableAirplane(context)
//            logNodeHeirarchy(nodeInfo,0)
            //
            if (isEnable == enable) {
                return false
            }
            var labels = getStringByName(context, LABEL_AIR_PLANE)
            var index = labels.indexOf("\n")
            if (index != -1) {
                labels = labels.substring(0, index)
            }
            var getList =
                nodeInfo.findAccessibilityNodeInfosByText(labels)
            //
            if (getList.size == 0) {
                labels = getStringByName(context, LABEL_AIR_PLANE_SS_LOW)
                index = labels.indexOf("\n")
                if (index != -1) {
                    labels = labels.substring(index + 1, labels.length)
                }
                getList =
                    nodeInfo.findAccessibilityNodeInfosByText(labels)
            }
            //
            if (getList.size == 0) {
                labels = getStringByName(context, LABEL_AIR_PLANE_HUAWEI)
                getList =
                    nodeInfo.findAccessibilityNodeInfosByText(labels)
            }
            //
            if (getList.size == 0) {
                labels = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.airplane_mode)} - Name",
                    ""
                )
                getList =
                    nodeInfo.findAccessibilityNodeInfosByText(labels)
            }
            //
            if (getList.size == 0) {
                try {
                    val location = PreferencesUtils.getString(
                        "${context.resources.getString(R.string.airplane_mode)} - Location",
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
                        isEnable = isEnableAirplane(context)
                        if (isEnable == enable) {
                            return true
                        } else {
                            autoClickInfo(child)
                        }
                    }
                }
                return false
            }
            //
        } catch (ex: Exception) {
            setUpSetting(context, listenerEndWindow)
        }
        return false
    }

    private  fun autoClickInfo(child : AccessibilityNodeInfo){
        child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        child.recycle()
    }
    fun logNodeHeirarchy(nodeInfo: AccessibilityNodeInfo?, location: Int) {
        if (nodeInfo == null) return
        when {
            "HUAWEI".equals(Build.MANUFACTURER, true) -> {
                nodeInfo.getChild(0).getChild(12).getChild(0).getChild(location)
                    .performAction(AccessibilityNodeInfo.ACTION_CLICK)
                nodeInfo.getChild(0).getChild(12).getChild(0).getChild(location).recycle()
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
//    fun logNodeHeirarchy(nodeInfo: AccessibilityNodeInfo?, depth: Int) {
//        if (nodeInfo == null) return
//        val logString = "Text:  ${nodeInfo.text}  -----  ${nodeInfo.contentDescription}"
//        Log.e("CheckSScreen", "logNodeHeirarchy: $depth  $logString")
//        for (i in 0 until nodeInfo.childCount) {
//            logNodeHeirarchy(nodeInfo.getChild(i), depth + 1)
//        }
//    }

    private fun isEnableAirplane(context: Context): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
    }

    private fun setUpSetting(context: Context, listenerEndWindow: () -> Unit) {
        if (alertDialogAirPlane != null) {
            if (!alertDialogAirPlane!!.isShowing) {
                showDialog(context, listenerEndWindow)
            }
        } else {
            showDialog(context, listenerEndWindow)
        }


    }

    private fun showDialog(context: Context, listenerEndWindow: () -> Unit) {
        alertDialogAirPlane = Dialog(context)
        val viewDialog = ViewDialogOpenSettings(context, null)
        viewDialog.binding.txtSetUpName.text = context.getString(R.string.set_up_airplane)
        viewDialog.binding.txtContentDialog.text =
            context.getString(R.string.describe_airplane)
        viewDialog.binding.icControl.setImageResource(R.drawable.ic_airplane)
        viewDialog.binding.btnOpenSettings.setOnClickListener {
            alertDialogAirPlane?.dismiss()
            listenerSettingBatterySaver(
                viewDialog.binding.txtSetUpName.context,
                listenerEndWindow
            )
        }
        viewDialog.binding.openHelper.setOnClickListener {
            alertDialogAirPlane!!.dismiss()
            val intent = Intent(context, SystemShadeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            listenerEndWindow.invoke()
        }
        alertDialogAirPlane?.setCancelable(true)
        alertDialogAirPlane?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialogAirPlane?.setContentView(viewDialog.binding.root)
        if (Build.VERSION.SDK_INT >= 22)
            alertDialogAirPlane?.window!!.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        else
            alertDialogAirPlane?.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        alertDialogAirPlane?.show()
    }

    private fun listenerSettingBatterySaver(context: Context, listenerEndWindow: () -> Unit) {
        val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
        listenerEndWindow.invoke()
    }

}