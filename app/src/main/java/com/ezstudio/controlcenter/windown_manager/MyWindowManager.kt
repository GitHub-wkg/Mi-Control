                                                                                                                                                                                                                                                                                       package com.ezstudio.controlcenter.windown_manager

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezstudio.controlcenter.MyGroupView
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutViewHideBinding
import com.ezstudio.controlcenter.databinding.WindownManagerBinding
import kotlinx.android.synthetic.main.layout_btn_seconds_line.view.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper


                                                                                                                                                                                                                                                                                       class MyWindowManager(var context: Context) {
    private lateinit var binding: WindownManagerBinding
    private lateinit var bindingViewHide: LayoutViewHideBinding
    private lateinit var windowManager: WindowManager
    private var dragY: Float = 0F
    private var countTouch = 0
    private var alpha = 0F
    private var isShow = false
    private var isViewHide = false
    private var isFullView = false
    private var marginX = 0
    private var dragScroll = 0F
    private var actionUp = true
    private var isHiding = false

    init {
        viewLayout()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun viewLayout() {

        windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        val myGroup = MyGroupView(context)
        binding = WindownManagerBinding.inflate(LayoutInflater.from(context), myGroup, false)
        bindingViewHide = LayoutViewHideBinding.inflate(LayoutInflater.from(context), myGroup, false)
        OverScrollDecoratorHelper.setUpOverScroll(binding.scrollViewContent)
        bindingViewHide.txtTopManager.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    isShow = false
                    countTouch = 0
                    dragY = event.y
                    binding.layoutWindow.alpha = 0.1F
                    alpha = 0.1F
                    dragScroll = event.y
                    binding.layoutContent.alpha = 0F
                    windowManager.addView(binding.root, setupLayout(true))
                }
                MotionEvent.ACTION_UP -> {
                    if (binding.layoutContent.y > 0) {
                        val valueAnimator = ValueAnimator.ofFloat(binding.layoutContent.y, 0f)
                        valueAnimator.duration = 450
                        valueAnimator.interpolator = DecelerateInterpolator()
                        valueAnimator.addUpdateListener { valueAnimator ->
                            binding.layoutContent.y = valueAnimator.animatedValue as Float
                        }
                        valueAnimator.start()
                    }
                    if (!isShow) {
                        val alphaAnimation = AlphaAnimation(alpha, 0F)
                        alphaAnimation.duration = 200
                        binding.imgBackgroundColor.startAnimation(alphaAnimation)
                        val handle = Handler()
                        handle.postDelayed({
                            windowManager.removeViewImmediate(binding.root)
                            windowManager.updateViewLayout(bindingViewHide.root, setupLayout(false))
                        }, 200)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if ((event.y - dragY) > 300F) {
                        dragY += ((event.y - dragY) - 300F)
                    } else if (event.y - dragY < 0) {
                        dragY = event.y
                    }
                    if ((event.y - dragScroll) > 200F) {
                        binding.layoutContent.y = ((event.y - dragScroll) - 200F) / 5F
                    } else {
                        binding.layoutContent.y = 0F
                    }
                    binding.layoutWindow.alpha = (event.y - dragY) / 300F
                    alpha = (event.y - dragY) / 300F
                    if (((event.y - dragY) / 300F) == 1F) {
                        countTouch++
                        if (countTouch == 1) {
                            isShow = true
                            isViewHide = false
                            binding.layoutContent.alpha = 1F
                            windowManager.updateViewLayout(binding.root, setupLayout(true))
                            startAnimationEnter()
                        }
                    } else {
                        if (!isViewHide) {
                            windowManager.updateViewLayout(binding.root, setupLayout(true))
                        }
                    }
                    if (isShow && ((event.y - dragY) / 300F) < 0.9) {
                        countTouch = 0
                        isShow = false
                        startAnimationExit()
                        val handle = Handler()
                        handle.postDelayed({
                            isViewHide = true
                            binding.layoutContent.alpha = 0F
                            windowManager.updateViewLayout(binding.root, setupLayout(true))
                        }, 180)
                    }
                }
            }
            //
            true
        }
        //
        binding.scrollViewContent.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    isViewHide = false
                    alpha = 1F
                    countTouch = 0
                    dragY = event.y
                    if (isFullView) {
                        dragScroll = event.y
                    }
                }
                MotionEvent.ACTION_UP -> {
                    actionUp = true
                    isHiding = false
                    if (!isViewHide) {
                        binding.layoutWindow.alpha = 1F
                        binding.imgBackgroundColor.alpha = 1F
                        if (marginX in 100..binding.layoutIconCenterHide.height) {
//                            val animTranslate = TranslateAnimation(0F, 0F, 0F, 100F - marginX)
                            marginX = binding.layoutIconCenterHide.height
                            binding.layoutIconCenterHide.alpha = 1F
                            isFullView = true
//                            Log.d("huy", "-> ACTION_UP done $marginX")
//                            animTranslate.fillAfter = true
//                            animTranslate.duration = 200
//                            binding.layoutLightScreen.startAnimation(animTranslate)
                            val layoutLightScreenParams = binding.layoutLightScreen.layoutParams as (ConstraintLayout.LayoutParams)
                            layoutLightScreenParams.setMargins(0, binding.layoutIconCenterHide.height, 0, 0)
                            binding.layoutLightScreen.requestLayout()
                            windowManager.updateViewLayout(binding.root, setupLayout(true))


                        } else if (marginX in 0..99) {
//                            val animTranslate = TranslateAnimation(0F, 0F, 0F, 0F - marginX)
                            marginX = 0
                            isFullView = false
//                            Log.d("huy", "-> ACTION_UP done $marginX")
//                            animTranslate.fillAfter = true
//                            animTranslate.duration = 200
//                            binding.layoutLightScreen.startAnimation(animTranslate)
                            val layoutLightScreenParams = binding.layoutLightScreen.layoutParams as (ConstraintLayout.LayoutParams)
                            layoutLightScreenParams.setMargins(0, 0, 0, 0)
                            binding.layoutLightScreen.requestLayout()
                            windowManager.updateViewLayout(binding.root, setupLayout(true))
                        }
                        if (alpha == 1F) {
                            binding.layoutContent.alpha = 1F
                            windowManager.updateViewLayout(binding.root, setupLayout(true))
                        }
                    } else {
                        val anim = AlphaAnimation(alpha, 0F)
                        anim.duration = 200
                        binding.layoutWindow.startAnimation(anim)
                        binding.imgBackgroundColor.startAnimation(anim)
                        val handle = Handler()
                        handle.postDelayed({
                            windowManager.removeViewImmediate(binding.root)
                            windowManager.updateViewLayout(bindingViewHide.root, setupLayout(false))
                        }, 200)
                    }
                    //
                    if (binding.layoutContent.y > 0) {
//                        startValueAnimator(binding.layoutContent)
//                        startValueAnimator(binding.layoutTaskBar)
//                        startValueAnimator(binding.controlCenter)
//                        startValueAnimator(binding.layoutBtnFist)
//                        startValueAnimator(binding.layoutBtnSecondsLine)
//                        startValueAnimator(binding.layoutBtnSecondsLine)
//                        startValueAnimator(binding.icControls)
//                        startValueAnimator(binding.icControlsSecondsLine)
//                        startValueAnimator(binding.layoutLightScreen)
//                        startValueAnimator(binding.lastLine)
//                        startValueAnimator(binding.layoutIconCenterHide)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
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
                        dragY - event.y > 300 -> {
                            dragY -= (dragY - event.y - 300)
                        }
                        dragY - event.y < 0 && !isHiding -> {
                            marginX += (0 - (dragY - event.y)).toInt()
                            if (marginX > binding.layoutIconCenterHide.height) {
                                marginX = binding.layoutIconCenterHide.height
                                binding.layoutIconCenterHide.alpha = 1F
                                isFullView = true
                            } else {
                                binding.layoutIconCenterHide.alpha = marginX / binding.layoutIconCenterHide.height.toFloat()
                            }
                            dragY = event.y

                        }
                        dragY - event.y > 0 && marginX > 0 && !isHiding -> {
                            marginX -= (dragY - event.y).toInt()
                            if (marginX < 0) {
                                marginX = 0
                                binding.layoutIconCenterHide.alpha = 0F
                            } else {
                                binding.layoutIconCenterHide.alpha = marginX / binding.layoutIconCenterHide.height.toFloat()
                                dragY = event.y
                            }
                        }
                    }
                    if (isFullView) {
                        dragY = event.y
                    }
//                    if (marginX > 0) {
//                        dragY = event.y
//                    }
                    //
                    if (event.y >= dragScroll && actionUp) {
//                        binding.layoutContent.translationY = (event.y - dragScroll) / 6F
//                        binding.layoutTaskBar.translationY = (event.y - dragScroll) / 15F
//                        binding.controlCenter.translationY = (event.y - dragScroll) / 14F
//                        binding.layoutBtnFist.translationY = (event.y - dragScroll) / 13F
//                        binding.layoutBtnSecondsLine.translationY = (event.y - dragScroll) / 12F
//                        binding.layoutBtnSecondsLine.translationY = (event.y - dragScroll) / 12F
//                        binding.icControls.translationY = (event.y - dragScroll) / 10.8F
//                        binding.icControlsSecondsLine.translationY = (event.y - dragScroll) / 9.6F
//                        binding.layoutLightScreen.translationY = (event.y - dragScroll) / 7.2F
//                        binding.lastLine.translationY = (event.y - dragScroll) / 19F
//                        binding.layoutIconCenterHide.translationY = (event.y - dragScroll) / 8.4F
                    }
                    binding.layoutWindow.alpha = 1 - (((dragY - event.y)) / 300)
                    alpha = 1 - (((dragY - event.y)) / 300)
                    windowManager.updateViewLayout(binding.root, setupLayout(true))
                    if ((1 - (((dragY - event.y)) / 300)) < 0.9F) {
                        countTouch++
                        if (countTouch == 1) {
                            isViewHide = true
                            startAnimationExit()
                            val handler = Handler()
                            handler.postDelayed({
                                binding.layoutContent.alpha = 0F
                                windowManager.updateViewLayout(binding.root, setupLayout(true))
                            }, 200)
                        }

                    } else {
                        if (isViewHide && 1 - ((dragY - event.y) / 300) >= 0.9) {
                            isViewHide = false
                            countTouch = 0
                            binding.layoutContent.alpha = 1F
                            windowManager.updateViewLayout(binding.root, setupLayout(true))
                            startAnimationEnter()
                        }
                    }
                    if (!isHiding) {
                        if (!isViewHide && 1 - ((dragY - event.y) / 300) == 1F && marginX <= binding.layoutIconCenterHide.height) {
                            val layoutLightScreenParams = binding.layoutLightScreen.layoutParams as (ConstraintLayout.LayoutParams)
                            if (layoutLightScreenParams.topMargin == 0 && marginX == 0
                                    || layoutLightScreenParams.topMargin == binding.layoutIconCenterHide.height && marginX == binding.layoutIconCenterHide.height) {
                                //
                            } else {
                                layoutLightScreenParams.setMargins(0, marginX, 0, 0)
                                binding.layoutLightScreen.requestLayout()
                                windowManager.updateViewLayout(binding.root, setupLayout(true))
                            }
                        } else if (marginX > binding.layoutIconCenterHide.height) {
                            marginX = binding.layoutIconCenterHide.height
                        } else if (marginX < 0) {
                            marginX = 0
                        }
                    }
                }
            }
            //
            true
        }
        windowManager.addView(bindingViewHide.root, setupLayout(false))
    }

    private fun setupLayout(open: Boolean): WindowManager.LayoutParams {
        val mLayoutParams: WindowManager.LayoutParams
        val flag = if (open) WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        else (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        val height = when (open) {
            true -> {
                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                val height = displayMetrics.heightPixels
                //
                val resources: Resources = context.resources
                val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
                if (resourceId > 0) {
                    height + resources.getDimensionPixelSize(resourceId)
                } else {
                    WindowManager.LayoutParams.MATCH_PARENT
                }

            }
            false -> {
                100
            }
        }
        mLayoutParams = WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, height, type, flag, PixelFormat.TRANSLUCENT)
        mLayoutParams.gravity = Gravity.TOP
        mLayoutParams.alpha = 1F
//
        return mLayoutParams
    }

    private fun startAnimationEnter() {
        val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.window_enter)
        binding.layoutTaskBar.binding.nameSim.startAnimation(animation)
        binding.layoutTaskBar.binding.viewsStatus.startAnimation(animation)
        //
        binding.controlCenter.binding.txtControlCenter.startAnimation(animation)

        //
        binding.layoutBtnFist.binding.btnMobileData.startAnimation(animation)
        binding.layoutBtnFist.binding.btnWifi.startAnimation(animation)
        binding.layoutBtnSecondsLine.binding.btnBluetooth.startAnimation(animation)
        binding.layoutBtnSecondsLine.binding.btnFlashlight.startAnimation(animation)
        //
        val scale = ScaleAnimation(0.8F, 1F, 0.8F, 1F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scale.duration = 180
        binding.icControls.binding.layoutIconMute.startAnimation(scale)
        binding.icControls.binding.layoutIconBatterySaver.startAnimation(scale)
        binding.icControls.binding.layoutIconAirplaneMode.startAnimation(scale)
        binding.icControls.binding.layoutIconDoNotDisturb.startAnimation(scale)
        binding.icControlsSecondsLine.binding.layoutIconAutoRotate.startAnimation(scale)
        binding.icControlsSecondsLine.binding.layoutNightLight.startAnimation(scale)
        binding.icControlsSecondsLine.binding.layoutDarkTheme.startAnimation(scale)
        binding.icControlsSecondsLine.binding.layoutHotspot.startAnimation(scale)
        //
        binding.icA.startAnimation(scale)
        binding.boxedVertical.startAnimation(scale)
        //
        binding.controlCenter.binding.icSetting.startAnimation(scale)
        binding.controlCenter.binding.icEdit.startAnimation(scale)
        //
        binding.lastLine.startAnimation(scale)
    }

    private fun startAnimationExit() {
        val alphaAnimation: Animation = AnimationUtils.loadAnimation(context, R.anim.window_exit)
        binding.layoutTaskBar.binding.nameSim.startAnimation(alphaAnimation)
        binding.layoutTaskBar.binding.viewsStatus.startAnimation(alphaAnimation)
        //
        binding.controlCenter.binding.txtControlCenter.startAnimation(alphaAnimation)
        //
        binding.layoutBtnFist.binding.btnMobileData.startAnimation(alphaAnimation)
        binding.layoutBtnFist.binding.btnWifi.startAnimation(alphaAnimation)
        binding.layoutBtnSecondsLine.binding.btnBluetooth.startAnimation(alphaAnimation)
        binding.layoutBtnSecondsLine.binding.btnFlashlight.startAnimation(alphaAnimation)
        //
        val scale = ScaleAnimation(1F, 0.5F, 1F, 0.5F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scale.duration = 200

        binding.icControls.binding.layoutIconMute.startAnimation(scale)
        binding.icControls.binding.layoutIconBatterySaver.startAnimation(scale)
        binding.icControls.binding.layoutIconAirplaneMode.startAnimation(scale)
        binding.icControls.binding.layoutIconDoNotDisturb.startAnimation(scale)
        binding.icControlsSecondsLine.binding.layoutIconAutoRotate.startAnimation(scale)
        binding.icControlsSecondsLine.binding.layoutNightLight.startAnimation(scale)
        binding.icControlsSecondsLine.binding.layoutDarkTheme.startAnimation(scale)
        binding.icControlsSecondsLine.binding.layoutHotspot.startAnimation(scale)
        //
        binding.icA.startAnimation(scale)
        binding.boxedVertical.startAnimation(scale)
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

}