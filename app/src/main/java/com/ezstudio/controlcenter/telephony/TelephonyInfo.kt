package com.ezstudio.controlcenter.telephony

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.TelephonyManager
import java.lang.reflect.Method

class TelephonyInfo private constructor() {


    /*public static void setImsiSIM1(String imeiSIM1) {
           TelephonyInfo.imeiSIM1 = imeiSIM1;
       }*/

    /*public static void setImsiSIM2(String imeiSIM2) {
           TelephonyInfo.imeiSIM2 = imeiSIM2;
       }*/
    var isSIM1Ready = false
        private set

    /*public static void setSIM1Ready(boolean isSIM1Ready) {
           TelephonyInfo.isSIM1Ready = isSIM1Ready;
       }*/
    var isSIM2Ready = false
        private set

    /*public static void setSIM2Ready(boolean isSIM2Ready) {
        TelephonyInfo.isSIM2Ready = isSIM2Ready;
    }*/

    private class GeminiMethodNotFoundException(info: String?) : Exception(info) {
        companion object {
            private const val serialVersionUID = -996812356902545308L
        }
    }

    companion object {
        private var telephonyInfo: TelephonyInfo? = null
        @SuppressLint("HardwareIds")
        fun getInstance(context: Context): TelephonyInfo? {
            if (telephonyInfo == null) {
                telephonyInfo = TelephonyInfo()
                val telephonyManager: TelephonyManager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                telephonyInfo!!.isSIM1Ready =
                    telephonyManager.simState === TelephonyManager.SIM_STATE_READY
                telephonyInfo!!.isSIM2Ready = false
                try {
                    telephonyInfo!!.isSIM1Ready = getSIMStateBySlot(context, "getSimStateGemini", 0)
                    telephonyInfo!!.isSIM2Ready = getSIMStateBySlot(context, "getSimStateGemini", 1)
                } catch (e: GeminiMethodNotFoundException) {
                    e.printStackTrace()
                    try {
                        telephonyInfo!!.isSIM1Ready = getSIMStateBySlot(context, "getSimState", 0)
                        telephonyInfo!!.isSIM2Ready = getSIMStateBySlot(context, "getSimState", 1)
                    } catch (e1: GeminiMethodNotFoundException) {
                        //Call here for next manufacturer's predicted method name if you wish
                        e1.printStackTrace()
                    }
                }
            }
            return telephonyInfo
        }
        @Throws(GeminiMethodNotFoundException::class)
        private fun getSIMStateBySlot(
            context: Context,
            predictedMethodName: String,
            slotID: Int
        ): Boolean {
            var isReady = false
            val telephony: TelephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            try {
                val telephonyClass = Class.forName(telephony.javaClass.name)
                val parameter = arrayOfNulls<Class<*>?>(1)
                parameter[0] = Int::class.javaPrimitiveType
                val getSimStateGemini: Method =
                    telephonyClass.getMethod(predictedMethodName, *parameter)
                val obParameter = arrayOfNulls<Any>(1)
                obParameter[0] = slotID
                val ob_phone: Any? = getSimStateGemini.invoke(telephony, obParameter)
                if (ob_phone != null) {
                    val simState = ob_phone.toString().toInt()
                    if (simState == TelephonyManager.SIM_STATE_READY) {
                        isReady = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                throw GeminiMethodNotFoundException(predictedMethodName)
            }
            return isReady
        }
    }
}