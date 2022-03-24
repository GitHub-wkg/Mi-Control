package com.ezstudio.controlcenter.viewholder

import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.RotateAnimation
import com.ezstudio.controlcenter.databinding.ItemSystemShadeBinding
import com.ezstudio.controlcenter.model.ItemSystemShade
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder


class ViewHolder(var binding: ItemSystemShadeBinding) : GroupViewHolder(binding.root) {
    fun bind(itemSystemShade: ItemSystemShade) {
        binding.imgIc.setImageResource(itemSystemShade.resId)
        binding.nameIcon.text = itemSystemShade.name

    }
    override fun expand() {
        animateExpand()
    }

    override fun collapse() {
        animateCollapse()
    }

    private fun animateExpand() {
        val rotate = RotateAnimation(360F, 180F, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f)
        rotate.setDuration(300)
        rotate.setFillAfter(true)
        binding.icShow.animation = rotate
    }

    private fun animateCollapse() {
        val rotate = RotateAnimation(180F, 360F, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f)
        rotate.setDuration(300)
        rotate.setFillAfter(true)
        binding.icShow.animation = rotate

    }
}