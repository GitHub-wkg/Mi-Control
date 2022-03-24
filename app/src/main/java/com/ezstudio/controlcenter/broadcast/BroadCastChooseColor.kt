package com.ezstudio.controlcenter.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutDialogBackgroundBinding
import com.ezteam.baseproject.utils.PreferencesUtils

class BroadCastChooseColor : BroadcastReceiver() {
    lateinit var binding: LayoutDialogBackgroundBinding
    private val COLOR = "COLOR"
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            if (context != null) {
                if (it.action == context.resources.getString(R.string.action_choose_color)) {
                    val bgShape = binding.icShapeColor.background as GradientDrawable
                    bgShape.mutate()
                    bgShape.setColor(
                        Color.parseColor(
                            PreferencesUtils.getString(
                                COLOR
                            )
                        )
                    )
                }
            }

        }
    }


}