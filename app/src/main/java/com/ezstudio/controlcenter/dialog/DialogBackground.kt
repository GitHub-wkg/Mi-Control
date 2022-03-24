package com.ezstudio.controlcenter.dialog

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.adapter.AdapterBackground
import com.ezstudio.controlcenter.broadcast.BroadCastChooseColor
import com.ezstudio.controlcenter.common.EventTracking
import com.ezstudio.controlcenter.databinding.LayoutDialogBackgroundBinding
import com.ezstudio.controlcenter.model.ItemBackground
import com.ezteam.baseproject.utils.PreferencesUtils
import com.google.android.gms.ads.ez.analytics.FlurryAnalytics


class DialogBackground(context: Context) : AlertDialog(context) {
    private lateinit var listBackground: MutableList<ItemBackground>
    private val BACKGROUND = "BACKGROUND"
    private val BACKGROUND_MODEL = "BACKGROUND_MODEL"
    private val OPACITY = "OPACITY"
    private val COLOR = "COLOR"
    lateinit var binding: LayoutDialogBackgroundBinding
    private lateinit var broadCastChooseColor: BroadCastChooseColor
    var listenerClickOK: (() -> Unit)? = null
    var listenerClickCancel: (() -> Unit)? = null
    var listenerRequestPermission: (() -> Unit)? = null
    var listenerChosePickImage: (() -> Unit)? = null
    var listenerClickColorPicker: (() -> Unit)? = null
    var listenerChangeBlurLive: (() -> Unit)? = null
    var listenerChangeBlurCustomPhoto: (() -> Unit)? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setCancelable(true)
        addDataListBackground()
        val intent = IntentFilter(context.resources.getString(R.string.action_choose_color))
        binding = LayoutDialogBackgroundBinding.inflate(LayoutInflater.from(context))
        binding.seekBar.progress = PreferencesUtils.getInteger(OPACITY, 50)
        binding.seekBarTransparent.progress = PreferencesUtils.getInteger(
            context.resources.getString(R.string.transparent_amount),
            50
        )
        //
        val bgShape = binding.icShapeColor.background as GradientDrawable
        bgShape.mutate()
        bgShape.setColor(Color.parseColor(PreferencesUtils.getString(COLOR, "#0094FF")))
        //
        broadCastChooseColor = BroadCastChooseColor()
        broadCastChooseColor.binding = binding
        context.registerReceiver(broadCastChooseColor, intent)
        val adapter = AdapterBackground(listBackground)
        adapter.listenerOnClick = {
            setUpLayout()
        }
        adapter.listenerRequestPermission = {
            listenerRequestPermission?.invoke()
        }
        binding.rclIconShape.adapter = adapter
        binding.rclIconShape.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.btnOk.setOnClickListener {
            listenerClickOK?.invoke()
            changeBlur()
            this.dismiss()
        }
        binding.btnCancel.setOnClickListener {
            listenerClickCancel?.invoke()
            changeBlur()
            dismiss()
        }
        binding.choseBackground.setOnClickListener {
            listenerRequestPermission?.invoke()
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                binding.choseBackground.setBackgroundResource(
                    R.drawable.custom_half_left_background)
                binding.choseCustomPhoto.setBackgroundResource(android.R.color.transparent)
                PreferencesUtils.putString(
                    BACKGROUND_MODEL,
                    context.resources.getString(R.string.back_ground)
                )
            }
        }
        binding.choseCustomPhoto.setOnClickListener {
            binding.choseBackground.setBackgroundResource(android.R.color.transparent)
            binding.choseCustomPhoto.setBackgroundResource(R.drawable.custom_half_right_background)
            listenerRequestPermission?.invoke()
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                PreferencesUtils.putString(
                    BACKGROUND_MODEL,
                    context.resources.getString(R.string.custom_photo)
                )
                listenerChosePickImage?.invoke()
            }
        }
        binding.icShapeColor.setOnClickListener {
            listenerClickColorPicker?.invoke()
        }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                PreferencesUtils.putInteger(OPACITY, seekBar.progress)
                changeBlur()
            }
        })
        binding.seekBarTransparent.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                PreferencesUtils.putInteger(
                    context.resources.getString(R.string.transparent_amount),
                    seekBar.progress
                )
                changeBlur()
            }
        })
        //
        //
        setUpLayout()
        setContentView(binding.root)
    }

    private fun changeBlur() {
        val intent = Intent(context.resources.getString(R.string.action_blur_image))
        when (PreferencesUtils.getInteger(BACKGROUND, R.drawable.ic_background_image)) {
            R.drawable.ic_background_image -> {
                context.sendBroadcast(intent)
                FlurryAnalytics.logEvent(EventTracking.CHANGE_BACKGROUND, "blur_image")
            }
            R.drawable.ic_background_color -> {
                PreferencesUtils.putString(BACKGROUND_MODEL, "null")
                listenerChangeBlurCustomPhoto?.invoke()
                FlurryAnalytics.logEvent(EventTracking.CHANGE_BACKGROUND, "monochrome_color")
            }
            R.drawable.ic_backround_live_blur -> {
                PreferencesUtils.putString(BACKGROUND_MODEL, "null")
                listenerChangeBlurLive?.invoke()
                FlurryAnalytics.logEvent(EventTracking.CHANGE_BACKGROUND, "live_blur")
            }
        }

    }

    private fun addDataListBackground() {
        listBackground = mutableListOf()
        listBackground.apply {
            add(
                ItemBackground(
                    context.resources.getString(R.string.txt_background),
                    context.resources.getString(R.string.content_background_image),
                    R.drawable.ic_background_image
                )
            )
            add(
                ItemBackground(
                    context.resources.getString(R.string.txt_background),
                    context.resources.getString(R.string.content_background_color),
                    R.drawable.ic_background_color
                )
            )
            add(
                ItemBackground(
                    context.resources.getString(R.string.txt_background),
                    context.resources.getString(R.string.content_background_live),
                    R.drawable.ic_backround_live_blur
                )
            )
        }
    }

    fun setUpLayout() {
        when (PreferencesUtils.getInteger(BACKGROUND, R.drawable.ic_background_image)) {
            R.drawable.ic_background_image -> {
                binding.txtOpacity.text = context.getString(R.string.blur_amount)
                binding.layoutOfBackgroundColor.visibility = View.GONE
                binding.layoutOfBackgroundImage.visibility = View.VISIBLE
                binding.layoutSetUpTransparent.visibility = View.VISIBLE
                if (PreferencesUtils.getString(BACKGROUND_MODEL) != "null") {
                    if (PreferencesUtils.getString(BACKGROUND_MODEL) == context.resources.getString(
                            R.string.back_ground
                        )
                    ) {
                        binding.choseBackground.setBackgroundResource(
                            R.drawable.custom_half_left_background)
                        binding.choseCustomPhoto.setBackgroundResource(android.R.color.transparent)

                    } else {
                        binding.choseCustomPhoto.setBackgroundResource(
                            R.drawable.custom_half_right_background)
                        binding.choseBackground.setBackgroundResource(android.R.color.transparent)
                    }
                } else {
                    binding.choseBackground.setBackgroundResource(android.R.color.transparent)
                    binding.choseCustomPhoto.setBackgroundResource(android.R.color.transparent)
                }
            }
            R.drawable.ic_background_color -> {
                binding.txtOpacity.text = context.getString(R.string.transparency)
                binding.layoutOfBackgroundColor.visibility = View.VISIBLE
                binding.layoutOfBackgroundImage.visibility = View.GONE
                binding.layoutSetUpTransparent.visibility = View.GONE
            }
            else -> {
                binding.txtOpacity.text = context.getString(R.string.blur_amount)
                binding.layoutOfBackgroundColor.visibility = View.GONE
                binding.layoutOfBackgroundImage.visibility = View.GONE
                binding.layoutSetUpTransparent.visibility = View.VISIBLE
            }
        }
    }

    override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        super.setOnDismissListener(listener)
        context.unregisterReceiver(broadCastChooseColor)
    }
}