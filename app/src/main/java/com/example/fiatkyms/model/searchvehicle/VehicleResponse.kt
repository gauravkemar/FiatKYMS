package com.example.fiatkyms.model.searchvehicle

import android.os.Parcel
import android.os.Parcelable

data class VehicleResponse(
    val color: String?,
    val engineNo: String?,
    val modelCode: String?,
    val vehicleId: Int?,
    val vehicleMilestones: Any ? = null,
    val vehicleRFIDMappings: Any ? = null,
    val vin: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        null,
        null,
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(color)
        parcel.writeString(engineNo)
        parcel.writeString(modelCode)
        vehicleId?.let { parcel.writeInt(it) }
        parcel.writeString(vin)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VehicleResponse> {
        override fun createFromParcel(parcel: Parcel): VehicleResponse {
            return VehicleResponse(parcel)
        }

        override fun newArray(size: Int): Array<VehicleResponse?> {
            return arrayOfNulls(size)
        }
    }
}