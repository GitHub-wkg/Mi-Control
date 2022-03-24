package com.ezstudio.controlcenter.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutIconControlBinding

class ViewIconControl(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    lateinit var binding: LayoutIconControlBinding

    init {
        initView()
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_icon_control, this, true)
        binding = LayoutIconControlBinding.bind(view)
        binding.txtAirplaneMode.isSelected = true
        binding.txtMute.isSelected = true
        binding.txtBatterySaver.isSelected = true
        binding.txtDoNotDisturb.isSelected = true
    }
}