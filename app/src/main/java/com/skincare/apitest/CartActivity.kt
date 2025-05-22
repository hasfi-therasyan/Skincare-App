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
import com.skincare.apitest.repository.CartItem
import com.skincare.apitest.ui.CartPackageAdapter


class CartActivity : AppCompatActivity(), ProductDetailDialogFragment.OnAddToCartClickListener {

    private lateinit var binding: ActivityCartBinding
    private lateinit var viewModel: ProductViewModel
    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartPackageAdapter: CartPackageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(ProductViewModel::class.java)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.cart)

        setupRecyclerView()
        observeCartItems()

        binding.checkoutButton.setOnClickListener {
            android.widget.Toast.makeText(this, "Checkout clicked", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onDeleteClick = { product ->
                viewModel.removeFromCart(product)
            },
            onItemClick = { product ->
                val dialog = ProductDetailDialogFragment.newInstance(product)
                dialog.setOnAddToCartClickListener(this)
                dialog.show(supportFragmentManager, "ProductDetailDialog")
            }
        )
        cartPackageAdapter = CartPackageAdapter(
            onDeleteClick = { packageProduct ->
                viewModel.removePackageFromCart(packageProduct)
            },
            onItemClick = { packageProduct ->
                // TODO: Implement package product detail dialog if needed
            }
        )
        binding.cartRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter // For simplicity, show individual products by default
        }
    }

    private fun observeCartItems() {
        lifecycleScope.launch {
            viewModel.cartItems.collectLatest { cartItems ->
                val products = cartItems.mapNotNull {
                    when (it) {
                        is CartItem.IndividualProduct -> it.product
                        else -> null
                    }
                }
                val packageProducts = cartItems.mapNotNull {
                    when (it) {
                        is CartItem.PackageProductItem -> it.packageProduct
                        else -> null
                    }
                }
                // For demonstration, show individual products and package products separately
                cartAdapter.submitList(products)
                cartPackageAdapter.submitList(packageProducts)

                // Calculate total price including both product types
                val totalPrice = cartItems.sumOf {
                    when (it) {
                        is CartItem.IndividualProduct -> it.product.price
                        is CartItem.PackageProductItem -> it.packageProduct.price
                    }
                }
                val formattedTotal = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("in", "ID")).format(totalPrice.toDouble())
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
