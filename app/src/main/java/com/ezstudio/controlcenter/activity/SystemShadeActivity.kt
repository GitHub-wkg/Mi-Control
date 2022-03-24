package com.ezstudio.controlcenter.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.adapter.ExpandableViewAdapter
import com.ezstudio.controlcenter.databinding.LayoutSystemShadeBinding
import com.ezstudio.controlcenter.model.ItemExpandable
import com.ezstudio.controlcenter.model.ItemSystemShade
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class SystemShadeActivity : AppCompatActivity() {
    private lateinit var binding: LayoutSystemShadeBinding
    private var listSystemShade = mutableListOf<ItemSystemShade>()
    private var expandedGroup: ExpandableGroup<*>? = null
    private var collapseGroup: ExpandableGroup<*>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_in_bottom)
        super.onCreate(savedInstanceState)
        binding = LayoutSystemShadeBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        val listExpand = mutableListOf<ItemExpandable>()
        listExpand.add(ItemExpandable(resources.getString(R.string.hint_name_edt)))
        listExpand.add(ItemExpandable(resources.getString(R.string.hint_location_edt)))
        listSystemShade.apply {
            add(
                ItemSystemShade(
                    R.drawable.ic_location,
                    resources.getString(R.string.location),
                    false,
                    listExpand
                )
            )
            add(
                ItemSystemShade(
                    R.drawable.ic_airplane,
                    resources.getString(R.string.airplane_mode), false, listExpand
                )
            )
            add(
                ItemSystemShade(
                    R.drawable.ic_rotation_lock,
                    resources.getString(R.string.auto_rotate), false, listExpand
                )
            )
            add(
                ItemSystemShade(
                    R.drawable.ic_battery_saver,
                    resources.getString(R.string.battery_saver), false, listExpand
                )
            )
            add(
                ItemSystemShade(
                    R.drawable.ic_hotspot,
                    resources.getString(R.string.hotspot),
                    false,
                    listExpand
                )
            )
            add(
                ItemSystemShade(
                    R.drawable.ic_nfc,
                    resources.getString(R.string.nfc),
                    false,
                    listExpand
                )
            )
            add(
                ItemSystemShade(
                    R.drawable.ic_data_saver_on,
                    resources.getString(R.string.data_saver), false, listExpand
                )
            )
            add(
                ItemSystemShade(
                    R.drawable.ic_dark_theme,
                    resources.getString(R.string.dark_theme),
                    false,
                    listExpand
                )
            )
            add(
                ItemSystemShade(
                    R.drawable.ic_night_light,
                    resources.getString(R.string.night_light), false, listExpand
                )
            )
            add(
                ItemSystemShade(
                    R.drawable.ic_screen_transmission,
                    resources.getString(R.string.screen_transmission), false, listExpand
                )
            )
        }
        val adapter = ExpandableViewAdapter(listSystemShade)
//        adapter.setOnGroupExpandCollapseListener(object : GroupExpandCollapseListener {
//            override fun onGroupExpanded(group: ExpandableGroup<*>?) {
//                if (expandedGroup != null) {
//                    adapter.toggleGroup(expandedGroup)
//                }
//                expandedGroup = group
//                (group as ItemSystemShade).isExpandable = true
//            }
//
//            override fun onGroupCollapsed(group: ExpandableGroup<*>?) {
//                collapseGroup = group
//                if (collapseGroup?.equals(expandedGroup) == true) {
//                    expandedGroup = null
//                }
//                (group as ItemSystemShade).isExpandable = false
//            }
//        })
//        adapter.setOnGroupExpandCollapseListener(object : GroupExpandCollapseListener {
//            override fun onGroupExpanded(group: ExpandableGroup<*>?) {
//                (group as ItemSystemShade).isExpandable = true
//            }
//
//            override fun onGroupCollapsed(group: ExpandableGroup<*>?) {
//                (group as ItemSystemShade).isExpandable = false
//            }
//        })

        binding.rclSysShade.adapter = adapter
        binding.rclSysShade.layoutManager =
            LinearLayoutManager(this)
        //
        binding.openSysShade.setOnClickListener {
            expandSettingsPanel(this)
        }
    }

    @SuppressLint("WrongConstant")
    private fun expandSettingsPanel(context: Context) {
        try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManager: Class<*> = Class.forName("android.app.StatusBarManager")
            val show: Method = statusBarManager.getMethod("expandSettingsPanel")
            show.invoke(statusBarService)
        } catch (_e: ClassNotFoundException) {
            _e.printStackTrace()
        } catch (_e: NoSuchMethodException) {
            _e.printStackTrace()
        } catch (_e: IllegalArgumentException) {
            _e.printStackTrace()
        } catch (_e: IllegalAccessException) {
            _e.printStackTrace()
        } catch (_e: InvocationTargetException) {
            _e.printStackTrace()
        }
    }
}