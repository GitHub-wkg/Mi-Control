package com.ezstudio.controlcenter.darktheme

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.UiModeManager
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
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

object DarkThemeFilterUtil {
    private const val TAG = "BlueLightFilterUtils"
    private const val LABEL_DARK_THEME = "quick_settings_ui_mode_night_label"
    private const val LABEL_DARK_THEME_HUAWEI = "dark_ui_mode"
    private var alertDialogDarkTheme: Dialog? = null
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
            var isEnable = detectDarkTheme(context)
            //
            if (isEnable == enable) {
                return false
            }

            var getList =
                nodeInfo.findAccessibilityNodeInfosByText(
                    getStringByName(
                        context,
                        LABEL_DARK_THEME
                    )
                )
            if (getList.size == 0) {
                getList =
                    nodeInfo.findAccessibilityNodeInfosByText(
                        getStringByName(
                            context,
                            LABEL_DARK_THEME_HUAWEI
                        )
                    )
            }
            if (getList.size == 0) {
                val labels = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.dark_theme)} - Name",
                    ""
                )
                getList =
                    nodeInfo.findAccessibilityNodeInfosByText(labels)
            }

            if (getList.size == 0) {
                try {
                    val location = PreferencesUtils.getString(
                        "${context.resources.getString(R.string.dark_theme)} - Location",
                        "0"
                    ).toInt()
                    if (location == 0) {
                        setUpSetting(context, listenerEndWindow)
                    } else {
                        logNodeHeirarchy(nodeInfo, location)
                        sendAction(context)
                    }
                } catch (ex: NumberFormatException) {
                    setUpSetting(context, listenerEndWindow)
                }
            } else {
                for (child in getList) {
                    if (child != null) {
                        isEnable = detectDarkTheme(context)
                        if (isEnable == enable) {
                            return true
                        } else {
                            child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            child.recycle()
                            sendAction(context)
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

    private fun setUpSetting(context: Context, listenerEndWindow: () -> Unit) {
        if (alertDialogDarkTheme != null) {
            if (!alertDialogDarkTheme!!.isShowing) {
                showDialog(context, listenerEndWindow)
            }
        } else {
            showDialog(context, listenerEndWindow)
        }
    }

    private fun showDialog(context: Context, listenerEndWindow: () -> Unit) {
        alertDialogDarkTheme = Dialog(context)
        val viewDialog = ViewDialogOpenSettings(context, null)
        viewDialog.binding.txtSetUpName.text = context.getString(R.string.set_up_dark_theme)
        viewDialog.binding.txtContentDialog.text =
            context.getString(R.string.describe_dark_theme)
        viewDialog.binding.icControl.setImageResource(R.drawable.ic_dark_theme)
        viewDialog.binding.btnOpenSettings.setOnClickListener {
            alertDialogDarkTheme?.dismiss()
            listenerSettingBatterySaver(
                viewDialog.binding.txtSetUpName.context,
                listenerEndWindow
            )
        }
        viewDialog.binding.openHelper.setOnClickListener {
            alertDialogDarkTheme!!.dismiss()
            val intent = Intent(context, SystemShadeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            listenerEndWindow.invoke()
        }
        alertDialogDarkTheme?.setCancelable(true)
        alertDialogDarkTheme?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialogDarkTheme?.setContentView(viewDialog.binding.root)
        if (Build.VERSION.SDK_INT >= 22)
            alertDialogDarkTheme?.window!!.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        else
            alertDialogDarkTheme?.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        alertDialogDarkTheme?.show()
        alertDialogDarkTheme?.setOnDismissListener {
            sendAction(context)
        }
    }

    private fun listenerSettingBatterySaver(context: Context, listenerEndWindow: () -> Unit) {
        expandSettingsPanel(context)
        listenerEndWindow.invoke()
    }

    private fun sendAction(context: Context) {
        val intent =
            Intent(context.resources.getString(R.string.action_dark_theme))
        Handler().postDelayed({
            context.sendBroadcast(intent)
        }, 200)
    }

    //
    fun detectDarkTheme(context: Context): Boolean {
        val uiManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        when (uiManager.nightMode) {
            UiModeManager.MODE_NIGHT_NO -> {
                return false
            }
            UiModeManager.MODE_NIGHT_YES, UiModeManager.MODE_NIGHT_AUTO -> {
                return true
            }
        }
        return false
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