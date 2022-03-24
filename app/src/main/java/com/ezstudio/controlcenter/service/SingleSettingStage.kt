package com.ezstudio.controlcenter.service

class SingleSettingStage {
    var isEnableBlueFilter = false
    var isEnableFightMode = false
    var isEnableAutoRotate = false
    var isEnableAirPlane = false
    var isEnableDarkTheme = false
    var isEnableHotspot = false
    var isWifi = false
    var isMobileData = false
    var isEnableScreenTransmission = false
    var isEnableNFC = false
    var isEnableLocation = false
    var isEnableBatterySaver = false
    var isEnableDataSaver = false

    private object Holder {
        val INSTANCE = SingleSettingStage()
    }

    fun cleanAll() {
        isEnableBlueFilter = false
        isEnableFightMode = false
        isEnableAutoRotate = false
        isEnableDarkTheme = false
        isEnableHotspot = false
        isEnableScreenTransmission = false
        isEnableNFC = false
        isEnableLocation = false
        isEnableBatterySaver = false
        isEnableDataSaver = false
        isEnableAirPlane = false
        isWifi = false
        isMobileData = false
    }

    companion object {
        @JvmStatic
        fun getInstance(): SingleSettingStage {
            return Holder.INSTANCE
        }
    }
}