package com.example.fiatkyms.model.prdoutmodel

data class GetAllPrdOutVinList(
    val cisnum: String,
    val createdBy: Any,
    val createdDate: String,
    val eX_COL_DESC: String,
    val ecolor: String,
    val isActive: Boolean,
    val modeL_NAME: String,
    val modifiedBy: Any,
    val modifiedDate: Any,
    val prodOut: String,
    val vehicleId: Int,
    val vehicleMilestones: List<Any>,
    val vin: String,
    val yardOut: Any,
    val zepointfldate: String,
    val zepointtime: String,
    val zmodel: String
)