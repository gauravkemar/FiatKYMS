package com.example.fiatkyms.model.geofence

data class GetAllGeofenceResponseItem(
    val coordinates: String,
    val displayName: String,
    val inAntenna: Int,
    val inReaderIP: Any,
    val isActive: Boolean,
    val locationCategory: Any,
    val locationCategoryId: String,
    val locationCode: String,
    val locationId: Int,
    val locationName: String,
    val locationType: Any,
    val outAntenna: Int,
    val outReaderIP: Any,
    val parentLocationCode: Any
)