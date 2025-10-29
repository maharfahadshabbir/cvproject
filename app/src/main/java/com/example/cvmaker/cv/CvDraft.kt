package com.example.cvmaker.cv

import android.os.Parcel
import android.os.Parcelable

class CvDraft(
    val imgUrl: String,
    val favourite: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(imgUrl)
        parcel.writeByte(if (favourite) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CvType> {
        override fun createFromParcel(parcel: Parcel): CvType {
            return CvType(parcel)
        }

        override fun newArray(size: Int): Array<CvType?> {
            return arrayOfNulls(size)
        }
    }
}