package com.ezstudio.controlcenter.sceentransmissionfilter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Handler
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.activity.SystemShadeActivity
import com.ezstudio.controlcenter.widget.ViewDialogOpenSettings
import com.ezteam.baseproject.utils.PreferencesUtils

object ScreenTransmissionFilterUtil {
    private const val ALL_DEVICE_LABEL = "quick_settings_cast_title"
    private const val ALL_DEVICE_LABEL_HUAWEI = "wireless_projection_name"
    private var alertDialogScreenTransmission: Dialog? = null
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
        var label = ""
        label = getStringByName(context, ALL_DEVICE_LABEL)
        var index = label.indexOf("\n")
        if (index != -1) {
            label = label.substring(0, index)
        }
        var list = nodeInfo
            .findAccessibilityNodeInfosByText(label)
        if (list.size == 0 && "HUAWEI".equals(Build.MANUFACTURER, true)) {
            list = nodeInfo
                .findAccessibilityNodeInfosByText(getStringByName(context, ALL_DEVICE_LABEL_HUAWEI))
        }
        if (list.size == 0) {
            label = PreferencesUtils.getString(
                "${context.resources.getString(R.string.screen_transmission)} - Name",
                ""
            )
            list =
                nodeInfo.findAccessibilityNodeInfosByText(label)
        }
        if (list.size == 0) {
            try {
                val location = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.screen_transmission)} - Location",
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
            for (child in list) {
                if (child != null) {
                    if (child.contentDescription != null) {
                        return child.contentDescription.toString().indexOf(label) != -1
                    } else if (child.text != null) {
                        return child.text.toString().indexOf(label) != -1
                    }
                }
            }
        }

        return false
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
            label = getStringByName(context, ALL_DEVICE_LABEL)

            val index = label.indexOf("\n")
            if (index != -1) {
                label = label.substring(0, index)
            }
            //
            var list = nodeInfo
                .findAccessibilityNodeInfosByText(label)
            if (list.size == 0) {
                label = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.screen_transmission)} - Name",
                    ""
                )
                list =
                    nodeInfo.findAccessibilityNodeInfosByText(label)
            }
            if (list.size == 0 && "HUAWEI".equals(Build.MANUFACTURER, true)) {
                list = nodeInfo
                    .findAccessibilityNodeInfosByText(
                        getStringByName(
                            context,
                            ALL_DEVICE_LABEL_HUAWEI
                        )
                    )
            }

            if (list.size > 0) {
                for (child in list) {
                    if (child != null) {
                        isEnable = checkEnable(context, nodeInfo)
                        if (isEnable == enable) {
                            return true
                        } else {
                            child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            child.recycle()
                            val intent =
                                Intent(context.resources.getString(R.string.action_screen_transmission))
                            context.sendBroadcast(intent)
                            setting(context,listenerEndWindow)
                        }
                    }
                }
            } else {
                try {
                    val location = PreferencesUtils.getString(
                        "${context.resources.getString(R.string.screen_transmission)} - Location",
                        "0"
                    ).toInt()
                    if (location == 0) {
                        setUpSetting(context, listenerEndWindow, nodeInfo)
                    } else {
                        logNodeHeirarchy(nodeInfo, location,context,listenerEndWindow)
                        val intent =
                            Intent(context.resources.getString(R.string.action_screen_transmission))
                        context.sendBroadcast(intent)
                    }
                } catch (ex: NumberFormatException) {
                    setUpSetting(context, listenerEndWindow, nodeInfo)
                }
            }
//            val intent = Intent("android.settings.CAST_SETTINGS")
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            context.startActivity(intent)
//            endLayoutManager?.invoke()
        } catch (ex: Exception) {
            setUpSetting(context, listenerEndWindow, nodeInfo!!)
        }
        //        nodeInfo.recycle();
        return false
    }

    fun logNodeHeirarchy(nodeInfo: AccessibilityNodeInfo?, location: Int,context: Context,listenerEndWindow: () -> Unit) {
        if (nodeInfo == null) return
        when {
            "HUAWEI".equals(Build.MANUFACTURER, true) -> {
                nodeInfo.getChild(0).getChild(12).getChild(0).getChild(location)
                    .performAction(AccessibilityNodeInfo.ACTION_CLICK)
                setting(context,listenerEndWindow)
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
                             setting(context,listenerEndWindow)
//                            nodeInfo.getChild(0).getChild(1).getChild(location - 13).getChild(0)
//                                .recycle()
                        } else {
                            nodeInfo.getChild(0).getChild(0).getChild(location - 1).getChild(0)
                                .performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            setting(context,listenerEndWindow)
//                            nodeInfo.getChild(0).getChild(0).getChild(location - 1).getChild(0)
//                                .recycle()
                        }
                    }
                }

            }
        }
    }

    private fun getChild(nodeInfo: AccessibilityNodeInfo?, location: Int): AccessibilityNodeInfo? {
            try {
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
            }catch (ex : Exception){
            }
        return null
    }

    private fun setUpSetting(
        context: Context,
        listenerEndWindow: () -> Unit,
        nodeInfo: AccessibilityNodeInfo
    ) {
        if (alertDialogScreenTransmission != null) {
            if (!alertDialogScreenTransmission!!.isShowing) {
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
//        StatusScreenTransmissionUtil.getInstance(context)?.setting!!.isEnableScreenTransmission =
//            checkEnable(context, nodeInfo)
//        val intent =
//            Intent(context.resources.getString(R.string.action_screen_transmission))
//        context.sendBroadcast(intent)
        alertDialogScreenTransmission = Dialog(context)
        val viewDialog = ViewDialogOpenSettings(context, null)
        viewDialog.binding.txtSetUpName.text =
            context.getString(R.string.set_up_screen_transmission)
        viewDialog.binding.txtContentDialog.text =
            context.getString(R.string.describe_screen_transmission)
        viewDialog.binding.icControl.setImageResource(R.drawable.ic_screen_transmission)
        viewDialog.binding.btnOpenSettings.setOnClickListener {
            alertDialogScreenTransmission?.dismiss()
            listenerSettingBatterySaver(
                viewDialog.binding.txtSetUpName.context,
                listenerEndWindow
            )
        }
        viewDialog.binding.openHelper.setOnClickListener {
            alertDialogScreenTransmission!!.dismiss()
            val intent = Intent(context, SystemShadeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            listenerEndWindow.invoke()
        }
        alertDialogScreenTransmission?.setCancelable(true)
        alertDialogScreenTransmission?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialogScreenTransmission?.setContentView(viewDialog.binding.root)
        if (Build.VERSION.SDK_INT >= 22)
            alertDialogScreenTransmission?.window!!.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        else
            alertDialogScreenTransmission?.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        alertDialogScreenTransmission?.show()
    }

    private fun listenerSettingBatterySaver(context: Context, listenerEndWindow: () -> Unit) {
        val intent = Intent("android.settings.CAST_SETTINGS")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
        listenerEndWindow.invoke()
    }

    private  fun setting(context: Context,listenerEndWindow: () -> Unit){
        val intent = Intent("android.settings.CAST_SETTINGS")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
        Handler().postDelayed({
            listenerEndWindow.invoke()
        },500)
    }
}