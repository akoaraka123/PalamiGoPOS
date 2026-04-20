package com.example.palamigopos.ui.pos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.palamigopos.data.local.AppDatabase
import com.example.palamigopos.data.model.CartItem
import com.example.palamigopos.data.model.CategoryEntity
import com.example.palamigopos.data.model.OrderEntity
import com.example.palamigopos.data.model.OrderItemEntity
import com.example.palamigopos.data.model.ProductEntity
import com.example.palamigopos.data.repository.CategoryRepository
import com.example.palamigopos.data.repository.OrderRepository
import com.example.palamigopos.data.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PosViewModel(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _selectedCategory = MutableLiveData<String>()
    val selectedCategory: LiveData<String> = _selectedCategory

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _products = MutableLiveData<List<ProductEntity>>(emptyList())
    val products: LiveData<List<ProductEntity>> = _products

    private var productsJob: Job? = null

    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private val _totalAmount = MutableLiveData(0.0)
    val totalAmount: LiveData<Double> = _totalAmount

    init {
        // Set initial category when categories are loaded
        viewModelScope.launch {
            categories.collect { categoryList ->
                if (_selectedCategory.value == null && categoryList.isNotEmpty()) {
                    _selectedCategory.value = categoryList.first().name
                    loadProductsForCategory(categoryList.first().name)
                }
            }
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
        loadProductsForCategory(category)
    }

    private fun loadProductsForCategory(category: String) {
        productsJob?.cancel()
        productsJob = viewModelScope.launch {
            productRepository.getActiveProductsByCategory(category).collect { list ->
                _products.postValue(list)
            }
        }
    }

    fun addToCart(product: ProductEntity) {
        val current = _cartItems.value.orEmpty().toMutableList()
        val index = current.indexOfFirst { it.productId == product.id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            current.add(
                CartItem(
                    productId = product.id,
                    name = product.name,
                    price = product.price,
                    quantity = 1
                )
            )
        }
        _cartItems.value = current
        recomputeTotal()
    }

    fun increaseQty(productId: Int) {
        val current = _cartItems.value.orEmpty().toMutableList()
        val index = current.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            val item = current[index]
            current[index] = item.copy(quantity = item.quantity + 1)
            _cartItems.value = current
            recomputeTotal()
        }
    }

    fun decreaseQty(productId: Int) {
        val current = _cartItems.value.orEmpty().toMutableList()
        val index = current.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            val item = current[index]
            val newQty = item.quantity - 1
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                current[index] = item.copy(quantity = newQty)
            }
            _cartItems.value = current
            recomputeTotal()
        }
    }

    fun removeItem(productId: Int) {
        val current = _cartItems.value.orEmpty().filterNot { it.productId == productId }
        _cartItems.value = current
        recomputeTotal()
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _totalAmount.value = 0.0
    }

    private fun recomputeTotal() {
        _totalAmount.value = _cartItems.value.orEmpty().sumOf { it.subtotal }
    }

    fun canCheckout(): Boolean = _cartItems.value.orEmpty().isNotEmpty()

    fun computeChange(cashReceived: Double): Double {
        val total = _totalAmount.value ?: 0.0
        return cashReceived - total
    }

    fun confirmPayment(
        cashReceived: Double,
        paymentMethod: String = "Cash",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cart = _cartItems.value.orEmpty()
        val total = _totalAmount.value ?: 0.0
        if (cart.isEmpty()) {
            onError("Cart is empty")
            return
        }
        if (cashReceived < total) {
            onError("Insufficient Amount")
            return
        }

        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val orderNumber = generateOrderNumber(now)
                val change = cashReceived - total

                val order = OrderEntity(
                    orderNumber = orderNumber,
                    totalAmount = total,
                    cashReceived = cashReceived,
                    changeAmount = change,
                    paymentMethod = paymentMethod,
                    createdAt = now
                )

                val items = cart.map {
                    OrderItemEntity(
                        orderId = 0,
                        productName = it.name,
                        productPrice = it.price,
                        quantity = it.quantity,
                        subtotal = it.subtotal
                    )
                }

                orderRepository.insertOrderWithItems(order, items)
                clearCart()
                onSuccess()
            } catch (t: Throwable) {
                onError(t.message ?: "Failed to save order")
            }
        }
    }

    private suspend fun generateOrderNumber(timestamp: Long): String {
        val datePart = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(timestamp))
        val totalCount = orderRepository.getOrderCount() + 1
        return "PAL-$datePart-${String.format(Locale.getDefault(), "%04d", totalCount)}"
    }

    class Factory(private val database: AppDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val productRepo = ProductRepository(database.productDao())
            val orderRepo = OrderRepository(database.orderDao())
            val categoryRepo = CategoryRepository(database.categoryDao())
            return PosViewModel(productRepo, orderRepo, categoryRepo) as T
        }
    }
}
