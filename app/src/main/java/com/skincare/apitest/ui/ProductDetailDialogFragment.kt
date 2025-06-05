package com.skincare.apitest.ui

import android.app.Dialog
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.skincare.apitest.databinding.DialogProductDetailBinding
import com.skincare.apitest.model.Product
import java.io.Serializable

class ProductDetailDialogFragment : DialogFragment() {

    private var _binding: DialogProductDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var product: Product

    private var listener: OnAddToCartClickListener? = null

    interface OnAddToCartClickListener {
        fun onAddToCartClicked(product: Product)
    }

    fun setOnAddToCartClickListener(listener: OnAddToCartClickListener) {
        this.listener = listener
    }

    companion object {
        private const val ARG_PRODUCT = "arg_product"

        fun newInstance(product: Product): ProductDetailDialogFragment {
            val fragment = ProductDetailDialogFragment()
            val args = Bundle()
            args.putSerializable(ARG_PRODUCT, product as Serializable)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            product = it.getSerializable(ARG_PRODUCT) as Product
        }
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.Theme_Material3_Light_Dialog_Alert)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        product.imageData?.let { imageData ->
            val base64Image = "data:image/png;base64,$imageData"
            Glide.with(binding.dialogProductImageView)
                .load(base64Image)
                .centerCrop()
                .into(binding.dialogProductImageView)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
