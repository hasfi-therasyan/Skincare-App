package com.skincare.apitest.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.skincare.apitest.R
import com.skincare.apitest.databinding.DialogPackageDetailBinding
import com.skincare.apitest.model.PackageProduct
import java.io.Serializable

class PackageDetailDialogFragment : DialogFragment() {

    private var _binding: DialogPackageDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var packageProduct: PackageProduct
    private val selectedItems = mutableSetOf<String>()
    private val itemCheckBoxes = mutableMapOf<String, CheckBox>()

    companion object {
        private const val ARG_PACKAGE_PRODUCT = "arg_package_product"
        private const val MAX_SELECTION = 3

        fun newInstance(packageProduct: PackageProduct): PackageDetailDialogFragment {
            val fragment = PackageDetailDialogFragment()
            val args = Bundle()
            args.putSerializable(ARG_PACKAGE_PRODUCT, packageProduct as Serializable)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            packageProduct = it.getSerializable(ARG_PACKAGE_PRODUCT) as PackageProduct
        }
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.Theme_Material3_Light_Dialog_Alert)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPackageDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPackageDetails()
    }

    private fun setupPackageDetails() {

        // Load package image
        packageProduct.imageData?.let { imageUrl ->
            Glide.with(binding.dialogPackageImageView)
                .load(imageUrl)
                .centerCrop()
                .into(binding.dialogPackageImageView)
        }
    }



    private fun showMaxSelectionMessage() {
        android.widget.Toast.makeText(
            context,
            "You can select up to $MAX_SELECTION items maximum",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun showSelectItemsMessage() {
        android.widget.Toast.makeText(
            context,
            "Please select at least one item",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}