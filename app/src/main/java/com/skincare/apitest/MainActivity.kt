package com.skincare.apitest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.skincare.apitest.model.ApiType
import com.skincare.apitest.performance.PerformanceTestActivity
import com.skincare.apitest.repository.CartRepository
import com.skincare.apitest.ui.IndividualProductsFragment
import com.skincare.apitest.ui.PackageProductsFragment
import com.skincare.apitest.viewmodel.ProductViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ProductViewModel
    private lateinit var cartButton: FloatingActionButton
    private lateinit var cartBadge: TextView
    private lateinit var resellerMapButton: FloatingActionButton
    private lateinit var performanceTestButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize buttons
        cartButton = findViewById(R.id.cartButton)
        cartBadge = findViewById(R.id.cartBadge)
        resellerMapButton = findViewById(R.id.resellerMapButton)
        performanceTestButton = findViewById(R.id.performanceTestButton)

        // Get selected API type from intent
        val selectedApiType = intent.getStringExtra("selectedApiType")
        val apiType = if (selectedApiType == "GRAPHQL") ApiType.GRAPHQL else ApiType.RETROFIT

        // Initialize ViewModel and Repository
        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        viewModel.setApiType(apiType)

        // Load initial data
        viewModel.fetchProducts()
        viewModel.fetchPackages()

        // Set up bottom navigation
        setupBottomNavigation()

        // Set up button click listeners
        setupButtonListeners()

        // Set title based on API type
        title = "Skincare App - ${apiType.name}"
    }

    override fun onResume() {
        super.onResume()
        updateCartBadge()
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_individual_products -> {
                    loadFragment(IndividualProductsFragment())
                    true
                }
                R.id.nav_package_products -> {
                    loadFragment(PackageProductsFragment())
                    true
                }
                else -> false
            }
        }

        // Load default fragment
        loadFragment(IndividualProductsFragment())
    }

    private fun setupButtonListeners() {
        // Cart button
        cartButton.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        // Reseller Map button
        resellerMapButton.setOnClickListener {
            val intent = Intent(this, com.skincare.apitest.ui.ResellerMapActivity::class.java)
            startActivity(intent)
        }

        // Performance Test button
        performanceTestButton.setOnClickListener {
            val intent = Intent(this, PerformanceTestActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun updateCartBadge() {
        val cartItemCount = CartRepository.cartItemCount.value
        
        if (cartItemCount > 0) {
            cartBadge.text = cartItemCount.toString()
            cartBadge.visibility = View.VISIBLE
        } else {
            cartBadge.visibility = View.GONE
        }
    }
}
