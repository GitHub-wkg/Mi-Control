package com.ezstudio.controlcenter.customview

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.ezstudio.controlcenter.R

class MyTextView(context: Context, attributeSet: AttributeSet) : AppCompatTextView(context, attributeSet) {
    init {

        val typeface = ResourcesCompat.getFont(context, R.font.font)
        setTypeface(typeface)
    }
}