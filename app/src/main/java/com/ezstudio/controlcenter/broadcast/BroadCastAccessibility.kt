package com.ezstudio.controlcenter.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.activity.Splash
import com.ezstudio.controlcenter.databinding.ActivityRequestPermissionBinding
import com.ezstudio.controlcenter.service.MyAccessibilityService

class BroadCastAccessibility : BroadcastReceiver() {
    var binding: ActivityRequestPermissionBinding? = null
    var myServiceAccessibility: MyAccessibilityService? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            if (it.action == "ACTION_ACCESSIBILITY_SERVICE" && binding != null) {
                binding!!.switchStatusAccessibility.isChecked =
                    it.getBooleanExtra("isRunning", false)
            }
            //
            if (it.action == "ACTION_DISABLE_ACCESSIBILITY_SERVICE" && myServiceAccessibility != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    myServiceAccessibility!!.disableSelf()
                }
            }
        }
    }
}