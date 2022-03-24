package com.ezstudio.controlcenter.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutDialogSetUpSettingsBinding

class ViewDialogOpenSettings(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs) {
    lateinit var binding: LayoutDialogSetUpSettingsBinding

    init {
        initView()

    }

    private fun initView() {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_dialog_set_up_settings, this, true)
        binding = LayoutDialogSetUpSettingsBinding.bind(view)

    }
}