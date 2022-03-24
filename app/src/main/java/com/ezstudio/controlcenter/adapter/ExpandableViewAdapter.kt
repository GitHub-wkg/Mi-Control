package com.ezstudio.controlcenter.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.ezstudio.controlcenter.databinding.ItemSystemShadeBinding
import com.ezstudio.controlcenter.databinding.LayoutExpandableRecycleViewBinding
import com.ezstudio.controlcenter.model.ItemExpandable
import com.ezstudio.controlcenter.model.ItemSystemShade
import com.ezstudio.controlcenter.viewholder.ExpandableViewHolder
import com.ezstudio.controlcenter.viewholder.ViewHolder
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup

class ExpandableViewAdapter(groups: MutableList<out ExpandableGroup<ItemExpandable>>?) :
    ExpandableRecyclerViewAdapter<ViewHolder, ExpandableViewHolder>(groups) {
    override fun onCreateGroupViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSystemShadeBinding.inflate(
                LayoutInflater.from(parent?.context),
                parent,
                false
            )
        )
    }

    override fun onCreateChildViewHolder(parent: ViewGroup?, viewType: Int): ExpandableViewHolder {
        return ExpandableViewHolder(
            LayoutExpandableRecycleViewBinding.inflate(
                LayoutInflater.from(parent?.context),
                parent,
                false
            )
        )
    }

    override fun onBindChildViewHolder(
        holder: ExpandableViewHolder?,
        flatPosition: Int,
        group: ExpandableGroup<*>?,
        childIndex: Int
    ) {
        val dataExpand = group?.items?.get(childIndex) as ItemExpandable
        holder?.bind(dataExpand, group as ItemSystemShade)
    }

    override fun onBindGroupViewHolder(
        holder: ViewHolder?,
        flatPosition: Int,
        group: ExpandableGroup<*>?
    ) {
        holder?.bind(group as ItemSystemShade)
    }


}