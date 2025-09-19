package com.skincare.apitest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import android.widget.TextView
import com.skincare.apitest.model.Product
import com.skincare.apitest.ui.CartAdapter
import com.skincare.apitest.ui.ProductDetailDialogFragment
import com.skincare.apitest.viewmodel.ProductViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.skincare.apitest.repository.CartItem
import com.skincare.apitest.ui.CartPackageAdapter
import com.skincare.apitest.ui.CombinedCartAdapter


class CartActivity : AppCompatActivity(), ProductDetailDialogFragment.OnAddToCartClickListener {

    private lateinit var viewModel: ProductViewModel
    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartPackageAdapter: CartPackageAdapter
    
    // UI Components
    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var totalPriceTextView: TextView
    private lateinit var checkoutButton: MaterialButton
    private lateinit var cartToolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // Initialize UI components
        cartRecyclerView = findViewById(R.id.cartRecyclerView)
        totalPriceTextView = findViewById(R.id.totalPriceTextView)
        checkoutButton = findViewById(R.id.checkoutButton)
        cartToolbar = findViewById(R.id.cartToolbar)

        viewModel = ViewModelProvider(this).get(ProductViewModel::class.java)

        // Setup toolbar
        setSupportActionBar(cartToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Shopping Cart"

        setupRecyclerView()
        observeCartItems()

        checkoutButton.setOnClickListener {
            android.widget.Toast.makeText(this, "Checkout clicked", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        val combinedCartAdapter = CombinedCartAdapter(
            onDeleteProductClick = { product ->
                viewModel.removeFromCart(product)
            },
            onItemProductClick = { product ->
                val dialog = ProductDetailDialogFragment.newInstance(product)
                dialog.setOnAddToCartClickListener(this)
                dialog.show(supportFragmentManager, "ProductDetailDialog")
            },
            onDeletePackageClick = { packageProduct ->
                viewModel.removePackageFromCart(packageProduct)
            },
            onItemPackageClick = { packageProduct ->
                // TODO: Implement package product detail dialog if needed
            }
        )
        cartRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = combinedCartAdapter
        }
    }

    private fun observeCartItems() {
        lifecycleScope.launch {
            viewModel.cartItems.collectLatest { cartItems ->
                val combinedCartAdapter = cartRecyclerView.adapter as CombinedCartAdapter
                combinedCartAdapter.submitList(cartItems)

                // Calculate total price including both product types
                val totalPrice = cartItems.sumOf {
                    when (it) {
                        is CartItem.IndividualProduct -> it.product.price
                        is CartItem.PackageProductItem -> it.packageProduct.price
                    }
                }
                val formattedTotal = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("in", "ID")).format(totalPrice.toDouble())
                totalPriceTextView.text = "Total: $formattedTotal"
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
