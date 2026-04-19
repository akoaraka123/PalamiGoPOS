package com.example.palamigopos.data.repository

import com.example.palamigopos.data.local.ProductDao
import com.example.palamigopos.data.model.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao
) {
    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()

    fun getActiveProducts(): Flow<List<ProductEntity>> = productDao.getActiveProducts()

    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> =
        productDao.getProductsByCategory(category)

    fun getActiveProductsByCategory(category: String): Flow<List<ProductEntity>> =
        productDao.getActiveProductsByCategory(category)

    suspend fun insertProduct(product: ProductEntity): Long = productDao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)

    suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)
}
