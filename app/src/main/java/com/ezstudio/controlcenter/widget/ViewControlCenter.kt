package com.ezstudio.controlcenter.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutControlCenterBinding

class ViewControlCenter(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
     lateinit var  binding  : LayoutControlCenterBinding

    init {
        initView()
    }

    private fun initView() {
        val view  =  LayoutInflater.from(context).inflate(R.layout.layout_control_center,this,true)
        binding  = LayoutControlCenterBinding.bind(view)

    }
}