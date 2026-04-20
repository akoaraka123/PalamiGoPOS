package com.example.palamigopos.data.repository

import com.example.palamigopos.data.local.CategoryDao
import com.example.palamigopos.data.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun getCategoryById(categoryId: Int): CategoryEntity? = categoryDao.getCategoryById(categoryId)

    suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)

    suspend fun deleteCategory(categoryId: Int) = categoryDao.deleteCategory(categoryId)

    suspend fun deleteAllCategories() = categoryDao.deleteAllCategories()
}
