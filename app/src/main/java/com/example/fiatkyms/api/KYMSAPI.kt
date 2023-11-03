package com.example.fiatkyms.api


import com.example.fiatkyms.helper.Constants
import com.example.fiatkyms.model.GeneralResponse
import com.example.fiatkyms.model.appDetails.GetAppDetailsResponse
import com.example.fiatkyms.model.dashboard.DashboardDataResponseItem
import com.example.fiatkyms.model.dashboard.DashboardGraphResponseItem
import com.example.fiatkyms.model.dashboard.DashboardPostRequest
import com.example.fiatkyms.model.dashboard.GetVehicleDashboardDataAdminResponse
import com.example.fiatkyms.model.dealercoordinates.AddDealerCoordinatesRequestResponse
import com.example.fiatkyms.model.dealercoordinates.GetDealerDataResponse
import com.example.fiatkyms.model.geofence.AddGeofenceRequest
import com.example.fiatkyms.model.geofence.GetAllGeofenceResponse
import com.example.fiatkyms.model.login.GenerateTokenResponse
import com.example.fiatkyms.model.login.LoginRequest
import com.example.fiatkyms.model.login.LoginResponse
import com.example.fiatkyms.model.parkinout.ParkInOutRequest
import com.example.fiatkyms.model.prdoutmodel.GetAllPrdOutVinList
import com.example.fiatkyms.model.searchvehicle.SearchVehiclePostRequest
import com.example.fiatkyms.model.searchvehicle.VehicleListResponseItem
import com.example.fiatkyms.model.vinrfidmapping.AddVehicle
import com.example.fiatkyms.model.vinrfidmapping.VinRfidRequest
import com.kemarport.kymsmahindra.model.parkinout.GetGeofenceYardResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST


interface KYMSAPI {
    @GET(Constants.GET_APP_DETAILS)
    suspend fun getAppDetails(
    ): Response<GetAppDetailsResponse>

    @POST(Constants.ADD_LOCATIONS)
    suspend fun addGeofence(
        @Header("Authorization") token: String,
        @Body
        addGeofenceRequest: AddGeofenceRequest
    ): Response<GeneralResponse>

    @GET(Constants.GET_PRD_OUT_VIN_LIST)
    suspend fun getAllPrdOutVinList(
        @Header("Authorization") bearerToken: String,
    ): Response<ArrayList<GetAllPrdOutVinList>>

    @POST(Constants.LOGIN_URL)
    suspend fun login(
        @Body
        loginRequest: LoginRequest
    ): Response<LoginResponse>

    @POST(Constants.ADD_VEHICLE_RFID_MAPPING)
    suspend fun vinRfidMapping(
        @Header("Authorization") bearerToken: String,
        @Body
        vinRfidRequest: VinRfidRequest
    ): Response<GeneralResponse>
    @POST(Constants.ADD_VEHICLE_VIN)
    suspend fun vinAddVehicle(
        @Header("Authorization") bearerToken: String,
        @Body
        addVehicle: AddVehicle
    ): Response<GeneralResponse>

    @POST(Constants.GET_VEHICLE_RFID_MAPPING)
    suspend fun validateVinRfidMapping(
        @Header("Authorization") bearerToken: String,
        @Body
        vinRfidRequest: VinRfidRequest
    ): Response<GeneralResponse>

    @POST(Constants.PARK_IN_OUT_VEHICLE)
    suspend fun parkInOutVehicle(
        @Header("Authorization") bearerToken: String,
        @Body
        parkInOutRequest: ParkInOutRequest
    ): Response<GeneralResponse>

    @GET(Constants.SEARCH_VEHICLE_LIST)
    suspend fun searchVehicleList(
        @Header("Authorization") bearerToken: String,
    ): Response<MutableList<VehicleListResponseItem>>

    @GET(Constants.GET_DEALERLOCATIONS)
    suspend fun getAllDealerList(
        @Header("Authorization") bearerToken: String,
    ): Response<ArrayList<GetDealerDataResponse>>

    @POST(Constants.ADD_DEALERLOCATIONS)
    suspend fun addDealerGeofence(
        @Header("Authorization") token: String,
        @Body
        addDealerCoordinatesRequestResponse: AddDealerCoordinatesRequestResponse
    ): Response<GeneralResponse>

    @GET(Constants.GET_PARENT_LOCATION)
    suspend fun getAllGeofence(
        @Header("Authorization") token: String
    ): Response<GetAllGeofenceResponse>

    @GET(Constants.GET_YARD_LOCATION)
    suspend fun getYardGeofence(
        @Header("Authorization") token: String
    ): Response<GetGeofenceYardResponse>

    @POST(Constants.GENERATE_TOKEN)
    suspend fun generateToken(
        @Body
        vehicleSearchListRequest: SearchVehiclePostRequest
    ): Response<GenerateTokenResponse>

    @POST(Constants.DASHBOARD_GRAPH_DATA)
    suspend fun getDashBoardGraphData(
        @Body
        dashboardPostRequest: DashboardPostRequest
    ): Response<List<DashboardGraphResponseItem>>

    @POST(Constants.DASHBOARD_DATA)
    suspend fun getDashboardData(
        @Body
        dashboardPostRequest: DashboardPostRequest
    ): Response<List<DashboardDataResponseItem>>

    @POST(Constants.DASHBOARD_DATA_ADMIN)
    suspend fun getDashboardDataAdmin(
        @Body
        dashboardPostRequest: DashboardPostRequest
    ): Response<List<GetVehicleDashboardDataAdminResponse>>





}