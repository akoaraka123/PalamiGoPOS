package com.example.palamigopos.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.palamigopos.data.local.AppDatabase
import com.example.palamigopos.data.repository.OrderRepository
import com.example.palamigopos.utils.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    val orders = orderRepository.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // For filtering by specific date
    private val _filteredOrders = kotlinx.coroutines.flow.MutableStateFlow<List<com.example.palamigopos.data.model.OrderEntity>>(emptyList())
    val filteredOrders: kotlinx.coroutines.flow.StateFlow<List<com.example.palamigopos.data.model.OrderEntity>> = _filteredOrders

    fun filterByDateRange(startMillis: Long, endMillis: Long) {
        viewModelScope.launch {
            orderRepository.getOrdersByDateRange(startMillis, endMillis).collect { orders ->
                _filteredOrders.value = orders
            }
        }
    }

    fun clearFilter() {
        _filteredOrders.value = emptyList()
    }

    // Ticker that emits every minute to trigger day boundary recalculation
    private val ticker = flow {
        while (true) {
            emit(Unit)
            kotlinx.coroutines.delay(60_000) // Emit every minute
        }
    }

    private val startOfDay = DateTimeUtils.startOfToday()
    private val endOfDay = DateTimeUtils.endOfToday()

    val todaySummary: StateFlow<TodaySummary> = combine(
        ticker,
        orderRepository.getTodayTotalSales(DateTimeUtils.startOfToday(), DateTimeUtils.endOfToday()),
        orderRepository.getTodayOrderCount(DateTimeUtils.startOfToday(), DateTimeUtils.endOfToday())
    ) { _, totalSales, orderCount ->
        TodaySummary(totalSales = totalSales, orderCount = orderCount)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodaySummary(0.0, 0))

    fun orderItems(orderId: Int) = orderRepository.getOrderItemsByOrderId(orderId)

    fun deleteOrder(orderId: Int) {
        viewModelScope.launch {
            orderRepository.deleteOrder(orderId)
        }
    }

    fun refreshTodaySummary() {
        // This function triggers a refresh by recalculating the flows
        // The combine will recalculate with new startOfDay and endOfDay
    }

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
