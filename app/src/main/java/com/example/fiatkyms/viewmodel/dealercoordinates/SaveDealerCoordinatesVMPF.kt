package com.example.fiatkyms.viewmodel.dealercoordinates

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fiatkyms.repository.KYMSRepository


class SaveDealerCoordinatesVMPF(
    private val application: Application,
    private val kymsRepository: KYMSRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SaveDealerCoordinatesVM(application, kymsRepository) as T
    }
}