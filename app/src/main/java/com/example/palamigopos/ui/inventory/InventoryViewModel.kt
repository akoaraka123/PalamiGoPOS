package com.example.palamigopos.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.palamigopos.data.local.AppDatabase
import com.example.palamigopos.data.model.CategoryEntity
import com.example.palamigopos.data.model.ProductEntity
import com.example.palamigopos.data.repository.CategoryRepository
import com.example.palamigopos.data.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val products: StateFlow<List<ProductEntity>> = productRepository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addCategory(name: String, onError: (String) -> Unit) {
        if (name.isBlank()) {
            onError("Category name is required")
            return
        }

        viewModelScope.launch {
            categoryRepository.insertCategory(
                CategoryEntity(name = name.trim())
            )
        }
    }

    fun deleteCategory(categoryId: Int) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(categoryId)
        }
    }

    fun addProduct(name: String, category: String, price: Double, isActive: Boolean, onError: (String) -> Unit) {
        if (name.isBlank()) {
            onError("Product name is required")
            return
        }
        if (price <= 0) {
            onError("Invalid price")
            return
        }

        viewModelScope.launch {
            productRepository.insertProduct(
                ProductEntity(
                    name = name.trim(),
                    category = category.trim(),
                    price = price,
                    isActive = isActive
                )
            )
        }
    }

    fun updateProduct(product: ProductEntity, name: String, category: String, price: Double, isActive: Boolean, onError: (String) -> Unit) {
        if (name.isBlank()) {
            onError("Product name is required")
            return
        }
        if (price <= 0) {
            onError("Invalid price")
            return
        }

        viewModelScope.launch {
            productRepository.updateProduct(
                product.copy(
                    name = name.trim(),
                    category = category.trim(),
                    price = price,
                    isActive = isActive
                )
            )
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
        }
    }

    fun setActive(product: ProductEntity, active: Boolean) {
        viewModelScope.launch {
            productRepository.updateProduct(product.copy(isActive = active))
        }
    }

    class Factory(private val database: AppDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InventoryViewModel(
                ProductRepository(database.productDao()),
                CategoryRepository(database.categoryDao())
            ) as T
        }
    }
}
