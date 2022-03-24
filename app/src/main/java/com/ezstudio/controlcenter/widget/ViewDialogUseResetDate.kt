package com.ezstudio.controlcenter.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutDialogUseResetDateBinding

class ViewDialogUseResetDate(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs) {
    lateinit var binding: LayoutDialogUseResetDateBinding
    init {
        initView()
    }
    private fun initView() {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_dialog_use_reset_date, this, true)
        binding = LayoutDialogUseResetDateBinding.bind(view)
    }
}