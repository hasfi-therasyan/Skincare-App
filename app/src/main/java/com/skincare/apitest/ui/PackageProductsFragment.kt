package com.skincare.apitest.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.skincare.apitest.databinding.FragmentIndividualProductsBinding
import com.skincare.apitest.model.ApiResponse
import com.skincare.apitest.model.ApiType
import com.skincare.apitest.model.PackageProduct
import com.skincare.apitest.viewmodel.ProductViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PackageProductsFragment : Fragment() {

    private var _binding: FragmentIndividualProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProductViewModel
    private lateinit var packageProductAdapter: PackageProductAdapter
    private var fullPackageList: List<PackageProduct> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIndividualProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(ProductViewModel::class.java)

        setupRecyclerView()
        setupSearchView()
        setupObservers()

        viewModel.fetchPackages()
    }

    private fun setupRecyclerView() {
        packageProductAdapter = PackageProductAdapter(
            onItemClick = { packageProduct ->
                // TODO: Implement package product detail dialog if needed
            },
            onCartClick = { packageProduct, selectedItems ->
                // Adapt viewModel method to accept selectedItems if needed
                viewModel.addPackageToCart(packageProduct, selectedItems)
            }
        )
        binding.productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = packageProductAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // No action on submit
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = if (newText.isNullOrBlank()) {
                    fullPackageList
                } else {
                    fullPackageList.filter {
                        it.packageName.contains(newText, ignoreCase = true)
                    }
                }
                packageProductAdapter.submitList(filteredList)
                return true
            }
        })
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.packageProductsState.collectLatest { response ->
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
                        fullPackageList = response.data
                        packageProductAdapter.submitList(fullPackageList)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
