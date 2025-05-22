package com.skincare.apitest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.skincare.apitest.databinding.ActivityCartBinding
import com.skincare.apitest.model.Product
import com.skincare.apitest.ui.CartAdapter
import com.skincare.apitest.ui.ProductDetailDialogFragment
import com.skincare.apitest.viewmodel.ProductViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CartActivity : AppCompatActivity(), ProductDetailDialogFragment.OnAddToCartClickListener {

    private lateinit var binding: ActivityCartBinding
    private lateinit var viewModel: ProductViewModel
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(ProductViewModel::class.java)

        // Removed setSupportActionBar to avoid conflict with window decor action bar
        // setSupportActionBar(binding.cartToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.cart)

        setupRecyclerView()
        observeCartItems()

        binding.checkoutButton.setOnClickListener {
            // Handle checkout button click
            // For now, just show a toast message
            android.widget.Toast.makeText(this, "Checkout clicked", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onDeleteClick = { product ->
                viewModel.removeFromCart(product)
            },
            onItemClick = { product ->
                // Show product detail dialog
                val dialog = ProductDetailDialogFragment.newInstance(product)
                dialog.setOnAddToCartClickListener(this)
                dialog.show(supportFragmentManager, "ProductDetailDialog")
            }
        )
        binding.cartRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
        }
    }

    private fun observeCartItems() {
        lifecycleScope.launch {
            viewModel.cartItems.collectLatest { cartItems ->
                val products = cartItems.map { it.product }
                cartAdapter.submitList(products)
                // Calculate total price
                val totalPrice = cartItems.sumOf { it.product.price }
                // Format total price as currency
                val formattedTotal = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("in", "ID")).format(totalPrice)
                // Update total price TextView
                binding.totalPriceTextView.text = "Total: $formattedTotal"
            }
        }
    }

    override fun onAddToCartClicked(product: Product) {
        viewModel.addToCart(product)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
