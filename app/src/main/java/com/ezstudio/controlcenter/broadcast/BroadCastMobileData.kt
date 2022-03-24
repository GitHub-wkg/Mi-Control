package com.ezstudio.controlcenter.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutBtnBinding
import com.ezteam.baseproject.utils.PreferencesUtils

class BroadCastMobileData : BroadcastReceiver() {
    lateinit var binding: LayoutBtnBinding
    private val BACKGROUND_COLOR = "BACKGROUND_COLOR"
    override fun onReceive(context: Context, intent: Intent) {

        if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action && !PreferencesUtils.getBoolean(
                context.resources.getString(R.string.state_usage_data),
                false
            ) && !PreferencesUtils.getBoolean(
                context.resources.getString(R.string.stata_usage_day),
                false
            )
        ) {
            if (isConnected(context)) {
                setColorDrawable(binding.btnMobileData, "#BF2CAF67")
                binding.txtStatusMobileData.text = context.getString(R.string.on)

            } else {
                setColorDrawable(
                    binding.btnMobileData,
                    PreferencesUtils.getString(
                        BACKGROUND_COLOR,
                        context.resources.getString(R.string.color_4DFFFFFF)
                    )
                )
                binding.txtStatusMobileData.text = context.getString(R.string.off)
            }
        }
    }

    private fun isConnected(context: Context): Boolean {
        val connManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mMobileNetwork = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (mMobileNetwork != null) {
            return mMobileNetwork.isConnected
        }
        return false
    }

    private fun setColorDrawable(layout: ConstraintLayout, color: String) {
        val bgShape = layout.background as GradientDrawable
        bgShape.mutate()
        bgShape.setColor(Color.parseColor(color))
    }
}