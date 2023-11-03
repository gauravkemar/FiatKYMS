package com.example.fiatkyms.repository


import com.example.fiatkyms.api.RetrofitInstance
import com.example.fiatkyms.model.dashboard.DashboardPostRequest
import com.example.fiatkyms.model.dealercoordinates.AddDealerCoordinatesRequestResponse
import com.example.fiatkyms.model.geofence.AddGeofenceRequest
import com.example.fiatkyms.model.login.LoginRequest
import com.example.fiatkyms.model.parkinout.ParkInOutRequest
import com.example.fiatkyms.model.searchvehicle.SearchVehiclePostRequest
import com.example.fiatkyms.model.vinrfidmapping.AddVehicle
import com.example.fiatkyms.model.vinrfidmapping.VinRfidRequest
import retrofit2.http.Body
import retrofit2.http.Header

class KYMSRepository {
    suspend fun getAppDetails(
        baseUrl: String
    ) = RetrofitInstance.api(baseUrl).getAppDetails()
    suspend fun addGeofence(
        @Header("Authorization") token: String,
        baseUrl: String,
        addGeofenceRequest: AddGeofenceRequest
    ) = RetrofitInstance.api(baseUrl).addGeofence(token, addGeofenceRequest)

    suspend fun getAllGeofence(
        @Header("Authorization") bearerToken: String,
        baseUrl: String
    ) = RetrofitInstance.api(baseUrl).getAllGeofence(bearerToken)

    suspend fun getAllPrdOutVinList(
        @Header("Authorization") bearerToken: String,
        baseUrl: String
    ) = RetrofitInstance.api(baseUrl).getAllPrdOutVinList(bearerToken)
    suspend fun getYardGeofence(
        @Header("Authorization") bearerToken: String,
        baseUrl: String
    ) = RetrofitInstance.api(baseUrl).getYardGeofence(bearerToken)

    suspend fun login(
        baseUrl: String,
        loginRequest: LoginRequest
    ) = RetrofitInstance.api(baseUrl).login(loginRequest)

    suspend fun vinRfidMapping(
        @Header("Authorization") bearerToken: String,
        baseUrl: String,
        vinRfidRequest: VinRfidRequest
    ) = RetrofitInstance.api(baseUrl).vinRfidMapping(bearerToken,vinRfidRequest)

    suspend fun vinAddVehicle(
        @Header("Authorization") bearerToken: String,
        baseUrl: String,
        addVehicle: AddVehicle
    ) = RetrofitInstance.api(baseUrl).vinAddVehicle(bearerToken,addVehicle)

    suspend fun validateVinRfidMapping(
        @Header("Authorization") bearerToken: String,
        baseUrl: String,
        vinRfidRequest: VinRfidRequest
    ) = RetrofitInstance.api(baseUrl).validateVinRfidMapping(bearerToken,vinRfidRequest)

    suspend fun parkInOutVehicle(
        @Header("Authorization") bearerToken: String,
        baseUrl: String,
        parkInOutRequest: ParkInOutRequest
    ) = RetrofitInstance.api(baseUrl).parkInOutVehicle(bearerToken,parkInOutRequest)

    suspend fun searchVehicleList(
        @Header("Authorization") bearerToken: String,
        baseUrl: String,
    ) = RetrofitInstance.api(baseUrl).searchVehicleList(bearerToken)

    suspend fun getAllDealerList(
        @Header("Authorization") bearerToken: String,
        baseUrl: String,
    ) = RetrofitInstance.api(baseUrl).getAllDealerList(bearerToken)

    suspend fun addDealerGeofence(
        @Header("Authorization") token: String,
        baseUrl: String,
        addDealerCoordinatesRequestResponse: AddDealerCoordinatesRequestResponse
    ) = RetrofitInstance.api(baseUrl).addDealerGeofence(token, addDealerCoordinatesRequestResponse)


    suspend fun generateToken(
        baseUrl: String,
        @Body
        vehicleSearchListRequest: SearchVehiclePostRequest
    ) = RetrofitInstance.api(baseUrl).generateToken(vehicleSearchListRequest)


    suspend fun getDashboardGraphData(
        baseUrl: String,
        @Body
        dashboardPostRequest: DashboardPostRequest
    ) = RetrofitInstance.api(baseUrl).getDashBoardGraphData(dashboardPostRequest)

    suspend fun getDashboardData(
        baseUrl: String,
        @Body
        dashboardPostRequest: DashboardPostRequest
    ) = RetrofitInstance.api(baseUrl).getDashboardData(dashboardPostRequest)

    suspend fun getDashboardDataAdmin(
        baseUrl: String,
        @Body
        dashboardPostRequest: DashboardPostRequest
    ) = RetrofitInstance.api(baseUrl).getDashboardDataAdmin(dashboardPostRequest)
}