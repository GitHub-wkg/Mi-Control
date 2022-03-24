package com.ezstudio.controlcenter.datasaver

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.activity.SystemShadeActivity
import com.ezstudio.controlcenter.widget.ViewDialogOpenSettings
import com.ezteam.baseproject.utils.PreferencesUtils
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

object DataSaverFilterUtil {
    private const val LABEL_DATA_SAVER = ""
    private const val LABEL_DATA_SAVER_SS_LOW = ""
    private const val LABEL_DATA_SAVER_HUAWEI = ""
    private var alertDialogDataSaver: Dialog? = null

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
            var isEnable = isEnableDataSaver(context)
            //
            if (isEnable == enable) {
                return false
            }
            var labels = getStringByName(context, LABEL_DATA_SAVER)
            var index = labels.indexOf("\n")
            if (index != -1) {
                labels = labels.substring(0, index)
            }
            var getList =
                nodeInfo.findAccessibilityNodeInfosByText(labels)
            if (getList.size == 0) {
                labels = getStringByName(context, LABEL_DATA_SAVER_SS_LOW)
                index = labels.indexOf("\n")
                if (index != -1) {
                    labels = labels.substring(index + 1, labels.length)
                }
                getList =
                    nodeInfo.findAccessibilityNodeInfosByText(labels)
            }
            if (getList.size == 0) {
                labels = getStringByName(context, LABEL_DATA_SAVER_HUAWEI)
                getList =
                    nodeInfo.findAccessibilityNodeInfosByText(labels)
            }
            if (getList.size == 0) {
                labels = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.data_saver)} - Name",
                    ""
                )
                getList =
                    nodeInfo.findAccessibilityNodeInfosByText(labels)
            }
            if (getList.size == 0) {
                try {
                    val location = PreferencesUtils.getString(
                        "${context.resources.getString(R.string.data_saver)} - Location",
                        "0"
                    ).toInt()
                    if (location == 0) {
                        setUpSetting(context, listenerEndWindow)
                    } else {
                        logNodeHeirarchy(nodeInfo, location)
                        val intent =
                            Intent(context.resources.getString(R.string.action_data_saver))
                        context.sendBroadcast(intent)
                    }
                } catch (ex: NumberFormatException) {
                    setUpSetting(context, listenerEndWindow)
                }
            } else {
                for (child in getList) {
                    if (child != null) {
                        isEnable = isEnableDataSaver(context)
                        if (isEnable == enable) {
                            return true
                        } else {
                            child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            child.recycle()
                            val intent =
                                Intent(context.resources.getString(R.string.action_data_saver))
                            context.sendBroadcast(intent)
                        }
                    }
                }
            }
            //

        } catch (ex: Exception) {
            setUpSetting(context, listenerEndWindow)
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
                    }
                }

            }
        }
    }

    private fun isEnableDataSaver(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when (connectivityManager.restrictBackgroundStatus) {
                ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED -> {
                    return true
                }
                ConnectivityManager.RESTRICT_BACKGROUND_STATUS_WHITELISTED, ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED -> {
                    return false
                }
            }
        }
        return false
    }

    private fun setUpSetting(context: Context, listenerEndWindow: () -> Unit) {
        if (alertDialogDataSaver != null) {
            if (!alertDialogDataSaver!!.isShowing) {
                showDialog(context, listenerEndWindow)
            }
        } else {
            showDialog(context, listenerEndWindow)
        }


    }

    private fun showDialog(context: Context, listenerEndWindow: () -> Unit) {
        alertDialogDataSaver = Dialog(context)
        val viewDialog = ViewDialogOpenSettings(context, null)
        viewDialog.binding.txtSetUpName.text = context.getString(R.string.set_up_data_saver)
        viewDialog.binding.txtContentDialog.text =
            context.getString(R.string.describe_data_saver)
        viewDialog.binding.icControl.setImageResource(R.drawable.ic_data_saver_on)
        viewDialog.binding.btnOpenSettings.setOnClickListener {
            alertDialogDataSaver?.dismiss()
            listenerSettingBatterySaver(
                viewDialog.binding.txtSetUpName.context,
                listenerEndWindow
            )
        }
        viewDialog.binding.openHelper.setOnClickListener {
            alertDialogDataSaver!!.dismiss()
            val intent = Intent(context, SystemShadeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            listenerEndWindow.invoke()
        }
        alertDialogDataSaver?.setCancelable(true)
        alertDialogDataSaver?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialogDataSaver?.setContentView(viewDialog.binding.root)
        if (Build.VERSION.SDK_INT >= 22)
            alertDialogDataSaver?.window!!.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        else
            alertDialogDataSaver?.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        alertDialogDataSaver?.show()
    }

    private fun listenerSettingBatterySaver(context: Context, listenerEndWindow: () -> Unit) {
        expandSettingsPanel(context)
        listenerEndWindow.invoke()
    }

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