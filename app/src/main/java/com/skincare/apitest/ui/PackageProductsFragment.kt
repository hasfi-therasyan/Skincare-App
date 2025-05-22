package com.skincare.apitest.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        setupObservers()

        viewModel.fetchPackages()
    }

    private fun setupRecyclerView() {
        packageProductAdapter = PackageProductAdapter(
            onItemClick = { packageProduct ->
                // TODO: Implement package product detail dialog if needed
            },
            onCartClick = { packageProduct ->
                viewModel.addPackageToCart(packageProduct)
            }
        )
        binding.productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = packageProductAdapter
        }
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
                        packageProductAdapter.submitList(response.data)
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
