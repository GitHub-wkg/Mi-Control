package com.ezstudio.controlcenter.broadcast

import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutViewNotificationBinding
import com.ezstudio.controlcenter.databinding.WindownManagerBinding
import com.ezstudio.controlcenter.service.MyAccessibilityService
import com.ezteam.baseproject.utils.PreferencesUtils
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wysaid.common.SharedContext
import org.wysaid.nativePort.CGEImageHandler
import java.io.File
import java.text.MessageFormat

class BroadCastBlurImage : BroadcastReceiver() {
    lateinit var binding: WindownManagerBinding
    lateinit var bindingNoti: LayoutViewNotificationBinding
    private val BACKGROUND_MODEL = "BACKGROUND_MODEL"
    private val BACKGROUND = "BACKGROUND"
    private val PATH_IMAGE = "PATH_IMAGE"
    private val OPACITY = "OPACITY"
    private val COLOR = "COLOR"
    private val PATH_CROP = "PATH_CROP"
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            if (context != null) {
                if (it.action == context.resources.getString(R.string.action_blur_image) && MyAccessibilityService.isRunning) {
                    setUpBackground(context, it)
                }
            }
        }
    }

    private fun setUpBackground(context: Context, intent: Intent) {
        when (PreferencesUtils.getInteger(BACKGROUND)) {
            R.drawable.ic_background_image -> {
                if (PreferencesUtils.getString(
                        BACKGROUND_MODEL,
                        "null"
                    ) == context.resources.getString(R.string.back_ground)
                ) {
                    binding.imgBackgroundColor.alpha = 1F
                    bindingNoti.imgBackgroundNotification.alpha = 1F
                    val drawable = WallpaperManager.getInstance(context).drawable
                    // cotrol
                    binding.imgBackgroundColor.setImageDrawable(drawable)
                    binding.imgBackgroundColor.post {
                        blurBackGround(
                            binding.imgBackgroundColor,
                            context
                        )
                    }
                    // noti
                    bindingNoti.imgBackgroundNotification.setImageDrawable(drawable)
                    bindingNoti.imgBackgroundNotification.post {
                        blurBackGround(bindingNoti.imgBackgroundNotification, context)
                    }

                } else if (PreferencesUtils.getString(
                        BACKGROUND_MODEL
                    ) == context.resources.getString(
                        R.string.custom_photo
                    )
                ) {
                    var bitMap: Bitmap? = null
                    if (UCrop.getOutput(intent)?.path != null) {
                        PreferencesUtils.putString(PATH_CROP, UCrop.getOutput(intent)?.path)
                        bitMap = MediaStore.Images.Media.getBitmap(
                            context.contentResolver,
                            UCrop.getOutput(intent)
                        )
                    }
                    val path = PreferencesUtils.getString(PATH_IMAGE, null)
                    binding.imgBackgroundColor.alpha = 1F
                    bindingNoti.imgBackgroundNotification.alpha = 1F
                    if (path != null) {
//                        val uri = Uri.parse(File(PreferencesUtils.getString(PATH_CROP)).toString())
                        PreferencesUtils.putBoolean(
                            context.resources.getString(R.string.isCropSuccess),
                            false
                        )
                        bitMap?.let {
                            //control
                            binding.imgBackgroundColor.setImageBitmap(it)
                            binding.imgBackgroundColor.post {
                                blurBackGround(binding.imgBackgroundColor, context, it)
                            }
                            //noti
                            bindingNoti.imgBackgroundNotification.setImageBitmap(it)
                            bindingNoti.imgBackgroundNotification.post {
                                blurBackGround(bindingNoti.imgBackgroundNotification, context, it)
                            }
                        }
                    }
                } else {
                    binding.imgBackgroundColor.alpha = 1F
                    bindingNoti.imgBackgroundNotification.alpha = 1F
                    // control
                    Glide.with(context).load(R.drawable.background_default)
                        .into(binding.imgBackgroundColor)
                    binding.imgBackgroundColor.post {
                        blurBackGroundDefault(
                            binding.imgBackgroundColor,
                            context
                        )
                    }
                    // noti
                    Glide.with(context).load(R.drawable.background_default)
                        .into(bindingNoti.imgBackgroundNotification)
                    bindingNoti.imgBackgroundNotification.post {
                        blurBackGroundDefault(bindingNoti.imgBackgroundNotification, context)
                    }
                }
            }
            R.drawable.ic_background_color -> {
                binding.imgBackgroundColor.setImageResource(android.R.color.transparent)
                bindingNoti.imgBackgroundNotification.setImageResource(android.R.color.transparent)
                binding.imgBackgroundColor.setBackgroundColor(
                    Color.parseColor(
                        PreferencesUtils.getString(
                            COLOR,
                            "#0094FF"
                        )
                    )
                )
                // noti
                bindingNoti.imgBackgroundNotification.setBackgroundColor(
                    Color.parseColor(
                        PreferencesUtils.getString(
                            COLOR,
                            "#0094FF"
                        )
                    )
                )
                //
                binding.imgBackgroundColor.alpha = PreferencesUtils.getInteger(OPACITY, 50) / 100F
                bindingNoti.imgBackgroundNotification.alpha =
                    PreferencesUtils.getInteger(OPACITY, 50) / 100F
            }
        }
    }

    private fun blurBackGround(
        imageView: AppCompatImageView,
        context: Context,
        bit: Bitmap? = null
    ) {
        val bitmap = bit ?: imageView.drawable.toBitmap()
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
                imageView.setImageBitmap(it)
            }
        }
    }

    private fun blurBackGroundDefault(imageView: AppCompatImageView, context: Context) {
        val bitmap =
            BitmapFactory.decodeResource(context.getResources(), R.drawable.background_default)
        CoroutineScope(Dispatchers.Main).launch {
            getBlurImageFromBitmap(
                bitmap,
                (PreferencesUtils.getInteger(OPACITY, 50)) / 10F,
                (PreferencesUtils.getInteger(
                    context.resources.getString(R.string.transparent_amount),
                    50
                ) / 100F) * 1.5F
            )?.let {
                imageView.setImageBitmap(it)
            }
        }
    }

    private suspend fun getBlurImageFromBitmap(
        bitmap: Bitmap?,
        intensity: Float,
        contrast: Float = 0.7f
    ): Bitmap? {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            var bitmapOut: Bitmap? = null
            bitmap?.let {
                val glContext = SharedContext.create().apply {
                    makeCurrent()
                }
                val handler = CGEImageHandler().apply {
                    initWithBitmap(it)
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

                    bitmapOut = resultBitmap
                }
                glContext.release()
            }
            bitmapOut
        }
    }
}