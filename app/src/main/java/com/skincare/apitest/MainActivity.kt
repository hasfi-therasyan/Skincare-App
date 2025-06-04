package com.skincare.apitest

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.tabs.TabLayoutMediator
import com.skincare.apitest.databinding.ActivityMainBinding
import com.skincare.apitest.model.ApiResponse
import com.skincare.apitest.model.ApiType
import com.skincare.apitest.ui.IndividualProductsFragment
import com.skincare.apitest.ui.PackageProductsFragment
import com.skincare.apitest.ui.ProductDetailDialogFragment
import com.skincare.apitest.viewmodel.ProductViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ProductViewModel

    private var cartBadge: BadgeDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(ProductViewModel::class.java)

        // Get selected API type from Intent extras
        val selectedApiTypeName = intent.getStringExtra("selectedApiType")
        val selectedApiType = when (selectedApiTypeName) {
            "RETROFIT" -> ApiType.RETROFIT
            "GRAPHQL" -> ApiType.GRAPHQL
            else -> ApiType.RETROFIT
        }
        viewModel.setApiType(selectedApiType)
        viewModel.clearCurrentImage()

        setupViewPagerWithTabs()

        setupObservers()
    }

    private fun setupViewPagerWithTabs() {
        val fragments = listOf(
            IndividualProductsFragment(),
            PackageProductsFragment()
        )
        val titles = listOf("Individual Products", "Package Products")

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.cartItemCount.collectLatest { count ->
                cartBadge?.let { badge ->
                    badge.isVisible = count > 0
                    badge.number = count
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(com.skincare.apitest.R.menu.menu_main, menu)
        val cartItem = menu?.findItem(com.skincare.apitest.R.id.action_cart)
        val actionView = cartItem?.actionView
        val cartIcon = actionView?.findViewById<View>(com.skincare.apitest.R.id.cart_icon)

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
            com.skincare.apitest.R.id.action_cart -> {
                // Navigate to CartActivity
                val intent = Intent(this, com.skincare.apitest.CartActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
