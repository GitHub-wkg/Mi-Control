package com.ezstudio.controlcenter.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.ActivityBackGroundBinding
import java.io.ByteArrayOutputStream
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*

class BackGroundActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBackGroundBinding
    private val REQUEST_CODE_LOCATION = 1
    private val REQUEST_CODE_WRITE_SETTINGS = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackGroundBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        val intent = intent
        when {
            intent.getStringExtra("PERMISSION") == "LOCATION" -> {
                requestPermissionLocation()
            }
            intent.getStringExtra("PERMISSION") == "WRITE_SETTING" -> {
                requestPermissionWriteSetting()
            }
        }
    }

    private fun requestPermissionLocation() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CODE_LOCATION
        )
    }

    private fun requestPermissionWriteSetting() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + this.packageName)
            startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS)
        } else {
            try {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_SETTINGS),
                    REQUEST_CODE_WRITE_SETTINGS
                )
            } catch (ex: SecurityException) {
                Toast.makeText(this, "Find and turn on", Toast.LENGTH_SHORT).show()
                expandSettingsPanel()
                finish()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_LOCATION -> {
                finish()
            }
            REQUEST_CODE_WRITE_SETTINGS -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        } else {
            finish()
        }
    }

    private fun requestPermission(permissionName: String, permissionRequestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permissionName), permissionRequestCode)
    }

    private fun showExplanation(title: String, message: String) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ -> expandSettingsPanel() }
            .create()
        if (Build.VERSION.SDK_INT >= 22)
            alertDialog.window!!.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        else
            alertDialog.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        alertDialog.show()
    }


    @SuppressLint("WrongConstant")
    private fun expandSettingsPanel() {
        try {
            val statusBarService = this.getSystemService("statusbar")
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