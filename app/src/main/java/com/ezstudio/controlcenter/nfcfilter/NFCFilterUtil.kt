package com.ezstudio.controlcenter.nfcfilter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Handler
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.activity.SystemShadeActivity
import com.ezstudio.controlcenter.widget.ViewDialogOpenSettings
import com.ezteam.baseproject.utils.PreferencesUtils

object NFCFilterUtil {
    private const val TAG = "BlueLightFilterUtils"
    private const val LABEL_NFC = "quick_settings_nfc_label"
    private var alertDialogNFC: Dialog? = null
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
            var isEnable = detectNFC(context)
            //
            if (isEnable == enable) {
                return false
            }
            var label = getStringByName(context, LABEL_NFC)
            //
            var list = nodeInfo
                .findAccessibilityNodeInfosByText(label)
            if (list.size == 0) {
                list = nodeInfo
                    .findAccessibilityNodeInfosByText("NFC")
            }
            if (list.size == 0) {
                label = PreferencesUtils.getString(
                    "${context.resources.getString(R.string.nfc)} - Name",
                    ""
                )
                list =
                    nodeInfo.findAccessibilityNodeInfosByText(label)
            }
            if (list.size > 0) {
                for (child in list) {
                    if (child != null) {
                        isEnable = detectNFC(context)
                        if (isEnable == enable) {
                            return true
                        } else {
                            child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            child.recycle()
                            Handler().postDelayed({
                                sendAction(context)
                            }, 100)
                        }
                    }
                }

            } else {
                try {
                    val location = PreferencesUtils.getString(
                        "${context.resources.getString(R.string.nfc)} - Location",
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

    private fun sendAction(context: Context) {
        val intent =
            Intent(context.resources.getString(R.string.action_nfc))
        Handler().postDelayed({
            context.sendBroadcast(intent)
        }, 200)
    }

    private fun setUpSetting(context: Context, listenerEndWindow: () -> Unit) {
        if (alertDialogNFC != null) {
            if (!alertDialogNFC!!.isShowing) {
                showDialog(context, listenerEndWindow)
            }
        } else {
            showDialog(context, listenerEndWindow)
        }
    }

    private fun showDialog(context: Context, listenerEndWindow: () -> Unit) {
        alertDialogNFC = Dialog(context)
        val viewDialog = ViewDialogOpenSettings(context, null)
        viewDialog.binding.txtSetUpName.text = context.getString(R.string.set_up_nfc)
        viewDialog.binding.txtContentDialog.text =
            context.getString(R.string.describe_nfc)
        viewDialog.binding.icControl.setImageResource(R.drawable.ic_nfc)
        viewDialog.binding.btnOpenSettings.setOnClickListener {
            alertDialogNFC?.dismiss()
            listenerSettingBatterySaver(
                viewDialog.binding.txtSetUpName.context,
                listenerEndWindow
            )
        }
        viewDialog.binding.openHelper.setOnClickListener {
            alertDialogNFC!!.dismiss()
            val intent = Intent(context, SystemShadeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            listenerEndWindow.invoke()
        }
        alertDialogNFC?.setCancelable(true)
        alertDialogNFC?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialogNFC?.setContentView(viewDialog.binding.root)
        if (Build.VERSION.SDK_INT >= 22)
            alertDialogNFC?.window!!.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        else
            alertDialogNFC?.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        alertDialogNFC?.show()
        alertDialogNFC?.setOnDismissListener {
            sendAction(context)
        }
    }

    private fun listenerSettingBatterySaver(context: Context, listenerEndWindow: () -> Unit) {
        val intent = if (Build.VERSION.SDK_INT >= 29) {
            Intent("android.settings.panel.action.NFC")
        } else {
            Intent("android.settings.NFC_SETTINGS")
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
        listenerEndWindow.invoke()
    }

    fun detectNFC(context: Context): Boolean {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        return nfcAdapter?.isEnabled ?: false
    }
}