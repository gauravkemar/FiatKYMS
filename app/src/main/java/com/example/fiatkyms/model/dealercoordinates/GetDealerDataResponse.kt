package com.example.fiatkyms.model.dealercoordinates

data class GetDealerDataResponse(
    val address1: String,
    val id: Int,
    val city: String,
    val coordinates: String,
    val country: String,
    val createdBy: String,
    val createdDate: String,
    val dealerCode: String,
    val dealerId: Int,
    val dealerName: String,
    val emailID: String,
    val mobileNumber: String,
    val modifiedBy: Any,
    val modifiedDate: String,
    val postalCode: String,
    val state: String,
    val status: Any
)