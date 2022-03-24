 package com.ezstudio.controlcenter.widget

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.GradientDrawable
import android.hardware.Camera
import android.hardware.Camera.open
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.broadcast.BroadCastBluetooth
import com.ezstudio.controlcenter.databinding.LayoutBtnSecondsLineBinding
import com.ezstudio.controlcenter.databinding.WindownManagerBinding
import com.ezteam.baseproject.utils.PreferencesUtils


class ViewButtonSecondsLine(context: Context, attrs: AttributeSet?) :
    BaseViewChild(context, attrs) {
    lateinit var binding: LayoutBtnSecondsLineBinding
    lateinit var layoutWindowManager: WindownManagerBinding
    lateinit var broadCastBluetooth: BroadCastBluetooth
    private var camera: Camera? = null
    private var parameters: Camera.Parameters? = null
    private var cameraManager: CameraManager? = null
    var endWindowManager: (() -> Unit)? = null

    init {
        initView()
        flashLight()
    }

    private fun initView() {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_btn_seconds_line, this, true)
        binding = LayoutBtnSecondsLineBinding.bind(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun bluetooth() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        setStageBluetooth()
        broadCastBluetooth = BroadCastBluetooth()
        broadCastBluetooth.layoutWindowManager = layoutWindowManager
        val intent = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(broadCastBluetooth, intent)
        //
        binding.btnBluetooth.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    longClickCountTimer(event) {
                        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                        if (mBluetoothAdapter == null) {
                            showExplanationError(
                                context.resources.getString(R.string.control_center_notification),
                                context.getString(R.string.device_does_not_support_bluetooth)
                            )
                            endWindowManager?.invoke()
                        } else {
                            val intent = Intent()
                            intent.action = android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
                            intent.flags = FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                            endWindowManager?.invoke()
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    actionUpItemView(event) {
                        Handler().postDelayed({
                            if (mBluetoothAdapter == null) {
                                showExplanationError(
                                    context.resources.getString(
                                        R.string.control_center_notification
                                    ),
                                    context.getString(
                                        R.string.device_does_not_support_bluetooth
                                    )
                                )
                                endWindowManager?.invoke()
                            } else {
                                // start anim vector
                                if (binding.txtStatusBluetooth.text.toString()
                                        .equals(
                                            context.getString(R.string.off),
                                            true
                                        )
                                ) {
                                    val drawable =
                                        binding.icBluetooth.drawable
                                    if (drawable is AnimatedVectorDrawableCompat) {
                                        drawable.start()
                                    } else if (drawable is AnimatedVectorDrawable) {
                                        drawable.start()
                                    }
                                }
                                //
                                if (mBluetoothAdapter.isEnabled) {
                                    setColorDrawable(
                                        binding.btnBluetooth,
                                        PreferencesUtils.getString(
                                            BACKGROUND_COLOR,
                                            context.resources.getString(
                                                R.string.color_4DFFFFFF
                                            )
                                        )
                                    )
                                    binding.txtStatusBluetooth.text =
                                        context.getString(R.string.off)

                                    mBluetoothAdapter.disable()
                                } else {
                                    mBluetoothAdapter.enable()
                                }
                            }
                        }, 200)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    actionMoveView(event)
                }
            }
            true
        }
    }

    fun setStageBluetooth() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        when {
            mBluetoothAdapter == null -> {
                //
            }
            mBluetoothAdapter.isEnabled -> {
                setColorDrawable(
                    binding.btnBluetooth,
                    PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")
                )
                binding.txtStatusBluetooth.text = context.getString(R.string.on)
            }
            else -> {
                setColorDrawable(
                    binding.btnBluetooth,
                    PreferencesUtils.getString(
                        BACKGROUND_COLOR,
                        context.resources.getString(R.string.color_4DFFFFFF)
                    )
                )
                binding.txtStatusBluetooth.text = context.getString(R.string.off)
            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun flashLight() {
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            turnFlashlightOff()
            binding.btnFlashlight.setOnTouchListener { v, event ->
                when (event!!.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_DOWN -> {
                        longClickCountTimer(event) {

                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        actionUpItemView(event) {
                            Handler().postDelayed({
                                if (binding.txtStatusFlash.text.toString()
                                        .equals(
                                            context.getString(R.string.off),
                                            true
                                        )
                                ) {
                                    val drawable =
                                        binding.icFlashLight.drawable
                                    if (drawable is AnimatedVectorDrawableCompat) {
                                        drawable.start()
                                    } else if (drawable is AnimatedVectorDrawable) {
                                        drawable.start()
                                    }
                                }
                                //
                                if (binding.txtStatusFlash.text.toString()
                                        .equals(
                                            context.getString(R.string.off),
                                            true
                                        )
                                ) {
                                    turnFlashlightOn()
                                } else {
                                    turnFlashlightOff()
                                }
                            }, 200)
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        actionMoveView(event)
                    }
                }
                true
            }

        } else {
            binding.txtStatusFlash.text = context.getString(R.string.off)
            setColorDrawable(
                binding.btnFlashlight,
                PreferencesUtils.getString(
                    BACKGROUND_COLOR,
                    context.resources.getString(R.string.color_4DFFFFFF)
                )
            )
            Toast.makeText(
                context,
                context.getString(R.string.unsupported_device),
                Toast.LENGTH_SHORT
            ).show()
        }


    }

    private fun turnFlashlightOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (cameraManager == null) {
                    cameraManager =
                        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                }

                if (cameraManager != null) {
                    val cameraId = try {
                        cameraManager!!.cameraIdList[0] ?: "0"
                    } catch (e: Exception) {
                        "0"
                    }
                    cameraManager!!.setTorchMode(cameraId, true)
                }
            } catch (e: CameraAccessException) {
            }
        } else {
//            camera = open()
//            parameters = camera!!.parameters
            setUpCamera()
            parameters!!.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            camera?.parameters = parameters
            camera?.startPreview()
        }
        binding.txtStatusFlash.text = context.getString(R.string.on)
        setColorDrawable(binding.btnFlashlight, "#FA8748")
    }

    private fun turnFlashlightOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (cameraManager == null) {
                    cameraManager =
                        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                }
                val cameraId: String
                if (cameraManager != null) {
                    try {
                        cameraId = cameraManager!!.cameraIdList[0]
                            ?: "0"// Usually front camera is at 0 position.
                        cameraManager!!.setTorchMode(cameraId, false)
                    } catch (e: Exception) {
                    }
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        } else {
            // release camera
            releaseCamera()
            // set up camera
            setUpCamera()
            parameters?.flashMode = Camera.Parameters.FLASH_MODE_OFF
            camera?.parameters = parameters
            camera?.stopPreview()
            camera?.release()
            camera = null
            parameters = null
        }
        binding.txtStatusFlash.text = context.getString(R.string.off)
        setColorDrawable(
            binding.btnFlashlight,
            PreferencesUtils.getString(
                BACKGROUND_COLOR,
                context.resources.getString(R.string.color_4DFFFFFF)
            )
        )
    }

    private fun setUpCamera() {
        if (camera == null) {
            try {
                camera = open()
                parameters = camera?.parameters
            } catch (re: RuntimeException) {
            }
        }
    }

    private fun releaseCamera() {
        if (camera != null) {
            camera?.release()
            camera = null
        }
    }

    private fun setColorDrawable(layout: ConstraintLayout, color: String) {
        val bgShape = layout.background as GradientDrawable
        bgShape.mutate()
        bgShape.setColor(Color.parseColor(color))
    }
}