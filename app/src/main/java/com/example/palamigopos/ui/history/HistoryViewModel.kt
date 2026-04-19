package com.example.palamigopos.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.palamigopos.data.local.AppDatabase
import com.example.palamigopos.data.repository.OrderRepository
import com.example.palamigopos.utils.DateTimeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    val orders = orderRepository.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val startOfDay = DateTimeUtils.startOfToday()
    private val endOfDay = DateTimeUtils.endOfToday()

    val todaySummary: StateFlow<TodaySummary> = combine(
        orderRepository.getTodayTotalSales(startOfDay, endOfDay),
        orderRepository.getTodayOrderCount(startOfDay, endOfDay)
    ) { totalSales, orderCount ->
        TodaySummary(totalSales = totalSales, orderCount = orderCount)
    }.stateIn(viewModelScope, SharingStarted.Lazily, TodaySummary(0.0, 0))

    fun orderItems(orderId: Int) = orderRepository.getOrderItemsByOrderId(orderId)

    data class TodaySummary(
        val totalSales: Double,
        val orderCount: Int
    )

    class Factory(private val database: AppDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val orderRepo = OrderRepository(database.orderDao())
            return HistoryViewModel(orderRepo) as T
        }
    }
}
