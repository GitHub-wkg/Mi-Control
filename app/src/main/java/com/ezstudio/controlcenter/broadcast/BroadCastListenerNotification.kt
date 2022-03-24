package com.ezstudio.controlcenter.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ezstudio.controlcenter.adapter.AdapterNotifications
import com.ezstudio.controlcenter.common.KeyBroadCast
import com.ezstudio.controlcenter.model.ItemNotification


class BroadCastListenerNotification : BroadcastReceiver() {
    lateinit var listDataNotification: MutableList<ItemNotification>
    lateinit var adapter: AdapterNotifications
    var listener: (() -> Unit)? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            context?.let { c ->
                if (it.action == KeyBroadCast.KEY_PULL_NOTIFICATION) {
                    it.setExtrasClassLoader(ItemNotification::class.java.classLoader)
                    val data = it.getParcelableExtra<ItemNotification>("ITEM")
                    if (data != null) {
                        var check = false
                        if (data.time != -1L) {
                            for (item in listDataNotification) {
                                if (item.key == data.key) {
                                    if (item.time != data.time && data.message != null) {
                                        item.message = "${item.message}\n${data.message}"
                                        adapter.notifyItemChanged(
                                            listDataNotification.indexOf(
                                                item
                                            )
                                        )
                                    }
                                    check = true
                                    break
                                }
                            }
                            //
                            if (!check) {
                                if (listDataNotification.size > 0) {
                                    for (i in (listDataNotification.size - 1) downTo 0) {
                                        val item = listDataNotification[i]
                                        if (item.packageName == data.packageName) {
                                            data.isParent = false
                                            listDataNotification.add(i + 1, data)
                                            adapter.notifyItemInserted(i + 1)
                                            break
                                        }
                                    }
                                }
                                //
                                if (data.isParent) {
                                    listDataNotification.add(0, data)
                                    adapter.notifyItemInserted(0)
                                }
                            }
                        } else {
                            for (item in listDataNotification) {
                                if (item.key == data.key) {
                                    adapter.removeSingleItem(item)
                                    break
                                }
                            }
                        }
                    }
                    listener?.invoke()
                }
            }
        }
    }
}