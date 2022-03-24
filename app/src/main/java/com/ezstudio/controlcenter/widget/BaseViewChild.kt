package com.ezstudio.controlcenter.widget

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.provider.Settings
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.activity.BackGroundActivity
import com.ezstudio.controlcenter.windown_manager.MyWindowManager
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.math.abs

open class BaseViewChild(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs) {

    var endLayoutManager: (() -> Unit)? = null
    lateinit var alertDialog: Dialog
    val animClickIcon = AnimationUtils.loadAnimation(context, R.anim.anim_click_icon)
    val BACKGROUND_COLOR = "BACKGROUND_COLOR"
    val SELECTED_COLOR = "SELECTED_COLOR"
    var myWindowManager: MyWindowManager? = null
    var startX = 0F
    var startY = 0F
    var endX = 0F
    var endY = 0F
    var countDownTimer: CountDownTimer? = null
    var lastClickItemView = 0L

    fun longClickCountTimer(event: MotionEvent, finishListener: (Unit) -> Unit) {
        startX = event.x
        startY = event.y
        myWindowManager?.actionDownWindowManager(event)
        countDownTimer = object : CountDownTimer(600, 600) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                finishListener(Unit)
            }
        }.start()
    }

    fun actionUpItemView(
        event: MotionEvent,
        duration: Long = 1000,
        finishListener: (Unit) -> Unit
    ) {
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
            if (System.currentTimeMillis() - lastClickItemView > duration) {
                lastClickItemView = System.currentTimeMillis()
                finishListener(Unit)
            }
        }
        countDownTimer?.cancel()
        countDownTimer = null
    }

    fun actionMoveView(event: MotionEvent) {
        val differenceX = abs(startX - event.x)
        val differenceY = abs(startY - event.y)
        if (differenceX > 200 || differenceY > 200) {
            countDownTimer?.cancel()
            countDownTimer = null
        }
        myWindowManager?.actionMoveWindowManager(event)
    }

    fun autoShowMaskBackgroundBlack() {
        myWindowManager?.binding?.imgBackgroundBlack?.visibility = View.VISIBLE
        myWindowManager?.binding?.imgBackgroundBlack?.alpha = 1F
        val alphaAnimation = AlphaAnimation(0.7F, 1F)
        alphaAnimation.fillBefore = true
        alphaAnimation.duration = 180
        myWindowManager?.binding?.imgBackgroundBlack?.startAnimation(
            alphaAnimation
        )
        Handler().postDelayed({
            val alphaAnimationEnd = AlphaAnimation(
                1F, 0.0F
            )
            alphaAnimationEnd.fillBefore = true
            alphaAnimationEnd.duration = 180
            myWindowManager?.binding?.imgBackgroundBlack?.startAnimation(
                alphaAnimationEnd
            )
            myWindowManager?.binding?.imgBackgroundBlack?.visibility =
                View.GONE
        }, 500)
    }

    fun startAnimVector(view: AppCompatImageView) {
        val drawable = view.drawable
        if (drawable is AnimatedVectorDrawableCompat) {
            drawable.start()
        } else if (drawable is AnimatedVectorDrawable) {
            drawable.start()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun showExplanationWriteSetting(title: String, message: String) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.allow) { _, _ ->
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                endLayoutManager?.invoke()
            }
            .create()
        if (Build.VERSION.SDK_INT >= 22)
            alertDialog.window!!.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        else
            alertDialog.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        alertDialog.show()
    }

    fun showExplanation(title: String, message: String) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.allow) { _, _ ->
                val intent = Intent(context, BackGroundActivity::class.java)
                intent.putExtra("PERMISSION", "WRITE_SETTING")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                endLayoutManager?.invoke()
            }
            .create()
        if (Build.VERSION.SDK_INT >= 22)
            alertDialog.window!!.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        else
            alertDialog.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        alertDialog.show()
    }

    fun showExplanationError(title: String, message: String) {
        val alertDialogShow = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        if (Build.VERSION.SDK_INT >= 22)
            alertDialogShow.window!!.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        else
            alertDialogShow.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        alertDialogShow.show()
    }

    @SuppressLint("WrongConstant")
    fun expandSettingsPanel() {
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