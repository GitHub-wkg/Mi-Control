package com.ezstudio.controlcenter.model

import android.os.Parcel
import android.os.Parcelable

data class ItemExpandable(
    val nameHint: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nameHint)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ItemExpandable> {
        override fun createFromParcel(parcel: Parcel): ItemExpandable {
            return ItemExpandable(parcel)
        }

        override fun newArray(size: Int): Array<ItemExpandable?> {
            return arrayOfNulls(size)
        }
    }
}