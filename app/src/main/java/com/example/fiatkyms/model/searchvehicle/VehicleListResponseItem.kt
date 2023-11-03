package com.example.fiatkyms.model.searchvehicle

import android.os.Parcel
import android.os.Parcelable

data class VehicleListResponseItem(
    val createdDate: String?,
    val status: String?,
    val vehicleId: Int?,
    val vehicleResponse: VehicleResponse?,
    val coordinates: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readParcelable(VehicleResponse::class.java.classLoader),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(createdDate)
        parcel.writeString(status)
        vehicleId?.let { parcel.writeInt(it) }
        parcel.writeParcelable(vehicleResponse, flags)
        parcel.writeString(coordinates)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VehicleListResponseItem> {
        override fun createFromParcel(parcel: Parcel): VehicleListResponseItem {
            return VehicleListResponseItem(parcel)
        }

        override fun newArray(size: Int): Array<VehicleListResponseItem?> {
            return arrayOfNulls(size)
        }
    }
}