package com.ezstudio.controlcenter.activity

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.projection.MediaProjectionManager
import android.net.TrafficStats
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.*
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.common.EventTracking
import com.ezstudio.controlcenter.common.KeyViewCenter
import com.ezstudio.controlcenter.databinding.ActivityMainBinding
import com.ezstudio.controlcenter.dialog.DialogBackground
import com.ezstudio.controlcenter.dialog.DialogIconShape
import com.ezstudio.controlcenter.service.MyAccessibilityService
import com.ezstudio.controlcenter.viewmodel.AreaModel
import com.ezstudio.controlcenter.widget.ViewDiaLogResetDefault
import com.ezstudio.controlcenter.widget.ViewDialogUseResetDate
import com.ezstudio.controlcenter.windown_manager.MyWindowManager
import com.ezteam.baseproject.utils.PathUtils.getPath
import com.ezteam.baseproject.utils.PreferencesUtils
import com.google.android.gms.ads.ez.analytics.FlurryAnalytics
import com.google.android.gms.ads.ez.nativead.AdmobNativeAdView
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.yalantis.ucrop.UCrop
import org.koin.android.ext.android.inject
import java.io.File


class MainActivity : AppCompatActivity(), ColorPickerDialogListener {
    private lateinit var binding: ActivityMainBinding
    private val ICON_SHAPE = "ICON_SHAPE"
    private val BACKGROUND = "BACKGROUND"
    private val BACKGROUND_CONTENT = "BACKGROUND_CONTENT"
    private val BACKGROUND_MODEL = "BACKGROUND_MODEL"
    private val BACKGROUND_COLOR = "BACKGROUND_COLOR"
    private val SELECTED_COLOR = "SELECTED_COLOR"
    private val PATH_IMAGE = "PATH_IMAGE"
    private val OPACITY = "OPACITY"
    private val COLOR = "COLOR"
    private val TOTAL_DATA_ON = "TOTAL_DATA_ON"
    private val STATUS_USAGE_DATA = "STATUS_USAGE_DATA"
    private val PATH_CROP = "PATH_CROP"
    private val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1
    private val CODE_SELECT_PICKTURE = 3
    private var image = 0
    private var content = ""
    private var blur = 50
    private var transparent = 50
    private var backgroundModel = "null"
    private var REQUEST_CODE_USAGE_ACCESS_SETTINGS = 2
    private var dialogBackground: DialogBackground? = null
    private var filePath: String? = null
    private lateinit var dialogResetDefault: Dialog
    private lateinit var dialogUseResetDate: Dialog
    private lateinit var dialogWarning: Dialog
    private var mMediaProjectionManager: MediaProjectionManager? = null
    private var mResultCode = 0
    private var mResultData: Intent? = null
    private var mDataCrop: Intent? = null
    private val STATE_RESULT_CODE = "result_code"
    private val STATE_RESULT_DATA = "result_data"
    private val REQUEST_MEDIA_PROJECTION = 4
    private val viewModel by inject<AreaModel>()

