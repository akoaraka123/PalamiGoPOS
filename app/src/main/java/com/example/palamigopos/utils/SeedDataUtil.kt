package com.example.palamigopos.utils

import com.example.palamigopos.data.local.AppDatabase
import com.example.palamigopos.data.model.ProductEntity

object SeedDataUtil {

    suspend fun seedIfNeeded(database: AppDatabase) {
        val productDao = database.productDao()
        val count = productDao.countProducts()
        if (count > 0) return

        productDao.insertAll(
            listOf(
                ProductEntity(name = "Señor Latte", category = "Coffee", price = 49.0, isActive = true),
                ProductEntity(name = "Caramelo", category = "Coffee", price = 49.0, isActive = true),
                ProductEntity(name = "Brewbe", category = "Coffee", price = 49.0, isActive = true),
                ProductEntity(name = "Dirty Matcha", category = "Coffee", price = 59.0, isActive = true),

                ProductEntity(name = "Strawberry", category = "Milk Creation", price = 59.0, isActive = true),
                ProductEntity(name = "Blueberry", category = "Milk Creation", price = 59.0, isActive = true),
                ProductEntity(name = "Ube", category = "Milk Creation", price = 59.0, isActive = true),
                ProductEntity(name = "Matcha", category = "Milk Creation", price = 59.0, isActive = true),

                ProductEntity(name = "Green Apple", category = "Soda Spark", price = 45.0, isActive = true),
                ProductEntity(name = "Strawberry", category = "Soda Spark", price = 45.0, isActive = true),
                ProductEntity(name = "Blueberry", category = "Soda Spark", price = 45.0, isActive = true),
                ProductEntity(name = "Lemon Blue", category = "Soda Spark", price = 45.0, isActive = true),
                ProductEntity(name = "Lychee", category = "Soda Spark", price = 45.0, isActive = true)
            )
        )
    }
}
