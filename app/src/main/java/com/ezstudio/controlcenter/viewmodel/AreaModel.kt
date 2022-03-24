package com.ezstudio.controlcenter.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ezteam.baseproject.viewmodel.BaseViewModel

class AreaModel(application: Application) : BaseViewModel(application) {
    var areaLiveData: MutableLiveData<Int> = MutableLiveData()

    fun setArea(area: Int) {
        areaLiveData.postValue(area)
    }
}