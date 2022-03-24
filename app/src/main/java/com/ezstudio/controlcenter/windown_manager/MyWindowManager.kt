package com.ezstudio.controlcenter.windown_manager

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.net.wifi.WifiManager
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.*
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ezstudio.controlcenter.MyGroupView
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.activity.BackGroundActivity
import com.ezstudio.controlcenter.activity.Splash
import com.ezstudio.controlcenter.activity.SystemShadeActivity
import com.ezstudio.controlcenter.broadcast.*
import com.ezstudio.controlcenter.common.EventTracking
import com.ezstudio.controlcenter.common.KeyBroadCast
import com.ezstudio.controlcenter.common.KeyViewCenter
import com.ezstudio.controlcenter.databinding.LayoutViewHideBinding
import com.ezstudio.controlcenter.databinding.LayoutViewNotificationBinding
import com.ezstudio.controlcenter.databinding.WindownManagerBinding
import com.ezstudio.controlcenter.homewatcher.HomeWatcher
import com.ezstudio.controlcenter.interfaces.OnHomePressedListener
import com.ezstudio.controlcenter.model.ItemNotification
import com.ezstudio.controlcenter.screenshot.ScreenShotHelper
import com.ezstudio.controlcenter.service.MyAccessibilityService
import com.ezstudio.controlcenter.service.SingleSettingStage
import com.ezstudio.controlcenter.viewmodel.AreaModel
import com.ezstudio.controlcenter.widget.BoxedVertical
import com.ezstudio.controlcenter.widget.ViewDialogOpenSettings
import com.ezteam.baseproject.extensions.getDisplayMetrics
import com.ezteam.baseproject.utils.PreferencesUtils
import com.google.android.gms.ads.ez.analytics.FlurryAnalytics
import com.google.android.gms.ads.ez.nativead.AdmobNativeAdView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.everything.android.ui.overscroll.IOverScrollDecor
import me.everything.android.ui.overscroll.IOverScrollState
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.wysaid.common.SharedContext
import org.wysaid.nativePort.CGEImageHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.text.MessageFormat
import kotlin.math.abs
import kotlin.math.max

@KoinApiExtension
class MyWindowManager(var context: Context) : KoinComponent {
    private val CLICK_ACTION_THRESHOLD = 150
    private val viewModel by inject<AreaModel>()

    companion object {
        var intentData: Intent? = null
    }

    lateinit var binding: WindownManagerBinding
    lateinit var bindingNoti: LayoutViewNotificationBinding
    private lateinit var bindingViewHide: LayoutViewHideBinding
    lateinit var windowManager: WindowManager
    private lateinit var wifiManager: WifiManager
    private var dragY: Float = 0F
    private var countTouch = 0
    private var startX = 0F
    private var startY = 0F
    private var endX = 0F
    private var endY = 0F
    private var alpha = 0F
    private var isShow = false
    private var isViewHide = false
    private var isFullView = false
    private var marginX = 0
    private var dragScroll = 0F
    private var actionUp = true
    private var isHiding = false
    private var alphaImageBackground = 1F
    private var dragScrollFlash = 0F
    private var checkMove1 = 0F
    private var checkMove2 = 0F
    private var countMove = 0
    private var point = 0F
    private var isSrcollFirst = true
    private var isRemovedNotify = false
    private var isShowNotify = false
    private var isShowControl = false
    private lateinit var alertDialogBrightness: Dialog
    private lateinit var countDownTimer: CountDownTimer
    private var countDownTimerALongClick: CountDownTimer? = null
    private var mVertOverScrollEffect: IOverScrollDecor? = null
    val broadCastWifi = BroadCastChangeWifi()
    val broadCastBlurImage = BroadCastBlurImage()
    val broadCastBackgroundColor = BroadCastBackgroundColor()
    val broadCastTextColor = BroadCastTextColor()
    val broadCastUsageData = BroadCastUsageData()
    val broadCastScreenShot = BroadCastScreenShot()
    val broadCastListenerNotification = BroadCastListenerNotification()
    val broadCastTimeChange = BroadCastTimeChange()
    var isFirstItem = true
    private var screenShot: ScreenShotHelper? = null
    private var isChangingBrightness = false
    private val animClickIcon = AnimationUtils.loadAnimation(context, R.anim.anim_click_icon)
    private val ICON_SHAPE = "ICON_SHAPE"
    private val BACKGROUND_COLOR = "BACKGROUND_COLOR"
    private val SELECTED_COLOR = "SELECTED_COLOR"
    private val TOTAL_DATA_ON = "TOTAL_DATA_ON"
    private val STATUS_USAGE_DATA = "STATUS_USAGE_DATA"
    private val BACKGROUND = "BACKGROUND"
    private val OPACITY = "OPACITY"

    init {
        viewLayout()
        initListener()
        initData()
        onSetting()
        onSettingWifi()
        onSettingBluetooth()
        onSettingMobileData()
        callBackViewTaskbar()
        callBackViewIconCenter()
        callBackViewIconSecondLine()
        callBackViewIconHide()
        callBackViewControlCenter()
    }

