package com.ezstudio.controlcenter.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezstudio.controlcenter.databinding.LayoutItemIconShapeBinding
import com.ezstudio.controlcenter.model.ItemIconShape

class AdapterIconShape(var listIconShape: MutableList<ItemIconShape>) :
    RecyclerView.Adapter<AdapterIconShape.ViewHolder>() {

    class ViewHolder(var binding: LayoutItemIconShapeBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val ICON_SHAPE = "ICON_SHAPE"
    private val IS_DRAW_SHAPE = "IS_DRAW_SHAPE"
    var listenerClick: ((Int) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutItemIconShapeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listIconShape[position]
        holder.binding.txtNameIcon.text = data.name
        holder.binding.imgIcon.setImageResource(data.res)
        holder.binding.radioIconShape.isChecked = data.isSelected
        holder.binding.radioIconShape.isChecked = data.isSelected
        holder.itemView.setOnClickListener {
            listenerClick?.invoke(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return listIconShape.size
    }
}