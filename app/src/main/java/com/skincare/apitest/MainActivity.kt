package com.skincare.apitest

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.skincare.apitest.databinding.ActivityMainBinding
import com.skincare.apitest.model.ApiResponse
import com.skincare.apitest.model.ApiType
import com.skincare.apitest.ui.ProductAdapter
import com.skincare.apitest.viewmodel.ProductViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.view.Menu
import android.view.MenuItem
import android.content.Intent
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.skincare.apitest.ui.ProductDetailDialogFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ProductViewModel
    private lateinit var productAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(ProductViewModel::class.java)

        // Remove setSupportActionBar call to avoid conflict with window decor action bar
        // setSupportActionBar(binding.topAppBar)

        // Get selected API type from Intent extras
        val selectedApiTypeName = intent.getStringExtra("selectedApiType")
        val selectedApiType = when (selectedApiTypeName) {
            "RETROFIT" -> ApiType.RETROFIT
            "GRAPHQL" -> ApiType.GRAPHQL
            else -> ApiType.RETROFIT
        }
        viewModel.setApiType(selectedApiType)
        viewModel.clearCurrentImage()

        // Automatically fetch products on startup
        viewModel.fetchProducts()

        setupRecyclerView()
        setupObservers()
    }

    private var cartBadge: BadgeDrawable? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val cartItem = menu?.findItem(R.id.action_cart)
        val actionView = cartItem?.actionView
        val cartIcon = actionView?.findViewById<View>(R.id.cart_icon)

        cartBadge = BadgeDrawable.create(this)
        cartBadge?.isVisible = false
        cartIcon?.let {
            @OptIn(com.google.android.material.badge.ExperimentalBadgeUtils::class)
            @Suppress("UnsafeOptInUsageError")
            BadgeUtils.attachBadgeDrawable(cartBadge!!, it)
            actionView.setOnClickListener {
                onOptionsItemSelected(cartItem)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                // Navigate to CartActivity
                val intent = Intent(this, CartActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            onItemClick = { product ->
                // Show product detail dialog
                val dialog = ProductDetailDialogFragment.newInstance(product)
                dialog.setOnAddToCartClickListener(object : ProductDetailDialogFragment.OnAddToCartClickListener {
                    override fun onAddToCartClicked(product: com.skincare.apitest.model.Product) {
                        viewModel.addToCart(product)
                    }
                })
                dialog.show(supportFragmentManager, "ProductDetailDialog")
            },
            onCartClick = { product ->
                viewModel.addToCart(product)
            }
        )
        binding.productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = productAdapter
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.productsState.collectLatest { response ->
                when (response) {
                    is ApiResponse.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.productsRecyclerView.visibility = View.GONE
                        binding.errorTextView.visibility = View.GONE
                    }
                    is ApiResponse.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.productsRecyclerView.visibility = View.VISIBLE
                        binding.errorTextView.visibility = View.GONE
                        productAdapter.submitList(response.data)
                    }
                    is ApiResponse.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.productsRecyclerView.visibility = View.GONE
                        binding.errorTextView.visibility = View.VISIBLE
                        binding.errorTextView.text = response.message
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.currentImageState.collectLatest { response ->
                when (response) {
                    is ApiResponse.Loading -> {
                        Toast.makeText(this@MainActivity, "Loading image...", Toast.LENGTH_SHORT).show()
                    }
                    is ApiResponse.Success -> {
                        Glide.with(this@MainActivity)
                            .load(response.data)
                            .into(binding.productImageView)
                    }
                    is ApiResponse.Error -> {
                        Toast.makeText(this@MainActivity, "Failed to load image: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                    null -> {
                        binding.productImageView.setImageDrawable(null)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.cartItemCount.collectLatest { count ->
                cartBadge?.let { badge ->
                    badge.isVisible = count > 0
                    badge.number = count
                }
            }
        }
    }
}
