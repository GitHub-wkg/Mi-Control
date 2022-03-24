package com.ezstudio.controlcenter.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.ItemWifiBinding
import com.ezstudio.controlcenter.model.ItemWifi

class AdapterWifi(var list: ArrayList<ItemWifi>) :
    RecyclerView.Adapter<AdapterWifi.ViewHolder>() {
    var listenerClickItem : (()->Unit)?  = null
    class ViewHolder(var binding: ItemWifiBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWifiBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        when (getWifiLevel(data.level)) {
            in 1 until 34 -> {
                holder.binding.icWifi.setImageResource(R.drawable.ic_wifi_dialog_min)
            }
            in 34 until 66 -> {
                holder.binding.icWifi.setImageResource(R.drawable.ic_wifi_dialog_normal)
            }
            in 66..100 -> {
                holder.binding.icWifi.setImageResource(R.drawable.ic_wifi_dialog_full)
            }
        }
        holder.binding.txtName.text = data.SSID
        if (data.isConnected) {
            holder.binding.layoutItemWifi.setBackgroundColor(Color.parseColor("#603271B5"))
            holder.binding.txtName.setTextColor(Color.parseColor("#2C61CC"))
            holder.binding.txtConnected.visibility = View.VISIBLE
            holder.binding.icWifi.setImageResource(R.drawable.ic_wifi_connected)
            holder.binding.icStatusWifi.setImageResource(R.drawable.ic_check)
        } else {
            holder.binding.layoutItemWifi.setBackgroundColor(Color.parseColor("#FFFFFF"))
            holder.binding.txtName.setTextColor(Color.parseColor("#000000"))
            holder.binding.txtConnected.visibility = View.GONE
            holder.binding.icStatusWifi.setImageResource(R.drawable.ic_lock)
        }
        holder.itemView.setOnClickListener{
            listenerClickItem?.invoke()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun getWifiLevel(linkSpeed: Int): Int {
        val MIN_RSSI = -100
        val MAX_RSSI = -55
        val numLevels = 101
        return when {
            linkSpeed <= MIN_RSSI -> {
                0
            }
            linkSpeed >= MAX_RSSI -> {
                numLevels - 1
            }
            else -> {
                val inputRange = (MAX_RSSI - MIN_RSSI).toFloat()
                val outputRange: Float = numLevels - 1F
                return ((linkSpeed - MIN_RSSI).toFloat() * outputRange / inputRange).toInt()
            }
        }
    }
}