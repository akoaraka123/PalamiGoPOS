package com.example.palamigopos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.palamigopos.R
import com.example.palamigopos.data.local.AppDatabase
import com.example.palamigopos.databinding.ActivityMainBinding
import com.example.palamigopos.ui.adapter.CartAdapter
import com.example.palamigopos.ui.adapter.ProductAdapter
import com.example.palamigopos.ui.history.HistoryActivity
import com.example.palamigopos.ui.inventory.InventoryActivity
import com.example.palamigopos.ui.pos.PosViewModel
import com.example.palamigopos.ui.reports.ReportsActivity
import com.example.palamigopos.utils.CurrencyUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import android.content.Intent
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val viewModel: PosViewModel by viewModels {
        PosViewModel.Factory(AppDatabase.getInstance(this))
    }

    private val productAdapter = ProductAdapter { product ->
        viewModel.addToCart(product)
    }

    private val cartAdapter = CartAdapter(
        onIncrease = { viewModel.increaseQty(it.productId) },
        onDecrease = { viewModel.decreaseQty(it.productId) },
        onRemove = { viewModel.removeItem(it.productId) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupProductGrid()

        binding.rvProducts.adapter = productAdapter

        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = cartAdapter
        }

        // Observe categories and dynamically create chips
        lifecycleScope.launch {
            viewModel.categories.collect { categories ->
                updateCategoryChips(categories)
            }
        }

        // Restore category chip selection on rotation
        viewModel.selectedCategory.value?.let { updateCategoryChipSelection(it) }

        viewModel.products.observe(this) { products ->
            productAdapter.submitList(products)
        }

        viewModel.cartItems.observe(this) { cart ->
            cartAdapter.submitList(cart)
            binding.btnCheckout.isEnabled = cart.isNotEmpty()
            binding.btnCheckout.alpha = if (cart.isNotEmpty()) 1.0f else 0.55f
        }

        viewModel.totalAmount.observe(this) { total ->
            binding.tvTotal.text = CurrencyUtils.format(total)
        }

        // Restore selected category chip UI state
        viewModel.selectedCategory.observe(this) { category ->
            updateCategoryChipSelection(category)
        }

        binding.btnEmptyCart.setOnClickListener {
            viewModel.clearCart()
        }

        binding.btnCheckout.setOnClickListener {
            showCheckoutDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }
            R.id.action_inventory -> {
                startActivity(Intent(this, InventoryActivity::class.java))
                true
            }
            R.id.action_reports -> {
                startActivity(Intent(this, ReportsActivity::class.java))
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh products when returning from inventory (in case active status changed)
        val currentCategory = viewModel.selectedCategory.value
        currentCategory?.let { viewModel.setCategory(it) }
    }

    /**
     * Dynamically creates category chips based on categories from database
     */
    private fun updateCategoryChips(categories: List<com.example.palamigopos.data.model.CategoryEntity>) {
        binding.chipGroupCategories.removeAllViews()
        
        categories.forEach { category ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = category.name
                isCheckable = true
                isClickable = true
                setOnClickListener {
                    viewModel.setCategory(category.name)
                }
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        viewModel.setCategory(category.name)
                    }
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
        
        // Select the first category by default if none selected
        if (binding.chipGroupCategories.checkedChipId == -1 && categories.isNotEmpty()) {
            (binding.chipGroupCategories.getChildAt(0) as? com.google.android.material.chip.Chip)?.isChecked = true
        }
    }

    /**
     * Updates the chip selection UI to match the selected category.
     * Called on rotation to restore UI state.
     */
    private fun updateCategoryChipSelection(category: String) {
        for (i in 0 until binding.chipGroupCategories.childCount) {
            val chip = binding.chipGroupCategories.getChildAt(i) as? com.google.android.material.chip.Chip
            chip?.isChecked = (chip.text == category)
        }
    }

    /**
     * Calculates optimal span count based on screen width and minimum card width
     * Ensures product cards are at least 160dp wide for good readability
     */
    private fun calculateSpanCount(): Int {
        val displayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val minCardWidthDp = 160f  // Minimum comfortable card width
        val marginsAndPaddingDp = 32f  // Account for margins
        
        val availableWidthDp = screenWidthDp - marginsAndPaddingDp
        val spanCount = maxOf(2, (availableWidthDp / minCardWidthDp).toInt())
        
        // For tablets (sw600dp+), use resources-defined span count if available
        val resourcesSpanCount = try {
            resources.getInteger(R.integer.product_span_count)
        } catch (e: Exception) {
            spanCount
        }
        
        return if (resources.configuration.smallestScreenWidthDp >= 600) {
            resourcesSpanCount
        } else {
            spanCount.coerceIn(2, 4)  // Cap at 4 columns max on phones
        }
    }

    private fun setupProductGrid() {
        val spanCount = calculateSpanCount()
        val layoutManager = GridLayoutManager(this, spanCount)
        binding.rvProducts.layoutManager = layoutManager
    }

    private fun showCheckoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_checkout, null)
        val total = viewModel.totalAmount.value ?: 0.0

        val tvTotal = dialogView.findViewById<TextView>(R.id.tvTotal)
        val tvPaymentStatus = dialogView.findViewById<TextView>(R.id.tvPaymentStatus)
        val rgPaymentMethod = dialogView.findViewById<RadioGroup>(R.id.rgPaymentMethod)
        val rbCash = dialogView.findViewById<RadioButton>(R.id.rbCash)
        val rbGCash = dialogView.findViewById<RadioButton>(R.id.rbGCash)
        val tilCash = dialogView.findViewById<TextInputLayout>(R.id.tilCash)
        val tilGCashRef = dialogView.findViewById<TextInputLayout>(R.id.tilGCashRef)
        val etCash = dialogView.findViewById<EditText>(R.id.etCash)

        tvTotal.text = "Total: ${CurrencyUtils.format(total)}"
        tvPaymentStatus.text = "Enter cash amount"

        var cashValue = 0.0
        var paymentMethod = "Cash"

        fun updateStatus() {
            val change = viewModel.computeChange(cashValue)
            tvPaymentStatus.text = when {
                paymentMethod == "GCash" -> "GCash Payment"
                cashValue <= 0.0 -> "Enter cash amount"
                change < 0.0 -> "Insufficient Amount"
                change == 0.0 -> "Exact Payment"
                else -> "Change: ${CurrencyUtils.format(change)}"
            }
        }

        rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                rbCash.id -> {
                    paymentMethod = "Cash"
                    tilCash.visibility = android.view.View.VISIBLE
                    tilGCashRef.visibility = android.view.View.GONE
                }
                rbGCash.id -> {
                    paymentMethod = "GCash"
                    tilCash.visibility = android.view.View.GONE
                    tilGCashRef.visibility = android.view.View.VISIBLE
                    cashValue = total
                }
            }
            updateStatus()
        }

        etCash.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                cashValue = s?.toString()?.toDoubleOrNull() ?: 0.0
                updateStatus()
            }
        })

        updateStatus()

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Confirm", null)
            .create()

        // Set dialog background to match app dark theme
        dialog.window?.setBackgroundDrawableResource(R.color.pg_surface)

        dialog.setOnShowListener {
            val positive = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            val negative = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
            positive.isEnabled = false

            // Set button text colors to be visible on dark background
            positive.setTextColor(ContextCompat.getColor(this, R.color.pg_gold))
            negative.setTextColor(ContextCompat.getColor(this, R.color.pg_text_secondary))

            fun updateConfirmEnabled() {
                positive.isEnabled = when (paymentMethod) {
                    "GCash" -> total > 0.0
                    else -> cashValue >= total && total > 0.0
                }
            }

            etCash.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    cashValue = s?.toString()?.toDoubleOrNull() ?: 0.0
                    updateStatus()
                    updateConfirmEnabled()
                }
            })

            rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    rbCash.id -> {
                        paymentMethod = "Cash"
                        tilCash.visibility = android.view.View.VISIBLE
                        tilGCashRef.visibility = android.view.View.GONE
                    }
                    rbGCash.id -> {
                        paymentMethod = "GCash"
                        tilCash.visibility = android.view.View.GONE
                        tilGCashRef.visibility = android.view.View.VISIBLE
                        cashValue = total
                    }
                }
                updateStatus()
                updateConfirmEnabled()
            }

            updateConfirmEnabled()

            positive.setOnClickListener {
                viewModel.confirmPayment(
                    cashReceived = cashValue,
                    paymentMethod = paymentMethod,
                    onSuccess = {
                        dialog.dismiss()
                        Snackbar.make(binding.root, "Order saved", Snackbar.LENGTH_SHORT).show()
                    },
                    onError = { msg ->
                        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                    }
                )
            }
        }

        dialog.show()
    }
}