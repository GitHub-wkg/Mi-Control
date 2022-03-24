package com.ezstudio.controlcenter.autorotatefilter

import android.annotation.SuppressLint
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
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

object AutoRotateFilterUtil {
    private const val AUTO_ROTATE_LABEL = "quick_settings_rotation_unlocked_label"
    private var alertDialogAutoRotate: Dialog? = null

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
            var isEnable = Settings.System.getInt(
                context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                0
            ) == 1

            if (isEnable == enable) {
                return false
            }
            //
            var getList =
                nodeInfo.findAccessibilityNodeInfosByText(
                    getStringByName(
                        context,
                        AUTO_ROTATE_LABEL
                    )
                )
            if (getList.size == 0) {
                val labels = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.auto_rotate)} - Name",
                    ""
                )
                getList =
                    nodeInfo.findAccessibilityNodeInfosByText(labels)
            }
            if (getList.size == 0) {
                try {
                    val location = PreferencesUtils.getString(
                        "${context.resources.getString(R.string.auto_rotate)} - Location",
                        "0"
                    ).toInt()
                    if (location == 0) {
                        setUpSetting(context, listenerEndWindow)
                    } else {
                        logNodeHeirarchy(nodeInfo, location)
                        val intent =
                            Intent(context.resources.getString(R.string.action_rotation))
                        context.sendBroadcast(intent)
                    }
                } catch (ex: NumberFormatException) {
                    setUpSetting(context, listenerEndWindow)
                }
            } else {
                for (child in getList) {
                    if (child != null) {
                        isEnable = isEnableRotation(context)
                        if (isEnable == enable) {
                            return true
                        } else {
                            child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            child.recycle()
                            val intent =
                                Intent(context.resources.getString(R.string.action_rotation))
                            context.sendBroadcast(intent)
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            setUpSetting(context, listenerEndWindow)
        }
        //        nodeInfo.recycle();
        return false
    }

    private fun isEnableRotation(context: Context): Boolean {
        return Settings.System.getInt(
            context.contentResolver,
            Settings.System.ACCELEROMETER_ROTATION,
            0
        ) == 1
    }

    private fun setUpSetting(context: Context, listenerEndWindow: () -> Unit) {
        if (alertDialogAutoRotate != null) {
            if (!alertDialogAutoRotate!!.isShowing) {
                showDialog(context, listenerEndWindow)
            }
        } else {
            showDialog(context, listenerEndWindow)
        }
    }

    private fun showDialog(context: Context, listenerEndWindow: () -> Unit) {
        alertDialogAutoRotate = Dialog(context)
        val viewDialog = ViewDialogOpenSettings(context, null)
        viewDialog.binding.txtSetUpName.text = context.getString(R.string.set_up_auto_rotate)
        viewDialog.binding.txtContentDialog.text =
            context.getString(R.string.describe_auto_rotate)
        viewDialog.binding.icControl.setImageResource(R.drawable.ic_rotation_lock)
        viewDialog.binding.btnOpenSettings.setOnClickListener {
            alertDialogAutoRotate?.dismiss()
            listenerSettingBatterySaver(
                viewDialog.binding.txtSetUpName.context,
                listenerEndWindow
            )
        }
        viewDialog.binding.openHelper.setOnClickListener {
            alertDialogAutoRotate!!.dismiss()
            val intent = Intent(context, SystemShadeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            listenerEndWindow.invoke()
        }
        alertDialogAutoRotate?.setCancelable(true)
        alertDialogAutoRotate?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialogAutoRotate?.setContentView(viewDialog.binding.root)
        if (Build.VERSION.SDK_INT >= 22)
            alertDialogAutoRotate?.window!!.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        else
            alertDialogAutoRotate?.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        alertDialogAutoRotate?.show()
    }

    private fun listenerSettingBatterySaver(context: Context, listenerEndWindow: () -> Unit) {
        expandSettingsPanel(context)
        listenerEndWindow.invoke()
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
                    }
                }

            }
        }
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

}