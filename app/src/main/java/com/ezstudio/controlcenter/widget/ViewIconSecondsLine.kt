package com.ezstudio.controlcenter.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutIconSecondsLineBinding

class ViewIconSecondsLine(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    lateinit var binding: LayoutIconSecondsLineBinding
    init {
        initView()
    }
    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_icon_seconds_line, this, true)
        binding = LayoutIconSecondsLineBinding.bind(view)
        binding.txtAutoRotate.isSelected = true
        binding.txtNightLight.isSelected = true
        binding.txtDarkTheme.isSelected = true
        binding.txtHotspot.isSelected = true
    }
}