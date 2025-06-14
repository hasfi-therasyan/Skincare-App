package com.skincare.apitest.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import androidx.lifecycle.ViewModelProvider
import com.skincare.apitest.viewmodel.ProductViewModel
import kotlinx.coroutines.launch
import java.util.Locale

class ResellerMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var searchProvinceEditText: EditText

    private lateinit var map: GoogleMap
    private lateinit var viewModel: ProductViewModel
    private var userMarker: Marker? = null
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

        searchProvinceEditText = findViewById(R.id.search_province_edit_text)

        searchProvinceEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val provinceName = searchProvinceEditText.text.toString()
                searchProvince(provinceName)
                return@setOnEditorActionListener true
            }
            false
        }
        addUserLocationMarker()
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

    private fun addResellerMarker(reseller: Reseller) {
        val position = LatLng(reseller.latitude, reseller.longitude)
        val markerOptions = MarkerOptions()
            .position(position)
            .title(reseller.shopName)
            .snippet("Reseller: ${reseller.resellerName}\\nCity: ${reseller.city}")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        val marker = map.addMarker(markerOptions)
        marker?.tag = reseller
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val reseller = marker.tag as? Reseller
        if (reseller != null) {
            showResellerDetailsDialog(reseller)
            return true
        } else if (marker.tag == "user_location") {
            Toast.makeText(this, "You are here", Toast.LENGTH_SHORT).show()
            return true
        }
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

        Glide.with(this)
            .load(reseller.profilePictureUrl)
            .placeholder(R.drawable.ic_launcher_background) // Replace with your placeholder image
            .error(R.drawable.ic_launcher_background) // Replace with your error image
            .into(profilePictureImageView)

        shopNameTextView.text = String.format("Shop Name: %s", reseller.shopName)
        resellerNameTextView.text = String.format("Reseller Name: %s", reseller.resellerName)
        whatsappNumberTextView.text = String.format("WhatsApp: %s", reseller.whatsappNumber ?: "N/A")
        facebookTextView.text = String.format("Facebook: %s", reseller.facebook ?: "N/A")
        instagramTextView.text = String.format("Instagram: %s", reseller.instagram ?: "N/A")
        cityTextView.text = String.format("City: %s", reseller.city ?: "N/A")

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

    private fun addUserLocationMarker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    val markerOptions = MarkerOptions()
                        .position(userLatLng)
                        .title("You are here")
                    val marker = map.addMarker(markerOptions)
                    marker?.tag = "user_location"
                }
            }
    }
}
