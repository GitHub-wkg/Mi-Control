package com.ezstudio.controlcenter.di

import com.ezstudio.controlcenter.viewmodel.AreaModel
import com.ezstudio.controlcenter.viewmodel.NotificationViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {
    single { NotificationViewModel(androidApplication()) }
    single { AreaModel(androidApplication()) }
}