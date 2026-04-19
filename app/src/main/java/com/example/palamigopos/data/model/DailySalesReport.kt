package com.example.palamigopos.data.model

data class DailySalesReport(
    val dateMillis: Long,
    val totalSales: Double,
    val totalOrders: Int
)