    //
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mResultCode =
                savedInstanceState.getInt(STATE_RESULT_CODE)
            mResultData =
                savedInstanceState.getParcelable(STATE_RESULT_DATA)
        }
        dialogResetDefault = Dialog(this)
        dialogUseResetDate = Dialog(this)
        dialogWarning = Dialog(this)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(binding.root)
        initView()
        initListener()
        FlurryAnalytics.logEvent(EventTracking.MAIN_OPEN, "open")
    }

    private fun initView() {
        //
        binding.layoutRate
        val activity: Activity = this
        val metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)
        PreferencesUtils.putInteger(resources.getString(R.string.densityDpi), metrics.densityDpi)
        mMediaProjectionManager =
            activity.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        // check state app
        checkStateApp()
        //
        binding.layoutArrange.icShape.setImageResource(
            PreferencesUtils.getInteger(
                ICON_SHAPE, R.drawable.ic_round
            )
        )
        when (PreferencesUtils.getInteger(
            BACKGROUND, 0
        )) {
            R.drawable.ic_background_image -> {
                binding.layoutColor.icBackground.setImageResource(R.drawable.ic_background_image)
            }
            R.drawable.ic_background_color -> {
                binding.layoutColor.icBackground.setImageResource(R.drawable.ic_background_color)
            }
            R.drawable.ic_backround_live_blur -> {
                binding.layoutColor.icBackground.setImageResource(R.drawable.ic_backround_live_blur)
            }
            else -> {
                binding.layoutColor.icBackground.setImageResource(R.drawable.ic_background_image)
                PreferencesUtils.putInteger(
                    BACKGROUND, R.drawable.ic_background_image
                )
                PreferencesUtils.putString(
                    BACKGROUND_MODEL, "null"
                )
            }
        }
        binding.layoutColor.txtContentBackground.text = PreferencesUtils.getString(
            BACKGROUND_CONTENT, resources.getString(R.string.blur_image)
        )
        binding.layoutColor.icBackgroundColor.setColorFilter(
            Color.parseColor(
                PreferencesUtils.getString(
                    BACKGROUND_COLOR,
                    resources.getString(R.string.color_4DFFFFFF)
                )
            )
        )
        //
        binding.layoutColor.icSelectedColor.setColorFilter(
            Color.parseColor(
                PreferencesUtils.getString(
                    SELECTED_COLOR,
                    resources.getString(R.string.color_2C61CC)
                )
            )
        )
        //
        binding.layoutColor.icShapeTextColor.setColorFilter(
            Color.parseColor(
                PreferencesUtils.getString(
                    resources.getString(R.string.TEXT_COLOR), "#FFFFFF"
                )
            )
        )
        //
        binding.layoutColor.icIconColor.setColorFilter(
            Color.parseColor(
                PreferencesUtils.getString(
                    resources.getString(R.string.ICON_COLOR), "#FFFFFF",
                )
            )
        )
        //
        if (checkUsageAccess()) {
            binding.layoutUseData.switchStatusShowData.isChecked =
                PreferencesUtils.getBoolean(resources.getString(R.string.state_usage_data), false)
        } else {
            binding.layoutUseData.switchStatusShowData.isChecked = false
        }
        //
        if (checkUsageAccess()) {
            binding.layoutUseData.switchStatusDataTrafficToday.isChecked =
                PreferencesUtils.getBoolean(resources.getString(R.string.stata_usage_day), false)
        } else {
            binding.layoutUseData.switchStatusDataTrafficToday.isChecked = false
        }
        binding.layoutColor.icIconDimmerColor.setColorFilter(
            Color.parseColor(
                PreferencesUtils.getString(
                    resources.getString(R.string.DIMMER_COLOR), "#FFFFFF",
                )
            )
        )
        //
        if (binding.layoutStateApp.switchStatusApp.isChecked) {
            if (PreferencesUtils.getInteger(BACKGROUND) != R.drawable.ic_backround_live_blur) {
                val intent = Intent(resources.getString(R.string.action_blur_image))
                sendBroadcast(intent)
            } else {
                if (!PreferencesUtils.getBoolean(
                        resources.getString(R.string.MEDIA_PROJECTION_ENABLE),
                        false
                    )
                ) {
                    startScreenCapture()
                }
            }
        }
        // setUp view center
        setUpViewCenter()
        // ads
        loadAds()
    }

    private fun setUpViewCenter() {
        Glide.with(this)
            .load(R.drawable.ic_enable_control)
            .into(binding.layoutChooseCenter.imgEnableControl)
        Glide.with(this)
            .load(R.drawable.ic_enable_notification)
            .into(binding.layoutChooseCenter.imgEnableNoti)
        if (PreferencesUtils.getBoolean(KeyViewCenter.KEY_VIEW_NOTIFICATION, true)) {
            binding.layoutChooseCenter.imgEnableNoti.visibility = View.VISIBLE
            binding.layoutChooseCenter.layoutDisableNoti.visibility = View.GONE
            binding.layoutChooseCenter.cbNoti.isChecked = true
        } else {
            binding.layoutChooseCenter.imgEnableNoti.visibility = View.GONE
            binding.layoutChooseCenter.layoutDisableNoti.visibility = View.VISIBLE
            binding.layoutChooseCenter.cbNoti.isChecked = false
        }
        //
        if (PreferencesUtils.getBoolean(KeyViewCenter.KEY_VIEW_CONTROL, true)) {
            binding.layoutChooseCenter.imgEnableControl.visibility = View.VISIBLE
            binding.layoutChooseCenter.layoutDisableControl.visibility = View.GONE
            binding.layoutChooseCenter.cbControl.isChecked = true
        } else {
            binding.layoutChooseCenter.imgEnableControl.visibility = View.GONE
            binding.layoutChooseCenter.layoutDisableControl.visibility = View.VISIBLE
            binding.layoutChooseCenter.cbControl.isChecked = false
        }
        //
        setUpSwapView()
    }

    private fun setUpSwapView() {
        val isSwap = PreferencesUtils.getBoolean(KeyViewCenter.KEY_SWAP_VIEW, false)
        if (!isSwap) {
            binding.layoutChooseCenter.txtNotification.text = getString(R.string.notifications)
            binding.layoutChooseCenter.txtControl.text = getString(R.string.controls)
        } else {
            binding.layoutChooseCenter.txtNotification.text = getString(R.string.controls)
            binding.layoutChooseCenter.txtControl.text = getString(R.string.notifications)
        }
    }

    private fun loadAds() {
        AdmobNativeAdView.getNativeAd(this, R.layout.native_admob_item_home, object : AdmobNativeAdView.NativeAdListener {
            override fun onError() {}
            override fun onLoaded(nativeAd: AdmobNativeAdView?) {
                nativeAd?.let {
                    if (it.parent != null) {
                        (it.parent as ViewGroup).removeView(it)
                    }
                    binding.adsView1.addView(it)
                }
            }

            override fun onClickAd() {
//                TODO("Not yet implemented")
            }
        })

        AdmobNativeAdView.getNativeAd(this, R.layout.native_admob_item_home, object : AdmobNativeAdView.NativeAdListener {
            override fun onError() {}
            override fun onLoaded(nativeAd: AdmobNativeAdView?) {
                nativeAd?.let {
                    if (it.parent != null) {
                        (it.parent as ViewGroup).removeView(it)
                    }
                    binding.adsView2.addView(it)
                }
            }

            override fun onClickAd() {
//                TODO("Not yet implemented")
            }
        })
    }

    private fun initListener() {
        binding.layoutStateApp.switchStatusApp.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    if (!isNotificationServiceRunning(this)
                    ) {
                        startActivityPermission()
                    } else {
                        checkSetPermissionPhone()
//                        startAccessibilitySetting()
                    }
                } else {
                    checkSetPermissionPhone()
                }
                binding.layoutStateApp.switchStatusApp.isChecked = false
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val intent = Intent("ACTION_DISABLE_ACCESSIBILITY_SERVICE")
                    binding.layoutStateApp.txtStateApp.text = resources.getString(R.string.turn_off)
                    sendBroadcast(intent)
                } else {
                    binding.layoutStateApp.switchStatusApp.isChecked = true
                    startAccessibilitySetting()
                }
            }
        }
        // arrange
        binding.layoutArrange.layoutIconShape.setOnClickListener {
            val dialogIconShape = DialogIconShape(this)
            dialogIconShape.listenerClickDone = {
                binding.layoutArrange.icShape.setImageResource(it)
            }
            dialogIconShape.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogIconShape.show()
        }
        // layout color
        binding.layoutColor.layoutBackground.setOnClickListener {
            image = PreferencesUtils.getInteger(BACKGROUND, R.drawable.ic_background_image)
            content = PreferencesUtils.getString(
                BACKGROUND_CONTENT,
                resources.getString(R.string.blur_image)
            )
            blur = PreferencesUtils.getInteger(OPACITY, 50)
            transparent =
                PreferencesUtils.getInteger(resources.getString(R.string.transparent_amount), 50)
            backgroundModel = PreferencesUtils.getString(
                BACKGROUND_MODEL,
                "null"
            )
            dialogBackground = DialogBackground(this)
            dialogBackground?.listenerClickOK = {
                binding.layoutColor.icBackground.setImageResource(
                    PreferencesUtils.getInteger(
                        BACKGROUND, R.drawable.ic_background_image
                    )
                )
                binding.layoutColor.txtContentBackground.text = PreferencesUtils.getString(
                    BACKGROUND_CONTENT, resources.getString(R.string.blur_image)
                )
                if (filePath != null && PreferencesUtils.getInteger(
                        BACKGROUND) == R.drawable.ic_background_image && PreferencesUtils.getString(
                        BACKGROUND_MODEL, "null"
                    ) == this.resources.getString(R.string.custom_photo)
                ) {
                    PreferencesUtils.putString(PATH_IMAGE, filePath)
                    sendBroadcastCrop()
                }
                if (PreferencesUtils.getInteger(BACKGROUND) == R.drawable.ic_backround_live_blur) {
                    if (!PreferencesUtils.getBoolean(
                            resources.getString(R.string.MEDIA_PROJECTION_ENABLE),
                            false
                        )
                    ) {
                        startScreenCapture()
                    }
                }
            }
            dialogBackground?.listenerClickCancel = {
                binding.layoutColor.icBackground.setImageResource(image)
                binding.layoutColor.txtContentBackground.text = content
                PreferencesUtils.putInteger(BACKGROUND, image)
                PreferencesUtils.putString(BACKGROUND_CONTENT, content)
                PreferencesUtils.putString(BACKGROUND_MODEL, backgroundModel)
                PreferencesUtils.putInteger(OPACITY, blur)
                PreferencesUtils.putInteger(
                    resources.getString(R.string.transparent_amount),
                    transparent
                )
                PreferencesUtils.putString(PATH_CROP, null)
                PreferencesUtils.putString(PATH_IMAGE, null)
            }
            dialogBackground?.listenerRequestPermission = {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_CODE_READ_EXTERNAL_STORAGE
                    )
                }

            }
            dialogBackground?.listenerChosePickImage = {
                try {
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(
                        Intent.createChooser(intent, "Select Picture"),
                        CODE_SELECT_PICKTURE
                    )
                } catch (e: Exception) {
                }
            }
            dialogBackground?.listenerClickColorPicker = {
                ColorPickerDialog.newBuilder()
                    .setDialogTitle(R.string.choose_color)
                    .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                    .setAllowPresets(false)
                    .setDialogId(1)
                    .setAllowCustom(false)
                    .setSelectedButtonText(R.string.done)
                    .setPresetsButtonText(R.string.cancel)
                    .setColor(Color.parseColor(PreferencesUtils.getString(COLOR, "#0094FF")))
                    .show(this)
            }
            dialogBackground?.listenerChangeBlurLive = {
                MyWindowManager.intentData?.let {
                    it.action =
                        (resources.getString(R.string.action_screen_shot))
                    sendBroadcast(it)
                }
            }
            dialogBackground?.listenerChangeBlurCustomPhoto = {
                sendBroadcastCrop()
            }
            //
            dialogBackground?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogBackground?.show()
        }

        //Rate
        binding.layoutRate.root.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            startActivity(intent)
        }
        //
        listenerBackgroundColor()
        listenerSelectedColor()
        listenerTextColor()
        listenerSetAllDefault()
        listenerIconColor()
        listenerSwitchShowData()
        listenerSwitchDataTrafficToday()
        listenerUseResetData()
        listenerWarningData()
        listenerDimmerColor()
        listenerChooseCenter()
    }

    private fun listenerChooseCenter() {
        binding.layoutChooseCenter.layoutDisableControl.setOnClickListener {
            enableControl()
        }
        //
        binding.layoutChooseCenter.layoutActionControl.setOnClickListener {
            if (binding.layoutChooseCenter.cbControl.isChecked) {
                disableControl()
            } else {
                enableControl()
            }
        }
        //
        binding.layoutChooseCenter.layoutDisableNoti.setOnClickListener {
            enableNotification()
        }
        //
        binding.layoutChooseCenter.layoutActionNotify.setOnClickListener {
            if (binding.layoutChooseCenter.cbNoti.isChecked) {
                disableNotification()
            } else {
                enableNotification()
            }
        }
        //
        binding.layoutChooseCenter.imgEnableControl.setOnClickListener {
            disableControl()
        }
        //
        binding.layoutChooseCenter.imgEnableNoti.setOnClickListener {
            disableNotification()
        }
        //
        binding.layoutChooseCenter.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                 TODO("Not yet implemented")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                 TODO("Not yet implemented")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    viewModel.setArea(it.progress)
                }
            }
        })
        // swap
        binding.layoutChooseCenter.icSwap.setOnClickListener {
            val isSwap = PreferencesUtils.getBoolean(KeyViewCenter.KEY_SWAP_VIEW, false)
            PreferencesUtils.putBoolean(KeyViewCenter.KEY_SWAP_VIEW, !isSwap)
            viewModel.setArea(binding.layoutChooseCenter.seekbar.progress)
            setUpSwapView()
        }
    }

    private fun enableControl() {
        PreferencesUtils.putBoolean(KeyViewCenter.KEY_VIEW_CONTROL, true)
        binding.layoutChooseCenter.cbControl.isChecked = true
        binding.layoutChooseCenter.imgEnableControl.visibility = View.VISIBLE
        binding.layoutChooseCenter.layoutDisableControl.visibility = View.GONE
    }

    private fun disableControl() {
        PreferencesUtils.putBoolean(KeyViewCenter.KEY_VIEW_CONTROL, false)
        binding.layoutChooseCenter.cbControl.isChecked = false
        binding.layoutChooseCenter.imgEnableControl.visibility = View.GONE
        binding.layoutChooseCenter.layoutDisableControl.visibility = View.VISIBLE
        if (!binding.layoutChooseCenter.cbNoti.isChecked) {
            PreferencesUtils.putBoolean(KeyViewCenter.KEY_VIEW_NOTIFICATION, true)
            binding.layoutChooseCenter.cbNoti.isChecked = true
            binding.layoutChooseCenter.imgEnableNoti.visibility = View.VISIBLE
            binding.layoutChooseCenter.layoutDisableNoti.visibility = View.GONE
        }
    }

    private fun enableNotification() {
        PreferencesUtils.putBoolean(KeyViewCenter.KEY_VIEW_NOTIFICATION, true)
        binding.layoutChooseCenter.cbNoti.isChecked = true
        binding.layoutChooseCenter.imgEnableNoti.visibility = View.VISIBLE
        binding.layoutChooseCenter.layoutDisableNoti.visibility = View.GONE
    }

    private fun disableNotification() {
        PreferencesUtils.putBoolean(KeyViewCenter.KEY_VIEW_NOTIFICATION, false)
        binding.layoutChooseCenter.cbNoti.isChecked = false
        binding.layoutChooseCenter.imgEnableNoti.visibility = View.GONE
        binding.layoutChooseCenter.layoutDisableNoti.visibility = View.VISIBLE
        if (!binding.layoutChooseCenter.cbControl.isChecked) {
            PreferencesUtils.putBoolean(KeyViewCenter.KEY_VIEW_CONTROL, true)
            binding.layoutChooseCenter.cbControl.isChecked = true
            binding.layoutChooseCenter.imgEnableControl.visibility = View.VISIBLE
            binding.layoutChooseCenter.layoutDisableControl.visibility = View.GONE
        }
    }

    private fun listenerWarningData() {
        binding.layoutUseData.layoutWarning.setOnClickListener {
            val viewDialog = ViewDialogUseResetDate(this, null)
            viewDialog.binding.titleName.text = resources.getString(R.string.warning_at_gb)
            viewDialog.binding.numberPicker.minValue = 0
            viewDialog.binding.numberPicker.value =
                PreferencesUtils.getInteger(resources.getString(R.string.warning_gb), 0)
            viewDialog.binding.numberPicker.wrapSelectorWheel = false
            viewDialog.binding.numberPicker.maxValue = 100
            viewDialog.binding.btnCancel.setOnClickListener {
                dialogWarning.dismiss()
            }
            viewDialog.binding.btnCancel.setOnClickListener {
                PreferencesUtils.putInteger(
                    resources.getString(R.string.warning_gb),
                    viewDialog.binding.numberPicker.value
                )
                dialogWarning.dismiss()
            }
            dialogWarning.setCancelable(true)
            dialogWarning.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogWarning.setContentView(viewDialog.binding.root)
            dialogWarning.show()
        }
    }

    private fun listenerUseResetData() {
        binding.layoutUseData.btnResetDate.setOnClickListener {
            val viewDialog = ViewDialogUseResetDate(this, null)
            viewDialog.binding.titleName.text = resources.getString(R.string.set_up_date)
            viewDialog.binding.numberPicker.minValue = 0
            when (PreferencesUtils.getInteger(resources.getString(R.string.month)) + 1) {
                1 -> viewDialog.binding.numberPicker.maxValue = 31
                2 -> viewDialog.binding.numberPicker.maxValue = 29
                3 -> viewDialog.binding.numberPicker.maxValue = 31
                4 -> viewDialog.binding.numberPicker.maxValue = 30
                5 -> viewDialog.binding.numberPicker.maxValue = 31
                6 -> viewDialog.binding.numberPicker.maxValue = 30
                7 -> viewDialog.binding.numberPicker.maxValue = 31
                8 -> viewDialog.binding.numberPicker.maxValue = 30
                9 -> viewDialog.binding.numberPicker.maxValue = 31
                10 -> viewDialog.binding.numberPicker.maxValue = 30
                11 -> viewDialog.binding.numberPicker.maxValue = 31
                12 -> viewDialog.binding.numberPicker.maxValue = 31
            }
            viewDialog.binding.numberPicker.value =
                PreferencesUtils.getInteger(resources.getString(R.string.use_date_set_up), 1)
            viewDialog.binding.numberPicker.wrapSelectorWheel = false
            viewDialog.binding.btnOk.setOnClickListener {
                PreferencesUtils.putInteger(
                    resources.getString(R.string.use_date_set_up),
                    viewDialog.binding.numberPicker.value
                )
                dialogUseResetDate.dismiss()
            }
            viewDialog.binding.btnCancel.setOnClickListener {
                dialogUseResetDate.dismiss()
            }
            dialogUseResetDate.setCancelable(true)
            dialogUseResetDate.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogUseResetDate.setContentView(viewDialog.binding.root)
            dialogUseResetDate.show()
        }
    }

    private fun listenerSwitchDataTrafficToday() {
        binding.layoutUseData.switchStatusDataTrafficToday.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (!checkUsageAccess()) {
                    startActivityForResult(
                        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                        REQUEST_CODE_USAGE_ACCESS_SETTINGS
                    )
                    binding.layoutUseData.switchStatusDataTrafficToday.isChecked = false
                    PreferencesUtils.putBoolean(
                        resources.getString(R.string.stata_usage_day),
                        false
                    )
                } else {
                    PreferencesUtils.putLong(
                        resources.getString(R.string.total_usage_data_of_day),
                        totalData()
                    )
                    val intent = Intent(resources.getString(R.string.action_usage_data_of_day))
                    sendBroadcast(intent)
                    PreferencesUtils.putBoolean(resources.getString(R.string.stata_usage_day), true)
                }
            } else {
                PreferencesUtils.putBoolean(resources.getString(R.string.stata_usage_day), false)
                val intent = Intent(resources.getString(R.string.action_usage_data_of_day))
                sendBroadcast(intent)
            }
        }
    }

    private fun listenerSwitchShowData() {
        binding.layoutUseData.switchStatusShowData.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (!checkUsageAccess()) {
                    startActivityForResult(
                        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                        REQUEST_CODE_USAGE_ACCESS_SETTINGS
                    )
                    binding.layoutUseData.switchStatusShowData.isChecked = false
                    PreferencesUtils.putBoolean(STATUS_USAGE_DATA, false)
                } else {
                    PreferencesUtils.putLong(TOTAL_DATA_ON, totalData())
                    val intent = Intent(resources.getString(R.string.action_usage_data))
                    sendBroadcast(intent)
                    PreferencesUtils.putBoolean(STATUS_USAGE_DATA, true)
                }
            } else {
                PreferencesUtils.putBoolean(STATUS_USAGE_DATA, false)
                val intent = Intent(resources.getString(R.string.action_usage_data))
                sendBroadcast(intent)
            }
        }
    }

    private fun totalData(): Long {
        val received = TrafficStats.getTotalRxBytes() / (1024 * 1024)
        val send = TrafficStats.getTotalTxBytes() / (1024 * 1024)
        return received + send
    }

    private fun checkSetPermissionPhone() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            startActivityPermission()
        } else {
            startAccessibilitySetting()
        }
    }

    private fun startActivityPermission() {
        val intent = Intent(this, ActivityRequestPermission::class.java)
        startActivity(intent)
    }

    private fun isNotificationServiceRunning(context: Context): Boolean {
        return try {
            val contentResolver: ContentResolver = context.contentResolver
            val enabledNotificationListeners: String =
                Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            val packageName: String = context.packageName
            enabledNotificationListeners.contains(
                packageName
            )
        } catch (e: Exception) {
            true
        }
    }

    private fun checkUsageAccess(): Boolean {
        return try {
            val packageManager = applicationContext.packageManager
            val applicationInfo =
                packageManager.getApplicationInfo(applicationContext.packageName, 0)
            val appOpsManager =
                applicationContext.getSystemService(APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo.uid,
                applicationInfo.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun listenerDimmerColor() {
        binding.layoutColor.layoutDimmerColor.setOnClickListener {
            ColorPickerDialog.newBuilder()
                .setDialogTitle(R.string.choose_color)
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowPresets(false)
                .setDialogId(6)
                .setAllowCustom(false)
                .setSelectedButtonText(R.string.done)
                .setPresetsButtonText(R.string.cancel)
                .setColor(
                    Color.parseColor(
                        PreferencesUtils.getString(
                            resources.getString(R.string.DIMMER_COLOR),
                            "#FFFFFFFF"
                        )
                    )
                )
                .show(this)
        }
    }

    private fun listenerIconColor() {
        binding.layoutColor.layoutIconColor.setOnClickListener {
            ColorPickerDialog.newBuilder()
                .setDialogTitle(R.string.choose_color)
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowPresets(false)
                .setDialogId(5)
                .setAllowCustom(false)
                .setSelectedButtonText(R.string.done)
                .setPresetsButtonText(R.string.cancel)
                .setColor(
                    Color.parseColor(
                        PreferencesUtils.getString(
                            resources.getString(R.string.ICON_COLOR),
                            "#FFFFFF"
                        )
                    )
                )
                .show(this)
        }
    }

    private fun listenerSetAllDefault() {
        binding.layoutColor.layoutSetAllDefault.setOnClickListener {
            val viewDialog = ViewDiaLogResetDefault(this, null)
            viewDialog.binding.txtContent.text =
                resources.getString(R.string.describe_reset_default_color)
            viewDialog.binding.btnYes.setOnClickListener {
                PreferencesUtils.putInteger(BACKGROUND, R.drawable.ic_background_image)
                PreferencesUtils.putString(
                    BACKGROUND_MODEL,
                    "null"
                )
                PreferencesUtils.putInteger(OPACITY, 50)
                PreferencesUtils.putInteger(resources.getString(R.string.transparent_amount), 50)
                PreferencesUtils.putString(COLOR, "#0094FF")
                PreferencesUtils.putString(
                    BACKGROUND_CONTENT,
                    resources.getString(R.string.blur_image)
                )
                PreferencesUtils.putString(resources.getString(R.string.TEXT_COLOR), "#FFFFFF")
                PreferencesUtils.putString(SELECTED_COLOR, "#2C61CC")
                PreferencesUtils.putString(
                    BACKGROUND_COLOR,
                    resources.getString(R.string.color_4DFFFFFF)
                )
                PreferencesUtils.putString(resources.getString(R.string.ICON_COLOR), "#FFFFFF")
                PreferencesUtils.putString(resources.getString(R.string.DIMMER_COLOR), "#FFFFFFFF")
                sendBroadcast(Intent(resources.getString(R.string.action_background_color)))
                sendBroadcast(Intent(resources.getString(R.string.action_text_color)))
                sendBroadcast(Intent(resources.getString(R.string.action_blur_image)))
                sendBroadcast(Intent(resources.getString(R.string.action_icon_color)))
                sendBroadcast(Intent(resources.getString(R.string.action_dimmer_color)))
                setUpUiDefault()
                binding.layoutColor.icBackground.setImageResource(
                    PreferencesUtils.getInteger(
                        BACKGROUND,
                        R.drawable.ic_background_image
                    )
                )
                dialogResetDefault.dismiss()
            }
            viewDialog.binding.btnNo.setOnClickListener {
                dialogResetDefault.dismiss()
            }
            dialogResetDefault.setCancelable(true)
            dialogResetDefault.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogResetDefault.setContentView(viewDialog.binding.root)
            dialogResetDefault.show()
            //

        }
    }

    private fun listenerTextColor() {
        binding.layoutColor.layoutTextColor.setOnClickListener {
            ColorPickerDialog.newBuilder()
                .setDialogTitle(R.string.choose_color)
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowPresets(false)
                .setDialogId(4)
                .setAllowCustom(false)
                .setSelectedButtonText(R.string.done)
                .setPresetsButtonText(R.string.cancel)
                .setShowAlphaSlider(true)
                .setColor(
                    Color.parseColor(
                        PreferencesUtils.getString(
                            resources.getString(R.string.TEXT_COLOR),
                            "#FFFFFF"
                        )
                    )
                )
                .show(this)
        }
    }

    private fun listenerSelectedColor() {
        binding.layoutColor.layoutSelectedColor.setOnClickListener {
            ColorPickerDialog.newBuilder()
                .setDialogTitle(R.string.choose_color)
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowPresets(false)
                .setDialogId(3)
                .setAllowCustom(false)
                .setSelectedButtonText(R.string.done)
                .setPresetsButtonText(R.string.cancel)
                .setShowAlphaSlider(true)
                .setColor(
                    Color.parseColor(
                        PreferencesUtils.getString(
                            SELECTED_COLOR,
                            "#2C61CC"
                        )
                    )
                )
                .show(this)
        }
    }

    private fun listenerBackgroundColor() {
        binding.layoutColor.layoutBackgroundColor.setOnClickListener {
            ColorPickerDialog.newBuilder()
                .setDialogTitle(R.string.choose_color)
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowPresets(false)
                .setDialogId(2)
                .setAllowCustom(false)
                .setSelectedButtonText(R.string.done)
                .setPresetsButtonText(R.string.cancel)
                .setShowAlphaSlider(true)
                .setColor(
                    Color.parseColor(
                        PreferencesUtils.getString(
                            BACKGROUND_COLOR,
                            resources.getString(R.string.color_4DFFFFFF)
                        )
                    )
                )
                .show(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            PreferencesUtils.putInteger(BACKGROUND, image)
            PreferencesUtils.putString(BACKGROUND_CONTENT, content)
            dialogBackground?.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CODE_SELECT_PICKTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val selectedImageUri = data?.data
                    try {
                        // OI FILE Manager
                        val fileManagerString = selectedImageUri?.path

                        // MEDIA GALLERY
                        val selectedImagePath = getPath(this, selectedImageUri)

                        when {
                            selectedImagePath != null -> {
                                filePath = selectedImagePath
                            }
                            fileManagerString != null -> {
                                filePath = fileManagerString
                            }
                            else -> {
                                Toast.makeText(
                                    this, "Don't find image",
                                    Toast.LENGTH_LONG
                                ).show()

                            }
                        }
                        filePath?.let {
                            UCrop.of(
                                Uri.fromFile(File(it)),
                                Uri.fromFile(File(this.cacheDir, "abc.png"))
                            )
                                .useSourceImageAspectRatio()
                                .withAspectRatio(9F, 16F)
                                .start(this)
                        }
                    } catch (e: Exception) {

                    }
                } else {
                    PreferencesUtils.putInteger(BACKGROUND, image)
                    PreferencesUtils.putString(BACKGROUND_CONTENT, content)
                    PreferencesUtils.putString(BACKGROUND_MODEL, backgroundModel)
                    dialogBackground?.setUpLayout()
                }
            }
            REQUEST_MEDIA_PROJECTION -> {
                if (resultCode != RESULT_OK) {
                    return
                }
                mResultCode = resultCode
                mResultData = data
                mResultData?.action = (resources.getString(R.string.action_screen_shot))
                PreferencesUtils.putBoolean(
                    resources.getString(R.string.MEDIA_PROJECTION_ENABLE),
                    true
                )
                mResultData?.putExtra("RESULT_CODE", mResultCode)
                sendBroadcast(mResultData)
            }
            UCrop.REQUEST_CROP -> {
                if (resultCode == RESULT_OK) {
                    val resultUri = data?.let { UCrop.getOutput(it) }
                    mDataCrop = data
                    mDataCrop?.action = resources.getString(R.string.action_blur_image)
                    sendBroadcast(mDataCrop)
                } else {
                    PreferencesUtils.putInteger(BACKGROUND, image)
                    PreferencesUtils.putString(BACKGROUND_CONTENT, content)
                    PreferencesUtils.putString(BACKGROUND_MODEL, backgroundModel)
                    dialogBackground?.setUpLayout()
                }
            }
        }
    }

    private fun sendBroadcastCrop() {
        val intent = Intent(resources.getString(R.string.action_blur_image))
        sendBroadcast(intent)
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        when (dialogId) {
            1 -> {
                when (Integer.toHexString(color).length) {
                    8 -> {
                        PreferencesUtils.putString(COLOR, "#${Integer.toHexString(color)}")
                    }
                    7 -> {
                        PreferencesUtils.putString(COLOR, "#0${Integer.toHexString(color)}")
                    }
                    6 -> {
                        PreferencesUtils.putString(COLOR, "#00${Integer.toHexString(color)}")
                    }
                    5 -> {
                        PreferencesUtils.putString(COLOR, "#000${Integer.toHexString(color)}")
                    }
                    else -> {
                        PreferencesUtils.putString(COLOR, "#FFFFFF")
                    }
                }
                val intent = Intent(resources.getString(R.string.action_choose_color))
                sendBroadcast(intent)
            }
            2 -> {
                when (Integer.toHexString(color).length) {
                    8 -> {
                        PreferencesUtils.putString(
                            BACKGROUND_COLOR, "#${Integer.toHexString(color)}")
                    }
                    7 -> {
                        PreferencesUtils.putString(
                            BACKGROUND_COLOR, "#0${Integer.toHexString(color)}")
                    }
                    6 -> {
                        PreferencesUtils.putString(
                            BACKGROUND_COLOR, "#00${Integer.toHexString(color)}")
                    }
                    5 -> {
                        PreferencesUtils.putString(
                            BACKGROUND_COLOR, "#000${Integer.toHexString(color)}")
                    }
                    else -> {
                        PreferencesUtils.putString(
                            BACKGROUND_COLOR, "#FFFFFF")
                    }
                }

                binding.layoutColor.icBackgroundColor.setColorFilter(
                    Color.parseColor(
                        PreferencesUtils.getString(
                            BACKGROUND_COLOR,
                            resources.getString(R.string.color_4DFFFFFF)
                        )
                    )
                )
                val intent = Intent(resources.getString(R.string.action_background_color))
                sendBroadcast(intent)
            }
            3 -> {
                when (Integer.toHexString(color).length) {
                    8 -> {
                        PreferencesUtils.putString(
                            SELECTED_COLOR, "#${Integer.toHexString(color)}")
                    }
                    7 -> {
                        PreferencesUtils.putString(
                            SELECTED_COLOR, "#0${Integer.toHexString(color)}")
                    }
                    6 -> {
                        PreferencesUtils.putString(
                            SELECTED_COLOR, "#00${Integer.toHexString(color)}")
                    }
                    5 -> {
                        PreferencesUtils.putString(
                            SELECTED_COLOR, "#000${Integer.toHexString(color)}")
                    }
                    else -> {
                        PreferencesUtils.putString(SELECTED_COLOR, "#FFFFFF")
                    }
                }
                binding.layoutColor.icSelectedColor.setColorFilter(
                    Color.parseColor(
                        PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")
                    )
                )
                val intent = Intent(resources.getString(R.string.action_background_color))
                sendBroadcast(intent)
            }
            4 -> {
                when (Integer.toHexString(color).length) {
                    8 -> {
                        PreferencesUtils.putString(
                            resources.getString(R.string.TEXT_COLOR),
                            "#${Integer.toHexString(color)}")
                    }
                    7 -> {
                        PreferencesUtils.putString(
                            resources.getString(R.string.TEXT_COLOR),
                            "#0${Integer.toHexString(color)}")
                    }
                    6 -> {
                        PreferencesUtils.putString(
                            resources.getString(R.string.TEXT_COLOR),
                            "#00${Integer.toHexString(color)}")
                    }
                    5 -> {
                        PreferencesUtils.putString(
                            resources.getString(R.string.TEXT_COLOR),
                            "#000${Integer.toHexString(color)}")
                    }
                    else -> {
                        PreferencesUtils.putString(
                            resources.getString(R.string.TEXT_COLOR),
                            "#FFFFFF")
                    }
                }
                binding.layoutColor.icShapeTextColor.setColorFilter(
                    Color.parseColor(
                        PreferencesUtils.getString(
                            resources.getString(R.string.TEXT_COLOR),
                            "#FFFFFF"
                        )
                    )
                )
                val intent = Intent(resources.getString(R.string.action_text_color))
                sendBroadcast(intent)
            }
            5 -> {
                when (Integer.toHexString(color).length) {
                    8 -> {
                        PreferencesUtils.putString(
                            resources.getString(R.string.ICON_COLOR),
                            "#${Integer.toHexString(color)}")
                    }
                    7 -> {
                        PreferencesUtils.putString(
                            resources.getString(R.string.ICON_COLOR),
                            "#0${Integer.toHexString(color)}")
                    }
                    6 -> {
                        PreferencesUtils.putString(
                            resources.getString(R.string.ICON_COLOR),
                            "#00${Integer.toHexString(color)}")
                    }
                    5 -> {
                        PreferencesUtils.putString(
                            resources.getString(R.string.ICON_COLOR),
                            "#000${Integer.toHexString(color)}")
                    }
                    else -> {
                        PreferencesUtils.putString(
                            resources.getString(R.string.ICON_COLOR),
                            "#FFFFFF")
                    }
                }
                binding.layoutColor.icIconColor.setColorFilter(
                    Color.parseColor(
                        PreferencesUtils.getString(
                            resources.getString(R.string.ICON_COLOR),
                            "#FFFFFF"
                        )
                    )
                )
                val intent = Intent(resources.getString(R.string.action_icon_color))
                sendBroadcast(intent)
            }
            6 -> {
                when (Integer.toHexString(color).length) {
                    8 -> {
                        PreferencesUtils.putString(
                            resources.getString(R.string.DIMMER_COLOR),
                            "#${Integer.toHexString(color)}")
                    }
                    7 -> {
                        PreferencesUtils.putString(
                            resources.getString(R.string.DIMMER_COLOR),
                            "#0${Integer.toHexString(color)}")
                    }
                    6 -> {
                        PreferencesUtils.putString(
                            resources.getString(R.string.DIMMER_COLOR),
                            "#00${Integer.toHexString(color)}")
                    }
                    5 -> {
                        PreferencesUtils.putString(
                            resources.getString(R.string.DIMMER_COLOR),
                            "#000${Integer.toHexString(color)}")
                    }
                    else -> {
                        PreferencesUtils.putString(
                            resources.getString(R.string.DIMMER_COLOR),
                            "#FFFFFF")
                    }
                }
                binding.layoutColor.icIconDimmerColor.setColorFilter(
                    Color.parseColor(
                        PreferencesUtils.getString(
                            resources.getString(R.string.DIMMER_COLOR),
                            "#FFFFFF"
                        )
                    )
                )

                val intent = Intent(resources.getString(R.string.action_dimmer_color))
                sendBroadcast(intent)
            }
        }
    }

    override fun onDialogDismissed(dialogId: Int) {

    }

    private fun setUpUiDefault() {
        binding.layoutColor.icBackgroundColor.setColorFilter(
            Color.parseColor(
                PreferencesUtils.getString(
                    BACKGROUND_COLOR,
                    resources.getString(R.string.color_4DFFFFFF)
                )
            )
        )
        //
        binding.layoutColor.icSelectedColor.setColorFilter(
            Color.parseColor(
                PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")
            )
        )
        //
        binding.layoutColor.icShapeTextColor.setColorFilter(
            Color.parseColor(
                PreferencesUtils.getString(
                    resources.getString(R.string.TEXT_COLOR),
                    "#FFFFFF"
                )
            )
        )
        //
        binding.layoutColor.icIconColor.setColorFilter(
            Color.parseColor(
                PreferencesUtils.getString(
                    resources.getString(R.string.ICON_COLOR),
                    "#FFFFFF"
                )
            )
        )
        //
        binding.layoutColor.icIconDimmerColor.setColorFilter(
            Color.parseColor(
                PreferencesUtils.getString(
                    resources.getString(R.string.DIMMER_COLOR),
                    "#FFFFFF"
                )
            )
        )
    }

    private fun checkStateApp() {
        binding.layoutStateApp.switchStatusApp.isChecked =
            MyAccessibilityService.isRunning
        if (binding.layoutStateApp.switchStatusApp.isChecked) {
            binding.layoutStateApp.txtStateApp.text = resources.getString(R.string.turn_on)
            FlurryAnalytics.logEvent(EventTracking.CONTROL_CENTER, "turn_on")
        } else {
            binding.layoutStateApp.txtStateApp.text = resources.getString(R.string.turn_off)
            FlurryAnalytics.logEvent(EventTracking.CONTROL_CENTER, "turn_off")
        }
    }

    private fun startScreenCapture() {
        // This initiates a prompt dialog for the user to confirm screen projection.
        startActivityForResult(
            mMediaProjectionManager?.createScreenCaptureIntent(),
            REQUEST_MEDIA_PROJECTION
        )
    }

    private fun startAccessibilitySetting() {
        var intent = Intent("com.samsung.accessibility.installed_service")
        if (intent.resolveActivity(packageManager) == null) {
            intent = Intent("android.settings.ACCESSIBILITY_SETTINGS")
        }
        val bundle = Bundle()
        val str = packageName + "/" + MyAccessibilityService::class.java.getName()
        bundle.putString(":settings:fragment_args_key", str)
        intent.putExtra(":settings:fragment_args_key", str)
        intent.putExtra(":settings:show_fragment_args", bundle)
        try {
            startActivity(intent)
            Toast.makeText(
                applicationContext,
                resources.getString(R.string.find_app_here, resources.getString(R.string.app_name)),
                Toast.LENGTH_SHORT
            ).show()
        } catch (unused: java.lang.Exception) {
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, Splash::class.java)
        startActivity(intent)
    }

    override fun onRestart() {
        super.onRestart()
        checkStateApp()
    }
}