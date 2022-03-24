package com.ezstudio.controlcenter.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.adapter.AdapterNotifications
import com.ezstudio.controlcenter.databinding.LayoutContentNotificationBinding
import com.ezstudio.controlcenter.model.ItemNotification
import com.ezstudio.controlcenter.touchswipe.SimpleItemTouchHelperCallback
import com.ezstudio.controlcenter.viewmodel.NotificationViewModel
import com.ezstudio.controlcenter.windown_manager.MyWindowManager
import com.google.android.gms.ads.ez.nativead.AdmobNativeAdView
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class ViewContentNotification(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs), KoinComponent {
    lateinit var binding: LayoutContentNotificationBinding
    lateinit var adapterNotification: AdapterNotifications
    var myWindowManager: MyWindowManager? = null
    var listNotification = mutableListOf<ItemNotification>()
    private val viewModel: NotificationViewModel by inject()

    init {
        initView()
        initData()
        initListener()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initData() {
        viewModel.listItemNotification.observeForever { list ->
            var ads: ItemNotification? = null
            listNotification.forEach {
                if (it.isAds) {
                    ads = it
                }
            }
            listNotification.clear()
            listNotification.addAll(list)
            ads?.let { i ->
                if (listNotification.size > 0) {
                    listNotification.add(1, i)
                } else {
                    listNotification.add(i)
                }
            }
            adapterNotification.notifyDataSetChanged()
            if (listNotification.size > 0) {
                binding.rclNoti.scrollToPosition(0)
            } else {
                checkEmptyNotification(listNotification)
            }
        }
    }

    private fun initListener() {
    }

    private fun initView() {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_content_notification, this, true)
        binding = LayoutContentNotificationBinding.bind(view)
        adapterNotification = AdapterNotifications(context, listNotification)
        adapterNotification.listenerMore = {
            listNotification[it].isMore = !listNotification[it].isMore
            adapterNotification.notifyItemChanged(it)
        }
        adapterNotification.listener = {
            checkEmptyNotification(listNotification)
        }
        adapterNotification.listenerEndWindow = {
            myWindowManager?.endWindownNoti()
        }
        adapterNotification.listenerRemoveItem = {
            val position = listNotification.indexOf(it)
            //
            if (position != -1) {
                if (listNotification[position].isParent && position + 1 < listNotification.size &&
                    listNotification[position].packageName == listNotification[position + 1].packageName
                ) {
                    listNotification[position + 1].isParent = true
                    adapterNotification.notifyItemChanged(position + 1)
                }
                listNotification.removeAt(position)
                adapterNotification.notifyItemRemoved(position)
                checkEmptyNotification(listNotification)
            }
        }
        binding.rclNoti.adapter = adapterNotification
        // unableAminRecycleView
        unableAminRecycleView(binding.rclNoti)//
        addItemTouchCallback(binding.rclNoti)
        // load ads
        loadAds()
    }

    private fun loadAds() {
        AdmobNativeAdView.getNativeAd(
            context,
            R.layout.native_admob_item_notification,
            object : AdmobNativeAdView.NativeAdListener {
                override fun onError() {
                }

                override fun onLoaded(nativeAd: AdmobNativeAdView?) {
                    adapterNotification.adsView = nativeAd
                    adapterNotification.addAds(ItemNotification(isAds = true), 1)
                }

                override fun onClickAd() {
                    myWindowManager?.endWindownNoti()
                }
            })
    }

    private fun unableAminRecycleView(rel: RecyclerView) {
        val animator: RecyclerView.ItemAnimator? = rel.itemAnimator
        animator?.let {
            if (it is SimpleItemAnimator) {
                (it).supportsChangeAnimations = false
            }
        }//
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addItemTouchCallback(recyclerView: RecyclerView) {
        val callBack = SimpleItemTouchHelperCallback()
        callBack.listenerSwipe = { position, direction ->
            adapterNotification.swipe(position, direction)
        }
        val itemTouchHelper = ItemTouchHelper(callBack)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    private fun checkEmptyNotification(listNotification: MutableList<ItemNotification>) {
        if (listNotification.size == 0) {
            binding.txtNoNotifications.visibility = View.VISIBLE
        } else {
            binding.txtNoNotifications.visibility = View.GONE
        }
    }
}