package com.example.fiatkyms.model.dealercoordinates

data class AddDealerCoordinatesRequestResponse(
  /*  val Address1: String,
    val City: String,

    val Country: String,



    val EmailID: String,
    val MobileNumber: String,
    val PostalCode: String,
    val State: String,
    val Status: String
*/
    val DealerId: Int?,
    val DealerCode: String,
    val DealerName: String,
    val Coordinates: String,
    val Id:Int,
)