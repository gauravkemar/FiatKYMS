package com.example.fiatkyms.helper

object Constants {


    val LONGITUDE: String = "longitude"
    val LATITUDE: String = "latitude"
    const val KEY_USER_ID = "id"
    const val LOGGEDIN = "loggedIn"
    const val IS_ADMIN = "isAdmin"
    const val USERNAME = "username"
    const val TOKEN = "token"

    const val NO_INTERNET = "No Internet Connection"
    const val NETWORK_FAILURE = "Network Failure"
    const val CONFIG_ERROR = "Please configure network details"
    const val INCOMPLETE_DETAILS = "Please fill the required details"
    const val EXCEPTION_ERROR="No Data Found"

    const val SHARED_PREF = "shared_pref"
    const val SERVER_IP = "server_ip"
    const val ISFIRSTTIME = "is_first_time"
    const val HTTP_ERROR_MESSAGE="message"

    const val SERVER_IP_SHARED = "192.168.1.105"

    const val GET = 1
    const val POST = 2
    const val HTTP_OK = 200
    const val HTTP_CREATED = 201
    const val HTTP_EXCEPTION = 202
    const val HTTP_UPDATED = 204
    const val HTTP_FOUND = 302
    const val HTTP_NOT_FOUND = 404
    const val HTTP_CONFLICT = 409
    const val HTTP_INTERNAL_SERVER_ERROR = 500
    const val DEFAULT_PORT = 5500
    const val HTTP_ERROR = 400

    const val GET_APP_DETAILS = "MobileApp/GetLatestApkVersion"
    const val GET_PRD_OUT_VIN_LIST = "Vehicle/GetProductionOutVehicle"
    const val DASHBOARD_DATA_ADMIN = "VehicleMilestone/GetVehicleandMilestoneDashboardDataAdmin"
    const val DASHBOARD_DATA = "VehicleMilestone/GetVehicleMilestoneDashBoardData"
    const val DASHBOARD_GRAPH_DATA = "VehicleMilestone/GetDriverwiseVehicleParkedCount"
    const val ADD_LOCATIONS = "LocationMapping/AddLocations"
    const val ADD_DEALERLOCATIONS = "Dealer/AddDealer"
    const val GET_DEALERLOCATIONS = "Dealer/GetAllDealers"
    const val GENERATE_TOKEN = "UserManagement/authenticate"
    const val GET_PARENT_LOCATION = "LocationMapping/GetParentLocation"
    const val GET_YARD_LOCATION = "LocationMapping/GetYardLocation"
    const val LOGIN_URL = "UserManagement/authenticate"
    const val ADD_VEHICLE_RFID_MAPPING = "VehicleRFID/AddVehicleRFIDMapping"
    const val ADD_VEHICLE_VIN = "Vehicle/AddVehicles"
    const val GET_VEHICLE_RFID_MAPPING = "VehicleRFID/GetVehicleRFIDMapping"
    const val PARK_IN_OUT_VEHICLE = "VehicleMilestone/AddVehicleMilestone"
    const val SEARCH_VEHICLE_LIST = "VehicleMilestone/GetVehicleMilestoneandVehicleDetails"
    //const val BASE_URL = "http://103.240.90.141:5050/service/api/"
   // const val BASE_URL = "http://192.168.1.27:5000/api/"
   //  const val BASE_URL = "http://192.168.1.205:8011/service/api/"
    //const val BASE_URL = "http://103.240.90.141:6767/Service/api/"
    const val BASE_URL = "http://10.16.178.70/api/"
    //const val BASE_URL = "http://192.168.1.43:5000/api/"
    const val BASE_URL_LOCAL = "http://103.240.90.141:5050/Service/api/"

}