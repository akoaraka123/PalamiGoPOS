package com.example.palamigopos.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.palamigopos.data.local.AppDatabase
import com.example.palamigopos.data.model.DailySalesReport
import com.example.palamigopos.data.repository.OrderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ReportsViewModel(
    orderRepository: OrderRepository
) : ViewModel() {

    val dailyReports: StateFlow<List<DailySalesReport>> = orderRepository.getDailySalesReports()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    class Factory(private val database: AppDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportsViewModel(OrderRepository(database.orderDao())) as T
        }
    }
}
