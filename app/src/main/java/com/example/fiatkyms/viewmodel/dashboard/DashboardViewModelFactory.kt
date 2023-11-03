package com.example.fiatkyms.viewmodel.dashboard

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fiatkyms.repository.KYMSRepository

class DashboardViewModelFactory(
    private val application: Application,
    private val kymsRepository: KYMSRepository
) : ViewModelProvider.Factory   {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DashboardViewModel(application, kymsRepository) as T
    }
}