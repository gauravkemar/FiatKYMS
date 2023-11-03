package com.example.fiatkyms.helper

import android.location.Location
import com.google.android.gms.maps.model.LatLng

fun Location.toLatLong(): LatLng {
    return LatLng(this.latitude, this.longitude)
}