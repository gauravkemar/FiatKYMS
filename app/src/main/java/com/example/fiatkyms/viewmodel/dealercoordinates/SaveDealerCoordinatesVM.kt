package com.example.fiatkyms.viewmodel.dealercoordinates

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fiatkyms.helper.Constants
import com.example.fiatkyms.helper.Resource
import com.example.fiatkyms.helper.Utils
import com.example.fiatkyms.model.GeneralResponse
import com.example.fiatkyms.model.dealercoordinates.AddDealerCoordinatesRequestResponse
import com.example.fiatkyms.model.dealercoordinates.GetDealerDataResponse
import com.example.fiatkyms.repository.KYMSRepository

import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class SaveDealerCoordinatesVM(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {
    val dealerListLiveData: MutableLiveData<Resource<ArrayList<GetDealerDataResponse>>> = MutableLiveData()

    fun getAllDealers(
        token: String,
        baseUrl: String
    ) {
        viewModelScope.launch {
            safeAPICallGetAllDealers(token,baseUrl )
        }
    }
    private suspend fun safeAPICallGetAllDealers( token: String,baseUrl: String) {
        dealerListLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getAllDealerList(token,baseUrl)
                dealerListLiveData.postValue(handleGetAllDealersResponse(response))
            } else {
                dealerListLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    dealerListLiveData.postValue(Resource.Error("${t.message}"))
                }
                else -> dealerListLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
    private fun handleGetAllDealersResponse(response: Response<ArrayList<GetDealerDataResponse>>): Resource<ArrayList<GetDealerDataResponse>>{
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

    ////save dealer cooordintates

    val addDealerGeofence: MutableLiveData<Resource<GeneralResponse>> =
        MutableLiveData()

    fun addDealerGeofence(
        token: String,
        baseUrl: String,
        addDealerCoordinatesRequestResponse: AddDealerCoordinatesRequestResponse
    ) {
        viewModelScope.launch {
            safeAPICallAddDealerGeofence(token,baseUrl, addDealerCoordinatesRequestResponse)
        }
    }

    private suspend fun safeAPICallAddDealerGeofence(
        token: String,
        baseUrl: String,
        addDealerCoordinatesRequestResponse: AddDealerCoordinatesRequestResponse
    ) {
        addDealerGeofence.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.addDealerGeofence(token, baseUrl, addDealerCoordinatesRequestResponse)
                addDealerGeofence.postValue(handleAddGeofenceResponse(response))
            } else {
                addDealerGeofence.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    addDealerGeofence.postValue(Resource.Error("${t.message}"))
                }
                else -> addDealerGeofence.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleAddGeofenceResponse(response: Response<GeneralResponse>): Resource<GeneralResponse>? {
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