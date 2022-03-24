package com.ezteam.baseproject.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import java.lang.IllegalStateException
import kotlin.collections.HashMap

abstract class BaseDialog<BD : ViewBinding, B : BuilderDialog> (var builder: B) : DialogFragment() {
    lateinit var binding: BD
    override fun show(manager: FragmentManager, tag: String?) {
        val t = javaClass.simpleName
        if (manager.findFragmentByTag(t) == null) {
            try {
                super.show(manager, t)
            } catch (e: IllegalStateException) {}
        }
    }

    protected abstract val viewBinding: BD
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(
            requireActivity()
        )
        binding = viewBinding
        dialogBuilder.setView(binding.root)
        initView()
        initListener()
        return dialogBuilder.create()
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        val window: Window? = dialog?.window
        val windowParams = window?.attributes
        windowParams?.dimAmount = 0.7f
        windowParams?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        window?.attributes = windowParams
        dialog?.setCancelable(builder.cancelable)
        dialog?.setCanceledOnTouchOutside(builder.canOnTouchOutside)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    open fun initView() {
        title?.let {
            it.text = builder.title.orEmpty()
        }

        positiveButton?.let {
            if (!builder.positiveButton.isNullOrEmpty()) {
                it.text = builder.positiveButton
                it.setOnClickListener {
                    handleClickPositiveButton(HashMap())
                }
            }
        }

        negativeButton?.let {
            if (!builder.negativeButton.isNullOrEmpty()) {
                it.text = builder.negativeButton
                it.setOnClickListener(::handleClickNegativeButton)
            }
        }

        message?.let {
            it.text = builder.message.orEmpty()
        }
    }

    protected abstract fun initListener()
    protected open val positiveButton: TextView?
        get() = null
    protected open val negativeButton: TextView?
        get() = null
    protected open val title: TextView?
        get() = null
    protected open val message: TextView?
        get() = null

    protected open fun handleClickNegativeButton(view: View) {
        builder.negativeButtonListener?.let {
            it(this)
        }
        dismiss()
    }

    protected open fun handleClickPositiveButton(data: HashMap<String?, Any?>) {
        builder.positiveButtonListener?.let {
            it(this, data)
        }
        dismiss()
    }
}