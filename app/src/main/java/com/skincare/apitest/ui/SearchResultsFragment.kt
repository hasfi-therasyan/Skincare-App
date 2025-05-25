package com.skincare.apitest.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.skincare.apitest.databinding.FragmentSearchResultsBinding
import com.skincare.apitest.viewmodel.ProductViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchResultsFragment : Fragment() {
    private var _binding: FragmentSearchResultsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProductViewModel
    private lateinit var searchAdapter: SearchResultsAdapter

    companion object {
        private const val ARG_QUERY = "search_query"

        fun newInstance(query: String): SearchResultsFragment {
            return SearchResultsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_QUERY, query)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity()).get(ProductViewModel::class.java)
        
        setupRecyclerView()
        
        // Get the search query
        val query = arguments?.getString(ARG_QUERY) ?: ""
        
        // Ensure data is loaded before searching
        ensureDataAndSearch(query)
    }

    private fun setupRecyclerView() {
        searchAdapter = SearchResultsAdapter { searchItem ->
            // Handle item click
            // You can add navigation to detail view here
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
        }
    }

    private fun ensureDataAndSearch(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            // Show loading state
            binding.progressBar.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            binding.noResultsText.visibility = View.GONE

            // Ensure products and packages are loaded and wait for success
            if (viewModel.productsState.value !is ApiResponse.Success) {
                viewModel.fetchProducts()
                viewModel.productsState.collect { state ->
                    if (state is ApiResponse.Success) return@collect
                }
            }

            if (viewModel.packageProductsState.value !is ApiResponse.Success) {
                viewModel.fetchPackages()
                viewModel.packageProductsState.collect { state ->
                    if (state is ApiResponse.Success) return@collect
                }
            }

            // Perform the search after data is loaded
            viewModel.searchProducts(query)

            // Observe search results
            viewModel.searchResults.collectLatest { results ->
                binding.progressBar.visibility = View.GONE
                
                if (results.isEmpty()) {
                    binding.recyclerView.visibility = View.GONE
                    binding.noResultsText.visibility = View.VISIBLE
                } else {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.noResultsText.visibility = View.GONE
                    searchAdapter.submitList(results)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
