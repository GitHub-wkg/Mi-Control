package com.ezstudio.controlcenter.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.AnimatedVectorDrawable
import android.media.AudioManager
import androidx.appcompat.widget.AppCompatImageView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutStatusBinding
import com.ezstudio.controlcenter.databinding.WindownManagerBinding
import com.ezteam.baseproject.utils.PreferencesUtils

class BroadCastChangeRingerMode : BroadcastReceiver() {
    lateinit var binding: LayoutStatusBinding
    var layoutWindowManager: WindownManagerBinding? = null
    private val BACKGROUND_COLOR = "BACKGROUND_COLOR"
    private val SELECTED_COLOR = "SELECTED_COLOR"
    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == AudioManager.RINGER_MODE_CHANGED_ACTION) {
            val am = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            when (am.ringerMode) {
                AudioManager.RINGER_MODE_SILENT -> {
                    binding.icSound.setImageResource(R.drawable.ic_mute)
                    layoutWindowManager?.layoutIconControls?.binding?.icMute?.setImageResource(
                        R.drawable.ic_cc_qs_mute_on)
                    if (layoutWindowManager != null) {
                        startAnimVector(layoutWindowManager!!.layoutIconControls.binding.icMute)
                        layoutWindowManager?.layoutIconControls?.binding?.backgroundIcSound?.setColorFilter(
                            Color.parseColor(
                                PreferencesUtils.getString(
                                    BACKGROUND_COLOR,
                                    context.resources.getString(R.string.color_4DFFFFFF)
                                )
                            ),
                            PorterDuff.Mode.SRC_IN
                        )
                    }
                }
                AudioManager.RINGER_MODE_VIBRATE -> {
                    binding.icSound.setImageResource(R.drawable.ic_vibrate)
                    layoutWindowManager?.layoutIconControls?.binding?.icMute?.setImageResource(
                        R.drawable.ic_vibrate)
                    if (layoutWindowManager != null) {
                        layoutWindowManager?.layoutIconControls?.binding?.backgroundIcSound?.setColorFilter(
                            Color.parseColor(
                                PreferencesUtils.getString(
                                    SELECTED_COLOR,
                                    "#2C61CC"
                                )
                            ),
                            PorterDuff.Mode.SRC_IN
                        )
                    }
                }
                AudioManager.RINGER_MODE_NORMAL -> {
                    binding.icSound.setImageResource(R.drawable.ic_ring)
                    layoutWindowManager?.layoutIconControls?.binding?.icMute?.setImageResource(
                        R.drawable.ic_cc_qs_mute_off)
                    if (layoutWindowManager != null) {
                        startAnimVector(layoutWindowManager!!.layoutIconControls.binding.icMute)
                        layoutWindowManager?.layoutIconControls?.binding?.backgroundIcSound?.setColorFilter(
                            Color.parseColor(
                                PreferencesUtils.getString(
                                    SELECTED_COLOR,
                                    "#2C61CC"
                                )
                            ),
                            PorterDuff.Mode.SRC_IN
                        )
                    }
                }
            }
        }
    }

    private fun startAnimVector(view: AppCompatImageView) {
        val drawable = view.drawable
        if (drawable is AnimatedVectorDrawableCompat) {
            drawable.start()
        } else if (drawable is AnimatedVectorDrawable) {
            drawable.start()
        }
    }
}