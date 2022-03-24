package com.ezstudio.controlcenter.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutBtnSecondsLineBinding


class ViewButtonSecondsLine(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    lateinit var binding: LayoutBtnSecondsLineBinding

    init {
        initView()
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_btn_seconds_line, this, true)
        binding = LayoutBtnSecondsLineBinding.bind(view)
    }
}