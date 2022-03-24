package com.ezteam.baseproject.dialog

import android.view.LayoutInflater
import com.ezteam.baseproject.databinding.DialogLoadingBinding

class DialogLoading(builder: ExtendBuilder) :

    BaseDialog<DialogLoadingBinding, DialogLoading.ExtendBuilder>(builder) {

    class ExtendBuilder : BuilderDialog() {

        override fun build(): BaseDialog<*, *> {
            return DialogLoading(this)
        }

    }

    override fun initView() {
        super.initView()
    }

    override fun initListener() {

    }

    override val viewBinding: DialogLoadingBinding
        get() = DialogLoadingBinding.inflate(LayoutInflater.from(context))
}