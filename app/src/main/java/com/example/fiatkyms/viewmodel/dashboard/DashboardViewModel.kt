package com.example.fiatkyms.viewmodel.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fiatkyms.helper.Constants
import com.example.fiatkyms.helper.Resource
import com.example.fiatkyms.helper.Utils
import com.example.fiatkyms.model.dashboard.DashboardDataResponseItem
import com.example.fiatkyms.model.dashboard.DashboardGraphResponseItem
import com.example.fiatkyms.model.dashboard.DashboardPostRequest
import com.example.fiatkyms.model.dashboard.GetVehicleDashboardDataAdminResponse
import com.example.fiatkyms.repository.KYMSRepository

import kotlinx.coroutines.launch

class DashboardViewModel(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {

    private val _dashboardGraphMutableLiveData = MutableLiveData<Resource<List<DashboardGraphResponseItem>?>>()
    val dashboardGraphMutableLiveData = _dashboardGraphMutableLiveData

    private val _dashboardDataMutableLiveData = MutableLiveData<Resource<List<DashboardDataResponseItem>?>>()
    val dashboardDataMutableLiveData = _dashboardDataMutableLiveData

    private val _dashboardDataAdminMutableLiveData = MutableLiveData<Resource<List<GetVehicleDashboardDataAdminResponse>?>>()
    val dashboardDataAdminMutableLiveData = _dashboardDataAdminMutableLiveData

    fun getDashboardDataAdmin(
        baseUrl: String = Constants.BASE_URL,
        request: DashboardPostRequest
    ) {
        viewModelScope.launch {
            fetchDashboardDataAdmin(baseUrl,request )
        }
    }


    fun getDashboardData(
        baseUrl: String = Constants.BASE_URL,
        request: DashboardPostRequest
    ) {
        viewModelScope.launch {
            fetchDashboardData(baseUrl, request)
        }
    }

    fun getDashboardGraphData(
        baseUrl: String = Constants.BASE_URL,
        request: DashboardPostRequest
    ) {
        viewModelScope.launch {
            fetchDashboardGraphData(baseUrl, request)
        }
    }

    private suspend fun fetchDashboardDataAdmin(
        baseUrl: String = Constants.BASE_URL,
        request: DashboardPostRequest
    ) {
        dashboardDataAdminMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getDashboardDataAdmin(baseUrl ,request)
                dashboardDataAdminMutableLiveData.postValue(Resource.Success(response.body()))
            } else {
                dashboardDataAdminMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (e: Exception) {
            when (e) {
                is Exception -> {
                    dashboardDataAdminMutableLiveData.postValue(Resource.Error("${e.message}"))
                }

                else -> dashboardDataAdminMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private suspend fun fetchDashboardData(
        baseUrl: String = Constants.BASE_URL,
        request: DashboardPostRequest
    ) {
        _dashboardDataMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getDashboardData(baseUrl, request)
                _dashboardDataMutableLiveData.postValue(Resource.Success(response.body()))
            } else {
                _dashboardDataMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (e: Exception) {
            when (e) {
                is Exception -> {
                    _dashboardDataMutableLiveData.postValue(Resource.Error("${e.message}"))
                }

                else -> _dashboardDataMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }


    private suspend fun fetchDashboardGraphData(
        baseUrl: String = Constants.BASE_URL,
        request: DashboardPostRequest
    ) {
        _dashboardGraphMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getDashboardGraphData(baseUrl, request)
                _dashboardGraphMutableLiveData.postValue(Resource.Success(response.body()))
            } else {
                _dashboardGraphMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (e: Exception) {
            when (e) {
                is Exception -> {
                    _dashboardGraphMutableLiveData.postValue(Resource.Error("${e.message}"))
                }

                else -> _dashboardGraphMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

}