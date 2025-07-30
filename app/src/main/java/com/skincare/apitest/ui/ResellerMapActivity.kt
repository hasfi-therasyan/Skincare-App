package com.skincare.apitest.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.TextView
import com.google.android.gms.maps.model.MarkerOptions
import com.skincare.apitest.R
import com.skincare.apitest.model.ApiResponse
import android.location.Location
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.skincare.apitest.model.Reseller
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.ViewModelProvider
import com.skincare.apitest.viewmodel.ProductViewModel
import kotlinx.coroutines.launch
import java.util.Locale

class ResellerMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var searchProvinceEditText: EditText
    private lateinit var btnZoomIn: ImageButton
    private lateinit var btnZoomOut: ImageButton

    private lateinit var searchTypeSpinner: Spinner
    private lateinit var searchQueryEditText: EditText
    private lateinit var searchButton: Button

    private lateinit var map: GoogleMap
    private lateinit var viewModel: ProductViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            enableUserLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reseller_map)

        viewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        searchProvinceEditText = findViewById(R.id.search_province_edit_text)
        btnZoomIn = findViewById(R.id.btn_zoom_in)
        btnZoomOut = findViewById(R.id.btn_zoom_out)
        searchTypeSpinner = findViewById(R.id.search_type_spinner)
        searchQueryEditText = findViewById(R.id.search_query_edit_text)
        searchButton = findViewById(R.id.search_button)

        setupSearchSpinner()
        setupSearchFunctionality()

        loadLimitedResellers()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableUserLocation()
        loadLimitedResellers()
        map.setOnMarkerClickListener(this)

        val indonesia = LatLng(-2.5489, 118.0149)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(indonesia, 5f))

        searchProvinceEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val provinceName = searchProvinceEditText.text.toString()
                searchProvince(provinceName)
                return@setOnEditorActionListener true
            }
            false
        }

        btnZoomIn.setOnClickListener {
            map.animateCamera(CameraUpdateFactory.zoomIn())
        }

        btnZoomOut.setOnClickListener {
            map.animateCamera(CameraUpdateFactory.zoomOut())
        }
    }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        } else {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun loadLimitedResellers() {
        lifecycleScope.launch {
            viewModel.fetchLimitedResellers()
            viewModel.limitedResellersState.collect { response ->
                when (response) {
                    is ApiResponse.Loading -> { }
                    is ApiResponse.Success -> {
                        response.data.forEach { reseller ->
                            addResellerMarker(reseller)
                        }
                    }
                    is ApiResponse.Error -> {
                        Toast.makeText(this@ResellerMapActivity, "Failed to load resellers", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun addResellerMarker(reseller: Reseller): Marker? {
        val position = LatLng(reseller.latitude, reseller.longitude)
        val markerOptions = MarkerOptions()
            .position(position)
            .title(reseller.shopName)
            .snippet("Reseller: ${reseller.resellerName}\nCity: ${reseller.city}")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        val marker = map.addMarker(markerOptions)
        marker?.tag = reseller
        return marker
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val reseller = marker.tag as? Reseller
        if (reseller != null) {
            showResellerDetailsDialog(reseller)
            return true
        }
        return false
    }

    private fun setupSearchSpinner() {
        val searchOptions = arrayOf("Reseller Name", "City")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, searchOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        searchTypeSpinner.adapter = adapter
    }

    private fun setupSearchFunctionality() {
        searchButton.setOnClickListener {
            val query = searchQueryEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                val selectedSearchType = searchTypeSpinner.selectedItem.toString()
                performSearch(query, selectedSearchType)
            } else {
                Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show()
            }
        }

        searchQueryEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchQueryEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    val selectedSearchType = searchTypeSpinner.selectedItem.toString()
                    performSearch(query, selectedSearchType)
                }
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun performSearch(query: String, searchType: String) {
        lifecycleScope.launch {
            when (searchType) {
                "Reseller Name" -> viewModel.searchResellersByName(query)
                "City" -> viewModel.searchResellersByCity(query)
            }

            viewModel.searchResultsState.collect { response ->
                when (response) {
                    is ApiResponse.Loading -> {}
                    is ApiResponse.Success -> {
                        showSearchResultsDialog(response.data, searchType, query)
                    }
                    is ApiResponse.Error -> {
                        Toast.makeText(this@ResellerMapActivity, "Search failed: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showSearchResultsDialog(results: List<Reseller>, searchType: String, query: String) {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_search_results, null)
        builder.setView(dialogView)

        val titleTextView = dialogView.findViewById<TextView>(R.id.search_results_title)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.search_results_recycler_view)
        val noResultsText = dialogView.findViewById<TextView>(R.id.no_results_text)

        titleTextView.text = "Search Results for \"$query\" in $searchType"

        if (results.isEmpty()) {
            recyclerView.visibility = android.view.View.GONE
            noResultsText.visibility = android.view.View.VISIBLE
        } else {
            recyclerView.visibility = android.view.View.VISIBLE
            noResultsText.visibility = android.view.View.GONE
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = SearchResultsAdapter(results) { reseller ->
                val position = LatLng(reseller.latitude, reseller.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
                showResellerDetailsDialog(reseller)
            }
        }

        builder.setTitle("Search Results")
            .setNegativeButton("Close") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }

    private fun showResellerDetailsDialog(reseller: Reseller) {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_reseller_detail, null)
        builder.setView(dialogView)

        val profilePictureImageView = dialogView.findViewById<ImageView>(R.id.profile_picture)
        val shopNameTextView = dialogView.findViewById<TextView>(R.id.shop_name)
        val resellerNameTextView = dialogView.findViewById<TextView>(R.id.reseller_name)
        val whatsappNumberTextView = dialogView.findViewById<TextView>(R.id.whatsapp_number)
        val facebookTextView = dialogView.findViewById<TextView>(R.id.facebook)
        val instagramTextView = dialogView.findViewById<TextView>(R.id.instagram)
        val cityTextView = dialogView.findViewById<TextView>(R.id.city)

        Glide.with(this)
            .load(reseller.profilePictureUrl ?: "")
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(profilePictureImageView)

        shopNameTextView.text = "Shop Name: ${reseller.shopName ?: "Not available"}"
        resellerNameTextView.text = "Reseller Name: ${reseller.resellerName ?: "Not available"}"
        whatsappNumberTextView.text = "WhatsApp: ${reseller.whatsappNumber ?: "Not available"}"
        facebookTextView.text = "Facebook: ${reseller.facebook ?: "Not available"}"
        instagramTextView.text = "Instagram: ${reseller.instagram ?: "Not available"}"
        cityTextView.text = "City: ${reseller.city ?: "Not available"}"

        builder.setTitle("Reseller Details")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }

    private fun searchProvince(provinceName: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(provinceName, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val location = addresses[0]
                val latLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
            } else {
                Toast.makeText(this, "Province not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error searching province", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
class SearchResultsAdapter(
    private val resellers: List<Reseller>,
    private val onItemClick: (Reseller) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    inner class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val profilePicture: ImageView = view.findViewById(R.id.profile_picture)
        val shopName: TextView = view.findViewById(R.id.shop_name)
        val resellerName: TextView = view.findViewById(R.id.reseller_name)
        val city: TextView = view.findViewById(R.id.city)

        init {
            view.setOnClickListener {
                val reseller = resellers[adapterPosition]
                onItemClick(reseller)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reseller = resellers[position]

        holder.shopName.text = reseller.shopName ?: "Unknown"
        holder.resellerName.text = reseller.resellerName ?: "Unknown"
        holder.city.text = reseller.city ?: "City not available"

        if (!reseller.profilePictureUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(reseller.profilePictureUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.profilePicture)
        } else {
            holder.profilePicture.setImageResource(R.drawable.ic_launcher_background)
        }
    }

    override fun getItemCount(): Int = resellers.size
}

