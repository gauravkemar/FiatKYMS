package com.example.fiatkyms.viewmodel.vinrfidmapping

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fiatkyms.helper.Constants
import com.example.fiatkyms.helper.Resource
import com.example.fiatkyms.helper.Utils
import com.example.fiatkyms.model.GeneralResponse
import com.example.fiatkyms.model.vinrfidmapping.AddVehicle
import com.example.fiatkyms.model.vinrfidmapping.VinRfidRequest
import com.example.fiatkyms.repository.KYMSRepository

import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class VinRfidMappingViewModel(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {
    val vinRfidMutableLiveData: MutableLiveData<Resource<GeneralResponse>> = MutableLiveData()

    fun vinRfidMapping(
        token: String,
        baseUrl: String,
        vinRfidRequest: VinRfidRequest
    ) {
        viewModelScope.launch {
            safeAPICallVinRfid(token,baseUrl, vinRfidRequest)
        }
    }

    private suspend fun safeAPICallVinRfid(token: String,baseUrl: String,  vinRfidRequest: VinRfidRequest) {
        vinRfidMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.vinRfidMapping(token,baseUrl, vinRfidRequest)
                vinRfidMutableLiveData.postValue(handleVinRfidMappingResponse(response))
            } else {
                vinRfidMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    vinRfidMutableLiveData.postValue(Resource.Error("${t.message}"))
                }
                else -> vinRfidMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
    private fun handleVinRfidMappingResponse(response: Response<GeneralResponse>): Resource<GeneralResponse> {
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

    //////////////////////////////validate
    val validateVinRfidMutableLiveData: MutableLiveData<Resource<GeneralResponse>> = MutableLiveData()

    fun validateVinRfidMapping(
        token: String,
        baseUrl: String,
        vinRfidRequest: VinRfidRequest
    ) {
        viewModelScope.launch {
            safeAPICallValidateVinRfid(token,baseUrl, vinRfidRequest)
        }
    }

    private suspend fun safeAPICallValidateVinRfid(token: String,baseUrl: String,  vinRfidRequest: VinRfidRequest) {
        validateVinRfidMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.validateVinRfidMapping(token,baseUrl, vinRfidRequest)
                validateVinRfidMutableLiveData.postValue(handleValidateVinRfidMappingResponse(response))
            } else {
                validateVinRfidMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    validateVinRfidMutableLiveData.postValue(Resource.Error("${t.message}"))
                }
                else -> validateVinRfidMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
    private fun handleValidateVinRfidMappingResponse(response: Response<GeneralResponse>): Resource<GeneralResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let {  Response ->
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

///////////// add vehicle
val vinAddVehicleMutableLiveData: MutableLiveData<Resource<GeneralResponse>> = MutableLiveData()

    fun vinAddVehicle(
        token: String,
        baseUrl: String,
        addVehicle: AddVehicle
    ) {
        viewModelScope.launch {
            safeAPICallVinAddVehicle(token,baseUrl, addVehicle)
        }
    }

    private suspend fun safeAPICallVinAddVehicle(token: String,baseUrl: String,   addVehicle: AddVehicle) {
        vinRfidMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.vinAddVehicle(token,baseUrl, addVehicle)
                vinRfidMutableLiveData.postValue(handleVinVehicleAddResponse(response))
            } else {
                vinRfidMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    vinRfidMutableLiveData.postValue(Resource.Error("${t.message}"))
                }
                else -> vinRfidMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
    private fun handleVinVehicleAddResponse(response: Response<GeneralResponse>): Resource<GeneralResponse> {
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

}