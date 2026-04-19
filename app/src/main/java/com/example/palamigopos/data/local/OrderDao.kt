package com.example.palamigopos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.palamigopos.data.model.OrderEntity
import com.example.palamigopos.data.model.OrderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId ORDER BY id ASC")
    fun getOrderItemsByOrderId(orderId: Int): Flow<List<OrderItemEntity>>

    @Query("SELECT IFNULL(SUM(totalAmount), 0) FROM orders WHERE createdAt BETWEEN :startOfDay AND :endOfDay")
    fun getTodayTotalSales(startOfDay: Long, endOfDay: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM orders WHERE createdAt BETWEEN :startOfDay AND :endOfDay")
    fun getTodayOrderCount(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun getOrderCount(): Int

    // Daily sales report: groups by calendar date (local time) from createdAt
    @Query("""
        SELECT 
            (createdAt / 86400000) * 86400000 as dateMillis,
            IFNULL(SUM(totalAmount), 0) as totalSales,
            COUNT(*) as totalOrders
        FROM orders
        GROUP BY dateMillis
        ORDER BY dateMillis DESC
    """)
    fun getDailySalesReports(): Flow<List<DailySalesReportTuple>>
}

data class DailySalesReportTuple(
    val dateMillis: Long,
    val totalSales: Double,
    val totalOrders: Int
)
