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
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.skincare.apitest.model.Reseller
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import androidx.lifecycle.ViewModelProvider
import com.skincare.apitest.viewmodel.ProductViewModel
import kotlinx.coroutines.launch
import java.util.Locale

class ResellerMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var searchProvinceEditText: EditText
    private lateinit var btnZoomIn: ImageButton
    private lateinit var btnZoomOut: ImageButton

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

        // Load resellers when the activity is created
        loadResellers()

        // TODO: Setup search bar and observe resellers data
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableUserLocation()
        loadResellers()
        map.setOnMarkerClickListener(this)

        // Set initial view to Indonesia
        val indonesia = LatLng(-2.5489, 118.0149)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(indonesia, 5f))

        searchProvinceEditText = findViewById(R.id.search_province_edit_text)

        searchProvinceEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val provinceName = searchProvinceEditText.text.toString()
                searchProvince(provinceName)
                return@setOnEditorActionListener true
            }
            false
        }

        // Setup zoom buttons
        btnZoomIn = findViewById(R.id.btn_zoom_in)
        btnZoomOut = findViewById(R.id.btn_zoom_out)

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

    private fun loadResellers() {
        lifecycleScope.launch {
            viewModel.fetchResellers()
            viewModel.resellersState.collect { response ->
                when (response) {
                    is ApiResponse.Loading -> { }
                    is ApiResponse.Success -> {
                        if (response.data.isEmpty()) {
                            Toast.makeText(this@ResellerMapActivity, "No resellers found", Toast.LENGTH_SHORT).show()
                        } else {
                            response.data.forEach { reseller ->
                                println("Reseller data: $reseller") // Debug log
                                val marker = addResellerMarker(reseller)
                                if (marker?.tag == null) {
                                    Toast.makeText(this@ResellerMapActivity, "Failed to create marker for ${reseller.shopName}", Toast.LENGTH_SHORT).show()
                                } else {
                                    println("Marker created with tag: ${marker.tag}") // Debug log
                                }
                            }
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
        Log.d("ResellerDebug", """
            Reseller Data:
            ID: ${reseller.id}
            Shop: ${reseller.shopName}
            Name: ${reseller.resellerName}
            WhatsApp: ${reseller.whatsappNumber}
            Facebook: ${reseller.facebook}
            Instagram: ${reseller.instagram}
            City: ${reseller.city}
            Location: ${reseller.latitude},${reseller.longitude}
        """.trimIndent())

        val position = LatLng(reseller.latitude, reseller.longitude)
        val markerOptions = MarkerOptions()
            .position(position)
            .title(reseller.shopName)
            .snippet("Reseller: ${reseller.resellerName}\\nCity: ${reseller.city}")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        val marker = map.addMarker(markerOptions)
        marker?.tag = reseller
        return marker
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        println("Marker clicked with tag: ${marker.tag}") // Debug log
        val reseller = marker.tag as? Reseller
        if (reseller != null) {
            println("Reseller data from marker: $reseller") // Debug log
            showResellerDetailsDialog(reseller)
            return true
        } else if (marker.tag == "user_location") {
            Toast.makeText(this, "You are here", Toast.LENGTH_SHORT).show()
            return true
        }
        Toast.makeText(this, "Marker has no reseller data", Toast.LENGTH_SHORT).show()
        return false
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

        // Load profile picture if URL exists
        if (!reseller.profilePictureUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(reseller.profilePictureUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(profilePictureImageView)
        } else {
            profilePictureImageView.setImageResource(R.drawable.ic_launcher_background)
        }

        // Set text values with null checks
        shopNameTextView.text = if (!reseller.shopName.isNullOrEmpty()) {
            String.format("Shop Name: %s", reseller.shopName)
        } else {
            "Shop Name: Not available"
        }

        resellerNameTextView.text = if (!reseller.resellerName.isNullOrEmpty()) {
            String.format("Reseller Name: %s", reseller.resellerName)
        } else {
            "Reseller Name: Not available"
        }

        whatsappNumberTextView.text = if (!reseller.whatsappNumber.isNullOrEmpty()) {
            String.format("WhatsApp: %s", reseller.whatsappNumber)
        } else {
            "WhatsApp: Not available"
        }

        facebookTextView.text = if (!reseller.facebook.isNullOrEmpty()) {
            String.format("Facebook: %s", reseller.facebook)
        } else {
            "Facebook: Not available"
        }

        instagramTextView.text = if (!reseller.instagram.isNullOrEmpty()) {
            String.format("Instagram: %s", reseller.instagram)
        } else {
            "Instagram: Not available"
        }

        cityTextView.text = if (!reseller.city.isNullOrEmpty()) {
            String.format("City: %s", reseller.city)
        } else {
            "City: Not available"
        }

        builder.setTitle("Reseller Details")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
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
