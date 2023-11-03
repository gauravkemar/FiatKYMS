package com.example.fiatkyms.viewmodel.Geofence

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fiatkyms.helper.Constants
import com.example.fiatkyms.helper.Resource
import com.example.fiatkyms.helper.Utils
import com.example.fiatkyms.model.GeneralResponse
import com.example.fiatkyms.model.geofence.AddGeofenceRequest
import com.example.fiatkyms.model.geofence.GetAllGeofenceResponse
import com.example.fiatkyms.repository.KYMSRepository

import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class GeofenceVM(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {

    val addGeofenceMLD: MutableLiveData<Resource<GeneralResponse>> =
        MutableLiveData()
    //val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjEiLCJSb2xlIjoiU3VwZXJBZG1pbiIsIm5iZiI6MTY5MDk3OTU0MiwiZXhwIjoxNjkwOTgwNzQyLCJpYXQiOjE2OTA5Nzk1NDJ9.-4L6nYXZg09D8tTApz_2rntnIeLkpxt9SGGiPXtXfuA"

    fun addGeofence(
        token: String,
        baseUrl: String,
        addGeofenceRequest: AddGeofenceRequest
    ) {
        viewModelScope.launch {
            safeAPICallAddGeofence(token,baseUrl, addGeofenceRequest)
        }
    }

    private suspend fun safeAPICallAddGeofence(
        token: String,
        baseUrl: String,
        addGeofenceRequest: AddGeofenceRequest
    ) {
        addGeofenceMLD.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.addGeofence(token, baseUrl, addGeofenceRequest)
                addGeofenceMLD.postValue(handleAddGeofenceResponse(response))
            } else {
                addGeofenceMLD.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    addGeofenceMLD.postValue(Resource.Error("${t.message}"))
                }
                else -> addGeofenceMLD.postValue(Resource.Error(Constants.CONFIG_ERROR))
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

    val getAllGeofenceMLD: MutableLiveData<Resource<GetAllGeofenceResponse>> =
        MutableLiveData()

    fun getAllGeofence(
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
                val response = kymsRepository.getAllGeofence(token, baseUrl)
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

    private fun handleGetAllGeofenceResponse(response: Response<GetAllGeofenceResponse>): Resource<GetAllGeofenceResponse>? {
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