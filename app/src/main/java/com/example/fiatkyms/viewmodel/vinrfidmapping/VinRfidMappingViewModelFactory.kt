package com.example.fiatkyms.viewmodel.vinrfidmapping

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fiatkyms.repository.KYMSRepository


class VinRfidMappingViewModelFactory(
    private val application: Application,
    private val kymsRepository: KYMSRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VinRfidMappingViewModel(application, kymsRepository) as T
    }
}