package com.ezstudio.controlcenter.adapter

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.common.KeyBroadCast
import com.ezstudio.controlcenter.common.KeyIntent
import com.ezstudio.controlcenter.databinding.LayoutItemNotificationBinding
import com.ezstudio.controlcenter.model.ItemNotification
import com.ezteam.baseproject.utils.PreferencesUtils
import java.text.SimpleDateFormat
import java.util.*

class AdapterNotifications(
    val context: Context, var list: MutableList<ItemNotification>,
    var listener: (() -> Unit)? = null,
    var listenerEndWindow: (() -> Unit)? = null,
    var listenerRemoveItem: ((ItemNotification) -> Unit)? = null,
    var listenerMore: ((Int) -> Unit)? = null
) :
    RecyclerView.Adapter<AdapterNotifications.ViewHolder>() {
    var adsView: RelativeLayout? = null
    private val BACKGROUND_COLOR = "BACKGROUND_COLOR"
    private val animClickIcon = AnimationUtils.loadAnimation(context, R.anim.anim_click_icon)

    class ViewHolder(val binding: LayoutItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutItemNotificationBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.binding.contentView.isVisible = !data.isAds
        holder.binding.adsView.isVisible = data.isAds
        //load ads
        if (data.isAds) {
            adsView?.let {
                if (it.parent != null) {
                    (it.parent as ViewGroup).removeView(it)
                }
                holder.binding.adsView.addView(it)
            }
            return
        }
        //set color
        setUpColorView(holder.binding)
        //
        if (!data.isParent) {
            holder.binding.layoutAppMarket.visibility = View.GONE
        } else {
            holder.binding.layoutAppMarket.visibility = View.VISIBLE
            val anim = AnimationUtils.loadAnimation(context, R.anim.anim_hide_view)
            holder.binding.layoutAppMarket.startAnimation(anim)
        }
        // icon
        var iconApp: Drawable? = null
        try {
            iconApp = context.packageManager.getApplicationIcon(data.packageName ?: "")
        } catch (e: PackageManager.NameNotFoundException) {
        }
        var icon: Drawable? = null
        icon = if (data.resId != 0) {
            try {
                val packageContext = context.createPackageContext(data.packageName, 0)
                packageContext.resources.getDrawable(data.resId)
            } catch (e: Resources.NotFoundException) {
                iconApp
            } catch (e: PackageManager.NameNotFoundException) {
                iconApp
            }

        } else {
            iconApp
        }
        //
        val packageManager = context.packageManager
        val app: ApplicationInfo? = try {
            packageManager.getApplicationInfo(
                data.packageName!!,
                PackageManager.GET_META_DATA
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        val name = app?.let { packageManager.getApplicationLabel(it) }

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = data.time
        //
        holder.binding.apply {
            imgAppMarket.setImageDrawable(iconApp)
            icNotification.setImageDrawable(icon)
            txtTitle.text = data.title ?: name
            time.text = SimpleDateFormat("hh:mm a").format(calendar.time)
            appName.text = name
        }
        holder.binding.txtMessage.text = if (data.message == null) "" else data.message
        if (holder.binding.txtMessage.lineCount > 3) {
            if (!data.isMore) {
                holder.binding.more.text = context.getString(R.string.see_more)
                holder.binding.more.visibility = View.VISIBLE
                holder.binding.txtMessage.maxLines = 3
                holder.binding.txtMessage.invalidate()
            } else {
                holder.binding.more.text = context.getString(R.string.collapse_)
                holder.binding.more.visibility = View.VISIBLE
                holder.binding.txtMessage.maxLines = Int.MAX_VALUE
                holder.binding.txtMessage.invalidate()
            }
        } else {
            holder.binding.more.visibility = View.GONE
            holder.binding.txtMessage.maxLines = Int.MAX_VALUE
        }
        //
        if (data.message == null) {
            holder.binding.txtMessage.visibility = View.GONE
        } else {
            holder.binding.txtMessage.visibility = View.VISIBLE
        }
        // action click
        holder.itemView.setOnClickListener {
            holder.binding.layoutContent.startAnimation(animClickIcon)
            Handler().postDelayed({
                try {
                    data.contentIntent?.send()
                } catch (e: PendingIntent.CanceledException) {
                }
                listenerEndWindow?.invoke()
            }, 100)
        }
        // more
        holder.binding.layoutMore.setOnClickListener {
            listenerMore?.invoke(holder.adapterPosition)
        }
    }

    private fun setUpColorView(binding: LayoutItemNotificationBinding) {
        val colorText = Color.parseColor(
            PreferencesUtils.getString(
                context.resources.getString(R.string.TEXT_COLOR), "#FFFFFF"
            )
        )
        binding.txtTitle.setTextColor(colorText)
        binding.txtMessage.setTextColor(colorText)
        binding.time.setTextColor(colorText)
        // background view
        val colorBackground = PreferencesUtils.getString(
            BACKGROUND_COLOR,
            context.resources.getString(R.string.color_4DFFFFFF)
        )
        setColorDrawable(binding.layoutContent, colorBackground)
    }

    private fun setColorDrawable(layout: ConstraintLayout, color: String) {
        val bgShape = layout.background as GradientDrawable
        bgShape.mutate()
        bgShape.setColor(Color.parseColor(color))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun removeSingleItem(item: ItemNotification) {
        listenerRemoveItem?.invoke(item)
    }

    //action close
    fun swipe(position: Int, direction: Int) {
        val data = list[position]
        if (data.isClearable) {
            // send broadCastRemove
            list.removeAt(position)
            notifyItemRemoved(position)
            sendClearByKey(data.key, data.notificationId, context)
            // cal back
            listener?.invoke()
        } else {
            notifyItemChanged(position)
        }
    }

    private fun sendClearByKey(key: String?, id: Int, context: Context) {
        if (key != null) {
            val intent =
                Intent(KeyBroadCast.ACTION_NOTIFICATION_CLEAR_FOR_KEY)
            intent.putExtra(
                KeyIntent.key,
                key

            )
            intent.putExtra(
                KeyIntent.ID,
                id
            )
            context.sendBroadcast(intent)
        }
    }

    fun addAds(model: ItemNotification, position: Int) {
        if (list.size < position) {
            list.add(0, model)
            notifyItemInserted(0)
        } else {
            list.add(position, model)
            notifyItemInserted(position)
        }
    }
}