package com.ezstudio.controlcenter

import com.ezstudio.controlcenter.di.appModule
import com.ezteam.baseproject.utils.PreferencesUtils
import com.google.android.gms.ads.ez.EzApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ControlApplication : EzApplication() {

    override fun onCreate() {
        super.onCreate()
        PreferencesUtils.init(this)
        setupKoin()

    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@ControlApplication)
            modules(
                appModule
            )
        }
    }
}