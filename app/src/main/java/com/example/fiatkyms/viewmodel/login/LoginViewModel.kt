package com.example.fiatkyms.viewmodel.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fiatkyms.helper.Constants
import com.example.fiatkyms.helper.Resource
import com.example.fiatkyms.helper.Utils
import com.example.fiatkyms.model.appDetails.GetAppDetailsResponse
import com.example.fiatkyms.model.login.LoginRequest
import com.example.fiatkyms.model.login.LoginResponse
import com.example.fiatkyms.repository.KYMSRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class LoginViewModel (
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {
    val loginMutableLiveData: MutableLiveData<Resource<LoginResponse>> = MutableLiveData()

    fun login(
        baseUrl: String,
        loginRequest: LoginRequest
    ) {
        viewModelScope.launch {
            safeAPICallDtmsLogin(baseUrl, loginRequest)
        }
    }

    private fun handleDtmsUserLoginResponse(response: Response<LoginResponse>): Resource<LoginResponse> {
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

    private suspend fun safeAPICallDtmsLogin(baseUrl: String, loginRequest: LoginRequest) {
        loginMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.login(baseUrl, loginRequest)
                loginMutableLiveData.postValue(handleDtmsUserLoginResponse(response))
            } else {
                loginMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    loginMutableLiveData.postValue(Resource.Error("${t.message}"))
                }
                else -> loginMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    //getApp details
    val getAppDetailsMutableLiveData: MutableLiveData<Resource<GetAppDetailsResponse>> = MutableLiveData()

    fun getAppDetails(
        baseUrl: String
    ) = viewModelScope.launch {
        safeAPICallGetAppDetails(baseUrl)
    }

    private suspend fun safeAPICallGetAppDetails(baseUrl: String) {
        getAppDetailsMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getAppDetails(baseUrl)
                getAppDetailsMutableLiveData.postValue(handleGetAppDetailsResponse(response))
            } else {
                getAppDetailsMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getAppDetailsMutableLiveData.postValue(Resource.Error("${t.message}"))

                }
                else -> getAppDetailsMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleGetAppDetailsResponse(response: Response<GetAppDetailsResponse>): Resource<GetAppDetailsResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { appDetailsResponse ->
                return Resource.Success(appDetailsResponse)
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