    private fun initData() {
        viewModel.areaLiveData.observeForever {
            setAreaViewCenter(it)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        bindingNoti.layoutRclNotification.binding.icClearAll.setOnClickListener {
            it.startAnimation(animClickIcon)
            context.sendBroadcast(Intent(KeyBroadCast.ACTION_NOTIFICATION_CLEAR_ALL))
        }
        // scroll
        mVertOverScrollEffect?.setOverScrollUpdateListener { decor, state, offset ->
//            if (oldOffSet != 0F) {
            when (state) {
                IOverScrollState.STATE_IDLE -> {
                    isRemovedNotify = false
                }
                IOverScrollState.STATE_DRAG_START_SIDE -> {
                    isRemovedNotify = false
                }
                IOverScrollState.STATE_DRAG_END_SIDE -> {
                    isRemovedNotify = false
                    if (!isSrcollFirst) {
                        bindingNoti.layoutNotification.alpha =
                            1 - ((max(offset, -120F) * -1) / 120F)
                        windowManager.updateViewLayout(bindingNoti.root, setupLayout(true))
                    }
                }
                IOverScrollState.STATE_BOUNCE_BACK -> {
                    if (!isSrcollFirst && !isRemovedNotify) {
                        val alpha = bindingNoti.layoutNotification.alpha
                        bindingViewHide.txtTopManagerLeft.isEnabled = false
                        bindingNoti.layoutContentNotification.isEnabled = false
                        if (alpha > 0.5F) {
                            bindingNoti.layoutNotification.animate().alpha(1F).setDuration(100)
                                .start()
                            Handler().postDelayed({
                                bindingViewHide.txtTopManagerLeft.isEnabled = true
                                bindingNoti.layoutContentNotification.isEnabled = true
                            }, 100)
                        } else {
                            isRemovedNotify = true
                            bindingNoti.layoutNotification.animate().alpha(0F).setDuration(100)
                                .start()
                            Handler().postDelayed({
                                bindingViewHide.txtTopManagerLeft.isEnabled = true
                                bindingNoti.layoutContentNotification.isEnabled = true
                                try {
                                    windowManager.removeViewImmediate(bindingNoti.root)
                                } catch (e: IllegalArgumentException) {
                                }
                                windowManager.updateViewLayout(
                                    bindingViewHide.root,
                                    setupLayout(false)
                                )
                            }, 100)
                        }
                    }
                    isSrcollFirst = false
                }
            }
        }
        bindingNoti.layoutRclNotification.binding.rclNoti.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                isSrcollFirst = true
            }
        })
        //home watcher
        startHomeWatcher()
    }

    private fun startHomeWatcher() {
        val mHomeWatcher = HomeWatcher(context)
        mHomeWatcher.setOnHomePressedListener(object : OnHomePressedListener {
            override fun onHomePressed() {
                try {
                    if (isShowNotify) {
                        endWindownNoti()
                    } else if (isShowControl) {
                        endWindowManager()
                    }
                } catch (e: IllegalArgumentException) {

                }
            }

            override fun onHomeLongPressed() {
            }
        })
        mHomeWatcher.startWatch()
    }

    private fun registerBroadCastBlurImage() {
        broadCastBlurImage.binding = binding
        broadCastBlurImage.bindingNoti = bindingNoti
        val intent = IntentFilter(context.resources.getString(R.string.action_blur_image))
        context.registerReceiver(broadCastBlurImage, intent)
    }

    private fun registerBroadCastBackgroundColor() {
        broadCastBackgroundColor.binding = binding
        broadCastBackgroundColor.windowManager = this
        val intent = IntentFilter(context.resources.getString(R.string.action_background_color))
        context.registerReceiver(broadCastBackgroundColor, intent)
    }

    private fun registerBroadCastTextColor() {
        broadCastTextColor.binding = binding
        val intent = IntentFilter()
        intent.addAction(context.resources.getString(R.string.action_text_color))
        intent.addAction(context.resources.getString(R.string.action_icon_color))
        intent.addAction(context.resources.getString(R.string.action_dimmer_color))
        context.registerReceiver(broadCastTextColor, intent)
    }

    private fun registerBroadCastUsageData() {
        broadCastUsageData.binding = binding
        val intent = IntentFilter()
        intent.addAction(context.resources.getString(R.string.action_usage_data))
        intent.addAction(context.resources.getString(R.string.action_usage_data_of_day))
        context.registerReceiver(broadCastUsageData, intent)
    }

    private fun registerBroadCastScreenShot() {
        broadCastScreenShot.myWindowManager = this
        val intent = IntentFilter()
        intent.addAction(context.resources.getString(R.string.action_screen_shot))
        context.registerReceiver(broadCastScreenShot, intent)
    }

    fun updateViewUseAccessibility(
        doingAction: MyAccessibilityService.DoingAction,
        action: Boolean? = null
    ) {
        when (doingAction) {
            MyAccessibilityService.DoingAction.BlueFilter -> {
                binding.layoutControlsSecondsLine.setStageViewNightLight()
            }
            MyAccessibilityService.DoingAction.AutoRotate -> {
                binding.layoutControlsSecondsLine.setStageAutoRotation()
            }
            MyAccessibilityService.DoingAction.DarkTheme -> {
                binding.layoutControlsSecondsLine.setStageDarkTheme()
            }
            MyAccessibilityService.DoingAction.Hotspot -> {
                binding.layoutControlsSecondsLine.setStageHotspost()
            }
            MyAccessibilityService.DoingAction.DataSaver -> {
                binding.layoutIconCenterHide.setStageDataSaver()
            }
            MyAccessibilityService.DoingAction.ScreenTransmission -> {
                binding.layoutIconCenterHide.setStageScreenTransmission()
            }
            MyAccessibilityService.DoingAction.NFC -> {
                binding.layoutIconCenterHide.setStageNFC(action)
            }
            MyAccessibilityService.DoingAction.Location -> {
                binding.layoutIconCenterHide.setStageLocation()
            }
            MyAccessibilityService.DoingAction.BatterySaver -> {
                binding.layoutIconControls.setStageBatterySaver()
            }
            MyAccessibilityService.DoingAction.AirPlane -> {
                binding.layoutIconControls.setStageAirPlane()
            }
            MyAccessibilityService.DoingAction.Wifi -> {
                binding.layoutBtnFist.setStageWifi(action)
            }
            MyAccessibilityService.DoingAction.FirstInit -> {
                SingleSettingStage.getInstance().let {
                    binding.layoutControlsSecondsLine.setStageViewNightLight()
                    binding.layoutControlsSecondsLine.setStageAutoRotation()
                    binding.layoutControlsSecondsLine.setStageDarkTheme()
                    binding.layoutControlsSecondsLine.setStageHotspost()
                    binding.layoutIconCenterHide.setStageDataSaver()
                    binding.layoutIconCenterHide.setStageScreenTransmission()
                    binding.layoutIconCenterHide.setStageNFC()
                    binding.layoutIconCenterHide.setStageLocation()
                    binding.layoutIconControls.setStageBatterySaver()
                    binding.layoutIconControls.setStageAirPlane()
                }
            }
        }
    }

    private fun registerNotification() {
        broadCastListenerNotification.adapter =
            bindingNoti.layoutRclNotification.adapterNotification
        broadCastListenerNotification.listDataNotification =
            bindingNoti.layoutRclNotification.listNotification
        broadCastListenerNotification.listener = {
            checkEmptyNotification(bindingNoti.layoutRclNotification.listNotification)
        }
        val intent = IntentFilter()
        intent.addAction(KeyBroadCast.KEY_PULL_NOTIFICATION)
        context.registerReceiver(broadCastListenerNotification, intent)
    }

    private fun registerTime() {
        broadCastTimeChange.binding = bindingNoti.layoutTopNotification.binding
        val intent = IntentFilter()
        intent.addAction("android.intent.action.TIME_TICK")
        context.registerReceiver(broadCastTimeChange, intent)
    }

    private fun checkEmptyNotification(listNotification: MutableList<ItemNotification>) {
        if (listNotification.size == 0) {
            bindingNoti.layoutRclNotification.binding.txtNoNotifications.visibility = View.VISIBLE
        } else {
            bindingNoti.layoutRclNotification.binding.txtNoNotifications.visibility = View.GONE
        }
    }

    private fun startCountDownTimer() {
        countDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                val intent = Intent(context.resources.getString(R.string.action_usage_data))
                context.sendBroadcast(intent)
                this.start()
            }
        }
    }

    private fun blurBackGround(imageView: AppCompatImageView, context: Context) {
        val bitmap = BitmapFactory.decodeResource(
            context.getResources(),
            R.drawable.background_default
        )
//                    binding.imgBackgroundColor.setImageBitmap
        CoroutineScope(Dispatchers.Main).launch {
            getBlurImageFromBitmap(
                bitmap,
                (PreferencesUtils.getInteger(OPACITY, 50)) / 10F,
                (PreferencesUtils.getInteger(
                    context.resources.getString(R.string.transparent_amount),
                    50
                ) / 100F) * 1.5F
            )?.let {
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
                imageView.setImageBitmap(it)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun viewLayout() {
        wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        val myGroup = MyGroupView(context)
        // layoutCenter
        binding = WindownManagerBinding.inflate(LayoutInflater.from(context), myGroup, false)
        // layout notification
        bindingNoti =
            LayoutViewNotificationBinding.inflate(LayoutInflater.from(context), null, false)
        mVertOverScrollEffect = OverScrollDecoratorHelper
            .setUpOverScroll(
                bindingNoti.layoutRclNotification.binding.rclNoti,
                OverScrollDecoratorHelper.ORIENTATION_VERTICAL
            )

        bindingNoti.layoutTopNotification.listenerEndWindow = {
            endWindownNoti()
        }
        context.sendBroadcast(Intent(KeyBroadCast.ACTION_RELOAD))
        //
        binding.view.height =
            (context.resources.displayMetrics.heightPixels * (0.115F + 0.12)).toInt()
        stateBrightnessMode()
        changeBrightness()
        registerBroadCastBlurImage()
        registerBroadCastBackgroundColor()
        registerBroadCastTextColor()
        registerBroadCastUsageData()
        registerBroadCastScreenShot()
        registerNotification()
        registerTime()

        alertDialogBrightness = Dialog(context)
        // blur background
        binding.imgBackgroundColor.setImageResource(R.drawable.background_default)
        binding.imgBackgroundColor.post { blurBackGround(binding.imgBackgroundColor, context) }
        //noti
        bindingNoti.imgBackgroundNotification.setImageResource(R.drawable.background_default)
        bindingNoti.imgBackgroundNotification.post {
            blurBackGround(
                bindingNoti.imgBackgroundNotification,
                context
            )
        }
        //
        intIconShape()
        setupTextColor(
            PreferencesUtils.getString(
                context.resources.getString(R.string.TEXT_COLOR),
                "#FFFFFF"
            )
        )
        setUpColorIcon(
            PreferencesUtils.getString(
                context.resources.getString(R.string.ICON_COLOR),
                "#FFFFFF"
            )
        )
        setUpDimmerColor(
            PreferencesUtils.getString(
                context.resources.getString(R.string.DIMMER_COLOR),
                "#FFFFFFFF"
            )
        )
        binding.layoutContent.alpha = 0F
        // view hide
        bindingViewHide =
            LayoutViewHideBinding.inflate(LayoutInflater.from(context), myGroup, false)
        val areaNotify = PreferencesUtils.getInteger(KeyViewCenter.KEY_AREA_NOTIFY, 50)
        // set area
        setAreaViewCenter(areaNotify)
        //
        binding.layoutIconControls.myWindowManager = this
        binding.layoutControlsSecondsLine.myWindowManager = this
        binding.layoutIconCenterHide.myWindowManager = this
        binding.layoutBtnFist.myWindowManager = this
        binding.layoutBtnSecondsLine.myWindowManager = this
        binding.controlCenter.myWindowManager = this
        binding.layoutTaskBar.layoutWindowManager = binding
        binding.layoutBtnSecondsLine.layoutWindowManager = binding
        binding.layoutTaskBar.broadCastWifi = broadCastWifi
        binding.layoutBtnFist.broadCastWifi = broadCastWifi
        binding.layoutIconControls.layoutManager = binding
        binding.layoutTaskBar.stateWifi()
        binding.layoutTaskBar.statusAudio()
        binding.layoutBtnSecondsLine.bluetooth()
        // noti
        bindingNoti.layoutRclNotification.myWindowManager = this
        // view top left
        bindingViewHide.txtTopManagerLeft.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    changeColorLine()
                    startScreenShot()
                    //
                    if (PreferencesUtils.getBoolean(KeyViewCenter.KEY_VIEW_NOTIFICATION, true)) {
                        bindingNoti.layoutRclNotification.binding.rclNoti.adapter?.let {
                            if (it.itemCount > 0) {
                                bindingNoti.layoutRclNotification.binding.rclNoti.scrollToPosition(0)
                            }
                        }
                        actionDownTopLeftManager(event)
                    } else {
                        startY = event.y
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (PreferencesUtils.getBoolean(KeyViewCenter.KEY_VIEW_NOTIFICATION, true)) {
                        actionUpTopLeftManager()
//                        context.sendBroadcast(Intent(KeyBroadCast.ACTION_RELOAD))
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (PreferencesUtils.getBoolean(KeyViewCenter.KEY_VIEW_NOTIFICATION, true)) {
                        actionMoveTopLeftManager(event.y)
                    } else {
                        endY = event.y
                        if (endY - startY > 200F) {
                            expandSettingsPanel()
                        }
                    }
                }
            }
            true
        }
        // view notification
        bindingNoti.layoutTopNotification.setOnTouchListener { v, event -> true }
        bindingNoti.layoutContentNotification.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    actionDownViewNotification(event.y)
                }
                MotionEvent.ACTION_UP -> {
                    actionUpViewNotification()
                }
                MotionEvent.ACTION_MOVE -> {
                    actionMoveViewNotification(event.y)
                }
            }
            true
        }
        // view bottom
        binding.view.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    actionDownWindowManager(event)
                    countDownTimerALongClick = object : CountDownTimer(600, 600) {
                        override fun onTick(millisUntilFinished: Long) {
                        }

                        override fun onFinish() {
//                                long click
                        }
                    }.start()
                }
                MotionEvent.ACTION_UP -> {
                    endX = event.x
                    endY = event.y
                    actionUpWindowManager(event)
                    if (isAClick(
                            startX,
                            endX,
                            startY,
                            endY
                        ) && countDownTimerALongClick != null
                    ) {
                        clickViewBottom()
                    }
                    countDownTimerALongClick?.cancel()
                    countDownTimerALongClick = null
                }
                MotionEvent.ACTION_MOVE -> {
                    val differenceX = abs(startX - event.x)
                    val differenceY = abs(startY - event.y)
                    if (differenceX > 200 || differenceY > 200) {
                        countDownTimerALongClick?.cancel()
                        countDownTimerALongClick = null
                    }
                    actionMoveWindowManager(event)
                }
            }
            true
        }
        // view top right
        bindingViewHide.txtTopManagerRight.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    if (PreferencesUtils.getBoolean(KeyViewCenter.KEY_VIEW_CONTROL, true)) {
                        // timer
                        startCountDownTimer()
                        // screen Shot
                        startScreenShot()
                        //
                        val intent = Intent(context.resources.getString(R.string.action_usage_data))
                        context.sendBroadcast(intent)
                        intIconShape()
                        binding.layoutTaskBar.statePhone()
                        binding.layoutControlsSecondsLine.setStageAutoRotation()
                        binding.layoutControlsSecondsLine.setStageViewNightLight()
                        binding.layoutControlsSecondsLine.setStageDarkTheme()
                        binding.layoutIconCenterHide.setStageScreenTransmission()
                        binding.layoutIconCenterHide.setStageDataSaver()
                        binding.layoutIconCenterHide.setStageNFC()
                        binding.layoutBtnSecondsLine.setStageBluetooth()
                        detectBrightnessMode()
                        detectChangeBrightness()
                        changeColorLine()
                        //
                        if (PreferencesUtils.getBoolean(
                                STATUS_USAGE_DATA,
                                false
                            ) || PreferencesUtils.getBoolean(
                                context.resources.getString(R.string.stata_usage_day)
                            )
                        ) {
                            binding.layoutBtnFist.setUpUsageDataLayout(context)
                        }
                        binding.layoutIconCenterHide.visibility = View.INVISIBLE
                        actionDownTopManager(event)
                    } else {
                        startY = event.y
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (PreferencesUtils.getBoolean(KeyViewCenter.KEY_VIEW_CONTROL, true)) {
                        actionUpTopManager(event)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (PreferencesUtils.getBoolean(KeyViewCenter.KEY_VIEW_CONTROL, true)) {
                        actionMoveTopManager(event)
                    } else {
                        endY = event.y
                        if (endY - startY > 200F) {
                            expandSettingsPanel()
                        }
                    }
                }
            }
            //
            true
        }
        // view control
        binding.layoutContent.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    actionDownWindowManager(event)
                }
                MotionEvent.ACTION_UP -> {
                    actionUpWindowManager(event)
                }
                MotionEvent.ACTION_MOVE -> {
                    actionMoveWindowManager(event)
                }
            }
            //
            true
        }
        // add view top
        windowManager.addView(bindingViewHide.root, setupLayout(false))
        // load ads
        loadAds()
    }

    private fun loadAds() {
        AdmobNativeAdView.getNativeAd(
            context,
            R.layout.native_admod_control,
            object : AdmobNativeAdView.NativeAdListener {
                override fun onError() {
                }

                @SuppressLint("ClickableViewAccessibility")
                override fun onLoaded(nativeAd: AdmobNativeAdView?) {
                    nativeAd?.let {
                        if (it.parent != null) {
                            (it.parent as ViewGroup).removeView(it)
                        }
                        binding.adsView.addView(it)
                        it.setOnTouch { v, event ->
                            when (event!!.action and MotionEvent.ACTION_MASK) {
                                MotionEvent.ACTION_DOWN -> {
                                    startX = event.x
                                    startY = event.y
                                    actionDownWindowManager(event)
                                    countDownTimerALongClick = object : CountDownTimer(600, 600) {
                                        override fun onTick(millisUntilFinished: Long) {
                                        }

                                        override fun onFinish() {
                                        }
                                    }.start()
                                }
                                MotionEvent.ACTION_MOVE -> {
                                    val differenceX = abs(startX - event.x)
                                    val differenceY = abs(startY - event.y)
                                    if (differenceX > 200 || differenceY > 200) {
                                        countDownTimerALongClick?.cancel()
                                        countDownTimerALongClick = null
                                    }
                                    actionMoveWindowManager(event)
                                }
                                MotionEvent.ACTION_UP -> {
                                    endX = event.x
                                    endY = event.y
                                    actionUpWindowManager(event)
                                    if (isAClick(
                                            startX,
                                            endX,
                                            startY,
                                            endY
                                        ) && countDownTimerALongClick != null
                                    ) {

                                    }
                                    countDownTimerALongClick?.cancel()
                                    countDownTimerALongClick = null
                                }
                            }
                            true
                        }
                    }

                }

                override fun onClickAd() {
                    endWindowManager()
                }
            })
    }

    private fun dropDownViewTopLeft(event: MotionEvent) {
        checkMove1 = event.y
        checkAddViewNotification()
    }

    private fun dropMoveViewTopLeft(event: MotionEvent) {
        countMove++
        if (countMove > 1) {
            checkMove1 = checkMove2
            checkMove2 = event.y
        } else {
            checkMove2 = event.y
        }
        bindingNoti.root.translationY = (-bindingNoti.root.height.toFloat() + event.y)
    }

    private fun dropUpViewTopLeft(event: MotionEvent) {
        countMove = 0
        when {
            checkMove1 == event.y -> {
                if (bindingNoti.root.translationY >= -bindingNoti.root.height / 1.2) {
                    showNotification()
                } else {
                    hideNotification()
                }
            }
            checkMove1 > event.y -> {
                hideNotification()
            }
            checkMove1 < event.y -> {
                showNotification()
            }
        }
    }

    private fun dropMoveViewNotify(event: MotionEvent) {
        countMove++
        if (countMove > 1) {
            checkMove1 = checkMove2
            checkMove2 = event.y
        } else {
            checkMove2 = event.y
        }
        bindingNoti.root.translationY = (-bindingNoti.root.height.toFloat() + event.y)
    }

    private fun dropDownViewNotify(event: MotionEvent) {
        checkMove1 = event.y
    }

    private fun dropUpViewNotify(event: MotionEvent) {
        countMove = 0
        when {
            checkMove1 == event.y -> {
                if (bindingNoti.root.translationY >= -bindingNoti.root.height / 10F) {
                    showNotification()
                } else {
                    hideNotification()
                }
            }
            checkMove1 > event.y -> {
                hideNotification()
            }
            checkMove1 < event.y -> {
                showNotification()
            }
        }
    }

    private fun setAreaViewCenter(area: Int) {
        val isSwap = PreferencesUtils.getBoolean(KeyViewCenter.KEY_SWAP_VIEW, false)
        val constraintSet = ConstraintSet()
        constraintSet.clone(bindingViewHide.root)
        if (!isSwap) {
            constraintSet.constrainPercentWidth(R.id.txt_top_manager_left, area / 100F)
            constraintSet.constrainPercentWidth(R.id.txt_top_manager_right, (100 - area) / 100F)
        } else {
            constraintSet.constrainPercentWidth(R.id.txt_top_manager_left, (100 - area) / 100F)
            constraintSet.constrainPercentWidth(R.id.txt_top_manager_right, area / 100F)
        }
        constraintSet.applyTo(bindingViewHide.root)
    }

    private fun hideNotification() {
        startValueAnimatorForNoti(bindingNoti.root, -bindingNoti.root.height.toFloat())
        Handler().postDelayed({
            windowManager.removeViewImmediate(bindingNoti.root)
        }, 350)
    }

    private fun showNotification() {
        startValueAnimatorForNoti(bindingNoti.root, 0F)
    }

    private fun clickViewBottom() {
        val alphaAnimation: Animation =
            AnimationUtils.loadAnimation(context, R.anim.window_exit)
        binding.layoutWindow.startAnimation(alphaAnimation)
        Handler().postDelayed({
            try {
                windowManager.removeView(binding.root)
                windowManager.updateViewLayout(
                    bindingViewHide.root, setupLayout(false)
                )
            } catch (ex: Exception) {

            }
        }, 200)
    }

    fun isAClick(startX: Float, endX: Float, startY: Float, endY: Float): Boolean {
        val differenceX = abs(startX - endX)
        val differenceY = abs(startY - endY)
        return !(differenceX > CLICK_ACTION_THRESHOLD || differenceY > CLICK_ACTION_THRESHOLD)
    }

    // action move notification
    private fun actionMoveViewNotification(eventY: Float) {
        val heightScreen = context.getDisplayMetrics().heightPixels
        val heightNavigation = getNavigationBarSize(context).y
        val isHideNavigationBar =
            heightNavigation < 130F && point <= heightNavigation + heightScreen && point > heightScreen
        // lưu vi tri dragY mới
        if (bindingNoti.layoutContentNotification.translationY > 0F) {
            dragY = eventY
        }
        when {
            dragY - eventY > if (isHideNavigationBar) 90F else 250F -> { // đây là thời điểm move ngược về ( view đã bị ẩn hoàn toàn)
                dragY -= (dragY - eventY - if (isHideNavigationBar) 90F else 250F) // lưu dragY mới
            }
        }
        // Translation view
        if (eventY >= dragScroll) {
            animationTranslationNotification(eventY)
        } else {
            bindingNoti.layoutContentNotification.translationY = 0F
        }
        // alpha view
        bindingNoti.layoutNotification.alpha =
            1 - (((dragY - eventY)) / if (isHideNavigationBar) 90F else 250F)
        alpha = 1 - (((dragY - eventY)) / if (isHideNavigationBar) 90F else 250F)
        windowManager.updateViewLayout(bindingNoti.root, setupLayout(true))
        //
    }

    //action move center
    fun actionMoveWindowManager(event: MotionEvent) {
        if (binding.layoutContent.translationY > 0F) {
            dragY = event.y
        }
        if (!isFullView) {
            actionUp = false
        }
        if (dragY - event.y > 0 && isFullView) {
            actionUp = false
        }
        if (!isFullView && dragY - event.y > 0 && marginX <= 0) {
            isHiding = true
        }
        when {
            dragY - event.y > 250F -> {
                dragY -= (dragY - event.y - 250F)
            }
            dragY - event.y < 0 && !isHiding -> {
                marginX += (0 - (dragY - event.y)).toInt()
                if (marginX > binding.layoutIconCenterHide.height) {
                    marginX = binding.layoutIconCenterHide.height
                    binding.layoutIconCenterHide.alpha = 1F
                    isFullView = true
                } else {
                    if (!binding.layoutIconCenterHide.isShown) {
                        binding.layoutIconCenterHide.visibility = View.VISIBLE
                    }
                    binding.layoutIconCenterHide.alpha =
                        marginX / binding.layoutIconCenterHide.height.toFloat()
                }
                dragY = event.y
            }
            dragY - event.y > 0 && marginX > 0 && !isHiding -> {
                marginX -= (dragY - event.y).toInt()
                if (marginX < 0) {
                    marginX = 0
                    binding.layoutIconCenterHide.alpha = 0F
                } else {
                    if (!binding.layoutIconCenterHide.isShown) {
                        binding.layoutIconCenterHide.visibility = View.VISIBLE
                    }
                    binding.layoutIconCenterHide.alpha =
                        marginX / binding.layoutIconCenterHide.height.toFloat()
                    dragY = event.y
                }
            }
        }
        if (isFullView) {
            dragY = event.y
        }
        if (event.y >= dragScroll && actionUp) {
            animationTranslationWindowManger(event)
        }
        //
        dragScrollFlash = event.y
        //
        binding.layoutWindow.alpha = 1 - (((dragY - event.y)) / 250)
        binding.adsView.alpha = 1 - (((dragY - event.y)) / 250)
        alpha = 1 - (((dragY - event.y)) / 250F)
        windowManager.updateViewLayout(binding.root, setupLayout(true))
        if ((1 - (((dragY - event.y)) / 250F)) <= 0.9F) {
            countTouch++
            if (countTouch == 1) {
                isViewHide = true
                startAnimationExit(false)
                binding.layoutContent.alpha = 0F
                //update layout
                try {
                    windowManager.updateViewLayout(
                        binding.root, setupLayout(true)
                    )
                } catch (e: java.lang.IllegalArgumentException) {
                    windowManager.addView(
                        binding.root, setupLayout(true)
                    )
                }
            }
        } else {
            if (isViewHide && 1 - ((dragY - event.y) / 250F) >= 0.9) {
                isViewHide = false
                countTouch = 0
                binding.layoutContent.alpha = 1F
                windowManager.updateViewLayout(binding.root, setupLayout(true))
                startAnimationEnter(false)
            }
        }
        if (!isHiding) {
            if (!isViewHide && 1 - ((dragY - event.y) / 250F) == 1F && marginX <= binding.layoutIconCenterHide.height) {
                if (binding.layoutLightScreen.translationY == 0F && marginX == 0
                    || marginX == binding.layoutIconCenterHide.height
                ) {
                    //
                } else {
                    binding.layoutLightScreen.translationY = marginX.toFloat()
                }
            } else if (marginX > binding.layoutIconCenterHide.height) {
                marginX = binding.layoutIconCenterHide.height
            } else if (marginX < 0) {
                marginX = 0
            }
        }
    }

    private fun animationTranslationNotification(eventY: Float) {
        bindingNoti.layoutContentNotification.translationY = (eventY - dragScroll) / 6F
        bindingNoti.layoutRclNotification.binding.txtNotification.translationY =
            (eventY - dragScroll) / 12F
        bindingNoti.layoutRclNotification.binding.rclNoti.translationY = (eventY - dragScroll) / 7F
    }

    private fun animationTranslationWindowManger(event: MotionEvent) {
        binding.layoutContent.translationY = (event.y - dragScroll) / 6F
        binding.layoutTaskBar.translationY = (event.y - dragScroll) / 15F
        binding.controlCenter.translationY = (event.y - dragScroll) / 14F
        binding.layoutBtnFist.translationY = (event.y - dragScroll) / 13F
        binding.layoutBtnSecondsLine.translationY = (event.y - dragScroll) / 12F
        binding.layoutBtnSecondsLine.translationY = (event.y - dragScroll) / 12F
        binding.adsView.translationY = (event.y - dragScroll) / 8F
        binding.layoutIconControls.translationY = (event.y - dragScroll) / 8F
        binding.layoutControlsSecondsLine.translationY = (event.y - dragScroll) / 7.6F
        binding.layoutIconCenterHide.translationY = (event.y - dragScroll) / 6.4F
        binding.layoutLightScreen.translationY = (event.y - dragScroll) / 5.2F + marginX
        binding.lastLine.translationY = (event.y - dragScroll) / 20F
        binding.view.translationY = (event.y - dragScroll) / 24F
        binding.adsView.translationY = (event.y - dragScroll) / 10F
    }

    private fun actionUpViewNotification() {
        if (bindingNoti.layoutContentNotification.translationY > 0) {
            startAnimationTranslationTopNotification()
        } else {
            if (bindingNoti.layoutNotification.alpha >= 0.5F) {
                bindingViewHide.txtTopManagerLeft.isEnabled = false
                bindingNoti.layoutContentNotification.isEnabled = false
                bindingNoti.layoutNotification.alpha = 1F
                val anim = AlphaAnimation(alpha, 1F)
                anim.fillAfter = true
                anim.duration = 200
                bindingNoti.layoutNotification.startAnimation(anim)
                Handler().postDelayed({
                    bindingViewHide.txtTopManagerLeft.isEnabled = true
                    bindingNoti.layoutContentNotification.isEnabled = true
                    windowManager.updateViewLayout(bindingNoti.root, setupLayout(true))
                    isShowNotify = true
                }, 100)
            } else {
                try {
                    bindingViewHide.txtTopManagerLeft.isEnabled = false
                    bindingNoti.layoutContentNotification.isEnabled = false
//                val anim = AlphaAnimation(alpha, 0F)
//                anim.duration = 200
                    bindingNoti.layoutNotification.animate().alpha(0F).setDuration(200).start()
                    Handler().postDelayed({
                        bindingViewHide.txtTopManagerLeft.isEnabled = true
                        bindingNoti.layoutContentNotification.isEnabled = true
                        windowManager.removeViewImmediate(bindingNoti.root)
                        windowManager.updateViewLayout(bindingViewHide.root, setupLayout(false))
                        isShowNotify = false
                    }, 200)
                } catch (e: IllegalArgumentException) {
                }
            }
        }
        //
    }

    fun actionUpWindowManager(event: MotionEvent) {
        actionUp = true
        isHiding = false
        if (!isViewHide) {
            binding.layoutWindow.alpha = 1F
            binding.adsView.alpha = 1F
            binding.layoutContent.alpha = 1F
            if (marginX in 100..binding.layoutIconCenterHide.height) {
                // set up view bottom (ads)
                binding.view.height =
                    (context.resources.displayMetrics.heightPixels * 0.115F).toInt()
                //
                binding.layoutIconCenterHide.alpha = 1F
                val alphaAnimation =
                    AlphaAnimation(marginX / binding.layoutIconCenterHide.height.toFloat(), 1F)
                alphaAnimation.duration = 200
                alphaAnimation.fillAfter = true
                binding.layoutIconCenterHide.startAnimation(alphaAnimation)
                //
                marginX = binding.layoutIconCenterHide.height
                isFullView = true
                startValueAnimator(binding.layoutLightScreen, true)
                binding.layoutIconCenterHide.alpha = 1F
                try {
                    windowManager.updateViewLayout(
                        binding.root, setupLayout(true)
                    )
                } catch (e: java.lang.IllegalArgumentException) {
                    windowManager.addView(
                        binding.root, setupLayout(true)
                    )
                }
                isShowControl = true
            } else if (marginX in 0 until 100) {
                // set up view bottom (ads)
                if (isFullView) {
                    binding.view.height =
                        (context.resources.displayMetrics.heightPixels * (0.115 + 0.12)).toInt()
                }
                //
                isFullView = false
                binding.layoutIconCenterHide.alpha = 1F
                //
                val alphaAnimation =
                    AlphaAnimation(marginX / binding.layoutIconCenterHide.height.toFloat(), 0F)
                alphaAnimation.duration = 200
                alphaAnimation.fillBefore = true
                binding.layoutIconCenterHide.startAnimation(alphaAnimation)
                marginX = 0
                startValueAnimator(binding.layoutLightScreen, false)
                binding.layoutIconCenterHide.alpha = 0F
                try {
                    windowManager.updateViewLayout(
                        binding.root, setupLayout(true)
                    )
                } catch (e: java.lang.IllegalArgumentException) {
                    windowManager.addView(
                        binding.root, setupLayout(true)
                    )
                }
                isShowControl = true
            }
            if (alpha == 1F) {
                binding.layoutContent.alpha = 1F
                windowManager.updateViewLayout(binding.root, setupLayout(true))
                isShowControl = true
            }
        } else {
            bindingViewHide.txtTopManagerRight.isEnabled = false
            binding.layoutContent.isEnabled = false
            val anim = AlphaAnimation(alpha, 0F)
            anim.duration = 200
            binding.layoutContent.alpha = 0F
            binding.layoutWindow.startAnimation(anim)
            binding.imgBackgroundColor.startAnimation(anim)
            val handle = Handler()
            handle.postDelayed({
                bindingViewHide.txtTopManagerRight.isEnabled = true
                binding.layoutContent.isEnabled = true
                endWindowManager()
            }, 100)
        }
        //
        if (binding.layoutContent.translationY > 0) {
            startAnimationTranslationWindowManager()
        }
    }

    private fun startAnimationTranslationWindowManager() {
        startValueAnimator(binding.layoutContent)
        startValueAnimator(binding.layoutTaskBar)
        startValueAnimator(binding.controlCenter)
        startValueAnimator(binding.layoutBtnFist)
        startValueAnimator(binding.layoutBtnSecondsLine)
        startValueAnimator(binding.layoutBtnSecondsLine)
        startValueAnimatorDelay(binding.layoutIconControls)
        startValueAnimatorDelay(binding.layoutControlsSecondsLine)
        startValueAnimatorDelay(binding.layoutIconCenterHide)
        startValueAnimatorDelay(binding.lastLine)
        startValueAnimatorDelay(binding.view)
        startValueAnimatorDelay(binding.adsView)
        val valueAnimator =
            ValueAnimator.ofFloat(binding.layoutLightScreen.translationY, marginX.toFloat())
        valueAnimator.duration = 600
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.addUpdateListener { valueAnimator ->
            binding.layoutLightScreen.translationY = valueAnimator.animatedValue as Float
        }
        valueAnimator.start()
    }

    fun actionDownWindowManager(event: MotionEvent) {
//        binding.layoutWindow.invalidate()
        isViewHide = binding.layoutContent.alpha != 1F
        alpha = 1F
        countTouch = 0
        dragY = event.y
        if (isFullView) {
            dragScroll = event.y
        }
    }

    // action down notification
    fun actionDownViewNotification(eventY: Float) {
        point = eventY
        alpha = 1F
        dragY = eventY
        dragScroll = eventY
        bindingNoti.root.translationY = 0F
    }

    private fun actionDownTopManager(event: MotionEvent) {
        isShow = false
        isFullView = false
        countTouch = 0
        dragY = event.y
        binding.layoutWindow.alpha = 0.1F
        binding.adsView.alpha = 0.1F
        alpha = 0.1F
        dragScroll = event.y
        binding.layoutContent.alpha = 0F
        checkAddViewCenter()
        countDownTimer.start()
    }

    private fun actionDownTopLeftManager(event: MotionEvent) {
        isShow = false
        dragY = event.y
        bindingNoti.layoutNotification.alpha = 0.1F
        alpha = 0.1F
        dragScroll = event.y
        checkAddViewNotification()
    }

    private fun actionMoveTopLeftManager(eventY: Float) {
        when {
            bindingNoti.layoutContentNotification.translationY > 0 -> {
                dragY = eventY - 250F  //  xử lí dragY luôn cách even.y ko quá 250
            }
            (eventY - dragY) > 250F -> {
                dragY += ((eventY - dragY) - 250F) // xử lí  điểm dragY luôn cách even.y  250 khi kéo quá 250
            }
            eventY - dragY < 0 -> {
                dragY = eventY // lưu điểm dragY mới khi event.y kéo ngược về quá điểm dragY đc lưu
            }
        }
        if ((eventY - dragScroll) > 250F) {
            animationTranslationNotification(eventY) // Translation khi kéo vượt ngưỡng 250
        } else {
            bindingNoti.layoutContentNotification.translationY = 0F
        }
        // alpha theo độ move
        bindingNoti.layoutNotification.alpha = (eventY - dragY) / 250F
        // lưu lại độ alpha
        alpha = (eventY - dragY) / 250F
        //
        if (((eventY - dragY) / 250F) >= 1F) { // kéo vượt ngưỡng 250 , update view theo độ kéo
            countTouch++
            if (countTouch == 1) {
                bindingNoti.layoutContentNotification.alpha = 1F
                windowManager.updateViewLayout(bindingNoti.root, setupLayout(true))
            }
        } else {
            countTouch = 0
            windowManager.updateViewLayout(bindingNoti.root, setupLayout(true))
        }
    }


    private fun actionMoveTopManager(event: MotionEvent) {
        when {
            binding.layoutContent.translationY > 0 -> {
                dragY = event.y - 250F
            }
            (event.y - dragY) > 250F -> {
                dragY += ((event.y - dragY) - 250F)
            }
            event.y - dragY < 0 -> {
                dragY = event.y
            }
        }
        if ((event.y - dragScroll) > 250F) {
            animationTranslationTopManager(event)
        } else {
            binding.layoutContent.translationY = 0F
        }
        binding.layoutWindow.alpha = (event.y - dragY) / 250F
        binding.adsView.alpha = (event.y - dragY) / 250F
        alpha = (event.y - dragY) / 250F
        if (((event.y - dragY) / 250F) == 1F) {
            countTouch++
            if (countTouch == 1) {
                isShow = true
                isViewHide = false
                binding.layoutContent.alpha = 1F
                windowManager.updateViewLayout(binding.root, setupLayout(true))
                startAnimationEnter(false)
            }
            FlurryAnalytics.logEvent(EventTracking.CONTROL_CENTER, "expand")
        } else {
            if (!isViewHide) {
                windowManager.updateViewLayout(binding.root, setupLayout(true))
            }
        }
        if (isShow && ((event.y - dragY) / 250F) <= 0.9) {
            countTouch = 0
            isShow = false
            startAnimationExit(false)
            isViewHide = true
            binding.layoutContent.alpha = 0F
            try {
                windowManager.updateViewLayout(
                    binding.root, setupLayout(true)
                )
            } catch (e: java.lang.IllegalArgumentException) {
                windowManager.addView(
                    binding.root, setupLayout(true)
                )
            }
        }
    }

    private fun animationTranslationTopManager(event: MotionEvent) {
        binding.layoutContent.translationY = ((event.y - dragScroll) - 250F) / 4F
        binding.layoutTaskBar.translationY = ((event.y - dragScroll) - 250F) / 14F
        binding.controlCenter.translationY = ((event.y - dragScroll) - 250F) / 12.7F
        binding.layoutBtnFist.translationY = ((event.y - dragScroll) - 250F) / 11.3F
        binding.layoutBtnSecondsLine.translationY = ((event.y - dragScroll) - 250F) / 10.5F
        binding.layoutBtnSecondsLine.translationY = ((event.y - dragScroll) - 250F) / 9.7F
        binding.adsView.translationY = ((event.y - dragScroll) - 250F) / 8F
        binding.layoutIconControls.translationY = ((event.y - dragScroll) - 250F) / 7.5F
        binding.layoutControlsSecondsLine.translationY = ((event.y - dragScroll) - 250F) / 7.2F
        binding.layoutIconCenterHide.translationY = ((event.y - dragScroll) - 250F) / 6.1F
        binding.layoutLightScreen.translationY = ((event.y - dragScroll) - 250F) / 6F
        binding.lastLine.translationY = ((event.y - dragScroll) - 250F) / 20F
    }

    private fun animationTranslationTopLeft(event: MotionEvent) {
        bindingNoti.layoutContentNotification.translationY = ((event.y - dragScroll) - 250F) / 4F
    }

    private fun actionUpTopLeftManager() {
        if (bindingNoti.layoutContentNotification.translationY > 0) {
            startAnimationTranslationTopNotification()
        } else {
            if (bindingNoti.layoutNotification.alpha < 0.5F) { // remove notification view
                bindingViewHide.txtTopManagerLeft.isEnabled = false
                bindingNoti.layoutContentNotification.isEnabled = false
                // animation remove view
                val alphaAnimation = AlphaAnimation(alpha, 0F)
                alphaAnimation.duration = 200
                bindingNoti.layoutNotification.startAnimation(alphaAnimation)
//                binding.imgBackgroundColor.startAnimation(alphaAnimation)
                Handler().postDelayed({
                    bindingViewHide.txtTopManagerLeft.isEnabled = true
                    bindingNoti.layoutContentNotification.isEnabled = true
                }, 200)
                // remove
                windowManager.removeViewImmediate(bindingNoti.root)
                // update
                windowManager.updateViewLayout(
                    bindingViewHide.root, setupLayout(false)
                )
                isShowNotify = false
            } else {
                // hiên view
                bindingNoti.layoutNotification.alpha = 1F
                val alphaAnimation = AlphaAnimation(alpha, 1F)
                alphaAnimation.duration = 200
                bindingNoti.layoutNotification.startAnimation(alphaAnimation)
                try {
                    windowManager.updateViewLayout(
                        bindingNoti.root, setupLayout(true)
                    )
                } catch (e: java.lang.IllegalArgumentException) {
                    windowManager.addView(
                        bindingNoti.root, setupLayout(true)
                    )
                }
                isShowNotify = true
            }
        }
    }

    private fun actionUpTopManager(event: MotionEvent) {
        if (binding.layoutContent.translationY > 0) {
            startAnimationTranslationTopManager()
        }
        if (!isShow) {
            bindingViewHide.txtTopManagerRight.isEnabled = false
            binding.layoutContent.isEnabled = false
            val alphaAnimation = AlphaAnimation(alpha, 0F)
            binding.layoutContent.alpha = 0F
            alphaAnimation.duration = 200
            binding.imgBackgroundColor.startAnimation(alphaAnimation)
            bindingViewHide.txtTopManagerRight.isEnabled = true
            binding.layoutContent.isEnabled = true
            // remove binding
            windowManager.removeViewImmediate(binding.root)
            //
            windowManager.updateViewLayout(
                bindingViewHide.root, setupLayout(false)
            )
            isShowControl = false
            countDownTimer.cancel()
        } else {
            binding.layoutWindow.alpha = 1F
            binding.adsView.alpha = 1F
            val alphaAnimation = AlphaAnimation(alpha, 1F)
            alphaAnimation.duration = 200
            binding.layoutWindow.startAnimation(alphaAnimation)
            try {
                windowManager.updateViewLayout(
                    binding.root, setupLayout(true)
                )
            } catch (e: java.lang.IllegalArgumentException) {
                windowManager.addView(
                    binding.root, setupLayout(true)
                )
            }
            isShowControl = true
        }
    }

    private fun startAnimationTranslationTopManager() {
        startValueAnimator(binding.layoutContent)
        startValueAnimator(binding.layoutTaskBar)
        startValueAnimator(binding.controlCenter)
        startValueAnimator(binding.layoutBtnFist)
        startValueAnimator(binding.adsView)
        startValueAnimator(binding.layoutBtnSecondsLine)
        startValueAnimator(binding.layoutBtnSecondsLine)
        startValueAnimatorDelay(binding.layoutIconControls)
        startValueAnimatorDelay(binding.layoutControlsSecondsLine)
        startValueAnimatorDelay(binding.layoutIconCenterHide)
        startValueAnimatorDelay(binding.layoutLightScreen)
        startValueAnimatorDelay(binding.lastLine)
    }

    private fun startAnimationTranslationTopNotification() {
        startValueAnimator(bindingNoti.layoutContentNotification)
        startValueAnimator(bindingNoti.layoutRclNotification.binding.txtNotification)
        startValueAnimator(bindingNoti.layoutRclNotification.binding.rclNoti)
    }

    fun updateView(canClick: Boolean) {
        windowManager.updateViewLayout(binding.root, setupLayout(true))
    }

    private fun setupLayout(
        open: Boolean
    ): WindowManager.LayoutParams {
        val mLayoutParams: WindowManager.LayoutParams

        val flag = if (open) {
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        } else {
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        }
        val type =
            if (Build.VERSION.SDK_INT >= 22) WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            else WindowManager.LayoutParams.TYPE_SYSTEM_ERROR

        val height = when (open) {
            true -> {
                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                val height = displayMetrics.heightPixels
                if (getNavigationBarSize(context).y > 0) {
                    height + (getNavigationBarSize(context).y)
                } else {
                    WindowManager.LayoutParams.MATCH_PARENT
                }
            }
            false -> {
                getStatusBarHeight() + 15
            }
        }
        mLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            height,
            type,
            flag,
            PixelFormat.TRANSLUCENT
        )
        mLayoutParams.gravity = Gravity.TOP
        mLayoutParams.alpha = 1F
