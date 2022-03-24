package com.ezstudio.controlcenter.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutIconCenterHideBinding

class ViewControlCenterHide(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    lateinit var binding: LayoutIconCenterHideBinding

    init {
        initView()
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_icon_center_hide, this, true)
        binding = LayoutIconCenterHideBinding.bind(view)
        binding.txtAirplaneMode.isSelected = true
        binding.txtMute.isSelected = true
        binding.txtBatterySaver.isSelected = true
        binding.txtDoNotDisturb.isSelected = true
    }
}