package com.ezstudio.controlcenter.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.ActivitySplashBinding
import com.ezteam.baseproject.activity.BaseActivity
import com.google.android.gms.ads.ez.AdFactoryListener
import com.google.android.gms.ads.ez.LogUtils
import com.google.android.gms.ads.ez.admob.AdmobOpenAdUtils

class Splash : BaseActivity<ActivitySplashBinding>() {
    private val isFromPermission by lazy {
        intent?.getBooleanExtra("PERMISSION", false) ?: false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_in_bottom)
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        setAppActivityFullScreenOver(this)
        if (isFromPermission) {
            binding.root.postDelayed({
                openMain()
            }, 1000)
        } else {
            loadOpenAds()
        }
    }

    override fun initData() {
    }

    override fun initListener() {
    }

    private fun loadOpenAds() {
        AdmobOpenAdUtils.getInstance(this).setAdListener(object : AdFactoryListener() {
            override fun onError() {
                LogUtils.logString(Splash::class.java, "onError")
                openMain()
            }

            override fun onLoaded() {
                LogUtils.logString(Splash::class.java, "onLoaded")
                // show ads ngay khi loaded
                AdmobOpenAdUtils.getInstance(this@Splash).showAdIfAvailable()
            }

            override fun onDisplay() {
                super.onDisplay()
                LogUtils.logString(Splash::class.java, "onDisplay")
            }

            override fun onDisplayFaild() {
                super.onDisplayFaild()
                LogUtils.logString(Splash::class.java, "onDisplayFaild")
                openMain()
            }

            override fun onClosed() {
                super.onClosed()
                // tam thoi bo viec load lai ads thi dismis
                LogUtils.logString(Splash::class.java, "onClosed")
                openMain()
            }
        }).loadAd()
    }

    private fun openMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun viewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(LayoutInflater.from(this))
    }
}