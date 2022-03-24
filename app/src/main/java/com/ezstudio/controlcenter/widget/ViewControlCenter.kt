package com.ezstudio.controlcenter.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import android.provider.Settings.ACTION_SETTINGS
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.core.content.ContextCompat.startActivity
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutControlCenterBinding
import com.ezstudio.controlcenter.windown_manager.MyWindowManager
import kotlin.math.abs

class ViewControlCenter(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    lateinit var binding: LayoutControlCenterBinding
    var listenerEdit: (() -> Unit)? = null
    private var startX = 0F
    private var startY = 0F
    private var endX = 0F
    private var endY = 0F
    private val animClickIcon = AnimationUtils.loadAnimation(context, R.anim.anim_click_icon)
    var myWindowManager: MyWindowManager? = null
    private var countDownTimer: CountDownTimer? = null

    init {
        initView()
    }

    fun onSetting() {
        val intent = Intent(ACTION_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(context, intent, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_control_center, this, true)
        binding = LayoutControlCenterBinding.bind(view)
        binding.icEdit.setOnTouchListener { v, event ->
            when (event!!.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    myWindowManager?.actionDownWindowManager(event)
                    countDownTimer = object : CountDownTimer(600, 600) {
                        override fun onTick(millisUntilFinished: Long) {
                        }

                        override fun onFinish() {
                        }
                    }.start()
                }
                MotionEvent.ACTION_UP -> {
                    endX = event.x
                    endY = event.y
                    myWindowManager?.actionUpWindowManager(event)
                    if (myWindowManager!!.isAClick(
                            startX,
                            endX,
                            startY,
                            endY
                        ) && countDownTimer != null
                    ) {
                        v.startAnimation(animClickIcon)
                        Handler().postDelayed({
                            listenerEdit?.invoke()
                            myWindowManager?.binding?.root?.invalidate()
                            myWindowManager?.binding?.layoutWindow?.invalidate()
                        }, 200)
                    }
                    countDownTimer?.cancel()
                    countDownTimer = null
                }
                MotionEvent.ACTION_MOVE -> {
                    val differenceX = abs(startX - event.x)
                    val differenceY = abs(startY - event.y)
                    if (differenceX > 200 || differenceY > 200) {
                        countDownTimer?.cancel()
                        countDownTimer = null
                    }
                    myWindowManager?.actionMoveWindowManager(event)
                }
            }
            true
        }
    }
}