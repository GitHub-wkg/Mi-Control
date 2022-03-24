package com.ezstudio.controlcenter.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.adapter.AdapterIconShape
import com.ezstudio.controlcenter.databinding.LayoutIconShapeBinding
import com.ezstudio.controlcenter.model.ItemIconShape
import com.ezteam.baseproject.utils.PreferencesUtils

class DialogIconShape(context: Context) : AlertDialog(context) {
    private lateinit var listIconShape: MutableList<ItemIconShape>
    private lateinit var binding: LayoutIconShapeBinding
    private val ICON_SHAPE = "ICON_SHAPE"

    var listenerClickDone: ((Int) -> Unit)? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setCancelable(true)
        addDataListIconShape()
        binding = LayoutIconShapeBinding.inflate(LayoutInflater.from(context))
        binding.rclIconShape.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val adapter = AdapterIconShape(listIconShape)
        adapter.listenerClick = {
            for (item in listIconShape) {
                val check = item.name == listIconShape[it].name
                if (check != item.isSelected) {
                    item.isSelected = check
                    adapter.notifyItemChanged(listIconShape.indexOf(item))
                }
            }
        }
        binding.rclIconShape.adapter = adapter
        // clear anim
        clearAnimRecycleView(binding.rclIconShape)
        //
        for (item in listIconShape) {
            if (item.res == PreferencesUtils.getInteger(
                    ICON_SHAPE, R.drawable.ic_round
                )
            ) {
                val position = listIconShape.indexOf(item) - 2
                binding.rclIconShape.scrollToPosition(if (position < 0) 0 else position)
                break
            }
        }

        binding.btnOk.setOnClickListener {
            for (item in listIconShape) {
                if (item.isSelected) {
                    PreferencesUtils.putInteger(
                        ICON_SHAPE,
                        item.resDisplay ?: item.res
                    )
                    listenerClickDone?.invoke(item.resDisplay ?: item.res)
                    break
                }
            }
            this.dismiss()
        }

        binding.btnCancel.setOnClickListener {
            this.dismiss()
        }

        setContentView(binding.root)
    }

    private fun addDataListIconShape() {
        listIconShape = mutableListOf()
        listIconShape.apply {
            add(
                ItemIconShape(
                    context.resources.getString(R.string.round),
                    R.drawable.ic_round, R.drawable.ic_round,
                    checkSelectedIcon(R.drawable.ic_round)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.round_outline),
                    R.drawable.ic_round_outline, R.drawable.ic_dp_roundoutline,
                    checkSelectedIcon(R.drawable.ic_round_outline)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.dotted_round),
                    R.drawable.ic_dotted_round, R.drawable.ic_dp_dotted_round,
                    checkSelectedIcon(R.drawable.ic_dotted_round)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.round_outside),
                    R.drawable.ic_round_outside, R.drawable.ic_dp_round_outside,
                    checkSelectedIcon(R.drawable.ic_round_outside)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.droplets),
                    R.drawable.ic_droplets, null,
                    checkSelectedIcon(R.drawable.ic_droplets)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.square_corners),
                    R.drawable.ic_square_corners, null,
                    checkSelectedIcon(R.drawable.ic_square_corners)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.cornered_rectangle),
                    R.drawable.ic_cornered_rectangle, R.drawable.ic_dp_cornered_rectangle,
                    checkSelectedIcon(R.drawable.ic_cornered_rectangle)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.square),
                    R.drawable.ic_square, null,
                    checkSelectedIcon(R.drawable.ic_square)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.rectangle),
                    R.drawable.ic_rectangle, R.drawable.ic_dp_rectangle,
                    checkSelectedIcon(R.drawable.ic_rectangle)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.square_corner),
                    R.drawable.ic_square_corner, null,
                    checkSelectedIcon(R.drawable.ic_square_corner)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.hexagon),
                    R.drawable.ic_hexagon, null,
                    checkSelectedIcon(R.drawable.ic_hexagon)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.pentagon),
                    R.drawable.ic_pentagon, null,
                    checkSelectedIcon(R.drawable.ic_pentagon)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.flower),
                    R.drawable.ic_flower, null,
                    checkSelectedIcon(R.drawable.ic_flower)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.vase),
                    R.drawable.ic_vase,
                    null,
                    checkSelectedIcon(R.drawable.ic_vase)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.tapered_square),
                    R.drawable.ic_tapered_square, null,
                    checkSelectedIcon(R.drawable.ic_tapered_square)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.pebble),
                    R.drawable.ic_pebble, null,
                    checkSelectedIcon(R.drawable.ic_pebble)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.diamond),
                    R.drawable.ic_diamond, null,
                    checkSelectedIcon(R.drawable.ic_diamond)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.heart),
                    R.drawable.ic_heart,
                    null,
                    checkSelectedIcon(R.drawable.ic_heart)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.dog_leg),
                    R.drawable.ic_dog_leg, null,
                    checkSelectedIcon(R.drawable.ic_dog_leg)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.bell),
                    R.drawable.ic_bell,
                    null,
                    checkSelectedIcon(R.drawable.ic_bell)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.sun),
                    R.drawable.ic_sun,
                    null,
                    checkSelectedIcon(R.drawable.ic_sun)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.star),
                    R.drawable.ic_star,
                    null,
                    checkSelectedIcon(R.drawable.ic_star)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.file),
                    R.drawable.ic_file,
                    null,
                    checkSelectedIcon(R.drawable.ic_file)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.folder),
                    R.drawable.ic_folder, null,
                    checkSelectedIcon(R.drawable.ic_folder)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.suitcase),
                    R.drawable.ic_suitcase, null,
                    checkSelectedIcon(R.drawable.ic_suitcase)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.stickers),
                    R.drawable.ic_stickers, null,
                    checkSelectedIcon(R.drawable.ic_stickers)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.light_bulb),
                    R.drawable.ic_light_bulb, null,
                    checkSelectedIcon(R.drawable.ic_light_bulb)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.medal),
                    R.drawable.ic_medal,
                    null,
                    checkSelectedIcon(R.drawable.ic_medal)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.icon),
                    R.drawable.ic_icon,
                    null,
                    checkSelectedIcon(R.drawable.ic_icon)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.shield),
                    R.drawable.ic_shield, null,
                    checkSelectedIcon(R.drawable.ic_shield)
                )
            )
            add(
                ItemIconShape(
                    context.resources.getString(R.string.droid),
                    R.drawable.ic_droid,
                    null,
                    checkSelectedIcon(R.drawable.ic_droid)
                )
            )
        }
    }

    private fun checkSelectedIcon(icon: Int): Boolean {
        return icon == PreferencesUtils.getInteger(
            ICON_SHAPE,
            R.drawable.ic_round
        )
    }

    private fun clearAnimRecycleView(rcl: RecyclerView) {
        val animator: RecyclerView.ItemAnimator? = rcl.itemAnimator
        animator?.let {
            if (it is SimpleItemAnimator) {
                (it).supportsChangeAnimations = false
            }
        }
    }
}