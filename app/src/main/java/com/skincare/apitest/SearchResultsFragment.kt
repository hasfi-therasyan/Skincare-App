package com.skincare.apitest

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
    private lateinit var adapter: SearchResultsAdapter

    companion object {
        private const val ARG_QUERY = "arg_query"
        fun newInstance(query: String): SearchResultsFragment {
            val fragment = SearchResultsFragment()
            val bundle = Bundle()
            bundle.putString(ARG_QUERY, query)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(ProductViewModel::class.java)

        adapter = SearchResultsAdapter { searchItem ->
            // Handle search item click (e.g., open detail dialog)
        }
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.searchRecyclerView.adapter = adapter

        val query = arguments?.getString(ARG_QUERY) ?: ""
        viewModel.searchProducts(query)

        lifecycleScope.launch {
            viewModel.searchResults.collectLatest { results ->
                if (results.isNotEmpty()) {
                    binding.noResultsTextView.visibility = View.GONE
                    adapter.submitList(results)
                    binding.searchRecyclerView.visibility = View.VISIBLE
                } else {
                    binding.noResultsTextView.visibility = View.VISIBLE
                    binding.searchRecyclerView.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