//
        return mLayoutParams
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId: Int =
            context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        } else {
            result = 80
        }
        return result
    }

    private fun getNavigationBarSize(context: Context): Point {
        val appUsableSize = getAppUsableScreenSize(context)
        val realScreenSize = getRealScreenSize(context)

        // navigation bar on the right
        if (appUsableSize.x < realScreenSize.x) {
            return Point(realScreenSize.x - appUsableSize.x, appUsableSize.y)
        }

        // navigation bar at the bottom
        return if (appUsableSize.y < realScreenSize.y) {
            Point(appUsableSize.x, realScreenSize.y - appUsableSize.y)
        } else Point()

        // navigation bar is not present
    }

    private fun getAppUsableScreenSize(context: Context): Point {
        val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size
    }

    private fun getRealScreenSize(context: Context): Point {
        val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size)
        } else if (Build.VERSION.SDK_INT >= 14) {
            try {
                size.x = (Display::class.java.getMethod("getRawWidth").invoke(display) as Int)
                size.y = (Display::class.java.getMethod("getRawHeight").invoke(display) as Int)
            } catch (e: IllegalAccessException) {
            } catch (e: InvocationTargetException) {
            } catch (e: NoSuchMethodException) {
            }
        }
        return size
    }

    private fun startAnimationEnter(isChangingBrightness: Boolean) {
        val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.window_enter)
        binding.layoutTaskBar.binding.layoutSim1.startAnimation(animation)
        binding.layoutTaskBar.binding.layoutSim2.startAnimation(animation)
        binding.layoutTaskBar.binding.viewsStatus.startAnimation(animation)
        //
        binding.controlCenter.binding.txtControlCenter.startAnimation(animation)
        //
        binding.layoutBtnFist.binding.btnMobileData.startAnimation(animation)
        binding.layoutBtnFist.binding.btnWifi.startAnimation(animation)
        binding.layoutBtnSecondsLine.binding.btnBluetooth.startAnimation(animation)
        binding.layoutBtnSecondsLine.binding.btnFlashlight.startAnimation(animation)
        // ads
        //
        val scale = ScaleAnimation(
            0.8F,
            1F,
            0.8F,
            1F,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        scale.duration = 180
        val scales = ScaleAnimation(
            0.85F,
            1F,
            0.85F,
            1F,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        scales.duration = 200
        binding.adsView.startAnimation(scales)
        binding.layoutIconControls.binding.layoutIconMute.startAnimation(scale)
        binding.layoutIconControls.binding.layoutIconBatterySaver.startAnimation(scale)
        binding.layoutIconControls.binding.layoutIconAirplaneMode.startAnimation(scale)
        binding.layoutIconControls.binding.layoutIconDoNotDisturb.startAnimation(scale)
        binding.layoutControlsSecondsLine.binding.layoutIconAutoRotate.startAnimation(scale)
        binding.layoutControlsSecondsLine.binding.layoutNightLight.startAnimation(scale)
        binding.layoutControlsSecondsLine.binding.layoutDarkTheme.startAnimation(scale)
        binding.layoutControlsSecondsLine.binding.layoutHotspot.startAnimation(scale)
        binding.layoutIconA.startAnimation(scale)
        binding.icBrightness.startAnimation(scale)
        //
        binding.icA.startAnimation(scale)
        if (!isChangingBrightness) {
            binding.boxedVertical.startAnimation(scale)
        }
        //

        binding.controlCenter.binding.icSetting.startAnimation(scale)
        binding.controlCenter.binding.icEdit.startAnimation(scale)
        //
        binding.lastLine.startAnimation(scale)
    }

    private fun startAnimationExit(isChangingBrightness: Boolean) {
        val alphaAnimation: Animation =
            AnimationUtils.loadAnimation(context, R.anim.window_exit)
        binding.layoutTaskBar.binding.layoutSim1.startAnimation(alphaAnimation)
        binding.layoutTaskBar.binding.layoutSim2.startAnimation(alphaAnimation)
        binding.layoutTaskBar.binding.viewsStatus.startAnimation(alphaAnimation)
        //
        binding.controlCenter.binding.txtControlCenter.startAnimation(alphaAnimation)
        //
        binding.layoutBtnFist.binding.btnMobileData.startAnimation(alphaAnimation)

        binding.layoutBtnSecondsLine.binding.btnBluetooth.startAnimation(alphaAnimation)
        // ads

        //
        val scale = ScaleAnimation(
            1F,
            0.8F,
            1F,
            0.8F,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        scale.duration = 200
        binding.adsView.startAnimation(scale)
        binding.layoutBtnFist.binding.btnWifi.startAnimation(scale)
        binding.layoutBtnSecondsLine.binding.btnFlashlight.startAnimation(scale)
        binding.layoutIconControls.binding.layoutIconMute.startAnimation(scale)
        binding.layoutIconControls.binding.layoutIconBatterySaver.startAnimation(scale)
        binding.layoutIconControls.binding.layoutIconAirplaneMode.startAnimation(scale)
        binding.layoutIconControls.binding.layoutIconDoNotDisturb.startAnimation(scale)
        binding.layoutControlsSecondsLine.binding.layoutIconAutoRotate.startAnimation(scale)
        binding.layoutControlsSecondsLine.binding.layoutNightLight.startAnimation(scale)
        binding.layoutControlsSecondsLine.binding.layoutDarkTheme.startAnimation(scale)
        binding.layoutControlsSecondsLine.binding.layoutHotspot.startAnimation(scale)
        binding.layoutIconA.startAnimation(scale)
        binding.icBrightness.startAnimation(scale)
        //
        binding.icA.startAnimation(scale)
        if (!isChangingBrightness) {
            binding.boxedVertical.startAnimation(scale)
        }
        //
        binding.controlCenter.binding.icSetting.startAnimation(scale)
        binding.controlCenter.binding.icEdit.startAnimation(scale)
        //
        binding.lastLine.startAnimation(scale)
    }

    private fun startValueAnimator(view: View) {
        val valueAnimator = ValueAnimator.ofFloat(view.translationY, 0f)
        valueAnimator.duration = 500
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.addUpdateListener { valueAnimator ->
            view.translationY = valueAnimator.animatedValue as Float
        }
        valueAnimator.start()
    }

    private fun startValueAnimatorForNoti(view: View, translationY: Float) {
        val valueAnimator = ValueAnimator.ofFloat(view.translationY, translationY)
        valueAnimator.duration = 350
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.addUpdateListener { valueAnimator ->
            view.translationY = valueAnimator.animatedValue as Float
        }
        valueAnimator.start()
    }


    private fun startValueAnimatorDelay(view: View) {
        val valueAnimator = ValueAnimator.ofFloat(view.translationY, 0f)
        valueAnimator.duration = 600
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.addUpdateListener { valueAnimator ->
            view.translationY = valueAnimator.animatedValue as Float
        }
        valueAnimator.start()
    }

    private fun startValueAnimator(view: View, isShow: Boolean) {
        val valueAnimator = if (isShow) ValueAnimator.ofFloat(
            view.translationY,
            binding.layoutIconCenterHide.height.toFloat()
        )
        else ValueAnimator.ofFloat(view.translationY, 0f)
        valueAnimator.duration = 150
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.addUpdateListener { valueAnimator ->
            view.translationY = valueAnimator.animatedValue as Float
        }
        valueAnimator.start()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onSetting() {
        binding.controlCenter.binding.icSetting.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    actionDownWindowManager(event)
                    countDownTimerALongClick = object : CountDownTimer(600, 600) {
                        override fun onTick(millisUntilFinished: Long) {
                        }

                        override fun onFinish() {

                        }
                    }.start()
                }
                MotionEvent.ACTION_UP -> {
                    endX = event.x
                    endY = event.y
                    actionUpWindowManager(event)
                    if (isAClick(
                            startX,
                            endX,
                            startY,
                            endY
                        ) && countDownTimerALongClick != null
                    ) {
                        v.startAnimation(animClickIcon)
                        Handler().postDelayed({
                            binding.controlCenter.onSetting()
                            endWindowManager()
                        }, 200)
                    }
                    countDownTimerALongClick?.cancel()
                    countDownTimerALongClick = null
                }
                MotionEvent.ACTION_MOVE -> {
                    val differenceX = Math.abs(startX - event.x)
                    val differenceY = Math.abs(startY - event.y)
                    if (differenceX > 200 || differenceY > 200) {
                        countDownTimerALongClick?.cancel()
                        countDownTimerALongClick = null
                    }
                    actionMoveWindowManager(event)
                }
            }
            true
        }
    }

    private fun onSettingWifi() {
        binding.layoutBtnFist.layoutWindowManager = binding
        binding.layoutBtnFist.endWindowManager = {
            endWindowManager()
        }

    }

    private fun callBackViewTaskbar() {
        binding.layoutTaskBar.listenerConnectWifi = {
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            binding.layoutBtnFist.binding.name.text =
                if (wifiManager.connectionInfo.ssid.startsWith("<unknown"))
                    context.getString(
                        R.string.wi_fi
                    ) else wifiManager.connectionInfo.ssid.replace(
                    '"',
                    ' '
                ).trim()
        }
        binding.layoutTaskBar.endLayoutWindowManager = {
            endWindowManager()
        }
    }

    private fun onSettingBluetooth() {
        binding.layoutBtnSecondsLine.endWindowManager = {
            endWindowManager()
        }
    }

    private fun onSettingMobileData() {
        binding.layoutBtnFist.listener = {
            if (!PreferencesUtils.getBoolean(
                    context.resources.getString(R.string.state_usage_data),
                    false
                ) && !PreferencesUtils.getBoolean(
                    context.resources.getString(R.string.stata_usage_day),
                    false
                )
            ) {
                binding.layoutBtnFist.settingMobileData()
            } else {
                try {
                    val intent = Intent()
                    intent.flags = FLAG_ACTIVITY_NEW_TASK
                    intent.component = ComponentName(
                        "com.android.settings",
                        "com.android.settings.Settings\$DataUsageSummaryActivity"
                    )
                    context.startActivity(intent)
                } catch (ex: Exception) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.cant_open_fuction),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            endWindowManager()
        }
    }

    private fun callBackViewIconCenter() {
        binding.layoutIconControls.listenerEndLayout = {
            endWindowManager()
        }
        //
        binding.layoutIconControls.listenerChangeAirplane = {
            val isRequestSim = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
            if (it) {
                if (!isRequestSim) {
                    binding.layoutTaskBar.binding.layoutStateSim.visibility = View.VISIBLE
                }
                binding.layoutTaskBar.binding.icSilentSim1.visibility = View.VISIBLE
                binding.layoutTaskBar.binding.icSilentSim1.setImageResource(
                    R.drawable.ic_airplane
                )
                binding.layoutTaskBar.binding.nameSim1.visibility = View.GONE
                binding.layoutTaskBar.binding.icSim1.visibility = View.GONE
                binding.layoutTaskBar.binding.layoutSim2.visibility = View.GONE
            } else {
                if (!isRequestSim) {
                    binding.layoutTaskBar.binding.layoutStateSim.visibility = View.INVISIBLE
                }
                if (checkPhonePermission()) {
                    val telephonyManager =
                        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                    binding.layoutTaskBar.phoneCount(telephonyManager)
                }
            }
        }
    }

    private fun checkPhonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun callBackViewIconSecondLine() {
        binding.layoutControlsSecondsLine.endLayoutManager = {
            endWindowManager()
        }
    }

    private fun callBackViewIconHide() {
        binding.layoutIconCenterHide.endLayoutManager = {
            endWindowManager()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun stateBrightnessMode() {
        binding.layoutIconA.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    actionDownWindowManager(event)
                    countDownTimerALongClick = object : CountDownTimer(600, 600) {
                        override fun onTick(millisUntilFinished: Long) {
                        }

                        override fun onFinish() {
                            longClickA()
                        }
                    }.start()
                }
                MotionEvent.ACTION_UP -> {
                    endX = event.x
                    endY = event.y
                    actionUpWindowManager(event)
                    if (isAClick(
                            startX,
                            endX,
                            startY,
                            endY
                        ) && countDownTimerALongClick != null
                    ) {
                        v.startAnimation(animClickIcon)
                        if (Build.VERSION.SDK_INT >= 23) {
                            if (!Settings.System.canWrite(context)) {
                                showExplanation(
                                    "",
                                    context.getString(R.string.DESCRIBE_REQUEST_WRITE_SETTING)
                                )
                            } else {
                                val isEnabled = Settings.System.getInt(
                                    context.contentResolver,
                                    Settings.System.SCREEN_BRIGHTNESS_MODE, 0
                                ) != 0
                                Settings.System.putInt(
                                    context.contentResolver,
                                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                                    if (isEnabled) 0 else Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                                )
                                detectBrightnessMode()
                            }
                        } else {
                            try {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.WRITE_SETTINGS
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    val isEnabled = Settings.System.getInt(
                                        context.contentResolver,
                                        Settings.System.SCREEN_BRIGHTNESS_MODE, 0
                                    ) != 0
                                    Settings.System.putInt(
                                        context.contentResolver,
                                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                                        if (isEnabled) 0 else Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                                    )
                                    detectBrightnessMode()
                                } else {
                                    showExplanation(
                                        "",
                                        context.getString(
                                            R.string.DESCRIBE_REQUEST_WRITE_SETTING
                                        )
                                    )
                                }
                            } catch (e: SecurityException) {
                                val viewDialog = ViewDialogOpenSettings(context, null)
                                viewDialog.binding.txtSetUpName.text =
                                    context.getString(R.string.set_up_auto_brightness)
                                viewDialog.binding.txtContentDialog.text =
                                    context.getString(R.string.describe_auto_brightness)
                                viewDialog.binding.icControl.setImageResource(R.drawable.ic_a)
                                viewDialog.binding.btnOpenSettings.setOnClickListener {
                                    alertDialogBrightness.dismiss()
                                    expandSettingsPanel()
                                    endWindowManager()
                                }

                                viewDialog.binding.openHelper.setOnClickListener {
                                    alertDialogBrightness.dismiss()
                                    val intent =
                                        Intent(context, SystemShadeActivity::class.java)
                                    intent.flags = FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(intent)
                                    endWindowManager()
                                }
                                alertDialogBrightness.setCancelable(true)
                                alertDialogBrightness.window?.setBackgroundDrawable(
                                    ColorDrawable(
                                        Color.TRANSPARENT
                                    )
                                )
                                alertDialogBrightness.setContentView(viewDialog.binding.root)
                                if (Build.VERSION.SDK_INT >= 22)
                                    alertDialogBrightness.window!!.setType(
                                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                                    )
                                else
                                    alertDialogBrightness.window!!.setType(
                                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                                    )
                                alertDialogBrightness.show()
                            }
                            //
                        }
                    }
                    countDownTimerALongClick?.cancel()
                    countDownTimerALongClick = null
                }
                MotionEvent.ACTION_MOVE -> {
                    val differenceX = Math.abs(startX - event.x)
                    val differenceY = Math.abs(startY - event.y)
                    if (differenceX > 200 || differenceY > 200) {
                        countDownTimerALongClick?.cancel()
                        countDownTimerALongClick = null
                    }
                    actionMoveWindowManager(event)
                }
            }
            true
        }
        //
    }

    private fun longClickA() {
        val intent = Intent("android.settings.DISPLAY_SETTINGS")
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
        endWindowManager()
    }

    fun detectBrightnessMode() {
        val isEnabled = Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE, 0
        ) != 0
        //
        if (isEnabled) {
            binding.backgroundIcA.setColorFilter(
                Color.parseColor(PreferencesUtils.getString(SELECTED_COLOR, "#2C61CC")),
                PorterDuff.Mode.SRC_IN
            )
            Handler().postDelayed({
                detectChangeBrightness()
            }, 1000)
        } else {
            binding.backgroundIcA.setColorFilter(
                Color.parseColor(
                    PreferencesUtils.getString(
                        BACKGROUND_COLOR,
                        context.resources.getString(R.string.color_4DFFFFFF)
                    )
                ),
                PorterDuff.Mode.SRC_IN
            )
            detectChangeBrightness()
        }

    }

    private fun changeBrightness() {
        binding.boxedVertical.setOnBoxedPointsChangeListener(object :
            BoxedVertical.OnValuesChangeListener {
            var count = 0
            override fun onPointsChanged(boxedPoints: BoxedVertical?, points: Int) {
                count++
                if (isChangingBrightness) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (!Settings.System.canWrite(context)) {
                            if (count == 1) {
                                showExplanation(
                                    "",
                                    context.getString(R.string.DESCRIBE_REQUEST_WRITE_SETTING)
                                )
                                detectChangeBrightness()
                            }
                        } else {
                            if (count == 5) {
                                setAlphaLayout(0F)
                            }
                            if (points % 5 == 0) {
                                Settings.System.putInt(
                                    context.contentResolver,
                                    Settings.System.SCREEN_BRIGHTNESS,
                                    (points * 255) / 100
                                )
                            }

                        }
                    } else {
                        try {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.WRITE_SETTINGS
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {

                                if (count == 5) {
                                    setAlphaLayout(0F)
                                }
                                Settings.System.putInt(
                                    context.contentResolver,
                                    Settings.System.SCREEN_BRIGHTNESS,
                                    (points * 255) / 100
                                )
                            } else {
                                if (count == 1) {
                                    showExplanation(
                                        "",
                                        context.getString(
                                            R.string.DESCRIBE_REQUEST_WRITE_SETTING
                                        )
                                    )
                                    detectChangeBrightness()
                                }
                            }
                        } catch (e: SecurityException) {
                            val viewDialog = ViewDialogOpenSettings(context, null)
                            viewDialog.binding.txtSetUpName.text =
                                context.getString(R.string.set_up_brightness)
                            viewDialog.binding.txtContentDialog.text =
                                context.getString(R.string.describe_brightness)
                            viewDialog.binding.icControl.setImageResource(
                                R.drawable.ic_brightness
                            )
                            viewDialog.binding.btnOpenSettings.setOnClickListener {
                                alertDialogBrightness.dismiss()
                                expandSettingsPanel()
                                endWindowManager()
                            }
                            viewDialog.binding.openHelper.setOnClickListener {
                                alertDialogBrightness.dismiss()
                                val intent = Intent(context, SystemShadeActivity::class.java)
                                intent.flags = FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(intent)
                                endWindowManager()
                            }
                            alertDialogBrightness.setCancelable(true)
                            alertDialogBrightness.window?.setBackgroundDrawable(
                                ColorDrawable(
                                    Color.TRANSPARENT
                                )
                            )
                            alertDialogBrightness.setContentView(viewDialog.binding.root)
                            if (Build.VERSION.SDK_INT >= 22)
                                alertDialogBrightness.window!!.setType(
                                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                                )
                            else
                                alertDialogBrightness.window!!.setType(
                                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                                )
                            alertDialogBrightness.show()
                        }
                    }
                }
            }

            override fun onStartTrackingTouch(boxedPoints: BoxedVertical?) {
                isChangingBrightness = true
                count = 0
            }

            override fun onStopTrackingTouch(boxedPoints: BoxedVertical?) {
                count = 0
                isChangingBrightness = false
                if (binding.layoutBtnFist.alpha == 0F) {
                    setAlphaLayout(1F)
                }

            }
        })
    }

    fun changeColorLine() {
        val color = Color.parseColor(
            PreferencesUtils.getString(
                BACKGROUND_COLOR,
                context.resources.getString(R.string.color_4DFFFFFF)
            )
        )
        // control
        binding.lastLine.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        // noti
//        val bgShape = bindingNoti.viewLine.background as GradientDrawable
//        bgShape.mutate()
//        bgShape.setColor(color)
    }

    private fun detectChangeBrightness() {
        binding.boxedVertical.value =
            ((Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            ) / 255F) * 100).toInt()
    }

    private fun setAlphaLayout(alpha: Float) {
        if (alpha == 0F) {
            alphaImageBackground = binding.imgBackgroundColor.alpha
        } else {
            binding.imgBackgroundColor.alpha = alphaImageBackground
        }
        binding.layoutTaskBar.alpha = alpha
        binding.controlCenter.alpha = alpha
        binding.layoutBtnFist.alpha = alpha
        binding.layoutBtnSecondsLine.alpha = alpha
        binding.layoutIconControls.alpha = alpha
        binding.layoutControlsSecondsLine.alpha = alpha
        if (alpha == 1F && binding.layoutLightScreen.translationY > 0) {
            binding.layoutIconCenterHide.alpha = alpha
        } else if (alpha == 0F) {
            binding.layoutIconCenterHide.alpha = alpha
        }
        binding.layoutIconA.alpha = alpha
        binding.lastLine.alpha = alpha
    }

    fun endWindowManager() {
        try {
            windowManager.removeView(binding.root)
            windowManager.updateViewLayout(bindingViewHide.root, setupLayout(false))
            isShowControl = false
            countDownTimer.cancel()
        } catch (e: Exception) {
            //
        }
    }

    fun endWindownNoti() {
        try {
            windowManager.removeView(bindingNoti.root)
            windowManager.updateViewLayout(bindingViewHide.root, setupLayout(false))
            isShowNotify = false
        } catch (e: Exception) {
            //
        }
    }

    @SuppressLint("WrongConstant")
    private fun expandSettingsPanel() {
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

    private fun intIconShape() {
        val iconShape = PreferencesUtils.getInteger(ICON_SHAPE, R.drawable.ic_round)
        binding.layoutIconControls.binding.backgroundIcSound.setImageResource(iconShape)
        binding.layoutIconControls.binding.backgroundBatterySaver.setImageResource(iconShape)
        binding.layoutIconControls.binding.backgroundAirPlane.setImageResource(iconShape)
        binding.layoutIconControls.binding.backgroundDoNotDisturb.setImageResource(iconShape)
        //
        binding.layoutControlsSecondsLine.binding.backgroundAutoRotate.setImageResource(
            iconShape
        )
        binding.layoutControlsSecondsLine.binding.backgroundNightLight.setImageResource(
            iconShape
        )
        binding.layoutControlsSecondsLine.binding.backgroundDarkTheme.setImageResource(
            iconShape
        )
        binding.layoutControlsSecondsLine.binding.backgroundHotspot.setImageResource(iconShape)
        //
        binding.layoutIconCenterHide.binding.backgroundIcDataSaver.setImageResource(iconShape)
        binding.layoutIconCenterHide.binding.backgroundIcScreenTransmission.setImageResource(
            iconShape
        )
        binding.layoutIconCenterHide.binding.backgroundIcNfc.setImageResource(iconShape)
        binding.layoutIconCenterHide.binding.backgroundIcLocation.setImageResource(iconShape)
        //
        binding.backgroundIcA.setImageResource(iconShape)
    }

    private fun callBackViewControlCenter() {
        binding.controlCenter.listenerEdit = {
            endWindowManager()
            val intent = Intent(context, Splash::class.java)
            intent.flags = FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    private fun setupTextColor(color: String) {
        binding.layoutBtnFist.binding.txtMobileData.setTextColor(Color.parseColor(color))
        binding.layoutBtnFist.binding.txtStatusMobileData.setTextColor(Color.parseColor(color))
        binding.layoutBtnFist.binding.name.setTextColor(Color.parseColor(color))
        binding.layoutBtnFist.binding.txtStatusWifi.setTextColor(Color.parseColor(color))
        //
        binding.layoutBtnSecondsLine.binding.txtBluetooth.setTextColor(Color.parseColor(color))
        binding.layoutBtnSecondsLine.binding.txtStatusBluetooth.setTextColor(
            Color.parseColor(
                color
            )
        )
        binding.layoutBtnSecondsLine.binding.txtFlashlight.setTextColor(Color.parseColor(color))
        binding.layoutBtnSecondsLine.binding.txtStatusFlash.setTextColor(
            Color.parseColor(color)
        )
        //
        binding.layoutIconControls.binding.txtMute.setTextColor(Color.parseColor(color))
        binding.layoutIconControls.binding.txtBatterySaver.setTextColor(Color.parseColor(color))
        binding.layoutIconControls.binding.txtAirplaneMode.setTextColor(Color.parseColor(color))
        binding.layoutIconControls.binding.txtDoNotDisturb.setTextColor(Color.parseColor(color))
        //
        binding.layoutControlsSecondsLine.binding.txtAutoRotate.setTextColor(
            Color.parseColor(
                color
            )
        )
        binding.layoutControlsSecondsLine.binding.txtNightLight.setTextColor(
            Color.parseColor(
                color
            )
        )
        binding.layoutControlsSecondsLine.binding.txtDarkTheme.setTextColor(
            Color.parseColor(
                color
            )
        )
        binding.layoutControlsSecondsLine.binding.txtHotspot.setTextColor(
            Color.parseColor(color)
        )
        //
        binding.layoutIconCenterHide.binding.txtDataSaver.setTextColor(Color.parseColor(color))
        binding.layoutIconCenterHide.binding.txtScreenTransmission.setTextColor(
            Color.parseColor(
                color
            )
        )
        binding.layoutIconCenterHide.binding.txtNfc.setTextColor(Color.parseColor(color))
        binding.layoutIconCenterHide.binding.txtLocation.setTextColor(Color.parseColor(color))
    }

    private fun setUpColorIcon(color: String) {
        if (!PreferencesUtils.getBoolean(
                context.resources.getString(R.string.state_usage_data),
                false
            ) && !PreferencesUtils.getBoolean(
                context.resources.getString(R.string.stata_usage_day),
                false
            )
        ) {
            binding.layoutBtnFist.binding.icDataMobile.setColorFilter(Color.parseColor(color))
        }
        binding.layoutBtnFist.binding.icWifi.setColorFilter(Color.parseColor(color))
        //
        binding.layoutBtnSecondsLine.binding.icBluetooth.setColorFilter(Color.parseColor(color))
        binding.layoutBtnSecondsLine.binding.icFlashLight.setColorFilter(
            Color.parseColor(color)
        )
        //
        binding.layoutIconControls.binding.icMute.setColorFilter(Color.parseColor(color))
        binding.layoutIconControls.binding.icBatterySaver.setColorFilter(
            Color.parseColor(color)
        )
        binding.layoutIconControls.binding.icAirplaneMode.setColorFilter(
            Color.parseColor(color)
        )
        binding.layoutIconControls.binding.icDoNotDisturb.setColorFilter(
            Color.parseColor(color)
        )
        //
        binding.layoutControlsSecondsLine.binding.icAutoRotate.setColorFilter(
            Color.parseColor(
                color
            )
        )
        binding.layoutControlsSecondsLine.binding.icNightLight.setColorFilter(
            Color.parseColor(
                color
            )
        )
        binding.layoutControlsSecondsLine.binding.icDarkTheme.setColorFilter(
            Color.parseColor(
                color
            )
        )
        binding.layoutControlsSecondsLine.binding.icHotspot.setColorFilter(
            Color.parseColor(
                color
            )
        )
        //
        binding.layoutIconCenterHide.binding.icDataSaver.setColorFilter(Color.parseColor(color))
        binding.layoutIconCenterHide.binding.icScreenTransmission.setColorFilter(
            Color.parseColor(
                color
            )
        )
        binding.layoutIconCenterHide.binding.icNfc.setColorFilter(Color.parseColor(color))
        binding.layoutIconCenterHide.binding.icLocation.setColorFilter(Color.parseColor(color))
        //
        binding.icA.setColorFilter(Color.parseColor(color))
    }

    private fun setUpDimmerColor(color: String) {
        val colorOpacity = color.substring(3)
        binding.boxedVertical.setBackgroundColorDimmer =
            (Color.parseColor("#CC${colorOpacity}"))
        binding.boxedVertical.setProgressColor = (Color.parseColor(color))
    }


    private fun showExplanation(title: String, message: String) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.allow) { _, _ ->
                val intent = Intent(context, BackGroundActivity::class.java)
                intent.putExtra("PERMISSION", "WRITE_SETTING")
                intent.flags = FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                endWindowManager()
            }
            .create()
        if (Build.VERSION.SDK_INT >= 22)
            alertDialog.window!!.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        else
            alertDialog.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        alertDialog.show()
    }

    private fun startScreenShot() {
        if (PreferencesUtils.getInteger(
                BACKGROUND
            ) == R.drawable.ic_backround_live_blur
        ) {
            val metrics = DisplayMetrics()
            if (screenShot == null) {
                screenShot = ScreenShotHelper(context, windowManager, metrics)
            }
            windowManager.defaultDisplay.getRealMetrics(metrics)
            intentData?.let {
                screenShot!!.captureScreen(intentData, intentData!!.getIntExtra("RESULT_CODE", -1))
                screenShot!!.listenerScreenShot = { bitmap ->
                    CoroutineScope(Dispatchers.Main).launch {
                        getBlurImageFromBitmap(
                            bitmap,
                            (PreferencesUtils.getInteger(OPACITY, 50)) / 10F,
                            (PreferencesUtils.getInteger(
                                context.resources.getString(R.string.transparent_amount),
                                50
                            ) / 100F) * 1.5F
                        )?.let {
                            if (!bitmap.isRecycled) {
                                bitmap.recycle()
                            }
                            binding.imgBackgroundColor.setImageBitmap(it)
                            bindingNoti.imgBackgroundNotification.setImageBitmap(it)
                        }
                    }
                }
            }
        }
    }

    private suspend fun getBlurImageFromBitmap(
        bitmap: Bitmap?,
        intensity: Float,
        contrast: Float = 0.7f
    ): Bitmap? {
        bitmap?.let {
            return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                var bitmapOut: Bitmap? = it

                val glContext = SharedContext.create().apply {
                    makeCurrent()
                }
                val handler = CGEImageHandler().apply {
                    initWithBitmap(bitmap)
                }

                handler.apply {
                    setFilterWithConfig(
                        MessageFormat.format(
                            "@blur lerp {0} @adjust contrast {1}",
                            (intensity / 10.0f).toString(),
                            contrast.toString()
                        )
                    )
                    processFilters()
                    resultBitmap?.let { result ->
                        bitmapOut = result
                    }
                }
                glContext.release()
                bitmapOut
            }
        } ?: run {
            return bitmap
        }
    }

    private fun checkAddViewCenter() {
        try {
            windowManager.addView(binding.root, setupLayout(true))
        } catch (e: java.lang.IllegalStateException) {
            windowManager.removeView(binding.root)
            windowManager.addView(binding.root, setupLayout(true))
        } catch (x: WindowManager.BadTokenException) {
        }
    }

    private fun checkAddViewNotification() {
        try {
            windowManager.addView(bindingNoti.root, setupLayout(true))
        } catch (e: java.lang.IllegalStateException) {
            windowManager.removeView(bindingNoti.root)
            windowManager.addView(bindingNoti.root, setupLayout(true))
        }
//        bindingNoti.root.translationY = -(bindingNoti.root.height.toFloat())
    }
}