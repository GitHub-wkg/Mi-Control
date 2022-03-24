package com.ezstudio.controlcenter.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutDialogResetDefaultBinding

class ViewDiaLogResetDefault(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs) {
    lateinit var binding: LayoutDialogResetDefaultBinding
    init {
        initView()
    }

    private fun initView() {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_dialog_reset_default, this, true)
        binding = LayoutDialogResetDefaultBinding.bind(view)
    }
}