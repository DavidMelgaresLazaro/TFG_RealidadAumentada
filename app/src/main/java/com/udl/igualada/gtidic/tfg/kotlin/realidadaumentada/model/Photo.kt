package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model

import android.os.Parcel
import android.os.Parcelable

data class Photo(
    val filename: String = "",
    val url: String = "",
    val time: String = "",
    val modelName: String? = null,
    val size: Map<String, Float>? = null,
    val position: Map<String, Float>? = null,
    val distance: Float? = null,
    val comment: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        mutableMapOf<String, Float>().apply {
            parcel.readMap(this, Float::class.java.classLoader)
        },
        mutableMapOf<String, Float>().apply {
            parcel.readMap(this, Float::class.java.classLoader)
        },
        parcel.readValue(Float::class.java.classLoader) as? Float,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(filename)
        parcel.writeString(url)
        parcel.writeString(time)
        parcel.writeString(modelName)
        parcel.writeMap(size)
        parcel.writeMap(position)
        parcel.writeValue(distance)
        parcel.writeString(comment)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Photo> {
        override fun createFromParcel(parcel: Parcel): Photo {
            return Photo(parcel)
        }

        override fun newArray(size: Int): Array<Photo?> {
            return arrayOfNulls(size)
        }
    }
}
