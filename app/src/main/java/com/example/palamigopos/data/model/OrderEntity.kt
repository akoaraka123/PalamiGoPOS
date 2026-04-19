package com.example.palamigopos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orderNumber: String,
    val totalAmount: Double,
    val cashReceived: Double,
    val changeAmount: Double,
    val createdAt: Long
)
