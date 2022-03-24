package com.ezstudio.controlcenter.model

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup

data class ItemSystemShade(
    val resId: Int,
    val name: String,
    var isExpandable: Boolean = false,
    var listExpandable: MutableList<ItemExpandable>
) : ExpandableGroup<ItemExpandable>(name,listExpandable)