package com.example.palamigopos.data.repository

import com.example.palamigopos.data.local.OrderDao
import com.example.palamigopos.data.model.DailySalesReport
import com.example.palamigopos.data.model.OrderEntity
import com.example.palamigopos.data.model.OrderItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderRepository(
    private val orderDao: OrderDao
) {
    fun getAllOrders(): Flow<List<OrderEntity>> = orderDao.getAllOrders()

    fun getOrderItemsByOrderId(orderId: Int): Flow<List<OrderItemEntity>> =
        orderDao.getOrderItemsByOrderId(orderId)

    fun getTodayTotalSales(startOfDay: Long, endOfDay: Long): Flow<Double> =
        orderDao.getTodayTotalSales(startOfDay, endOfDay)

    fun getTodayOrderCount(startOfDay: Long, endOfDay: Long): Flow<Int> =
        orderDao.getTodayOrderCount(startOfDay, endOfDay)

    suspend fun insertOrderWithItems(order: OrderEntity, items: List<OrderItemEntity>) {
        val orderId = orderDao.insertOrder(order).toInt()
        val itemsWithId = items.map { it.copy(orderId = orderId) }
        orderDao.insertOrderItems(itemsWithId)
    }

    suspend fun getOrderCount(): Int = orderDao.getOrderCount()

    fun getDailySalesReports(): Flow<List<DailySalesReport>> =
        orderDao.getDailySalesReports().map { tuples ->
            tuples.map { DailySalesReport(it.dateMillis, it.totalSales, it.totalOrders) }
        }
}
