package com.example.fiatkyms.viewmodel.parkinout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fiatkyms.helper.Constants
import com.example.fiatkyms.helper.Resource
import com.example.fiatkyms.helper.Utils
import com.example.fiatkyms.model.GeneralResponse
import com.example.fiatkyms.model.parkinout.ParkInOutRequest
import com.example.fiatkyms.model.prdoutmodel.GetAllPrdOutVinList
import com.example.fiatkyms.repository.KYMSRepository
import com.kemarport.kymsmahindra.model.parkinout.GetGeofenceYardResponse
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class ParkInOutViewModel (
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application){
    val parkInOutMutableLiveData: MutableLiveData<Resource<GeneralResponse>> = MutableLiveData()

    fun parkInOut(
        token: String,
        baseUrl: String,
        parkInOutRequest: ParkInOutRequest
    ) {
        viewModelScope.launch {
            safeAPICallParkInOut(token,baseUrl, parkInOutRequest)
        }
    }
    private suspend fun safeAPICallParkInOut(  token: String,baseUrl: String, parkInOutRequest: ParkInOutRequest) {
        parkInOutMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.parkInOutVehicle(token,baseUrl, parkInOutRequest)
                parkInOutMutableLiveData.postValue(handleParkInOutResponse(response))
            } else {
                parkInOutMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    parkInOutMutableLiveData.postValue(Resource.Error("${t.message}"))
                }
                else -> parkInOutMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
    private fun handleParkInOutResponse(response: Response<GeneralResponse>): Resource<GeneralResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { Response ->
                return Resource.Success(Response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }
    /////////////////////////////////////////////////////////
    val getAllGeofenceMLD: MutableLiveData<Resource<GetGeofenceYardResponse>> =
        MutableLiveData()

    fun getYardGeofence(
        token: String,
        baseUrl: String
    ) {
        viewModelScope.launch {
            safeAPICallGetAllGeofence(token,baseUrl)
        }
    }

    private suspend fun safeAPICallGetAllGeofence(
        token: String,
        baseUrl: String
    ) {
        getAllGeofenceMLD.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getYardGeofence(token, baseUrl)
                getAllGeofenceMLD.postValue(handleGetAllGeofenceResponse(response))
            } else {
                getAllGeofenceMLD.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getAllGeofenceMLD.postValue(Resource.Error("${t.message}"))
                }
                else -> getAllGeofenceMLD.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleGetAllGeofenceResponse(response: Response<GetGeofenceYardResponse>): Resource<GetGeofenceYardResponse>? {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { response ->
                return Resource.Success(response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }


    ////get all PRD OUT Vin list
    val getAllPrdOutVinListMutable: MutableLiveData<Resource<ArrayList<GetAllPrdOutVinList>>> =
        MutableLiveData()

    fun getAllPrdOutVinList(
        token: String,
        baseUrl: String
    ) {
        viewModelScope.launch {
            safeAPICallGetAllPrdOutVinList(token,baseUrl)
        }
    }

    private suspend fun safeAPICallGetAllPrdOutVinList(
        token: String,
        baseUrl: String
    ) {
        getAllPrdOutVinListMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getAllPrdOutVinList(token, baseUrl)
                getAllPrdOutVinListMutable.postValue(handleGetAllPrdOutVinListResponse(response))
            } else {
                getAllPrdOutVinListMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getAllPrdOutVinListMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> getAllPrdOutVinListMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleGetAllPrdOutVinListResponse(response: Response<ArrayList<GetAllPrdOutVinList>>): Resource<ArrayList<GetAllPrdOutVinList>>? {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { response ->
                return Resource.Success(response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }



}