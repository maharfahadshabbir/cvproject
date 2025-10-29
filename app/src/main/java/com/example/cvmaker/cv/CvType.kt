package com.example.cvmaker.cv

import android.os.Parcel
import android.os.Parcelable


class CvType(
    val id:Int = 0,
    var generationId:Int = 0,
    var profileId:Int = 0,
    val imgUrl: String,
    val pdfUrl: String,
    val template_type_name: String,
    var favourite: Boolean,
    var isChecked: Boolean = false,

    ): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(generationId)
        parcel.writeInt(profileId)
        parcel.writeString(imgUrl)
        parcel.writeString(pdfUrl)
        parcel.writeString(template_type_name)
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