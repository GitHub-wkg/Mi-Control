package com.ezstudio.controlcenter.viewholder

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.Toast
import com.ezstudio.controlcenter.R
import com.ezstudio.controlcenter.databinding.LayoutExpandableRecycleViewBinding
import com.ezstudio.controlcenter.model.ItemExpandable
import com.ezstudio.controlcenter.model.ItemSystemShade
import com.ezteam.baseproject.utils.PreferencesUtils
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder

class ExpandableViewHolder(var binding: LayoutExpandableRecycleViewBinding) :
    ChildViewHolder(binding.root) {
    private val KEY_NAME_SHAPE = "KEY_NAME_SHAPE"
    fun bind(itemExpandable: ItemExpandable?, group: ItemSystemShade) {

        binding.edtName.hint = itemExpandable?.nameHint
        itemExpandable?.let {
            if (it.nameHint.equals(binding.root.context.resources.getString(R.string.hint_location_edt))) {
                binding.edtName.inputType = InputType.TYPE_CLASS_NUMBER
                binding.edtName.setText(PreferencesUtils.getString("${group.name} - Location", ""))
            } else {
                binding.edtName.inputType = InputType.TYPE_CLASS_TEXT
                binding.edtName.setText(PreferencesUtils.getString("${group.name} - Name", ""))
            }
            binding.edtName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (binding.edtName.isFocused && PreferencesUtils.getString(KEY_NAME_SHAPE)
                            .equals(group.name)
                    ) {
                        if (it.nameHint.equals(binding.root.context.resources.getString(R.string.hint_location_edt))) {
                            if (s.toString().matches(Regex("^\\d+\$"))) {
                                PreferencesUtils.putString(
                                    "${PreferencesUtils.getString(KEY_NAME_SHAPE)} - Location",
                                    s.toString()
                                )
                            } else {
                                Toast.makeText(
                                    binding.root.context,
                                    "Invalid location.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else if (it.nameHint.equals(binding.root.context.resources.getString(R.string.hint_name_edt))) {

                            if (s.toString().matches(Regex("[a-zA-Z ]+"))) {
                                PreferencesUtils.putString(
                                    "${PreferencesUtils.getString(KEY_NAME_SHAPE)} - Name",
                                    s.toString()
                                )
                            } else {
                                Toast.makeText(
                                    binding.root.context,
                                    "Invalid name.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            })
            binding.edtName.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    PreferencesUtils.putString(KEY_NAME_SHAPE, group.name)
                }
            }
        }

        //
    }

}