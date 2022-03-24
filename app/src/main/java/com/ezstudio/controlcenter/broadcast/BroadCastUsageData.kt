package com.ezstudio.controlcenter.broadcast

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.TrafficStats
import android.telephony.TelephonyManager
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.WindownManagerBinding
import com.ezteam.baseproject.utils.PreferencesUtils

class BroadCastUsageData : BroadcastReceiver() {
    lateinit var binding: WindownManagerBinding
    private val TOTAL_DATA_ON = "TOTAL_DATA_ON"

    @SuppressLint("SetTextI18n")
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            if (context != null) {
                if (it.action == context.resources.getString(R.string.action_usage_data) || it.action == context.resources.getString(
                        R.string.action_usage_data_of_day
                    )
                ) {
                    setUpUsageDataLayout(context)
                }
            }
        }
    }

    private fun setColorDrawable(layout: ConstraintLayout, color: String) {
        val bgShape = layout.background as GradientDrawable
        bgShape.mutate()
        bgShape.setColor(Color.parseColor(color))
    }

    @SuppressLint("SetTextI18n")
    private fun setUpUsageDataLayout(context: Context) {
        if (PreferencesUtils.getBoolean(
                context.resources.getString(R.string.state_usage_data),
                false
            ) && !PreferencesUtils.getBoolean(
                context.resources.getString(R.string.stata_usage_day),
                false
            )
        ) {
            setUpDetailUsageDataLayout(
                context.resources.getString(R.string.this_month),
                TOTAL_DATA_ON
            )
        } else if (!PreferencesUtils.getBoolean(
                context.resources.getString(R.string.state_usage_data),
                false
            ) && PreferencesUtils.getBoolean(
                context.resources.getString(R.string.stata_usage_day),
                false
            )
        ) {
            setUpDetailUsageDataLayout(
                context.resources.getString(R.string.this_day),
                context.resources.getString(R.string.total_usage_data_of_day)
            )
        } else if (PreferencesUtils.getBoolean(
                context.resources.getString(R.string.state_usage_data),
                false
            ) && PreferencesUtils.getBoolean(
                context.resources.getString(R.string.stata_usage_day),
                false
            )
        ) {
            binding.layoutBtnFist.binding.layoutContentDataMobile.visibility = View.GONE
            binding.layoutBtnFist.binding.layoutContentUsageData.visibility = View.VISIBLE
            binding.layoutBtnFist.binding.icDataMobile.setImageResource(R.drawable.ic_usage_data)
            binding.layoutBtnFist.binding.icDataMobile.setColorFilter(Color.parseColor("#FF2F80ED"))
            //
            if (totalData() - PreferencesUtils.getLong(
                    TOTAL_DATA_ON,
                    0
                ) >= PreferencesUtils.getInteger(
                    binding.layoutBtnFist.context.resources.getString(R.string.warning_gb),0
                )
                && PreferencesUtils.getInteger(
                    binding.layoutBtnFist.context.resources.getString(R.string.warning_gb),0
                ) != 0
                //
                || totalData() - PreferencesUtils.getLong(
                    context.resources.getString(R.string.total_usage_data_of_day),
                    0
                ) >= PreferencesUtils.getInteger(
                    binding.layoutBtnFist.context.resources.getString(R.string.warning_gb),0
                )
                && PreferencesUtils.getInteger(
                    binding.layoutBtnFist.context.resources.getString(R.string.warning_gb),0
                ) != 0
            //
            ) {
                setColorDrawable(binding.layoutBtnFist.binding.btnMobileData, "#EB5757")
            }
            //
            when (totalData() - PreferencesUtils.getLong(
                TOTAL_DATA_ON,
                0
            )) {
                in 0..999 -> {
                    binding.layoutBtnFist.binding.txtMonth.text =
                        "${context.resources.getString(R.string.this_month)}\n${
                            totalData() - PreferencesUtils.getLong(
                                TOTAL_DATA_ON,
                                0
                            )
                        }  kb"
                }
                in 1000..999999 -> {
                    binding.layoutBtnFist.binding.txtMonth.text =
                        "${context.resources.getString(R.string.this_month)}\n${
                            (totalData() - PreferencesUtils.getLong(
                                TOTAL_DATA_ON,
                                0
                            )) / 1000F
                        }  Mb"
                }
                in 1000000..9000000000 -> {
                    binding.layoutBtnFist.binding.txtMonth.text =
                        "${context.resources.getString(R.string.this_month)}\n${
                            (totalData() - PreferencesUtils.getLong(
                                TOTAL_DATA_ON,
                                0
                            )) / 1000000F
                        }  Gb"
                }
            }
            //
            when (totalData() - PreferencesUtils.getLong(
                context.resources.getString(R.string.total_usage_data_of_day),
                0
            )) {
                in 0..999 -> {
                    binding.layoutBtnFist.binding.txtDay.text =
                        "${context.resources.getString(R.string.this_day)}\n${
                            totalData() - PreferencesUtils.getLong(
                                context.resources.getString(R.string.total_usage_data_of_day),
                                0
                            )
                        }  kb"
                }
                in 1000..999999 -> {
                    binding.layoutBtnFist.binding.txtDay.text =
                        "${context.resources.getString(R.string.this_day)}\n${
                            (totalData() - PreferencesUtils.getLong(
                                context.resources.getString(R.string.total_usage_data_of_day),
                                0
                            )) / 1000F
                        }  Mb"
                }
                in 1000000..9000000000 -> {
                    binding.layoutBtnFist.binding.txtDay.text =
                        "${context.resources.getString(R.string.this_day)}\n${
                            (totalData() - PreferencesUtils.getLong(
                                context.resources.getString(R.string.total_usage_data_of_day),
                                0
                            )) / 1000000F
                        }  Gb"
                }
            }
            binding.layoutBtnFist.binding.btnMobileData.isClickable = false
            setColorDrawable(binding.layoutBtnFist.binding.btnMobileData, "#FFFFFF")
        } else {
            binding.layoutBtnFist.binding.icDataMobile.setImageResource(R.drawable.ic_cc_qs_data_mobile_on)
            binding.layoutBtnFist.binding.icDataMobile.setColorFilter(
                Color.parseColor(
                    PreferencesUtils.getString(
                        context.resources.getString(R.string.ICON_COLOR),
                        "#FFFFFF"
                    )
                )
            )
            binding.layoutBtnFist.binding.txtMobileData.text =
                context.resources.getString(R.string.mobile_data)
            binding.layoutBtnFist.binding.btnMobileData.isClickable = true
            binding.layoutBtnFist.setStageDataMobile()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpDetailUsageDataLayout(name: String, totalName: String) {
        binding.layoutBtnFist.binding.layoutContentDataMobile.visibility = View.VISIBLE
        binding.layoutBtnFist.binding.layoutContentUsageData.visibility = View.GONE
        binding.layoutBtnFist.binding.icDataMobile.setImageResource(R.drawable.ic_usage_data)
        binding.layoutBtnFist.binding.icDataMobile.setColorFilter(Color.parseColor("#FF2F80ED"))
        binding.layoutBtnFist.binding.txtMobileData.text = name
        setUpTotalData(totalName)
        binding.layoutBtnFist.binding.btnMobileData.isClickable = false
        setColorDrawable(binding.layoutBtnFist.binding.btnMobileData, "#FFFFFF")
    }

    private fun totalData(): Long {
        val received = TrafficStats.getTotalRxBytes() / (1024 * 1024)
        val send = TrafficStats.getTotalTxBytes() / (1024 * 1024)
        return received + send
    }

    @SuppressLint("SetTextI18n")
    private fun setUpTotalData(nameTotalData: String) {
        if (totalData() - PreferencesUtils.getLong(
                nameTotalData,
                0
            ) >= PreferencesUtils.getInteger(
                binding.layoutBtnFist.context.resources.getString(R.string.warning_gb),0
            )
            //
            && PreferencesUtils.getInteger(
                binding.layoutBtnFist.context.resources.getString(R.string.warning_gb),0
            ) != 0

        //
        ) {
            setColorDrawable(binding.layoutBtnFist.binding.btnMobileData, "#EB5757")
        }
        when (totalData() - PreferencesUtils.getLong(
            nameTotalData,
            0
        )) {
            in 0..999 -> {
                binding.layoutBtnFist.binding.txtStatusMobileData.text =
                    "${
                        totalData() - PreferencesUtils.getLong(
                            nameTotalData,
                            0
                        )
                    }  kb"
            }
            in 1000..999999 -> {
                binding.layoutBtnFist.binding.txtStatusMobileData.text =
                    "${
                        (totalData() - PreferencesUtils.getLong(
                            nameTotalData,
                            0
                        )) / 1000F
                    }  Mb"
            }
            in 1000000..9000000000 -> {
                binding.layoutBtnFist.binding.txtStatusMobileData.text =
                    "${
                        (totalData() - PreferencesUtils.getLong(
                            nameTotalData,
                            0
                        )) / 1000000F
                    }  Gb"
            }
        }
    }
}