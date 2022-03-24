package com.ezstudio.controlcenter.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutItemBackgroundBinding
import com.ezstudio.controlcenter.model.ItemBackground
import com.ezteam.baseproject.utils.PreferencesUtils

class AdapterBackground(var listBackground: MutableList<ItemBackground>) :
    RecyclerView.Adapter<AdapterBackground.ViewHolder>() {
    var listenerOnClick: (() -> Unit)? = null
    var listenerRequestPermission: (() -> Unit)? = null

    class ViewHolder(var binding: LayoutItemBackgroundBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val BACKGROUND = "BACKGROUND"
    private val BACKGROUND_CONTENT = "BACKGROUND_CONTENT"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutItemBackgroundBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listBackground[position]
        holder.binding.txtNameBackground.text = data.name
        holder.binding.txtContent.text = data.content
        holder.binding.imgIcon.setImageResource(data.resId)

        holder.binding.radioIconShape.isChecked =
            listBackground[position].resId == PreferencesUtils.getInteger(
                BACKGROUND,
                R.drawable.ic_background_image
            )
        holder.itemView.setOnClickListener {
            PreferencesUtils.putInteger(
                BACKGROUND, listBackground[holder.adapterPosition].resId
            )
            PreferencesUtils.putString(
                BACKGROUND_CONTENT, listBackground[holder.adapterPosition].content
            )
            listenerOnClick?.invoke()
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return listBackground.size
    }
}