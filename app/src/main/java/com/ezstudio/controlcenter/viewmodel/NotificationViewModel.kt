package com.ezstudio.controlcenter.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ezstudio.controlcenter.model.ItemNotification
import com.ezteam.baseproject.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : BaseViewModel(application) {
    val listItemNotification: MutableLiveData<MutableIterable<ItemNotification>> = MutableLiveData()

    @SuppressLint("SimpleDateFormat")
    fun getNotification(activeNotifications: Array<StatusBarNotification>, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = mutableListOf<ItemNotification>()
                for (sbn in activeNotifications) {
                    val itemDataNotification = ItemNotification(
                        sbn.notification.extras.getInt(Notification.EXTRA_SMALL_ICON),
                        sbn.notification.extras.getString(Notification.EXTRA_TITLE),
                        sbn.notification.extras.getString(Notification.EXTRA_TEXT),
                        sbn.postTime,
                        sbn.packageName,
                        sbn.key,
                        sbn.id,
                        sbn.notification.contentIntent,
                        sbn.isClearable
                    )
                    //
                    try {
                        context.packageManager.getApplicationLabel(
                            context.packageManager.getApplicationInfo(
                                sbn.packageName!!,
                                PackageManager.GET_META_DATA
                            )
                        )
                        // check cùng key
                        for (item in list) {
                            if (item.key == itemDataNotification.key) {
                                if (item.time != itemDataNotification.time) {
                                    item.message =
                                        "${item.message}\n${itemDataNotification.message}"
                                }
                                break
                            }
                        }
                        //check cùng pakge
                        if (list.size > 0) {
                            for (i in (list.size - 1) downTo 0) {
                                val item = list[i]
                                if (item.packageName == itemDataNotification.packageName) {
                                    itemDataNotification.isParent = false
                                    list.add(i + 1, itemDataNotification)
                                    break
                                }
                            }
                        }
                        //check đã khác pakge
                        if (itemDataNotification.isParent) {
                            list.add(itemDataNotification)
                        }
                        //
                    } catch (e: PackageManager.NameNotFoundException) {
                        Log.d("Huy", "getNotification : ")
                    }
                }
                listItemNotification.postValue(list)
            } catch (e: Exception) {
                //
            }
        }
    }
}