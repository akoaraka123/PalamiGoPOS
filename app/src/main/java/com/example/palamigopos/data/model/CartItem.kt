package com.example.palamigopos.data.model

data class CartItem(
    val productId: Int,
    val name: String,
    val price: Double,
    val quantity: Int
) {
    val subtotal: Double
        get() = price * quantity
}
