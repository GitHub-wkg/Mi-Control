package com.ezstudio.controlcenter.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutBtnBinding


class ViewButton(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    lateinit var binding: LayoutBtnBinding

    init {
        initView()
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_btn, this, true)
        binding = LayoutBtnBinding.bind(view)
    }
}