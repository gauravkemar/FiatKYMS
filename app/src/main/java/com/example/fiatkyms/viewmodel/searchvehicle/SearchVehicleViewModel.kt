package com.example.fiatkyms.viewmodel.searchvehicle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fiatkyms.helper.Constants
import com.example.fiatkyms.helper.Resource
import com.example.fiatkyms.helper.Utils
import com.example.fiatkyms.model.login.GenerateTokenResponse
import com.example.fiatkyms.model.searchvehicle.SearchVehiclePostRequest
import com.example.fiatkyms.model.searchvehicle.VehicleListResponseItem
import com.example.fiatkyms.repository.KYMSRepository

import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class SearchVehicleViewModel(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application)
{

    val searchVehicleListMutableLiveData: MutableLiveData<Resource<MutableList<VehicleListResponseItem>?>> = MutableLiveData()
    val generateTokenMutableLiveData: MutableLiveData<Resource<GenerateTokenResponse>> = MutableLiveData()

    init {
        generateToken()
    }

    fun searchVehicleList(
        token: String,
        baseUrl: String ,

    ) {
        viewModelScope.launch {
            safeAPICallSearchVehicleList(token,baseUrl)
        }
    }
    private suspend fun safeAPICallSearchVehicleList(  token: String,baseUrl: String ) {
        searchVehicleListMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.searchVehicleList(token,baseUrl)
                searchVehicleListMutableLiveData.postValue(Resource.Success(response.body())!!)
            } else {
                searchVehicleListMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    searchVehicleListMutableLiveData.postValue(Resource.Error("${t.message}"))
                }
                else -> searchVehicleListMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private suspend fun generateToken(baseUrl: String ,vehicleListRequest: SearchVehiclePostRequest) {
        generateTokenMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.generateToken(baseUrl, vehicleListRequest)
                generateTokenMutableLiveData.postValue(handleGenerateTokenResponse(response))
            } else {
                generateTokenMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    generateTokenMutableLiveData.postValue(Resource.Error("${t.message}"))
                }
                else -> generateTokenMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
    private fun handleSearchVehicleListResponse(response: Response<MutableList<VehicleListResponseItem>>): Resource<MutableList<VehicleListResponseItem>> {
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

    private fun handleGenerateTokenResponse(response: Response<GenerateTokenResponse>): Resource<GenerateTokenResponse> {
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

    fun generateToken() {
        viewModelScope.launch {
            generateToken(
                baseUrl = Constants.BASE_URL, vehicleListRequest = SearchVehiclePostRequest(
                    UserName = "Bhola",
                    Password = "Bhola@123"
                )
            )
        }
    }

}